package com.voicebell.clock.presentation.screens.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.WorldClock
import com.voicebell.clock.domain.usecase.worldclock.AddWorldClockUseCase
import com.voicebell.clock.domain.usecase.worldclock.DeleteWorldClockUseCase
import com.voicebell.clock.domain.usecase.worldclock.GetWorldClocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for World Clocks screen following MVI pattern.
 */
@HiltViewModel
class WorldClocksViewModel @Inject constructor(
    private val getWorldClocksUseCase: GetWorldClocksUseCase,
    private val addWorldClockUseCase: AddWorldClockUseCase,
    private val deleteWorldClockUseCase: DeleteWorldClockUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorldClocksState())
    val state: StateFlow<WorldClocksState> = _state.asStateFlow()

    private val _effect = Channel<WorldClocksEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadWorldClocks()
    }

    /**
     * Loads all world clocks from repository.
     */
    private fun loadWorldClocks() {
        viewModelScope.launch {
            getWorldClocksUseCase.invoke().collect { worldClocks ->
                _state.update {
                    it.copy(
                        worldClocks = worldClocks,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handles events from the UI.
     */
    fun onEvent(event: WorldClocksEvent) {
        when (event) {
            is WorldClocksEvent.AddWorldClock -> addWorldClock(event.worldClock)
            is WorldClocksEvent.DeleteWorldClock -> deleteWorldClock(event.id)
            is WorldClocksEvent.ShowDeleteDialog -> showDeleteDialog(event.worldClock)
            is WorldClocksEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is WorldClocksEvent.ShowAddDialog -> showAddDialog()
            is WorldClocksEvent.DismissAddDialog -> dismissAddDialog()
        }
    }

    /**
     * Adds a new world clock.
     */
    private fun addWorldClock(worldClock: WorldClock) {
        viewModelScope.launch {
            addWorldClockUseCase.invoke(worldClock)
                .onSuccess {
                    dismissAddDialog()
                    _effect.send(WorldClocksEffect.ShowMessage("World clock added"))
                }
                .onFailure { error ->
                    _effect.send(
                        WorldClocksEffect.ShowError(
                            error.message ?: "Failed to add world clock"
                        )
                    )
                }
        }
    }

    /**
     * Deletes a world clock.
     */
    private fun deleteWorldClock(id: Long) {
        viewModelScope.launch {
            deleteWorldClockUseCase.invoke(id)
                .onSuccess {
                    dismissDeleteDialog()
                    _effect.send(WorldClocksEffect.ShowMessage("World clock deleted"))
                }
                .onFailure { error ->
                    _effect.send(
                        WorldClocksEffect.ShowError(
                            error.message ?: "Failed to delete world clock"
                        )
                    )
                }
        }
    }

    /**
     * Shows delete confirmation dialog.
     */
    private fun showDeleteDialog(worldClock: WorldClock) {
        _state.update {
            it.copy(
                showDeleteDialog = true,
                worldClockToDelete = worldClock
            )
        }
    }

    /**
     * Dismisses delete confirmation dialog.
     */
    private fun dismissDeleteDialog() {
        _state.update {
            it.copy(
                showDeleteDialog = false,
                worldClockToDelete = null
            )
        }
    }

    /**
     * Shows add world clock dialog.
     */
    private fun showAddDialog() {
        _state.update {
            it.copy(showAddDialog = true)
        }
    }

    /**
     * Dismisses add world clock dialog.
     */
    private fun dismissAddDialog() {
        _state.update {
            it.copy(showAddDialog = false)
        }
    }
}

/**
 * UI state for World Clocks screen.
 */
data class WorldClocksState(
    val worldClocks: List<WorldClock> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val worldClockToDelete: WorldClock? = null
)

/**
 * Events from UI to ViewModel.
 */
sealed class WorldClocksEvent {
    data class AddWorldClock(val worldClock: WorldClock) : WorldClocksEvent()
    data class DeleteWorldClock(val id: Long) : WorldClocksEvent()
    data class ShowDeleteDialog(val worldClock: WorldClock) : WorldClocksEvent()
    object DismissDeleteDialog : WorldClocksEvent()
    object ShowAddDialog : WorldClocksEvent()
    object DismissAddDialog : WorldClocksEvent()
}

/**
 * One-time effects from ViewModel to UI.
 */
sealed class WorldClocksEffect {
    data class ShowMessage(val message: String) : WorldClocksEffect()
    data class ShowError(val message: String) : WorldClocksEffect()
}
