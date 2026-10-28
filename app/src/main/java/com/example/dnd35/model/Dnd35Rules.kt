package com.example.dnd35.model

/**
 * Regras completas e catálogo do Livro do Jogador (Player's Handbook) D&D 3.5.
 * Contém todas as raças, sub-raças, todas as 11 classes com progressões e habilidades,
 * catálogo extensivo de magias por escola/classe/nível, e catálogo de itens por categoria e slot corporal.
 */
object Dnd35Rules {

    // ==========================================
    // ESPAÇOS DE CORPO (BODY SLOTS) DO D&D 3.5
    // ==========================================
    object EquipmentSlots {
        const val NONE = "Mochila (Não Equipado)"
        const val AMMUNITION = "Munição equipada"
        const val MAIN_HAND = "Mão Principal"
        const val OFF_HAND = "Mão Secundária / Escudo"
        const val TWO_HANDS = "Duas Mãos"
        const val ARMOR_BODY = "Corpo (Armadura / Vestimenta)"
        const val HEAD = "Cabeça (Elmo / Tiara)"
        const val EYES = "Olhos (Óculos / Lentes)"
        const val NECK = "Pescoço (Amuleto / Colar)"
        const val SHOULDERS = "Ombros (Capa / Manto)"
        const val ARMS = "Braços (Braçadeiras)"
        const val HANDS = "Mãos (Luvas / Manoplas)"
        const val RING_1 = "Dedo (Anel 1)"
        const val RING_2 = "Dedo (Anel 2)"
        const val WAIST = "Cintura (Cinto / Faixa)"
        const val FEET = "Pés (Botas / Sapatos)"

        val ALL_SLOTS = listOf(
            MAIN_HAND,
            OFF_HAND,
            TWO_HANDS,
            ARMOR_BODY,
            HEAD,
            EYES,
            NECK,
            SHOULDERS,
            ARMS,
            HANDS,
            RING_1,
            RING_2,
            WAIST,
            FEET,
            NONE
        )
    }

    // ==========================================
    // TODAS AS RAÇAS E SUB-RAÇAS DE D&D 3.5
    // ==========================================
    data class RaceInfo(
        val name: String,
        val subrace: String = "Padrão",
        val strMod: Int = 0,
        val dexMod: Int = 0,
        val conMod: Int = 0,
        val intMod: Int = 0,
        val wisMod: Int = 0,
        val chaMod: Int = 0,
        val size: String = "Médio",
        val baseSpeedMeters: Float = 9.0f,
        val traits: String,
        val favoredClass: String
    )

    val RACES = listOf(
        RaceInfo(
            name = "Humano",
            traits = "+1 Talento adicional no 1º nível, 4 pontos de perícia adicionais no 1º nível e +1 por nível subsequente.",
            favoredClass = "Qualquer"
        ),
        RaceInfo(
            name = "Anão",
            subrace = "Anão da Colina",
            conMod = 2,
            chaMod = -2,
            baseSpeedMeters = 6.0f,
            traits = "Visão no escuro 18m, +2 em testes de resistência contra veneno, magias e habilidades similares a magia; afinidade com pedras (+2 em Procurar para construções incomuns de pedra), estabilidade (+4 contra encontrão e derrubar enquanto apoiado no chão), +1 nas jogadas de ataque contra orcs e goblinoides, +4 de esquiva na CA contra gigantes, familiaridade com armas anãs (machado de guerra anão e urgrosh anão tratados como armas marciais).",
            favoredClass = "Guerreiro"
        ),
        RaceInfo(
            name = "Anão da Montanha",
            subrace = "Montanha",
            conMod = 2,
            chaMod = -2,
            baseSpeedMeters = 6.0f,
            traits = "Mesmos traços raciais do Anão da Colina: visão no escuro 18m, resistência a veneno e magia, afinidade com pedras, estabilidade, bônus contra orcs/goblinoides e gigantes e familiaridade com armas anãs.",
            favoredClass = "Guerreiro"
        ),
        RaceInfo(
            name = "Elfo",
            subrace = "Alto Elfo",
            dexMod = 2,
            conMod = -2,
            traits = "Imunidade a sono mágico, +2 contra encantamentos, visão na penumbra, +2 em Ouvir, Procurar e Observar; nota portas secretas automaticamente a 1,5m; proficiência com espada longa, rapieira, arco longo e arco curto, incluindo versões compostas.",
            favoredClass = "Mago"
        ),
        RaceInfo(
            name = "Elfo da Floresta",
            subrace = "Floresta",
            strMod = 2,
            dexMod = 2,
            conMod = -2,
            intMod = -2,
            traits = "Físico atlético das florestas ancestrais, imunidade a sono mágico, +2 contra encantamentos, visão na penumbra, +2 em Ouvir, Procurar e Observar; nota portas secretas automaticamente a 1,5m; proficiência com espada longa, rapieira, arco longo, arco curto e versões compostas.",
            favoredClass = "Ranger"
        ),
        RaceInfo(
            name = "Drow (Elfo Negro)",
            subrace = "Subterrâneo",
            dexMod = 2,
            conMod = -2,
            intMod = 2,
            chaMod = 2,
            traits = "Imunidade a sono mágico, visão no escuro 36m, +2 nos testes de resistência contra magias e habilidades similares a magia, +2 em Ouvir, Procurar e Observar; nota portas secretas automaticamente a 1,5m; proficiência com besta de mão, rapieira e espada curta; resistência à magia (11 + nível de personagem), habilidades similares a magia 1/dia (Globos de Luz, Escuridão e Fogo das Fadas) e cegueira à luz intensa.",
            favoredClass = "Mago"
        ),
        RaceInfo(
            name = "Gnomo",
            subrace = "Gnomo da Rocha",
            strMod = -2,
            conMod = 2,
            size = "Pequeno",
            baseSpeedMeters = 6.0f,
            traits = "Tamanho Pequeno (+1 na CA e nas jogadas de ataque, +4 Esconder-se), visão na penumbra, +2 nos testes de resistência contra ilusões, +1 na CD das ilusões conjuradas, +1 nas jogadas de ataque contra kobolds e goblinoides, +4 de bônus de esquiva na CA contra gigantes, +2 em Ouvir e Ofícios (Alquimia), familiaridade com o martelo-gancho gnômico como arma marcial, habilidades similares a magia (Globos de Luz, Som Fantasma, Prestidigitação e Falar com Animais para mamíferos escavadores).",
            favoredClass = "Bardo"
        ),
        RaceInfo(
            name = "Halfling",
            subrace = "Pés-Leves",
            strMod = -2,
            dexMod = 2,
            size = "Pequeno",
            baseSpeedMeters = 6.0f,
            traits = "+1 CA e ataque por tamanho pequeno (+4 Esconder-se), +1 em todos os testes de resistência, +2 de moral contra medo, +1 nas jogadas de ataque com armas de arremesso e fundas, +2 em Escalar, Saltar, Ouvir e Furtividade.",
            favoredClass = "Ladino"
        ),
        RaceInfo(
            name = "Halfling Robusto",
            subrace = "Robusto",
            strMod = -2,
            dexMod = 2,
            size = "Pequeno",
            baseSpeedMeters = 6.0f,
            traits = "Variante Coração-Forte de Forgotten Realms: tamanho Pequeno (+1 na CA e nos ataques, +4 Esconder-se), +2 em Escalar, Saltar, Ouvir e Furtividade, +2 de moral contra medo, +1 nas jogadas de ataque com armas de arremesso e fundas; ganha um talento adicional no 1º nível e não recebe o +1 racial geral nos testes de resistência dos halflings Pés-Leves.",
            favoredClass = "Ladino"
        ),
        RaceInfo(
            name = "Meio-Elfo",
            traits = "Imunidade a sono mágico, +2 nos testes de resistência contra encantamentos, Visão na penumbra, +2 em Obter Informação e Diplomacia, +1 em Ouvir, Procurar e Observar, sangue élfico.",
            favoredClass = "Qualquer"
        ),
        RaceInfo(
            name = "Meio-Orc",
            strMod = 2,
            intMod = -2,
            chaMod = -2,
            traits = "Visão no escuro 18m, sangue orc (considerado orc para efeitos relacionados à raça).",
            favoredClass = "Bárbaro"
        )
    )

