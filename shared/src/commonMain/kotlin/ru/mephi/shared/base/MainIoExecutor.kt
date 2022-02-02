package ru.mephi.shared.base

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.domain.MainDispatcher
import kotlin.coroutines.CoroutineContext

abstract class MainIoExecutor : IExecutorScope, CoroutineScope, KoinComponent {

    private val mainDispatcher: MainDispatcher by inject()
    val ioDispatcher: CoroutineDispatcher by inject()

    private val job: CompletableJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = job + mainDispatcher.dispatcher

    override fun cancel() {
        job.cancel()
    }

    protected fun <T> launch(
        flow: Flow<T>,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ) {
        launch {
            flow
                .flowOn(ioDispatcher)
                .catch {
                    onError?.invoke(it)
                }
                .collect {
                    onSuccess(it)
                }
        }
    }

    protected fun <T> collect(flow: Flow<T>, collect: (T) -> Unit) {
        launch {
            flow.collect { collect(it) }
        }
    }
}