package com.github.chenlijun99.jetbrainsassignment.coroutine

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service

/**
 * Test implementation of [AppDispatchers] that uses [Dispatchers.EDT]
 * for all dispatchers.
 */
class TestAppDispatchers() : AppDispatchers {
    override val IO
        get() = Dispatchers.EDT
    override val Default
        get() = Dispatchers.EDT
    override val EDT
        get() = Dispatchers.EDT
}
