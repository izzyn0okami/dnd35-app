#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Gera os recursos de ícones do app a partir de um clone de https://github.com/game-icons/icons.

Uso:
    python3 tools/game_icons/gerar.py --source /caminho/para/game-icons/icons

O que faz (tudo determinístico e idempotente):
  * converte cada ícone listado em mapa_icones.py para VectorDrawable (res/drawable/gi_*.xml);
  * gera ui/icons/GameIcons.kt (registro chave -> recurso, rótulo, grupo, autor);
  * gera ui/icons/GameIconCredits.kt (autores dos ícones realmente usados, para a tela de créditos);
  * grava o campo "icon" de cada item em dnd35/items.json de acordo com ITEM_RULES;
  * gera docs/ICONES_CREDITOS.md (ícone por ícone: autor, licença e arquivo de origem).

Os SVGs do game-icons.net têm dois <path>: um retângulo de fundo preto e o desenho em branco.
Só o desenho é aproveitado; o app o tinge com a cor do tema.
"""
import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from mapa_icones import AUTHOR_OVERRIDES, ICON_GROUPS, ITEM_RULES  # noqa: E402

REPO = Path(__file__).resolve().parents[2]
DRAWABLE_DIR = REPO / "app/src/main/res/drawable"
ICONS_KT_DIR = REPO / "app/src/main/java/com/example/dnd35/ui/icons"
ITEMS_JSON = REPO / "app/src/main/resources/dnd35/items.json"
CREDITS_MD = REPO / "docs/ICONES_CREDITOS.md"

LICENSE_URL_BY = "https://creativecommons.org/licenses/by/3.0/"
LICENSE_URL_CC0 = "https://creativecommons.org/publicdomain/zero/1.0/"

# Pasta do autor -> (nome, página, licença). Conforme o license.txt do repositório game-icons/icons.
AUTHORS = {
    "lorc": ("Lorc", "http://lorcblog.blogspot.com", "CC BY 3.0"),
    "delapouite": ("Delapouite", "http://delapouite.com", "CC BY 3.0"),
    "john-colburn": ("John Colburn", "http://ninmunanmu.com", "CC BY 3.0"),
    "felbrigg": ("Felbrigg", "http://blackdogofdoom.blogspot.co.uk", "CC BY 3.0"),
    "john-redman": ("John Redman", "http://www.uniquedicetowers.com", "CC BY 3.0"),
    "carl-olsen": ("Carl Olsen", "https://twitter.com/unstoppableCarl", "CC BY 3.0"),
    "sbed": ("Sbed", "http://opengameart.org/content/95-game-icons", "CC BY 3.0"),
    "priorblue": ("PriorBlue", "https://game-icons.net", "CC BY 3.0"),
    "willdabeast": ("Willdabeast", "http://wjbstories.blogspot.com", "CC BY 3.0"),
    "viscious-speed": ("Viscious Speed", "http://viscious-speed.deviantart.com", "CC0 1.0"),
    "lord-berandas": ("Lord Berandas", "http://berandas.deviantart.com", "CC BY 3.0"),
    "irongamer": ("Irongamer", "http://ecesisllc.wix.com/home", "CC BY 3.0"),
    "heavenly-dog": ("HeavenlyDog", "http://www.gnomosygoblins.blogspot.com", "CC BY 3.0"),
    "lucasms": ("Lucas", "https://game-icons.net", "CC BY 3.0"),
    "faithtoken": ("Faithtoken", "http://fungustoken.deviantart.com", "CC BY 3.0"),
    "skoll": ("Skoll", "https://game-icons.net", "CC BY 3.0"),
    "andymeneely": ("Andy Meneely", "http://www.se.rit.edu/~andy/", "CC BY 3.0"),
    "cathelineau": ("Cathelineau", "https://game-icons.net", "CC BY 3.0"),
    "kier-heyl": ("Kier Heyl", "https://game-icons.net", "CC BY 3.0"),
    "aussiesim": ("Aussiesim", "https://game-icons.net", "CC BY 3.0"),
    "sparker": ("Sparker", "http://citizenparker.com", "CC BY 3.0"),
    "zeromancer": ("Zeromancer", "https://game-icons.net", "CC0 1.0"),
    "rihlsul": ("Rihlsul", "https://game-icons.net", "CC BY 3.0"),
    "quoting": ("Quoting", "https://game-icons.net", "CC BY 3.0"),
    "guard13007": ("Guard13007", "https://guard13007.com", "CC BY 3.0"),
    "darkzaitzev": ("DarkZaitzev", "http://darkzaitzev.deviantart.com", "CC BY 3.0"),
    "spencerdub": ("SpencerDub", "https://game-icons.net", "CC BY 3.0"),
    "generalace135": ("GeneralAce135", "https://game-icons.net", "CC BY 3.0"),
    "zajkonur": ("Zajkonur", "https://game-icons.net", "CC BY 3.0"),
    "catsu": ("Catsu", "https://game-icons.net", "CC BY 3.0"),
    "starseeker": ("Starseeker", "https://game-icons.net", "CC BY 3.0"),
    "pepijn-poolman": ("Pepijn Poolman", "https://game-icons.net", "CC BY 3.0"),
    "pierre-leducq": ("Pierre Leducq", "https://game-icons.net", "CC BY 3.0"),
    "caro-asercion": ("Caro Asercion", "https://game-icons.net", "CC BY 3.0"),
    "seregacthtuf": ("SeregaCthtuf", "https://game-icons.net", "CC BY 3.0"),
}
# Preferência quando o mesmo nome de arquivo existe em mais de um autor.
AUTHOR_PRIORITY = ["lorc", "delapouite", "skoll", "willdabeast", "sbed"]


def normalize(text):
    text = unicodedata.normalize("NFD", text.strip())
    return "".join(c for c in text if not unicodedata.combining(c)).lower()


def kotlin_str(text):
    return '"' + text.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def resource_name(key):
    return "gi_" + key.replace("-", "_")


def index_source(source):
    index = {}
    for svg in sorted(source.glob("*/*.svg")):
        index.setdefault(svg.stem, []).append(svg.parent.name)
    return index


def resolve_author(key, index):
    folders = index.get(key)
    if not folders:
        raise SystemExit(f"Ícone inexistente no repositório de origem: {key}")
    if key in AUTHOR_OVERRIDES:
        folder = AUTHOR_OVERRIDES[key]
    elif len(folders) == 1:
        folder = folders[0]
    else:
        folder = next((a for a in AUTHOR_PRIORITY if a in folders), None)
        if folder is None:
            raise SystemExit(f"Nome ambíguo ({', '.join(folders)}): defina AUTHOR_OVERRIDES['{key}']")
    if folder not in AUTHORS:
        raise SystemExit(f"Autor sem licença conhecida no license.txt: {folder} (ícone {key})")
    return folder


def svg_to_vector(svg_text, key):
    paths = re.findall(r"<path\b[^>]*>", svg_text)
    foreground = [p for p in paths if 'fill="#fff"' in p]
    if len(paths) != 2 or len(foreground) != 1 or 'd="M0 0h512v512H0z"' not in paths[0]:
        raise SystemExit(f"Formato de SVG inesperado em {key}: {len(paths)} paths")
    path_data = re.search(r'\bd="([^"]*)"', foreground[0]).group(1)
    path_data = " ".join(path_data.split())
    xml = (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="48dp"\n'
        '    android:height="48dp"\n'
        '    android:viewportWidth="512"\n'
        '    android:viewportHeight="512">\n'
        '    <path\n'
        '        android:fillColor="#FFFFFFFF"\n'
        f'        android:pathData="{path_data}" />\n'
        '</vector>\n'
    )
    return xml, len(path_data)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--source", required=True, help="clone de github.com/game-icons/icons")
    source = Path(parser.parse_args().source)
    if not (source / "license.txt").exists():
        raise SystemExit(f"{source} não parece ser o repositório game-icons/icons")

    index = index_source(source)
    keys = []
    for group, icons in ICON_GROUPS.items():
        for key, _label, _keywords in icons:
            if key in keys:
                raise SystemExit(f"Chave repetida em ICON_GROUPS: {key}")
            keys.append(key)
    key_set = set(keys)

    # --- catálogo: toda regra aponta para ícone existente e todo item é coberto ---
    for _pattern, key in ITEM_RULES:
        if key not in key_set:
            raise SystemExit(f"ITEM_RULES usa ícone que não está em ICON_GROUPS: {key}")
    compiled = [(re.compile(p), k) for p, k in ITEM_RULES]
    items = json.loads(ITEMS_JSON.read_text(encoding="utf-8"))
    unmatched = []
    for item in items:
        name = normalize(item["name"])
        key = next((k for rx, k in compiled if rx.search(name)), None)
        if key is None:
            unmatched.append(f'{item["name"]} ({item["category"]})')
        else:
            item["icon"] = key
    if unmatched:
        raise SystemExit("Itens sem ícone:\n  " + "\n  ".join(unmatched))
    used_by_catalog = {item["icon"] for item in items}

    # --- converte SVG -> VectorDrawable ---
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    wanted = set()
    author_of = {}
    biggest = (0, "")
    for key in keys:
        folder = resolve_author(key, index)
        author_of[key] = folder
        xml, size = svg_to_vector((source / folder / f"{key}.svg").read_text(encoding="utf-8"), key)
        biggest = max(biggest, (size, key))
        name = resource_name(key) + ".xml"
        wanted.add(name)
        (DRAWABLE_DIR / name).write_text(xml, encoding="utf-8")
    for stale in DRAWABLE_DIR.glob("gi_*.xml"):
        if stale.name not in wanted:
            stale.unlink()

    # --- registro Kotlin ---
    ICONS_KT_DIR.mkdir(parents=True, exist_ok=True)
    lines = [
        "// GERADO por tools/game_icons/gerar.py a partir de tools/game_icons/mapa_icones.py. Não edite à mão.",
        "package com.example.dnd35.ui.icons",
        "",
        "import androidx.annotation.DrawableRes",
        "import com.example.R",
        "",
        "/** Um ícone do game-icons.net incluído no app. [key] é o texto gravado em fichas, backups e catálogo. */",
        "data class GameIcon(",
        "    val key: String,",
        "    @DrawableRes val resId: Int,",
        "    val label: String,",
        "    val keywords: String,",
        "    val group: String,",
        "    val authorName: String",
        ")",
        "",
        "object GameIcons {",
        "    val groups: List<String> = listOf(" + ", ".join(kotlin_str(g) for g in ICON_GROUPS) + ")",
        "",
        "    val all: List<GameIcon> = listOf(",
    ]
    entries = []
    for group, icons in ICON_GROUPS.items():
        for key, label, keywords in icons:
            author = AUTHORS[author_of[key]][0]
            entries.append(
                f"        GameIcon({kotlin_str(key)}, R.drawable.{resource_name(key)}, {kotlin_str(label)}, "
                f"{kotlin_str(keywords)}, {kotlin_str(group)}, {kotlin_str(author)})"
            )
    lines.append(",\n".join(entries))
    lines += [
        "    )",
        "",
        "    private val byKey: Map<String, GameIcon> = all.associateBy { it.key }",
        "",
        "    fun find(key: String?): GameIcon? = key?.let { byKey[it] }",
        "}",
        "",
    ]
    (ICONS_KT_DIR / "GameIcons.kt").write_text("\n".join(lines), encoding="utf-8")

    # --- créditos (só autores dos ícones incluídos) ---
    per_author = {}
    for key in keys:
        per_author.setdefault(author_of[key], []).append(key)
    credit_lines = [
        "// GERADO por tools/game_icons/gerar.py. Não edite à mão.",
        "package com.example.dnd35.ui.icons",
        "",
        "/** Autor de ícones do game-icons.net presentes no app, com a licença exigida por ele. */",
        "data class GameIconAuthor(val name: String, val url: String, val license: String, val iconCount: Int)",
        "",
        "object GameIconCredits {",
        "    const val SITE_URL = \"https://game-icons.net\"",
        f"    const val CC_BY_URL = {kotlin_str(LICENSE_URL_BY)}",
        f"    const val CC0_URL = {kotlin_str(LICENSE_URL_CC0)}",
        "",
        "    val authors: List<GameIconAuthor> = listOf(",
    ]
    author_entries = []
    for folder in sorted(per_author, key=lambda f: (-len(per_author[f]), AUTHORS[f][0].lower())):
        name, url, lic = AUTHORS[folder]
        author_entries.append(
            f"        GameIconAuthor({kotlin_str(name)}, {kotlin_str(url)}, {kotlin_str(lic)}, {len(per_author[folder])})"
        )
    credit_lines.append(",\n".join(author_entries))
    credit_lines += ["    )", "}", ""]
    (ICONS_KT_DIR / "GameIconCredits.kt").write_text("\n".join(credit_lines), encoding="utf-8")

    # --- items.json (mesmo formato do arquivo original) ---
    original = ITEMS_JSON.read_text(encoding="utf-8")
    rendered = json.dumps(items, ensure_ascii=False, indent=2) + ("\n" if original.endswith("\n") else "")
    ITEMS_JSON.write_text(rendered, encoding="utf-8")

    # --- documentação de créditos ---
    md = [
        "# Créditos dos ícones",
        "",
        "Os ícones de itens vêm do [game-icons.net](https://game-icons.net) (repositório "
        "[game-icons/icons](https://github.com/game-icons/icons)). Segundo o `license.txt` do repositório, "
        f"os ícones são distribuídos sob [Creative Commons 3.0 BY]({LICENSE_URL_BY}), exceto os de autores marcados "
        f"como CC0 ([CC0 1.0]({LICENSE_URL_CC0})). A licença pede a menção \"Icons made by {{autor}}\".",
        "",
        "**Alterações feitas pelo app:** os desenhos foram convertidos de SVG para VectorDrawable do Android, "
        "sem o retângulo de fundo preto, e são pintados em tempo de execução com a cor do tema.",
        "",
        "Esta lista e a tela **Créditos e licenças** (menu ⋮ do app) são geradas por `tools/game_icons/gerar.py`.",
        "",
        "## Autores",
        "",
        "| Autor | Licença | Ícones no app |",
        "|---|---|---|",
    ]
    for folder in sorted(per_author, key=lambda f: (-len(per_author[f]), AUTHORS[f][0].lower())):
        name, url, lic = AUTHORS[folder]
        md.append(f"| [{name}]({url}) | {lic} | {len(per_author[folder])} |")
    md += ["", "## Ícone por ícone", "", "| Chave | Rótulo | Grupo | Autor | Arquivo de origem |", "|---|---|---|---|---|"]
    for group, icons in ICON_GROUPS.items():
        for key, label, _keywords in icons:
            folder = author_of[key]
            md.append(f"| `{key}` | {label} | {group} | {AUTHORS[folder][0]} | `{folder}/{key}.svg` |")
    md.append("")
    CREDITS_MD.write_text("\n".join(md), encoding="utf-8")

    print(f"{len(keys)} ícones ({len(used_by_catalog)} usados pelo catálogo), {len(per_author)} autores")
    print(f"maior pathData: {biggest[0]} caracteres ({biggest[1]})")
    dup = {k: index[k] for k in keys if len(index[k]) > 1}
    if dup:
        print("nomes presentes em mais de um autor (resolvidos por prioridade):", dup)


if __name__ == "__main__":
    main()
