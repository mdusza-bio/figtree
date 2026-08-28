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

## Jak tego użyć

Słownik wczytuje się **wprost**: *File → Import Annotations…* i wskazać plik.
Nazwa jednowyrazowa w pierwszej kolumnie (np. `Trichia`) jest traktowana jak
rodzaj — jej rodzina i rząd trafiają do **wszystkich** okazów, których nazwa
zaczyna się od tego rodzaju (`Trichia_lutescens_MA83355`, `Trichia_varia_MA80112`…).
Okno po imporcie mówi, ile nazw dopasowało się po rodzaju i ile okazów objęły;
rodzaje, których w drzewie nie ma, są po prostu pomijane (i wypisane w oknie).

Gdy w drzewie trafi się rodzaj, którego słownik nie zna, doda się go najprościej
przez *File → Export Group Template…* — program wypisze alfabetyczną listę
rodzajów otwartego drzewa z pustymi kolumnami; brakujące wiersze można przekleić
do słownika. Tak słownik z każdym drzewem robi się kompletniejszy.

Wyjątki od słownika (np. jeden okaz o spornym przypisaniu) załatwia dodatkowy
wiersz z **pełną nazwą okazu** — pełna nazwa zawsze wygrywa z rodzajem, obie
postacie mogą siedzieć w jednym pliku.

## Uwaga o ujęciach systematycznych

`sluzowce.tsv` trzyma się układu tradycyjnego (Martin & Alexopoulos, baza
nomenklatoryczna Lado): *Trichiales* = Trichiaceae + Arcyriaceae + Dianemataceae,
*Liceales* razem. Nowsze ujęcie molekularne (Leontyev i in. 2019) łączy te trzy
rodziny w Trichiaceae, dzieli Liceales na Cribrariales i Reticulariales i zmienia
Stemonitales na Stemonitidales. Jeśli publikacja ma iść w tym drugim układzie,
najprościej zrobić drugi plik, np. `sluzowce_2019.tsv`, i wybierać przy imporcie.
