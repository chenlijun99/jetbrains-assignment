package com.github.chenlijun99.jetbrainsassignment

import java.io.IOException
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.application.EDT
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.application.edtWriteAction
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationGroupManager

import com.github.luben.zstd.Zstd

class ZstdCompressAction : DumbAwareAction() {
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(Constants.NOTIFICATION_GROUP)

    override fun actionPerformed(e: AnActionEvent) {
        thisLogger().info("ZstdCompressAction triggered")

        // Can be null, if not null gives more contextual info to user
        val project = e.project

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val parentDirectory = virtualFile.parent ?: return
        val newFileName = "${virtualFile.name}.zst"
        val existingFile = parentDirectory.findChild(newFileName)
        val doCompression = if (existingFile != null) {
            Messages.showYesNoDialog(
                project,
                Bundle.message("file.exists.message", newFileName),
                Bundle.message("file.exists.title"),
                Messages.getWarningIcon()
            ) == Messages.YES
        } else true

        if (!doCompression) {
            return;
        }

        val fileContent = try {
            VfsUtil.loadText(virtualFile)
        } catch (e: IOException) {
            thisLogger().error("Failed to read file content: ${virtualFile.path}", e)
            notificationGroup
                .createNotification(
                    Bundle.message("file.read.error.title"),
                    Bundle.message("file.read.error.message", virtualFile.name, e.localizedMessage),
                    NotificationType.ERROR
                )
                .notify(project)
            return
        }

        currentThreadCoroutineScope().launch {
            val compressedData = withContext(Dispatchers.Default) {
                var compressed: ByteArray
                val elapsed = measureTime {
                    val compressionLevel = 3
                    compressed = Zstd.compress(fileContent.toByteArray(Charsets.UTF_8), compressionLevel)
                }
                thisLogger().info("File compression performed in $elapsed")
                compressed
            }


            withContext(Dispatchers.EDT) {
                try {
                    val newFile = edtWriteAction {
                        val newFile =
                            if (existingFile?.isValid == true) {
                                existingFile
                            } else {
                                parentDirectory.createChildData(this, newFileName)
                            }
                        newFile.setBinaryContent(compressedData)
                        newFile
                    }
                    notificationGroup
                        .createNotification(
                            Bundle.message("file.created.title"),
                            Bundle.message("file.created.message", newFile.name),
                            NotificationType.INFORMATION
                        )
                        .notify(project)
                } catch (e: IOException) {
                    notificationGroup
                        .createNotification(
                            Bundle.message("file.write.error.title"),
                            Bundle.message("file.write.error.message", virtualFile.name, e.localizedMessage),
                            NotificationType.ERROR
                        )
                        .notify(project)

                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        /*
         * Enable the action only if folder of the current file is writable
         * (e.g. the current file is not within a jar file).
         */
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabled = virtualFile?.parent?.isWritable == true
    }
}
