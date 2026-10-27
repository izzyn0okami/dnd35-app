import re
import sys

def main(path):
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()
    classname_match = re.search(r'classname="([^"]*)"', content)
    classname = classname_match.group(1) if classname_match else path
    for m in re.finditer(
        r'<testcase[^>]*name="([^"]*)"[^>]*>.*?<failure[^>]*message="([^"]*)"',
        content,
        re.S,
    ):
        name, msg = m.group(1), m.group(2)
        msg = msg.replace("%", "%25").replace("\r", "").replace("\n", "%0A")
        print(f"::error::{classname}.{name}: {msg}")


if __name__ == "__main__":
    main(sys.argv[1])
