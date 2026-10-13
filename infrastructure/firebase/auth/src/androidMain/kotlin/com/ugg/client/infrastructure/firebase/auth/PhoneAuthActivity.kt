package com.ugg.client.infrastructure.firebase.auth

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Firebase needs an Activity to send a code (it may show a reCAPTCHA fallback). The app hands its
 * Activity over in `onCreate`; held weakly so a destroyed Activity is not kept alive.
 */
object PhoneAuthActivity {
    private var reference: WeakReference<Activity>? = null

    fun attach(activity: Activity) {
        reference = WeakReference(activity)
    }

    internal val current: Activity?
        get() = reference?.get()?.takeUnless { it.isFinishing || it.isDestroyed }
}
