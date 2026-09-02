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
- [x] 2.6 🔴 **Automatyczne unikanie kolizji** etykiet węzłów: program sprawdza,
  czy prostokąt tekstu nachodzi na inny, i przesuwa go (w górę/dół, na drugą
  stronę gałęzi). To już prawdziwy algorytm — zrobić dopiero, gdy 2.1–2.5
  nie wystarczą. **Zrobione w 6.6** (przesuwanie w lewo wzdłuż własnej gałęzi
  z linią wskazującą; bez przerzucania na drugą stronę gałęzi).

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

- [x] 5.1 🟢 **Ctrl + kółko = rozsuwanie (Expansion)**, Ctrl + Shift + kółko =
  zoom. Jeden nasłuch kółka na panelu drzewa podpięty pod istniejące akcje
  increase/decreaseVerticalExpansion i increase/decreaseZoom. Kółko bez Ctrl
  zostaje przewijaniem.
- [x] 5.2 🟢 **Naprawa skrótów na Windowsie**: zamiast `meta` użyć
  `Toolkit.getMenuShortcutKeyMask()` (Ctrl na Windows, Cmd na Macu) —
  Ctrl+`=` / Ctrl+`-` rozsuwanie, Ctrl+Alt+`=`/`-` zoom, Ctrl+0 reset.
- [x] 5.3 🟢 **Rozsuwanie „wokół kursora"**: po Ctrl+kółku przewinąć widok tak,
  żeby gałąź pod kursorem została w tym samym miejscu ekranu (inaczej drzewo
  ucieka w dół). Zrobić po 5.1, jeśli będzie przeszkadzać.

Uwagi do etapu 5 (zrobione 5.1–5.3): przy testach doszły dwie dodatkowe poprawki,
spoza pierwotnego planu, zgłoszone przez użytkowniczkę na żywo:
- **Szybsze przewijanie samym kółkiem** — `TreePane` nie zgłaszał Swingowi żadnej
  sugerowanej „jednostki" przewijania, więc kółko ledwo ruszało widok (1 px na
  „ząbek"). Ustawiony stały skok (24 px) w `DefaultTreeViewer` i w scrollu panelu
  kontrolek po lewej (`figtree/application/FigTreePanel.java`) — oba miejsca
  osobno korzystały z domyślnej, zbyt małej wartości Swinga.
- **Ctrl+kółko zbyt mało czułe** — akcje rozsuwania/zoomu są pomyślane pod
  przytrzymany klawisz (powtarzanie), więc jedno wywołanie z kółka ledwo było
  widoczne. Jeden „ząbek" kółka wykonuje teraz tę samą akcję 20 razy pod rząd
  (stała `WHEEL_ZOOM_STEPS`), zamiast raz.

---

## Etap 6 — poprawki po pierwszym przeklikaniu (feedback 2026-08-23)

