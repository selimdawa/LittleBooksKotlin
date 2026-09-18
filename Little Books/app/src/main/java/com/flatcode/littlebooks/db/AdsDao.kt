package com.flatcode.littlebooks.db

import androidx.room.*
import com.flatcode.littlebooks.model.ADs
import kotlinx.coroutines.flow.Flow

@Dao
interface AdsDao {
    @Query("SELECT * FROM ads")
    fun getAllAds(): Flow<List<ADs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: ADs)
}


