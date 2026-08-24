# Słowniki grup organizmów

Tabelki „rodzaj → rodzina → rząd" do opisywania liści drzewa (panel *Group Bars*,
menu *File → Import Annotations…*). Jeden plik na grupę organizmów:

- [sluzowce.tsv](sluzowce.tsv) — śluzowce (Myxomycetes), 63 rodzaje. **Roboczy,
  do sprawdzenia.**

Nowy plik dla innej grupy (grzyby, porosty, rośliny) robi się tak samo — wystarczy
zachować format. Nazwa pliku jest dowolna, np. `grzyby.tsv`, `porosty.tsv`.

## Format

Zwykły plik tekstowy, kolumny rozdzielone tabulatorem (w Excelu: *Zapisz jako →
Tekst rozdzielany tabulatorami*). Pierwszy wiersz to nagłówki:

```
genus	family	order
Trichia	Trichiaceae	Trichiales
Arcyria	Arcyriaceae	Trichiales
```

- **Pierwsza kolumna** to klucz — nazwa, po której szukamy (tu: rodzaj).
- **Pozostałe kolumny są dowolne.** Nagłówek kolumny staje się nazwą atrybutu
  w programie, więc `family` i `order` można uzupełnić o cokolwiek, co ma się
  znaleźć na rycinie albo w kolorach: `substrat`, `region`, `grupa_troficzna`.
- Wiersze zaczynające się od `#` to komentarze — program je pomija. Dobre miejsce
  na notatki o spornych ujęciach systematycznych.
- Puste komórki są dozwolone: taki okaz po prostu nie dostaje atrybutu (pasek
  grupy się w tym miejscu przerywa).

## Jak tego użyć dzisiaj

*Import Annotations…* dopasowuje **pełne nazwy okazów**, a nie rodzaje, więc
słownik jest na razie tabelą pomocniczą: w Excelu dokleja się rodzinę do listy
okazów, a dopiero wynik wczytuje do programu.

1. W Excelu w kolumnie **A** wklej nazwy okazów dokładnie takie, jak w pliku
   drzewa (`Trichia_lutescens_MA83355`).
2. W **B** wyciągnij rodzaj, czyli pierwszy człon nazwy:
   `=LEWY(A2;ZNAJDŹ("_";A2)-1)`
3. Wklej słownik do drugiego arkusza (nazwij go `slownik`) i w **C** oraz **D**:
   `=WYSZUKAJ.PIONOWO(B2;slownik!$A:$C;2;FAŁSZ)` — rodzina
   `=WYSZUKAJ.PIONOWO(B2;slownik!$A:$C;3;FAŁSZ)` — rząd
4. Skopiuj formuły w dół, zamień wynik na wartości (*Wklej specjalnie → Wartości*),
   usuń kolumnę B, nagłówki zrób `name`, `family`, `order`.
5. Zapisz jako tekst rozdzielany tabulatorami i wczytaj przez
   *File → Import Annotations…* Okno powie, ile nazw udało się dopasować.

`#N/D` w kolumnie C oznacza rodzaj, którego w słowniku nie ma — warto go dopisać,
wtedy słownik z każdym drzewem robi się kompletniejszy.

## Planowane ułatwienie

Docelowo program ma sam dopasowywać takie słowniki po pierwszym członie nazwy
(czyli po rodzaju) i sam wypisywać listę rodzajów z otwartego drzewa do
wypełnienia — wtedy kroki 1–4 znikają. Zadanie czeka w
[kolejce zmian](../kolejka_zmian.md).

## Uwaga o ujęciach systematycznych

`sluzowce.tsv` trzyma się układu tradycyjnego (Martin & Alexopoulos, baza
nomenklatoryczna Lado): *Trichiales* = Trichiaceae + Arcyriaceae + Dianemataceae,
*Liceales* razem. Nowsze ujęcie molekularne (Leontyev i in. 2019) łączy te trzy
rodziny w Trichiaceae, dzieli Liceales na Cribrariales i Reticulariales i zmienia
Stemonitales na Stemonitidales. Jeśli publikacja ma iść w tym drugim układzie,
najprościej zrobić drugi plik, np. `sluzowce_2019.tsv`, i wybierać przy imporcie.
