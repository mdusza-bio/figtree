# Kolejka proponowanych zmian w MyFigTree

Lista rzeczy do zrobienia, ułożona od najprostszych i najbardziej bolesnych do
większych. Każdy punkt ma być **osobną, małą zmianą**, którą da się skompilować
i obejrzeć w programie, zanim przejdziemy do następnej.

Jak z tego korzystać: zaznaczaj `[x]` przy zrobionych, dopisuj uwagi pod punktem,
zmieniaj kolejność, wykreślaj co niepotrzebne. Kiedy chcesz coś zrobić, wystarczy
powiedzieć np. „zróbmy punkt 1.2".

Legenda: 🟢 mała zmiana (jedna sesja) · 🟡 średnia · 🔴 duża / do rozbicia na kroki

---

## Etap 0 — porządki (prawie gotowe)

- [x] 0.1 🟢 Zmiana wyświetlanej nazwy programu na „MyFigTree" w tytule okna
  (plik `src/figtree/application/FigTreeApplication.java`), żeby odróżnić od
  zwykłego FigTree.
- [x] 0.2 🟢 Pierwszy commit forka: CLAUDE.md, build.bat, run.bat, ta kolejka.

---

## Etap 1 — nazwy okazów (podkreślniki, prefiksy)

Dziś: nazwy z FASTA wyglądają jak `Dianema_depressum_MA80673`, a na rycinie chcesz
*Dianema depressum* MA80673.

Gdzie w kodzie: `src/figtree/treeviewer/painters/BasicLabelPainter.java`,
metoda `getLabel()` — tu decyduje się, jaki tekst trafia na etykietę. Ważne:
zmieniamy tylko **wyświetlanie**, plik z drzewem zostaje nietknięty.

- [x] 1.1 🟢 **Opcja „zamień `_` na spację"** w panelu *Tip Labels*
  (checkbox). Najprostsza, natychmiast widoczna zmiana.
- [x] 1.2 🟢 **Ukrywanie prefiksów / sufiksów.** Pole tekstowe w panelu, gdzie
  wpisujesz co wyciąć (np. `EBOV|`, `_contig1`). Może być kilka wzorców
  oddzielonych przecinkiem. Opcjonalnie: pole na wyrażenie regularne dla
  bardziej zaawansowanych przypadków.
- [x] 1.3 🟢 **Zapamiętywanie tych ustawień w pliku `.tree`** (blok FigTree w
  NEXUS-ie, pliki `FigTreeNexusImporter/Exporter.java`), żeby po ponownym
  otwarciu drzewa nie ustawiać wszystkiego od nowa.
- [x] 1.4 🟡 **Podział nazwy na części** (np. `rodzaj gatunek numer` po spacjach
  lub `|`) — fundament pod etap 3, gdzie każda część dostanie własny styl.

---

## Etap 2 — nachodzące etykiety i „ściśnięte" gałęzie (drzewa ML)

Dziś: nazwy liści i wartości bootstrap kolidują ze sobą; poprawiane ręcznie
w Corelu.

Gdzie w kodzie: `BasicLabelPainter.java` (pozycja tekstu względem gałęzi),
`src/figtree/treeviewer/treelayouts/RectilinearTreeLayout.java` (odstępy
między gałęziami), `TreePane.java` (rysowanie całości).

