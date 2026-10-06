package com.example.ui.util

import android.content.Context
import android.util.Log
import org.osmdroid.config.Configuration
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * Pre-installs and extracts bundled OpenStreetMap tiles and offline assets
 * directly from the application APK assets on initial launch.
 * Ensures 100% offline functionality right out of the box with zero internet connection required.
 */
object OfflineMapBundleManager {

    private const val TAG = "OfflineMapBundle"
    private const val PREFS_KEY_INSTALLED = "bourges_offline_map_bundle_installed_v2"
    private const val ASSET_MAP_ZIP = "bourges_offline_map.zip"

    fun installOfflineMapBundle(context: Context) {
        val prefs = context.getSharedPreferences("bourges_user_settings", Context.MODE_PRIVATE)
        val isAlreadyInstalled = prefs.getBoolean(PREFS_KEY_INSTALLED, false)

        try {
            val config = Configuration.getInstance()
            config.userAgentValue = context.packageName

            val basePath = config.osmdroidBasePath ?: File(context.filesDir, "osmdroid")
            if (!basePath.exists()) basePath.mkdirs()

            val tileCache = config.osmdroidTileCache ?: File(context.cacheDir, "osmdroid/tiles")
            if (!tileCache.exists()) tileCache.mkdirs()

            // 1. Copy archive to basePath so OsmDroid ArchiveFileFactory can discover it
            val destZip = File(basePath, ASSET_MAP_ZIP)
            if (!destZip.exists() || !isAlreadyInstalled) {
                context.assets.open(ASSET_MAP_ZIP).use { input ->
                    FileOutputStream(destZip).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.i(TAG, "Copied $ASSET_MAP_ZIP to ${destZip.absolutePath} (${destZip.length()} bytes)")
            }

            // 2. Extract tiles directly into tileCache/Mapnik/{z}/{x}/{y}.png.tile & .png for instant local access
            if (!isAlreadyInstalled) {
                var extractedCount = 0
                context.assets.open(ASSET_MAP_ZIP).use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        val buffer = ByteArray(8192)
                        while (entry != null) {
                            if (!entry.isDirectory && entry.name.endsWith(".png")) {
                                // Extract as standard file and as .tile file for osmdroid
                                val targetFile = File(tileCache, entry.name)
                                targetFile.parentFile?.mkdirs()
                                val tileFile = File(tileCache, entry.name + ".tile")

                                FileOutputStream(targetFile).use { out ->
                                    var len: Int
                                    while (zip.read(buffer).also { len = it } > 0) {
                                        out.write(buffer, 0, len)
                                    }
                                }
                                // Duplicate as .tile
                                try {
                                    targetFile.copyTo(tileFile, overwrite = true)
                                } catch (_: Exception) {}

                                extractedCount++
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }
                Log.i(TAG, "Successfully extracted $extractedCount offline map tiles to ${tileCache.absolutePath}")
                prefs.edit().putBoolean(PREFS_KEY_INSTALLED, true).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error installing offline map bundle from assets", e)
        }
    }
}
