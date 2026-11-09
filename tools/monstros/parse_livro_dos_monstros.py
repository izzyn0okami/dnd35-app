#!/usr/bin/env python3
"""Extrai as fichas (blocos de estatísticas) do texto do Livro dos Monstros 3.5 em JSON.

Uso: parse_livro_dos_monstros.py <livro.txt> <saida.json> <rejeitadas.json>

Só entram fichas que passam em todas as validações (PV compatível com os dados de vida,
seis atributos, três testes de resistência, ND e CA numéricos). O resto vai para o arquivo
de rejeitadas, com o motivo, para tratamento manual. A prosa de cada criatura (descrição,
táticas) não é extraída: só o bloco de estatísticas.
"""
import json, re, sys, unicodedata

LABELS = ['Dados de Vida', 'Iniciativa', 'Deslocamento', 'Classe de Armadura', 'CA', 'Ataque Base/Agarrar',
          'Ataque Total', 'Ataque', 'Espaço/Alcance', 'Ataques Especiais', 'Qualidades Especiais',
          'Testes de Resistência', 'Habilidades', 'Perícias', 'Talentos', 'Ambiente', 'Organização',
          'Nível de Desafio', 'Tesouro', 'Tendência', 'Progressão', 'Ajuste de Nível']
LABEL_RE = re.compile(r'^(' + '|'.join(re.escape(l) for l in LABELS) + r'):\s*(.*)$')
TYPES = {'aberração': 'Aberração', 'animal': 'Animal', 'besta mágica': 'Besta Mágica', 'constructo': 'Construto',
         'construto': 'Construto', 'dragão': 'Dragão', 'elemental': 'Elemental', 'extra–planar': 'Extraplanar',
         'extraplanar': 'Extraplanar', 'fada': 'Fada', 'gigante': 'Gigante', 'humanoide': 'Humanoide',
         'humanoide monstruoso': 'Humanoide Monstruoso', 'inseto': 'Inseto', 'limo': 'Limo',
         'morto–vivo': 'Morto-vivo', 'planta': 'Planta', 'verme': 'Verme'}
SIZES = ['Minúsculo', 'Miúdo', 'Pequeno', 'Médio', 'Grande', 'Enorme', 'Imenso', 'Colossal', 'Diminuto', 'Mínimo']
NAME_LINE = re.compile(r'^[A-ZÀ-Ú][A-ZÀ-Ú \-–,()]{3,}$')
TYPE_RE = re.compile(r'^([A-Za-zÀ-ú–\- ]+?) \((.+)\)$')

def fix_ligatures(s):
    s = re.sub(r'(?<=\w)(ffi|ffl|fi|fl|ff) (?=[a-zà-ú])', r'\1', s)   # "Refl exos" -> "Reflexos"
    s = re.sub(r'(?<=\s)(ffi|ffl|fi|fl|ff) (?=[a-zà-ú])', r'\1', s)   # "fi rmemente"
    s = re.sub(r'(?<=^)(ffi|ffl|fi|fl|ff) (?=[a-zà-ú])', r'\1', s)
    return s

def clean(s):
    s = fix_ligatures(re.sub(r'\s+', ' ', s).strip())
    return s.replace('–', '–')

def to_int(s):
    s = s.replace('–', '-').replace('−', '-').replace(' ', '')
    return int(s)

def title_name(n):
    m = re.match(r'^(\w+) \((\w+)\)$', n)          # "Constritora (Cobra)" -> "Cobra Constritora"
    if m and n != n.upper():
        n = f'{m.group(2)} {m.group(1)}'
    small = {'de', 'da', 'do', 'das', 'dos', 'e', 'a', 'o'}
    words = n.lower().split(' ')
    return ' '.join(w if (i > 0 and w in small) else w[:1].upper() + w[1:] for i, w in enumerate(words))

def slug(n):
    s = unicodedata.normalize('NFD', n.lower()).encode('ascii', 'ignore').decode()
    return re.sub(r'[^a-z0-9]+', '-', s).strip('-')

