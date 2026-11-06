package com.example.dnd35.session

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Exact currency arithmetic for multiplayer purchases.
 *
 * The Player's Handbook exchange rates are represented in copper pieces:
 * 1 pp = 10 pc, 1 po = 100 pc, and 1 pl = 1,000 pc.
 * Values are validated before a transaction; invalid or sub-copper prices are rejected.
 */
data class SessionCoinPurse(
    val goldGp: Float,
    val silverSp: Float,
    val copperCp: Float,
    val platinumPp: Float
) {
    fun toCharacterUpdate(character: CharacterEntity): CharacterEntity = character.copy(
        goldGp = goldGp,
        silverSp = silverSp,
        copperCp = copperCp,
        platinumPp = platinumPp
    )
}

object SessionCurrency {
    private val TEN = BigDecimal.TEN
    private val HUNDRED = BigDecimal("100")
    private val THOUSAND = BigDecimal("1000")

    /** Returns null for negative, non-finite, sub-copper or overflowing balances. */
    fun totalCopper(character: CharacterEntity): Long? {
        val values = listOf(character.goldGp, character.silverSp, character.copperCp, character.platinumPp)
        if (values.any { !it.isFinite() || it < 0f }) return null
        return try {
            val gp = BigDecimal(character.goldGp.toString())
            val sp = BigDecimal(character.silverSp.toString())
            val cp = BigDecimal(character.copperCp.toString())
            val pp = BigDecimal(character.platinumPp.toString())
            // Fractional denomination values are accepted only when their combined value
            // is an exact whole copper piece; sub-copper balances cannot be spent exactly.
            gp.multiply(HUNDRED)
                .add(sp.multiply(TEN))
                .add(cp)
                .add(pp.multiply(THOUSAND))
                .longValueExact()
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    /** Converts a price expressed in gold pieces to whole copper pieces without rounding. */
    fun priceToCopper(costGp: Float): Long? {
        if (!costGp.isFinite() || costGp < 0f) return null
        return try {
            BigDecimal(costGp.toString())
                .multiply(HUNDRED)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact()
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    /**
     * Debits a price and returns a canonical purse (platinum, gold, silver, copper).
     * Null means invalid data or insufficient funds. No input object is mutated.
     */
    fun debit(character: CharacterEntity, costGp: Float): SessionCoinPurse? {
        val available = totalCopper(character) ?: return null
        val price = priceToCopper(costGp) ?: return null
        if (price > available) return null

        var remaining = available - price
        val pp = remaining / 1000L
        remaining %= 1000L
        val gp = remaining / 100L
        remaining %= 100L
        val sp = remaining / 10L
        val cp = remaining % 10L

        val purse = SessionCoinPurse(
            goldGp = gp.toFloat(),
            silverSp = sp.toFloat(),
            copperCp = cp.toFloat(),
            platinumPp = pp.toFloat()
        )
        // Float-backed sheet fields cannot represent every very large integer exactly.
        // Reject such a debit rather than silently losing a copper piece during conversion.
        return purse.takeIf { totalCopper(it.toCharacterUpdate(character)) == available - price }
    }
}

/** One approved resource delivery, targeted at the local character selected for this session. */
data class SessionItemGrant(
    val grantId: String,
    val characterId: Long,
    val item: CharacterInventoryItem,
    val coinPurse: SessionCoinPurse? = null
)