- [x] 2.1 🟢 **Większy zakres odstępu etykiety od gałęzi** („padding") dla
  *Node Labels* / *Branch Labels* — osobno w poziomie i w pionie, żeby
  bootstrap dało się odsunąć od linii i od nazw.
- [x] 2.2 🟢 **Pozycja wartości poparcia względem gałęzi**: nad / pod / na
  końcu gałęzi przy węźle (na wzorze: pod gałęzią, tuż przed węzłem,
  dwie wartości jedna nad drugą — bootstrap i PP).
- [x] 2.3 🟢 **Próg wyświetlania poparcia** — pokazuj wartość tylko gdy
  ≥ X (np. 50). Mniej etykiet = mniej kolizji, i tak standard w publikacjach.
- [x] 2.4 🟡 **Dwie wartości na węźle naraz** (np. `bootstrap / PP` albo jedna
  nad drugą, jak na wzorze) bez sklejania ich ręcznie w pliku.
- [x] 2.5 🟡 **Minimalny odstęp pionowy między liśćmi** — ustawienie, które
  automatycznie „rozciąga" drzewo tak, żeby etykiety liści się nie nakładały
  przy danej czcionce (zamiast ręcznego kręcenia suwakiem *Expansion*).
- [ ] 2.6 🔴 **Automatyczne unikanie kolizji** etykiet węzłów: program sprawdza,
  czy prostokąt tekstu nachodzi na inny, i przesuwa go (w górę/dół, na drugą
  stronę gałęzi). To już prawdziwy algorytm — zrobić dopiero, gdy 2.1–2.5
  nie wystarczą.

Uwagi do etapu 2 (zrobione 2.1–2.5): nowe kontrolki są w panelach *Node Labels*
i *Branch Labels* (Position, Offset X/Y, Show only if >=, Second value, Layout)
oraz w panelu *Layout* → Rectangular (Min tip spacing). Wszystko zapisuje się do
pliku `.tree` (klucze `nodeLabels.*`, `branchLabels.*`,
`rectilinearLayout.minTipSpacing`). Punkt 2.6 pominięty — najpierw sprawdzić,
czy 2.1–2.5 wystarczają.

---

## Etap 3 — formatowanie tekstu etykiet (wzór: `doc/wzor.png`)

Dziś: jedna czcionka, jeden kolor, jeden styl na całą etykietę. Na wzorze:
*Dianema depressum* kursywą, `MA80673` prosto; rodziny WIELKIMI LITERAMI
z rozstrzeleniem.

- [x] 3.1 🟢 **Styl dla części nazwy**: po podziale z 1.4 — część 1–2 (rodzaj,
  gatunek) kursywą, reszta prosto. Na początek sztywno „pierwsze N słów
  kursywą", potem konfigurowalnie.
- [x] 3.2 🟢 **Wielkość liter**: bez zmian / WIELKIE / małe / Jak W Zdaniu —
  per część nazwy.
- [x] 3.3 🟡 **Pogrubienie i kolor per część nazwy** (np. numer okazu szary,
  nazwa czarna).
- [x] 3.4 🟡 **Podświetlenie wybranych liści** (np. nowe okazy pogrubione lub
  w ramce) — przez atrybut w drzewie albo listę nazw.
- [x] 3.5 🔴 **„Szablon etykiety"** — jedno pole, gdzie piszesz np.
  `{1 kursywa} {2 kursywa} {3 wielkie}`, i etykieta się z tego składa. To
  docelowa, ogólna wersja 3.1–3.3; zrobić gdy proste opcje przestaną wystarczać.

Uwagi do etapu 3 (zrobione 3.1–3.5): nowe kontrolki są w panelu *Tip Labels*
(Italic first N parts, Case (italic parts) / Case (other parts), Bold + kolor
dla obu grup, Highlight names containing + Bold + kolor, Advanced template).
Kolor pusty = zwykły kolor etykiety; „x" czyści kolor. Szablon, np.
`{1-2:i} {3:U}` — zakresy `2`, `1-2`, `3-` (do końca), `*`; flagi `i` kursywa,
`b` pogrubienie, `U`/`L`/`S` wielkość liter, `#rrggbb` kolor; tekst poza `{}`
kopiowany dosłownie. Wypełniony szablon nadpisuje proste opcje; błędny jest
ignorowany (pole robi się czerwone). Stylowanie działa, gdy *Display* = Names
i nie ma drugiej wartości. Zapis do pliku `.tree` (klucze `tipLabels.italicParts`,
`italicCase`, `italicBold`, `italicColour`, `otherCase`, `otherBold`,
`otherColour`, `highlight`, `highlightBold`, `highlightColour`, `template`).

---

## Etap 4 — wygląd drzewa „jak z publikacji"

- [x] 4.1 🟢 **Kropki na węzłach z wysokim poparciem** (na wzorze: czarne
  kropki przy bootstrap ≥ próg). FigTree ma *Node Shapes*, ale nie da się ich
  warunkować progiem — dodać próg.
- [x] 4.2 🟢 **Domyślne ustawienia „do publikacji"** — przycisk / preset:
  czcionka, grubość linii, wyrównanie etykiet liści, brak tła itd. Zamiast
  klikać 15 rzeczy przy każdym drzewie.
- [x] 4.3 🟡 **Pionowe paski grup po prawej** (rodziny, rzędy — jak A/B/C/D/E
  na wzorze) z nazwą obróconą o 90°. FigTree umie kolorować klady, ale nie
  rysuje takich pasków.
- [x] 4.4 🟡 **Kolorowe tła za kladami** (pastelowe prostokąty jak na miniaturze
  w rogu wzoru).
- [x] 4.5 🟢 **Łamana gałąź „//"** dla bardzo długich gałęzi (na wzorze:
  *Lycogala* i *Reticularia lycoperdon*), żeby outgroup nie ściskał reszty.