NOISE = re.compile(r'^(\d+\s*)?CAPÍTULO \d+.*$|^CAPÍTULO \d+.*\d+$|^\d{1,3}$')

def explode(lines):
    """Remove cabeçalhos de página e separa rótulos grudados na mesma linha ("... escalada 9 m CA; 13 ...")."""
    out = []
    for l in lines:
        if NOISE.match(l.strip()):
            continue
        m = re.match(r'^(.*\S)\s+(CA|Ataque Base/Agarrar|Ataque Total|Espaço/Alcance|Ataques Especiais|Qualidades Especiais|Testes de Resistência|Habilidades|Perícias|Talentos)[:;]\s*(.*)$', l)
        if m and (m.group(2) == 'CA'):
            out.extend([m.group(1), 'CA: ' + m.group(3)])
        else:
            out.append(l)
    return out

def parse_block(lines):
    fields, cur = {}, None
    for l in explode(lines):
        l = fix_ligatures(l.rstrip())
        m = LABEL_RE.match(l)
        if m and m.group(1) not in fields:
            cur = m.group(1); fields[cur] = m.group(2)
        elif cur is not None and l.strip():
            fields[cur] += ' ' + l.strip()
        if cur == 'Ajuste de Nível' and fields[cur].strip():
            break
    return {k: clean(v) for k, v in fields.items()}

