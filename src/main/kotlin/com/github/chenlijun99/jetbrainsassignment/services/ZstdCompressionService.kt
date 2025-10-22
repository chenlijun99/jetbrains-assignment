package com.github.chenlijun99.jetbrainsassignment.services

import java.io.IOException
import java.nio.file.Path
import java.nio.file.Files

import kotlin.math.log10
import kotlin.math.pow
import kotlin.time.measureTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

import com.github.luben.zstd.Zstd

import com.github.chenlijun99.jetbrainsassignment.Constants
import com.github.chenlijun99.jetbrainsassignment.Bundle
import com.github.chenlijun99.jetbrainsassignment.coroutine.AppDispatchers
import com.github.chenlijun99.jetbrainsassignment.coroutine.DefaultAppDispatchers

@Service
class ZstdCompressionService(
    private val cs: CoroutineScope
) {
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(Constants.NOTIFICATION_GROUP)

    fun performCompression(
        project: Project?,
        sourceFile: VirtualFile,
        targetPath: Path,
        dispatchers: AppDispatchers = DefaultAppDispatchers()
    ): Job {
        return cs.launch(dispatchers.Default) {
            val fileContentBytes = try {
                sourceFile.contentsToByteArray()
            } catch (e: IOException) {
                //run {
                withContext(dispatchers.EDT) {
                    thisLogger().error("Failed to read file content: ${sourceFile.path}", e)
                    notificationGroup
                        .createNotification(
                            Bundle.message("file.read.error.title"),
                            Bundle.message("file.read.error.message", sourceFile.name, e.localizedMessage),
                            NotificationType.ERROR
                        )
                        .notify(project)
                }
                return@launch
            }

            val originalSize = fileContentBytes.size.toLong()

            var compressedData: ByteArray
            val elapsed = measureTime {
                val compressionLevel = 3
                compressedData = Zstd.compress(fileContentBytes, compressionLevel)
            }
            thisLogger().info("File compression performed in $elapsed")

            val compressedSize = compressedData.size.toLong()
            val compressionPercentage = if (originalSize > 0) {
                ((originalSize - compressedSize).toDouble() / originalSize.toDouble()) * 100.0
            } else {
                0.0
            }

            val error = withContext(dispatchers.IO) {
                try {
                    Files.write(targetPath, compressedData)
                    null
                } catch (e: IOException) {
                    e
                }
            }

            withContext(dispatchers.EDT) {
                if (error == null) {
                    notificationGroup
                        .createNotification(
                            Bundle.message("file.created.title"),
                            Bundle.message(
                                "file.created.message",
                                targetPath,
                                formatBytes(originalSize),
                                formatBytes(compressedSize),
                                String.format("%.2f", compressionPercentage)
                            ),
                            NotificationType.INFORMATION
                        )
                        .notify(project)

                    // Trigger asynchronous VFS refresh so that compressed file 
                    // is almost immediately visible in project tree.
                    VfsUtil.markDirtyAndRefresh(
                        true,
                        false,
                        false,
                        targetPath.toFile()
                    )
                } else {
                    thisLogger().error("Failed to write compressed file content: ${targetPath}", error)
                    notificationGroup
                        .createNotification(
                            Bundle.message("file.write.error.title"),
                            Bundle.message("file.write.error.message", sourceFile.name, error.localizedMessage),
                            NotificationType.ERROR
                        )
                        .notify(project)
                }
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }
}
