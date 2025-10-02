package com.bodakesatish.sandhyasbeautyservices

import kotlin.concurrent.write
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppointmentSummary(
    val id: String,
    val customerName: String,
    val dateTime: String, // Or a Date/Timestamp object
    val servicesSummary: String, // e.g., "Haircut, Manicure"
    val totalAmount: Double,
    val status: String
)

object ExcelExporterUtil {

    fun exportAppointmentsToExcel(
        context: Context,
        appointments: List<AppointmentSummary>,
        fileNamePrefix: String = "Appointment_Summaries"
    ): Uri? {
        val workbook = XSSFWorkbook() // Create a .xlsx workbook
        val sheet: XSSFSheet = workbook.createSheet("Appointments")

        // 1. Create Header Row
        val headerRow: Row = sheet.createRow(0)
        val headers = listOf(
            "ID", "Customer Name", "Date & Time",
            "Services", "Total Amount", "Status"
        )
        headers.forEachIndexed { index, headerText ->
            val cell: Cell = headerRow.createCell(index)
            cell.setCellValue(headerText)
            // Optional: Style header cells (bold, background color, etc.)
            // val headerStyle = workbook.createCellStyle()
            // val font = workbook.createFont()
            // font.bold = true
            // headerStyle.setFont(font)
            // cell.cellStyle = headerStyle
        }

        // 2. Populate Data Rows
        appointments.forEachIndexed { rowIndex, summary ->
            val dataRow: Row = sheet.createRow(rowIndex + 1) // +1 because header is at row 0
            dataRow.createCell(0).setCellValue(summary.id)
            dataRow.createCell(1).setCellValue(summary.customerName)
            dataRow.createCell(2).setCellValue(summary.dateTime)
            dataRow.createCell(3).setCellValue(summary.servicesSummary)
            dataRow.createCell(4).setCellValue(summary.totalAmount) // POI handles double
            dataRow.createCell(5).setCellValue(summary.status)
        }

        // Optional: Auto-size columns (can be performance intensive for many columns/rows)
        // headers.indices.forEach { sheet.autoSizeColumn(it) }

        // 3. Save the Workbook
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = simpleDateFormat.format(Date())
        val fileName = "${fileNamePrefix}_$timestamp.xlsx"

        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Exports")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        return try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close() // Close the workbook
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
