package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.model.MemberCalculation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SomityPdfExporter {

    fun generateMemberPdf(
        context: Context,
        calculation: MemberCalculation
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val subtitlePaint = Paint()
        val headerPaint = Paint()
        val boldPaint = Paint()

        // Header Background Banner
        paint.color = Color.parseColor("#0F6E38") // Emerald Green
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Text
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 22f
        titlePaint.isFakeBoldText = true
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("শিংলাব চরপোতন সমিতি", 595f / 2, 45f, titlePaint)

        subtitlePaint.color = Color.parseColor("#E8F5E9")
        subtitlePaint.textSize = 14f
        subtitlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("সদস্য কিস্তি ও জমার হিসাব বিবরণী (শুরু: ০১/০১/২০২৫)", 595f / 2, 72f, subtitlePaint)

        // Member Information Box
        val member = calculation.member
        val startY = 130f

        headerPaint.color = Color.BLACK
        headerPaint.textSize = 14f
        headerPaint.isFakeBoldText = true

        canvas.drawText("সদস্যের তথ্য:", 40f, startY, headerPaint)

        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = false

        canvas.drawText("নাম: ${member.name}", 40f, startY + 22f, paint)
        canvas.drawText("সদস্য নং: ${member.memberNo}", 40f, startY + 40f, paint)
        canvas.drawText("মোবাইল: ${member.phone}", 320f, startY + 22f, paint)
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("তারিখ: ${MemberCalculation.toBengaliDigits(todayStr)}", 320f, startY + 40f, paint)

        // Summary Statistics Box
        val boxTop = startY + 60f
        paint.color = Color.parseColor("#F4F6F4")
        canvas.drawRect(35f, boxTop, 560f, boxTop + 85f, paint)

        paint.color = Color.parseColor("#0F6E38")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(35f, boxTop, 560f, boxTop + 85f, paint)

        paint.style = Paint.Style.FILL
        boldPaint.color = Color.BLACK
        boldPaint.textSize = 12f
        boldPaint.isFakeBoldText = true

        val paidStr = "মোট দেওয়া কিস্তি: ${MemberCalculation.formatNumber(calculation.totalPaidInstallments)} টি"
        val paidAmtStr = "মোট জমা টাকা: ${MemberCalculation.formatCurrency(calculation.totalPaidAmount)}"
        val dueStr = "বকেয়া কিস্তি: ${MemberCalculation.formatNumber(calculation.dueInstallments)} টি"
        val dueAmtStr = "বকেয়া টাকা: ${MemberCalculation.formatCurrency(calculation.dueAmount)}"

        canvas.drawText(paidStr, 50f, boxTop + 30f, boldPaint)
        canvas.drawText(paidAmtStr, 50f, boxTop + 60f, boldPaint)

        val dueColor = if (calculation.dueAmount > 0) Color.parseColor("#C62828") else Color.parseColor("#1B5E20")
        boldPaint.color = dueColor
        canvas.drawText(dueStr, 320f, boxTop + 30f, boldPaint)
        canvas.drawText(dueAmtStr, 320f, boxTop + 60f, boldPaint)

        // Payments Table
        val tableTop = boxTop + 115f
        headerPaint.textSize = 14f
        headerPaint.color = Color.BLACK
        canvas.drawText("জমার বিবরণী (Payment History):", 40f, tableTop, headerPaint)

        // Table Header
        val thTop = tableTop + 15f
        paint.color = Color.parseColor("#0F6E38")
        canvas.drawRect(35f, thTop, 560f, thTop + 25f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("তারিখ", 50f, thTop + 17f, paint)
        canvas.drawText("মাস/বছর", 150f, thTop + 17f, paint)
        canvas.drawText("কিস্তি সংখ্যা", 270f, thTop + 17f, paint)
        canvas.drawText("জমা টাকা", 380f, thTop + 17f, paint)
        canvas.drawText("রশিদ নং", 480f, thTop + 17f, paint)

        // Table Rows
        var currentY = thTop + 25f
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.isFakeBoldText = false

        val payments = calculation.payments
        if (payments.isEmpty()) {
            canvas.drawText("এখনো কোনো জমা জমা হয়নি", 50f, currentY + 20f, paint)
            currentY += 30f
        } else {
            payments.forEachIndexed { index, item ->
                if (index % 2 == 1) {
                    val bgPaint = Paint()
                    bgPaint.color = Color.parseColor("#F8F9FA")
                    canvas.drawRect(35f, currentY, 560f, currentY + 22f, bgPaint)
                }

                canvas.drawText(MemberCalculation.toBengaliDigits(item.paymentDate), 50f, currentY + 15f, paint)
                canvas.drawText(item.monthYear, 150f, currentY + 15f, paint)
                canvas.drawText("${MemberCalculation.formatNumber(item.installmentCount)} টি", 270f, currentY + 15f, paint)
                canvas.drawText(MemberCalculation.formatCurrency(item.amount), 380f, currentY + 15f, paint)
                canvas.drawText(item.receiptNo.ifEmpty { "-" }, 480f, currentY + 15f, paint)

                currentY += 22f
            }
        }

        // Footer & Signature
        val footerY = 780f
        paint.color = Color.GRAY
        paint.textSize = 10f
        canvas.drawText("প্রস্তুতকারীর স্বাক্ষর: _______________", 40f, footerY, paint)
        canvas.drawText("ক্যাশিয়ার/কোষাধ্যক্ষ: _______________", 350f, footerY, paint)
        canvas.drawText("শিংলাব চরপোতন সমিতি মোবাইল অ্যাপ কর্তৃক স্বয়ংক্রিয়ভাবে তৈরি।", 40f, footerY + 25f, paint)

        document.finishPage(page)

        // Save file
        val pdfFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "Scsm_Member_${member.memberNo}_Report.pdf"
        )

        return try {
            val fos = FileOutputStream(pdfFile)
            document.writeTo(fos)
            document.close()
            fos.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "পিডিএফ রিপোর্ট শেয়ার করুন"))
    }
}
