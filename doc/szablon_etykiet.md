# Pole „Advanced template" — co to jest i czy tego potrzebujesz

## W jednym zdaniu

To sposób, żeby napisać „jak ma wyglądać etykieta" jednym krótkim wzorem,
zamiast klikać kilka osobnych opcji. **Jeśli proste opcje (Italic first N parts,
Case, Bold, kolor) wystarczają — szablonu nie potrzebujesz i można go usunąć.**

## Jak to działa

Nazwa okazu jest najpierw dzielona na **części** (po spacjach, po zamianie `_`):

```
Dianema_depressum_MA80673   →   [1] Dianema  [2] depressum  [3] MA80673
```

Szablon mówi, które części wziąć i jak je ostylować. Każdy kawałek `{…}` to
„weź część (lub zakres części) i zastosuj flagi". Tekst poza nawiasami jest
kopiowany dosłownie (spacja, przecinek, nawias, myślnik).

| Flaga | Znaczenie |
|---|---|
| `i` | kursywa |
| `b` | pogrubienie |
| `U` | WIELKIE LITERY |
| `L` | małe litery |
| `S` | Jak w zdaniu (pierwsza wielka) |
| `#rrggbb` | kolor, np. `#808080` szary |

Zakresy: `2` (tylko druga część), `1-2` (od pierwszej do drugiej), `3-` (od
trzeciej do końca), `*` (wszystkie).

## Przykłady

| Nazwa w pliku | Szablon | Wynik na rycinie |
|---|---|---|
| `Dianema_depressum_MA80673` | `{1-2:i} {3}` | *Dianema depressum* MA80673 |
| `Dianema_depressum_MA80673` | `{1-2:i} {3:#808080}` | *Dianema depressum* MA80673 (numer na szaro) |
| `Dianema_depressum_MA80673` | `{1-2:i} ({3})` | *Dianema depressum* (MA80673) |
| `Dianema_depressum_MA80673` | `{3:b} — {1-2:i}` | **MA80673** — *Dianema depressum* |
| `Dianema_depressum_MA80673` | `{1-2:i}` | *Dianema depressum* (numer ukryty) |
| `Trichia_sordida_var_sordidoides_MA1` | `{1-2:i} var. {4:i} {5}` | *Trichia sordida* var. *sordidoides* MA1 |
| `MA80673_Dianema_depressum` | `{2-3:i} {1:U}` | *Dianema depressum* MA80673 |

Dwa ostatnie przykłady pokazują, kiedy szablon jest naprawdę potrzebny: gdy
w pliku kolejność jest inna niż na rycinie, albo chcesz coś przestawić/ukryć.
Proste opcje tego nie umieją.

## Ograniczenia

- Jeden szablon obowiązuje dla **wszystkich** liści. Jeśli drzewo ma nazwy o
  różnej liczbie części (jedne z `var.`, inne bez), jeden szablon nie pasuje do
  wszystkich — tu lepiej sprawdzi się „mądra kursywa" z zadania 6.1.
- Błędny szablon (np. niedomknięty nawias) jest ignorowany, pole robi się czerwone.
- Gdy szablon jest wypełniony, nadpisuje proste opcje (Italic first N itd.).

## Decyzja (zaznacz)

- [ ] zostawiamy (przydaje się przy odwróconej kolejności / ukrywaniu fragmentów)
- [ ] usuwamy (proste opcje + 6.1 wystarczają)
