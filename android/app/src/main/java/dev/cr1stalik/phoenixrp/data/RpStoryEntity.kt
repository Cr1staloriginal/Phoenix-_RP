package dev.cr1stalik.phoenixrp.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "stories")
data class RpStoryEntity(@PrimaryKey val id: String, val title: String, val description: String = "", val createdAt: Long = System.currentTimeMillis())
