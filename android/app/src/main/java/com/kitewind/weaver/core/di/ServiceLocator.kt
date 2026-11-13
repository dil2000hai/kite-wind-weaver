package com.kitewind.weaver.core.di

import android.content.Context
import com.kitewind.weaver.core.feedback.FeedbackController
import com.kitewind.weaver.data.local.MeadowPreferences
import com.kitewind.weaver.data.repository.MeadowRepositoryImpl
import com.kitewind.weaver.domain.repository.MeadowRepository

object ServiceLocator {
    private var storage: MeadowRepository? = null
    private var provider: AppViewModelFactory? = null
    val feedback = FeedbackController()
    val factory: AppViewModelFactory get() = checkNotNull(provider)
    fun initialize(context: Context) {
        if (storage != null) return
        val repository = MeadowRepositoryImpl(MeadowPreferences(context.applicationContext))
        storage = repository
        provider = AppViewModelFactory(repository)
    }
}
