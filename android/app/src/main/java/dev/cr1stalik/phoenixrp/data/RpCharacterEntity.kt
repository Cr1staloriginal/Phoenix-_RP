package dev.cr1stalik.phoenixrp.data
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "characters")
data class RpCharacterEntity(
 @PrimaryKey val id: String, val name: String, val description: String, val opening: String,
 val createdAt: Long = System.currentTimeMillis(), @ColumnInfo(defaultValue = "'default'") val storyId: String = "default",
 @ColumnInfo(defaultValue = "''") val appearance: String = "", @ColumnInfo(defaultValue = "''") val background: String = "", @ColumnInfo(defaultValue = "''") val speechStyle: String = ""
)
