# Changelog — MyFigTree MD

Prywatny fork FigTree 1.4.4 (`rambaut/figtree`) dostosowany do przygotowywania
rycin drzew filogenetycznych do publikacji. Wszystkie nowe ustawienia zapisują
się w pliku `.tree` (blok FigTree w NEXUS-ie); pliki z oryginalnego FigTree
otwierają się bez zmian.

Instrukcja obsługi (co robi która opcja): [doc/instrukcja.md](doc/instrukcja.md).
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

### Etap 6.14 — *Publication preset* to teraz zestaw użytkowniczki
Przycisk ustawiał mój zestaw „domyślny"; po pierwszej prawdziwej rycinie
użytkowniczka podała, co faktycznie klika za każdym razem, i to trafiło do presetu:

| ustawienie | wartość |
|---|---|
| *Trees* → Order nodes | decreasing |
| *Tip Labels* → font | Times New Roman 11 pt |
| *Tip Labels* → Not italic words | domyślne + `holotypus paratypus isotypus` |
| *Tip Labels* → Highlight | `holotypus,paratypus,isotypus`, pogrubione |
| *Node Labels* → Display | atrybut poparcia (patrz niżej) |
| *Node Labels* → font | Times New Roman 9 pt |
| *Node Labels* → Position | Above branch |
| *Node Labels* → Show only if >= | 70 |
| *Node Labels* → Avoid overlap / White backing | włączone |

Wyrzucone z presetu (na jej prośbę): czarne kropki na węzłach ≥ 95 oraz wyrównanie
nazw do prawej — preset ich nie dotyka, więc zostają takie, jakie sobie ustawi.

Przy okazji: **rozpoznawanie atrybutu z poparciem** sprawdza teraz kolejno
`bootstrap`, `support`, `label`. FigTree przy imporcie pyta, jak nazwać liczby
na węzłach, więc nie zawsze jest to domyślne `label`.

### Etap 6.13 — zapis, który nie może skasować danych
**Co się stało (2026-08-25).** Program był otwarty, gdy jar został przebudowany.
Działająca kopia straciła dostęp do klas, których jeszcze nie wczytała, więc
*Tree → Annotate*, *File → Export Trees* i **zapis** przestały działać — w zupełnej
ciszy, bez komunikatu. Gorzej: zapis najpierw **czyścił plik docelowy**, a dopiero
potem pisał do niego drzewo, więc po nieudanym *Save* został plik 0-bajtowy.
Drzewo użytkowniczki (`ML_SmE.raxml.support`) zostało odtworzone RAxML-em
z ocalałych `.bestTree` + `.bootstraps`.

**Poprawki:**
- **Zapis do pliku tymczasowego + podmiana.** Drzewo idzie najpierw do pliku
  `figtree*.part` obok docelowego i dopiero kompletny plik podmienia stary
  (atomowo, jeśli system plików to potrafi). Nieudany zapis nie rusza tego,
  co już masz na dysku.
- **Nieudany zapis mówi, co się stało** — okienko z treścią błędu i zapewnieniem,
  że stary plik jest nietknięty. Wcześniej błąd inny niż `IOException` przechodził
  bez śladu.
- To samo dla *File → Export Trees…* i *Tree → Annotate…* — zamiast „nic się nie
  dzieje" pokazuje się komunikat.

**Zasada pracy** (dopisana do `CLAUDE.md`): przed każdą kompilacją zamknąć
działający program.

### Etap 6.12 — zmiana podpisu liścia i kropka tylko przy skrótach
- **Zmiana podpisu (*Tree → Annotate… → Name*) często „nic nie robiła".** *Annotate*
  zapisuje nową nazwę na **taksonie**, gdy zaznaczona jest etykieta liścia, ale na
  **węźle**, gdy zaznaczony jest węzeł — a program przy rysowaniu nazw liści
  zaglądał wyłącznie do taksonu. Teraz sprawdza oba miejsca, więc zmiana nazwy
  działa niezależnie od tego, jak liść został zaznaczony.
