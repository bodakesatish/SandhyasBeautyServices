package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.AppointmentSummary
import com.bodakesatish.sandhyasbeautyservices.BillData
import com.bodakesatish.sandhyasbeautyservices.ExcelExporterUtil
import com.bodakesatish.sandhyasbeautyservices.PdfGeneratorUtil
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.ServiceItem
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAppointmentDetailsBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.ServiceDiscountAdapter
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

/**
 * A [Fragment] that displays a summary of an appointment, including customer details,
 * appointment date/time, status, services, billing information, and notes.
 *
 * This fragment receives an `appointmentId` through navigation arguments and uses it
 * to fetch and display the relevant appointment details via the [AppointmentBillDetailViewModel].
 *
 * It allows users to:
 * - View appointment and billing details.
 * - Navigate to edit the appointment.
 * - Navigate to edit the services for the appointment.
 * - Navigate to the billing screen (either to proceed with billing or edit an existing bill).
 * - Navigate to view an invoice (if applicable).
 *
 * The fragment uses Hilt for dependency injection and Jetpack Navigation for screen transitions.
 * It observes LiveData/Flow from the ViewModel to update the UI dynamically.
 */
@AndroidEntryPoint
class AppointmentSummaryFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val args: AppointmentSummaryFragmentArgs by navArgs()

    val viewModel: AppointmentBillDetailViewModel by viewModels()

    private lateinit var billServiceAdapter: ServiceDiscountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppointmentDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()

        // Set the appointmentId from navigation arguments
        // Only set if it's a non-zero ID (for edit mode)
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        }

        setupListeners()
        setupObservers()
        setupFragmentResultListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        billServiceAdapter = ServiceDiscountAdapter()
        binding.rvServicesForBilling.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = billServiceAdapter
            // Optional: Add ItemDecoration for dividers if not handled by item layout
            // addItemDecoration(MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupListeners() {
        binding.btnEditAppointment.setOnClickListener {
            // Handle item click
            val action =
                AppointmentSummaryFragmentDirections.actionAppointmentBillDetailFragmentToNavigationCreateAppointment(
                    viewModel.appointmentIdFlow.value
                )
            findNavController().navigate(action)
        }
        binding.btnEditServices.setOnClickListener {
            // Handle item click
            val action =
                AppointmentSummaryFragmentDirections.actionAppointmentBillDetailFragmentToAddServicesToAppointmentFragment(
                    viewModel.appointmentIdFlow.value
                )
            findNavController().navigate(action)
//            findNavController().navigate(R.id.navigation_add_services_to_appointment)
        }
        binding.btnViewInvoice.setOnClickListener {
            // Navigate to Billing Screen (passing appointmentId)
            handleShareBillAsPdf()

        }

        binding.btnEditBill.setOnClickListener {
            // Navigate to Billing Screen for editing (passing appointmentId and perhaps billingId)
            // findNavController().navigate(R.id.action_fragmentAppointmentDetails_to_billingFragment, bundleOf("appointmentId" to appointmentId, "billingId" to billing?.id))
            // Handle item click
            val action =
                AppointmentSummaryFragmentDirections.actionAppointmentBillDetailFragmentToBillingFragment(
                    viewModel.appointmentIdFlow.value
                )
            findNavController().navigate(action)
        }
        binding.btnProceedToBilling.setOnClickListener {
            handleExportAppointmentsToExcel()
        }

        binding.btnDeleteAppointment.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title)) // Add to strings.xml: "Confirm Deletion"
            .setMessage(getString(R.string.confirm_delete_appointment_message)) // Add to strings.xml: "Are you sure you want to delete this appointment? This action cannot be undone."
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> // Add to strings.xml: "Cancel"
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, _ -> // Add to strings.xml: "Delete"
                viewModel.deleteCurrentAppointment()
                dialog.dismiss()
            }
            .show()
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appointmentDeletionStatus.collectLatest { success ->
                    if (success) {
                        Log.d(tag, "Appointment deleted successfully, navigating back.")
                        // Show a Toast or Snackbar for feedback (optional)
                        // Toast.makeText(context, "Appointment deleted", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() // Or navigate to a specific destination
                    } else {
                        // Deletion failed, show an error message (e.g., Snackbar)
                        // This might also be triggered if you emit false for "no valid ID" in ViewModel
                        Log.e(tag, "Failed to delete appointment.")
                        // Toast.makeText(context, "Failed to delete appointment", Toast.LENGTH_LONG).show()
                        // Or show a Snackbar:
                        // Snackbar.make(binding.root, "Error deleting appointment.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.serviceDetailList.collect { list ->
                    billServiceAdapter.submitList(list)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appointmentDetailFlow
                        .filterNotNull() // Ensure we only process non-null details
                        .collectLatest { detail ->
                            // Update UI fields with initial appointment details
                            binding.tvDetailCustomerName.text =
                                detail.customer?.firstName + " " + detail.customer?.lastName
                            binding.tvDetailAppointmentDatetime.text = DateHelper.getFormattedDate(
                                detail.appointment.appointmentDate,
                                "dd MMM yyyy"
                            ) + ", " + DateHelper.formatTime(detail.appointment.appointmentTime)

                            // --- Appointment Status ---
                            binding.tvDetailAppointmentStatus.text =
                                detail.appointment.appointmentStatus.name // Or a more user-friendly string from string resources

                            var services = ""
                            detail.serviceDetailsWithServices.map {
                                services =
                                    services.plus("• ${it.serviceName}  (₹${it.originalPrice.toInt()})\n")
                                Log.i(tag, services)
                            }
                            Log.i(tag, services)
                            binding.tvServiceWithPrice.text = services

                            // --- Billing Status ---
                            binding.tvDetailBillingSubtotal.text =
                                formatCurrency(detail.appointment.totalBillAmount)
                            binding.tvDetailBillingOtherDiscount.text =
                                "- ${viewModel.formatCurrency(detail.appointment.otherDiscount)}"
                            binding.tvDetailBillingTotalDiscount.text =
                                "- ${viewModel.formatCurrency(detail.appointment.totalDiscount)}"
                            binding.tvDetailBillingGrandTotal.text =
                                viewModel.formatCurrency(detail.appointment.netTotal)

                            binding.tvDetailNotesContent.text = detail.appointment.appointmentNotes
                            binding.tvDetailNoNotes.text = detail.appointment.paymentNotes
                        }
                }
            }
        }
    }

    private fun setupHeader() {
        binding.headerGeneric.tvHeader.text = getString(R.string.appointment_and_billing_details)
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFragmentResultListeners() {
        // Listen for the result from the SelectServicesDialogFragment
        setFragmentResultListener(EditAppointmentServicesFragment.REQUEST_KEY_SELECTED_SERVICES) { requestKey, bundle ->
            // Ensure we handle the correct request key
            if (requestKey == EditAppointmentServicesFragment.REQUEST_KEY_SELECTED_SERVICES) {
                val selectedServices =
                    bundle.getSerializable(EditAppointmentServicesFragment.BUNDLE_KEY_SELECTED_SERVICES) as? ArrayList<Service>
                selectedServices?.let {
                    Log.i(tag, "Received Selected Services from Dialog: $it")
                    // Update the ViewModel with the new list of selected services
                    // This assumes you have a function in your ViewModel to handle this update
                    it.map { service -> service.id }.toSet()
                    //  viewModel.updateCategoryServiceSelection(selectedServicesIds)
                }
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount) // For INR
    }

// Inside your Fragment or Activity (e.g., AppointmentDetailsFragment)

    fun getCurrentBillData(): BillData {
        // --- Sample Data - Replace with actual data retrieval from your UI elements & ViewModel ---

        // Sample Customer and Appointment Details (from your TextViews)
        val customerName = binding.tvDetailCustomerName.text.toString() // "Satish Bodake"
        val appointmentDateTime = binding.tvDetailAppointmentDatetime.text.toString() // "25 Dec 2023, 10:00 AM"
        val status = binding.tvDetailAppointmentStatus.text.toString() // "Pending Payment" or "Completed"

        // Sample Services (from your ll_services_summary_container or rv_services_for_billing)
        // If using rv_services_for_billing, you'd iterate through its adapter's data.
        // For simplicity, let's hardcode a few based on the layout.
        val services = mutableListOf<ServiceItem>()

        // Example 1: From the static TextView in ll_services_summary_container
        // You'd need to parse this if it's your only source, or ideally get structured data.
        // For now, assuming you have structured data for services.
        services.add(ServiceItem(name = "Haircut", price = 50.00, quantity = 1))
        services.add(ServiceItem(name = "Hair Coloring", price = 1200.00, quantity = 11))
        services.add(ServiceItem(name = "Manicure", price = 700.00, quantity = 1, discount = 50.00)) // Example with discount

        // Sample Billing Summary (from your TextViews in card_billing_details)
        // These should be calculated based on the services and any applied discounts
        var calculatedSubtotal = 0.0
        services.forEach { calculatedSubtotal += (it.price * it.quantity) }

        // Assuming otherDiscount is entered or calculated elsewhere
        val otherDiscountText = binding.tvDetailBillingOtherDiscount.text.toString()
            .replace("₹", "")
            .replace("-", "")
            .trim()
        val otherDiscount = otherDiscountText.toDoubleOrNull() ?: 0.0

        val totalDiscountOnServices = services.sumOf { it.discount }
        val calculatedTotalDiscount = totalDiscountOnServices + otherDiscount

        val calculatedGrandTotal = calculatedSubtotal - calculatedTotalDiscount

        // Construct the BillData object
        return BillData(
            customerName = customerName,
            appointmentDateTime = appointmentDateTime,
            status = status,
            services = services,
            subtotal = calculatedSubtotal, // e.g., binding.tvDetailBillingSubtotal.text.toString().replace("₹", "").toDoubleOrNull() ?: 0.0,
            otherDiscount = otherDiscount,
            totalDiscount = calculatedTotalDiscount, // e.g., binding.tvDetailBillingTotalDiscount.text.toString().replace("₹", "").replace("-", "").toDoubleOrNull() ?: 0.0,
            grandTotal = calculatedGrandTotal, // e.g., binding.tvDetailBillingGrandTotal.text.toString().replace("₹", "").toDoubleOrNull() ?: 0.0,
            companyName = "Sandhya's Beauty Services", // Or from a config/string resource
            companyAddress = "123 Salon Avenue, Beautytown, ST 45678", // Or from a config/string resource
            invoiceNumber = "INV-${System.currentTimeMillis()}" // Generate dynamically or from a persistent source
        )
    }


    private fun handleShareBillAsPdf() {
        // 1. Gather the current bill data
        val billData = getCurrentBillData() // Calls the function defined above

        // 2. Create PDF (Consider background thread for complex PDFs)
        // For this example, running on the main thread for simplicity.
        // Use lifecycleScope for coroutines if PdfGeneratorUtil.createBillPdf becomes a suspend function
        lifecycleScope.launch {
            // If createBillPdf is not a suspend function, you can call it directly.
            // If it becomes a suspend function (e.g., for I/O):
            // val pdfUri = withContext(Dispatchers.IO) {
            //     PdfGeneratorUtil.createBillPdf(requireContext(), billData)
            // }

            val pdfUri = PdfGeneratorUtil.createBillPdf(requireContext(), billData) // Assuming it's not suspend for now

            if (pdfUri != null) {
                // 3. Share the generated PDF
                sharePdf(pdfUri, billData.customerName) // Pass customer name for subject
            } else {
                Toast.makeText(requireContext(), "Error: Could not generate PDF bill.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sharePdf(pdfUri: Uri, customerName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice for $customerName from Sandhya's Beauty Services")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Crucial for FileProvider
        }
        try {
            startActivity(Intent.createChooser(shareIntent, "Share Bill PDF via"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No application found to share PDF files.", Toast.LENGTH_SHORT).show()
        }
    }

    // Inside your Fragment or Activity where you have the list of appointments

// Example: private lateinit var appointmentListViewModel: AppointmentListViewModel
// Assume appointmentListViewModel.appointmentSummaries.value contains List<AppointmentSummary>

    private fun handleExportAppointmentsToExcel() {
       // val summariesToExport: List<AppointmentSummary>? = /* Get your list of appointments here */
        // For example, from a ViewModel:
        // val summariesToExport = appointmentListViewModel.appointmentSummaries.value

        // Create some dummy data for now if you don't have live data source
        val dummySummaries = listOf(
            AppointmentSummary("A001", "Satish Bodake", "2023-12-25 10:00", "Haircut, Shave", 750.0, "Completed"),
            AppointmentSummary(
                "A002",
                "Jane Doe",
                "2023-12-26 14:30",
                "Manicure, Pedicure",
                1200.0,
                "Pending"
            ),
            AppointmentSummary("A003", "Peter Jones", "2023-12-27 09:00", "Facial", 900.0, "Completed")
        )
        // Replace dummySummaries with your actual data source in production
        // val currentSummaries = appointmentListViewModel.appointmentSummaries.value


        if (dummySummaries.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No appointment data to export.", Toast.LENGTH_SHORT).show()
            return
        }

        // IMPORTANT: Perform file operations on a background thread
        lifecycleScope.launch(Dispatchers.IO) { // Switch to IO dispatcher for file operations
            val excelFileUri = ExcelExporterUtil.exportAppointmentsToExcel(
                requireContext(),
                dummySummaries // Pass your actual list here
            )

            withContext(Dispatchers.Main) { // Switch back to Main thread for UI updates/Intent
                if (excelFileUri != null) {
                    // Option 1: Share the Excel file
                    shareExcelFile(excelFileUri)

                    // Option 2: Try to open the Excel file directly (less reliable as user might not have an app)
                    // openExcelFile(excelFileUri)

                    Toast.makeText(requireContext(), "Excel file generated: ${excelFileUri.path}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to export appointments to Excel.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareExcelFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // MIME type for .xlsx
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Appointment Summaries Export") // Optional
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No app found to share Excel files.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openExcelFile(uri: Uri) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) // Optional
        }
        try {
            startActivity(Intent.createChooser(viewIntent, "Open Excel File with..."))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No app found to open Excel files. Please install an Excel viewer.", Toast.LENGTH_LONG).show()
        }
    }

// Call handleExportAppointmentsToExcel() from your button's OnClickListener or menu item selection
// Example:
// binding.btnExportToExcel.setOnClickListener {
//     handleExportAppointmentsToExcel()
// }


}