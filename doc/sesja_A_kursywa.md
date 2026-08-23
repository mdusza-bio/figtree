# Sesja A — mądrzejsza kursywa w nazwach (zadanie 6.1)

Niezależna od sesji B, C, D. Dotyka tylko:
`src/figtree/treeviewer/painters/LabelStyle.java`, `LabelPainterController.java`
(panel *Tip Labels*), `TreeAppearanceController.java` (preset).

## Problem

„Italic first N parts" zakłada, że nazwa gatunkowa ma zawsze tyle samo części.
Nieprawda:

| Nazwa | części nazwy | numer |
|---|---|---|
| `Trichia_lutescens_MA83355` | 2 | MA83355 |
| `Trichia_sordida_var_sordidoides_MA12345` | 4 (var. prosto) | MA12345 |
| `Dianema_sp_MA86506` | 2 (sp. prosto) | MA86506 |
| `Arcyria_ferruginea_BR5020022218059` | 2 | BR5020022218059 |
| `Licea_marginata_DWM7368` | 2 | DWM7368 |

Numery zielnikowe mają różny format (prefiks literowy + cyfry, czasem same
cyfry, czasem z `-` lub `/`).

## Pomysł: kursywa „do pierwszego numeru", z wyjątkami

W panelu *Tip Labels* nowe combo **Italic mode**:

1. **First N parts** — jak dziś.
2. **Until first number-like part** (nowy, domyślny) — kursywą są części od
   początku **do pierwszej części zawierającej cyfrę** (ta i następne — prosto).
   Do tego pole **Not italic words** (domyślnie `var subsp ssp f sp cf aff nov x`)
   — części z tej listy (z kropką lub bez, bez rozróżniania wielkości liter)
   zostają prosto, ale **nie przerywają** kursywy dla dalszych części.

Przykłady w trybie 2:
- `Trichia_sordida_var_sordidoides_MA12345` → *Trichia sordida* var. *sordidoides* MA12345
- `Dianema_sp_MA86506` → *Dianema* sp. MA86506
- `Lycogala_epidendrum_MA83194` → *Lycogala epidendrum* MA83194

Opcjonalnie checkbox **Add dot after rank words** — `var` → `var.`, `sp` → `sp.`
(w FASTA kropek zwykle nie ma).

## Kroki dla Claude'a

1. W `LabelStyle` dodać enum `ItalicMode { FIRST_N, UNTIL_NUMBER }`, pole
   `nonItalicWords` (zbiór, porównanie po usunięciu końcowej kropki i bez
   wielkości liter) oraz `addRankDots` (boolean).
2. W `getRuns()` wydzielić metodę `boolean[] italicMask(String[] parts)`
   obsługującą oba tryby.
3. Kontrolki w `LabelPainterController` (obok *Italic first N parts*): combo
   *Italic mode*, pole *Not italic words*, checkbox *Add dot*. Klucze:
   `tipLabels.italicMode`, `tipLabels.nonItalicWords`, `tipLabels.addRankDots`.
   Stare pliki bez tych kluczy → tryb FIRST_N.
4. Preset publikacyjny przełączyć na UNTIL_NUMBER.
5. Szybki test bez GUI na przykładach z tabeli.
6. `/compile`, commit „Etap 6.1: kursywa do pierwszego numeru", odhaczyć 6.1,
   dopisać do CHANGELOG.md.

## Do ustalenia z użytkowniczką

- Czy bywają numery bez żadnej cyfry? (wtedy dodatkowa reguła: „część z samych
  WIELKICH liter").
- Czy `sp.` ma być prosto (standard botaniczny: tak).
