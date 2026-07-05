package com.tracker.construction.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.tracker.construction.data.entities.Project
import com.tracker.construction.data.entities.UnitRecord
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ExportFormat { PDF, XLSX, CSV }

/** Generates the requested export file and opens the system share sheet so the supervisor
 * can send it by email, save it to Drive, or hand it off any other way — fully offline
 * until the person chooses where to send it. */
fun exportAndShare(context: Context, project: Project, units: List<UnitRecord>, format: ExportFormat) {
    val file = when (format) {
        ExportFormat.PDF -> ExportUtil.exportPdf(context, project, units)
        ExportFormat.XLSX -> ExportUtil.exportXlsx(context, project, units)
        ExportFormat.CSV -> ExportUtil.exportCsv(context, project, units)
    }
    val uri = FileProvider.getUriForFile(context, "com.tracker.construction.fileprovider", file)
    val mime = when (format) {
        ExportFormat.PDF -> "application/pdf"
        ExportFormat.XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ExportFormat.CSV -> "text/csv"
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Share export").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

private fun stamp(millis: Long?): String = millis?.let { dateFmt.format(Date(it)) } ?: ""
private fun mark(done: Boolean): String = if (done) "YES" else "NO"

object ExportUtil {

    private fun exportsDir(context: Context): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun rows(units: List<UnitRecord>): List<List<String>> {
        val header = listOf("Unit", "Tile", "Grout", "LVP", "Silicone", "QC", "Tile At", "Grout At", "LVP At", "Silicone At", "QC At")
        val body = units.sortedWith(compareBy { it.sortOrder }).map { u ->
            listOf(
                u.unitNumber,
                mark(u.tileDone), mark(u.groutDone), mark(u.lvpDone), mark(u.siliconeDone), mark(u.qcDone),
                stamp(u.tileAt), stamp(u.groutAt), stamp(u.lvpAt), stamp(u.siliconeAt), stamp(u.qcAt)
            )
        }
        return listOf(header) + body
    }

    fun exportCsv(context: Context, project: Project, units: List<UnitRecord>): File {
        val safeName = project.name.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(exportsDir(context), "${safeName}_export.csv")
        file.bufferedWriter().use { writer ->
            rows(units).forEach { row ->
                writer.write(row.joinToString(",") { cell -> "\"${cell.replace("\"", "\"\"")}\"" })
                writer.newLine()
            }
        }
        return file
    }

    fun exportPdf(context: Context, project: Project, units: List<UnitRecord>): File {
        val safeName = project.name.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(exportsDir(context), "${safeName}_export.pdf")

        val doc = PdfDocument()
        val pageWidth = 842 // A4 landscape points
        val pageHeight = 595
        val margin = 24f
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 10f; isFakeBoldText = true }
        val cellPaint = Paint().apply { textSize = 9f }
        val colWidths = listOf(70f, 60f, 60f, 60f, 65f, 60f, 90f, 90f, 90f, 90f, 90f)
        val rowHeight = 18f

        val sorted = units.sortedWith(compareBy { it.sortOrder })
        val header = listOf("Unit", "Tile", "Grout", "LVP", "Silicone", "QC", "Tile At", "Grout At", "LVP At", "Silicone At", "QC At")
        val dataRows = rows(sorted).drop(1)

        var rowIndex = 0
        var pageNum = 1
        while (rowIndex == 0 || rowIndex < dataRows.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = doc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = margin + 20f
            canvas.drawText("${project.name} — Unit Progress (page $pageNum)", margin, y, titlePaint)
            y += 20f

            var x = margin
            header.forEachIndexed { i, h ->
                canvas.drawText(h, x, y, headerPaint)
                x += colWidths[i]
            }
            y += rowHeight

            val maxRowsPerPage = ((pageHeight - y - margin) / rowHeight).toInt()
            var printed = 0
            while (rowIndex < dataRows.size && printed < maxRowsPerPage) {
                x = margin
                dataRows[rowIndex].forEachIndexed { i, cell ->
                    canvas.drawText(cell, x, y, cellPaint)
                    x += colWidths[i]
                }
                y += rowHeight
                rowIndex++
                printed++
            }

            doc.finishPage(page)
            pageNum++
            if (dataRows.isEmpty()) break
        }

        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * Writes a minimal but valid .xlsx (OOXML) file by hand — no external library required,
     * so the app stays lightweight and fully offline.
     */
    fun exportXlsx(context: Context, project: Project, units: List<UnitRecord>): File {
        val safeName = project.name.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(exportsDir(context), "${safeName}_export.xlsx")
        val allRows = rows(units)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zip ->
            fun writeEntry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }

            writeEntry(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
            )

            writeEntry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            )

            writeEntry(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
            )

            writeEntry(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Units" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""
            )

            fun escapeXml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

            val sb = StringBuilder()
            sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
            allRows.forEachIndexed { r, row ->
                sb.append("<row r=\"${r + 1}\">")
                row.forEachIndexed { c, cell ->
                    val colLetter = ('A' + c)
                    sb.append("<c r=\"$colLetter${r + 1}\" t=\"inlineStr\"><is><t>${escapeXml(cell)}</t></is></c>")
                }
                sb.append("</row>")
            }
            sb.append("</sheetData></worksheet>")

            writeEntry("xl/worksheets/sheet1.xml", sb.toString())
        }
        return file
    }
}
