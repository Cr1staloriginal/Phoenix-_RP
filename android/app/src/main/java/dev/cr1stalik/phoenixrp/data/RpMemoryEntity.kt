package dev.cr1stalik.phoenixrp.data
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "memories", indices = [Index("storyId"), Index("characterId")])
data class RpMemoryEntity(@PrimaryKey val id: String, val storyId: String, val characterId: String?, val content: String, val source: String = "manual", val createdAt: Long = System.currentTimeMillis())
