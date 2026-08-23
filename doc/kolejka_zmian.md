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
  zwykłego FigTree. *(zmiana jest w plikach, jeszcze nie zacommitowana)*
- [ ] 0.2 🟢 Pierwszy commit forka: CLAUDE.md, build.bat, run.bat, ta kolejka.

---

## Etap 1 — nazwy okazów (podkreślniki, prefiksy)

Dziś: nazwy z FASTA wyglądają jak `Dianema_depressum_MA80673`, a na rycinie chcesz
*Dianema depressum* MA80673.

Gdzie w kodzie: `src/figtree/treeviewer/painters/BasicLabelPainter.java`,
metoda `getLabel()` — tu decyduje się, jaki tekst trafia na etykietę. Ważne:
zmieniamy tylko **wyświetlanie**, plik z drzewem zostaje nietknięty.

- [ ] 1.1 🟢 **Opcja „zamień `_` na spację"** w panelu *Tip Labels*
  (checkbox). Najprostsza, natychmiast widoczna zmiana.
- [ ] 1.2 🟢 **Ukrywanie prefiksów / sufiksów.** Pole tekstowe w panelu, gdzie
  wpisujesz co wyciąć (np. `EBOV|`, `_contig1`). Może być kilka wzorców
  oddzielonych przecinkiem. Opcjonalnie: pole na wyrażenie regularne dla
  bardziej zaawansowanych przypadków.
- [ ] 1.3 🟢 **Zapamiętywanie tych ustawień w pliku `.tree`** (blok FigTree w
  NEXUS-ie, pliki `FigTreeNexusImporter/Exporter.java`), żeby po ponownym
  otwarciu drzewa nie ustawiać wszystkiego od nowa.
- [ ] 1.4 🟡 **Podział nazwy na części** (np. `rodzaj gatunek numer` po spacjach
  lub `|`) — fundament pod etap 3, gdzie każda część dostanie własny styl.

---

## Etap 2 — nachodzące etykiety i „ściśnięte" gałęzie (drzewa ML)

Dziś: nazwy liści i wartości bootstrap kolidują ze sobą; poprawiane ręcznie
w Corelu.

Gdzie w kodzie: `BasicLabelPainter.java` (pozycja tekstu względem gałęzi),
`src/figtree/treeviewer/treelayouts/RectilinearTreeLayout.java` (odstępy
między gałęziami), `TreePane.java` (rysowanie całości).

- [ ] 2.1 🟢 **Większy zakres odstępu etykiety od gałęzi** („padding") dla
  *Node Labels* / *Branch Labels* — osobno w poziomie i w pionie, żeby
  bootstrap dało się odsunąć od linii i od nazw.
- [ ] 2.2 🟢 **Pozycja wartości poparcia względem gałęzi**: nad / pod / na
  końcu gałęzi przy węźle (na wzorze: pod gałęzią, tuż przed węzłem,
  dwie wartości jedna nad drugą — bootstrap i PP).
- [ ] 2.3 🟢 **Próg wyświetlania poparcia** — pokazuj wartość tylko gdy
  ≥ X (np. 50). Mniej etykiet = mniej kolizji, i tak standard w publikacjach.
- [ ] 2.4 🟡 **Dwie wartości na węźle naraz** (np. `bootstrap / PP` albo jedna
  nad drugą, jak na wzorze) bez sklejania ich ręcznie w pliku.
- [ ] 2.5 🟡 **Minimalny odstęp pionowy między liśćmi** — ustawienie, które
  automatycznie „rozciąga" drzewo tak, żeby etykiety liści się nie nakładały
  przy danej czcionce (zamiast ręcznego kręcenia suwakiem *Expansion*).
- [ ] 2.6 🔴 **Automatyczne unikanie kolizji** etykiet węzłów: program sprawdza,
  czy prostokąt tekstu nachodzi na inny, i przesuwa go (w górę/dół, na drugą
  stronę gałęzi). To już prawdziwy algorytm — zrobić dopiero, gdy 2.1–2.5
  nie wystarczą.

---

## Etap 3 — formatowanie tekstu etykiet (wzór: `doc/wzor.png`)

Dziś: jedna czcionka, jeden kolor, jeden styl na całą etykietę. Na wzorze:
*Dianema depressum* kursywą, `MA80673` prosto; rodziny WIELKIMI LITERAMI
z rozstrzeleniem.

- [ ] 3.1 🟢 **Styl dla części nazwy**: po podziale z 1.4 — część 1–2 (rodzaj,
  gatunek) kursywą, reszta prosto. Na początek sztywno „pierwsze N słów
  kursywą", potem konfigurowalnie.
- [ ] 3.2 🟢 **Wielkość liter**: bez zmian / WIELKIE / małe / Jak W Zdaniu —
  per część nazwy.
- [ ] 3.3 🟡 **Pogrubienie i kolor per część nazwy** (np. numer okazu szary,
  nazwa czarna).
- [ ] 3.4 🟡 **Podświetlenie wybranych liści** (np. nowe okazy pogrubione lub
  w ramce) — przez atrybut w drzewie albo listę nazw.
- [ ] 3.5 🔴 **„Szablon etykiety"** — jedno pole, gdzie piszesz np.
  `{1 kursywa} {2 kursywa} {3 wielkie}`, i etykieta się z tego składa. To
  docelowa, ogólna wersja 3.1–3.3; zrobić gdy proste opcje przestaną wystarczać.

---

## Etap 4 — wygląd drzewa „jak z publikacji"

- [ ] 4.1 🟢 **Kropki na węzłach z wysokim poparciem** (na wzorze: czarne
  kropki przy bootstrap ≥ próg). FigTree ma *Node Shapes*, ale nie da się ich
  warunkować progiem — dodać próg.
- [ ] 4.2 🟢 **Domyślne ustawienia „do publikacji"** — przycisk / preset:
  czcionka, grubość linii, wyrównanie etykiet liści, brak tła itd. Zamiast
  klikać 15 rzeczy przy każdym drzewie.
- [ ] 4.3 🟡 **Pionowe paski grup po prawej** (rodziny, rzędy — jak A/B/C/D/E
  na wzorze) z nazwą obróconą o 90°. FigTree umie kolorować klady, ale nie
  rysuje takich pasków.
- [ ] 4.4 🟡 **Kolorowe tła za kladami** (pastelowe prostokąty jak na miniaturze
  w rogu wzoru).
- [ ] 4.5 🟢 **Łamana gałąź „//"** dla bardzo długich gałęzi (na wzorze:
  *Lycogala* i *Reticularia lycoperdon*), żeby outgroup nie ściskał reszty.
- [ ] 4.6 🟢 **Eksport: większa kontrola nad PDF/SVG** — rozmiar strony,
  marginesy, czcionki osadzone, żeby wynik nie wymagał poprawek w Corelu.

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
