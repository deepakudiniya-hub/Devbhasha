# Devbhasha design tokens

`design/tokens.json` is the shared palette and dimension source. Import it into
Tokens Studio for Figma. Export changes back to this file, then regenerate Kotlin:

```sh
python3 design/generate_kotlin.py
```

Commit both the source and `app/src/main/java/com/example/ui/theme/DevTokens.kt`.
Never edit the generated Kotlin manually. The Design Tokens CI workflow
regenerates it and rejects drift.

## Current palette

- Current main-branch brand: teal `#0D656C`, deep teal `#125157`, gold `#D0BF75`,
  cream `#FAF6EA` (`DevColorTokens.brand*`).
- The `home` group preserves the existing HomeV2 palette exactly.
- Legacy white/black/saffron, slate, status and decorative accent tokens remain
  available for exact-value migrations. Their presence does not make saffron the
  current brand CTA colour.
- Theme aliases retain their existing names and values, including light/dark
  schemes. Dynamic Material colours still follow the existing runtime setting.

The source also includes spacing, radius, font-family names and typography.
`DevSpacing` and `DevRadius` delegate to generated values. Theme shape tokens are
separate because the current theme uses 16dp cards and 9999dp pills while shared
components use 24dp cards and 999dp pills; this migration preserves both.
Font-family names are metadata; they do not load Android font resources.
Existing runtime typography and fonts are unchanged.

## Migration status

This continues the token source introduced in PR #8 and the exact-match approach
from PR #4, on main commit `8e1b12f3fa699d69e0849a972ad4bc1427ac079e`.
Neither older branch is assumed merged.

- [x] Token source, generator and generated Kotlin.
- [x] Theme colour aliases and existing light/dark schemes use generated tokens.
- [x] Shared design-system spacing/radius aliases use generated tokens.
- [x] Exact-value colour migrations in DevDesignSystem, SpiritualModels,
  HomeScreen, HomeRedesignViews, BentoEditorialViews, BentoEditorialSheets,
  BentoEditorialSubScreens, ChatConsultationScreen, CallConsultationScreen,
  DreamJournalScreen, JournalSearchBar, AskAndDreamCards and UserProfileScreen.
- [ ] Review remaining decorative shades and other screens individually.
- [ ] Add a guard against new hardcoded screen colours; the current CI only
  checks source/generated-file synchronisation.

Across these 14 existing Kotlin files (including the theme), hex colour literals
drop from 762 to 152; 210 named white/black usages also use tokens.
ChatConsultationScreen, DreamJournalScreen, HomeRedesignViews, DevDesignSystem
and DevbhashaTheme have no remaining hex colour literals.

Do not substitute a nearby colour merely to remove a literal. Reuse a token
with the exact value and suitable role, or add a named token for an established
reusable palette. Palette consolidation is a separate design change.

PR #18 also changes these UI and theme files. Reconcile this branch with that
work before merging; no integration or billing code is changed here.

