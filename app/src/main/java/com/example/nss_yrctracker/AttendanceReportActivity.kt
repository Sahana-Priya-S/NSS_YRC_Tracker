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

    // These names are now synced with your XML IDs
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnExport: Button
    private val db = FirebaseFirestore.getInstance()
    private val attendanceList = mutableListOf<AttendanceRecord>()
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_report)

        // SYNCED: Matches your XML android:id="@+id/recyclerReport"
        recyclerView = findViewById(R.id.recyclerReport)

        // SYNCED: Matches your XML android:id="@+id/btnExportPdf"
        btnExport = findViewById(R.id.btnExportPdf)

        recyclerView.layoutManager = LinearLayoutManager(this)

        eventId = intent.getStringExtra("EVENT_ID") ?: ""

        loadAttendanceData()

        btnExport.setOnClickListener {
            generateAndSharePDF()
        }
    }

    private fun loadAttendanceData() {
        // Uses the enabled index for the attendance collection
        db.collection("attendance")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshots ->
                attendanceList.clear()
                for (doc in snapshots) {
                    val record = doc.toObject(AttendanceRecord::class.java)
                    attendanceList.add(record)
                }
                recyclerView.adapter = AttendanceReportAdapter(attendanceList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
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
            // This line will no longer be red if the data class has 'studentName'
            canvas.drawText("${record.studentName} - ${record.status}", 20f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        // Saves to the device's Documents folder
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Report_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            pdfDocument.close()

            // Opens the Android Share Sheet (Save as / Share)
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Security requirement
        }

        startActivity(Intent.createChooser(intent, "Share or Save PDF"))
    }
}