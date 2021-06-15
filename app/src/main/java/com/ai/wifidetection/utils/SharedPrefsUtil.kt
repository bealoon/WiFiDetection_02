package com.ai.wifidetection.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 本地存储SharedPrefs
 * 基本信息存储
 */
object SharedPrefsUtil {
    private val FILENAME = "setting.prf"

    object COMMOM_PARAMSTRING{
        var username = "username"
        var password = "password"
        var token = "token"
        var deviceInfo = "deviceInfo"
        var cookie = "cookie"
        var homeId = "homeId"
        var modeId = "modeId"
    }

    private fun getEditor(paramContext: Context, paramString: String): SharedPreferences.Editor {
        return getSharedPreferences(paramContext, paramString).edit()
    }

    private fun getSharedPreferences(paramContext: Context, paramString: String): SharedPreferences {
        return paramContext.getSharedPreferences(paramString, 0)
    }

    fun getValue(paramContext: Context, paramString: String, paramFloat: Float): Float {
        return getSharedPreferences(paramContext, FILENAME).getFloat(paramString, paramFloat)
    }

    fun getValue(paramContext: Context, paramString: String, paramInt: Int): Int {
        return getSharedPreferences(paramContext, FILENAME).getInt(paramString, paramInt)
    }

    fun getValue(paramContext: Context, paramString: String, paramLong: Long): Long {
        return getSharedPreferences(paramContext, FILENAME).getLong(paramString, paramLong)
    }

    fun getValue(paramContext: Context, paramString1: String, paramString2: String): String? {
        return getSharedPreferences(paramContext, FILENAME).getString(paramString1, paramString2)
    }

    fun getValue(paramContext: Context, paramString: String, paramBoolean: Boolean): Boolean {
        return getSharedPreferences(paramContext, FILENAME).getBoolean(paramString, paramBoolean)
    }

    fun putValue(paramContext: Context, paramString: String, paramFloat: Float) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.putFloat(paramString, paramFloat)
        localEditor.commit()
    }

    fun putValue(paramContext: Context, paramString: String, paramInt: Int) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.putInt(paramString, paramInt)
        localEditor.commit()
    }

    fun putValue(paramContext: Context, paramString: String, paramLong: Long) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.putLong(paramString, paramLong)
        localEditor.commit()
    }

    fun putValue(paramContext: Context, paramString1: String, paramString2: String) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.putString(paramString1, paramString2)
        localEditor.commit()
    }

    fun putValue(paramContext: Context, paramString: String, paramBoolean: Boolean) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.putBoolean(paramString, paramBoolean)
        localEditor.commit()
    }

    fun removeValue(paramContext: Context, paramString1: String) {
        val localEditor = getEditor(paramContext, FILENAME)
        localEditor.remove(paramString1)
        localEditor.commit()
    }
}
