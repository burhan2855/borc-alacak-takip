package com.burhan2855.borctakip.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.burhan2855.borctakip.data.FinancialTotals
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.reports.CategoryReport
import com.burhan2855.borctakip.ui.reports.MonthlyReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfUtils {

    // Renk sabitleri
    private val PRIMARY_COLOR = Color.parseColor("#6A1B9A")
    private val SECONDARY_COLOR = Color.parseColor("#8E24AA")
    private val ACCENT_COLOR = Color.parseColor("#AB47BC")
    private val TEXT_COLOR = Color.parseColor("#2C2C2C")
    private val LIGHT_GRAY = Color.parseColor("#F5F5F5")
    private val MEDIUM_GRAY = Color.parseColor("#9E9E9E")

    /**
     * Modern PDF başlığı çizer
     */
    private fun drawModernHeader(
        canvas: Canvas,
        paint: Paint,
        title: String,
        pageWidth: Int,
        yPosition: Float
    ): Float {
        var currentY = yPosition

        // Arka plan gradient efekti (basit dikdörtgen)
        paint.color = LIGHT_GRAY
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

        // Ana başlık
        paint.color = PRIMARY_COLOR
        paint.textSize = 24f
        paint.isFakeBoldText = true
        val titleWidth = paint.measureText(title)
        canvas.drawText(title, (pageWidth / 2).toFloat() - (titleWidth / 2), 35f, paint)

        // Alt çizgi
        paint.color = SECONDARY_COLOR
        paint.strokeWidth = 3f
        canvas.drawLine(
            (pageWidth / 2).toFloat() - (titleWidth / 2),
            45f,
            (pageWidth / 2).toFloat() + (titleWidth / 2),
            45f,
            paint
        )

        // Tarih ve sayfa bilgisi
        paint.color = MEDIUM_GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Oluşturulma: $dateText", 40f, 60f, paint)
        canvas.drawText("Sayfa 1", pageWidth - 80f, 60f, paint)

        return 90f
    }

    /**
     * Modern tablo başlığı çizer
     */
    private fun drawModernTableHeader(
        canvas: Canvas,
        paint: Paint,
        headers: List<String>,
        positions: List<Float>,
        yPosition: Float,
        pageWidth: Int
    ): Float {
        // Başlık arka planı
        paint.color = PRIMARY_COLOR
        canvas.drawRect(30f, yPosition - 5f, (pageWidth - 30).toFloat(), yPosition + 20f, paint)

        // Başlık metinleri
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true

        headers.forEachIndexed { index, header ->
            canvas.drawText(header, positions[index], yPosition + 10f, paint)
        }

        paint.isFakeBoldText = false
        return yPosition + 30f
    }

    /**
     * Modern tablo satırı çizer
     */
    private fun drawModernTableRow(
        canvas: Canvas,
        paint: Paint,
        values: List<String>,
        positions: List<Float>,
        yPosition: Float,
        pageWidth: Int,
        isEvenRow: Boolean = false
    ): Float {
        // Alternatif satır rengi
        if (isEvenRow) {
            paint.color = LIGHT_GRAY
            canvas.drawRect(30f, yPosition - 3f, (pageWidth - 30).toFloat(), yPosition + 15f, paint)
        }

        // Satır metinleri
        paint.color = TEXT_COLOR
        paint.textSize = 9f

        values.forEachIndexed { index, value ->
            canvas.drawText(value, positions[index], yPosition + 8f, paint)
        }

        return yPosition + 18f
    }

    fun createTransactionListPdf(
        context: Context,
        transactions: List<Transaction>,
        title: String,
        currencySymbol: String,
        financialTotals: FinancialTotals
    ): File {
        val pdfFile = createPdfFile(context, title)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Modern başlık
        var yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f)
        yPosition += 20f

        // Modern tablo başlığı
        val headers = listOf("İşlem", "Tutar", "Kategori", "Tarih", "Durum")
        val positions = listOf(40f, 180f, 280f, 380f, 480f)
        yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)

        // Tablo içeriği
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var rowIndex = 0
        
        for (transaction in transactions) {
            if (yPosition > 720) { // Yeni sayfa kontrolü
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f) + 20f
                yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)
                rowIndex = 0
            }

            val values = listOf(
                (transaction.title ?: "").take(20) + if ((transaction.title ?: "").length > 20) "..." else "",
                com.burhan2855.borctakip.formatCurrency(transaction.amount, currencySymbol),
                (transaction.category ?: "").take(12),
                dateFormat.format(Date(transaction.date ?: System.currentTimeMillis())),
                transaction.status
            )

            yPosition = drawModernTableRow(
                canvas, paint, values, positions, yPosition, 
                pageInfo.pageWidth, rowIndex % 2 == 0
            )
            rowIndex++
        }

        // Genel toplam bölümü için sayfa kontrolü
        if (yPosition > 650) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 40f
        }

        // Modern genel toplam bölümü
        addModernGeneralTotalsSection(canvas, paint, yPosition, pageInfo.pageWidth, financialTotals, currencySymbol)

        document.finishPage(page)
        savePdf(document, pdfFile)
        return pdfFile
    }

    fun createMonthlySummaryPdf(
        context: Context,
        reports: List<MonthlyReport>,
        title: String,
        currencySymbol: String,
        financialTotals: FinancialTotals
    ): File {
        val pdfFile = createPdfFile(context, title)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Modern başlık
        var yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f)
        yPosition += 20f

        // Modern tablo başlığı
        val headers = listOf("Ay", "Gelir (kasa+banka+alacak)", "Gider", "Net")
        val positions = listOf(40f, 200f, 350f, 480f)
        yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)

        // Tablo içeriği
        var rowIndex = 0
        for (report in reports) {
            if (yPosition > 720) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f) + 20f
                yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)
                rowIndex = 0
            }

            val values = listOf(
                report.month,
                com.burhan2855.borctakip.formatCurrency(report.income + report.accountTotal, currencySymbol),
                com.burhan2855.borctakip.formatCurrency(report.expense, currencySymbol),
                com.burhan2855.borctakip.formatCurrency((report.income + report.accountTotal) - report.expense, currencySymbol)
            )

            yPosition = drawModernTableRow(
                canvas, paint, values, positions, yPosition, 
                pageInfo.pageWidth, rowIndex % 2 == 0
            )
            rowIndex++
        }

        // Genel toplam bölümü
        if (yPosition > 650) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 40f
        }

        addModernGeneralTotalsSection(canvas, paint, yPosition, pageInfo.pageWidth, financialTotals, currencySymbol)

        document.finishPage(page)
        savePdf(document, pdfFile)
        return pdfFile
    }

    fun createCategoryReportPdf(
        context: Context,
        reports: List<CategoryReport>,
        title: String,
        currencySymbol: String,
        financialTotals: FinancialTotals
    ): File {
        val pdfFile = createPdfFile(context, title)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Modern başlık
        var yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f)
        yPosition += 20f

        // Modern tablo başlığı
        val headers = listOf("Kategori", "Tutar", "Yüzde")
        val positions = listOf(40f, 300f, 450f)
        yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)

        // Toplam harcama hesapla
        val totalSpending = reports.sumOf { it.total }

        // Tablo içeriği
        var rowIndex = 0
        for (report in reports) {
            if (yPosition > 720) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f) + 20f
                yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)
                rowIndex = 0
            }

            val percentage = if (totalSpending > 0) (report.total / totalSpending * 100).toInt() else 0
            val values = listOf(
                report.category,
                com.burhan2855.borctakip.formatCurrency(report.total, currencySymbol),
                "%$percentage"
            )

            yPosition = drawModernTableRow(
                canvas, paint, values, positions, yPosition, 
                pageInfo.pageWidth, rowIndex % 2 == 0
            )
            rowIndex++
        }

        // Genel toplam bölümü
        if (yPosition > 650) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 40f
        }

        addModernGeneralTotalsSection(canvas, paint, yPosition, pageInfo.pageWidth, financialTotals, currencySymbol)

        document.finishPage(page)
        savePdf(document, pdfFile)
        return pdfFile
    }

    fun createGeneralSummaryPdf(
        context: Context,
        cashBalance: Double,
        bankBalance: Double,
        unpaidDebts: Double,
        unpaidReceivables: Double,
        netWorth: Double,
        currencySymbol: String,
        detailed: Boolean,
        transactions: List<Transaction>
    ): File {
        // FinancialTotals nesnesi oluştur
        val financialTotals = FinancialTotals(
            cashBalance = cashBalance,
            bankBalance = bankBalance,
            unpaidDebts = unpaidDebts,
            unpaidReceivables = unpaidReceivables,
            netWorth = netWorth
        )
        val title = if (detailed) "Hesap Ekstresi" else "Genel Hesap Özeti"
        val pdfFile = createPdfFile(context, title)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Modern başlık
        var yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f)
        yPosition += 20f

        if (detailed) {
            // Detaylı hesap ekstresi - modern tablo tasarımı
            val sortedTransactions = transactions.sortedBy { it.transactionDate }
            var runningBalance = 0.0

            val headers = listOf("Tarih", "Açıklama", "Alacak", "Borç", "Bakiye", "Vade")
            val positions = listOf(40f, 110f, 260f, 330f, 400f, 470f)
            yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            var rowIndex = 0
            
            for (transaction in sortedTransactions) {
                if (yPosition > 720) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = drawModernHeader(canvas, paint, title, pageInfo.pageWidth, 0f) + 20f
                    yPosition = drawModernTableHeader(canvas, paint, headers, positions, yPosition, pageInfo.pageWidth)
                    rowIndex = 0
                }

                val isDebt = transaction.isDebt
                val amount = transaction.amount
                runningBalance += if (isDebt) -amount else amount

                val values = listOf(
                    dateFormat.format(Date(transaction.transactionDate ?: System.currentTimeMillis())),
                    transaction.title.take(15) + if (transaction.title.length > 15) "..." else "",
                    if (!isDebt) com.burhan2855.borctakip.formatCurrency(amount, currencySymbol) else "",
                    if (isDebt) com.burhan2855.borctakip.formatCurrency(amount, currencySymbol) else "",
                    com.burhan2855.borctakip.formatCurrency(runningBalance, currencySymbol),
                    if (isDebt) dateFormat.format(Date(transaction.date ?: System.currentTimeMillis())) else ""
                )

                yPosition = drawModernTableRow(
                    canvas, paint, values, positions, yPosition, 
                    pageInfo.pageWidth, rowIndex % 2 == 0
                )
                rowIndex++
            }

            // Toplam bakiye kutusu
            yPosition += 20f
            paint.color = SECONDARY_COLOR
            canvas.drawRect(300f, yPosition - 5f, (pageInfo.pageWidth - 40).toFloat(), yPosition + 25f, paint)
            
            paint.color = Color.WHITE
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("TOPLAM BAKİYE:", 310f, yPosition + 8f, paint)
            canvas.drawText(com.burhan2855.borctakip.formatCurrency(runningBalance, currencySymbol), 450f, yPosition + 8f, paint)
            yPosition += 40f

        } else {
            // Basit özet - modern kart tasarımı
            drawModernSummaryCards(canvas, paint, yPosition, pageInfo.pageWidth, 
                                 cashBalance, bankBalance, unpaidDebts, unpaidReceivables, netWorth, currencySymbol)
            yPosition += 120f
        }

        // Genel toplam bölümü için sayfa kontrolü
        if (yPosition > 650) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 40f
        }

        // Modern genel toplam bölümü
        addModernGeneralTotalsSection(canvas, paint, yPosition, pageInfo.pageWidth, financialTotals, currencySymbol)

        document.finishPage(page)
        savePdf(document, pdfFile)
        return pdfFile
    }

    private fun createPdfFile(context: Context, title: String): File {
        val pdfDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return File(pdfDir, "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
    }

    /**
     * Modern genel finansal durum bölümü çizer
     */
    private fun addModernGeneralTotalsSection(
        canvas: Canvas,
        paint: Paint,
        yPosition: Float,
        pageWidth: Int,
        financialTotals: FinancialTotals,
        currencySymbol: String
    ): Float {
        var currentY = yPosition + 40f

        // Modern başlık kutusu
        paint.color = PRIMARY_COLOR
        canvas.drawRect(30f, currentY - 10f, (pageWidth - 30).toFloat(), currentY + 25f, paint)

        paint.color = Color.WHITE
        paint.textSize = 16f
        paint.isFakeBoldText = true
        val title = "GENEL FİNANSAL DURUM"
        val titleWidth = paint.measureText(title)
        canvas.drawText(title, (pageWidth / 2).toFloat() - (titleWidth / 2), currentY + 8f, paint)
        currentY += 45f

        // Finansal kartlar
        val cardHeight = 35f
        val cardSpacing = 10f

        // Kasa kartı
        drawFinancialCard(canvas, paint, "KASA", financialTotals.cashBalance, currencySymbol, 
                         40f, currentY, 120f, cardHeight, Color.parseColor("#FF9800"))
        
        // Banka kartı
        drawFinancialCard(canvas, paint, "BANKA", financialTotals.bankBalance, currencySymbol, 
                         170f, currentY, 120f, cardHeight, Color.parseColor("#2196F3"))
        
        // Borç kartı
        drawFinancialCard(canvas, paint, "BORÇLAR", financialTotals.unpaidDebts, currencySymbol, 
                         300f, currentY, 120f, cardHeight, Color.parseColor("#E53935"))
        
        // Alacak kartı
        drawFinancialCard(canvas, paint, "ALACAKLAR", financialTotals.unpaidReceivables, currencySymbol, 
                         430f, currentY, 120f, cardHeight, Color.parseColor("#4CAF50"))
        
        currentY += cardHeight + 20f

        // Net varlık - özel tasarım
        val netColor = if (financialTotals.netWorth >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#E53935")
        paint.color = netColor
        canvas.drawRect(40f, currentY - 5f, (pageWidth - 40).toFloat(), currentY + 30f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        val netText = "NET VARLIK: ${com.burhan2855.borctakip.formatCurrency(financialTotals.netWorth, currencySymbol)}"
        val netWidth = paint.measureText(netText)
        canvas.drawText(netText, (pageWidth / 2).toFloat() - (netWidth / 2), currentY + 18f, paint)

        return currentY + 50f
    }

    /**
     * Finansal kart çizer
     */
    private fun drawFinancialCard(
        canvas: Canvas,
        paint: Paint,
        title: String,
        amount: Double,
        currencySymbol: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int
    ) {
        // Kart arka planı
        paint.color = color
        canvas.drawRect(x, y, x + width, y + height, paint)

        // Başlık
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText(title, x + 5f, y + 12f, paint)

        // Tutar
        paint.textSize = 10f
        val amountText = com.burhan2855.borctakip.formatCurrency(amount, currencySymbol)
        canvas.drawText(amountText, x + 5f, y + 26f, paint)
        paint.isFakeBoldText = false
    }



    /**
     * Modern özet kartları çizer
     */
    private fun drawModernSummaryCards(
        canvas: Canvas,
        paint: Paint,
        yPosition: Float,
        pageWidth: Int,
        cashBalance: Double,
        bankBalance: Double,
        unpaidDebts: Double,
        unpaidReceivables: Double,
        netWorth: Double,
        currencySymbol: String
    ) {
        var currentY = yPosition

        // Başlık
        paint.color = PRIMARY_COLOR
        paint.textSize = 16f
        paint.isFakeBoldText = true
        val title = "FİNANSAL ÖZET"
        val titleWidth = paint.measureText(title)
        canvas.drawText(title, (pageWidth / 2).toFloat() - (titleWidth / 2), currentY, paint)
        currentY += 30f

        // Kartlar
        val cardWidth = 100f
        val cardHeight = 60f
        val cardSpacing = 20f
        val startX = 50f

        // Kasa
        drawFinancialCard(canvas, paint, "KASA", cashBalance, currencySymbol, 
                         startX, currentY, cardWidth, cardHeight, Color.parseColor("#FF9800"))
        
        // Banka
        drawFinancialCard(canvas, paint, "BANKA", bankBalance, currencySymbol, 
                         startX + cardWidth + cardSpacing, currentY, cardWidth, cardHeight, Color.parseColor("#2196F3"))
        
        // Borç
        drawFinancialCard(canvas, paint, "BORÇLAR", unpaidDebts, currencySymbol, 
                         startX + (cardWidth + cardSpacing) * 2, currentY, cardWidth, cardHeight, Color.parseColor("#E53935"))
        
        // Alacak
        drawFinancialCard(canvas, paint, "ALACAKLAR", unpaidReceivables, currencySymbol, 
                         startX + (cardWidth + cardSpacing) * 3, currentY, cardWidth, cardHeight, Color.parseColor("#4CAF50"))
    }

    /**
     * Eski genel toplam fonksiyonu (geriye uyumluluk için)
     */
    private fun addGeneralTotalsSection(
        canvas: Canvas,
        paint: Paint,
        yPosition: Float,
        pageWidth: Int,
        financialTotals: FinancialTotals,
        currencySymbol: String
    ): Float {
        return addModernGeneralTotalsSection(canvas, paint, yPosition, pageWidth, financialTotals, currencySymbol)
    }

    private fun savePdf(document: PdfDocument, file: File) {
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}