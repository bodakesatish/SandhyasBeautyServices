package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentBillingBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentMode
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class BillEditorFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: BillEditorViewModel by viewModels()
    private lateinit var billingServicesAdapter: BillServiceListAdapter
    val args: BillEditorFragmentArgs by navArgs()

    // To store the actual enum values for easy lookup
    private lateinit var selectablePaymentModes: List<PaymentMode>
    private lateinit var selectableAppointmentModes: List<AppointmentStatus>
    private var isDiscountEditable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the appointmentId from navigation arguments
        // Only set if it's a non-zero ID (for edit mode)
        initData()
        setupPaymentModeDropdown()
        setupAppointmentStatusDropdown()
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        }
        setupHeader()
        setupListeners()
        setupRecyclerView()
        observeViewModel()
        setupFragmentResultListener()
        setupOtherDiscountField()

    }

    private fun initData() {
        // Get all enum values, then filter out UNKNOWN and PENDING for user selection
        selectablePaymentModes = PaymentMode.entries.filter {
            it != PaymentMode.UNKNOWN
        }
        selectableAppointmentModes = AppointmentStatus.entries.filter {
            it != AppointmentStatus.UNKNOWN
        }

    }

    private fun setupListeners() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveBill.setOnClickListener {
            val paymentNotes = binding.tilPaymentNotes.editText?.text.toString()
            viewModel.updateAppointmentDetail(paymentNotes)
        }

        binding.actvPaymentMode.setOnItemClickListener { parent, view, position, id ->
            val selectedPaymentModeEnum = selectablePaymentModes[position]
            viewModel.updatePaymentMode(selectedPaymentModeEnum)
        }
        binding.actvAppointmentStatus.setOnItemClickListener { parent, view, position, id ->
            val selectedAppointmentStatusEnum = selectableAppointmentModes[position]
            viewModel.updateAppointmentStatus(selectedAppointmentStatusEnum)
        }
        binding.etOtherDiscount.doAfterTextChanged { editable ->
            isValidDiscount()
        }
        binding.etOtherDiscount.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleDiscount()
                // Optionally, clear focus
