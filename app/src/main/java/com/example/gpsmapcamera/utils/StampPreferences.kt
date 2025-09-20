package com.example.gpsmapcamera.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.gpsmapcamera.models.NoteModel
import com.example.gpsmapcamera.models.StampConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StampPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("stamp_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val key = "stamp_list"

    fun saveList(templateType: String,list: List<StampConfig>) {
        val json = gson.toJson(list)
        prefs.edit().putString("${key}_$templateType", json).apply()
    }

    fun getList(templateType: String): List<StampConfig> {
        val json = prefs.getString("${key}_$templateType", null) ?: return emptyList()
        val type = object : TypeToken<List<StampConfig>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveWholeList(key:String, list: List<String>) {
        val json = gson.toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    fun getWholeList(key:String): List<String> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveNoteModel(key:String, list: List<NoteModel>) {
        val json = gson.toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    fun getNoteModel(key:String): List<NoteModel> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<NoteModel>>() {}.type
        return gson.fromJson(json, type)
    }

}
