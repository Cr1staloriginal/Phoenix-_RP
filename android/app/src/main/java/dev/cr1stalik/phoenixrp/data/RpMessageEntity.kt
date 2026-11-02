package dev.cr1stalik.phoenixrp.data
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "messages", indices = [Index(value = ["characterId", "createdAt"])])
data class RpMessageEntity(@PrimaryKey val id: String, val characterId: String, val fromUser: Boolean, val text: String, val createdAt: Long = System.currentTimeMillis())
