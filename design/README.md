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

The format is **W3C Design Tokens (DTCG)** (`$type` / `$value`), which Tokens
Studio reads and writes directly.

---

## Figma workflow (Tokens Studio)

1. In Figma, install the **Tokens Studio for Figma** plugin.
2. **Import** `design/tokens.json` (plugin → Settings → Import → load the file).
   The token sets (color / space / radius / font / typography) appear in the plugin.
3. Design with those tokens — use them in styles and components, never raw hex.
4. When tokens change in Figma, **Export** from Tokens Studio back to
   `design/tokens.json`, then run the generator (below) and commit both files.

Either side can lead; git is the authority. If Figma and the repo disagree, the
repo wins until Figma is exported back.

---

## Code workflow

```
python3 design/generate_kotlin.py
```

Writes `app/src/main/java/com/example/ui/theme/DevTokens.kt`:

- `DevColorTokens` — one `val` per colour (`Color(0xFF…)`)
- `DevSpacingTokens` / `DevRadiusTokens` — `Dp` values
- `DevFontTokens` — font-family names
- `DevTypeTokens` — size / line-height / weight per style

Then commit `tokens.json` **and** `DevTokens.kt` together.

---

## CI

`.github/workflows/tokens-ci.yml` regenerates the Kotlin from `tokens.json` and
fails the PR if the committed `DevTokens.kt` differs — so the code can never
drift from the design source.

---

## Migration path (the real payoff)

`DevTokens.kt` is the destination. Today, 797 hardcoded `Color(0xFF…)` literals
sit across 23 screens and bypass the design system. The plan:

1. Point `DevbhashaTheme.kt` at `DevColorTokens` (theme = generated tokens).
2. Migrate screens screen-by-screen: `Color(0xFF737373)` → `DevColorTokens.textSecondary`.
   One PR per screen, easy to review.
3. Add a lint/CI guard that rejects new hardcoded colours, so it can't regress.

That turns "797 colours scattered" into "one palette, one file, both tools in sync".
