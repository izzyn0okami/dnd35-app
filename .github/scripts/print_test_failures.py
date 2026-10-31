#!/usr/bin/env python3
"""Imprime as falhas de testes JUnit (stacktrace) a partir dos relatórios XML do Gradle.
Script de diagnóstico temporário — não faz parte do pipeline permanente."""
import glob
import xml.etree.ElementTree as ET

report_files = glob.glob("app/build/test-results/**/*.xml", recursive=True)
found_failure = False
for path in report_files:
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        continue
    root = tree.getroot()
    for testcase in root.iter("testcase"):
        for tag in ("failure", "error"):
            node = testcase.find(tag)
            if node is not None:
                found_failure = True
                classname = testcase.get("classname", "?")
                name = testcase.get("name", "?")
                print(f"=== FALHA: {classname}.{name} ===")
                print(node.get("message", ""))
                print(node.text or "")
                print()
if not found_failure:
    print("Nenhuma falha de teste encontrada nos relatórios XML.")
