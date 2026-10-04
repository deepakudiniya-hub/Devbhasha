# Devbhasha — Design Tokens

One source of truth for design, shared by **Figma** and the **Android app**.

```
design/tokens.json          ← the source of truth (edit here)
        │
        ├──► Figma (Tokens Studio plugin)   — designers edit / preview here
        │
        └──► design/generate_kotlin.py  ──►  app/…/ui/theme/DevTokens.kt  (generated, committed)
```

**Rule:** edit `tokens.json`, regenerate the Kotlin, commit both. Never hand-edit
`DevTokens.kt` — CI will fail if it drifts.

---

## The tokens

- **Color** — pure white canvas + black text + a single Saffron accent `#FF6B00`
  (CTA only), grey secondary, light borders, `#16A34A` success.
- **Space** — 8px-based scale: 4 / 8 / 12 / 16 / 20 / 24 / 32.
- **Radius** — 4 / 8 / 24 (card) / 999 (pill).
- **Font** — Serif headings, Noto Sans Devanagari for Hindi-first copy.
- **Typography** — H1 32/40, H2 24/32, H3 18/24, Body 14/20, Caption 12/16.

Format: **W3C Design Tokens (DTCG)** (`$type` / `$value`), which Tokens Studio
reads and writes directly.

---

## Figma workflow (Tokens Studio)

1. In Figma, install the **Tokens Studio for Figma** plugin.
2. **Import** `design/tokens.json` (plugin → Settings → Import → load the file).
3. Design with those tokens — never raw hex.
4. On change, **Export** from Tokens Studio back to `design/tokens.json`, run
   the generator below, and commit both files.

Git is the authority. If Figma and the repo disagree, the repo wins until Figma
is exported back.

---

## Code workflow

```
python3 design/generate_kotlin.py
```

Writes `app/src/main/java/com/example/ui/theme/DevTokens.kt`:

- `DevColorTokens` — one `val` per colour
- `DevSpacingTokens` / `DevRadiusTokens` — `Dp` values
- `DevFontTokens` — font-family names
- `DevTypeTokens` — size / line-height / weight per style

Commit `tokens.json` **and** `DevTokens.kt` together.

---

## CI

`.github/workflows/tokens-ci.yml` regenerates the Kotlin from `tokens.json` and
fails the PR if the committed `DevTokens.kt` differs — the code can never drift
from the design source.

---

## Migration status

- [x] Token source + generator + CI.
- [x] `DevbhashaTheme.kt` now reads from `DevColorTokens` (theme = tokens).
- [ ] Screens migrated screen-by-screen: `Color(0xFF737373)` → `DevColorTokens.textSecondary`.
      One PR per screen, easy to review.
- [ ] CI guard that rejects new hardcoded colours.

797 hardcoded literals across 23 screens are the backlog. Each screen becomes
"one palette, one file, both tools in sync".
