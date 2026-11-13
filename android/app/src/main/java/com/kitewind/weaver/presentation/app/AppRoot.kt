package com.kitewind.weaver.presentation.app

import android.app.Activity
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitewind.weaver.core.di.ServiceLocator
import com.kitewind.weaver.core.navigation.Screen
import com.kitewind.weaver.core.theme.LocalReducedMotion
import com.kitewind.weaver.domain.model.FlightPhase
import com.kitewind.weaver.presentation.calm.*
import com.kitewind.weaver.presentation.game.*
import com.kitewind.weaver.presentation.gameover.*
import com.kitewind.weaver.presentation.kitebox.*
import com.kitewind.weaver.presentation.meadow.*
import com.kitewind.weaver.presentation.menu.*
import com.kitewind.weaver.presentation.settings.*
import com.kitewind.weaver.presentation.splash.*

@Composable
fun AppRoot() {
    val factory = ServiceLocator.factory
    val app: AppViewModel = viewModel(factory = factory)
    val splash: SplashViewModel = viewModel(factory = factory)
    val menu: MenuViewModel = viewModel(factory = factory)
    val game: GameViewModel = viewModel(factory = factory)
    val calm: CalmModeViewModel = viewModel(factory = factory)
    val result: GameOverViewModel = viewModel(factory = factory)
    val meadow: MeadowViewModel = viewModel(factory = factory)
    val kites: KiteBoxViewModel = viewModel(factory = factory)
    val settings: SettingsViewModel = viewModel(factory = factory)
    val screen by app.screen.collectAsStateWithLifecycle()
    val appState by app.uiState.collectAsStateWithLifecycle()
    val menuState by menu.uiState.collectAsStateWithLifecycle()
    val gameState by game.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val view = LocalView.current
    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = screen != Screen.Splash
                isAppearanceLightNavigationBars = screen != Screen.Splash
            }
        }
    }
    fun play(hill: Int, calmFlight: Boolean = false, again: Boolean = false) {
        val id = app.newRunId()
        val destination = if (calmFlight) {
            calm.start(hill, id)
            Screen.CalmMode(id)
        } else {
            game.start(hill, id)
            Screen.Game(hill, id)
        }
        if (again) app.replay(destination) else app.navigate(destination)
    }
    fun openMeadow(hill: Int) {
        meadow.show(hill)
        app.navigate(Screen.MeadowView(hill))
    }
    fun openResult(id: Long?) {
        if (id == null) return
        result.show(id)
        app.navigate(Screen.GameOver(id))
    }
    fun back() {
        when (app.screen.value) {
            is Screen.Game -> game.pause()
            is Screen.CalmMode -> calm.pause()
            else -> Unit
        }
        app.back()
        when (val previous = app.screen.value) {
            is Screen.Game -> game.activate(previous.runId)
            is Screen.CalmMode -> calm.activate(previous.runId)
            is Screen.GameOver -> result.show(previous.resultId)
            else -> Unit
        }
    }
    fun flightEvent(event: FlightEvent) {
        when (event) {
            is FlightEvent.Result -> {
                val current = app.screen.value
                if ((current is Screen.Game && current.runId == event.result.id) ||
                    (current is Screen.CalmMode && current.runId == event.result.id)) openResult(event.result.id)
            }
            is FlightEvent.Collection -> {
                ServiceLocator.feedback.collection(event.sound)
                if (event.haptics && view.hasWindowFocus()) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
    }
    LaunchedEffect(Unit) { app.events.collect { if (it == AppEvent.Exit) activity?.finish() } }
    LaunchedEffect(Unit) { game.events.collect { flightEvent(it) } }
    LaunchedEffect(Unit) { calm.events.collect { flightEvent(it) } }
    LaunchedEffect(Unit) { settings.events.collect { game.clear(); calm.clear(); app.menu() } }
    LaunchedEffect(Unit) { menu.events.collect { game.clear(); calm.clear(); app.menu() } }
    BackHandler { back() }
    CompositionLocalProvider(LocalReducedMotion provides appState.reducedMotion) {
        AnimatedContent(targetState = screen, transitionSpec = {
            val duration = if (appState.reducedMotion) 120 else 220
            fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
        }, label = "meadowNavigation") { destination ->
            when (destination) {
                Screen.Splash -> SplashRoute(splash, app::splashFinished)
                Screen.Menu -> MenuRoute(menu,
                    canResume = gameState.session?.phase == FlightPhase.Paused && gameState.session?.hill == menuState.progress.selectedHill,
                    onResume = {
                        game.flightState.value.session?.let { session ->
                            game.resume()
                            app.navigate(Screen.Game(session.hill, session.runId))
                        }
                    },
                    onPlay = { play(menu.uiState.value.progress.selectedHill) },
                    onSettings = { app.navigate(Screen.Settings) },
                    onMeadow = { openMeadow(menu.uiState.value.progress.selectedHill) },
                    onKites = { app.navigate(Screen.KiteBox) },
                    onCalm = {
                        val existing = calm.flightState.value.session
                        if (existing == null) play(menu.uiState.value.progress.selectedHill, calmFlight = true)
                        else app.navigate(Screen.CalmMode(existing.runId))
                    })
                is Screen.Game -> GameRoute(game, destination.runId, ::back, app::menu,
                    { openResult(game.flightState.value.resultId) },
                    { play(game.flightState.value.session?.hill ?: destination.hillId, again = true) })
                is Screen.CalmMode -> CalmModeRoute(calm, destination.runId, ::back, app::menu,
                    { openResult(calm.flightState.value.resultId) },
                    { play(calm.flightState.value.session?.hill ?: menuState.progress.selectedHill, calmFlight = true, again = true) })
                is Screen.GameOver -> GameOverRoute(result, ::back,
                    onAgain = { result.uiState.value.result?.let { play(it.hill, it.calm, true) } },
                    onNext = { result.uiState.value.result?.nextHill?.let { play(it, again = true) } },
                    onMeadow = { result.uiState.value.result?.let { openMeadow(it.hill) } }, onMenu = app::menu)
                is Screen.MeadowView -> MeadowRoute(meadow, ::back, { play(it) })
                Screen.KiteBox -> KiteBoxRoute(kites, ::back, app::menu)
                Screen.Settings -> SettingsRoute(settings, ::back, app::menu)
            }
        }
    }
}