def main(src, out, rej):
    L = open(src, encoding='utf-8').read().split('\n')
    starts = [i for i, l in enumerate(L) if l.startswith('Dados de Vida:')]
    ok, bad, seen = [], [], set()
    for n, i in enumerate(starts):
        end = starts[n + 1] - 2 if n + 1 < len(starts) else len(L)
        type_raw = L[i - 1].strip()
        name_raw, j = L[i - 2].strip(), i - 3
        while j > 0 and name_raw == name_raw.upper() and NAME_LINE.match(L[j].strip()) and len(name_raw) < 40:
            name_raw, j = L[j].strip() + ' ' + name_raw, j - 1
        why = None
        fields = parse_block(L[i:end])
        tm = TYPE_RE.match(type_raw)
        if not tm or tm.group(1).lower() not in TYPES:
            why = 'linha de tipo irregular'
        elif not re.search(r'[A-Za-zÀ-ú]{4}', name_raw) or name_raw.startswith('(') or len(name_raw) > 50 or len(name_raw.split()) > 6 \
                or name_raw[-1] in '.:;,' or not name_raw[0].isupper():
            why = 'nome irregular'
        if not why:
            try:
                dv = fields['Dados de Vida']
                if dv.count('PV') != 1:
                    raise ValueError('mais de uma variante (dados de vida)')
                m = re.match(r'^(\d+/\d+|\d+)d(\d+)\s*([+\-–−]\s*\d+)?\s*\((\d+) PV\)', dv)
                if not m: raise ValueError('dados de vida ilegíveis')
                if '/' in m.group(1):
                    a, b = m.group(1).split('/'); cnt = int(a) / int(b)
                else:
                    cnt = int(m.group(1))
                die, mod, hp = int(m.group(2)), to_int(m.group(3) or '0'), int(m.group(4))
                avg = int(cnt * (die + 1) / 2 + mod)
                if abs(avg - hp) > 1: raise ValueError(f'PV {hp} incompatível com {cnt}d{die}{mod:+d} (média {avg})')
                ab = re.findall(r'(For|Des|Con|Int|Sab|Car)\s+(—|-|\d+)', fields['Habilidades'])
                if [a for a, _ in ab] != ['For', 'Des', 'Con', 'Int', 'Sab', 'Car']: raise ValueError('habilidades ilegíveis')
                abil = {a: (None if v in '—-' else int(v)) for a, v in ab}
                sv = re.findall(r'(Fort|Ref|Von)\s*([+\-–−]\s*\d+)', fields['Testes de Resistência'])
                if [a for a, _ in sv] != ['Fort', 'Ref', 'Von']: raise ValueError('testes de resistência ilegíveis')
                saves = {a: to_int(v) for a, v in sv}
                ca = fields.get('Classe de Armadura') or fields.get('CA') or ''
                cm = re.match(r'^(\d+) \(?.*?toque (\d+),? surpresa (\d+)', ca)
                if not cm: raise ValueError('CA ilegível')
                cr = fields.get('Nível de Desafio', '')
                cr_full = cr
                cr = re.sub(r'(\d) /(\d)', r'\1/\2', cr).split(' ')[0]
                if not re.match(r'^(\d+|1/\d+)$', cr): raise ValueError(f'ND ilegível ({cr_full!r})')
                init = re.match(r'^([+\-–−]\s*\d+|\d+)', fields.get('Iniciativa', ''))
                if not init: raise ValueError('iniciativa ilegível')
                if len(set(fields)) < 15: raise ValueError('campos faltando')
                bb = re.match(r'^([+\-–−]\s*\d+|—)\s*/\s*([+\-–−]\s*\d+|—)', fields.get('Ataque Base/Agarrar', ''))
                if not bb: raise ValueError('BBA/agarrar ilegível')
                size = re.split(r'[ ,—]', tm.group(2).strip())[0].capitalize()
                size = {'Média': 'Médio', 'Pequena': 'Pequeno', 'Miúda': 'Miúdo', 'Minúscula': 'Minúsculo'}.get(size, size)
                if size not in SIZES: raise ValueError(f'tamanho ilegível ({tm.group(2)!r})')
                name = title_name(name_raw)
                if slug(name) in seen: raise ValueError('nome duplicado (variante)')
                seen.add(slug(name))
                g = lambda k: fields.get(k, '')   # campo ausente no bloco extraído = '' (diferente do '—' impresso no livro)
                ok.append({
                    'id': slug(name), 'name': name, 'type': TYPES[tm.group(1).lower()], 'size': size,
                    'typeDetail': tm.group(2), 'hitDice': f'{m.group(1)}d{die}' + (f'{mod:+d}' if mod else ''),
                    'hdCount': cnt, 'hdType': die, 'hitPoints': hp, 'initiative': to_int(init.group(1)),
                    'speed': g('Deslocamento'), 'armorClass': int(cm.group(1)), 'armorClassText': ca,
                    'touchAc': int(cm.group(2)), 'flatFootedAc': int(cm.group(3)),
                    'baseAttack': to_int(bb.group(1)) if bb.group(1) != '—' else 0,
                    'grapple': to_int(bb.group(2)) if bb.group(2) != '—' else 0,
                    'attack': g('Ataque'), 'fullAttack': g('Ataque Total'), 'spaceReach': g('Espaço/Alcance'),
                    'specialAttacks': g('Ataques Especiais'), 'specialQualities': g('Qualidades Especiais'),
                    'saves': {'fort': saves['Fort'], 'ref': saves['Ref'], 'will': saves['Von']},
                    'abilities': {'str': abil['For'], 'dex': abil['Des'], 'con': abil['Con'],
                                  'int': abil['Int'], 'wis': abil['Sab'], 'cha': abil['Car']},
                    'skills': g('Perícias'), 'feats': g('Talentos'), 'environment': g('Ambiente'),
                    'organization': g('Organização'), 'challengeRating': cr, 'treasure': g('Tesouro'),
                    'alignment': g('Tendência'), 'advancement': g('Progressão'), 'levelAdjustment': g('Ajuste de Nível'),
                    'source': 'Livro dos Monstros 3.5'})
            except (ValueError, KeyError) as e:
                why = str(e)
        if why:
            bad.append({'linha': i + 1, 'nome': name_raw, 'tipo': type_raw, 'motivo': why})
    json.dump(ok, open(out, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
    json.dump(bad, open(rej, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
    print(f'{len(starts)} blocos; {len(ok)} aceitos; {len(bad)} rejeitados')

if __name__ == '__main__':
    main(*sys.argv[1:4])
