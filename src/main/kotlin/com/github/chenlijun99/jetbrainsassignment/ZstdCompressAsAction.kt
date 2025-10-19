package com.github.chenlijun99.jetbrainsassignment

import java.io.FileOutputStream
import java.io.IOException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VfsUtil

import com.github.luben.zstd.Zstd

class ZstdCompressAsAction : DumbAwareAction() {
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(Constants.NOTIFICATION_GROUP)

    override fun actionPerformed(e: AnActionEvent) {
        thisLogger().info("ZstdCompressAsAction triggered")

        // Can be null, if not null gives more contextual info to user
        val project = e.project

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val fileContent = try {
            VfsUtil.loadText(virtualFile)
        } catch (ioe: IOException) {
            thisLogger().error("Failed to read file content: ${virtualFile.path}", ioe)
            notificationGroup
                .createNotification(
                    Bundle.message("file.read.error.title"),
                    Bundle.message("file.read.error.message", virtualFile.name, ioe.localizedMessage),
                    NotificationType.ERROR
                )
                .notify(project)
            return
        }

        val defaultFileName = "${virtualFile.name}.zst"
        val descriptor = FileSaverDescriptor(
            e.presentation.text,
            "",
        )
        val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        val saveFileWrapper = saveFileDialog.save(virtualFile.parent, defaultFileName)

        val selectedFile = saveFileWrapper?.file?.toPath()
        if (selectedFile == null) {
            thisLogger().info("File save dialog cancelled by user.")
            return
        }

        ZstdCompressionUtil.performCompression(project, virtualFile, selectedFile)
    }
}
