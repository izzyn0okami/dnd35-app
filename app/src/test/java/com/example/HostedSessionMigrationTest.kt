package com.example

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dnd35.data.AppDatabase
import com.example.dnd35.model.CharacterEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Dao
interface LegacyV9CharacterDao {
    @Insert
    suspend fun insert(character: CharacterEntity): Long
}

@Database(entities = [CharacterEntity::class], version = 9, exportSchema = false)
abstract class LegacyV9Database : RoomDatabase() {
    abstract fun characters(): LegacyV9CharacterDao
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HostedSessionMigrationTest {

    @Test
    fun `migracao 9 para 11 preserva fichas existentes e cria tabela de sessao`() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val databaseName = "hosted-session-migration-test.db"
        context.deleteDatabase(databaseName)

        val legacy = Room.databaseBuilder(context, LegacyV9Database::class.java, databaseName).build()
        val character = CharacterEntity(
            name = "Aragorn",
            characterClass = "Patrulheiro",
            level = 7,
            currentHp = 42,
            maxHp = 48,
            goldGp = 88f,
            inventoryJson = """[{"id":"sword-1","name":"Espada longa","category":"Armas"}]"""
        )
        val insertedId = legacy.characters().insert(character)
        legacy.close()

        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11)
            .build()

        try {
            val restored = upgraded.characterDao().getAllCharactersOnce().single()
            assertEquals(insertedId, restored.id)
            assertEquals("Aragorn", restored.name)
            assertEquals(7, restored.level)
            assertEquals(42, restored.currentHp)
            assertEquals(88f, restored.goldGp)
            assertEquals(character.inventoryJson, restored.inventoryJson)
            assertEquals("", restored.creatureBondsJson)
            assertNull(upgraded.hostedSessionDao().getStoredSession())
        } finally {
            upgraded.close()
            context.deleteDatabase(databaseName)
        }
    }
}
