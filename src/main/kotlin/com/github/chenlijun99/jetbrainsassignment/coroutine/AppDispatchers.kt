package com.github.chenlijun99.jetbrainsassignment.coroutine

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service

/**
 * Provides different CoroutineDispatchers for dependency injection.
 * In a production environment, this will provide the standard Dispatchers.
 * In tests, a test-specific implementation can be swapped in 
 */
@Suppress("PropertyName")
interface AppDispatchers {
    val IO: CoroutineContext
    val Default: CoroutineContext
    val EDT: CoroutineContext
}

/**
 * Default production implementation of [AppDispatchers].
 */
class DefaultAppDispatchers : AppDispatchers {
    override val IO
        get() = Dispatchers.IO
    override val Default
        get() = Dispatchers.Default
    override val EDT
        get() = Dispatchers.EDT
}
