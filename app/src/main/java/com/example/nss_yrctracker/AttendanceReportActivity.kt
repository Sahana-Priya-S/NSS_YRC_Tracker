package com.example.nss_yrctracker

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class AttendanceReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnExport: Button
    private val db = FirebaseFirestore.getInstance()
    private val attendanceList = mutableListOf<AttendanceRecord>()
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_report)

        // SYNCED IDs: Verified against activity_attendance_report.xml
        recyclerView = findViewById(R.id.recyclerReport)
        btnExport = findViewById(R.id.btnExportPdf)

        recyclerView.layoutManager = LinearLayoutManager(this)
        eventId = intent.getStringExtra("EVENT_ID") ?: ""

        if (eventId.isNotEmpty()) {
            loadAttendanceData()
        } else {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show()
        }

        btnExport.setOnClickListener {
            generateAndSharePDF()
        }
    }

    private fun loadAttendanceData() {
        // IMPORTANT: Verify if the collection is "attendance" or "Attendance" in Firebase Console
        db.collection("attendance")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshots ->
                attendanceList.clear()
                if (snapshots.isEmpty) {
                    Toast.makeText(this, "No attendance records found for this event", Toast.LENGTH_SHORT).show()
                } else {
                    for (doc in snapshots) {
                        val record = doc.toObject(AttendanceRecord::class.java)
                        attendanceList.add(record)
                    }
                }
                recyclerView.adapter = AttendanceReportAdapter(attendanceList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateAndSharePDF() {
        if (attendanceList.isEmpty()) {
            Toast.makeText(this, "No records to export", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        paint.textSize = 20f
        canvas.drawText("Attendance Report", 20f, 50f, paint)

        paint.textSize = 14f
        var y = 100f
        for (record in attendanceList) {
            // Uses the variable we mapped with @PropertyName
            canvas.drawText("${record.studentName} - ${record.status}", 20f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AttendanceReport_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            pdfDocument.close()
            sharePDF(filePath)
        } catch (e: Exception) {
            Toast.makeText(this, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePDF(pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Attendance Report"))
    }
}