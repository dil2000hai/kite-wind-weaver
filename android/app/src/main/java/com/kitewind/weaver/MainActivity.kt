package com.kitewind.weaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kitewind.weaver.core.di.ServiceLocator
import com.kitewind.weaver.core.theme.AppTheme
import com.kitewind.weaver.presentation.app.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent { AppTheme { AppRoot() } }
    }
    override fun onStart() {
        super.onStart()
        ServiceLocator.feedback.start()
    }
    override fun onStop() {
        ServiceLocator.feedback.stop()
        super.onStop()
    }
}
