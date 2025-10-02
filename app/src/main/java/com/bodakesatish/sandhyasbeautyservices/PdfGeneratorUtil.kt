package com.bodakesatish.sandhyasbeautyservices

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class to hold bill information (simplify as needed)
data class BillData(
    val customerName: String,
    val appointmentDateTime: String,
    val status: String,
    val services: List<ServiceItem>, // Define ServiceItem data class
    val subtotal: Double,
    val otherDiscount: Double,
    val totalDiscount: Double,
    val grandTotal: Double,
    val companyName: String = "Sandhya's Beauty Services", // Example
    val companyAddress: String = "123 Beauty St, Cityville", // Example
    val invoiceNumber: String = "INV-${System.currentTimeMillis()}" // Example
)

data class ServiceItem(
    val name: String,
    val price: Double,
    val quantity: Int = 1, // Add quantity if applicable
    val discount: Double = 0.0 // Discount per service item
) {
    val totalPrice: Double
        get() = (price * quantity) - discount
}


object PdfGeneratorUtil {

    private const val PAGE_WIDTH = 595 // A4 page width in points (approx 8.27 inches * 72 PPI)
    private const val PAGE_HEIGHT = 842 // A4 page height in points (approx 11.69 inches * 72 PPI)
    private const val MARGIN = 40f
    private const val LINE_SPACING = 18f // Adjust as needed
    private const val TEXT_SIZE_NORMAL = 12f
    private const val TEXT_SIZE_LARGE = 16f
    private const val TEXT_SIZE_SMALL = 10f

    fun createBillPdf(context: Context, billData: BillData): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = TEXT_SIZE_LARGE
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = TEXT_SIZE_NORMAL
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = TEXT_SIZE_NORMAL
            color = Color.DKGRAY
        }
        val smallTextPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = TEXT_SIZE_SMALL
            color = Color.GRAY
        }
        val totalPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = TEXT_SIZE_NORMAL
            color = Color.BLACK
        }
        val linePaint = Paint().apply {
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        var yPosition = MARGIN

        // --- Header ---
        canvas.drawText(billData.companyName, MARGIN, yPosition, titlePaint)
        yPosition += LINE_SPACING * 1.5f
        canvas.drawText(billData.companyAddress, MARGIN, yPosition, smallTextPaint)
        yPosition += LINE_SPACING
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Date: $date", MARGIN, yPosition, smallTextPaint)
        yPosition += LINE_SPACING * 2

        canvas.drawText("INVOICE / BILL", (PAGE_WIDTH / 2f) - titlePaint.measureText("INVOICE / BILL") / 2, yPosition, titlePaint.apply{textSize = 18f})
        yPosition += LINE_SPACING * 1.5f

        // --- Bill To & Appointment Info ---
        canvas.drawText("Bill To:", MARGIN, yPosition, headerPaint)
        val billToX = MARGIN + headerPaint.measureText("Bill To:") + 20f
        canvas.drawText(billData.customerName, billToX, yPosition, textPaint)

        val invoiceNumberText = "Invoice #: ${billData.invoiceNumber}"
        val invoiceNumberX = PAGE_WIDTH - MARGIN - textPaint.measureText(invoiceNumberText)
        canvas.drawText(invoiceNumberText, invoiceNumberX, yPosition, textPaint)
        yPosition += LINE_SPACING

        canvas.drawText("Appointment:", MARGIN, yPosition, headerPaint)
        val appointmentToX = MARGIN + headerPaint.measureText("Appointment:") + 20f
        canvas.drawText(billData.appointmentDateTime, appointmentToX, yPosition, textPaint)

        val statusText = "Status: ${billData.status}"
        val statusX = PAGE_WIDTH - MARGIN - textPaint.measureText(statusText)
        canvas.drawText(statusText, statusX, yPosition, textPaint)
        yPosition += LINE_SPACING * 2

        // --- Services Table Header ---
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
        yPosition += LINE_SPACING
        canvas.drawText("Service Description", MARGIN, yPosition, headerPaint)
        val priceHeaderX = PAGE_WIDTH - MARGIN - 150f // Adjust for price column
        canvas.drawText("Qty", priceHeaderX - 80f, yPosition, headerPaint.apply{textAlign = Paint.Align.RIGHT})
        canvas.drawText("Price", priceHeaderX, yPosition, headerPaint.apply{textAlign = Paint.Align.RIGHT})
        canvas.drawText("Total", PAGE_WIDTH - MARGIN, yPosition, headerPaint.apply{textAlign = Paint.Align.RIGHT})
        yPosition += LINE_SPACING * 0.5f
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
        yPosition += LINE_SPACING

        var num = 1
        // --- Services List ---
        billData.services.forEach { service ->
            val serviceNameToX = MARGIN + headerPaint.measureText(service.name)
            canvas.drawText(num.toString()+". "+service.name, MARGIN, yPosition,textPaint.apply{textAlign = Paint.Align.LEFT})
            canvas.drawText(service.quantity.toString(), priceHeaderX - 80f, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT})
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", service.price), priceHeaderX, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT})
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", service.totalPrice), PAGE_WIDTH - MARGIN, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT})
            yPosition += LINE_SPACING
            num++
        }
        yPosition += LINE_SPACING * 0.5f
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
        yPosition += LINE_SPACING

        // --- Totals Section ---
        val totalsStartX = PAGE_WIDTH - MARGIN - 200f // Start X for labels in totals

        // Subtotal
        canvas.drawText("Subtotal:", totalsStartX, yPosition, textPaint.apply{textAlign = Paint.Align.LEFT})
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", billData.subtotal), PAGE_WIDTH - MARGIN, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT})
        yPosition += LINE_SPACING

        // Other Discount
        if (billData.otherDiscount > 0) {
            canvas.drawText("Other Discount:", totalsStartX, yPosition, textPaint.apply{textAlign = Paint.Align.LEFT; color = Color.RED})
            canvas.drawText(String.format(Locale.getDefault(), "- ₹%.2f", billData.otherDiscount), PAGE_WIDTH - MARGIN, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT; color = Color.RED})
            yPosition += LINE_SPACING
        }

        // Total Discount (could be sum of service discounts + other discount)
        canvas.drawText("Total Discount:", totalsStartX, yPosition, textPaint.apply{textAlign = Paint.Align.LEFT; color = Color.RED})
        canvas.drawText(String.format(Locale.getDefault(), "- ₹%.2f", billData.totalDiscount), PAGE_WIDTH - MARGIN, yPosition, textPaint.apply{textAlign = Paint.Align.RIGHT; color = Color.RED})
        yPosition += LINE_SPACING
        canvas.drawLine(totalsStartX - 20, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
        yPosition += LINE_SPACING

        // Grand Total
        canvas.drawText("Grand Total:", totalsStartX, yPosition, totalPaint.apply{textAlign = Paint.Align.LEFT})
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", billData.grandTotal), PAGE_WIDTH - MARGIN, yPosition, totalPaint.apply{textAlign = Paint.Align.RIGHT})
        yPosition += LINE_SPACING * 2

        // --- Footer / Thank You ---
       // val thankYouText = "Thank you for your business!"
       // canvas.drawText(thankYouText, (PAGE_WIDTH / 2f) - textPaint.measureText(thankYouText)/2, yPosition, textPaint)

        pdfDocument.finishPage(page)

        // --- Save the PDF ---
        // Save to app-specific directory for better scoped storage compatibility
        val fileName = "Invoice_${billData.customerName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            // Use FileProvider to get a shareable URI
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}