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

## 2026-08-23 — etap 6 (poprawki po przeklikaniu)

### Etap 6.1 — mądrzejsza kursywa (sesja A)
Panel *Tip Labels*:
- **Italic mode** — nowa lista wyboru zamiast samego „pierwsze N części":
  - *Off* — bez kursywy;
  - *First N parts* — jak dotąd, według pola **Italic first N parts**;
  - *Until first number* (domyślne) — kursywą wszystko **do pierwszej części
    wyglądającej jak numer okazu** (czyli zawierającej cyfrę); ta część i dalsze
    zostają prosto. Dzięki temu *Trichia lutescens* MA83355 i *Trichia sordida*
    var. *sordidoides* MA12345 wychodzą dobrze bez zmiany ustawień.
- **Not italic words** — słowa zostające prosto w środku nazwy (domyślnie
  `var subsp ssp f sp cf aff nov x`). **Nie przerywają** kursywy dla dalszych
  części; wielkość liter i końcowa kropka nie mają znaczenia przy dopasowaniu.
- **Add dot** — dopisuje kropkę: `var` → `var.`, `sp` → `sp.` (w nagłówkach FASTA
  kropek zwykle nie ma). Znak krzyżówki `x` kropki nie dostaje.
- **ALL CAPS** — kończy kursywę także na części pisanej samymi wielkimi literami
  bez cyfr, np. `KRAM` w numerze `KRAM M-1234`. Domyślnie włączone.
- **Join number** — składa numer okazu, który podkreślniki rozbiły na kawałki,
  z powrotem w postać, w której się go cytuje. Reguły ustalone z użytkowniczką:
  - pojedyncza wielka litera przykleja się do liczby:
    `KRAM M 1156` → `KRAM M-1156`;
  - człony numeru zielnikowego łączy myślnik:
    `UK100 1b` → `UK100-1b`, `UARK CA 6 131` → `UARK CA. 6-131`;
  - po **numerze zbieracza** (z wielkiej litery, z małymi literami i cyframi,
    np. `Ron324`) kolejne liczby to części, na które podzielono jeden zbiór
    terenowy, więc łączy je ukośnik: `Ron324 2 3` → `Ron324 2/3` (tak samo
    `2 10` → `2/10`);
  - słowo bez cyfr na końcu (np. `new`) zostaje osobno.
  Nazwy, które mają już myślnik w pliku, zostają bez zmian. Domyślnie włączone.
- **Dot after codes** — lista skrótów kolekcji cytowanych z kropką, domyślnie `CA`
  (`UARK CA. 6-131`). Puste pole = żadnych kropek.
- Pola nieużywane w wybranym trybie są wyszarzone.
- **Publication preset** przestawia teraz na *Until first number* z kropkami.
- Pliki zapisane wcześniej (bez klucza `tipLabels.italicMode`) otwierają się
  w trybie *First N parts*, więc wyglądają tak jak dotąd.

### Etap 6.2 — usunięty *Advanced template*
Pole **Advanced template** (szablon typu `{1-2:i} {3:U}` z etapu 3.5) zostało
usunięte z panelu *Tip Labels*: tryby **Italic mode** z 6.1 robią to samo bez
uczenia się składni. Zniknie też z kodu (parser szablonów w `LabelStyle`) i
z dokumentacji (`doc/szablon_etykiet.md`). Klucz `tipLabels.template` w starych
plikach jest po prostu ignorowany — pliki otwierają się normalnie.

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

### Etap 6.4 — cofanie presetu publikacyjnego (sesja C)
Przycisk **Publication preset** w panelu *Appearance* działa teraz jak
przełącznik: pierwsze kliknięcie zapamiętuje pełne bieżące ustawienia wyglądu
i dopiero potem nakłada preset, a napis zmienia się na **Undo preset**; drugie
kliknięcie przywraca dokładnie to, co było wcześniej, i napis wraca do
**Publication preset**. Zapamiętana kopia jest ważna tylko dla aktualnie
otwartego drzewa — po wczytaniu innego drzewa znika, a przycisk sam wraca do
stanu początkowego. Plan: `doc/sesja_C_preset_undo.md`.

### Nie zrobione / do sprawdzenia
- 2.6 automatyczne rozsuwanie kolidujących etykiet — odłożone.
- Etap 5 (Ctrl+kółko = rozsuwanie, naprawa skrótów `meta` → Ctrl na Windowsie) —
  zaplanowany, patrz kolejka.
- Etapy 1–4 były kompilowane, ale nie oglądane w działającym programie — czekają
  na przeklikanie.