//                true // Consume the event
            } else {
                false // Do not consume other events
            }
        }
        binding.tilOtherDiscount.setEndIconOnClickListener {
            isDiscountEditable = !isDiscountEditable
            updateDiscountEditState(isDiscountEditable)

            if (isDiscountEditable) {
                // Request focus and show keyboard
                binding.etOtherDiscount.requestFocus()
                binding.etOtherDiscount.setSelection(binding.etOtherDiscount.text!!.length)
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(binding.etOtherDiscount, InputMethodManager.SHOW_IMPLICIT)
            } else {
                if (binding.etOtherDiscount.text.toString().toInt() == 0) {
                    binding.etOtherDiscount.setText("")
                }
                // Hide keyboard if it was made non-editable by icon click
                hideKeyboard(binding.etOtherDiscount)
                handleDiscount()
            }
        }
    }

    private fun handleDiscount(): Boolean {
        val discountValueStr = binding.tilOtherDiscount.editText?.text.toString()
        val otherDiscount = discountValueStr.toDoubleOrNull() ?: 0.0
        return if (isValidDiscount()) {
            viewModel.setOtherDiscount(otherDiscount)
            // Optionally, clear focus
            binding.etOtherDiscount.clearFocus()
            isDiscountEditable = false
            updateDiscountEditState(false)
            hideKeyboard(binding.tilOtherDiscount)
            true
        } else {
            false
        }
    }

    private fun setupOtherDiscountField() {
        // Initial state: not editable, show edit icon
        updateDiscountEditState(editable = false)


    }

    private fun updateDiscountEditState(editable: Boolean) {
        binding.etOtherDiscount.isEnabled = editable
        if (editable) {
            binding.tilOtherDiscount.endIconDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_done_24)
            binding.tilOtherDiscount.setEndIconContentDescription("Done Editing Discount")
            // You could also change endIconMode here if needed, but changing drawable is often enough
            // binding.tilOtherDiscount.endIconMode = TextInputLayout.END_ICON_CUSTOM or END_ICON_CLEAR_TEXT etc.
        } else {
            binding.tilOtherDiscount.endIconDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_24)
            binding.tilOtherDiscount.setEndIconContentDescription("Edit Discount")
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isValidDiscount(): Boolean {
        val discountValueStr = binding.etOtherDiscount.text.toString()
        val otherDiscount = discountValueStr.toDoubleOrNull() ?: 0.0
        val discountInput =
            (viewModel.appointmentDetail.value?.servicesDiscount ?: 0.0) + otherDiscount
        val maxDiscount = (viewModel.appointmentDetail.value?.totalBillAmount
            ?: 0.0) - (viewModel.appointmentDetail.value?.servicesDiscount ?: 0.0)
        val calculatedDiscount: Double
        val finalPrice: Double

        calculatedDiscount =
            discountInput.coerceAtMost(viewModel.appointmentDetail.value?.totalBillAmount ?: 0.0)

        finalPrice =
            (viewModel.appointmentDetail.value?.totalBillAmount ?: 0.0) - calculatedDiscount

        var result =
            discountInput >= 0 && discountInput <= (viewModel.appointmentDetail.value?.totalBillAmount
                ?: 0.0)

        // Replace MAX_DISCOUNT_ALLOWED with actualSubtotal or another dynamic limit if needed
        if (!result) {
            binding.tilOtherDiscount.error = "Max other discount is $maxDiscount"
            result = false
        } else {
            binding.tilOtherDiscount.error = null // Clear error if all checks pass
        }

        binding.btnSaveBill.isEnabled = result

        return result
    }

    private fun setupRecyclerView() {
        billingServicesAdapter = BillServiceListAdapter { serviceItem ->
            // Handle click to edit discount for a service item
            val dialog = ApplyDiscountDialog.newInstance(serviceItem)
            dialog.show(childFragmentManager, ApplyDiscountDialog.TAG)
        }
        binding.rvServicesForBilling.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = billingServicesAdapter
            // Optional: Add ItemDecoration for dividers if not handled by item layout
            // addItemDecoration(MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

    }

    private fun setupHeader() {
        binding.headerGeneric.tvHeader.text = getString(R.string.billing_details)
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
//        viewModel.serviceItems.observe(viewLifecycleOwner) { items ->
//            billingServicesAdapter.submitList(items)
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.serviceDetailList.collect { list ->
                    billingServicesAdapter.submitList(list)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.appointmentDetail.collect { appointmentDetail ->
                    appointmentDetail?.let {
                        binding.tvValueSubtotal.text = viewModel.formatCurrency(it.totalBillAmount)
                        binding.tvValueTotalDiscount.text =
                            "- ${viewModel.formatCurrency(it.totalDiscount)}"
                        binding.tvValueTotalDiscount.visibility =
                            if (it.totalDiscount > 0) View.VISIBLE else View.GONE
                        if (it.otherDiscount.toInt() > 0) {
                            binding.etOtherDiscount.setText(it.otherDiscount.toInt().toString())
                        } else {
                            binding.etOtherDiscount.setText("")
                        }
                        binding.tvValueGrandTotal.text = viewModel.formatCurrency(it.netTotal)
                        binding.actvAppointmentStatus.setText(it.appointmentStatus.name, false)
                        binding.tilPaymentNotes.editText?.setText(it.paymentNotes)
                        binding.btnSaveBill.isEnabled =
                            it.totalDiscount >= 0 && it.totalDiscount <= it.totalBillAmount
                        binding.actvPaymentMode.setText(it.paymentMode.name, false)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe for save result (success or failure)
                viewModel.saveResult.collectLatest { success ->
                    if (success) {
                        showSnackBar("Bill saved successfully")
                        navigateToPreviousScreen()
                    } else {
                        showSnackBar("Bill failed to save")
                    }
                }

            }
        }

        // You might have other LiveData for customer name, date, etc.
        // binding.tvCustomerNameBilling.text = "Customer: Sample Name"
        // binding.tvBillingDate.text = "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            ApplyDiscountDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val serviceId = bundle.getInt(ApplyDiscountDialog.RESULT_SERVICE_ID, 0)
            val discountAmount =
                bundle.getDouble(ApplyDiscountDialog.RESULT_DISCOUNT_AMOUNT, 0.0)

            viewModel.updateServiceDiscount(serviceId, discountAmount)
        }
    }

    private fun navigateToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun showSnackBar(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupPaymentModeDropdown() {

        // Get the display names for the adapter
        val paymentModeDisplayNames = selectablePaymentModes.map { it.name }

        if (paymentModeDisplayNames.isEmpty()) {
            // Handle case where no selectable payment modes are available
            binding.tilPaymentMode.isEnabled = false
            binding.tilPaymentMode.hint = "No payment modes available" // Or some other indication
            return
        }

        // Create an ArrayAdapter
        // android.R.layout.simple_dropdown_item_1line is a standard layout for dropdown items
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            paymentModeDisplayNames
        )

        // Set the adapter to the AutoCompleteTextView
        // Note: You target the AutoCompleteTextView directly, not the TextInputLayout
        binding.actvPaymentMode.setAdapter(adapter)

//        // Optional: Set a default selected item if needed (e.g., the first item)
//        if (paymentModes.isNotEmpty()) {
//            binding.actvPaymentMode.setText(paymentModes[0], false) // Set text without filtering
//        }

    }

    private fun setupAppointmentStatusDropdown() {

        // Get the display names for the adapter
        val appointmentStatusDisplayNames = selectableAppointmentModes.map { it.name }

        if (appointmentStatusDisplayNames.isEmpty()) {
            // Handle case where no selectable appointment status are available
            binding.tilPaymentMode.isEnabled = false
            binding.tilPaymentMode.hint =
                "No Appointment status available" // Or some other indication
            return
        }

        // Create an ArrayAdapter
        // android.R.layout.simple_dropdown_item_1line is a standard layout for dropdown items
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            appointmentStatusDisplayNames
        )

        // Set the adapter to the AutoCompleteTextView
        // Note: You target the AutoCompleteTextView directly, not the TextInputLayout
        binding.actvAppointmentStatus.setAdapter(adapter)

//        // Optional: Set a default selected item if needed (e.g., the first item)
//        if (paymentModes.isNotEmpty()) {
//            binding.actvPaymentMode.setText(paymentModes[0], false) // Set text without filtering
//        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvServicesForBilling.adapter = null // Clear adapter to prevent memory leaks
        _binding = null
    }

}