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

class ZstdCompressAsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // TODO
    }
}

