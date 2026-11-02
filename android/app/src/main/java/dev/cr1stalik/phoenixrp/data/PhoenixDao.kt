package dev.cr1stalik.phoenixrp.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface PhoenixDao {
 @Query("SELECT * FROM characters ORDER BY createdAt DESC") fun observeCharacters(): Flow<List<RpCharacterEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCharacter(character: RpCharacterEntity)
 @Query("DELETE FROM characters WHERE id = :id") suspend fun deleteCharacter(id: String)
 @Query("SELECT * FROM messages WHERE characterId = :characterId ORDER BY createdAt ASC") fun observeMessages(characterId: String): Flow<List<RpMessageEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMessage(message: RpMessageEntity)
 @Query("DELETE FROM messages WHERE characterId = :characterId") suspend fun deleteMessages(characterId: String)
}