Każdy punkt ma własny plan sesji w `doc/sesja_*.md` — można je robić
niezależnie, równolegle, w osobnych rozmowach (np. „zrób sesję A").

- [x] 6.1 🟡 **Mądrzejsza kursywa** — „pierwsze N części" nie wystarcza: *Trichia
  sordida* var. *sordidoides* MA12345 ma 4 części nazwy, a *Trichia lutescens*
  MA83355 tylko 2. Zamiast liczby: kursywa „do pierwszej części wyglądającej jak
  numer" + lista słów bez kursywy (`var.`, `subsp.`, `f.`, `sp.`, `cf.`, `aff.`).
  Plan: `doc/sesja_A_kursywa.md`. **Zrobione** — lista *Italic mode* w panelu
  *Tip Labels*, tryb *Until first number* jest domyślny.
- [x] 6.2 🟢 **Szablon etykiet — usunięty.** Decyzja użytkowniczki 2026-08-23:
  tryby *Italic mode* z 6.1 załatwiają sprawę, więc pole *Advanced template*
  zniknęło z panelu *Tip Labels* razem z całą obsługą szablonów w kodzie
  i opisem `doc/szablon_etykiet.md`.
- [x] 6.3 🟡 **Group Bars nic nie pokazują** — bo liście nie mają atrybutu
  „rodzina". FigTree nie zna rodzin; trzeba je podać. Zrobione: poprawiony import
  z pliku `nazwa<TAB>rodzina` (*File → Import Annotations…*, z podsumowaniem
  dopasowań) + przycisk *Assign to selection…* w panelu *Group Bars*.
  Plan: `doc/sesja_B_group_bars.md`. Pliki testowe: `doc/przyklad.tree`,
  `doc/przyklad_rodziny.tsv`. **Czeka na przeklikanie.**
- [x] 6.4 🟢 **Cofanie presetu publikacyjnego** — przed zastosowaniem zapamiętać
  poprzednie ustawienia; przycisk zmienia się w *Undo preset*. Plan:
  `doc/sesja_C_preset_undo.md`. **Zrobione** — przycisk *Publication preset* w
  panelu *Appearance* działa jak przełącznik: pierwsze kliknięcie zapamiętuje
  pełne ustawienia i nakłada preset (napis zmienia się na *Undo preset*),
  drugie przywraca zapamiętaną kopię. Kopia znika po wczytaniu innego drzewa.
- [x] 6.5 🟢 **Etap 5 (mysz/klawiatura)** — plan: `doc/sesja_D_mysz.md`. **Zrobione**
  — patrz uwagi przy 5.1–5.3 wyżej.

- [x] 6.6 🟡 **Min tip spacing nie wystarcza przy drzewach „drabinkowych"** —
  zgłoszone 2026-08-23 ze zrzutem ekranu: mimo Min tip spacing = 30, etykiety
  poparcia (bootstrap) nadal się nakładają w miejscach, gdzie drzewo dokłada
  po jednym takson na raz (długi łańcuch kladów 2-elementowych, np.
  *Lamproderma aeneum* Ron2281 → Ron1658 → Ron2295 → …). Przyczyna: Min tip
  spacing pilnuje odstępu między **liśćmi**, a węzły wewnętrzne (na których
  wisi numer poparcia) leżą tam, gdzie wypada środek między dziećmi — przy
  takiej topologii kilka węzłów wewnętrznych mieści się w przestrzeni węższej
  niż wysokość samej etykiety liczby, niezależnie od odstępu liści. Plan:
  `doc/sesja_E_min_node_spacing.md`. **Zrobione** — w polu *Crowded values* na
  dole panelu *Node Labels* są dwa checkboxy: *Avoid overlap* (kolidująca
  wartość poparcia odsuwana w lewo nad własną gałąź; jeśli daleko — cienka
  szara linia wskazuje jej węzeł) i *White backing* (prostokąt w kolorze tła
  pod każdą liczbą, żeby kreski gałęzi nie przecinały cyfr; można odznaczyć,
  jeśli gałęzie mają zostać w całości czarne). Drzewo samo nie jest ruszane.
  Klucze w pliku `.tree`: `nodeLabels.avoidOverlap`, `nodeLabels.labelBacking`.
  Uzupełnienie 2026-09-02: *Avoid overlap* omija teraz także **nazwy okazów**
  (tip labels) i etykiety gałęzi, nie tylko inne wartości poparcia — zgłoszone
  ze zrzutami: liczby lądowały na nazwach taksonów przy różnym rozciągnięciu
  drzewa i trzeba je było rozsuwać w Corelu.
  Iterowane na żywo na zrzutach ekranu (v1 zsuwanie w dół — odrzucone, v2
  w lewo + linie, v3 tło, v4 tło jako osobny checkbox); szczegóły w planie
  sesji E.

  Przy okazji wyszło coś ważniejszego niż sama opcja: pierwotny zrzut ekranu
  pokazywał **zły plik** — `ciemnozarodnikowe_v3_modeltest.tree` to wynik testu
  modelu, w którym **wszystkie gałęzie mają sztuczną długość `0.1`**, stąd
  idealne „schodki". Drzewo do ryciny to
  `ciemnozarodnikowe_calosc_ML_v3.raxml.support` (prawdziwe długości gałęzi
  i bootstrap jako etykieta węzła — w panelu *Node Labels* → *Display*
  wybrać `bootstrap`, nie `Node ages`). Samo otwarcie właściwego pliku
  usunęło większość problemu.

- [x] 6.7 🟡 **Słowniki grup: dopasowanie po rodzaju + szablon do wypełnienia** —
  zgłoszone 2026-08-23: pisanie pliku `nazwa<TAB>rodzina` dla setek okazów to
  strata czasu, a rodzina i rząd zależą od **rodzaju**, czyli pierwszego członu
  nazwy liścia. **Zrobione 2026-08-28** (impuls: import `sluzowce.tsv` do
  prawdziwego drzewa dał „Matched 0 of 63"): (a) przy imporcie nazwa
  **jednowyrazowa** (bez `_` i spacji), która nie pasuje do żadnej pełnej nazwy
  okazu, jest traktowana jako rodzaj i dostają ją wszystkie okazy o nazwie
  zaczynającej się od tego rodzaju; podsumowanie osobno liczy takie dopasowania
  i objęte nimi okazy; wiersz z pełną nazwą zawsze nadpisuje wartość z rodzaju,
  niezależnie od kolejności w pliku. Nazwy wielowyrazowe celowo **nie** spadają
  na rodzaj — brakujący w drzewie okaz nie rozleje swoich wartości na cały
  rodzaj. (b) *File → Export Group Template…* — zapis alfabetycznej listy
  rodzajów otwartego drzewa z pustymi kolumnami `family`/`order` (zapis przez
  plik tymczasowy, jak przy Save). Zmiany: `FigTreeFrame.java`
  (`applyAnnotationTable`, `doExportGroupTemplate`, `genusOf`, `isSingleWord`),
  nowa pozycja w obu fabrykach menu File. Dokumentacja: `doc/instrukcja.md` §9,
  `doc/slowniki/README.md`. **Czeka na przeklikanie.**
- [ ] 6.8 🟡 **Kolory grup: przycisk *Colours…* w panelu Group Bars** — zgłoszone
  2026-08-23 przy przeklikiwaniu 6.3. Miało być tak, że kolory ustawia się
  w *Appearance* → *Colour by* → *Setup: Colours*, ale ta lista **w ogóle nie
  pokazuje atrybutów liści**: `AttributeComboHelper` bez `PainterIntent` chodzi
  tylko po `tree.getNodes()`, a `family` z importu siedzi na obiektach `Taxon`.
  Czyli dziś kolorów grup nie da się wybrać w ogóle — paski biorą wbudowaną
  paletę pastelową. Do zrobienia: przycisk otwierający `DiscreteColourScaleDialog`
  dla atrybutu pasków wprost w panelu *Group Bars* (i/albo dopuszczenie atrybutów
  liści w *Colour by*). Uwaga na `setupControls()` — blokuje indeks 0 combo,
  a w *Group Bars* pod zerem jest już prawdziwy atrybut.
- [ ] 6.9 🔴 **DO ZROBIENIA PRZEZ UŻYTKOWNICZKĘ: sprawdzić słownik śluzowców** —
  `doc/slowniki/sluzowce.tsv` (63 rodzaje) napisał Claude z pamięci i **wymaga
  weryfikacji specjalistki**, zanim pójdzie na rycinę do publikacji. Poprawki
  wpisywać wprost w pliku (zwykły tekst, otwiera się w Excelu i Notatniku).
  Rodzaje o spornym umiejscowieniu mają komentarz `#` z alternatywą — pierwsze
  do sprawdzenia: *Perichaena* (Trichiaceae czy Arcyriaceae), *Prototrichia*,
  *Listerella*, *Diachea*, *Amaurochaete* i *Brefeldia*, *Lamproderma* +
  *Diacheopsis* + *Leptoderma*, *Colloderma*, *Elaeomyxa*, *Ceratiomyxa*,
  *Enteridium*. Przyjęty układ: tradycyjny (Martin & Alexopoulos / Lado).
- [ ] 6.10 🟢 **Instrukcja obsługi** — `doc/instrukcja.md`: krótko, co robi każda
  opcja dodana w forku, jak uruchamiać program i jak używać plików towarzyszących
  (`przyklad.tree`, `przyklad_rodziny.tsv`, `doc/slowniki/`). Pierwsza wersja
  napisana 2026-08-23; do dokończenia zrzuty ekranu, przykłady „przed / po",
  przepis na typową pracę i rozdział o częstych problemach. Plan:
  `doc/sesja_F_instrukcja.md`.
- [x] 6.11 🟡 **Ręczna poprawka pojedynczego liścia** — zgłoszone 2026-08-23 ze
  zrzutem: `Trichia sordida holotypus` idzie w całości kursywą, bo w nazwie nie
  ma numeru, na którym reguła mogłaby się zatrzymać. **Zrobione**: przycisk
  **Style selected tips…** w panelu *Tip Labels* — kursywa człon po członie
  i pogrubienie całej nazwy dla zaznaczonych liści, *Back to rules* kasuje
  poprawkę. Ustawienie siedzi na węźle (`!labelItalic`, `!labelBold`) i zapisuje
  się z drzewem. (Atrybuty taksonów też się zapisują — w bloku `taxa`; moja
  wcześniejsza notatka, że giną, była błędna.)
- [x] 6.12 🟢 **Zmiana podpisu liścia (*Tree → Annotate… → Name*) nie działała** —
  zgłoszone 2026-08-23. Przyczyna: *Annotate* zapisuje `!name` na **taksonie**, gdy
  zaznaczona jest etykieta liścia, ale na **węźle**, gdy zaznaczony jest węzeł —
  a `BasicLabelPainter.getRawName()` dla liści zaglądał wyłącznie do taksonu.
  W efekcie zmiana nazwy „nic nie robiła" przy jednym ze sposobów zaznaczania.
  **Zrobione**: dla liści sprawdzany jest też `!name` z węzła.
- [x] 6.13 🔴 **Nieudany zapis kasował plik z drzewem** — awaria 2026-08-25.
  Jar został przebudowany, gdy program był otwarty; działająca kopia przestała
  wczytywać nowe klasy, więc *Annotate*, *Export Trees* i *Save* umarły bez
  komunikatu — a `writeToFile()` zdążyło wyczyścić plik docelowy przed awarią
  (`ML_SmE.raxml.support` → 0 bajtów; odtworzone RAxML-em z `.bestTree`
  + `.bootstraps`). **Zrobione**: zapis przez plik tymczasowy z podmianą po
  sukcesie, komunikat błędu zamiast cichej śmierci (także w *Export Trees*
  i *Annotate*), oraz zasada w `CLAUDE.md`: przed kompilacją zamknąć program.
- [x] 6.14 🟢 **Publication preset = zestaw użytkowniczki** — podany 2026-08-25 po
  pierwszej prawdziwej rycinie: order decreasing, Times 11/9 pt, poparcie ≥ 70 nad
  gałęzią z Avoid overlap i White backing, `holotypus paratypus isotypus` prosto
  i pogrubione. Kropki na węzłach i wyrównanie nazw świadomie **wyrzucone**
  z presetu. Dodatkowo `findSupportAttribute()` sprawdza `bootstrap`, `support`
  i `label`, bo nazwa atrybutu zależy od tego, co użytkowniczka wpisze przy imporcie.
- [x] 6.15 🟢 **„Clear Hilighting" nie do kliknięcia** — zgłoszone 2026-08-25:
  po podświetleniu kladu nie dało się tego cofnąć. Przyczyna: pozycja menu
  *Tree → Clear Hilighting…* była włączana tylko przy zaznaczeniu
  (`clearHilightingAction.setEnabled(hasSelection)`), a `clearHilightedNodes()`
  bez zaznaczenia czyści **wszystkie** podświetlenia — czyli jedyna droga
  „skasuj wszystko" była wyszarzona dokładnie wtedy, gdy była potrzebna.
  **Zrobione**: pozycja zawsze aktywna.
- [x] 6.16 🟡 **Przesuwanie pojedynczej wartości poparcia** — zgłoszone 2026-08-25:
  *Offset X/Y* rusza wszystkie etykiety naraz, a zawsze trafi się jedna, którą
  trzeba odsunąć osobno. **Zrobione**: przycisk **Move selected label…** w panelu
  *Node Labels*, przesunięcie w punktach na zaznaczonych węzłach, zapisywane jako
  `!labelDX` / `!labelDY`. Etykieta ruszona ręcznie jest pomijana przez
  *Avoid overlap* (ale liczy się dla niego jako przeszkoda).
  Uzupełnienie 2026-09-02: klik w samą liczbę zaznacza dokładnie jej węzeł
  (w każdym trybie zaznaczania) — wcześniej dawało się kliknąć tylko gałąź,
  co w trybie *Clade* łapało wszystkie liczby kladu naraz.
  Uzupełnienie 2026-09-02 (2): **przeciąganie etykiety myszą zrobione** —
  najechanie na liczbę pokazuje kursor ✥, wciśnięcie i przeciągnięcie przenosi
  ją na żywo, puszczenie zapisuje `!labelDX`/`!labelDY` (te same atrybuty co
  okienko). Przeciągnięcie z powrotem w okolicę punktu wyjścia (±0.5 pt) kasuje
  przesunięcie. Start przeciągania uwzględnia odsunięcie, które wcześniej nadał
  *Avoid overlap*, więc liczba nie skacze przy złapaniu.
  Uzupełnienie 2026-09-02 (3): **strzałki dosuwają zaznaczone wartości** —
  feedback po wypróbowaniu przeciągania („fajna opcja, ale czasem mało
  precyzyjna"). Klik w liczbę + strzałki: 1 pt na naciśnięcie, z Shift 10 pt,
  działa na wielu zaznaczonych naraz; bez zaznaczenia strzałki dalej przewijają
  widok (zdarzenie jest konsumowane tylko, gdy jakaś etykieta faktycznie się
  ruszyła).
- [x] 6.17 🔴 **Przecinek dziesiętny psuł każdy zapisany plik** — awaria
  2026-08-25: `Save` zapisywał długości gałęzi jako `0,001243` (polska
  lokalizacja), a własny importer się na tym wykładał (*Taxon in tree,
  '00124300' is unknown*). W `FigTreeApplication.main()` autor oryginalu zostawil
  zakomentowana poprawke `Locale.setDefault(Locale.US)`. **Zrobione**: włączona
  jako `Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT)` — liczby z kropką,
  język interfejsu bez zmian. Uszkodzony plik użytkowniczki naprawiony skryptem
  i zweryfikowany importerem.
- [x] 6.18 🟢 **Własny zapis presetu publikacyjnego** — zgłoszone 2026-09-02:
  zestaw z 6.14 był na stałe w kodzie, a użytkowniczka chce go móc odświeżać
  bez programowania. **Zrobione**: przycisk **Save current as preset** w panelu
  *Appearance* zapamiętuje bieżący stan wszystkich paneli w pliku
  `MyFigTree_publication_preset.txt` (folder domowy); *Publication preset*
  odtwarza ten plik, a bez niego — wbudowany zestaw z 2026-08-25 (powrót:
  **Back to original preset** w okienku zapisu). Pomijane ukorzenienie,
  atrybut poparcia wykrywany na nowo per drzewo. Zapis przez plik tymczasowy.

---

## Do zrobienia przez użytkowniczkę (stan na 2026-08-25, wieczór)

Lista spisana na jej prośbę na koniec długiej sesji. Nic z tego nie wymaga
programowania — to przeklikanie i decyzje. Kolejność od najważniejszego.

### 1. Sprawdzić odzyskane drzewo — najpierw to

Otworzyć **`ML_SmE_odtworzony_naprawiony.tree`**
(`Pulpit\jasno sierpień 2026\jasno_wielogenowe\v1\`) i zobaczyć, czy rycina
wygląda tak, jak została zostawiona o 17:29: Times 11 pt, poparcie ≥ 70 nad
gałęzią, order decreasing, `Trichia sordida HOLOTYPE`.

Sprawdzone importerem programu (383 taksony, 142 ustawienia wczytują się), ale
**nikt tego jeszcze nie widział na ekranie**. Jeśli czegoś brakuje — zgłosić.

W tym samym folderze został pusty `ML_SmE.raxml.support` (0 bajtów, po awarii
zapisu) — do skasowania, gdy naprawiony plik okaże się w porządku.

### 2. Poprawić `xholotypus` w polu *Not italic words*

W zapisanym pliku jest `... aff nov xholotypus paratypus isotypus` — brakuje
spacji między `x` a `holotypus`, więc program widzi jedno słowo i **ani `x`, ani
`holotypus` nie są rozpoznawane**. Na rycinie tego nie widać, bo holotyp został
przemianowany na `HOLOTYPE`. Wystarczy dopisać spację albo kliknąć
*Publication preset*, który wpisuje poprawną listę.

### 3. Przeklikać to, co zrobione, ale nieoglądane

- [ ] **Publication preset** (6.14) — czy jednym kliknięciem daje to, co dotąd
  ustawiała ręcznie. Można go bezpiecznie wypróbować na gotowej rycinie:
  przycisk zmienia się wtedy w *Undo preset* i cofa wszystko.
- [ ] **Move selected label…** (6.16) — przesuwanie **jednej** wartości poparcia:
  kliknąć samą liczbę (od 2026-09-02 to zaznacza dokładnie jej węzeł), przycisk
  w panelu *Node Labels*, wpisać przesunięcie w punktach.
  Sprawdzić, czy wpisywanie liczb jest znosne — jeśli męczące, do zrobienia
  przeciąganie myszą (patrz 6.16 wyżej).
- [ ] **Clear Hilighting** (6.15) — *Tree → Clear Hilighting…* działa teraz zawsze:
  bez zaznaczenia czyści wszystkie podświetlenia, z zaznaczonym kladem — tylko jego.
- [ ] **Style selected tips…** (6.11) — ręczna kursywa/pogrubienie na pojedynczym
  liściu. Zrobione, ale ani razu nieużyte (w praktyce wystarczyło *Not italic words*).
- [ ] **Group Bars** (6.3) — paski grup i import rodzin z pliku. Zrobione
  2026-08-23, nadal nieprzeklikane. Pliki testowe: `doc/przyklad.tree`,
  `doc/przyklad_rodziny.tsv`.

### 4. Decyzje dla mnie (jedno zdanie wystarczy)

- [ ] **Wyróżnienie typów**: preset wpisuje teraz **obie** pisownie — `holotypus,
  paratypus, isotypus` i `HOLOTYPE, PARATYPE, ISOTYPE`. Zostawić obie czy tylko
  wielkie litery, skoro tak wyglądają nazwy z FASTA?
- [ ] **Wartości w presecie** — gdyby po kilku rycinach okazało się, że coś nie
  pasuje (próg 70, 11/9 pt, order decreasing), zmiana to jedna liczba.

### 5. Sprawdzić słownik śluzowców — patrz 6.9

`doc/slowniki/sluzowce.tsv` (63 rodzaje) napisał Claude z pamięci i **wymaga
weryfikacji specjalistki**, zanim pójdzie na rycinę do publikacji.

### Na przyszłość, żeby uniknąć dzisiejszych kłopotów

- **Zamykać program, zanim Claude kompiluje.** Podmiana `figtree.jar` pod
  działającą kopią psuje ją po cichu (menu przestają otwierać okienka, zapis
  milczy). Claude ma to zapisane w `CLAUDE.md` i ma o to prosić — ale warto
  wiedzieć, skąd się biorą takie objawy.
- **Zapisywać pod nową nazwą** (*File → Save As…*), gdy rycina jest ważna.
  Od 6.13 nieudany zapis nie kasuje już starego pliku, ale osobna kopia i tak
  nie zaszkodzi.
- Drzewo ML z RAxML-a zawsze da się odtworzyć z `.bestTree` + `.bootstraps` —
  przepis w `doc/instrukcja.md`, rozdział 15.
