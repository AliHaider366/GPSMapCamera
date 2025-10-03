package com.example.gpsmapcamera.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Selected.Language"
    private var sLocale: Locale? = null

    fun onAttach(context: Context): Context {
        val lang =getPersistedData(
            context,
            Locale.getDefault().language
        )
        return setLocale(context, lang)
    }

    fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(
            context,
            defaultLanguage
        )
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(
            context,
            Locale.getDefault().language
        )
    }

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(preferences.edit()) {
            putString(SELECTED_LANGUAGE, language)
            apply()
        }
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }


}