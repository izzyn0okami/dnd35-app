import glob
import re
import sys
import xml.etree.ElementTree as ET

files = glob.glob("app/build/test-results/**/*.xml", recursive=True)
found = False
for f in files:
    try:
        tree = ET.parse(f)
    except ET.ParseError:
        continue
    root = tree.getroot()
    for testcase in root.iter("testcase"):
        for tag in ("failure", "error"):
            node = testcase.find(tag)
            if node is not None:
                found = True
                name = testcase.get("name")
                classname = testcase.get("classname")
                print(f"=== FAILED: {classname}.{name} ===")
                text = node.get("message", "") or ""
                full = node.text or ""
                print(text)
                # Only print lines from our own package to keep output short.
                for line in full.splitlines():
                    if "com.example" in line or "Exception" in line or "Error" in line:
                        print(line)
                print()
if not found:
    print("Nenhuma falha encontrada nos relatorios XML (ou nenhum relatorio gerado).")
