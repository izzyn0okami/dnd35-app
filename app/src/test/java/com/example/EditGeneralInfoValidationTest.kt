package com.example

import com.example.dnd35.ui.dialogs.normalizeEditedCharacterName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditGeneralInfoValidationTest {

    @Test
    fun rejectsEmptyOrWhitespaceOnlyNames() {
        assertNull(normalizeEditedCharacterName(""))
        assertNull(normalizeEditedCharacterName("   "))
        assertNull(normalizeEditedCharacterName("\t\n"))
    }

    @Test
    fun trimsLeadingAndTrailingWhitespace() {
        assertEquals("Arannis", normalizeEditedCharacterName("  Arannis  "))
    }

    @Test
    fun preservesInternalWhitespace() {
        assertEquals("Arannis Lua", normalizeEditedCharacterName(" Arannis Lua "))
    }
}