- [x] 4.6 🟢 **Eksport: większa kontrola nad PDF/SVG** — rozmiar strony,
  marginesy, czcionki osadzone, żeby wynik nie wymagał poprawek w Corelu.

Uwagi do 4.3 / 4.4 / 4.6 (zrobione): nowy panel *Group Bars* (tylko układ
prostokątny; w polarnym/radialnym nic nie rysuje). Checkbox tytułu włącza paski
po prawej (Attribute, Bar width, Gap from labels, Font size), a sekcja
*Backgrounds* z własnym Attribute i Opacity (%) rysuje pastelowe prostokąty za
kladami (od wspólnego przodka liści do prawej krawędzi etykiet). Paski/tła
powstają dla ciągów sąsiednich liści o tej samej wartości atrybutu (np. `family`);
kolory biorą się ze schematu *Colour by* dla tego atrybutu. Zapis do `.tree`
(klucze `groupBars.isShown`, `attribute`, `barWidth`, `gap`, `fontSize`,
`backgrounds`, `backgroundAttribute`, `backgroundAlpha`). Eksport PDF pokazuje
najpierw okienko: Page size (Fit to tree = jak dotąd / A4 portrait / A4 landscape),
Margin (mm) i Embed fonts (osadza czcionki TrueType z folderu systemowego;
pierwsze użycie trwa kilka sekund). SVG bez zmian.

Uwagi do 4.1 / 4.2 / 4.5: w panelach *Node Shapes* / *Tip Shapes* są pola
„Threshold attribute" i „Show only if >=" (kropka rysuje się tylko na węzłach,
których atrybut, np. `label` = bootstrap, jest ≥ progu; klucze
`nodeShapeInternal.thresholdAttribute`, `nodeShapeInternal.showThreshold`).
W panelu *Appearance* jest przycisk **Publication preset** — ustawia naraz:
Times New Roman 10 pt, kursywa 2 pierwszych części nazwy, `_` → spacja,
wyrównane etykiety liści, linie 1 pt, białe tło, wartości poparcia ≥ 50 pod
gałęzią (8 pt), czarne kropki przy poparciu ≥ 95 (jeśli drzewo ma atrybut
`label`). W panelu *Layout* → Rectangular jest „Shorten branches longer than"
(0 = wyłączone; dłuższe gałęzie rysowane są skrócone do tej długości ze
znakiem `//`; klucz `rectilinearLayout.maxBranchLength`). Uwaga: skrócona gałąź
nie zgadza się ze skalą — o to właśnie chodzi w `//`.

---

## Uwagi / pytania do rozstrzygnięcia

- Zrzuty `doc/shot1–5.png` i `doc/large_trees.md` pochodzą z **oryginalnego
  repozytorium** (pokazują drzewo na 1610 liści i obejście suwakiem *Expansion*).
  Pokazują problem 2.x, ale nie są Twoimi drzewami — warto wrzucić do `doc/`
  własny przykładowy plik `.tree` (mały, kilkanaście liści), na którym będziemy
  testować każdą zmianę.
- Jakie są typowe formaty Twoich nazw? (np. `Rodzaj_gatunek_NUMER`,
  `Rodzaj_gatunek_sp_NUMER`, coś z `|`?) — od tego zależy, jak zrobić 1.4.
