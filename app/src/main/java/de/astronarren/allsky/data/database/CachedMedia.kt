package de.astronarren.allsky.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_media")
data class CachedMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // 'timelapses', 'keograms', 'startrails', 'images', 'meteors'
    val date: String,
    val url: String
)