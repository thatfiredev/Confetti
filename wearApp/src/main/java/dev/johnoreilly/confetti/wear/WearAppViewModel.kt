package dev.johnoreilly.confetti.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class WearAppViewModel(
    private val tileUpdater: TileUpdater,
    private val complicationUpdater: ComplicationUpdater,
    val repository: ConfettiRepository,
    phoneSettingsSync: PhoneSettingsSync,
    authentication: Authentication,
) : ViewModel() {
    val appState = combine(
        phoneSettingsSync.settingsFlow,
        phoneSettingsSync.conferenceFlow,
        authentication.currentUser
    ) { phoneSettings, defaultConference, user ->
        AppUiState(
            defaultConference = defaultConference,
            settings = phoneSettings,
            user = user
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun updateSurfaces() {
        tileUpdater.updateAll()
        complicationUpdater.update()
    }

    val waitingOnThemeOrData: Boolean
        get() = appState.value == null
}

