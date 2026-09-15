package com.flatcode.littlebooks.data.local.dao

import androidx.room.*
import com.flatcode.littlebooks.Model.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}