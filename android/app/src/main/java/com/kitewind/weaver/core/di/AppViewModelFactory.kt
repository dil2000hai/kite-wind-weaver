package com.kitewind.weaver.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.*
import com.kitewind.weaver.presentation.app.AppViewModel
import com.kitewind.weaver.presentation.splash.SplashViewModel
import com.kitewind.weaver.presentation.menu.MenuViewModel
import com.kitewind.weaver.presentation.game.GameViewModel
import com.kitewind.weaver.presentation.calm.CalmModeViewModel
import com.kitewind.weaver.presentation.gameover.GameOverViewModel
import com.kitewind.weaver.presentation.meadow.MeadowViewModel
import com.kitewind.weaver.presentation.kitebox.KiteBoxViewModel
import com.kitewind.weaver.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val repository: MeadowRepository) : ViewModelProvider.Factory {
    private val rules = MeadowRules(repository.hills)
    private val engine = FlightEngine()
    private val credit = CreditSeedUseCase(repository, rules)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val model: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel(repository)
            SplashViewModel::class.java -> SplashViewModel(repository)
            MenuViewModel::class.java -> MenuViewModel(repository, rules)
            GameViewModel::class.java -> GameViewModel(repository, engine, credit, rules)
            CalmModeViewModel::class.java -> CalmModeViewModel(repository, engine, credit, rules)
            GameOverViewModel::class.java -> GameOverViewModel(repository, rules)
            MeadowViewModel::class.java -> MeadowViewModel(repository, rules)
            KiteBoxViewModel::class.java -> KiteBoxViewModel(repository, rules)
            SettingsViewModel::class.java -> SettingsViewModel(repository, rules)
            else -> error(modelClass.name)
        }
        @Suppress("UNCHECKED_CAST")
        return model as T
    }
}
