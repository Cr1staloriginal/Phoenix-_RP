package dev.cr1stalik.phoenixrp.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [RpCharacterEntity::class, RpMessageEntity::class], version = 1, exportSchema = false)
abstract class PhoenixDatabase : RoomDatabase() {
 abstract fun phoenixDao(): PhoenixDao
 companion object {
  @Volatile private var instance: PhoenixDatabase? = null
  fun getInstance(context: Context): PhoenixDatabase = instance ?: synchronized(this) {
   instance ?: Room.databaseBuilder(context.applicationContext, PhoenixDatabase::class.java, "phoenix-rp.db").build().also { instance = it }
  }
 }
}
