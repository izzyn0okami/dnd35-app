import glob
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
    for case in root.iter("testcase"):
        for tag in ("failure", "error"):
            node = case.find(tag)
            if node is not None:
                found = True
                classname = case.get("classname")
                name = case.get("name")
                message = (node.get("message") or "").replace("\n", " ")[:500]
                print(f"::error::{classname}.{name}: {message}")
                text = (node.text or "").strip()
                if text:
                    for chunk_start in range(0, min(len(text), 4000), 900):
                        print(f"::error::{classname}.{name} stack[{chunk_start}]: {text[chunk_start:chunk_start+900]}")
if not found:
    print("Nenhuma falha de teste encontrada nos XMLs JUnit.")
