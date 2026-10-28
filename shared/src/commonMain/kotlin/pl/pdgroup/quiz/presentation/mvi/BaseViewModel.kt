package pl.pdgroup.quiz.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : ViewState, I : ViewIntent, E : ViewEffect> : ViewModel() {

    private val initialState: S by lazy { createInitialState() }

    abstract fun createInitialState(): S

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<E> = MutableSharedFlow()
    val effect: SharedFlow<E> = _effect.asSharedFlow()

    fun handleIntent(intent: I) {
        viewModelScope.launch {
            processIntent(intent)
        }
    }

    protected abstract suspend fun processIntent(intent: I)

    protected fun setState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    protected fun setEffect(builder: () -> E) {
        val effectValue = builder()
        viewModelScope.launch { _effect.emit(effectValue) }
    }
}