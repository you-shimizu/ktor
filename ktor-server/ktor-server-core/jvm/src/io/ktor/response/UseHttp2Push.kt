/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.response

/**
 * HTTP/2 push is no longer supported by Chrome web browser.
 * Other browsers may discard it at some point.
 * With such browsers, HTTP/2 push will be disabled, therefore
 * using this feature is safe but it will have no effect.
 * On the other hand, this feature is not deprecated and generally it is still allowed
 * to use it, so feel free to opt-in this annotation to eliminate this warning, if
 * you are sure that you need it. For example, it makes sense to use with
 * a non-browser client that for sure supports HTTP/2 push.
 */
@RequiresOptIn(
    "HTTP/2 push is no longer supported by some web browsers.",
    level = RequiresOptIn.Level.WARNING
)
public annotation class UseHttp2Push
