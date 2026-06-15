package org.newsagg.project.util

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

fun ComponentContext.coroutineScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
): CoroutineScope {
    val scope =  CoroutineScope(SupervisorJob() + dispatcher)
    lifecycle.doOnDestroy {
        scope.cancel()
    }
    return scope
}