package com.example

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.ui.backupFileName
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CharacterBackupTest {

    private fun sampleCharacter(name: String = "Tordek"): CharacterEntity {
        val skills = CharacterJsonAdapters.serializeSkills(
            listOf(
                CharacterSkill("Escalar", "FOR", ranks = 3.5f, miscBonus = 2, isClassSkill = true),
                CharacterSkill("Ouvir", "SAB", ranks = 1f)
            )
        )
        return CharacterEntity(
            name = name,
            characterClass = "Guerreiro 3 / Ladino 2",
            race = "Anão",
            level = 5,
            alignment = "Leal e Bom",
            deity = "Moradin",
            size = "Médio",
            currentHp = 41,
            maxHp = 52,
            tempHp = 3,
            nonLethalDamage = 2,
            baseStr = 17, baseDex = 12, baseCon = 16, baseInt = 10, baseWis = 11, baseCha = 8,
            modStrBonus = 2, modDexBonus = 0, modConBonus = 1, modIntBonus = 0, modWisBonus = 0, modChaBonus = 0,
            armorBonus = 8, shieldBonus = 2, naturalArmor = 1, deflectionBonus = 1, miscAcBonus = 1,
            miscFortitude = 1, miscReflex = 2, miscWill = 3, miscInitiative = 4,
            baseSpeedMeters = 6.0f,
            goldGp = 123.45f, silverSp = 7f, copperCp = 0.5f, platinumPp = 2f,
            skillsJson = skills,
            languagesJson = "[\"Comum\",\"Anão\"]",
            skillPointHistoryJson = "[{\"characterLevel\":1,\"className\":\"Guerreiro\"}]",
            languageAcquisitionsJson = "[]",
            inventoryJson = "[{\"name\":\"Machado de Guerra\",\"quantity\":1}]",
            featsJson = "[]",
            spellsJson = "[]",
            spellSlotsJson = "[{\"level\":0,\"totalSlots\":3}]",
            multiclassJson = "[{\"className\":\"Guerreiro\",\"level\":3},{\"className\":\"Ladino\",\"level\":2}]",
            notes = "Linha 1\nLinha 2 com \"aspas\" e acentuação: ção, ñ, 日本",
            updatedAt = 1_700_000_000_000L
        )
    }

    private fun success(json: String): CharacterBackup.ParseResult.Success {
        val result = CharacterBackup.parse(json)
        assertTrue("esperava sucesso, veio $result", result is CharacterBackup.ParseResult.Success)
        return result as CharacterBackup.ParseResult.Success
    }

    private fun failure(json: String): String {
        val result = CharacterBackup.parse(json)
        assertTrue("esperava falha, veio $result", result is CharacterBackup.ParseResult.Failure)
        return (result as CharacterBackup.ParseResult.Failure).message
    }

    private fun envelope(characters: String, version: Int = 1, app: String = CharacterBackup.APP_ID) =
        "{\"app\":\"$app\",\"formatVersion\":$version,\"characters\":$characters}"

    @Test
    fun roundTripPreservesEveryField() {
        val original = sampleCharacter()
        val json = CharacterBackup.toJson(listOf(original))
        val parsed = success(json)
        assertEquals(1, parsed.characters.size)
        assertTrue(parsed.warnings.isEmpty())
        assertEquals(original, parsed.characters[0].entity)
    }

    @Test
    fun roundTripKeepsFloatsWithoutBinaryNoise() {
        val original = sampleCharacter().copy(goldGp = 1.1f, baseSpeedMeters = 4.5f)
        val json = CharacterBackup.toJson(listOf(original))
        assertTrue(json.contains("1.1"))
        assertTrue(!json.contains("1.100000"))
        val entity = success(json).characters[0].entity
        assertEquals(1.1f, entity.goldGp, 0f)
        assertEquals(4.5f, entity.baseSpeedMeters, 0f)
    }

    @Test
    fun roundTripKeepsSeveralCharactersInOrder() {
        val list = listOf(sampleCharacter("A"), sampleCharacter("B"), sampleCharacter("C"))
        val parsed = success(CharacterBackup.toJson(list))
        assertEquals(listOf("A", "B", "C"), parsed.characters.map { it.entity.name })
    }

    @Test
    fun importedCharactersAlwaysGetNewIds() {
        val withId = sampleCharacter().copy(id = 42)
        val entity = success(CharacterBackup.toJson(listOf(withId))).characters[0].entity
        assertEquals(0L, entity.id)
    }

    @Test
    fun presetPortraitIsKeptAsReference() {
        val original = sampleCharacter().copy(photoUri = "preset:hero_portrait_mage_1792461617980")
        val imported = success(CharacterBackup.toJson(listOf(original))).characters[0]
        assertEquals("preset:hero_portrait_mage_1792461617980", imported.entity.photoUri)
        assertNull(imported.photoBytes)
    }

    @Test
    fun customPortraitIsEmbeddedAndRecovered() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5, -1, -2, 127, 0)
        val original = sampleCharacter().copy(photoUri = "file:///data/portrait.jpg")
        val json = CharacterBackup.toJson(listOf(original)) { uri ->
            if (uri == "file:///data/portrait.jpg") bytes else null
        }
        val imported = success(json).characters[0]
        assertArrayEquals(bytes, imported.photoBytes)
        // O caminho antigo não vale no outro aparelho; quem importa grava o arquivo e define o novo caminho.
        assertNull(imported.entity.photoUri)
    }

    @Test
    fun unreadablePortraitIsLeftOutWithoutFailingTheExport() {
        val original = sampleCharacter().copy(photoUri = "content://outro/app/foto")
        val imported = success(CharacterBackup.toJson(listOf(original)) { null }).characters[0]
        assertNull(imported.photoBytes)
        assertNull(imported.entity.photoUri)
    }

    @Test
    fun rejectsInvalidJson() {
        assertTrue(failure("isto não é json").contains("inválido"))
        assertTrue(failure("").isNotEmpty())
        assertTrue(failure("[1,2,3]").isNotEmpty())
    }

    @Test
    fun rejectsFilesFromOtherApps() {
        failure(envelope("[{\"name\":\"X\"}]", app = "outro-app"))
        failure("{\"formatVersion\":1,\"characters\":[{\"name\":\"X\"}]}")
    }

    @Test
    fun rejectsNewerAndInvalidFormatVersions() {
        val newer = failure(envelope("[{\"name\":\"X\"}]", version = CharacterBackup.FORMAT_VERSION + 1))
        assertTrue(newer.contains("mais nova"))
        failure(envelope("[{\"name\":\"X\"}]", version = 0))
        failure(envelope("[{\"name\":\"X\"}]", version = -3))
    }

    @Test
    fun rejectsMissingOrEmptyCharacterList() {
        failure("{\"app\":\"${CharacterBackup.APP_ID}\",\"formatVersion\":1}")
        failure(envelope("[]"))
        failure(envelope("[1,\"x\",null]"))
    }

    @Test
    fun rejectsTooManyCharacters() {
        val items = (0..CharacterBackup.MAX_CHARACTERS).joinToString(",", "[", "]") { "{\"name\":\"P$it\"}" }
        assertTrue(failure(envelope(items)).contains("demais"))
    }

    @Test
    fun missingFieldsFallBackToDefaultsAndUnknownFieldsAreIgnored() {
        val parsed = success(envelope("[{\"name\":\"Mínima\",\"campoDoFuturo\":{\"a\":1}}]"))
        val e = parsed.characters[0].entity
        val d = CharacterEntity()
        assertEquals("Mínima", e.name)
        assertEquals(d.characterClass, e.characterClass)
        assertEquals(d.baseStr, e.baseStr)
        assertEquals(d.goldGp, e.goldGp, 0f)
        assertEquals(e.maxHp, e.currentHp)
        assertEquals("", e.skillsJson)
        assertTrue(parsed.warnings.isEmpty())
    }

    @Test
    fun jsonNullValuesBecomeDefaultsNotTheTextNull() {
        val parsed = success(envelope("[{\"name\":\"Nula\",\"race\":null,\"notes\":null,\"deity\":null}]"))
        val e = parsed.characters[0].entity
        assertEquals(CharacterEntity().race, e.race)
        assertEquals(CharacterEntity().deity, e.deity)
        assertEquals("", e.notes)
    }

    @Test
    fun blankNameGetsPlaceholderAndWarning() {
        val parsed = success(envelope("[{\"name\":\"   \"}]"))
        assertEquals("Personagem importado", parsed.characters[0].entity.name)
        assertEquals(1, parsed.warnings.size)
    }

    @Test
    fun outOfRangeValuesAreClamped() {
        val parsed = success(
            envelope(
                "[{\"name\":\"Extremo\",\"level\":500,\"maxHp\":-5,\"currentHp\":999,\"tempHp\":-2," +
                    "\"baseStr\":0,\"baseDex\":1000,\"modConBonus\":9999,\"goldGp\":-10,\"silverSp\":\"abc\"," +
                    "\"baseSpeedMeters\":-3}]"
            )
        )
        val e = parsed.characters[0].entity
        assertEquals(100, e.level)
        assertEquals(1, e.maxHp)
        assertEquals(1, e.currentHp)
        assertEquals(0, e.tempHp)
        assertEquals(1, e.baseStr)
        assertEquals(99, e.baseDex)
        assertEquals(100, e.modConBonus)
        assertEquals(0f, e.goldGp, 0f)
        assertEquals(CharacterEntity().silverSp, e.silverSp, 0f)
        assertEquals(0f, e.baseSpeedMeters, 0f)
    }

    @Test
    fun currentHpIsNeverAboveMaxHp() {
        val e = success(envelope("[{\"name\":\"X\",\"maxHp\":20,\"currentHp\":35}]")).characters[0].entity
        assertEquals(20, e.currentHp)
    }

    @Test
    fun invalidJsonColumnsAreDroppedWithWarningAndGoodOnesKept() {
        val bad = JSONObject().put("name", "Parcial")
            .put("skillsJson", "{isto quebrou")
            .put("inventoryJson", "[{\"name\":\"Corda\"}]")
        val parsed = success(envelope(JSONArray().put(bad).toString()))
        val e = parsed.characters[0].entity
        assertEquals("", e.skillsJson)
        assertEquals("[{\"name\":\"Corda\"}]", e.inventoryJson)
        assertEquals(1, parsed.warnings.size)
        assertTrue(parsed.warnings[0].contains("skillsJson"))
    }

    @Test
    fun nonObjectItemsAreSkippedWithWarning() {
        val parsed = success(envelope("[7,{\"name\":\"Boa\"},\"texto\"]"))
        assertEquals(listOf("Boa"), parsed.characters.map { it.entity.name })
        assertEquals(2, parsed.warnings.size)
    }

    @Test
    fun corruptedPortraitIsIgnoredWithWarning() {
        val parsed = success(envelope("[{\"name\":\"Foto\",\"photoJpegBase64\":\"@@@não é base64@@@\"}]"))
        assertNull(parsed.characters[0].photoBytes)
        assertTrue(parsed.warnings.any { it.contains("retrato") })
    }

    @Test
    fun updatedAtIsKeptWhenValidAndReplacedWhenMissing() {
        val kept = success(envelope("[{\"name\":\"A\",\"updatedAt\":1700000000000}]")).characters[0].entity
        assertEquals(1_700_000_000_000L, kept.updatedAt)
        val before = System.currentTimeMillis()
        val missing = success(envelope("[{\"name\":\"B\"}]")).characters[0].entity
        assertTrue(missing.updatedAt >= before)
    }

    @Test
    fun exportedFileDeclaresAppAndVersion() {
        val root = JSONObject(CharacterBackup.toJson(listOf(sampleCharacter()), exportedAt = 123L))
        assertEquals(CharacterBackup.APP_ID, root.getString("app"))
        assertEquals(CharacterBackup.FORMAT_VERSION, root.getInt("formatVersion"))
        assertEquals(123L, root.getLong("exportedAt"))
        assertEquals(1, root.getJSONArray("characters").length())
    }

    @Test
    fun backupFileNameIsSafeForAnyCharacterName() {
        val single = backupFileName("Tordek, o Anão!")
        assertTrue(single, single.startsWith("ficha-Tordek_o_Anao-"))
        assertTrue(single.endsWith(".json"))
        assertTrue(backupFileName("日本語 ???").startsWith("ficha-ficha-"))
        assertTrue(backupFileName("../../etc/passwd").none { it == '/' })
        assertTrue(backupFileName(null).startsWith("fichas-dnd35-"))
        assertTrue(backupFileName("   ").startsWith("fichas-dnd35-"))
        assertTrue(backupFileName("A".repeat(300)).length < 80)
    }
}
