package com.az.core

import android.content.SharedPreferences
import com.az.core.data.auth.response.SignInResponseData


interface Preferences {
    val sharedPref: SharedPreferences

    fun getLoginStatus(): Int
    fun setLoginStatus(status: LoginStatus)

    fun getLoginSession(): SignInResponseData?
    fun setLoginSession(session: SignInResponseData)
    fun clearLoginSession()
    fun clearLoginStatus()
}