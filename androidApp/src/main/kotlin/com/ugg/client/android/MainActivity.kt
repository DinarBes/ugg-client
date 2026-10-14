package com.ugg.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthActivity
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.core.domain.AppInfo
import com.ugg.client.shared.ContentView
import com.ugg.client.shared.TemplateApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Firebase needs a live Activity to send an SMS code (reCAPTCHA fallback).
        PhoneAuthActivity.attach(this)

        val app = TemplateApp {
            environment = AppEnvironment.PRODUCTION
            appInfo = AppInfo(versionName = BuildConfig.VERSION_NAME, versionCode = BuildConfig.VERSION_CODE)
            // Persistent, encrypted storage: tokens and the «onboarding seen» flag survive a
            // restart. InMemoryKeystore here would forget them every time the app is killed.
            keystore {
                context = applicationContext
                filename = "ugg_client_secure_storage"
            }
            core = TemplateCore {
                androidContext = this@MainActivity
            }
        }

        setContent {
            ContentView(app)
        }
    }
}

@Preview
@Composable
fun ContentViewAndroidPreview() {
    val app = TemplateApp()

    ContentView(app)
}
