package dev.cr1stalik.phoenixrp.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
@Database(entities = [RpCharacterEntity::class, RpMessageEntity::class, RpStoryEntity::class, RpMemoryEntity::class, LocalModelEntity::class], version = 2, exportSchema = false)
abstract class PhoenixDatabase : RoomDatabase() {
 abstract fun phoenixDao(): PhoenixDao
 companion object {
  private val MIGRATION_1_2 = object : Migration(1, 2) {
   override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE characters ADD COLUMN storyId TEXT NOT NULL DEFAULT 'default'")
    db.execSQL("ALTER TABLE characters ADD COLUMN appearance TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE characters ADD COLUMN background TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE characters ADD COLUMN speechStyle TEXT NOT NULL DEFAULT ''")
    db.execSQL("CREATE TABLE IF NOT EXISTS stories (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, createdAt INTEGER NOT NULL)")
    db.execSQL("INSERT OR IGNORE INTO stories (id, title, description, createdAt) VALUES ('default', 'Моя первая история', '', ${System.currentTimeMillis()})")
    db.execSQL("CREATE TABLE IF NOT EXISTS memories (id TEXT NOT NULL PRIMARY KEY, storyId TEXT NOT NULL, characterId TEXT, content TEXT NOT NULL, source TEXT NOT NULL, createdAt INTEGER NOT NULL)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_memories_storyId ON memories(storyId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_memories_characterId ON memories(characterId)")
    db.execSQL("CREATE TABLE IF NOT EXISTS local_models (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, filePath TEXT NOT NULL, sizeBytes INTEGER NOT NULL, format TEXT NOT NULL, addedAt INTEGER NOT NULL)")
   }
  }
  @Volatile private var instance: PhoenixDatabase? = null
  fun getInstance(context: Context): PhoenixDatabase = instance ?: synchronized(this) {
   instance ?: Room.databaseBuilder(context.applicationContext, PhoenixDatabase::class.java, "phoenix-rp.db").addMigrations(MIGRATION_1_2).build().also { instance = it }
  }
 }
}
