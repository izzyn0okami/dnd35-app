package com.example.dnd35.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterFeat
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellSlotInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CharacterEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE characters ADD COLUMN multiclassJson TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dnd35_database"
                )
                .addMigrations(MIGRATION_1_2)
                // Não usar fallbackToDestructiveMigration: fichas são dados do usuário e
                // não devem ser apagadas silenciosamente quando surgir uma versão nova.
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).characterDao()
                    // Criar primeiro personagem pronto e épico: Sir Valerius
                    val defaultSkills = Dnd35Rules.SKILL_DEFINITIONS.map { def ->
                        val isClass = listOf("Escalar", "Cavalgar", "Intimidação", "Salto", "Natação").contains(def.name)
                        val ranks = when (def.name) {
                            "Escalar" -> 4.0f
                            "Salto" -> 4.0f
                            "Intimidação" -> 2.0f
                            "Cavalgar" -> 2.0f
                            else -> 0.0f
                        }
                        CharacterSkill(
                            name = def.name,
                            keyAbility = def.keyAbility,
                            ranks = ranks,
                            miscBonus = 0,
                            isClassSkill = isClass
                        )
                    }

                    val defaultInventory = listOf(
                        CharacterInventoryItem(
                            name = "Espada Longa",
                            category = "Armas",
                            quantity = 1,
                            weightLb = 4.0f,
                            costGp = 15.0f,
                            damageOrAc = "1d8 (19-20/x2)",
                            equipped = true,
                            equippedSlot = Dnd35Rules.EquipmentSlots.MAIN_HAND,
                            notes = "Dano de Corte, uma mão"
                        ),
                        CharacterInventoryItem(
                            name = "Armadura Completa (Full Plate)",
                            category = "Armaduras",
                            quantity = 1,
                            weightLb = 50.0f,
                            costGp = 1500.0f,
                            damageOrAc = "+8 CA",
                            equipped = true,
                            equippedSlot = Dnd35Rules.EquipmentSlots.ARMOR_BODY,
                            notes = "Des Max +1, Pen -6, Falha 35%"
                        ),
                        CharacterInventoryItem(
                            name = "Escudo Pesado de Aço",
                            category = "Escudos",
                            quantity = 1,
                            weightLb = 15.0f,
                            costGp = 20.0f,
                            damageOrAc = "+2 CA",
                            equipped = true,
                            equippedSlot = Dnd35Rules.EquipmentSlots.OFF_HAND,
                            notes = "Pen -2, Falha 15%"
                        ),
                        CharacterInventoryItem(
                            name = "Arco Longo Composto",
                            category = "Armas",
                            quantity = 1,
                            weightLb = 3.0f,
                            costGp = 100.0f,
                            damageOrAc = "1d8 (x3)",
                            equipped = false,
                            equippedSlot = Dnd35Rules.EquipmentSlots.NONE,
                            notes = "Perfuração, alcance 33m"
                        ),
                        CharacterInventoryItem(
                            name = "Mochila de Couro",
                            category = "Equipamento",
                            quantity = 1,
                            weightLb = 2.0f,
                            costGp = 2.0f,
                            damageOrAc = "-",
                            equipped = false,
                            notes = "Contém suprimentos"
                        ),
                        CharacterInventoryItem(
                            name = "Corda de Cânhamo (15m)",
                            category = "Equipamento",
                            quantity = 1,
                            weightLb = 10.0f,
                            costGp = 1.0f,
                            damageOrAc = "-",
                            equipped = false,
                            notes = "Resistente"
                        ),
                        CharacterInventoryItem(
                            name = "Poção de Curar Ferimentos Leves",
                            category = "Equipamento",
                            quantity = 2,
                            weightLb = 1.0f,
                            costGp = 100.0f,
                            damageOrAc = "Cura 1d8+1",
                            equipped = false,
                            notes = "Poção mágica"
                        )
                    )

                    val defaultFeats = listOf(
                        CharacterFeat(
                            name = "Ataque Poderoso",
                            prerequisites = "FOR 13",
                            summary = "Troca ataque por dano",
                            description = "Subtrai do ataque para adicionar ao dano corpo a corpo."
                        ),
                        CharacterFeat(
                            name = "Trespassar",
                            prerequisites = "FOR 13, Ataque Poderoso",
                            summary = "Ataque extra ao derrubar oponente",
                            description = "Ganha um ataque gratuito adicional em criatura adjacente ao derrubar um inimigo."
                        ),
                        CharacterFeat(
                            name = "Foco em Arma (Espada Longa)",
                            prerequisites = "BBA +1, Proficiência",
                            summary = "+1 nas jogadas de ataque",
                            description = "+1 em todas as jogadas de ataque com espada longa."
                        )
                    )

                    val defaultSpells = listOf(
                        CharacterSpell(
                            name = "Bênção",
                            level = 1,
                            school = "Encantamento",
                            castTime = "1 ação padrão",
                            range = "15m",
                            duration = "1 min/nível",
                            savingThrow = "Nenhum",
                            preparedCount = 1,
                            description = "+1 nas jogadas de ataque e resistências contra medo."
                        )
                    )

                    val defaultSpellSlots = (0..9).map { lvl ->
                        SpellSlotInfo(level = lvl, totalSlots = if (lvl == 1) 2 else 0, usedSlots = 0)
                    }

                    val firstHero = CharacterEntity(
                        name = "Sir Valerius",
                        characterClass = "Guerreiro",
                        race = "Humano",
                        level = 2,
                        alignment = "Leal e Bom",
                        deity = "Heironeous",
                        size = "Médio",
                        photoUri = "preset:hero_portrait_default_1792461602152",
                        currentHp = 22,
                        maxHp = 22,
                        tempHp = 0,
                        nonLethalDamage = 0,
                        baseStr = 16,
                        baseDex = 13,
                        baseCon = 14,
                        baseInt = 10,
                        baseWis = 12,
                        baseCha = 10,
                        armorBonus = 0,
                        shieldBonus = 0,
                        naturalArmor = 0,
                        deflectionBonus = 0,
                        miscAcBonus = 0,
                        miscFortitude = 0,
                        miscReflex = 0,
                        miscWill = 0,
                        miscInitiative = 0,
                        baseSpeedMeters = 9.0f,
                        goldGp = 75.0f,
                        silverSp = 12.0f,
                        copperCp = 30.0f,
                        platinumPp = 2.0f,
                        skillsJson = CharacterJsonAdapters.serializeSkills(defaultSkills),
                        inventoryJson = CharacterJsonAdapters.serializeInventory(defaultInventory),
                        featsJson = CharacterJsonAdapters.serializeFeats(defaultFeats),
                        spellsJson = CharacterJsonAdapters.serializeSpells(defaultSpells),
                        spellSlotsJson = CharacterJsonAdapters.serializeSpellSlots(defaultSpellSlots),
                        multiclassJson = CharacterJsonAdapters.serializeClassEntries(listOf(CharacterClassEntry("Guerreiro", 2))),
                        notes = "Um valoroso guerreiro da ordem da Espada Radiante, jurado a defender os inocentes e expurgar a escuridão.",
                        updatedAt = System.currentTimeMillis()
                    )

                    dao.insertCharacter(firstHero)
                }
            }
        }
    }
}
