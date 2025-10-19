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

    override fun actionPerformed(e: AnActionEvent) {
        thisLogger().info("ZstdCompressAction triggered")

        // Can be null, if not null gives more contextual info to user
        val project = e.project

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val parentDirectory = virtualFile.parent ?: return

        var newFilePath = virtualFile.toNioPath()
        newFilePath = newFilePath.parent.resolve("${newFilePath.fileName}.zst")

        val existingFile = parentDirectory.findChild(newFilePath.fileName.toString())
        val doCompression = if (existingFile != null) {
            Messages.showYesNoDialog(
                project,
                Bundle.message("file.exists.message", newFilePath),
                Bundle.message("file.exists.title"),
                Messages.getWarningIcon()
            ) == Messages.YES
        } else true

        if (!doCompression) {
            return;
        }

        ZstdCompressionUtil.performCompression(project, virtualFile, newFilePath)
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
