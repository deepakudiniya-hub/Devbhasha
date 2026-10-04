#!/usr/bin/env python3
"""
Generate Kotlin design-token objects from design/tokens.json.

design/tokens.json is the single source of truth (shared with Figma via
Tokens Studio). This script turns it into:

    app/src/main/java/com/example/ui/theme/DevTokens.kt

Run it after any token change:

    python3 design/generate_kotlin.py

CI fails if the committed DevTokens.kt is out of sync (see tokens-ci.yml).
"""

import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
TOKENS = os.path.join(HERE, "tokens.json")
OUT = os.path.join(
    ROOT, "app", "src", "main", "java", "com", "example", "ui", "theme", "DevTokens.kt"
)

WEIGHT = {100: "Thin", 200: "ExtraLight", 300: "Light", 400: "Normal",
          500: "Medium", 600: "SemiBold", 700: "Bold", 800: "ExtraBold", 900: "Black"}


def camel(parts):
    head, *rest = parts
    return head + "".join(p[:1].upper() + p[1:] for p in rest)


def hex_to_kotlin(value):
    v = value.lstrip("#")
    if len(v) == 6:
        v = "FF" + v  # add opaque alpha
    return "Color(0x%s)" % v.upper()


def dp_to_kotlin(value):
    n = re.sub(r"[^0-9.]", "", str(value))
    return "%s.dp" % n


def sp_to_kotlin(value):
    n = re.sub(r"[^0-9.]", "", str(value))
    return "%s.sp" % n


def walk(node, path, leaf):
    if isinstance(node, dict) and "$value" in node:
        leaf(node, path)
        return
    if isinstance(node, dict):
        for key, child in node.items():
            if key.startswith("$"):
                continue
            walk(child, path + [key], leaf)


def main():
    with open(TOKENS, "r", encoding="utf-8") as fh:
        data = json.load(fh)

    colors, dims, fonts, typos = [], [], [], []

    def collect(node, path):
        t = node.get("$type")
        v = node.get("$value")
        if t == "color":
            colors.append((camel(path), hex_to_kotlin(v)))
        elif t == "dimension":
            dims.append((camel(path), dp_to_kotlin(v)))
        elif t == "fontFamily":
            fonts.append((camel(path), v))
        elif t == "typography":
            name = camel(path)
            typos.append((
                name,
                sp_to_kotlin(v["fontSize"]),
                sp_to_kotlin(v["lineHeight"]),
                WEIGHT.get(int(v.get("fontWeight", 400)), "Normal"),
            ))

    for group in ("color", "space", "radius", "font", "typography"):
        if group in data:
            walk(data[group], [group] if group in ("space", "radius") else [], collect)

    lines = []
    lines.append("package com.example.ui.theme")
    lines.append("")
    lines.append("import androidx.compose.ui.graphics.Color")
    lines.append("import androidx.compose.ui.text.font.FontWeight")
    lines.append("import androidx.compose.ui.unit.dp")
    lines.append("import androidx.compose.ui.unit.sp")
    lines.append("")
    lines.append("// GENERATED FILE — DO NOT EDIT BY HAND.")
    lines.append("// Source: design/tokens.json  ·  Regenerate: python3 design/generate_kotlin.py")
    lines.append("")
    lines.append("object DevColorTokens {")
    for name, val in colors:
        lines.append("    val %s = %s" % (name, val))
    lines.append("}")
    lines.append("")
    lines.append("object DevSpacingTokens {")
    for name, val in dims:
        if name.startswith("space"):
            lines.append("    val %s = %s" % (name[len("space"):][:1].lower() + name[len("space"):][1:], val))
    lines.append("}")
    lines.append("")
    lines.append("object DevRadiusTokens {")
    for name, val in dims:
        if name.startswith("radius"):
            lines.append("    val %s = %s" % (name[len("radius"):][:1].lower() + name[len("radius"):][1:], val))
    lines.append("}")
    lines.append("")
    lines.append("object DevFontTokens {")
    for name, val in fonts:
        lines.append('    const val %s = "%s"' % (name, val))
    lines.append("}")
    lines.append("")
    lines.append("object DevTypeTokens {")
    for name, size, lh, weight in typos:
        lines.append("    val %sSize = %s" % (name, size))
        lines.append("    val %sLineHeight = %s" % (name, lh))
        lines.append("    val %sWeight = FontWeight.%s" % (name, weight))
    lines.append("}")
    lines.append("")

    out = "\n".join(lines)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as fh:
        fh.write(out)
    print("Wrote %s (%d colors, %d dims, %d fonts, %d type styles)"
          % (os.path.relpath(OUT, ROOT), len(colors), len(dims), len(fonts), len(typos)))


if __name__ == "__main__":
    sys.exit(main())
