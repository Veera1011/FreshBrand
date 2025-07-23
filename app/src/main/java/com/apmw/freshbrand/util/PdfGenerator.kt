//// PdfGenerator.kt
//package com.apmw.freshbrand.util
//
//import android.content.Context
//import android.graphics.pdf.PdfDocument
//import com.apmw.freshbrand.model.Order
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//object PdfGenerator {
//    fun generateInvoice(context: Context, order: Order): File {
//        val pdfFile = File.createTempFile("invoice_${order.id}", ".pdf", context.cacheDir)
//
//        // Create a new PDF document
//        val document = PdfDocument()
//
//        // Create a page description
//        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//
//        // Start a page
//        val page = document.startPage(pageInfo)
//
//        // Draw content on the page
//        val canvas = page.canvas
//        val paint = android.graphics.Paint()
//        paint.textSize = 12f
//
//        var yPos = 50f
//
//        // Draw title
//        canvas.drawText("INVOICE", 50f, yPos, paint.apply { textSize = 24f })
//        yPos += 40f
//
//        // Draw order info
//        canvas.drawText("Order #: ${order.id.takeLast(8)}", 50f, yPos, paint.apply { textSize = 14f })
//        yPos += 20f
//        canvas.drawText("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(order.orderDate))}", 50f, yPos, paint)
//        yPos += 20f
//        canvas.drawText("Customer: ${order.userName}", 50f, yPos, paint)
//        yPos += 20f
//        canvas.drawText("Email: ${order.userEmail}", 50f, yPos, paint)
//        yPos += 20f
//        canvas.drawText("Phone: ${order.userPhone}", 50f, yPos, paint)
//        yPos += 30f
//
//        // Draw items table header
//        canvas.drawText("Item", 50f, yPos, paint.apply { isFakeBoldText = true })
//        canvas.drawText("Qty", 300f, yPos, paint.apply { isFakeBoldText = true })
//        canvas.drawText("Price", 400f, yPos, paint.apply { isFakeBoldText = true })
//        canvas.drawText("Total", 500f, yPos, paint.apply { isFakeBoldText = true })
//        yPos += 20f
//
//        // Draw items
//        order.items.forEach { item ->
//            canvas.drawText(item.productName, 50f, yPos, paint)
//            canvas.drawText(item.quantity.toString(), 300f, yPos, paint)
//            canvas.drawText("₹${item.price}", 400f, yPos, paint)
//            canvas.drawText("₹${item.price * item.quantity}", 500f, yPos, paint)
//            yPos += 20f
//        }
//
//        yPos += 20f
//
//        // Draw totals
//        canvas.drawText("Subtotal: ₹${order.subtotal}", 400f, yPos, paint)
//        yPos += 20f
//        canvas.drawText("Tax: ₹${order.tax}", 400f, yPos, paint)
//        yPos += 20f
//        canvas.drawText("Total: ₹${order.totalAmount}", 400f, yPos, paint.apply { textSize = 16f; isFakeBoldText = true })
//
//        // Finish the page
//        document.finishPage(page)
//
//        // Write the document to a file
//        FileOutputStream(pdfFile).use { out ->
//            document.writeTo(out)
//        }
//
//        // Close the document
//        document.close()
//
//        return pdfFile
//    }
//}