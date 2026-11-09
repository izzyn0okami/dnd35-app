package com.example.dnd35.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Símbolos específicos para efeitos reconhecíveis de magias do Livro do Jogador.
 * A chave é apenas visual; nenhuma informação é gravada na ficha ou no catálogo.
 */
private val SPELL_ICON_OVERRIDES = mapOf(
    // Fogo, calor e luz intensa
    "mãos flamejantes" to "fire-bottle",
    "bola de fogo" to "fire-bottle",
    "bola de fogo controlável" to "fire-bottle",
    "muralha de fogo" to "fire-bottle",
    "coluna de chamas" to "fire-bottle",
    "tempestade de fogo" to "fire-bottle",
    "sementes de fogo" to "fire-bottle",
    "produzir chama" to "fire-bottle",
    "criar chamas" to "fire-bottle",
    "esquentar metal" to "fire-bottle",
    "explosão solar" to "glowing-artifact",
    "luz" to "candle-light",
    "luz do dia" to "primitive-torch",
    "brilho" to "glowing-artifact",
    "globos de luz" to "candle-light",
    "luz cegante" to "glowing-artifact",
    "fogo das fadas" to "primitive-torch",

    // Ácido, eletricidade e trovões
    "raio de ácido" to "acid",
    "névoa ácida" to "acid",
    "relâmpago" to "thunderball",
    "corrente de relâmpagos" to "thunderball",
    "toque chocante" to "thunderball",

    // Cura, vitalidade e morte
    "curar ferimentos mínimos" to "health-potion",
    "curar ferimentos leves" to "health-potion",
    "curar ferimentos moderados" to "health-potion",
    "curar ferimentos graves" to "health-potion",
    "curar ferimentos críticos" to "health-potion",
    "curar ferimentos leves em massa" to "health-potion",
    "curar ferimentos moderados em massa" to "health-potion",
    "curar ferimentos graves em massa" to "health-potion",
    "curar ferimentos críticos em massa" to "health-potion",
    "cura completa" to "health-potion",
    "cura completa (heal)" to "health-potion",
    "cura completa em massa" to "health-potion",
    "bom fruto" to "health-potion",
    "restauração" to "health-potion",
    "restauração menor" to "health-potion",
    "restauração maior" to "health-potion",
    "regeneração" to "health-potion",
    "ressurreição" to "holy-grail",
    "ressurreição verdadeira" to "holy-grail",
    "milagre" to "holy-grail",
    "infligir ferimentos mínimos" to "skull-crossed-bones",
    "infligir ferimentos leves" to "skull-crossed-bones",
    "infligir ferimentos graves" to "skull-crossed-bones",
    "infligir ferimentos críticos" to "skull-crossed-bones",
    "infligir ferimentos leves em massa" to "skull-crossed-bones",
    "infligir ferimentos graves em massa" to "skull-crossed-bones",
    "infligir ferimentos críticos em massa" to "skull-crossed-bones",
    "romper morto-vivo" to "skull-crossed-bones",
    "dedo da morte" to "skull-crossed-bones",
    "símbolo da morte" to "skull-crossed-bones",
    "criar mortos-vivos" to "skull-crossed-bones",
    "criar mortos-vivos menor" to "skull-crossed-bones",
    "criar mortos-vivos maior" to "skull-crossed-bones",
    "drenar energia" to "skull-crossed-bones",
    "drenar força vital" to "skull-crossed-bones",
    "grito da banshee" to "skull-crossed-bones",

    // Venenos, contenção e estados
    "detectar venenos" to "poison-bottle",
    "retardar envenenamento" to "poison-bottle",
    "neutralizar venenos" to "poison-bottle",
    "sono" to "person-in-bed",
    "canção de ninar" to "lyre",
    "teia" to "fishing-net",
    "constrição" to "fishing-net",
    "imobilizar pessoa" to "handcuffs",
    "imobilizar pessoa em massa" to "handcuffs",
    "imobilizar animal" to "handcuffs",
    "imobilizar monstro" to "handcuffs",
    "imobilizar monstro em massa" to "handcuffs",
    "prisão" to "crossed-chains",
    "aprisionamento" to "crossed-chains",
    "prender a alma" to "crossed-chains",
    "armadilha" to "lockpicks",

    // Proteção, deslocamento e percepção
    "armadura arcana" to "magic-shield",
    "escudo arcano" to "magic-shield",
    "escudo da fé" to "magic-shield",
    "proteção contra o mal" to "magic-shield",
    "proteção contra o bem" to "magic-shield",
    "proteção contra o caos" to "magic-shield",
    "proteção contra a lei" to "magic-shield",
    "proteção contra elementos" to "magic-shield",
    "proteção contra magias" to "magic-shield",
    "campo antimagia" to "magic-shield",
    "muralha de força" to "magic-shield",
    "voo" to "wingfoot",
    "andar no ar" to "wingfoot",
    "caminhar no vento" to "wingfoot",
    "teletransporte" to "wingfoot",
    "teletransporte maior" to "wingfoot",
    "porta dimensional" to "wingfoot",
    "círculo de teletransporte" to "wingfoot",
    "detectar magia" to "crystal-ball",
    "identificação" to "crystal-ball",
    "vidência maior" to "crystal-ball",
    "visão arcana maior" to "crystal-ball",
    "visão da verdade" to "crystal-ball",
    "sexto sentido" to "crystal-ball",
    "olhos observadores maior" to "spyglass",
    "detectar portas secretas" to "magnifying-glass",
    "detectar armadilhas" to "magnifying-glass",
    "lendas e histórias" to "open-book",
    "ler magias" to "open-book",
    "compreender idiomas" to "open-book",
    "mensagem" to "envelope",
    "alarme" to "ringing-bell",

    // Ilusão, influência e metamorfose
    "imagem silenciosa" to "mirror-mirror",
    "animação ilusória" to "mirror-mirror",
    "projetar imagem" to "mirror-mirror",
    "visão falsa" to "mirror-mirror",
    "dissimular tendência" to "domino-mask",
    "disfarçar-se" to "domino-mask",
    "enfeitiçar pessoa" to "domino-mask",
    "enfeitiçar animal" to "domino-mask",
    "enfeitiçar monstro em massa" to "domino-mask",
    "metamorfose" to "fairy-wand",
    "forma animal" to "fairy-wand",
    "alterar forma" to "fairy-wand",
    "metamorfosear objetos" to "fairy-wand",
    "transformação momentânea" to "fairy-wand",

    // Natureza, água e animais
    "criar água" to "water-flask",
    "caminhar na água" to "water-flask",
    "respirar na água" to "water-flask",
    "controlar água" to "water-flask",
    "forma de árvore" to "herbs-bundle",
    "animar plantas" to "herbs-bundle",
    "comandar plantas" to "herbs-bundle",
    "ampliar plantas" to "herbs-bundle",
    "enfraquecer plantas" to "herbs-bundle",
    "muralha de espinhos" to "herbs-bundle",
    "falar com animais" to "hound",
    "detectar animais ou plantas" to "herbs-bundle",
    "mensageiro animal" to "hound",
    "dominar animal" to "hound",
    "acalmar animais" to "hound",
    "invocar aliado da natureza i" to "herbs-bundle",
    "invocar aliado da natureza ii" to "herbs-bundle",
    "invocar aliado da natureza iii" to "herbs-bundle",
    "invocar aliado da natureza iv" to "herbs-bundle",
    "invocar aliado da natureza vii" to "herbs-bundle",
    "invocar aliado da natureza viii" to "herbs-bundle",
    "invocar aliado da natureza ix" to "herbs-bundle",

    // Magias de arma, som, tempo e invocação
    "arma espiritual" to "rune-sword",
    "espada sagrada" to "rune-sword",
    "espada de mordenkainen" to "rune-sword",
    "barreira de lâminas" to "broadsword",
    "arma mágica" to "rune-sword",
    "arma mágica maior" to "rune-sword",
    "invocar instrumento" to "lyre",
    "grito maior" to "lyre",
    "parar o tempo" to "hourglass",
    "estase temporal" to "hourglass",
    "invocar criaturas i" to "magic-lamp",
    "invocar criaturas vii" to "magic-lamp",
    "invocar criaturas viii" to "magic-lamp",
    "invocar criaturas ix" to "magic-lamp",
    "portal" to "magic-lamp"
)

/** Retorna apenas associações específicas; null significa usar o ícone da escola. */
internal fun spellIconKey(spellName: String): String? =
    SPELL_ICON_OVERRIDES[spellName.trim().lowercase(Locale.ROOT)]

/** Ícone específico da magia, com fallback visual para sua escola. */
@Composable
fun SpellIcon(spellName: String, school: String, modifier: Modifier = Modifier) {
    val icon = GameIcons.find(spellIconKey(spellName) ?: spellSchoolIcon(school))
    if (icon != null) {
        Icon(
            painter = painterResource(id = icon.resId),
            contentDescription = "Ícone da magia: $spellName",
            modifier = modifier,
            tint = Color.Unspecified
        )
    }
}
