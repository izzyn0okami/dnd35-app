#!/usr/bin/env python3
"""Acrescenta ao creatures.json as fichas de animais/vermes do SRD 3.5 (OGL) que o parser do
Livro dos Monstros não conseguiu ler. Uso: gen_srd_animais.py <dados.txt> <creatures.json>
Pula ids que já existem. Todo registro passa pela mesma validação numérica do parser."""
import json, re, sys

SIZE = {'Fine': 'Mínimo', 'Diminutive': 'Diminuto', 'Tiny': 'Miúdo', 'Small': 'Pequeno', 'Medium': 'Médio',
        'Large': 'Grande', 'Huge': 'Enorme', 'Gargantuan': 'Imenso', 'Colossal': 'Colossal'}
TYPE = {'Animal': 'Animal', 'Vermin': 'Inseto'}
NAME = {'hiena': 'Hiena', 'lagarto-monitor': 'Lagarto-Monitor', 'cavalo-de-guerra-leve': 'Cavalo de Guerra Leve'}
GEN = {'tubarao': ('Tubarão', 'm'), 'vibora': ('Víbora', 'f'), 'escorpiao': ('Escorpião', 'm'),
       'aranha': ('Aranha', 'f'), 'centopeia': ('Centopeia', 'f')}
SZN = {'miudo': 'Miúdo', 'miuda': 'Miúda', 'pequeno': 'Pequeno', 'pequena': 'Pequena', 'medio': 'Médio',
       'media': 'Média', 'grande': 'Grande', 'enorme': 'Enorme', 'imenso': 'Imenso', 'imensa': 'Imensa',
       'colossal': 'Colossal'}
SKILL = {'Balance': 'Equilíbrio', 'Climb': 'Escalar', 'Hide': 'Esconder-se', 'Listen': 'Ouvir', 'Spot': 'Observar',
         'Swim': 'Natação', 'Jump': 'Saltar', 'Move Silently': 'Furtividade'}
FEAT = {'Alertness': 'Prontidão', 'Weapon Finesse': 'Acuidade com Arma', 'Endurance': 'Tolerância', 'Run': 'Corrida',
        'Great Fortitude': 'Fortitude Maior', 'Improved Initiative': 'Iniciativa Aprimorada',
        'Iron Will': 'Vontade de Ferro', 'Weapon Focus (bite)': 'Foco em Arma (mordida)'}
SQ = [('Low-light vision', 'Visão na penumbra'), ('Darkvision 60 ft.', 'Visão no escuro 18 m'),
      ('tremorsense 60 ft.', 'sentido sísmico 18 m'), ('vermin traits', 'características de inseto'),
      ('Blindsense', 'Percepção às cegas'), ('keen scent', 'faro apurado'), ('scent', 'faro'), ('Scent', 'Faro'),
      ('Poison', 'Veneno'), ('Constrict', 'Constrição'), ('improved grab', 'agarrar aprimorado'),
      ('poison', 'veneno'), ('web', 'teia'), ('Trip', 'Imobilização')]
ENV = {'Warm deserts': 'Desertos quentes', 'Warm forests': 'Florestas quentes', 'Cold aquatic': 'Aquático frio',
       'Temperate plains': 'Planícies temperadas', 'Temperate marshes': 'Pântanos temperados',
       'Temperate forests': 'Florestas temperadas', 'Underground': 'Subterrâneo'}
ORG = [('Solitary', 'Solitário'), ('Domesticated', 'Domesticado'), ('pair', 'casal'), ('pack', 'matilha'),
       ('school', 'cardume'), ('colony', 'colônia'), ('Colony', 'Colônia'), ('swarm', 'enxame'), (' or ', ' ou ')]


def n(s):
    return int(s.replace('−', '-').replace('–', '-'))


def ft_m(ft):
    ft = ft.replace('2-1/2', '2.5')
    v = float(ft) * 0.3
    return ('%g' % round(v, 2)).replace('.', ',')


def speed(s):
    s = re.sub(r'(\d+) ft\.', lambda m: ft_m(m.group(1)) + ' m', s)
    s = re.sub(r'\((\d+) squares\)', r'(\1 quadrados)', s)
    return s.replace('climb', 'escalada').replace('swim', 'natação').replace('Swim', 'Natação')


def reach(s):
    s = s.replace('2-1/2 ft.', '2.5 ft.')
    parts = [p.strip() for p in s.split('/')]
    out = []
    for p in parts:
        v = float(p.replace(' ft.', ''))
        out.append(('0,75 m' if abs(v - 2.5) < 1e-9 else ('%s m' % ft_m(str(v).rstrip('0').rstrip('.')))))
    return '/'.join(out)


def tr_attack(s):
    s = s.replace('Bite', 'mordida').replace('Claw', 'garra').replace('Hoof', 'casco').replace('2 claws', '2 garras')
    s = s.replace('2 hooves', '2 cascos').replace('sting', 'ferrão').replace('bite', 'mordida').replace('Arms', 'braços')
    s = s.replace(' melee ', ' ').replace(' and ', ' e ').replace(' plus poison', ' mais veneno')
    s = re.sub(r'\((.*?)\)', r'(dano: \1)', s)
    return 'Corpo a corpo: ' + s


