package dev.cr1stalik.phoenixrp.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "characters")
data class RpCharacterEntity(
 @PrimaryKey val id: String, val name: String, val description: String, val opening: String,
 val createdAt: Long = System.currentTimeMillis(), val storyId: String = "default",
 val appearance: String = "", val background: String = "", val speechStyle: String = ""
)
