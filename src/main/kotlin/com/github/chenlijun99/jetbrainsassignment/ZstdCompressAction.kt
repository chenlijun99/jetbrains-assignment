package com.github.chenlijun99.jetbrainsassignment

import com.intellij.openapi.components.service
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.DumbAwareAction

import com.github.chenlijun99.jetbrainsassignment.services.ZstdCompressionService

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

        service<ZstdCompressionService>().performCompression(project, virtualFile, newFilePath)
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
