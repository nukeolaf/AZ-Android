package com.az.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.az.core.data.auth.response.SignInResponseData

class PreferencesImpl(context: Context) : Preferences {

    override val sharedPref: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)

    private fun editSharedPreference(action: (s: SharedPreferences.Editor) -> Unit) {
        with(sharedPref.edit()) {
            action(this)
            apply()
        }
    }

    override fun getLoginStatus(): Int =
        sharedPref.getInt(SHARED_PREF_KEY.LOGIN_STATUS, LoginStatus.UNDEFINED.status)

    override fun setLoginStatus(loginStatus: LoginStatus) {
        editSharedPreference {
            it.putInt(SHARED_PREF_KEY.LOGIN_STATUS, loginStatus.status)
        }
    }

    override fun getLoginSession(): SignInResponseData? {
        val sessionJson = sharedPref.getString(SHARED_PREF_KEY.USER_SESSION, null) ?: return null
        return Gson().fromJson(sessionJson, SignInResponseData::class.java)
    }

    override fun setLoginSession(session: SignInResponseData) {
        editSharedPreference {
            it.putString(SHARED_PREF_KEY.USER_SESSION, Gson().toJson(session))
        }
    }

    override fun clearLoginSession() {
        editSharedPreference {
            it.remove(SHARED_PREF_KEY.USER_SESSION)
        }
    }

    override fun clearLoginStatus() {
        editSharedPreference {
            it.remove(SHARED_PREF_KEY.LOGIN_STATUS)
        }
    }
}