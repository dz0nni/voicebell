package com.voicebell.clock.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for downloading and installing Vosk speech recognition model.
 *
 * The model is ~40MB and downloaded on first use to keep APK size small.
 * Supports offline-first architecture as per the original plan.
 */
@Singleton
class VoskModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "VoskModelManager"

        // Vosk model details
        private const val MODEL_NAME = "vosk-model-small-en-us-0.15"
        private const val MODEL_URL = "https://alphacephei.com/vosk/models/$MODEL_NAME.zip"
        private const val MODEL_DIR_NAME = "vosk-model"

        // Alternative: Include model in assets (uncomment if bundling in APK)
        // private const val MODEL_ASSET_NAME = "vosk-model-small-en-us-0.15"
    }

    private val modelDir = File(context.filesDir, MODEL_DIR_NAME)

    /**
     * Check if Vosk model is downloaded and ready.
     */
    fun isModelDownloaded(): Boolean {
        val exists = modelDir.exists() && modelDir.isDirectory && modelDir.listFiles()?.isNotEmpty() == true
        Log.d(TAG, "Model exists: $exists at ${modelDir.absolutePath}")
        return exists
    }

    /**
     * Get the path to the model directory.
     */
    fun getModelPath(): String {
        // The ZIP file contains a subdirectory with the model name
        // Check if subdirectory exists, otherwise use modelDir directly
        val subdirectory = File(modelDir, MODEL_NAME)
        return if (subdirectory.exists() && subdirectory.isDirectory) {
            subdirectory.absolutePath
        } else {
            modelDir.absolutePath
        }
    }

    /**
     * Download and extract Vosk model from the internet.
     *
     * @param onProgress Callback for download progress (0.0 to 1.0)
     * @return Result indicating success or failure
     */
    suspend fun downloadModel(
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting model download from $MODEL_URL")

            // Create model directory if it doesn't exist
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            // Download ZIP file
            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    Exception("Server returned HTTP ${connection.responseCode}")
                )
            }

            val fileLength = connection.contentLength
            Log.d(TAG, "File size: ${fileLength / 1024 / 1024} MB")

            // Download with progress tracking
            connection.inputStream.use { input ->
                val tempZipFile = File(context.cacheDir, "$MODEL_NAME.zip")
                FileOutputStream(tempZipFile).use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (fileLength > 0) {
                            val progress = totalBytesRead.toFloat() / fileLength
                            onProgress(progress * 0.7f) // 70% for download
                        }
                    }
                }

                // Extract ZIP
                Log.d(TAG, "Extracting model...")
                extractZip(tempZipFile, modelDir) { extractProgress ->
                    onProgress(0.7f + extractProgress * 0.3f) // 30% for extraction
                }

                // Clean up temp file
                tempZipFile.delete()
            }

            Log.i(TAG, "Model downloaded and extracted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to download model", e)

            // Clean up on failure
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }

            Result.failure(e)
        }
    }

    /**
     * Extract Vosk model from assets (if bundled in APK).
     * Alternative to downloading from internet.
     *
     * @param onProgress Callback for extraction progress (0.0 to 1.0)
     * @return Result indicating success or failure
     */
    suspend fun extractModelFromAssets(
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Extracting model from assets")

            // Create model directory
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            // Open assets ZIP
            context.assets.open("$MODEL_NAME.zip").use { input ->
                val tempZipFile = File(context.cacheDir, "$MODEL_NAME.zip")
                FileOutputStream(tempZipFile).use { output ->
                    input.copyTo(output)
                }

                // Extract ZIP
                extractZip(tempZipFile, modelDir, onProgress)

                // Clean up
                tempZipFile.delete()
            }

            Log.i(TAG, "Model extracted from assets successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract model from assets", e)

            // Clean up on failure
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }

            Result.failure(e)
        }
    }

    /**
     * Delete downloaded model to free up space.
     */
    fun deleteModel(): Boolean {
        return try {
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
                Log.i(TAG, "Model deleted")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model", e)
            false
        }
    }

    /**
     * Get model size in bytes.
     */
    fun getModelSize(): Long {
        return if (modelDir.exists()) {
            modelDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } else {
            0L
        }
    }

    /**
     * Extract ZIP file to destination directory.
     */
    private fun extractZip(
        zipFile: File,
        destDir: File,
        onProgress: (Float) -> Unit = {}
    ) {
        ZipInputStream(zipFile.inputStream()).use { zipInput ->
            var entry = zipInput.nextEntry
            var entryCount = 0

            while (entry != null) {
                val file = File(destDir, entry.name)

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        zipInput.copyTo(output)
                    }
                }

                entryCount++
                if (entryCount % 10 == 0) {
                    onProgress(entryCount.toFloat() / 100) // Approximate progress
                }

                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }

            onProgress(1.0f)
        }
    }
}