- **Add dot dopisywał kropkę do każdego słowa** z listy *Not italic words* — więc po
  dopisaniu tam `holotypus` na rycinie wychodziło `holotypus.`. Kropkę dostają teraz
  tylko skróty, czyli słowa **do 5 liter** (`var` → `var.`, `subsp` → `subsp.`),
  a wypisane w całości słowa zostają nietknięte.

### Etap 6.11 — ręczna poprawka pojedynczego liścia
Ogólne reguły kursywy nie mają jak zgadnąć wszystkiego — np.
`Trichia_sordida_holotypus` nie ma w nazwie numeru, więc całość łącznie ze słowem
`holotypus` szła kursywą. Panel *Tip Labels* dostaje przycisk
**Style selected tips...**:
- zaznaczasz liść (albo kilka) w drzewie i w okienku odznaczasz **italic** przy
  tych członach, które mają być pismem prostym;
- **bold whole label** pogrubia całą nazwę tego liścia;
- człony są pokazane tak, jak się rysują — sklejony numer `KRAM M-2749` jest
  jednym polem;
- **Back to rules** kasuje ręczne ustawienie i liść wraca do ogólnych reguł.

Ręczne ustawienie siedzi na węźle liścia (atrybuty `!labelItalic`, `!labelBold`)
i zapisuje się razem z drzewem.

**Poprawka do wcześniejszej notatki:** w pierwszej wersji napisałam tu, że
eksporter gubi atrybuty taksonów. To był błąd — sprawdziłam to metodą
`exportTree()`, a *Save* używa `exportTrees(..., true)`, który wypisuje też blok
`taxa`. Atrybuty taksonów (np. `family` z importu, `!name` ze zmiany podpisu)
zapisują się normalnie.

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

Słowniki grup organizmów (rodzaj → rodzina → rząd) leżą w
[doc/slowniki/](doc/slowniki/) — na razie roboczy, do sprawdzenia
[sluzowce.tsv](doc/slowniki/sluzowce.tsv) z 63 rodzajami śluzowców. Opis formatu
i sposób użycia w Excelu: [doc/slowniki/README.md](doc/slowniki/README.md).

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

### Etap 5 / 6.5 — mysz i klawiatura (sesja D)
Do tej pory kółko myszy tylko przewijało, a skróty zoom/rozsuwania używały
klawisza `meta` (Cmd z Maca), więc na Windowsie nie działały wcale.
- **Ctrl+kółko** nad drzewem rozsuwa/ściska pionowo (jak suwak *Expansion*), a
  w układach, gdzie rozsuwanie nie istnieje (polarny, radialny) — przybliża/
  oddala (zoom). **Ctrl+Shift+kółko** zawsze zbliża/oddala. Samo kółko dalej
  zwyczajnie przewija widok.
- **Skróty klawiszowe naprawione na Windowsie**: Ctrl+`=` / Ctrl+`-`
  rozsuwanie, Ctrl+Alt+`=` / Ctrl+Alt+`-` zoom, Ctrl+0 reset (wcześniej
  wymagały klawisza Cmd, którego na Windowsie nie ma).
- **Rozsuwanie/zoom kółkiem trzyma się kursora** — gałąź, nad którą jest
  kursor, zostaje w tym samym miejscu ekranu zamiast uciekać (poprzednio widok
  zawsze wracał na środek okna).
- **Szybsze, gładsze przewijanie kółkiem** (bez Ctrl) — i nad samym drzewem, i
  na liście ustawień po lewej stronie; wcześniej trzeba było się „naklikać"
  kółkiem, żeby przesunąć widok o kawałek.
- **Ctrl+kółko wyraźnie czulsze** — jeden „ząbek" kółka daje teraz odczuwalną
  zmianę zamiast mikroskopijnej.

### Nie zrobione / do sprawdzenia
- 2.6 automatyczne rozsuwanie kolidujących etykiet — odłożone.
- Etap 5 (Ctrl+kółko = rozsuwanie, naprawa skrótów `meta` → Ctrl na Windowsie) —
  zaplanowany, patrz kolejka.
- Etapy 1–4 były kompilowane, ale nie oglądane w działającym programie — czekają
  na przeklikanie.
