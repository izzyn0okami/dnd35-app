#!/usr/bin/env python3
"""Diagnóstico temporário de CI: imprime falhas de teste de um XML JUnit como
anotações ::error:: do GitHub Actions. Removido junto com o passo que o chama
assim que o pipeline voltar a ficar verde."""
import re
import sys

text = open(sys.argv[1], encoding="utf-8").read()
pattern = r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*>\s*<failure message="([^"]*)"'
for name, classname, msg in re.findall(pattern, text):
    msg = msg.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print(f"::error::{classname}.{name}: {msg}")
