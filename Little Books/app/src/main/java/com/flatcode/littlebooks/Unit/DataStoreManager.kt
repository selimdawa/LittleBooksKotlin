package com.flatcode.littlebooks.Unit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    suspend fun saveString(key: String, value: String) {
        context.dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    fun getString(key: String): Flow<String?> =
        context.dataStore.data.map { it[stringPreferencesKey(key)] }

    suspend fun saveInt(key: String, value: Int) {
        context.dataStore.edit { it[intPreferencesKey(key)] = value }
    }

    fun getInt(key: String): Flow<Int?> =
        context.dataStore.data.map { it[intPreferencesKey(key)] }

    suspend fun saveBoolean(key: String, value: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    fun getBoolean(key: String): Flow<Boolean?> =
        context.dataStore.data.map { it[booleanPreferencesKey(key)] }
}