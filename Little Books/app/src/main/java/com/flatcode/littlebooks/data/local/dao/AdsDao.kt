package com.flatcode.littlebooks.data.local.dao

import androidx.room.*
import com.flatcode.littlebooks.Model.ADs
import kotlinx.coroutines.flow.Flow

@Dao
interface AdsDao {
    @Query("SELECT * FROM ads")
    fun getAllAds(): Flow<List<ADs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: ADs)
}