# Changelog — MyFigTree MD

Prywatny fork FigTree 1.4.4 (`rambaut/figtree`) dostosowany do przygotowywania
rycin drzew filogenetycznych do publikacji. Wszystkie nowe ustawienia zapisują
się w pliku `.tree` (blok FigTree w NEXUS-ie); pliki z oryginalnego FigTree
otwierają się bez zmian.

Szczegółowa lista planowanych i zrobionych punktów: [doc/kolejka_zmian.md](doc/kolejka_zmian.md).

## 2026-08-23 — etapy 0–4

### Start forka (commit `2f2ed83`)
- Wyświetlana nazwa programu: **MyFigTree MD** (tytuł okna, okno „About"), żeby
  odróżnić od zwykłego FigTree zainstalowanego obok. Nazwy klas bez zmian.
- `build.bat`, `run.bat`, `CLAUDE.md`, kolejka zmian w `doc/`.

### Etap 1 — nazwy okazów (commit `ba44cb7`)
Panele *Tip Labels* / *Node Labels* / *Branch Labels*:
- **Replace '_' with space** — podkreślniki z FASTA zamieniane na spacje
  (tylko na ekranie/rycinie, plik drzewa nietknięty). Domyślnie włączone dla
  nazw liści.
- **Hide parts** — lista fragmentów do wycięcia z nazwy, po przecinku
  (np. `EBOV|,_contig1`).
- **Hide regex** — to samo wyrażeniem regularnym; błędne wyrażenie jest
  ignorowane.
- Nowa klasa `LabelFormatter` dzieli nazwę na części (fundament pod etap 3).

### Etap 2 — etykiety poparcia, ściśnięte gałęzie (commit `f246dcf`)
Panele *Node Labels* / *Branch Labels*:
- **Position** (tylko Node Labels): At node / Above branch / Below branch.
- **Offset X / Offset Y** — odsunięcie etykiety od węzła i od linii gałęzi
  (w punktach, mogą być ujemne).
- **Show only if >=** — próg; wartości poniżej nie są rysowane (np. bootstrap < 50).
- **Second value** + **Layout** — druga wartość (np. PP obok bootstrapu) w formie
  `a / b` albo jedna nad drugą (*stacked*).

Panel *Layout* → Rectangular:
- **Min tip spacing** — minimalny odstęp pionowy między liśćmi (pt); drzewo
  samo się wydłuża, żeby nazwy się nie nakładały. Działa też przy eksporcie PDF.

### Etap 3 — formatowanie części nazwy (commit `99c83c7`)
Panel *Tip Labels*:
- **Italic first N parts** — np. 2 → *Dianema depressum* kursywą, `MA80673` prosto.
- **Case (italic parts) / Case (other parts)** — As is / UPPER / lower / Sentence.
- **Bold** i **kolor** osobno dla części kursywnych i pozostałych („x" czyści
  kolor → zwykły kolor etykiety).
- **Highlight names containing** — lista fragmentów nazw; pasujące liście
  dostają pogrubienie / kolor.
- **Advanced template** — np. `{1-2:i} {3:U}`; zakresy `1-2`, `3-`, `*`; flagi
  `i` `b` `U` `L` `S` `#rrggbb`. Nadpisuje proste opcje; błędny szablon jest
  ignorowany (pole na czerwono).
- Stylowane etykiety trafiają do eksportu PDF i SVG.
- Naprawa `.gitignore`: wzorzec `FigTree*` ukrywał nowe pliki w `src/figtree/`.

### Etap 4 — wygląd „jak z publikacji" (commit `c7e44a4`)
- *Node Shapes* / *Tip Shapes*: **Threshold attribute** + **Show only if >=** —
  np. czarne kropki tylko na węzłach z bootstrap ≥ 95.
- *Appearance*: przycisk **Publication preset** — białe tło, linie 1 pt, Times
  New Roman 10 pt, kursywa pierwszych 2 części, podkreślniki → spacje, poparcie
  (atrybut `label`) pod gałęzią z progiem 50, kropki przy ≥ 95, układ prostokątny
  z wyrównanymi nazwami.
- *Layout* → Rectangular: **Shorten branches longer than** — zbyt długie gałęzie
  (np. outgroup) są skracane i oznaczane `//`. Uwaga: skrócone gałęzie nie
  odpowiadają już skali.
- Nowy panel **Group Bars** — pionowe paski po prawej z nazwą grupy (rodzina,
  rząd…) obróconą o 90°, wg atrybutu liści; sekcja **Backgrounds** — pastelowe tła
  za kladami (od wspólnego przodka do krawędzi etykiet). Tylko układ prostokątny.
  Kolory ze schematu *Colour by* tego atrybutu.
- Eksport PDF: okno z wyborem **Page size** (Fit to tree / A4 portrait / A4
  landscape), **Margin (mm)** i **Embed fonts** (pierwsze użycie trwa kilka sekund).

### Etap 6.3 — skąd program ma znać rodziny (Group Bars)
Paski grup rysują się według **atrybutu liści** (np. `family = Trichiaceae`),
a zwykły plik z drzewem z IQ-TREE czy MrBayes takich informacji nie zawiera —
dlatego panel *Group Bars* wyglądał na zepsuty. Teraz są dwa wygodne sposoby,
żeby te informacje dodać.

**1. Plik z przypisaniem — *File → Import Annotations…* (Ctrl+I).**
Tabelka z Excela, zapisana jako „tekst rozdzielany tabulatorami" albo CSV:

```
name	family	order
Trichia_lutescens_MA83355	Trichiaceae	Trichiales
Arcyria_ferruginea_MA58962	Arcyriaceae	Trichiales
```

Pierwszy wiersz to nagłówki (to one stają się nazwami atrybutów), pierwsza
kolumna to nazwy okazów **dokładnie jak w pliku drzewa**, z podkreślnikami.
Sam import istniał już w FigTree, ale był bardzo wybredny; poprawki:
- kolumny rozdzielone tabulatorem, przecinkiem **albo średnikiem** (tak zapisuje
  CSV polski Excel),
- polskie znaki czytane i z UTF-8, i z kodowania systemowego,
- puste wiersze, komórki w cudzysłowach i wiersze zaczynające się od `#` nie
  przeszkadzają; pusta komórka = brak atrybutu (pasek się tam przerywa),
- po wczytaniu pojawia się okno z podsumowaniem: **ile nazw z pliku udało się
  dopasować** do liści drzewa i lista tych, których w drzewie nie ma (wcześniej
  import, który nie dopasował niczego, wyglądał tak samo jak udany),
- nazwy różniące się tylko wielkością liter albo spacjami zamiast podkreślników
  też są dopasowywane (okno mówi, ilu takich było).

**2. Przycisk *Assign to selection…* w panelu *Group Bars*.**
Zaznacz klad (tryb *Clade*) albo kilka nazw liści, kliknij przycisk, podaj nazwę
atrybutu (np. `family`) i wartość (np. `Trichiaceae`). Wszystkie liście
z zaznaczenia dostają ten atrybut naraz, panel od razu przełącza się na niego
i włącza paski. To samo co *Annotate*, ale bez klikania okaz po okazie.

Atrybuty liści zapisują się w pliku `.tree` przy *Save*, więc po ponownym
otwarciu są już na miejscu.

Pliki do testów: [doc/przyklad.tree](doc/przyklad.tree) (8 okazów) razem
z [doc/przyklad_rodziny.tsv](doc/przyklad_rodziny.tsv).

### Nie zrobione / do sprawdzenia
- 2.6 automatyczne rozsuwanie kolidujących etykiet — odłożone.
- Etap 5 (Ctrl+kółko = rozsuwanie, naprawa skrótów `meta` → Ctrl na Windowsie) —
  zaplanowany, patrz kolejka.
- Etapy 1–4 były kompilowane, ale nie oglądane w działającym programie — czekają
  na przeklikanie.
