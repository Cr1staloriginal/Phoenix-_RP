package dev.cr1stalik.phoenixrp.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "local_models")
data class LocalModelEntity(@PrimaryKey val id: String, val name: String, val filePath: String, val sizeBytes: Long, val format: String = "GGUF", val addedAt: Long = System.currentTimeMillis())
