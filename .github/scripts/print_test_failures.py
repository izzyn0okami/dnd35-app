import glob
import subprocess
import xml.etree.ElementTree as ET

lines = []
for path in sorted(glob.glob("app/build/test-results/**/*.xml", recursive=True)):
    tree = ET.parse(path)
    root = tree.getroot()
    for case in root.iter("testcase"):
        for tag in ("failure", "error"):
            node = case.find(tag)
            if node is not None:
                lines.append(f"### {case.get('classname')} > {case.get('name')}")
                lines.append("```")
                lines.append((node.get("message") or "") + "\n" + (node.text or ""))
                lines.append("```")

body = "\n".join(lines) if lines else "No failure details found in XML reports."
# PRs comment body size limits are generous; truncate just in case.
body = body[:60000]

with open("/tmp/test_failures.md", "w") as f:
    f.write(body)

pr_number = subprocess.check_output(
    ["gh", "pr", "view", "--json", "number", "-q", ".number"]
).decode().strip()

subprocess.run(["gh", "pr", "comment", pr_number, "--body-file", "/tmp/test_failures.md"], check=True)