    // ==========================================
    // TODAS AS 11 CLASSES OFICIAIS DO D&D 3.5
    // ==========================================
    enum class BabProgression { FULL, THREE_QUARTERS, HALF }

    /** Rótulo em português para exibição da progressão de BBA na interface. */
    fun babProgressionLabel(progress: BabProgression): String = when (progress) {
        BabProgression.FULL -> "Integral (BBA igual ao nível)"
        BabProgression.THREE_QUARTERS -> "Três quartos (BBA de 3/4 do nível)"
        BabProgression.HALF -> "Metade (BBA de 1/2 do nível)"
    }
    enum class SaveProgression { GOOD, POOR }

    data class ClassFeature(
        val level: Int,
        val name: String,
        val description: String
    )

    data class ClassInfo(
        val name: String,
        val hitDie: Int, // d4, d6, d8, d10, d12
        val babType: BabProgression,
        val fortType: SaveProgression,
        val refType: SaveProgression,
        val willType: SaveProgression,
        val skillPointsPerLevel: Int, // + mod Int
        val spellCastingAbility: String? = null, // INT, SAB, CAR ou null
        val classSkills: List<String>,
        val features: List<ClassFeature>,
        val description: String
    )

    val CLASSES = listOf(
        ClassInfo(
            name = "Bárbaro",
            hitDie = 12,
            babType = BabProgression.FULL,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.POOR,
            willType = SaveProgression.POOR,
            skillPointsPerLevel = 4,
            classSkills = listOf("Adestrar Animais", "Cavalgar", "Escalar", "Intimidação", "Natação", "Ofícios", "Ouvir", "Saltar", "Sobrevivência"),
            features = listOf(
                ClassFeature(1, "Fúria 1/dia", "+4 FOR, +4 CON, +2 testes de Vontade, -2 CA por 3 + mod Con rodadas."),
                ClassFeature(1, "Analfabetismo", "O bárbaro não sabe ler nem escrever automaticamente; pode gastar 2 pontos de perícia para aprender a ler e escrever todos os idiomas que fala."),
                ClassFeature(1, "Movimento Rápido", "+3 metros no deslocamento terrestre enquanto não usa armadura média ou pesada nem carrega carga média ou pesada."),
                ClassFeature(2, "Esquiva Sobrenatural", "Conserva o bônus de Destreza na CA mesmo quando surpreendido ou atacado por um oponente invisível; ainda perde esse bônus se estiver imobilizado."),
                ClassFeature(3, "Sentir Armadilhas +1", "+1 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(4, "Fúria 2/dia", "Pode entrar em fúria 2 vezes ao dia."),
                ClassFeature(5, "Esquiva Sobrenatural Aprimorada", "Não pode ser flanqueado exceto por ladino 4 níveis acima."),
                ClassFeature(6, "Sentir Armadilhas +2", "+2 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(7, "Redução de Dano 1/-", "Reduz em 1 o dano de cada ataque sofrido."),
                ClassFeature(8, "Fúria 3/dia", "Pode entrar em fúria 3 vezes ao dia."),
                ClassFeature(9, "Sentir Armadilhas +3", "+3 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(10, "Redução de Dano 2/-", "Reduz em 2 o dano de cada ataque sofrido."),
                ClassFeature(11, "Fúria Maior", "Bônus de fúria sobem para +6 FOR, +6 CON, +3 Vontade."),
                ClassFeature(12, "Sentir Armadilhas +4", "+4 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(12, "Fúria 4/dia", "Pode entrar em fúria 4 vezes ao dia."),
                ClassFeature(13, "Redução de Dano 3/-", "Reduz em 3 o dano de cada ataque sofrido."),
                ClassFeature(14, "Vontade Indomável", "+4 em testes de Vontade contra encantamentos enquanto em fúria."),
                ClassFeature(15, "Sentir Armadilhas +5", "+5 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(16, "Fúria 5/dia e Redução de Dano 4/-", "Pode entrar em fúria 5 vezes ao dia e reduz em 4 o dano de cada ataque sofrido."),
                ClassFeature(17, "Fúria Incansável", "Não fica fatigado após terminar a fúria."),
                ClassFeature(18, "Sentir Armadilhas +6", "+6 nos testes de Reflexos e CA contra armadilhas."),
                ClassFeature(19, "Redução de Dano 5/-", "Reduz em 5 o dano de cada ataque sofrido."),
                ClassFeature(20, "Fúria Poderosa e Fúria 6/dia", "Bônus de fúria sobem para +8 FOR, +8 CON, +4 Vontade e pode entrar em fúria 6 vezes ao dia.")
            ),
            description = "Guerreiro feroz com fúria de batalha primata, dado de vida d12 e grande resistência."
        ),
        ClassInfo(
            name = "Bardo",
            hitDie = 6,
            babType = BabProgression.THREE_QUARTERS,
            fortType = SaveProgression.POOR,
            refType = SaveProgression.GOOD,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 6,
            spellCastingAbility = "CAR",
            classSkills = listOf("Acrobacia", "Arte da Fuga", "Atuação", "Avaliação", "Blefar", "Concentração", "Conhecimento (Arcano)", "Conhecimento (Arquitetura e Engenharia)", "Conhecimento (Geografia)", "Conhecimento (História)", "Conhecimento (Local)", "Conhecimento (Masmorras)", "Conhecimento (Natureza)", "Conhecimento (Nobreza e Realeza)", "Conhecimento (Planos)", "Conhecimento (Religião)", "Decifrar Escrita", "Diplomacia", "Disfarces", "Equilíbrio", "Escalar", "Esconder-se", "Furtividade", "Identificar Magia", "Natação", "Obter Informação", "Ofícios", "Ouvir", "Prestidigitação", "Profissão", "Saltar", "Sentir Motivação", "Usar Instrumento Mágico"),
            features = listOf(
                ClassFeature(1, "Música de Bardo", "Pode usar habilidades de música de bardo um número de vezes por dia igual ao nível de bardo; exige graduações em Atuação conforme a habilidade."),
                ClassFeature(1, "Conhecimento de Bardo", "Pode tentar recordar informações lendárias com um teste especial de conhecimento."),
                ClassFeature(1, "Música de Proteção", "Usa música ou poesia para neutralizar efeitos mágicos dependentes de som."),
                ClassFeature(1, "Fascinar (1 criatura)", "Pode fascinar uma criatura que a escute e falhe no teste de resistência; a cada três níveis após o 1º, afeta uma criatura adicional."),
                ClassFeature(4, "Fascinar (2 criaturas)", "Pode fascinar até duas criaturas com uma única ativação."),
                ClassFeature(7, "Fascinar (3 criaturas)", "Pode fascinar até três criaturas com uma única ativação."),
                ClassFeature(10, "Fascinar (4 criaturas)", "Pode fascinar até quatro criaturas com uma única ativação."),
                ClassFeature(13, "Fascinar (5 criaturas)", "Pode fascinar até cinco criaturas com uma única ativação."),
                ClassFeature(16, "Fascinar (6 criaturas)", "Pode fascinar até seis criaturas com uma única ativação."),
                ClassFeature(19, "Fascinar (7 criaturas)", "Pode fascinar até sete criaturas com uma única ativação."),
                ClassFeature(1, "Inspirar Coragem +1", "Aliados que escutam recebem +1 nos testes contra medo e encantamento e nas jogadas de ataque e dano."),
                ClassFeature(3, "Inspirar Competência", "Concede +2 de bônus de competência nos testes de perícia de um aliado."),
                ClassFeature(6, "Sugestão", "Pode sugerir uma ação a uma criatura já fascinada, conforme as regras da habilidade."),
                ClassFeature(8, "Inspirar Coragem +2", "O bônus de Inspirar Coragem aumenta para +2."),
                ClassFeature(9, "Inspirar Grandeza", "Concede 2 Dados de Vida adicionais e bônus de combate; afeta uma criatura no 9º nível e uma criatura adicional a cada três níveis de bardo depois disso (até quatro no 18º nível)."),
                ClassFeature(12, "Melodia da Libertação", "Pode produzir um efeito semelhante à magia Cancelar Encantamento por meio de música."),
                ClassFeature(14, "Inspirar Coragem +3", "O bônus de Inspirar Coragem aumenta para +3."),
                ClassFeature(15, "Inspirar Heroísmo", "Concede +4 de bônus de esquiva na CA e +4 de bônus de moral nos testes de resistência; afeta um aliado no 15º nível e um alvo adicional no 18º nível."),
                ClassFeature(18, "Sugestão em Massa", "Permite sugerir simultaneamente uma ação a qualquer número de criaturas já fascinadas, conforme as regras da habilidade."),
                ClassFeature(20, "Inspirar Coragem +4", "O bônus de Inspirar Coragem aumenta para +4.")
            ),
            description = "Mestre das canções místicas, magia arcana espontânea e grande versatilidade."
        ),
        ClassInfo(
            name = "Clérigo",
            hitDie = 8,
            babType = BabProgression.THREE_QUARTERS,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.POOR,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 2,
            spellCastingAbility = "SAB",
            classSkills = listOf("Concentração", "Conhecimento (Arcano)", "Conhecimento (História)", "Conhecimento (Planos)", "Conhecimento (Religião)", "Cura", "Diplomacia", "Identificar Magia", "Ofícios", "Profissão"),
            features = listOf(
                ClassFeature(1, "Aura", "A força da aura depende do poder da divindade, conforme a regra de detectar o bem ou detectar o mal."),
                ClassFeature(1, "Expulsar ou Fascinar Mortos-Vivos", "Usa essa habilidade 3 + modificador de Carisma vezes por dia; clérigos bons ou que veneram uma divindade boa expulsam, clérigos maus ou de divindade má fascinam, e clérigos neutros escolhem conforme as regras."),
                ClassFeature(1, "Conversão Espontânea", "Clérigos bons convertem magias preparadas em magias de cura; clérigos maus, em magias de infligir. Clérigos neutros escolhem uma das opções ao criar o personagem, conforme as regras."),
                ClassFeature(1, "Domínios da Divindade", "Escolhe dois domínios permitidos pela divindade ou filosofia; recebe os poderes concedidos de ambos e um espaço de magia por nível para preparar uma magia de domínio.")
            ),
            description = "Campeão divino dos deuses com magia sagrada de cura, domínios e destruição do mal."
        ),
        ClassInfo(
            name = "Druida",
            hitDie = 8,
            babType = BabProgression.THREE_QUARTERS,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.POOR,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 4,
            spellCastingAbility = "SAB",
            classSkills = listOf("Adestrar Animais", "Cavalgar", "Concentração", "Conhecimento (Natureza)", "Cura", "Diplomacia", "Identificar Magia", "Natação", "Observar", "Ofícios", "Ouvir", "Profissão", "Sobrevivência"),
            features = listOf(
                ClassFeature(1, "Companheiro Animal", "Recebe um companheiro animal; as opções e os benefícios seguem as regras da habilidade."),
                ClassFeature(1, "Senso da Natureza", "Recebe +2 em testes de Conhecimento (Natureza) e Sobrevivência."),
                ClassFeature(1, "Empatia Selvagem", "Melhora a atitude de animais; teste 1d20 + nível de druida + modificador de Carisma."),
                ClassFeature(2, "Caminho da Floresta", "Move-se por vegetação natural sem perder deslocamento nem sofrer dano, conforme as regras da habilidade."),
                ClassFeature(3, "Rastro Invisível", "Não deixa rastros em ambientes naturais e não pode ser rastreado por meios não mágicos."),
                ClassFeature(4, "Resistência à Tentação", "+4 nos testes de resistência contra habilidades similares à magia de fadas."),
                ClassFeature(5, "Forma Selvagem 1/dia", "Transforma-se em animal Pequeno ou Médio uma vez por dia, por até 1 hora por nível."),
                ClassFeature(6, "Forma Selvagem 2/dia", "Pode usar Forma Selvagem duas vezes por dia."),
                ClassFeature(7, "Forma Selvagem 3/dia", "Pode usar Forma Selvagem três vezes por dia."),
                ClassFeature(8, "Forma Selvagem (Grande)", "Pode assumir formas animais Grandes."),
                ClassFeature(9, "Imunidade a Venenos", "Torna-se imune a todos os venenos."),
                ClassFeature(10, "Forma Selvagem 4/dia", "Pode usar Forma Selvagem quatro vezes por dia."),
                ClassFeature(11, "Forma Selvagem (Miúdo)", "Pode assumir formas animais Miúdas."),
                ClassFeature(12, "Forma Selvagem (Planta)", "Pode assumir formas de criaturas do tipo planta."),
                ClassFeature(13, "Mil Faces", "Pode mudar a própria aparência à vontade, como a magia Alterar-se, apenas na forma normal."),
                ClassFeature(14, "Forma Selvagem 5/dia", "Pode usar Forma Selvagem cinco vezes por dia."),
                ClassFeature(15, "Corpo Atemporal e Forma Selvagem (Enorme)", "Não sofre penalidades nos atributos físicos por envelhecimento nem pode ser envelhecido magicamente; pode assumir formas animais Enormes."),
                ClassFeature(16, "Forma Selvagem Elemental 1/dia", "Pode assumir formas de elementais Pequenos, Médios ou Grandes uma vez por dia; esses usos são adicionais à Forma Selvagem normal."),
                ClassFeature(18, "Forma Selvagem 6/dia e Elemental 2/dia", "Pode usar Forma Selvagem animal seis vezes por dia e assumir forma elemental duas vezes por dia."),
                ClassFeature(20, "Forma Selvagem Elemental 3/dia e Elemental Enorme", "Pode assumir forma elemental três vezes por dia, incluindo elementais Enormes.")
            ),
            description = "Guardião da natureza viva com magia divina, companheiro animal e transformação selvagem."
        ),
        ClassInfo(
            name = "Guerreiro",
            hitDie = 10,
            babType = BabProgression.FULL,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.POOR,
            willType = SaveProgression.POOR,
            skillPointsPerLevel = 2,
            classSkills = listOf("Adestrar Animais", "Cavalgar", "Escalar", "Intimidação", "Natação", "Ofícios", "Saltar"),
            features = listOf(
                ClassFeature(1, "Talento Bônus de Guerreiro (1º nível)", "Recebe um talento bônus escolhido da lista de talentos de guerreiro, atendendo a todos os pré-requisitos."),
                ClassFeature(2, "Talento Bônus de Guerreiro (2º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(4, "Talento Bônus de Guerreiro (4º nível)", "Recebe um talento bônus da lista de talentos de guerreiro; Especialização em Arma não é automática e exige seus pré-requisitos."),
                ClassFeature(6, "Talento Bônus de Guerreiro (6º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(8, "Talento Bônus de Guerreiro (8º nível)", "Recebe um talento bônus da lista de talentos de guerreiro; Foco em Arma Maior não é concedido automaticamente."),
                ClassFeature(10, "Talento Bônus de Guerreiro (10º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(12, "Talento Bônus de Guerreiro (12º nível)", "Recebe um talento bônus da lista de talentos de guerreiro; Especialização em Arma Maior não é automática e exige seus pré-requisitos."),
                ClassFeature(14, "Talento Bônus de Guerreiro (14º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(16, "Talento Bônus de Guerreiro (16º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(18, "Talento Bônus de Guerreiro (18º nível)", "Recebe um talento bônus da lista de talentos de guerreiro."),
                ClassFeature(20, "Talento Bônus de Guerreiro (20º nível)", "Recebe um talento bônus da lista de talentos de guerreiro.")
            ),
            description = "O mestre supremo de todas as armas e armaduras com o maior arsenal de talentos do jogo."
        ),
        ClassInfo(
            name = "Monge",
            hitDie = 8,
            babType = BabProgression.THREE_QUARTERS,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.GOOD,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 4,
            classSkills = listOf("Acrobacia", "Arte da Fuga", "Atuação", "Concentração", "Conhecimento (Arcano)", "Conhecimento (Religião)", "Diplomacia", "Equilíbrio", "Escalar", "Esconder-se", "Furtividade", "Natação", "Observar", "Ofícios", "Ouvir", "Profissão", "Saltar", "Sentir Motivação"),
            features = listOf(
                ClassFeature(1, "Bônus de CA sem Armadura", "Adiciona o modificador de Sabedoria à CA enquanto não usa armadura nem escudo e carrega apenas carga leve; o bônus aumenta em +1 a cada 5 níveis."),
                ClassFeature(1, "Rajada de Golpes", "Pode realizar ataques adicionais na rajada conforme a progressão de nível, usando as penalidades indicadas na tabela do Monge."),
                ClassFeature(1, "Ataque Desarmado Aprimorado", "Recebe o talento Ataque Desarmado Aprimorado como talento bônus; seus ataques desarmados causam dano letal e podem atingir criaturas com armas naturais."),
                ClassFeature(1, "Talento Bônus de Monge (1º nível)", "Escolhe Agarrar Aprimorado ou Ataque Atordoante como talento adicional, sem precisar cumprir os pré-requisitos."),
                ClassFeature(2, "Talento Bônus de Monge (2º nível)", "Escolhe Reflexos em Combate ou Desviar Objetos, sem precisar cumprir os pré-requisitos.") ,
                ClassFeature(2, "Evasão", "Quando está sem armadura ou usando armadura leve, não sofre dano em um teste de Reflexos bem-sucedido contra um efeito que normalmente causa metade do dano."),
                ClassFeature(3, "Mente Tranquila", "+2 nos testes de resistência contra magias e efeitos da escola de encantamento."),
                ClassFeature(3, "Movimento Rápido +3m", "O deslocamento terrestre aumenta para 12 m no 3º nível e mais 3 m a cada três níveis de monge; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(6, "Movimento Rápido +6m", "O deslocamento terrestre aumenta para 15 m; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(9, "Movimento Rápido +9m", "O deslocamento terrestre aumenta para 18 m; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(12, "Movimento Rápido +12m", "O deslocamento terrestre aumenta para 21 m; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(15, "Movimento Rápido +15m", "O deslocamento terrestre aumenta para 24 m; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(18, "Movimento Rápido +18m", "O deslocamento terrestre aumenta para 27 m; perde o bônus ao usar qualquer armadura ou escudo, ou carregar carga média ou pesada."),
                ClassFeature(4, "Queda Suave 6 m", "Reduz o dano de queda quando há uma parede ao alcance; a distância aumenta com o nível."),
                ClassFeature(4, "Ataque Chi (Mágico)", "Ataques desarmados contam como mágicos para superar redução de dano."),
                ClassFeature(5, "Pureza Corporal", "Imunidade a todas as doenças, exceto doenças sobrenaturais e mágicas."),
                ClassFeature(6, "Talento Bônus de Monge (6º nível)", "Escolhe Desarme Aprimorado ou Imobilização Aprimorada, sem precisar cumprir os pré-requisitos."),
                ClassFeature(6, "Queda Suave 9 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(7, "Integridade Corporal", "Cura diariamente uma quantidade de pontos de vida igual ao dobro do nível de monge."),
                ClassFeature(8, "Queda Suave 12 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(9, "Evasão Aprimorada", "Em um teste de Reflexos falho, sofre metade do dano; em um teste bem-sucedido, não sofre dano."),
                ClassFeature(10, "Queda Suave 15 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(10, "Ataque Chi (Ordem)", "Os ataques desarmados passam a contar como ordeiros para superar redução de dano."),
                ClassFeature(11, "Corpo de Diamante", "Imunidade a todos os venenos."),
                ClassFeature(11, "Rajada Maior", "Recebe um segundo ataque adicional na rajada de golpes, também com o bônus base de ataque máximo."),
                ClassFeature(12, "Passo Etéreo", "Uma vez por dia, desloca-se magicamente entre dois espaços com efeito semelhante à magia Porta Dimensional; nível de conjurador igual à metade do nível de monge, arredondado para baixo."),
                ClassFeature(12, "Queda Suave 18 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(13, "Alma de Diamante", "Recebe resistência à magia igual a 10 + nível de monge."),
                ClassFeature(14, "Queda Suave 21 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(15, "Mão Vibrante", "Uma vez por semana, pode tentar um ataque especial potencialmente mortal."),
                ClassFeature(16, "Ataque Chi (Adamante)", "Os ataques desarmados passam a contar como adamantinos para superar redução de dano."),
                ClassFeature(16, "Queda Suave 24 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(17, "Corpo Atemporal", "Não sofre penalidades nos atributos físicos por envelhecimento e não pode ser envelhecido magicamente."),
                ClassFeature(17, "Idiomas do Sol e da Lua", "Pode falar com qualquer criatura viva."),
                ClassFeature(18, "Queda Suave 27 m", "Reduz o dano de queda quando há uma parede ao alcance."),
                ClassFeature(19, "Corpo Vazio", "Pode tornar-se etéreo por 1 rodada por nível de monge ao dia."),
                ClassFeature(20, "Queda Suave Qualquer Distância", "Pode reduzir o dano de queda de qualquer distância quando há uma parede ao alcance."),
                ClassFeature(20, "Auto-Perfeição", "É tratado como extra-planar para magias e efeitos e recebe Redução de Dano 10/mágica.")
            ),
            description = "Guerreiro asceta das artes marciais com três testes de resistência bons e golpes desarmados mortais."
        ),
        ClassInfo(
            name = "Paladino",
            hitDie = 10,
            babType = BabProgression.FULL,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.POOR,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 2,
            spellCastingAbility = "SAB",
            classSkills = listOf("Adestrar Animais", "Cavalgar", "Concentração", "Conhecimento (Nobreza e Realeza)", "Conhecimento (Religião)", "Cura", "Diplomacia", "Ofícios", "Profissão", "Sentir Motivação"),
            features = listOf(
                ClassFeature(1, "Aura do Bem", "A aura de bondade é detectável por efeitos apropriados."),
                ClassFeature(1, "Detectar o Mal", "Pode detectar o mal à vontade."),
                ClassFeature(1, "Destruir o Mal 1/dia", "Uma vez ao dia, soma Carisma à jogada de ataque e nível de paladino ao dano contra um alvo maligno."),
                ClassFeature(2, "Graça Divina", "Adiciona o modificador de Carisma a todos os testes de resistência."),
                ClassFeature(2, "Imposição de Mãos", "Cura diariamente um total de pontos de vida igual ao nível de paladino × modificador de Carisma."),
                ClassFeature(3, "Aura de Coragem", "O paladino fica imune ao medo; aliados próximos recebem +4 nos testes contra medo."),
                ClassFeature(3, "Saúde Divina", "Imunidade a todas as doenças."),
                ClassFeature(4, "Expulsar Mortos-Vivos", "Expulsa mortos-vivos como um clérigo de nível igual ao nível de paladino menos 3."),
                ClassFeature(4, "Conjuração Divina", "Recebe a capacidade de conjurar magias divinas de paladino."),
                ClassFeature(5, "Destruir o Mal 2/dia", "Pode usar Destruir o Mal duas vezes ao dia."),
                ClassFeature(5, "Montaria Especial", "Recebe uma montaria especial."),
                ClassFeature(3, "Remover Doença 1/semana", "Pode remover doença uma vez por semana."),
                ClassFeature(6, "Remover Doença 2/semana", "Pode remover doença duas vezes por semana."),
                ClassFeature(10, "Destruir o Mal 3/dia", "Pode usar Destruir o Mal três vezes ao dia."),
                ClassFeature(9, "Remover Doença 3/semana", "Pode remover doença três vezes por semana."),
                ClassFeature(15, "Destruir o Mal 4/dia", "Pode usar Destruir o Mal quatro vezes ao dia."),
                ClassFeature(12, "Remover Doença 4/semana", "Pode remover doença quatro vezes por semana."),
                ClassFeature(15, "Remover Doença 5/semana", "Pode remover doença cinco vezes por semana."),
                ClassFeature(18, "Remover Doença 6/semana", "Pode remover doença seis vezes por semana."),
                ClassFeature(20, "Destruir o Mal 5/dia", "Pode usar Destruir o Mal cinco vezes ao dia.")
            ),
            description = "Campeão supremo da retidão e da honra divina, com Graça Divina e Destruição do Mal."
        ),
        ClassInfo(
            name = "Ranger",
            hitDie = 8,
            babType = BabProgression.FULL,
            fortType = SaveProgression.GOOD,
            refType = SaveProgression.GOOD,
            willType = SaveProgression.POOR,
            skillPointsPerLevel = 6,
            spellCastingAbility = "SAB",
            classSkills = listOf("Adestrar Animais", "Cavalgar", "Concentração", "Conhecimento (Geografia)", "Conhecimento (Masmorras)", "Conhecimento (Natureza)", "Cura", "Escalar", "Esconder-se", "Furtividade", "Natação", "Observar", "Ofícios", "Ouvir", "Procurar", "Profissão", "Saltar", "Sobrevivência", "Usar Cordas"),
            features = listOf(
                ClassFeature(1, "Inimigo Predileto +2", "+2 em testes de Blefar, Ouvir, Sentir Motivação, Observar e Sobrevivência, além de dano contra um tipo de criatura escolhido."),
                ClassFeature(1, "Rastrear e Empatia Selvagem", "Recebe o talento Rastrear e pode influenciar animais com um teste de nível de ranger + modificador de Carisma."),
                ClassFeature(2, "Estilo de Combate", "Escolhe arquearia ou combate com duas armas; os talentos do estilo não exigem seus pré-requisitos normais enquanto usar armadura leve ou nenhuma."),
                ClassFeature(3, "Tolerância", "Recebe o talento Tolerância como talento adicional."),
                ClassFeature(4, "Conjuração de Ranger", "Começa a conjurar magias divinas de ranger; a progressão e as magias conhecidas seguem a tabela da classe."),
                ClassFeature(4, "Companheiro Animal", "Recebe um companheiro animal; seu nível efetivo de druida é reduzido conforme a tabela do ranger."),
                ClassFeature(5, "Inimigo Predileto +2 adicional", "Escolhe outro inimigo predileto com bônus +2 ou aumenta em +2 o bônus contra um inimigo predileto existente."),
                ClassFeature(6, "Estilo de Combate Aprimorado", "Recebe o talento aprimorado do estilo escolhido, sem precisar cumprir seus pré-requisitos normais enquanto usar armadura leve ou nenhuma."),
                ClassFeature(7, "Caminho da Floresta", "Move-se através de vegetação rasteira natural sem sofrer impedimentos ou dano, salvo exceções mágicas."),
                ClassFeature(8, "Rastreador Veloz", "Pode rastrear à velocidade normal sem a penalidade de -5; rastrear correndo ainda impõe a penalidade aplicável."),
                ClassFeature(9, "Evasão", "Funciona apenas sem armadura ou com armadura leve; contra efeitos que permitem um teste de Reflexos para reduzir o dano à metade, não sofre dano em um sucesso."),
                ClassFeature(10, "Inimigo Predileto +2 adicional", "Escolhe outro inimigo predileto com bônus +2 ou aumenta em +2 o bônus contra um inimigo predileto existente."),
                ClassFeature(11, "Mestrado no Estilo de Combate", "Recebe o talento superior do estilo escolhido, sem precisar cumprir seus pré-requisitos normais enquanto usar armadura leve ou nenhuma."),
                ClassFeature(15, "Inimigo Predileto +2 adicional", "Escolhe outro inimigo predileto com bônus +2 ou aumenta em +2 o bônus contra um inimigo predileto existente."),
                ClassFeature(17, "Camuflagem", "Pode usar a perícia Esconder-se em qualquer tipo de terreno natural, mesmo sem cobertura ou camuflagem."),
                ClassFeature(18, "Esconder-se à Plena Vista", "Pode usar Esconder-se em qualquer tipo de terreno natural, mesmo sem cobertura ou camuflagem e mesmo enquanto observado, desde que esteja a até 3 metros de algum tipo de terreno natural."),
                ClassFeature(20, "Inimigo Predileto +2 adicional", "Escolhe outro inimigo predileto com bônus +2 ou aumenta em +2 o bônus contra um inimigo predileto existente.")
            ),
            description = "Mestre das terras ermas, com arquearia, rastreamento mortal e inimigos prediletos."
        ),
        ClassInfo(
            name = "Ladino",
            hitDie = 6,
            babType = BabProgression.THREE_QUARTERS,
            fortType = SaveProgression.POOR,
            refType = SaveProgression.GOOD,
            willType = SaveProgression.POOR,
            skillPointsPerLevel = 8,
            classSkills = listOf("Abrir Fechaduras", "Acrobacia", "Arte da Fuga", "Atuação", "Avaliação", "Blefar", "Decifrar Escrita", "Diplomacia", "Disfarces", "Equilíbrio", "Escalar", "Esconder-se", "Falsificação", "Furtividade", "Intimidação", "Natação", "Observar", "Obter Informação", "Ofícios", "Operar Mecanismo", "Ouvir", "Prestidigitação", "Profissão", "Conhecimento (Local)", "Procurar", "Saltar", "Sentir Motivação", "Usar Cordas", "Usar Instrumento Mágico"),
            features = listOf(
                ClassFeature(1, "Ataque Furtivo +1d6", "Dano extra contra oponentes flanqueados ou sem bônus de Destreza (+1d6 a cada 2 níveis)."),
                ClassFeature(1, "Encontrar Armadilhas", "Pode usar Procurar para localizar armadilhas com CD acima de 20 e Operar Mecanismo para desarmar armadilhas mágicas."),
                ClassFeature(2, "Evasão", "Funciona apenas sem armadura ou com armadura leve; contra efeitos que permitem um teste de Reflexos para reduzir o dano à metade, não sofre dano em um sucesso."),
                ClassFeature(3, "Sentir Armadilhas +1", "+1 na CA e Reflexos contra armadilhas."),
                ClassFeature(6, "Sentir Armadilhas +2", "+2 na CA e Reflexos contra armadilhas."),
                ClassFeature(9, "Sentir Armadilhas +3", "+3 na CA e Reflexos contra armadilhas."),
                ClassFeature(12, "Sentir Armadilhas +4", "+4 na CA e Reflexos contra armadilhas."),
                ClassFeature(15, "Sentir Armadilhas +5", "+5 na CA e Reflexos contra armadilhas."),
                ClassFeature(18, "Sentir Armadilhas +6", "+6 na CA e Reflexos contra armadilhas."),
                ClassFeature(3, "Ataque Furtivo +2d6", "Dano furtivo sobe para +2d6."),
                ClassFeature(4, "Esquiva Sobrenatural", "Conserva o bônus de Destreza na CA mesmo quando surpreendido ou atacado por um oponente invisível; ainda perde esse bônus se estiver imobilizado."),
                ClassFeature(5, "Ataque Furtivo +3d6", "Dano furtivo sobe para +3d6."),
                ClassFeature(7, "Ataque Furtivo +4d6", "Dano furtivo sobe para +4d6."),
                ClassFeature(8, "Esquiva Sobrenatural Aprimorada", "Não pode ser flanqueado, exceto por ladino pelo menos 4 níveis acima."),
                ClassFeature(9, "Ataque Furtivo +5d6", "Dano furtivo sobe para +5d6."),
                ClassFeature(10, "Habilidade Especial (10º Nv)", "Escolhe uma habilidade especial de ladino."),
                ClassFeature(11, "Ataque Furtivo +6d6", "Dano furtivo sobe para +6d6."),
                ClassFeature(13, "Ataque Furtivo +7d6", "Dano furtivo sobe para +7d6."),
                ClassFeature(13, "Habilidade Especial (13º Nv)", "Escolhe uma habilidade especial de ladino."),
                ClassFeature(15, "Ataque Furtivo +8d6", "Dano furtivo sobe para +8d6."),
                ClassFeature(16, "Habilidade Especial (16º Nv)", "Escolhe uma habilidade especial de ladino."),
                ClassFeature(17, "Ataque Furtivo +9d6", "Dano furtivo sobe para +9d6."),
                ClassFeature(19, "Ataque Furtivo +10d6", "Dano furtivo máximo de +10d6."),
                ClassFeature(19, "Habilidade Especial (19º Nv)", "Escolhe uma habilidade especial de ladino.")
            ),
            description = "Especialista mortal em infiltração, arrombamento de mecanismos e ataques furtivos devastadores."
        ),
        ClassInfo(
            name = "Feiticeiro",
            hitDie = 4,
            babType = BabProgression.HALF,
            fortType = SaveProgression.POOR,
            refType = SaveProgression.POOR,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 2,
            spellCastingAbility = "CAR",
            classSkills = listOf("Blefar", "Concentração", "Conhecimento (Arcano)", "Identificar Magia", "Ofícios", "Profissão"),
            features = listOf(
                ClassFeature(1, "Conjuração Espontânea", "Conjura magias arcanas sem prepará-las antecipadamente; conhece um repertório limitado de magias e tem um limite diário de usos por nível."),
                ClassFeature(1, "Invocar Familiar", "Obtém um familiar mágico que compartilha benefícios especiais com o feiticeiro; não é a habilidade Companheiro Animal do druida ou ranger.")
            ),
            description = "Conjurador nato com o poder de dragões ou seres arcanos fluindo diretamente no sangue."
        ),
        ClassInfo(
            name = "Mago",
            hitDie = 4,
            babType = BabProgression.HALF,
            fortType = SaveProgression.POOR,
            refType = SaveProgression.POOR,
            willType = SaveProgression.GOOD,
            skillPointsPerLevel = 2,
            spellCastingAbility = "INT",
            classSkills = listOf("Concentração", "Conhecimento (Arcano)", "Conhecimento (Arquitetura e Engenharia)", "Conhecimento (Geografia)", "Conhecimento (História)", "Conhecimento (Local)", "Conhecimento (Masmorras)", "Conhecimento (Natureza)", "Conhecimento (Nobreza e Realeza)", "Conhecimento (Planos)", "Conhecimento (Religião)", "Decifrar Escrita", "Identificar Magia", "Ofícios", "Profissão"),
            features = listOf(
                ClassFeature(1, "Preparação com Grimório", "Estuda e prepara magias diárias a partir do seu livro de feitiços."),
                ClassFeature(1, "Escrever Pergaminho", "Recebe Escrever Pergaminho como talento bônus no 1º nível."),
                ClassFeature(1, "Invocar Familiar", "Obtém um familiar mágico que concede benefícios especiais; não é um companheiro animal comum."),
                ClassFeature(1, "Especialização em Escola (Opcional)", "Se especializar, recebe uma magia preparada adicional por nível de magia da escola escolhida e perde acesso às escolas proibidas; um especialista em Adivinhação escolhe apenas uma escola proibida."),
                ClassFeature(5, "Talento Bônus de Mago (5º Nv)", "Recebe um talento bônus de metamagia, criação de item ou Maestria em Magia, se cumprir os pré-requisitos."),
                ClassFeature(10, "Talento Bônus de Mago (10º Nv)", "Recebe um talento bônus de metamagia, criação de item ou Maestria em Magia, se cumprir os pré-requisitos."),
                ClassFeature(15, "Talento Bônus de Mago (15º Nv)", "Recebe um talento bônus de metamagia, criação de item ou Maestria em Magia, se cumprir os pré-requisitos."),
                ClassFeature(20, "Talento Bônus de Mago (20º Nv)", "Recebe um talento bônus de metamagia, criação de item ou Maestria em Magia, se cumprir os pré-requisitos.")
            ),
            description = "Estudioso das artes arcanas que prepara magias a partir de um grimório e pode ampliar seu repertório ao aprender novas magias."
        )
    )

    fun calculateModifier(score: Int): Int {
        return Math.floorDiv(score - 10, 2)
    }

    fun calculateBaseAttackBonus(babType: BabProgression, level: Int): Int {
        val lvl = level.coerceAtLeast(1)
        return when (babType) {
            BabProgression.FULL -> lvl
            BabProgression.THREE_QUARTERS -> (lvl * 3) / 4
            BabProgression.HALF -> lvl / 2
        }
    }

    fun calculateBaseSave(progression: SaveProgression, level: Int): Int {
        val lvl = level.coerceAtLeast(1)
        return when (progression) {
            SaveProgression.GOOD -> 2 + (lvl / 2)
            SaveProgression.POOR -> lvl / 3
        }
    }

    // Tabela oficial 9-1: Capacidade de Carga (em Libras - lb)
    data class CarryingCapacity(
        val lightMaxLb: Int,
        val mediumMaxLb: Int,
        val heavyMaxLb: Int
    )

    private val LOAD_TABLE_LB = mapOf(
        1 to CarryingCapacity(3, 6, 10),
        2 to CarryingCapacity(6, 13, 20),
        3 to CarryingCapacity(10, 20, 30),
        4 to CarryingCapacity(13, 26, 40),
        5 to CarryingCapacity(16, 33, 50),
        6 to CarryingCapacity(20, 40, 60),
        7 to CarryingCapacity(23, 46, 70),
        8 to CarryingCapacity(26, 53, 80),
        9 to CarryingCapacity(30, 60, 90),
        10 to CarryingCapacity(33, 66, 100),
        11 to CarryingCapacity(38, 76, 115),
        12 to CarryingCapacity(43, 86, 130),
        13 to CarryingCapacity(50, 100, 150),
        14 to CarryingCapacity(58, 116, 175),
        15 to CarryingCapacity(66, 133, 200),
        16 to CarryingCapacity(76, 153, 230),
        17 to CarryingCapacity(86, 173, 260),
        18 to CarryingCapacity(100, 200, 300),
        19 to CarryingCapacity(116, 233, 350),
        20 to CarryingCapacity(133, 266, 400),
        21 to CarryingCapacity(153, 306, 460),
        22 to CarryingCapacity(173, 346, 520),
        23 to CarryingCapacity(200, 400, 600),
        24 to CarryingCapacity(233, 466, 700),
        25 to CarryingCapacity(266, 533, 800),
        26 to CarryingCapacity(306, 613, 920),
        27 to CarryingCapacity(346, 693, 1040),
        28 to CarryingCapacity(400, 800, 1200),
        29 to CarryingCapacity(466, 933, 1400)
    )

    fun getCarryingCapacity(strengthScore: Int, size: String = "Médio"): CarryingCapacity {
        val str = strengthScore.coerceAtLeast(1)
        val baseCapacity = if (str <= 29) {
            LOAD_TABLE_LB[str] ?: CarryingCapacity(33, 66, 100)
        } else {
            val remainderStr = (str - 10).coerceAtLeast(1)
            val subCap = getCarryingCapacity(remainderStr, "Médio")
            CarryingCapacity(subCap.lightMaxLb * 4, subCap.mediumMaxLb * 4, subCap.heavyMaxLb * 4)
        }

        // Multiplicadores de carga por categoria de tamanho (D&D 3.5).
        // As categorias menores estavam agrupadas incorretamente como Pequeno.
        val multiplier = when (size.trim().lowercase()) {
            "minúsculo", "minusculo", "minúscula", "minuscula", "fine" -> 0.125f
            "diminuto", "diminuta", "diminutive" -> 0.25f
            "miúdo", "miuda", "miúda", "tiny" -> 0.5f
            "pequeno", "pequena", "small" -> 0.75f
            "grande", "large" -> 2.0f
            "enorme", "huge" -> 4.0f
            "imenso", "imensa", "gargantuan" -> 8.0f
            "colossal" -> 16.0f
            else -> 1.0f
        }

        return CarryingCapacity(
            lightMaxLb = (baseCapacity.lightMaxLb * multiplier).toInt(),
            mediumMaxLb = (baseCapacity.mediumMaxLb * multiplier).toInt(),
            heavyMaxLb = (baseCapacity.heavyMaxLb * multiplier).toInt()
        )
    }

    /** Modificador de tamanho aplicado à CA e às jogadas de ataque. */
    fun getArmorClassSizeModifier(size: String): Int = when (size.trim().lowercase()) {
        "minúsculo", "minusculo", "fine" -> 8
        "diminuto", "diminuta", "diminutive" -> 4
        "miúdo", "miuda", "miúda", "tiny" -> 2
        "pequeno", "pequena", "small" -> 1
        "grande", "large" -> -1
        "enorme", "huge" -> -2
        "imenso", "imensa", "gargantuan" -> -4
        "colossal" -> -8
        else -> 0
    }

    /** Modificador de tamanho para testes de agarrar (oposto ao da CA/ataque). */
    fun getGrappleSizeModifier(size: String): Int = when (size.trim().lowercase()) {
        "minúsculo", "minusculo", "fine" -> -16
        "diminuto", "diminuta", "diminutive" -> -12
        "miúdo", "miuda", "miúda", "tiny" -> -8
        "pequeno", "pequena", "small" -> -4
        "grande", "large" -> 4
        "enorme", "huge" -> 8
        "imenso", "imensa", "gargantuan" -> 12
        "colossal" -> 16
        else -> 0
    }

    enum class EncumbranceLevel(
        val label: String,
        val maxDexBonus: Int?,
        val armorCheckPenalty: Int,
        val speedFactor: Float
    ) {
        LIGHT("Carga Leve", null, 0, 1.0f),
        MEDIUM("Carga Média", 3, -3, 0.67f),
        HEAVY("Carga Pesada", 1, -6, 0.67f),
        OVERLOADED("Sobrecarga", 0, -10, 0.0f)
    }

    fun calculateEncumbrance(totalWeightLb: Float, strengthScore: Int, size: String): EncumbranceLevel {
        val cap = getCarryingCapacity(strengthScore, size)
        return when {
            totalWeightLb <= cap.lightMaxLb -> EncumbranceLevel.LIGHT
            totalWeightLb <= cap.mediumMaxLb -> EncumbranceLevel.MEDIUM
            totalWeightLb <= cap.heavyMaxLb -> EncumbranceLevel.HEAVY
            else -> EncumbranceLevel.OVERLOADED
        }
    }

    /**
     * Converte nomes legados salvos em fichas para o rótulo canônico atual.
     * Não altera os dados persistidos automaticamente; o cálculo mantém graduações e bônus.
     */
    fun canonicalSkillName(name: String): String = when (name.trim().lowercase()) {
        "salto" -> "Saltar"
        "ofício" -> "Ofícios"
        else -> name.trim()
    }

    /** Ofícios (especialidade) é uma perícia própria, mas herda a classificação e o atributo de Ofícios. */
    fun isCraftSpecialty(name: String): Boolean {
        val normalized = canonicalSkillName(name)
        return normalized.startsWith("Ofícios (", ignoreCase = true) &&
            normalized.endsWith(")") &&
            normalized.substringAfter("(", "").removeSuffix(")").isNotBlank()
    }

    /** Conhecimento admite teste sem graduação para informações comuns (CD 10 ou inferior). */
    fun allowsUntrainedKnowledgeCheck(skillName: String): Boolean =
        canonicalSkillName(skillName).startsWith("Conhecimento (", ignoreCase = true)

    fun classSkillMatches(skillName: String, listedClassSkill: String): Boolean {
        val skill = canonicalSkillName(skillName)
        val listed = canonicalSkillName(listedClassSkill)
        return skill.equals(listed, ignoreCase = true) ||
            (listed.equals("Ofícios", ignoreCase = true) && isCraftSpecialty(skill))
    }

    fun canonicalSkillKeyAbility(skillName: String, fallback: String): String {
        val canonical = canonicalSkillName(skillName)
        return SKILL_DEFINITIONS.firstOrNull { it.name.equals(canonical, ignoreCase = true) }?.keyAbility
            ?: if (isCraftSpecialty(canonical)) "INT" else fallback
    }

    // Perícias canônicas de D&D 3.5, usando os nomes da tradução portuguesa.
    data class SkillDefinition(
        val name: String,
        val keyAbility: String,
        val armorCheckPenaltyApplies: Boolean,
        val trainedOnly: Boolean = false
    )

    val SKILL_DEFINITIONS = listOf(
        SkillDefinition("Abrir Fechaduras", "DES", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Acrobacia", "DES", armorCheckPenaltyApplies = true),
        SkillDefinition("Adestrar Animais", "CAR", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Arte da Fuga", "DES", armorCheckPenaltyApplies = true),
        SkillDefinition("Atuação", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Avaliação", "INT", armorCheckPenaltyApplies = false),
        SkillDefinition("Blefar", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Cavalgar", "DES", armorCheckPenaltyApplies = false),
        SkillDefinition("Concentração", "CON", armorCheckPenaltyApplies = false),
        SkillDefinition("Conhecimento (Arcano)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Arquitetura e Engenharia)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Geografia)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (História)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Local)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Masmorras)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Natureza)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Nobreza e Realeza)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Planos)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Conhecimento (Religião)", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Cura", "SAB", armorCheckPenaltyApplies = false),
        SkillDefinition("Decifrar Escrita", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Diplomacia", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Disfarces", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Equilíbrio", "DES", armorCheckPenaltyApplies = true),
        SkillDefinition("Escalar", "FOR", armorCheckPenaltyApplies = true),
        SkillDefinition("Esconder-se", "DES", armorCheckPenaltyApplies = true),
        SkillDefinition("Falsificação", "INT", armorCheckPenaltyApplies = false),
        SkillDefinition("Furtividade", "DES", armorCheckPenaltyApplies = true),
        SkillDefinition("Identificar Magia", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Intimidação", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Natação", "FOR", armorCheckPenaltyApplies = true),
        SkillDefinition("Observar", "SAB", armorCheckPenaltyApplies = false),
        SkillDefinition("Obter Informação", "CAR", armorCheckPenaltyApplies = false),
        SkillDefinition("Ofícios", "INT", armorCheckPenaltyApplies = false),
        SkillDefinition("Operar Mecanismo", "INT", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Ouvir", "SAB", armorCheckPenaltyApplies = false),
        SkillDefinition("Prestidigitação", "DES", armorCheckPenaltyApplies = true, trainedOnly = true),
        SkillDefinition("Procurar", "INT", armorCheckPenaltyApplies = false),
        SkillDefinition("Profissão", "SAB", armorCheckPenaltyApplies = false, trainedOnly = true),
        SkillDefinition("Saltar", "FOR", armorCheckPenaltyApplies = true),
        SkillDefinition("Sentir Motivação", "SAB", armorCheckPenaltyApplies = false),
        SkillDefinition("Sobrevivência", "SAB", armorCheckPenaltyApplies = false),
        SkillDefinition("Usar Cordas", "DES", armorCheckPenaltyApplies = false),
        SkillDefinition("Usar Instrumento Mágico", "CAR", armorCheckPenaltyApplies = false, trainedOnly = true)
    )

    // Catálogo de Talentos do PHB 3.5
    data class FeatDefinition(
        val name: String,
        val prerequisites: String,
        val summary: String,
        val benefit: String
    )

    val PRESET_FEATS: List<FeatDefinition> by lazy { loadFeatsCatalog() }

    // ==========================================
    // CATÁLOGO DE ITENS REFINADO COM CATEGORIAS E SLOTS
    // ==========================================
    data class PresetItem(
        val name: String,
        val category: String, // Armas, Armaduras, Escudos, Itens de Aventura, Itens Mágicos, Consumíveis
        val defaultSlot: String,
        val weightLb: Float,
        val costGp: Float,
        val damageOrAc: String,
        val properties: String
    )

    val PRESET_ITEMS: List<PresetItem> by lazy { loadItemsCatalog() }

    // ==========================================
    // CATÁLOGO EXPANDIDO DE MAGIAS DO LIVRO DO JOGADOR
    // ==========================================
    data class PresetSpell(
        val name: String,
        val level: Int, // 0 a 9
        val school: String, // Abjuração, Adivinhação, Conjuração, Encantamento, Evocação, Ilusão, Necromancia, Transmutação, Universal
        val classes: List<String>, // classes que podem conjurar a magia
        val castTime: String,
        val components: String, // V, S, M, DF
        val range: String,
        val duration: String,
        val savingThrow: String,
        val spellResistance: Boolean,
        val description: String,
        // Nível da magia por classe; quando omitido, usa "level" para todas.
        val classLevels: Map<String, Int> = classes.associateWith { level },
        // Níveis por domínio clerical; não são classes de personagem.
        val domainLevels: Map<String, Int> = emptyMap()
    )

    val PRESET_SPELLS: List<PresetSpell> by lazy { loadSpellsCatalog() }

    // ==========================================
    // CARREGAMENTO DOS CATÁLOGOS A PARTIR DE JSON
    // ==========================================
    // Talentos, itens e magias vêm de app/src/main/resources/dnd35/*.json, lidos pelo
    // classloader da aplicação. Funciona igual no app, em testes JVM comuns e no Robolectric,
    // sem depender de Context do Android. Ver docs/CATALOGOS_JSON.md.

    private fun loadFeatsCatalog(): List<FeatDefinition> {
        val array = CatalogJson.array("dnd35/feats.json")
        val seen = mutableSetOf<String>()
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val name = CatalogJson.requireString(o, "name", "talento #${i + 1}")
            check(seen.add(name.lowercase())) { "Talento duplicado no catálogo: $name" }
            FeatDefinition(
                name = name,
                prerequisites = CatalogJson.requireString(o, "prerequisites", name),
                summary = CatalogJson.requireString(o, "summary", name),
                benefit = CatalogJson.requireString(o, "benefit", name)
            )
        }
    }

    private fun loadItemsCatalog(): List<PresetItem> {
        val array = CatalogJson.array("dnd35/items.json")
        val seen = mutableSetOf<String>()
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val name = CatalogJson.requireString(o, "name", "item #${i + 1}")
            check(seen.add(name.lowercase())) { "Item duplicado no catálogo: $name" }
            PresetItem(
                name = name,
                category = CatalogJson.requireString(o, "category", name),
                defaultSlot = CatalogJson.requireString(o, "defaultSlot", name),
                weightLb = CatalogJson.requireFloat(o, "weightLb", name),
                costGp = CatalogJson.requireFloat(o, "costGp", name),
                damageOrAc = CatalogJson.requireString(o, "damageOrAc", name),
                properties = CatalogJson.requireString(o, "properties", name)
            )
        }
    }

    private fun loadSpellsCatalog(): List<PresetSpell> {
        val array = CatalogJson.array("dnd35/spells.json")
        val seen = mutableSetOf<String>()
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val name = CatalogJson.requireString(o, "name", "magia #${i + 1}")
            check(seen.add(name.lowercase())) { "Magia duplicada no catálogo: $name" }
            val level = CatalogJson.requireInt(o, "level", name)
            check(level in 0..9) { "Magia \"$name\": nível fora da faixa 0-9 ($level)" }
            val classes = CatalogJson.stringList(o, "classes")
            val explicitClassLevels = CatalogJson.intMap(o, "classLevels")
            val classLevels = if (explicitClassLevels.isNotEmpty()) {
                explicitClassLevels
            } else {
                classes.associateWith { level }
            }
            PresetSpell(
                name = name,
                level = level,
                school = CatalogJson.requireString(o, "school", name),
                classes = classes,
                castTime = CatalogJson.requireString(o, "castTime", name),
                components = CatalogJson.requireString(o, "components", name),
                range = CatalogJson.requireString(o, "range", name),
                duration = CatalogJson.requireString(o, "duration", name),
                savingThrow = CatalogJson.requireString(o, "savingThrow", name),
                spellResistance = CatalogJson.requireBoolean(o, "spellResistance", name),
                description = CatalogJson.requireString(o, "description", name),
                classLevels = classLevels,
                domainLevels = CatalogJson.intMap(o, "domainLevels")
            )
        }
    }
}

/**
 * Leitura de catálogos a partir de JSON empacotado em app/src/main/resources/.
 * Lança [IllegalStateException] com o nome/posição do item quando falta um campo
 * obrigatório, para que um JSON quebrado falhe no teste em vez de virar uma ficha incompleta.
 */
private object CatalogJson {
    fun array(resourcePath: String): org.json.JSONArray {
        val stream = Dnd35Rules::class.java.classLoader?.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Catálogo não encontrado no classpath: $resourcePath")
        val text = stream.use { String(it.readBytes(), Charsets.UTF_8) }
        return org.json.JSONArray(text)
    }

    fun requireString(o: org.json.JSONObject, key: String, context: String): String {
        if (!o.has(key) || o.isNull(key)) {
            throw IllegalStateException("$context: campo obrigatório ausente: $key")
        }
        return o.getString(key)
    }

    fun requireFloat(o: org.json.JSONObject, key: String, context: String): Float {
        if (!o.has(key) || o.isNull(key)) {
            throw IllegalStateException("$context: campo obrigatório ausente: $key")
        }
        return o.getDouble(key).toFloat()
    }

    fun requireInt(o: org.json.JSONObject, key: String, context: String): Int {
        if (!o.has(key) || o.isNull(key)) {
            throw IllegalStateException("$context: campo obrigatório ausente: $key")
        }
        return o.getInt(key)
    }

    fun requireBoolean(o: org.json.JSONObject, key: String, context: String): Boolean {
        if (!o.has(key) || o.isNull(key)) {
            throw IllegalStateException("$context: campo obrigatório ausente: $key")
        }
        return o.getBoolean(key)
    }

    fun stringList(o: org.json.JSONObject, key: String): List<String> {
        val arr = o.optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun intMap(o: org.json.JSONObject, key: String): Map<String, Int> {
        val obj = o.optJSONObject(key) ?: return emptyMap()
        val out = LinkedHashMap<String, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.getInt(k)
        }
        return out
    }
}
