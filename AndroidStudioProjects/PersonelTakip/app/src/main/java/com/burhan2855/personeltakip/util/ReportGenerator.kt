package com.burhan2855.personeltakip.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.burhan2855.personeltakip.data.Adjustment
import com.burhan2855.personeltakip.data.AdjustmentType
import com.burhan2855.personeltakip.data.Employee
import com.burhan2855.personeltakip.data.WorkLog
import com.burhan2855.personeltakip.logic.SalaryReport
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.VerticalAlignment
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ReportGenerator {

    private val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val tf = DateTimeFormatter.ofPattern("HH:mm")

    init {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory")
    }

    private fun getTurkishFont(): PdfFont {
        return try {
            val fontPath = "/system/fonts/Roboto-Regular.ttf"
            if (File(fontPath).exists()) {
                PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)
            } else {
                PdfFontFactory.createFont(StandardFonts.HELVETICA, "Cp1254")
            }
        } catch (e: Exception) {
            PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.WINANSI)
        }
    }

    private fun getTurkishFontBold(): PdfFont {
        return try {
            val fontPath = "/system/fonts/Roboto-Bold.ttf"
            if (File(fontPath).exists()) {
                PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)
            } else {
                PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, "Cp1254")
            }
        } catch (e: Exception) {
            PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, PdfEncodings.WINANSI)
        }
    }

    fun openMonthlySlipPdf(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0,
        isShare: Boolean = false
    ) {
        val file = generateMonthlySlipPdf(context, employee, report, adjustments, turnover)
        if (isShare) shareFile(context, file, "application/pdf")
        else viewFile(context, file, "application/pdf")
    }

    fun openMonthlySlipImage(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0
    ) {
        val pdfFile = generateMonthlySlipPdf(context, employee, report, adjustments, turnover)
        val imageFile = renderPdfToImage(context, pdfFile)
        if (imageFile != null) {
            shareFile(context, imageFile, "image/png")
        }
    }

    private fun renderPdfToImage(context: Context, pdfFile: File): File? {
        return try {
            val fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val page = renderer.openPage(0)
            
            // Render at high scale for quality (300 DPI approx)
            val scale = 3.0f 
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            val canvas = Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)
            
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            
            val imageFile = File(context.cacheDir, pdfFile.name.replace(".pdf", ".png"))
            val out = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateMonthlySlipPdf(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0
    ): File {
        val rangeStr = "${report.startDate.format(df)}-${report.endDate.format(df)}"
        val fileName = "${employee.firstName}_Maas_Bordrosu_${rangeStr}.pdf"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        val font = getTurkishFont()
        val fontBold = getTurkishFontBold()
        document.setFont(font)
        document.setMargins(20f, 30f, 20f, 30f)

        // Colors
        val bgPink = DeviceRgb(255, 235, 235)
        val textGreen = DeviceRgb(76, 175, 80)
        val textRed = DeviceRgb(244, 67, 54)
        val darkGray = DeviceRgb(66, 66, 66)

        // Header Table
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(12f, 88f))).setWidth(UnitValue.createPercentValue(70f))
        headerTable.setBorder(Border.NO_BORDER)
        headerTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
        
        // Placeholder Profile Circle - use actual image if available
        val profileCell = Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER)
        
        if (!employee.imageUri.isNullOrBlank()) {
            try {
                val imageData = com.itextpdf.io.image.ImageDataFactory.create(employee.imageUri)
                val image = com.itextpdf.layout.element.Image(imageData)
                image.setWidth(40f).setHeight(40f)
                profileCell.add(image)
            } catch (e: Exception) {
                profileCell.add(Paragraph("[P]").setFont(fontBold).setFontSize(18f).setFontColor(darkGray))
            }
        } else {
            profileCell.add(Paragraph("[P]").setFont(fontBold).setFontSize(18f).setFontColor(darkGray))
        }
        headerTable.addCell(profileCell)
        
        val infoCell = Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE)
        infoCell.add(Paragraph("${employee.firstName} ${employee.lastName}").setFont(fontBold).setFontSize(14f))
        
        val posSalaryPara = Paragraph()
        posSalaryPara.add("${employee.position}  ")
        val combinedSalary = (employee.hourlyRate * employee.dailyWorkingHours * 30) + employee.additionalSalary
        posSalaryPara.add(Paragraph(" Maaş: ${String.format("%,.0f", combinedSalary)} TL ")
            .setBackgroundColor(DeviceRgb(232, 245, 233))
            .setFontColor(textGreen)
            .setFontSize(7.5f)
            .setFont(fontBold))
        infoCell.add(posSalaryPara)
        
        val dateRangeStr = "Başlama: ${employee.startDate?.format(df) ?: "-"} " + (if(employee.endDate != null) " • Çıkış: ${employee.endDate.format(df)}" else "")
        infoCell.add(Paragraph(dateRangeStr).setFontSize(7.5f).setFontColor(darkGray))
        
        headerTable.addCell(infoCell)
        document.add(headerTable)
        
        // Report Date
        val reportDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        val reportDatePara = Paragraph("Rapor Tarihi: $reportDate")
            .setFont(font)
            .setFontSize(8f)
            .setFontColor(darkGray)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(reportDatePara)

        // IBAN Bar
        if (!employee.iban.isNullOrBlank()) {
            val ibanTable = Table(UnitValue.createPercentArray(floatArrayOf(100f))).setWidth(UnitValue.createPercentValue(70f)).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
            val ibanCell = Cell()
                .setBackgroundColor(DeviceRgb(250, 240, 240))
                .setPadding(8f)
                .setBorder(Border.NO_BORDER)
            
            ibanCell.add(Paragraph("🏦  IBAN").setFontSize(7.5f).setFontColor(darkGray).setFont(fontBold))
            ibanCell.add(Paragraph(employee.iban).setFontSize(10f).setFont(fontBold).setFontColor(darkGray))
            
            ibanTable.addCell(ibanCell)
            document.add(Paragraph(" "))
            document.add(ibanTable)
        }

        // Main Dashboard Card
        document.add(Paragraph(" "))
        val cardTable = Table(UnitValue.createPercentArray(floatArrayOf(65f, 35f))).setWidth(UnitValue.createPercentValue(70f))
        cardTable.setBackgroundColor(bgPink)
        cardTable.setPadding(8f)
        cardTable.setBorder(Border.NO_BORDER)
        cardTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
        
        // Top Summary Row
        val fixedBaseNetSalary = employee.hourlyRate * employee.dailyWorkingHours * 30
        val fixedAdditionalSalary = employee.additionalSalary
        val combinedMonthlySalary = fixedBaseNetSalary + fixedAdditionalSalary
        val grandTotal = report.totalEarnings + turnover

        cardTable.addCell(Cell().add(Paragraph("Sabit Net Maaş").setFont(font).setFontSize(8.5f).setFontColor(darkGray)).setBorder(Border.NO_BORDER))
        cardTable.addCell(Cell().add(Paragraph("${String.format("%,.2f", combinedMonthlySalary)} TL").setFont(fontBold).setFontSize(12f).setFontColor(darkGray).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER))
        
        cardTable.addCell(Cell().add(Paragraph("Top. Alacak (Genel)").setFont(font).setFontSize(8.5f).setFontColor(DeviceRgb(33, 150, 243))).setBorder(Border.NO_BORDER))
        cardTable.addCell(Cell().add(Paragraph("${String.format("%,.2f", grandTotal)} TL").setFont(fontBold).setFontSize(15f).setFontColor(DeviceRgb(33, 150, 243)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER))
        
        cardTable.addCell(Cell(1, 2).add(Paragraph(" ")).setBorderBottom(SolidBorder(DeviceRgb(200, 200, 200), 0.5f)).setBorder(Border.NO_BORDER))
        
        // Period
        cardTable.addCell(Cell(1, 2).add(Paragraph("Dönem: ${report.startDate.format(DateTimeFormatter.ofPattern("dd.MM"))} - ${report.endDate.format(DateTimeFormatter.ofPattern("dd.MM"))}").setFont(fontBold).setFontSize(8.5f).setFontColor(darkGray)).setBorder(Border.NO_BORDER).setPaddingTop(4f))
        
        // Detailed Rows
        addDashboardRow(cardTable, "Fazla Mesai (${String.format("%.1f", report.overtimeHours)} sa)", "+${String.format("%.2f", report.totalOvertimeEarnings)} TL", textGreen, font, darkGray)
        
        // Total Deductions Highlight
        val totalDeductions = report.unpaidLeaveDeduction + report.sickLeaveDeduction + report.missingHoursDeduction + report.totalAdjustments.avansAmount + report.totalAdjustments.kesintiAmount
        addDashboardRow(cardTable, "Top. Kesinti/Avans", "-${String.format("%.2f", totalDeductions)} TL", textRed, font, darkGray, isBold = true)
        
        if (report.sickLeaveDays > 0) addDashboardRow(cardTable, "Raporlu (${report.sickLeaveDays} gün)", "-${String.format("%.2f", report.sickLeaveDeduction)} TL", textRed, font, darkGray)
        if (report.missingHours > 0) addDashboardRow(cardTable, "Eksik Çalışma (${String.format("%.1f", report.missingHours)} sa)", "-${String.format("%.2f", report.missingHoursDeduction)} TL", textRed, font, darkGray)
        
        val adj = report.totalAdjustments
        if (adj.kesintiAmount > 0) addDashboardRow(cardTable, "Kesinti", "-${String.format("%.2f", adj.kesintiAmount)} TL", textRed, font, darkGray)
        if (adj.avansAmount > 0) addDashboardRow(cardTable, "Avans (${adj.avansCount} adet)", "-${String.format("%.2f", adj.avansAmount)} TL", textRed, font, darkGray)
        
        if (adj.yolAmount > 0) addDashboardRow(cardTable, "Yol (${adj.yolCount} adet)", "+${String.format("%.2f", adj.yolAmount)} TL", DeviceRgb(33, 150, 243), font, darkGray)
        if (adj.primAmount > 0) addDashboardRow(cardTable, "Prim (${adj.primCount} adet)", "+${String.format("%.2f", adj.primAmount)} TL", DeviceRgb(33, 150, 243), font, darkGray)
        if (report.totalMealAllowance > 0) addDashboardRow(cardTable, "Yemek Ücreti (Günlük)", "+${String.format("%.2f", report.totalMealAllowance)} TL", textGreen, font, darkGray)
        if (turnover != 0.0) addDashboardRow(cardTable, "Önceki Dönemden Devir", (if(turnover > 0) "+" else "") + String.format("%.2f TL", turnover), DeviceRgb(33, 150, 243), font, darkGray)
        
        // Divider
        cardTable.addCell(Cell(1, 2).add(Paragraph("\n")).setBorderBottom(SolidBorder(DeviceRgb(200, 200, 200), 0.5f)).setBorder(Border.NO_BORDER))
        
        // Ödeme Dağılımı
        // Bankadan Ödenecek = SADECE Aylık Net Maaş (Ek maaş dahil değil)
        val bankaOdenecek = fixedBaseNetSalary
        
        // Elden Ödenecek = Ek Maaş + Mesai + Yemek + Yol + Prim + Devir
        val eldenOdenecek = fixedAdditionalSalary + report.totalOvertimeEarnings + (report.totalMealAllowance + adj.yemekAmount) + (report.totalTransportAllowance + adj.yolAmount) + adj.primAmount + turnover
        
        cardTable.addCell(Cell().add(Paragraph("BANKADAN ÖDENECEK").setFont(fontBold).setFontSize(9f).setFontColor(DeviceRgb(0, 150, 136))).setBorder(Border.NO_BORDER))
        cardTable.addCell(Cell().add(Paragraph("${String.format("%,.2f", bankaOdenecek)} TL").setFont(fontBold).setFontSize(11f).setFontColor(DeviceRgb(0, 150, 136)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER))
        
        cardTable.addCell(Cell().add(Paragraph("ELDEN ÖDENECEK").setFont(fontBold).setFontSize(9f).setFontColor(DeviceRgb(255, 152, 0))).setBorder(Border.NO_BORDER))
        cardTable.addCell(Cell().add(Paragraph("${String.format("%,.2f", eldenOdenecek)} TL").setFont(fontBold).setFontSize(11f).setFontColor(DeviceRgb(255, 152, 0)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER))
        
        cardTable.addCell(Cell(1, 2).add(Paragraph(" ")).setBorderBottom(SolidBorder(DeviceRgb(200, 200, 200), 0.5f)).setBorder(Border.NO_BORDER))
        
        // Remaining Balance
        val remaining = grandTotal - adj.totalPaymentAmount
        cardTable.addCell(Cell().add(Paragraph("GÜNCEL BAKİYE (KALAN)").setFont(fontBold).setFontSize(10f).setFontColor(darkGray)).setBorder(Border.NO_BORDER).setPaddingTop(4f))
        cardTable.addCell(Cell().add(Paragraph("${String.format("%,.2f", remaining)} TL").setFont(fontBold).setFontSize(13f).setFontColor(darkGray).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER).setPaddingTop(4f))
        
        // Leave Status
        cardTable.addCell(Cell(1, 2).add(Paragraph("İzin Durumu").setFont(fontBold).setFontSize(8.5f).setFontColor(darkGray)).setBorder(Border.NO_BORDER).setPaddingTop(4f))
        val leaveStatusTable = Table(UnitValue.createPercentArray(floatArrayOf(33f, 33f, 33f))).useAllAvailableWidth()
        leaveStatusTable.setBorder(Border.NO_BORDER)
        
        addLeaveCell(leaveStatusTable, "Ücretli", report.paidLeaveDays.toString(), font, fontBold, darkGray)
        addLeaveCell(leaveStatusTable, "Ücretsiz", report.unpaidLeaveDays.toString(), font, fontBold, darkGray)
        addLeaveCell(leaveStatusTable, "Rapor", report.sickLeaveDays.toString(), font, fontBold, darkGray)
        
        val leaveCell = Cell(1, 2).add(leaveStatusTable).setBorder(Border.NO_BORDER)
        cardTable.addCell(leaveCell)
        
        cardTable.addCell(Cell(1, 2).add(Paragraph("Kalan Yıllık İzin Hakkı").setFont(font).setFontSize(8.5f).setFontColor(darkGray)
            .add(Paragraph("   ${report.remainingAnnualLeave} Gün").setFont(fontBold).setFontColor(textGreen).setFontSize(8.5f)))
            .setBorder(Border.NO_BORDER))

        document.add(cardTable)
        document.close()
        
        return file
    }

    private fun addDashboardRow(table: Table, label: String, value: String, valueColor: DeviceRgb, font: PdfFont, labelColor: DeviceRgb, isBold: Boolean = false) {
        table.addCell(Cell().add(Paragraph(label).setFont(font).setFontSize(8.5f).setFontColor(labelColor)).setBorder(Border.NO_BORDER).setPaddingTop(0.5f).setPaddingRight(2f))
        table.addCell(Cell().add(Paragraph(value).setFont(if(isBold) getTurkishFontBold() else font).setFontSize(9f).setFontColor(valueColor).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER).setPaddingTop(0.5f).setPaddingLeft(2f))
    }

    private fun addLeaveCell(table: Table, label: String, value: String, font: PdfFont, fontBold: PdfFont, color: DeviceRgb) {
        val cell = Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER)
        cell.add(Paragraph(label).setFontSize(8f).setFontColor(color))
        cell.add(Paragraph(value).setFont(fontBold).setFontSize(10f).setFontColor(color))
        table.addCell(cell)
    }

    fun openMonthlySlipExcel(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0,
        isShare: Boolean = false
    ) {
        val fileName = "${employee.firstName}_${report.month}_Maas_Bordrosu.xlsx"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Maas Bordrosu Ozet")
        var rowIdx = 0
        
        val r1 = sheet.createRow(rowIdx++); r1.createCell(0).setCellValue("PERSONEL MAAS BORDROSU (OZET)")
        rowIdx++
        
        val r2 = sheet.createRow(rowIdx++); r2.createCell(0).setCellValue("Personel:"); r2.createCell(1).setCellValue("${employee.firstName} ${employee.lastName}")
        if (!employee.iban.isNullOrBlank()) {
            val r3 = sheet.createRow(rowIdx++); r3.createCell(0).setCellValue("IBAN:"); r3.createCell(1).setCellValue(employee.iban)
        }
        val r4 = sheet.createRow(rowIdx++); r4.createCell(0).setCellValue("Donem:"); r4.createCell(1).setCellValue("${report.startDate.format(df)} - ${report.endDate.format(df)}")
        rowIdx++

        // Sabit Maaş Bilgileri
        val fixedBaseNet = employee.hourlyRate * employee.dailyWorkingHours * 30
        val fixedAdditional = employee.additionalSalary
        val combinedMonthly = fixedBaseNet + fixedAdditional
 
        val adj = report.totalAdjustments
        addExcelRow(sheet, rowIdx++, "Sozlesme Maasi (Sabit Toplam)", combinedMonthly)
        addExcelRow(sheet, rowIdx++, "Fazla Mesai (${report.overtimeHours} sa)", report.totalOvertimeEarnings)
        
        if (report.unpaidLeaveDeduction > 0) {
            addExcelRow(sheet, rowIdx++, "Ucretsiz Izin (${report.unpaidLeaveDays} gun)", -report.unpaidLeaveDeduction)
        }
        if (report.sickLeaveDeduction > 0) {
            addExcelRow(sheet, rowIdx++, "Raporlu (${report.sickLeaveDays} gun)", -report.sickLeaveDeduction)
        }
        if (report.missingHoursDeduction > 0) {
            addExcelRow(sheet, rowIdx++, "Eksik Calisma (${report.missingHours} sa)", -report.missingHoursDeduction)
        }
        if (report.totalMealAllowance > 0) addExcelRow(sheet, rowIdx++, "Yemek Ucreti (Gunluk)", report.totalMealAllowance)
        if (report.totalTransportAllowance > 0) addExcelRow(sheet, rowIdx++, "Yol Ucreti (Gunluk)", report.totalTransportAllowance)
        addExcelRow(sheet, rowIdx++, "Ek Yemek (${adj.yemekCount} ad)", adj.yemekAmount)
        addExcelRow(sheet, rowIdx++, "Ek Yol (${adj.yolCount} ad)", adj.yolAmount)
        addExcelRow(sheet, rowIdx++, "Prim (${adj.primCount} ad)", adj.primAmount)
        addExcelRow(sheet, rowIdx++, "Avans (${adj.avansCount} ad)", -adj.avansAmount)
        
        adjustments.filter { it.type == AdjustmentType.KESINTI && it.date.year == report.year && it.date.monthValue == report.month }.forEach { kes ->
            val label = if (!kes.description.isNullOrBlank()) kes.description else "Kesinti"
            addExcelRow(sheet, rowIdx++, label, -kes.amount)
        }
        
        rowIdx++
        val leaveRow = sheet.createRow(rowIdx++)
        leaveRow.createCell(0).setCellValue("Kalan Yıllık İzin:")
        leaveRow.createCell(1).setCellValue("${report.remainingAnnualLeave} Gün")
        
        rowIdx++
        addExcelRow(sheet, rowIdx++, "Onceki Donemden Devir", turnover)
        
        rowIdx++
        val netRow = sheet.createRow(rowIdx++)
        netRow.createCell(0).setCellValue("TOPLAM ALACAK (GENEL)")
        netRow.createCell(1).setCellValue(report.totalEarnings + turnover)
        
        val payRow = sheet.createRow(rowIdx++)
        payRow.createCell(0).setCellValue("YAPILAN ODEME")
        payRow.createCell(1).setCellValue(report.totalAdjustments.totalPaymentAmount)

        val remRow = sheet.createRow(rowIdx++)
        remRow.createCell(0).setCellValue("NET ODENECEK (KALAN)")
        remRow.createCell(1).setCellValue(report.totalEarnings + turnover - report.totalAdjustments.totalPaymentAmount)

        // Auto-size columns removed due to Android incompatibility (java.awt missing)
        for (i in 0..1) sheet.setColumnWidth(i, 25 * 256)

        workbook.write(outputStream); workbook.close(); outputStream.close()
        if (isShare) {
            shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        } else {
            viewFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
    }

    fun openDetailedPdf(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        logs: List<WorkLog>,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0,
        isShare: Boolean = false
    ) {
        val fileName = "${employee.firstName}_${report.month}_Detayli_Tablo.pdf"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        pdf.defaultPageSize = PageSize.A4.rotate() 
        val document = Document(pdf)
        val font = getTurkishFont()
        val fontBold = getTurkishFontBold()
        document.setFont(font)

        document.add(Paragraph("AYLIK MAAŞ BORDROsu").setFont(fontBold).setFontSize(18f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(8f))
        document.add(Paragraph("Dönem: ${report.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} - ${report.endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}").setFont(font).setFontSize(11f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(12f))

        val table = Table(UnitValue.createPointArray(floatArrayOf(65f, 80f, 45f, 45f, 45f, 45f, 45f, 45f, 45f, 45f, 45f, 65f)))
        table.setFontSize(8f).setFont(font)
        table.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)

        val headers = listOf("Tarih", "Personel", "Giriş", "Çıkış", "Eksik(s)", "Mesai(s)", "Yemek", "Yol", "Prim", "Avans", "Kesinti", "Net")
        headers.forEach { table.addHeaderCell(Paragraph(it).setFont(fontBold)) }

        val monthlyLogs = logs.filter { !it.date.isBefore(report.startDate) && !it.date.isAfter(report.endDate) }.sortedBy { it.date }
        val monthlyAdjustments = adjustments.filter { !it.date.isBefore(report.startDate) && !it.date.isAfter(report.endDate) }

        monthlyLogs.forEach { log ->
            val dailyAdjustments = monthlyAdjustments.filter { it.date == log.date }
            
            val additionalHourlyRate = if (employee.dailyWorkingHours > 0) employee.additionalSalary / (30 * employee.dailyWorkingHours) else 0.0
            val combinedHourlyRate = employee.hourlyRate + additionalHourlyRate
            var missing = 0.0; var overtime = 0.0; var dailyNormalEarning = 0.0

            if (log.isOnLeave) {
                // Hafta Sonu ve Resmi Tatil gibi yeni izin türlerini de dahil ediyoruz
                if (log.leaveType != "Ücretsiz İzin" && log.leaveType != "Raporlu") {
                    dailyNormalEarning = employee.dailyWorkingHours * combinedHourlyRate
                } else {
                    missing = employee.dailyWorkingHours
                    // Ücretsiz izin ve raporlu günlerde kesinti (combined rate üzerinden)
                }
            } else if (log.startTime != null && log.endTime != null) {
                val duration = Duration.between(log.startTime, log.endTime)
                val netMinutes = duration.toMinutes() - log.breakDurationMinutes
                val worked = netMinutes.coerceAtLeast(0).toDouble() / 60.0
                
                if (worked > employee.dailyWorkingHours) {
                    overtime = worked - employee.dailyWorkingHours
                    dailyNormalEarning = employee.dailyWorkingHours * combinedHourlyRate
                } else {
                    missing = employee.dailyWorkingHours - worked
                    dailyNormalEarning = worked * combinedHourlyRate
                }
            }

            val overtimeEarning = overtime * combinedHourlyRate * employee.overtimeMultiplier
            val dailyMeal = if (!log.isOnLeave && log.startTime != null) employee.mealAllowance else 0.0
            val dailyTransport = if (!log.isOnLeave && log.startTime != null) employee.transportAllowance else 0.0
            
            val adjYemek = dailyAdjustments.filter { it.type == AdjustmentType.YEMEK }.sumOf { it.amount }
            val adjYol = dailyAdjustments.filter { it.type == AdjustmentType.YOL }.sumOf { it.amount }
            val prim = dailyAdjustments.filter { it.type == AdjustmentType.PRIM }.sumOf { it.amount }
            val avans = dailyAdjustments.filter { it.type == AdjustmentType.AVANS }.sumOf { it.amount }
            val kesinti = dailyAdjustments.filter { it.type == AdjustmentType.KESINTI }.sumOf { it.amount }
            
            val dailyNet = (dailyNormalEarning + overtimeEarning + dailyMeal + dailyTransport + adjYemek + adjYol + prim) - (avans + kesinti)

            table.addCell(log.date.format(df))
            table.addCell("${employee.firstName} ${employee.lastName}")
            table.addCell(log.startTime?.format(tf) ?: "-")
            table.addCell(log.endTime?.format(tf) ?: "-")
            table.addCell(String.format("%.1f", missing))
            table.addCell(String.format("%.1f", overtime))
            table.addCell(String.format("%.2f", dailyMeal + adjYemek))
            table.addCell(String.format("%.2f", dailyTransport + adjYol))
            table.addCell(String.format("%.2f", prim))
            table.addCell(String.format("%.2f", avans))
            table.addCell(String.format("%.2f", kesinti))
            table.addCell(String.format("%.2f", dailyNet))
        }

        table.addCell(Paragraph("TOPLAM").setFont(fontBold))
        table.addCell("") 
        table.addCell("") 
        table.addCell("") 
        table.addCell(Paragraph(String.format("%.1f", report.missingHours)).setFont(fontBold))
        table.addCell(Paragraph(String.format("%.1f", report.overtimeHours)).setFont(fontBold))
        
        val totalMeal = report.totalMealAllowance + report.totalAdjustments.yemekAmount
        val totalTransport = report.totalTransportAllowance + report.totalAdjustments.yolAmount
        
        table.addCell(Paragraph(String.format("%.2f", totalMeal)).setFont(fontBold))
        table.addCell(Paragraph(String.format("%.2f", totalTransport)).setFont(fontBold))
        table.addCell(Paragraph(String.format("%.2f", report.totalAdjustments.primAmount)).setFont(fontBold))
        table.addCell(Paragraph(String.format("%.2f", report.totalAdjustments.avansAmount)).setFont(fontBold))
        table.addCell(Paragraph(String.format("%.2f", report.totalAdjustments.kesintiAmount)).setFont(fontBold))
        
        // Detailed report final total should also follow the balanced logic? 
        table.addCell(Paragraph(String.format("%.2f", report.totalEarnings)).setFont(fontBold))

        document.add(table)
        
        document.add(Paragraph("\nBakiye Özeti:").setFont(fontBold).setFontSize(12f))
        val summaryTable = Table(UnitValue.createPointArray(floatArrayOf(150f, 100f)))
        summaryTable.addCell("Dönem Toplam Hakediş")
        summaryTable.addCell(String.format("%.2f TL", report.totalEarnings))
        summaryTable.addCell("Önceki Dönemden Devir")
        summaryTable.addCell(String.format("%.2f TL", turnover))
        summaryTable.addCell("Toplam Alacak (Genel)")
        summaryTable.addCell(String.format("%.2f TL", report.totalEarnings + turnover))
        summaryTable.addCell("Yapılan Toplam Ödeme")
        summaryTable.addCell(String.format("%.2f TL", report.totalAdjustments.totalPaymentAmount))
        summaryTable.addCell("Net Kalan")
        summaryTable.addCell(String.format("%.2f TL", report.totalEarnings + turnover - report.totalAdjustments.totalPaymentAmount))
        
        document.add(summaryTable)
        document.close()
        if (isShare) {
            shareFile(context, file, "application/pdf")
        } else {
            viewFile(context, file, "application/pdf")
        }
    }

    fun openDetailedExcel(
        context: Context,
        employee: Employee,
        report: SalaryReport,
        logs: List<WorkLog>,
        adjustments: List<Adjustment>,
        turnover: Double = 0.0,
        isShare: Boolean = false
    ) {
        val fileName = "${employee.firstName}_${report.month}_Detayli_Tablo.xlsx"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)

        val workbook = XSSFWorkbook(); val sheet = workbook.createSheet("Detayli Maas Tablosu")
        var rowIdx = 0
        
        val headers = listOf("Tarih", "Personel", "Giris", "Cikis", "Eksik(s)", "Mesai(s)", "Yemek", "Yol", "Prim", "Avans", "Kesinti", "Net Hakedis")
        val headerRow = sheet.createRow(rowIdx++)
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        val monthlyLogs = logs.filter { it.date.monthValue == report.month && it.date.year == report.year }.sortedBy { it.date }
        val monthlyAdjustments = adjustments.filter { it.date.monthValue == report.month && it.date.year == report.year }
        monthlyLogs.forEach { log ->
            val dailyAdjustments = monthlyAdjustments.filter { it.date == log.date }
            val additionalHourlyRate = if (employee.dailyWorkingHours > 0) employee.additionalSalary / (30 * employee.dailyWorkingHours) else 0.0
            val combinedHourlyRate = employee.hourlyRate + additionalHourlyRate
            var missing = 0.0; var overtime = 0.0; var dailyNormalEarning = 0.0
            
            if (log.isOnLeave) {
                if (log.leaveType != "Ücretsiz İzin" && log.leaveType != "Raporlu") {
                    dailyNormalEarning = employee.dailyWorkingHours * combinedHourlyRate
                } else {
                    missing = employee.dailyWorkingHours
                }
            } else if (log.startTime != null && log.endTime != null) {
                val duration = Duration.between(log.startTime, log.endTime)
                val netMinutes = duration.toMinutes() - log.breakDurationMinutes
                val worked = netMinutes.coerceAtLeast(0).toDouble() / 60.0
                
                if (worked > employee.dailyWorkingHours) {
                    overtime = worked - employee.dailyWorkingHours
                    dailyNormalEarning = employee.dailyWorkingHours * combinedHourlyRate
                } else {
                    missing = employee.dailyWorkingHours - worked
                    dailyNormalEarning = worked * combinedHourlyRate
                }
            }

            val overtimeEarning = overtime * combinedHourlyRate * employee.overtimeMultiplier
            val dailyMeal = if (!log.isOnLeave && log.startTime != null) employee.mealAllowance else 0.0
            val dailyTransport = if (!log.isOnLeave && log.startTime != null) employee.transportAllowance else 0.0
            
            val adjYemek = dailyAdjustments.filter { it.type == AdjustmentType.YEMEK }.sumOf { it.amount }
            val adjYol = dailyAdjustments.filter { it.type == AdjustmentType.YOL }.sumOf { it.amount }
            val prim = dailyAdjustments.filter { it.type == AdjustmentType.PRIM }.sumOf { it.amount }
            val avans = dailyAdjustments.filter { it.type == AdjustmentType.AVANS }.sumOf { it.amount }
            val kesinti = dailyAdjustments.filter { it.type == AdjustmentType.KESINTI }.sumOf { it.amount }
            val dailyNet = (dailyNormalEarning + overtimeEarning + dailyMeal + dailyTransport + adjYemek + adjYol + prim) - (avans + kesinti)

            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue(log.date.format(df)); row.createCell(1).setCellValue("${employee.firstName} ${employee.lastName}")
            row.createCell(2).setCellValue(log.startTime?.format(tf) ?: "-"); row.createCell(3).setCellValue(log.endTime?.format(tf) ?: "-")
            row.createCell(4).setCellValue(missing); row.createCell(5).setCellValue(overtime)
            row.createCell(6).setCellValue(dailyMeal + adjYemek); row.createCell(7).setCellValue(dailyTransport + adjYol); row.createCell(8).setCellValue(prim)
            row.createCell(9).setCellValue(avans); row.createCell(10).setCellValue(kesinti); row.createCell(11).setCellValue(dailyNet)
        }

        val footerRow = sheet.createRow(rowIdx)
        footerRow.createCell(0).setCellValue("GENEL TOPLAM")
        footerRow.createCell(4).setCellValue(report.missingHours); footerRow.createCell(5).setCellValue(report.overtimeHours)
        
        val totalMeal = report.totalMealAllowance + report.totalAdjustments.yemekAmount
        val totalTransport = report.totalTransportAllowance + report.totalAdjustments.yolAmount
        
        footerRow.createCell(6).setCellValue(totalMeal); footerRow.createCell(7).setCellValue(totalTransport)
        footerRow.createCell(8).setCellValue(report.totalAdjustments.primAmount); footerRow.createCell(9).setCellValue(report.totalAdjustments.avansAmount)
        footerRow.createCell(10).setCellValue(report.totalAdjustments.kesintiAmount); footerRow.createCell(11).setCellValue(report.totalEarnings)

        rowIdx++
        addExcelRow(sheet, rowIdx++, "Onceki Donemden Devir", turnover)
        addExcelRow(sheet, rowIdx++, "Toplam Alacak (Genel)", report.totalEarnings + turnover)
        
        val aSum = report.totalAdjustments
        if (aSum.maasOdemeAmount > 0) addExcelRow(sheet, rowIdx++, "Maaş Ödemesi", aSum.maasOdemeAmount)
        if (aSum.bankPaymentAmount > 0) addExcelRow(sheet, rowIdx++, "Bankadan Ödeme", aSum.bankPaymentAmount)
        if (aSum.cashPaymentAmount > 0) addExcelRow(sheet, rowIdx++, "Elden Ödeme", aSum.cashPaymentAmount)
        
        addExcelRow(sheet, rowIdx++, "Net Kalan", report.totalEarnings + turnover - aSum.totalPaymentAmount)

        // Auto-size columns removed due to Android incompatibility
        for (i in 0..11) {
            sheet.setColumnWidth(i, 15 * 256)
        }

        workbook.write(outputStream); workbook.close(); outputStream.close()
        if (isShare) {
            shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        } else {
            viewFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
    }

    data class SummaryRowData(
        val employee: Employee,
        val report: SalaryReport,
        val adjustments: List<Adjustment>,
        val turnover: Double // Devir (Previous Balance)
    )

    fun openSummaryPdf(context: Context, data: List<SummaryRowData>, startDate: LocalDate, endDate: LocalDate, isShare: Boolean = false) {
        // Helper function for Turkish number formatting
        fun formatTurkishCurrency(value: Double): String {
            val formatted = String.format("%,.2f", value)
            return formatted.replace(',', ';').replace('.', ',').replace(';', '.') + " ₺"
        }
        
        val fileName = "Tum_Personel_Ozet_Rapor_${startDate.format(df)}_${endDate.format(df)}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        val writer = PdfWriter(outputStream); val pdf = PdfDocument(writer); pdf.defaultPageSize = PageSize.A4.rotate()
        val document = Document(pdf)
        val font = getTurkishFont()
        val fontBold = getTurkishFontBold()
        document.setFont(font)

        document.setMargins(15f, 20f, 15f, 20f)

        document.add(Paragraph("TÜM PERSONEL MAAŞ ÖZET TABLOSU").setFont(fontBold).setFontSize(14f).setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("Dönem: ${startDate.format(df)} - ${endDate.format(df)}").setTextAlignment(TextAlignment.CENTER).setFontSize(10f))

        val table = Table(UnitValue.createPointArray(floatArrayOf(
            65f, // Personel
            44f, // Net Maaş 
            38f, // Mesai
            38f, // Yemek
            38f, // Yol
            38f, // Prim
            38f, // Avans
            42f, // Kesinti
            44f, // Aylık Hak.
            40f, // Devir
            46f, // Toplam Alacak
            40f, // Maaş Öd.
            46f, // Bankadan Öd.
            44f, // Elden Öd.
            48f, // Kalan
            105f  // IBAN
        )))
        table.setFontSize(6.5f).setFont(font).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
        table.setWidth(UnitValue.createPercentValue(98f))

        val headers = listOf("Personel", "Net Maaş", "Mesai", "Yemek", "Yol", "Prim", "Avans", "Kesinti", "Aylık Hak.", "Devir", "Top. Alacak", "Maaş Öd.", "Banka", "Elden", "Kalan", "IBAN")
        headers.forEach { table.addHeaderCell(Paragraph(it).setFont(fontBold)) }

        data.forEach { rowData ->
            val emp = rowData.employee
            val rep = rowData.report
            val turnover = rowData.turnover
            
            // 1. Net Maaş (Fixed Monthly Net Salary - Baseline)
            val fixedBaseNetSalary = emp.hourlyRate * emp.dailyWorkingHours * 30
            val fixedAdditionalSalary = emp.additionalSalary
            val combinedNetSalary = fixedBaseNetSalary + fixedAdditionalSalary
            
            // 2. Mesai
            val mesai = rep.totalOvertimeEarnings
            
            // 3. Yemek (Allowance + Extra)
            val yemek = rep.totalMealAllowance + rep.totalAdjustments.yemekAmount
            
            // 4. Yol (Allowance + Extra)
            val yol = rep.totalTransportAllowance + rep.totalAdjustments.yolAmount
            
            // 5. Prim
            val prim = rep.totalAdjustments.primAmount

            // 6. Avans
            val avans = rep.totalAdjustments.avansAmount

            // 7. Kesinti (Puantaj Eksikleri + Manuel Kesintiler)
            // Mantık: Sabit Maaş ile Puantajdan gelen Normal Kazanç arasındaki fark + Manuel Kesintiler.
            // Bu sayede (Net Maaş - Kesinti) işlemi tam olarak puantaj hakedişini verir.
            val puantajDeduction = rep.totalMissingDeduction
            val manualKesinti = rep.totalAdjustments.kesintiAmount
            val kesinti = puantajDeduction + manualKesinti
            
            // 8. Devir = turnover
            
            // 9. Aylık Hakediş = Calculator sonucu (Sabit Maaş - Kesintiler + Mesailer + Yan Ödemeler)
            val aylikHakedis = rep.totalEarnings
            
            // 10. Toplam Alacak = Aylık Hakediş + Devir
            val topAlacak = aylikHakedis + turnover
            
            // 11. Ödenecekler (Yapılandırma)
            val odenenMaas = rep.totalAdjustments.maasOdemeAmount
            
            // Bankadan Ödenecek = SADECE Aylık Net Maaş (Sabit kısım, ek maaş DAHİL DEĞİL)
            val bankaOdenecek = fixedBaseNetSalary
            
            // Elden Ödenecek = Ek Maaş + Mesai + Yemek + Yol + Prim + Devir
            val eldenOdenecek = fixedAdditionalSalary + mesai + yemek + yol + prim + turnover
            
            // 12. Kalan = Toplam Alacak - Tüm Ödemeler (Sadece yapılan ödemeler)
            val kalan = topAlacak - odenenMaas

            table.addCell("${emp.firstName} ${emp.lastName}")
            table.addCell(formatTurkishCurrency(combinedNetSalary))
            table.addCell(formatTurkishCurrency(mesai))
            table.addCell(formatTurkishCurrency(yemek))
            table.addCell(formatTurkishCurrency(yol))
            table.addCell(formatTurkishCurrency(prim))
            table.addCell(formatTurkishCurrency(avans))
            table.addCell(formatTurkishCurrency(kesinti))
            table.addCell(formatTurkishCurrency(aylikHakedis))
            table.addCell(formatTurkishCurrency(turnover))
            table.addCell(formatTurkishCurrency(topAlacak))
            table.addCell(formatTurkishCurrency(odenenMaas))
            table.addCell(formatTurkishCurrency(bankaOdenecek))
            table.addCell(formatTurkishCurrency(eldenOdenecek))
            table.addCell(formatTurkishCurrency(kalan))
            table.addCell(emp.iban ?: "-")
        }

        table.addCell(Paragraph("GENEL TOPLAM").setFont(fontBold))
        
        val sums = DoubleArray(15) // Size for all sum columns
        
        data.forEach { r ->
             val emp = r.employee; val rep = r.report
             
             val fixedBase = emp.hourlyRate * emp.dailyWorkingHours * 30
             val fixedAdd = emp.additionalSalary
             val combined = fixedBase + fixedAdd
             
             val mesai = rep.totalOvertimeEarnings
             val yemek = rep.totalMealAllowance + rep.totalAdjustments.yemekAmount
             val yol = rep.totalTransportAllowance + rep.totalAdjustments.yolAmount
             val prim = rep.totalAdjustments.primAmount
             val avans = rep.totalAdjustments.avansAmount
             val kesinti = rep.totalMissingDeduction + rep.totalAdjustments.kesintiAmount
             val aylikHakedis = rep.totalEarnings
             val topAlacak = aylikHakedis + r.turnover
             val odenenMaas = rep.totalAdjustments.maasOdemeAmount
             val odenenBanka = rep.totalAdjustments.bankPaymentAmount
             val odenenElden = rep.totalAdjustments.cashPaymentAmount
             val kalan = topAlacak - rep.totalAdjustments.totalPaymentAmount
             
             sums[0] += combined 
             sums[1] += mesai 
             sums[2] += yemek 
             sums[3] += yol 
             sums[4] += prim 
             sums[5] += avans 
             sums[6] += kesinti
             sums[7] += aylikHakedis
             sums[8] += r.turnover 
             sums[9] += topAlacak
             sums[10] += odenenMaas
             sums[11] += odenenBanka
             sums[12] += odenenElden
             sums[13] += kalan 
        }

        sums.take(14).forEach { table.addCell(Paragraph(String.format("%.2f TL", it)).setFont(fontBold)) }
        table.addCell("") // IBAN sum column empty

        document.add(table); document.close()
        if (isShare) {
            shareFile(context, file, "application/pdf")
        } else {
            viewFile(context, file, "application/pdf")
        }
    }

    fun openSummaryExcel(context: Context, data: List<SummaryRowData>, startDate: LocalDate, endDate: LocalDate, isShare: Boolean = false) {
        val fileName = "Tum_Personel_Ozet_Rapor_${startDate.format(df)}_${endDate.format(df)}_${System.currentTimeMillis()}.xlsx"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        val workbook = XSSFWorkbook(); val sheet = workbook.createSheet("Personel Maas Ozet")
        var rowIdx = 0
        
        val headerRow = sheet.createRow(rowIdx++)
        val headers = listOf("Personel", "Net Maaş", "Mesai", "Yemek", "Yol", "Prim", "Avans", "Kesinti", "Aylık Hak.", "Devir", "Top. Alacak", "Maaş Öd.", "Banka Öd.", "Elden Öd.", "Kalan", "IBAN")
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        data.forEach { rowData ->
            val emp = rowData.employee
            val rep = rowData.report
            val turnover = rowData.turnover
            
            val combinedNetSalary = (emp.hourlyRate * emp.dailyWorkingHours * 30) + emp.additionalSalary
            
            val mesai = rep.totalOvertimeEarnings
            val yemek = rep.totalMealAllowance + rep.totalAdjustments.yemekAmount
            val yol = rep.totalTransportAllowance + rep.totalAdjustments.yolAmount
            val prim = rep.totalAdjustments.primAmount
            val avans = rep.totalAdjustments.avansAmount
            val kesinti = rep.totalMissingDeduction + rep.totalAdjustments.kesintiAmount
            
            val aylikHakedis = rep.totalEarnings
            val topAlacak = aylikHakedis + turnover
            val odenenMaas = rep.totalAdjustments.maasOdemeAmount
            
            // Bankadan Ödenecek = SADECE Aylık Net Maaş (Ek maaş dahil değil)
            val fixedBaseNetSalary = emp.hourlyRate * emp.dailyWorkingHours * 30
            val fixedAdditionalSalary = emp.additionalSalary
            val bankaOdenecek = fixedBaseNetSalary
            
            // Elden Ödenecek = Ek Maaş + Mesai + Yemek + Yol + Prim + Devir
            val eldenOdenecek = fixedAdditionalSalary + mesai + yemek + yol + prim + turnover
            
            val kalan = topAlacak - odenenMaas

            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue("${emp.firstName} ${emp.lastName}")
            row.createCell(1).setCellValue(combinedNetSalary)
            row.createCell(2).setCellValue(mesai)
            row.createCell(3).setCellValue(yemek)
            row.createCell(4).setCellValue(yol)
            row.createCell(5).setCellValue(prim)
            row.createCell(6).setCellValue(avans)
            row.createCell(7).setCellValue(kesinti)
            row.createCell(8).setCellValue(aylikHakedis)
            row.createCell(9).setCellValue(turnover)
            row.createCell(10).setCellValue(topAlacak)
            row.createCell(11).setCellValue(odenenMaas)
            row.createCell(12).setCellValue(bankaOdenecek)
            row.createCell(13).setCellValue(eldenOdenecek)
            row.createCell(14).setCellValue(kalan)
            row.createCell(15).setCellValue(emp.iban ?: "")
        }

        val footerRow = sheet.createRow(rowIdx)
        footerRow.createCell(0).setCellValue("GENEL TOPLAM")
        
        val sums = DoubleArray(15)
        data.forEach { r ->
             val emp = r.employee; val rep = r.report
             val combined = (emp.hourlyRate * emp.dailyWorkingHours * 30) + emp.additionalSalary
             
             val mesai = rep.totalOvertimeEarnings
             val yemek = rep.totalMealAllowance + rep.totalAdjustments.yemekAmount
             val yol = rep.totalTransportAllowance + rep.totalAdjustments.yolAmount
             val prim = rep.totalAdjustments.primAmount
             val avans = rep.totalAdjustments.avansAmount
             val kesinti = rep.totalMissingDeduction + rep.totalAdjustments.kesintiAmount

             val aylikHakedis = rep.totalEarnings
             val topAlacak = aylikHakedis + r.turnover
             val odenenMaas = rep.totalAdjustments.maasOdemeAmount
             val odenenBanka = rep.totalAdjustments.bankPaymentAmount
             val odenenElden = rep.totalAdjustments.cashPaymentAmount
             val kalan = topAlacak - rep.totalAdjustments.totalPaymentAmount
             
             sums[0] += combined 
             sums[1] += mesai 
             sums[2] += yemek 
             sums[3] += yol 
             sums[4] += prim 
             sums[5] += avans 
             sums[6] += kesinti
             sums[7] += aylikHakedis
             sums[8] += r.turnover 
             sums[9] += topAlacak
             sums[10] += odenenMaas
             sums[11] += odenenBanka
             sums[12] += odenenElden
             sums[13] += kalan 
        }
        
        sums.take(14).forEachIndexed { i, sum -> footerRow.createCell(i + 1).setCellValue(sum) }

        // Auto-size columns removed due to Android incompatibility
        for (i in 0..15) {
            sheet.setColumnWidth(i, 15 * 256)
        }

        workbook.write(outputStream); workbook.close(); outputStream.close()
        viewFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    private fun addSummaryRow(table: Table, label: String, value: String) {
        table.addCell(label); table.addCell(value)
    }

    private fun addExcelRow(sheet: org.apache.poi.ss.usermodel.Sheet, rowIdx: Int, label: String, value: Double) {
        val row = sheet.createRow(rowIdx)
        row.createCell(0).setCellValue(label)
        row.createCell(1).setCellValue(value)
    }

    fun viewFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(intent, "Dosyayı Aç")
        context.startActivity(chooser)
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(intent, "Paylaş")
        context.startActivity(chooser)
    }
}