def tr_list(s, table):
    out = []
    for item in [x.strip() for x in s.split(',')]:
        m = re.match(r'(.*?)(B?)( \(.*)?$', item)
        base, b, tail = item, '', ''
        if item.endswith('B') and item[:-1] in table:
            base, b = item[:-1], 'B'
        out.append(table.get(base, base) + b)
    return ', '.join(out)


def tr_skills(s):
    out = []
    for it in s.split(','):
        it = it.strip()
        m = re.match(r'(.*?) ([+-]\d+\*?)$', it)
        out.append('%s %s' % (SKILL[m.group(1)], m.group(2)))
    return ', '.join(out)


def tr_text(s, pairs):
    for a, b in pairs:
        s = s.replace(a, b)
    return s


def tr_adv(a):
    if a == '—':
        return a
    a = a.replace(' HD', ' DV')
    for en, pt in SIZE.items():
        a = a.replace('(%s)' % en, '(%s)' % pt)
    return a


def parse_row(line):
    f = line.rstrip('\n').split('|')
    assert len(f) == 21, (len(f), f[0])
    (key, st, hd, init, spd, ac, bag, atk, full, sr, sa, sq, sv, ab, sk, ft, env, org, cr, adv, la) = f
    size_en, kind = st.split(' ')[0], st.split(' ')[1]
    m = re.match(r'(\d+)(?:/(\d+))? ?d(\d+)([+-]\d+)? \((\d+) hp\)', hd)
    num, den, die, bonus, hp = m.groups()
    count = (int(num) / int(den)) if den else int(num)
    hd_txt = ('%s/%sd%s' % (num, den, die) if den else '%sd%s' % (num, die)) + (bonus or '')
    base, grapple = bag.split('/')
    acm = re.match(r'(\d+) \((.*?)\), touch (\d+), flat-footed (\d+)', ac)
    sizeTxt = SIZE[size_en]
    detail = sizeTxt + (' (Aquático)' if 'Aquatic' in st else '')
    parts = key.split('-')
    if key in NAME:
        name = NAME[key]
    else:
        base_name, g = GEN[parts[0]]
        name = '%s %s' % (base_name, SZN[parts[1]])
    abil = dict(re.findall(r'(Str|Dex|Con|Int|Wis|Cha) (\d+|—)', ab))
    abil = {k.lower(): (None if v == '—' else int(v)) for k, v in abil.items()}
    cr_txt = cr
    rec = {
        'id': key, 'name': name, 'type': TYPE[kind], 'size': sizeTxt, 'typeDetail': detail,
        'hitDice': hd_txt, 'hdCount': float(count), 'hdType': int(die), 'hitPoints': int(hp),
        'initiative': n(init), 'speed': speed(spd), 'armorClass': int(acm.group(1)),
        'armorClassText': tr_text(acm.group(0), [('Dex', 'Des'), ('natural', 'natural'), ('touch', 'toque'), ('flat-footed', 'surpresa'), (' size', ' tamanho')]),
        'touchAc': int(acm.group(3)), 'flatFootedAc': int(acm.group(4)), 'baseAttack': n(base),
        'grapple': n(grapple.replace('*', '')), 'attack': tr_attack(atk), 'fullAttack': tr_attack(full),
        'spaceReach': reach(sr), 'specialAttacks': '—' if sa == '—' else tr_text(sa, SQ),
        'specialQualities': tr_text(sq, SQ),
        'saves': dict(zip(('fort', 'ref', 'will'), map(int, re.findall(r'[+-]\d+', sv.replace('−', '-'))))),
        'abilities': {k: abil[k] for k in ('str', 'dex', 'con', 'int', 'wis', 'cha')},
        'skills': tr_skills(sk), 'feats': '—' if ft == '—' else tr_list(ft, FEAT),
        'environment': ENV[env], 'organization': tr_text(org, ORG), 'challengeRating': cr_txt,
        'treasure': '', 'alignment': '', 'advancement': tr_adv(adv),
        'levelAdjustment': la, 'source': 'SRD 3.5 (OGL), traduzido',
    }
    return rec


def validate(r):
    avg = r['hdCount'] * (r['hdType'] + 1) / 2
    bonus = int(re.search(r'([+-]\d+)$', r['hitDice']).group(1)) if re.search(r'[+-]\d+$', r['hitDice']) else 0
    expected = int(avg + bonus)
    assert abs(expected - r['hitPoints']) <= 1, (r['id'], expected, r['hitPoints'])
    s = r['saves']
    assert len(s) == 3
    assert len(r['abilities']) == 6


def main():
    src, dst = sys.argv[1], sys.argv[2]
    data = json.load(open(dst, encoding='utf-8'))
    ids = {c['id'] for c in data}
    added = []
    for line in open(src, encoding='utf-8'):
        if not line.strip() or line.startswith('#') or line.startswith('# key'):
            continue
        r = parse_row(line)
        validate(r)
        if r['id'] in ids:
            print('já existe, pulado:', r['id'])
            continue
        data.append(r)
        added.append(r['id'])
    json.dump(data, open(dst, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    print(len(added), 'adicionadas; total', len(data))


main()
