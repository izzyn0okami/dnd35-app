package com.example.dnd35.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dnd35.model.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY updatedAt DESC")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    fun getCharacterById(id: Long): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters ORDER BY updatedAt DESC")
    suspend fun getAllCharactersOnce(): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    // Room executa a inserção de uma lista em uma única transação.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCharacters(characters: List<CharacterEntity>): List<Long>

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteById(id: Long)
}
