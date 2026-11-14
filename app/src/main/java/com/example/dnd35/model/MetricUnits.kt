package com.example.dnd35.model

/** Conversões de unidades para a interface métrica. Os dados legados continuam armazenados em lb. */
object MetricUnits {
    const val KILOGRAMS_PER_POUND = 0.45359237f
    const val METERS_PER_FOOT = 0.3048f
    const val CENTIMETERS_PER_INCH = 2.54f

    fun poundsToKilograms(pounds: Float): Float = pounds * KILOGRAMS_PER_POUND
    fun kilogramsToPounds(kilograms: Float): Float = kilograms / KILOGRAMS_PER_POUND
    /** O Livro do Jogador brasileiro estabelece 100 moedas por quilograma. */
    fun coinsToPounds(coins: Float): Float = kilogramsToPounds(coins / 100f)
    fun feetToMeters(feet: Float): Float = feet * METERS_PER_FOOT
    fun inchesToCentimeters(inches: Float): Float = inches * CENTIMETERS_PER_INCH

    fun formatKilograms(pounds: Float, decimals: Int = 2): String =
        "%.${decimals}f".format(poundsToKilograms(pounds))
}
