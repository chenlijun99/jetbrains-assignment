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

import com.github.luben.zstd.Zstd

import com.github.chenlijun99.jetbrainsassignment.Bundle

class ZstdCompressAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        thisLogger().info("ZstdCompressAction triggered")

        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (virtualFile == null || project == null) {
            return;
        }
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

        val fileContent = VfsUtil.loadText(virtualFile)

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
                Messages.showInfoMessage(
                    project,
                    Bundle.message("file.created.message", newFile.name),
                    Bundle.message("file.created.title")
                )
            }
        }
    }
}
