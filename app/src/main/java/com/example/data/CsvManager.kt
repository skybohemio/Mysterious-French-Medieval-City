package com.example.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object CsvManager {

    val CSV_HEADER = "ID;Nom;Latitude;Longitude;Catégorie;Description (FR);Description (EN);Narration Text (FR);Narration Text (EN);Article Mystere;Image URL"

    /**
     * Escapes a single CSV field using standard CSV quoting rules.
     */
    fun escapeCsvField(value: String): String {
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return value
    }

    /**
     * Serializes a list of Site objects into Excel-compatible CSV format (semicolon delimited).
     */
    fun exportToCsvString(sites: List<Site>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")

        for (site in sites) {
            sb.append(site.id).append(";")
            sb.append(escapeCsvField(site.title)).append(";")
            sb.append(site.latitude).append(";")
            sb.append(site.longitude).append(";")
            sb.append(escapeCsvField(site.category)).append(";")
            sb.append(escapeCsvField(site.descriptionFr.ifBlank { site.description })).append(";")
            sb.append(escapeCsvField(site.descriptionEn.ifBlank { site.description })).append(";")
            sb.append(escapeCsvField(site.narrationFr.ifBlank { site.narrationText })).append(";")
            sb.append(escapeCsvField(site.narrationEn.ifBlank { site.narrationText })).append(";")
            sb.append(escapeCsvField(site.mysteryArticle)).append(";")
            sb.append(escapeCsvField(site.imageUrl))
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Robust CSV parser supporting quotes, escaped quotes, multiline cells, and both comma and semicolon delimiters.
     */
    fun parseCsv(content: String): List<List<String>> {
        val firstLine = content.lineSequence().firstOrNull() ?: ""
        val delimiter = if (firstLine.count { it == ';' } >= firstLine.count { it == ',' }) ';' else ','

        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < content.length) {
            val char = content[i]

            if (insideQuotes) {
                if (char == '"') {
                    if (i + 1 < content.length && content[i + 1] == '"') {
                        currentField.append('"')
                        i++ // Skip escaped quote
                    } else {
                        insideQuotes = false
                    }
                } else {
                    currentField.append(char)
                }
            } else {
                when {
                    char == '"' -> {
                        insideQuotes = true
                    }
                    char == delimiter -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                    }
                    char == '\r' -> {
                        // Skip carriage return
                    }
                    char == '\n' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    else -> {
                        currentField.append(char)
                    }
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }

    /**
     * Parses a CSV text into a list of Site entities.
     */
    fun parseSitesFromCsv(content: String): List<Site> {
        val records = parseCsv(content)
        if (records.isEmpty()) return emptyList()

        val sites = mutableListOf<Site>()
        val firstHeader = records[0].firstOrNull()?.replace("\uFEFF", "")?.trim()
        val startIndex = if (firstHeader?.equals("ID", ignoreCase = true) == true) 1 else 0

        for (index in startIndex until records.size) {
            val record = records[index]
            if (record.size < 4) continue

            val id = record[0].replace("\uFEFF", "").trim().toIntOrNull() ?: continue
            val title = record.getOrElse(1) { "" }.trim()
            if (title.isBlank()) continue

            val lat = record.getOrElse(2) { "0.0" }.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            val lng = record.getOrElse(3) { "0.0" }.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            val category = record.getOrElse(4) { "CATHEDRAL" }.trim()
            val descFr = record.getOrElse(5) { title }.trim()
            val descEn = record.getOrElse(6) { descFr }.trim()
            val narrFr = record.getOrElse(7) { descFr }.trim()
            val narrEn = record.getOrElse(8) { narrFr }.trim()
            val mystery = record.getOrElse(9) { "" }.trim()
            val imgUrl = record.getOrElse(10) { "" }.trim()

            val site = Site(
                id = id,
                title = title,
                description = descFr,
                narrationText = narrFr,
                latitude = lat,
                longitude = lng,
                category = category,
                isPreset = true,
                audioDurationSec = 300,
                titleFr = title,
                titleEn = title,
                titleDe = title,
                titleEs = title,
                titleNl = title,
                descriptionFr = descFr,
                descriptionEn = descEn,
                descriptionDe = descFr,
                descriptionEs = descFr,
                descriptionNl = descFr,
                narrationFr = narrFr,
                narrationEn = narrEn,
                narrationDe = narrFr,
                narrationEs = narrFr,
                narrationNl = narrFr,
                mysteryArticle = mystery,
                imageUrl = imgUrl
            )
            sites.add(site)
        }

        return sites
    }

    /**
     * Saves CSV with UTF-8 BOM so Excel opens it with French accents perfectly,
     * and shares via Android Share Sheet.
     */
    fun exportAndShareCsv(context: Context, sites: List<Site>) {
        val csvString = exportToCsvString(sites)
        val exportFile = File(context.cacheDir, "bourges_mysteres_points_interet.csv")
        
        // Write UTF-8 BOM for Microsoft Excel compatibility
        exportFile.outputStream().use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            fos.write(csvString.toByteArray(Charsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Export POI - Les Mystères de Bourges à vos oreilles")
            putExtra(Intent.EXTRA_TEXT, "Voici l'export CSV de tous les points d'intérêt et mystères de Bourges pour Excel.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Exporter les mystères de Bourges vers Excel"))
    }
}
