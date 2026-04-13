package de.astronarren.allsky.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MediaDao {
    @Query("SELECT * FROM cached_media WHERE type = :type")
    suspend fun getMediaByType(type: String): List<CachedMedia>

    @Query("DELETE FROM cached_media WHERE type = :type")
    suspend fun deleteByType(type: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<CachedMedia>)
}