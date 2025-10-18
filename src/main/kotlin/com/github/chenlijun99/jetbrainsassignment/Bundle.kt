package com.github.chenlijun99.jetbrainsassignment

import java.util.function.Supplier

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.Bundle"

/**
 * See docs https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundle-class
 */
internal object Bundle {
    private val INSTANCE = DynamicBundle(Bundle::class.java, BUNDLE)

    fun message(
        key: @PropertyKey(resourceBundle = BUNDLE) String,
        vararg params: Any
    ): @Nls String = INSTANCE.getMessage(key, *params)

    fun lazyMessage(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): Supplier<@Nls String> = INSTANCE.getLazyMessage(key, *params)
}