- Skąd biorą się wartości poparcia w Twoich plikach: jako etykieta węzła
  (`label`), jako atrybut (`bootstrap=`), jedna czy dwie (ML + bayesowskie)?

---

## Proponowana kolejność na start

1. 0.2 → 1.1 → 1.2 → 1.3 (jedna-dwie sesje; od razu widać efekt)
2. 2.1 → 2.2 → 2.3 (pozycja i próg poparcia)
3. 1.4 → 3.1 → 3.2 (kursywa + wielkie litery)
4. 4.1, 4.2, 4.5
5. Reszta według potrzeb.

---

## Etap 5 — obsługa myszy i klawiatury (nawigacja bez skakania po interfejsie)

Stan dziś: kółko myszy tylko przewija; skróty zoom/rozsuwanie są zdefiniowane
klawiszem `meta` (Cmd z Maca — `src/figtree/treeviewer/TreeViewerController.java`,
linie ~221–229), więc na Windowsie nie działają w ogóle. Zostaje tylko suwak.

- [ ] 5.1 🟢 **Ctrl + kółko = rozsuwanie (Expansion)**, Ctrl + Shift + kółko =
  zoom. Jeden nasłuch kółka na panelu drzewa podpięty pod istniejące akcje
  increase/decreaseVerticalExpansion i increase/decreaseZoom. Kółko bez Ctrl
  zostaje przewijaniem.
- [ ] 5.2 🟢 **Naprawa skrótów na Windowsie**: zamiast `meta` użyć
  `Toolkit.getMenuShortcutKeyMask()` (Ctrl na Windows, Cmd na Macu) —
  Ctrl+`=` / Ctrl+`-` rozsuwanie, Ctrl+Alt+`=`/`-` zoom, Ctrl+0 reset.
- [ ] 5.3 🟢 **Rozsuwanie „wokół kursora"**: po Ctrl+kółku przewinąć widok tak,
  żeby gałąź pod kursorem została w tym samym miejscu ekranu (inaczej drzewo
  ucieka w dół). Zrobić po 5.1, jeśli będzie przeszkadzać.

---

## Etap 6 — poprawki po pierwszym przeklikaniu (feedback 2026-08-23)

Każdy punkt ma własny plan sesji w `doc/sesja_*.md` — można je robić
niezależnie, równolegle, w osobnych rozmowach (np. „zrób sesję A").

- [ ] 6.1 🟡 **Mądrzejsza kursywa** — „pierwsze N części" nie wystarcza: *Trichia
  sordida* var. *sordidoides* MA12345 ma 4 części nazwy, a *Trichia lutescens*
  MA83355 tylko 2. Zamiast liczby: kursywa „do pierwszej części wyglądającej jak
  numer" + lista słów bez kursywy (`var.`, `subsp.`, `f.`, `sp.`, `cf.`, `aff.`).
  Plan: `doc/sesja_A_kursywa.md`.
- [ ] 6.2 🟢 **Szablon etykiet — wyjaśnić albo usunąć.** Opis z przykładami w
  `doc/szablon_etykiet.md`; decyzja po przeczytaniu. Jeśli 6.1 załatwia
  potrzeby, pole *Advanced template* usuwamy z panelu.
- [x] 6.3 🟡 **Group Bars nic nie pokazują** — bo liście nie mają atrybutu
  „rodzina". FigTree nie zna rodzin; trzeba je podać. Zrobione: poprawiony import
  z pliku `nazwa<TAB>rodzina` (*File → Import Annotations…*, z podsumowaniem
  dopasowań) + przycisk *Assign to selection…* w panelu *Group Bars*.
  Plan: `doc/sesja_B_group_bars.md`. Pliki testowe: `doc/przyklad.tree`,
  `doc/przyklad_rodziny.tsv`. **Czeka na przeklikanie.**
- [ ] 6.4 🟢 **Cofanie presetu publikacyjnego** — przed zastosowaniem zapamiętać
  poprzednie ustawienia; przycisk zmienia się w *Undo preset*. Plan:
  `doc/sesja_C_preset_undo.md`.
- [ ] 6.5 🟢 **Etap 5 (mysz/klawiatura)** — plan: `doc/sesja_D_mysz.md`.
