package dev.cr1stalik.phoenixrp.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
@Dao
interface PhoenixDao {
 @Query("SELECT * FROM stories ORDER BY createdAt DESC") fun observeStories(): Flow<List<RpStoryEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertStory(story: RpStoryEntity)
 @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun ensureStory(story: RpStoryEntity)
 @Query("DELETE FROM stories WHERE id = :id") suspend fun deleteStory(id: String)
 @Query("DELETE FROM messages WHERE characterId IN (SELECT id FROM characters WHERE storyId = :storyId)") suspend fun deleteMessagesForStory(storyId: String)
 @Query("DELETE FROM memories WHERE storyId = :storyId") suspend fun deleteMemoriesForStory(storyId: String)
 @Query("DELETE FROM characters WHERE storyId = :storyId") suspend fun deleteCharactersForStory(storyId: String)
 @Transaction suspend fun deleteStoryAndContent(storyId: String) {
  deleteMessagesForStory(storyId)
  deleteMemoriesForStory(storyId)
  deleteCharactersForStory(storyId)
  deleteStory(storyId)
 }
 @Query("SELECT * FROM characters WHERE storyId = :storyId ORDER BY createdAt DESC") fun observeCharacters(storyId: String): Flow<List<RpCharacterEntity>>
 @Query("SELECT * FROM characters ORDER BY createdAt DESC") fun observeAllCharacters(): Flow<List<RpCharacterEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCharacter(character: RpCharacterEntity)
 @Query("DELETE FROM characters WHERE id = :id") suspend fun deleteCharacter(id: String)
 @Query("DELETE FROM memories WHERE characterId = :characterId") suspend fun deleteMemoriesForCharacter(characterId: String)
 @Query("SELECT * FROM messages WHERE characterId = :characterId ORDER BY createdAt ASC") fun observeMessages(characterId: String): Flow<List<RpMessageEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMessage(message: RpMessageEntity)
 @Query("DELETE FROM messages WHERE characterId = :characterId") suspend fun deleteMessages(characterId: String)
 @Query("SELECT * FROM memories WHERE storyId = :storyId ORDER BY createdAt DESC") fun observeMemories(storyId: String): Flow<List<RpMemoryEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMemory(memory: RpMemoryEntity)
 @Query("DELETE FROM memories WHERE id = :id") suspend fun deleteMemory(id: String)
 @Query("SELECT * FROM local_models ORDER BY addedAt DESC") fun observeModels(): Flow<List<LocalModelEntity>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertModel(model: LocalModelEntity)
 @Query("DELETE FROM local_models WHERE id = :id") suspend fun deleteModel(id: String)
}
