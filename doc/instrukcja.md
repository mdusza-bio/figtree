# MyFigTree MD — instrukcja

Krótki opis tego, co robi każda opcja **dodana w tym forku**. Opcje odziedziczone
po zwykłym FigTree działają jak dotąd i nie są tu opisane.

> **Stan: w budowie.** Powstaje po kawałku, razem z kolejnymi zmianami w programie.
> Zrzuty ekranu i przykłady „przed / po" dojdą później — zadanie 6.10 w
> [kolejce zmian](kolejka_zmian.md).

---

## 1. Uruchamianie

Program to jeden plik `figtree.jar` w folderze `dist`. Najprościej kliknąć
**`run.bat`** w folderze `C:\Users\Magdalena Dusza\workspeace\my_figtree`.

Po zmianach w kodzie trzeba go najpierw zbudować — **`build.bat`** w tym samym
folderze (albo polecenie `/compile` w rozmowie z Claude'em). Budowanie trwa
kilka sekund i kończy się napisem `BUILD SUCCESSFUL`.

Tytuł okna to **MyFigTree MD** — po tym poznajesz, że to ta wersja, a nie zwykły
FigTree zainstalowany obok.

## 2. Pliki towarzyszące

| Plik | Co to jest | Jak użyć |
|---|---|---|
| `doc/przyklad.tree` | drzewko testowe, 8 okazów śluzowców | *File → Open* |
| `doc/przyklad_rodziny.tsv` | rodziny i rzędy do tego drzewka | *File → Import Annotations…* |
| `doc/slowniki/sluzowce.tsv` | słownik rodzaj → rodzina → rząd (63 rodzaje) | *File → Import Annotations…* (dopasowanie po rodzaju), patrz [slowniki/README.md](slowniki/README.md) |

Wszystkie te pliki to zwykły tekst — otwierają się w Notatniku i w Excelu.

### Który plik z RAxML-a otwierać do ryciny

RAxML zostawia w folderze kilkanaście plików o podobnych nazwach i łatwo otworzyć
niewłaściwy. Do ryciny służy **`.raxml.support`**:

| Plik | Co zawiera | Do ryciny? |
|---|---|---|
| `*.raxml.support` | drzewo ML z prawdziwymi długościami gałęzi **i wartościami bootstrap** | **tak** |
| `*.raxml.bestTree` | to samo drzewo, ale **bez** wartości poparcia | nie |
| `*_modeltest.tree` | drzewo pomocnicze z testu modelu; **wszystkie gałęzie mają sztuczną długość `0.1`** | nie |

Po czym poznać pomyłkę: jeśli drzewo wygląda jak równiutkie **schodki**, a gałęzie
mają identyczną długość, to prawie na pewno plik z testu modelu — długości gałęzi
nie są prawdziwe, więc taka rycina nic nie znaczy.

Po otwarciu `.raxml.support` trzeba jeszcze w panelu *Node Labels* ustawić
**Display: bootstrap** (samo `label` albo `Node ages` to nie to).

## 3. Panel *Layout*

- **Min tip spacing** — najmniejszy odstęp w pionie między nazwami liści (w punktach).
  Drzewo samo się wydłuża, żeby nazwy się nie nakładały. Działa też przy eksporcie
  do PDF. Uwaga: pilnuje odstępu **liści**, więc wartości poparcia na węzłach
  wewnętrznych mogą nadal kolidować — na to jest osobna opcja **Avoid overlap**
  w panelu *Node Labels* (rozdział 6).
- **Shorten branches longer than** — bardzo długie gałęzie (np. do outgrupy) są
  skracane i oznaczane `//`. Uwaga: skrócona gałąź nie odpowiada już skali. Wartość wpisać z kropka, przecinek nie działa, np. 0.2
- **Shorten selected branch: 2x / 3x / Full** — skraca **jedną wybraną gałąź**
  dokładnie dwa albo trzy razy i pisze nad znakiem `//` „2x” albo „3x”, tak jak
  robią to autorzy w artykułach. Dzięki temu czytelnik wie, ile razy gałąź jest
  naprawdę dłuższa, niż wygląda.
  - Jak: kliknij gałąź w drzewie (*Selection Mode: Node*) albo nazwę okazu
    (wtedy chodzi o gałąź prowadzącą do tego okazu) i kliknij **2x** lub **3x**.
    **Full** przywraca pełną długość.
  - Gdy zaznaczony jest cały klad, skracana jest tylko gałąź prowadząca do
    kladu, nie wszystkie gałęzie w środku.
  - **Write 2x / 3x above //** — włącza i wyłącza napis nad znakiem;
    **2x / 3x size** — wielkość tego napisu.
  - Skrócenie jest zapisane przy samej gałęzi w pliku `.tree`, więc zostaje po
    ponownym otwarciu i po zmianie korzenia.
  - Różnica wobec *Shorten branches longer than*: tamta opcja przycina
    **wszystkie** długie gałęzie do jednej długości i nie podaje, ile razy;
    ta skraca wybraną gałąź o znaną, okrągłą krotność. Można używać obu naraz —
    gałąź z własnym 2x / 3x nie jest już dodatkowo przycinana.
  - Skala pod drzewem nie dotyczy skróconych gałęzi — warto to napisać w
    podpisie ryciny.

## 4. Panel *Appearance*

- **Publication preset** — jedno kliknięcie ustawia cały Twój stały zestaw
  (ustalony 2026-08-25):
  - *Trees*: **Order nodes: decreasing**,
  - *Appearance*: białe tło, czarne linie 1 pt,
  - *Tip Labels*: Times New Roman **11 pt**, kursywa do numeru okazu, podkreślniki
    na spacje, `holotypus paratypus isotypus` pismem prostym **i pogrubione**
    (wyróżnienie typów na rycinie),
  - *Node Labels*: poparcie **9 pt nad gałęzią**, tylko **≥ 70**, **Avoid overlap**
    + **White backing**,
  - *Layout*: prostokątny.

  Preset **nie rusza** kropek na węzłach (*Node Shapes*) ani wyrównania nazw do
  prawej (*Align tip labels*) — zostają takie, jakie sobie ustawisz.
- Po jego użyciu przycisk zmienia się w **Undo preset** — cofa wszystkie
  ustawienia do stanu sprzed kliknięcia. Zapamiętany stan dotyczy tego jednego
  drzewa; po wczytaniu innego przycisk wraca do zwykłej postaci.
- **Save current as preset** — gdy zestaw z 2026-08-25 się zdeaktualizuje, nie
  trzeba nic programować: ustaw wszystko ręcznie w panelach tak, jak ma być,
  i kliknij ten przycisk. Od tej pory **Publication preset** będzie przywracał
  właśnie ten zapamiętany stan (całość: czcionki, kolory, układ, także kropki
  na węzłach i wyrównanie nazw — dokładnie to, co widać w chwili zapisu).
  Szczegóły:
  - zapamiętywane jest wszystko **oprócz ukorzenienia** (*Rooting*) — ono zależy
    od konkretnego drzewa, więc preset go nie przestawia; podobnie nazwa
    atrybutu z poparciem (`bootstrap`/`label`) jest za każdym razem wykrywana
    na nowo w otwartym drzewie,
  - zapis trafia do małego pliku `MyFigTree_publication_preset.txt` w Twoim
    folderze domowym (`C:\Users\Magdalena Dusza`) — przetrwa zamknięcie
    programu; można go skopiować na inny komputer albo dołączyć do kopii
    zapasowej,
  - jeśli zapisany zestaw już istnieje, okienko zapytania ma dodatkowy przycisk
    **Back to original preset** — usuwa zapamiętany zestaw i przywraca
    wbudowany styl z 2026-08-25,
  - najechanie myszą na **Publication preset** podpowiada, który zestaw
    zadziała: własny zapisany czy wbudowany.

  **A po udostępnieniu programu innym (np. kolegom z instytutu):** nic nie
  trzeba konfigurować. Plik z zestawem nie jest częścią programu i nie musi
  istnieć — u nowej osoby *Publication preset* po prostu stosuje wbudowany
  styl z 2026-08-25. Plik powstaje dopiero, gdy ktoś sam kliknie
  *Save current as preset* — u każdego użytkownika osobno, w **jego** folderze
  domowym (ścieżka jest brana z systemu, nie wpisana na sztywno), więc zestawy
  różnych osób się nie mieszają, nawet na wspólnym komputerze z osobnymi
  kontami Windows. Udostępniając program, przekazujesz sam folder z
  `figtree.jar` — swojego pliku z zestawem **nie** dołączasz. A jeśli ktoś ma
  dostać dokładnie Twoje ustawienia: kopiujesz mu Twój
  `MyFigTree_publication_preset.txt` do jego `C:\Users\<nazwa>` i przy
  najbliższym kliknięciu *Publication preset* program go podchwyci.
- **Colour by** — kolorowanie gałęzi według atrybutu **węzłów** (np. `bootstrap`).
  Uwaga: nie widać tu atrybutów przypisanych do liści (jak `family` z importu) —
  te są na razie dostępne tylko w panelach etykiet i w *Group Bars* (zadanie 6.8).

## 5. Panel *Tip Labels* (nazwy okazów)

Czyszczenie nazwy:
- **Replace '_' with space** — podkreślniki z nagłówków FASTA zamieniane na spacje.
  Zmienia tylko to, co widać na ekranie i na rycinie; plik z drzewem zostaje
  nietknięty. Domyślnie włączone.
- **Hide parts** — fragmenty do wycięcia z nazwy, po przecinku (np. `EBOV|,_contig1`).
  Tnie dosłownie i **wszędzie** — każde wystąpienie wpisanego tekstu znika,
  także ze środka nazwy.
- **Hide regex** — to samo, ale wyrażeniem regularnym (wzorcem), którym można
  doprecyzować, **gdzie** wycinać. Błędne wyrażenie jest po prostu ignorowane
  (nazwa zostaje nietknięta).
- Który wybrać — na przykładzie ukrycia `_new` z końcówki nazwy:
  - *Hide parts* z wpisem `_new` utnie `_new` też ze środka — z
    `Arcyria_newtoniana_MA123` zrobi się po cichu `Arcyriatoniana MA123`;
  - *Hide regex* z wpisem `_new$` usunie `_new` **tylko na końcu nazwy**
    (znak `$` znaczy „koniec nazwy") — i to jest właściwe narzędzie tutaj.

  Reguła kciuka: zwykłe „wytnij ten napis" → *Hide parts*; „wytnij, ale tylko
  na końcu / tylko w konkretnej sytuacji" → *Hide regex*. W obu polach pisz
  z podkreślnikiem (`_new`, nie ` new`) — wycinanie działa na surowej nazwie
  z pliku, zanim podkreślniki zamienią się w spacje. Uwaga: w regexie znaki
  `. | ( ) [ ] $ ^ * + ?` mają specjalne znaczenie (np. `|` znaczy „albo") —
  dlatego fragment z kreską, jak `EBOV|`, wycina się przez *Hide parts*.

Kursywa i skład nazwy:
- **Italic mode** — sposób wybierania, co ma być kursywą:
  - *Off* — nic,
  - *First N parts* — pierwsze N członów nazwy (pole **Italic first N parts**),
  - *Until first number* (domyślne) — kursywa aż do pierwszego członu wyglądającego
    na numer okazu. Dzięki temu *Trichia lutescens* MA83355 i *Trichia sordida*
    var. *sordidoides* MA12345 wychodzą dobrze bez zmiany ustawień.
- **Not italic words** — słowa, które zostają pismem prostym w środku nazwy
  (domyślnie `var subsp ssp f sp cf aff nov x`). Nie przerywają kursywy dla
  dalszych członów.
- **Add dot** — dopisuje kropkę: `var` → `var.`, `sp` → `sp.` (w nagłówkach FASTA
  kropek zwykle nie ma). Kropkę dostają tylko **skróty, do 5 liter** — słowo
  wypisane w całości, np. `holotypus`, zostaje bez kropki. Znak krzyżówki `x`
  też kropki nie dostaje.
- **ALL CAPS** — kończy kursywę także na członie pisanym samymi wielkimi literami
  bez cyfr, np. `KRAM` w numerze `KRAM M-1234`.
- **Join number** — skleja numer okazu rozbity przez podkreślniki w postać, w której
  się go cytuje: `KRAM M 1156` → `KRAM M-1156`, `UK100 1b` → `UK100-1b`,
  `Ron324 2 3` → `Ron324 2/3`. Nazwy, które mają już myślnik, zostają bez zmian.
- **Dot after codes** — lista skrótów kolekcji cytowanych z kropką, domyślnie `CA`
  (`UARK CA. 6-131`). Puste pole = żadnych kropek.

Ręczna poprawka pojedynczego liścia (gdy reguły się mylą):
- **Style selected tips...** — klikasz w drzewie liść, który wyszedł źle
  (Shift+klik, żeby zaznaczyć kilka), naciskasz ten przycisk i w okienku
  odznaczasz **italic** przy tych członach nazwy, które mają być pismem prostym.
  Klasyczny przypadek: `Trichia sordida holotypus` — reguła robi kursywą wszystko,
  bo w nazwie nie ma numeru, a `holotypus` powinien zostać prosto.
- **bold whole label** w tym samym okienku pogrubia całą nazwę tego liścia —
  wygodne do zaznaczenia typu na rycinie.
- Człony w okienku są pokazane tak, jak się rysują, więc sklejony numer
  (`KRAM M-2749`) jest jednym polem, nie dwoma.
- **Back to rules** kasuje ręczne ustawienie i liść wraca do ogólnych reguł.
- Ustawienie siedzi na tym konkretnym liściu i zapisuje się z drzewem
  (atrybuty `!labelItalic` / `!labelBold` w pliku `.tree`).
- Jeśli to samo słowo wraca w wielu nazwach (np. `holotypus`, `paratypus`),
  szybciej dopisać je do **Not italic words** niż poprawiać liść po liściu.

Wygląd:
- **Case (italic parts)** / **Case (other parts)** — wielkość liter osobno dla
  części kursywnych i pozostałych: *As is / UPPER / lower / Sentence*.
- **Bold** i kolor — osobno dla części kursywnych i pozostałych. Przycisk **x**
  czyści kolor, czyli wraca do zwykłego koloru etykiety.
- **Highlight names containing** — lista fragmentów nazw po przecinku; pasujące
  liście dostają wyróżnienie (pogrubienie / kolor) ustawione w **Highlight style**.

Opcje z oryginalnego FigTree (przy nazwach okazów zwykle niepotrzebne):
- **Format** / **Sig. Digits** — jak wypisywać **liczby** (dziesiętnie, naukowo,
  procent; ile cyfr po przecinku). Działają tylko wtedy, gdy w *Display* wybierzesz
  coś liczbowego zamiast nazw (np. *Node ages* przy drzewie datowanym). Przy
  nazwach okazów nic nie zmieniają.
- **Box Size** — działa tylko przy *Display* = **Solid box**: zamiast nazwy przy
  każdym liściu rysuje się kolorowy prostokąt (kolor wg *Colour by*), np. pasek
  kolorów regionu albo żywiciela. *Box Size* to długość tego prostokąta w punktach.

## 6. Panele *Node Labels* i *Branch Labels* (poparcie)

Oba panele umieją prawie to samo: wartość poparcia można pokazać przy węźle
(*Node Labels*) albo na gałęzi (*Branch Labels*). Do rycin używaj **Node
Labels** — tylko tam są *Position*, *Avoid overlap* i *White backing*.

**Replace '_' with space, Hide parts, Hide regex** są tu widoczne tylko wtedy,
gdy *Display* = **Names**. Te opcje czyszczą **nazwy**, a w tych panelach
prawie zawsze wyświetla się liczba (bootstrap, PP), w której nie ma czego
czyścić. *Names* pojawia się na liście tylko wtedy, gdy węzły wewnętrzne
w pliku drzewa mają wpisane **słowne** nazwy, np. `(A,B)Physaraceae:0.1` —
w drzewach z RAxML-a, IQ-TREE czy MrBayesa w tym miejscu stoją liczby.

- **Position** (tylko *Node Labels*) — *At node / Above branch / Below branch*.
- **Offset X / Offset Y** — odsunięcie etykiety od węzła i od linii gałęzi
  (w punktach, mogą być ujemne).
- **Show only if >=** — próg; wartości poniżej nie są rysowane (np. bootstrap < 50).
- **Second value** + **Layout** — druga wartość obok pierwszej (np. PP obok
  bootstrapu), w formie `a / b` albo jedna nad drugą (*stacked*).
- **Crowded values → Avoid overlap** (tylko *Node Labels*) — gdy wartość
  poparcia wypadłaby na innej wartości, **na nazwie okazu**, na etykiecie
  gałęzi albo **na pionowej linii kladu** (czarna kreska przez cyfry robiła
  z „72" nieczytelne „7|"), kolidująca liczba jest odsuwana **w lewo**, w pustą
  przestrzeń nad swoją gałęzią (tak, jak robi się to ręcznie na rycinach
  w publikacjach). Liczba zatrzymuje się ok. 2 pkt przed pionową linią, więc
  cyfry nigdy się z nią nie zlewają.
  Nazwy okazów nigdy nie są ruszane — to liczby je omijają. Jeśli liczba musi
  odjechać dalej niż o własną wysokość, od liczby do jej węzła rysowana jest
  cienka szara **linia wskazująca**, żeby nie było wątpliwości, do której
  gałęzi należy. Samo drzewo się nie zmienia — przesuwa się wyłącznie tekst;
  wartości, które się nie gryzą, zostają na miejscu.

  Obok jest drugi checkbox **White backing**: zaznaczony (domyślnie) — każda
  wartość poparcia dostaje pod spodem **prostokąt w kolorze tła**, więc na
  gęstym drzewie linie gałęzi nie przecinają cyfr (jak na rycinach
  drukowanych); odznaczony — wszystkie gałęzie zostają w całości czarne,
  nawet jeśli liczba wyląduje na kresce. Do wyboru wedle gustu.

- **Przeciąganie liczby myszą** — najprostszy sposób na przestawienie
  **jednej** wartości, gdy wyląduje w złym miejscu, a reszta drzewa wygląda
  dobrze: najedź kursorem na liczbę (kursor zmienia się w strzałki ✥), wciśnij
  lewy przycisk i **przeciągnij ją tam, gdzie ma być** — jak w Corelu, tylko od
  razu w programie. Liczba zostaje tam, gdzie ją puścisz, a pozycja zapisuje
  się z drzewem. Przeciągnięcie z powrotem w okolicę pierwotnego miejsca
  przywraca automatyczne ustawianie. Zwykłe kliknięcie (bez przeciągania)
  dalej tylko zaznacza.

- **Strzałki = precyzyjne dosuwanie** — gdy przeciąganie jest za mało dokładne:
  kliknij liczbę (to ją zaznacza), a potem naciskaj **strzałki** na klawiaturze.
  Jedno naciśnięcie przesuwa o **1 punkt**, z przytrzymanym **Shift** — o **10
  punktów**. Działa też na kilku zaznaczonych wartościach naraz (przesuwają się
  razem). Gdy nic nie jest zaznaczone, strzałki normalnie przewijają widok.

- **Linia wskazująca przy ręcznym przesunięciu** — wartość odsunięta ręcznie
  (strzałkami, myszką albo okienkiem) **dalej niż o wysokość własnej cyfry**
  dostaje automatycznie tę samą cienką szarą linię do swojego węzła, którą
  rysuje *Avoid overlap* — żeby na rycinie nie było wątpliwości, do której
  gałęzi należy. Małe dosunięcie (kilka punktów „dla oddechu") linii nie
  dostaje. Przy przeciąganiu linia pojawia się i znika na żywo, więc od razu
  widać, kiedy liczba jest już „za daleko".

- **Move selected label…** (tylko *Node Labels*) — to samo, ale z wpisywaniem
  dokładnych liczb, gdy trzeba np. przesunąć kilka etykiet o identyczny odcinek:
  1. kliknij w drzewie **samą liczbę** — to zaznacza dokładnie jej węzeł,
     niezależnie od kafelka trybu zaznaczania (*Clade*/*Node*/*Taxa*/*Tips*)
     u góry okna. Działa też na liczbie odsuniętej od węzła (przez *Avoid
     overlap* albo wcześniejsze ręczne przesunięcie),
  2. naciśnij przycisk,
  3. wpisz przesunięcie w punktach — **w lewo/prawo** i **góra/dół**; wartości
     ujemne przesuwają w lewo i do góry.

  Można też kliknąć gałąź zamiast liczby, ale uwaga: w trybie *Clade* klik
  w gałąź zaznacza **cały klad**, więc to samo przesunięcie poszłoby na
  wszystkie liczby w kladzie (okno wtedy uprzedza, ilu etykiet dotyczy).
  Do pojedynczej gałęzi przełącz kafelek na *Node* — albo po prostu klikaj
  w liczbę.

  Różnica względem *Offset X / Offset Y*: tamte przesuwają **wszystkie** wartości
  naraz, ta — tylko zaznaczone. Etykieta przesunięta ręcznie **zostaje tam, gdzie
  ją postawisz**: *Avoid overlap* jej nie rusza, a jedynie omija ją przy
  rozsuwaniu pozostałych. **Back to default** kasuje przesunięcie. Ustawienie
  siedzi na węźle (`!labelDX`, `!labelDY`) i zapisuje się z drzewem.

### Przepis: duże drzewo ML z wartościami bootstrap

Sprawdzona kolejność kroków dla gęstego drzewa (setki okazów, klady po
kilkanaście prawie identycznych sekwencji):

1. Otwórz plik **`.raxml.support`** (nie `bestTree`, nie `modeltest` —
   patrz rozdział 2).
2. *Node Labels*: **Display** = `bootstrap`, **Position** = *Above branch*,
   czcionka wedle potrzeb (8–10 pt).
3. **Show only if >=** np. `70` — to usuwa większość kolizji, bo słabo
   wsparte węzły (a tych jest najwięcej w gęstych kladach) nie dostają liczb.
   W publikacjach i tak zwykle pokazuje się tylko poparcie ≥ 50–70.
4. *Layout*: **Min tip spacing** ok. `15` — wyrównuje odstępy liści.
5. Jeśli w najgęstszych miejscach liczby dalej się gryzą: zaznacz
   **Avoid overlap** (+ **White backing** wedle gustu).

Kolejność ma znaczenie: próg i odstęp liści załatwiają większość, *Avoid
overlap* jest od ostatnich, pojedynczych kolizji — nie odwrotnie.

  Kiedy tego użyć: gdy w gęstym kladzie (dużo bardzo podobnych okazów jednego
  gatunku) liczby nachodzą na siebie mimo **Min tip spacing**. To normalne —
  *Min tip spacing* rozsuwa **liście**, a wartości poparcia siedzą na węzłach
  wewnętrznych, które mogą leżeć znacznie bliżej siebie niż liście.

### Kropki pełnego poparcia: panel *Support Dots*

Panel leży zaraz pod *Node Labels*. Rysuje czarną kropkę na każdej gałęzi,
która osiągnęła **maksymalne poparcie** — tak jak na wielu rycinach
w publikacjach, gdzie kropka zastępuje „100" albo „100 / 1".

- **Support** — atrybut z wartością poparcia (np. `bootstrap` albo `label` —
  ta nazwa, którą podałaś przy otwieraniu pliku RAxML-a). **Dot if >=** —
  od jakiej wartości jest kropka; domyślnie `100`.
- **and support** — druga analiza (np. `posterior` / `pp` z MrBayesa).
  Domyślnie *None* = kropka zależy tylko od pierwszej wartości. Gdy wybierzesz
  drugą wartość, kropka pojawia się tylko tam, gdzie **obie** osiągnęły próg
  (domyślnie `100` i `1`). Ułamki wpisuj z kropką (`0.95`), jak wszędzie.
- **Position** — **Middle of branch** (w połowie gałęzi prowadzącej do kladu,
  jak w większości artykułów) albo **At node** (na samym węźle).
- **Dot size** — średnica kropki w punktach.
- **Hide values at dots** — przy gałęziach z kropką liczby w *Node Labels* /
  *Branch Labels* znikają, więc pełne poparcie oznacza sama kropka, a liczby
  zostają tylko przy słabszych kladach. W podpisie ryciny warto napisać np.
  „kropka = BS 100 i PP 1,0".

Kropki nie trafiają na liście ani na korzeń. Wartość zapisana jako `0.99999`
(MrBayes czasem tak zaokrągla) liczy się jak `1`.

## 7. Panele *Node Shapes* / *Tip Shapes* (kropki)

- **Threshold attribute** + **Show only if >=** — kropka pojawia się tylko tam,
  gdzie wybrany atrybut osiąga próg, np. czarne kropki na węzłach z bootstrap ≥ 95.
  To starsze, prostsze narzędzie: kropka zawsze na węźle i tylko według jednej
  wartości. Do kropek pełnego poparcia lepszy jest panel *Support Dots* (wyżej).

## 8. Panel *Group Bars* (paski grup)

Pionowe paski po prawej stronie ryciny z nazwą grupy (rodzina, rząd…) obróconą
o 90°. Rysują się według **atrybutu liści** — patrz punkt 9, skąd go wziąć.
Działa tylko w układzie prostokątnym.

- **Attribute** — który atrybut liści wyznacza grupy (np. `family`). Lista
  pokazuje tylko atrybuty wczytane z pliku albo nadane przez *Assign to
  selection…*; pierwszy jest wybierany sam. Pusta lista (z podpowiedzią pod
  spodem) znaczy, że drzewo nie ma jeszcze żadnych grup — zacznij od punktu 9.
- **Assign to selection…** — zaznacz klad (tryb *Clade*) albo kilka nazw liści,
  kliknij, podaj nazwę atrybutu i wartość. Wszystkie liście z zaznaczenia dostają
  ją naraz, a panel od razu przełącza się na ten atrybut. Wygodne dla kilku grup;
  przy większej liczbie lepszy jest import z pliku.
  Okno pokazuje, który atrybut rysuje paski, a który tła (*Bars: order ·
  Backgrounds: family*), oraz co zaznaczone okazy mają teraz (*Now: …*). Gdy
  wszystkie mają jedną wartość, pole *Value* zaczyna od niej — wystarczy ją
  poprawić (np. wstawić `|`). **Restore original** cofa zmianę zrobioną tym
  oknem i przywraca poprzednią wartość (gwiazdka → nazwa rzędu, „Outgroup” →
  rodzina), nawet po zapisaniu i ponownym otwarciu pliku. Gdy nie ma czego
  cofnąć, wystarczy ponownie zaimportować słownik.
- **Bar width** — grubość paska (pt).
- **Gap from labels** — odstęp paska od najdłuższej nazwy (pt).
- **Font size** — wielkość napisu na pasku.
- **Text reads** — kierunek napisu z nazwą grupy:
  - *Upwards, facing tree* (domyślnie) — czyta się od dołu do góry, góra liter
    zwrócona w stronę drzewa;
  - *Downwards, facing away* — czyta się od góry do dołu, góra liter zwrócona
    na zewnątrz, w stronę brzegu strony.

  W artykułach spotyka się obie wersje — wybierz tę, której wymaga czasopismo.
- **Łamanie napisu na pasku**: `|` w wartości zaczyna nową linię
  (`ECHINO-|STELIALES`); linie układają się od paska na zewnątrz. Ta sama
  reguła co w napisach w tłach.
- **Hide names that do not fit** — napis dłuższy niż wysokość grupy jest
  pomijany zamiast wchodzić na sąsiadów (pasek zostaje). Dla takich grup:
  złamać nazwę przez `|` albo dać gwiazdkę (niżej).
- **Italic names** — napisy na paskach kursywą. Czy rodziny i rzędy pisze się
  kursywą, zależy od czasopisma (kod botaniczny tak, tradycja zoologiczna i
  spora część czasopism nie) — stąd przełącznik.
- **Napis, który się nie mieści, gwiazdka zamiast nazwy.** Dla małej grupy
  zaznacz jej okazy, *Assign to selection…*, atrybut pasków (np. `order`),
  wartość `*` albo `**`. Gwiazdki rysują się prosto i większe niż napisy.
  Objaśnienie gwiazdek to na razie legenda w programie graficznym (zadanie 7.7).
  Zmiana siedzi tylko w pliku `.tree` — słownik zostaje nietknięty. Jeśli
  gwiazdka trafiła przez pomyłkę też do rodziny, zaznacz te same okazy i
  *Assign to selection…* z atrybutem `family` i właściwą nazwą rodziny.
- **Backgrounds** — pastelowe tła za kladami, od wspólnego przodka do krawędzi
  etykiet; **Attribute** i **Opacity (%)** osobno dla tła.
  - **Gap between (pt)** — biała przerwa między sąsiednimi tłami (domyślnie 2),
    żeby klady się nie zlewały; 0 = tła stykają się.
  - **Gradient** — *None* (płaskie tło), *White at node, colour at names*
    (jak w artykułach: biel przy węźle kladu, pełny kolor przy napisach) albo
    *Colour at node, white at names* (odwrotnie). *Opacity* działa wtedy jako
    moc koloru; 100 = pełny kolor.
- **Clade names in backgrounds** (7.4) — nazwa kladu wpisana w jego tło, przy
  prawej krawędzi, wyrównana do prawej i wyśrodkowana w pionie. Napis to po
  prostu wartość atrybutu teł (np. `Clade 3 Argentodermataceae`), więc nie
  trzeba osobnej kolumny. W wartości `|` zaczyna nową linię
  (`Clade 11|Diacheaceae`), a `<b>…</b>` wokół całości pogrubia ten jeden
  napis. Tła sięgają wtedy pod kolumnę napisów, a paski rzędów stoją za nią.
  - **Name size** — wielkość napisu (domyślnie 11). Przy 10 i mniej Java na
    Windowsie rysuje pogrubienie tak samo jak zwykły tekst — to nie błąd
    programu, wystarczy dać 11.
  - **Bold names** — pogrubia wszystkie napisy naraz; `<b>…</b>` działa
    niezależnie, dla wybranych.
  - **Italic names** — wszystkie napisy w tłach kursywą (patrz uwaga o
    kursywie przy paskach).
  - **Not italic words** — słowa, które przy kursywie zostają proste, po
    przecinku lub spacji, wielkość liter bez znaczenia; domyślnie `Outgroup`,
    więc `Outgroup Trichiales` ma pochylone tylko *Trichiales*. Działa dla teł
    i pasków naraz.
  - **Napis „Outgroup”.** Zaznacz klad z outgroupem (tryb *Clade*), *Assign to
    selection…*, atrybut teł (np. `family`), wartość `Outgroup` albo
    `Outgroup|Trichiales` na dwie linie. Okazy outgroupu dostają jedno wspólne
    tło i jeden napis, jak w artykułach; słownik zostaje nietknięty, zmiana jest
    tylko w `.tree`.
- **Colours…** — własny kolor każdej grupy. Okno ma listę grup teł i pasków
  (w kolejności z ryciny, od góry), przy każdej przycisk z kolorem: kliknij,
  wybierz, drzewo zmienia się od razu. *Cancel* cofa wszystko, co zmieniłaś w
  tym oknie; **Back to built-in colours** wraca do wbudowanej palety. Kolor
  jest przypisany do pary atrybut + nazwa grupy, więc ta sama rodzina ma ten
  sam kolor w każdym miejscu drzewa. Przy płaskich tłach kolor jest rozjaśniany
  przez *Opacity*; przy gradiencie z *Opacity* 100 widać go w pełni. Kolory
  zapisują się w `.tree`. Grupy bez własnego koloru biorą wbudowaną paletę
  pastelową, w kolejności pojawiania się.
  - **Save colours…** zapisuje kolory z okna do małej tabelki (domyślnie
    `kolory_grup.tsv`): trzy kolumny rozdzielone tabulatorem — atrybut, grupa,
    kolor `#RRGGBB`, jedna grupa w wierszu. Da się ją otworzyć i poprawić w
    Excelu albo Notatniku.
  - **Load colours…** wczytuje taką tabelkę na innym drzewie: grupy o tej
    samej nazwie atrybutu i grupy dostają te same kolory, reszta zostaje jak
    była. Okno mówi, ile grup tego drzewa dostało kolor. Nazwy muszą się
    zgadzać co do znaku (także `|` i `<b>…</b>`, jeśli są w wartości).
    Tak przenosi się kolory z ryciny na rycinę — słownik rodzajów zostaje bez
    zmian.

## 8a. Panel *Figure Text* (tekst w rogu ryciny)

Kilka linii własnego tekstu nałożonych na gotową rycinę — np. objaśnienie
gwiazdek z pasków grup (`* ARGENTODERMATALES`, w drugiej linii
`** MERIDERMATALES`) albo podpis ryciny.

- **Text** — jedna linia ryciny = jedna linia w polu (Enter łamie).
- **Corner** — róg: *Top left*, *Top right*, *Bottom left*, *Bottom right*.
- **Font size**, **Margin (pt)** (odstęp od brzegu), **Bold**, **Italic**.

Tekst zapisuje się w `.tree` (`figureText.*`; łamania linii jako `|`). Trafia
do PDF/SVG/PNG tak jak reszta ryciny.

## 9. Menu *File → Import Annotations…* (Ctrl+I)

Wczytuje tabelkę, która przypisuje okazom dodatkowe informacje — to stąd biorą
się rodziny do *Group Bars*.

Plik: pierwszy wiersz to nagłówki (stają się nazwami atrybutów), pierwsza kolumna
to nazwy okazów **dokładnie jak w pliku drzewa** (z podkreślnikami) **albo same
nazwy rodzajów** — obie postacie mogą być w jednym pliku:

```
name	family	order
Trichia_lutescens_MA83355	Trichiaceae	Trichiales
Arcyria	Arcyriaceae	Trichiales
```

- **Nazwa jednowyrazowa** (bez podkreślników i spacji) jest traktowana jak rodzaj:
  jej wartości dostają **wszystkie** okazy, których nazwa zaczyna się od tego
  rodzaju. Dzięki temu słownik `doc/slowniki/sluzowce.tsv` wczytuje się wprost.
- Jeśli jakiś okaz jest w pliku wymieniony i z pełnej nazwy, i „załapuje się"
  przez rodzaj, wygrywa wiersz z pełną nazwą — można więc słownikiem opisać
  całość, a pojedyncze wyjątki nadpisać osobnym wierszem.
- Kolumny rozdzielone tabulatorem, przecinkiem albo średnikiem — w Excelu
  wystarczy *Zapisz jako → Tekst rozdzielany tabulatorami* (działa też *Tekst
  Unicode* i *CSV*). Samego pliku `.xlsx` program nie czyta — trzeba go
  najpierw zapisać jako tekst.
- Puste wiersze, cudzysłowy i wiersze zaczynające się od `#` nie przeszkadzają.
- Pusta komórka = brak atrybutu, czyli pasek grupy się w tym miejscu przerywa.
- Po wczytaniu okno mówi, **ile nazw udało się dopasować**. Jeśli widzisz
  „Matched 0 of…", prawie zawsze znaczy to, że w pierwszej kolumnie są nazwy
  w innej postaci niż w drzewie.

#### Jak czytać okno po imporcie (na przykładzie słownika `sluzowce.tsv`)

```
Matched 14 of 65 names in the file to tips of the tree
(14 of them as genus names, covering 208 tips).
```
Słownik zna 65 rodzajów, a w tym drzewie występuje 14 z nich. Te 14 rodzajów
opisało razem 208 okazów. **Wszystko w porządku** — słownik jest ogólny, a jedno
drzewo zawiera zwykle tylko część rodzajów.

```
4 tips of the tree got no value ... so the group bar has a gap there. By genus:
    MERspi: 3 tips
    Xxx: Xxx_yyy_Ron123
```
To jedyna część, która **wymaga działania**: tych okazów słownik nie objął
(rodzaju nie ma w pliku, ma literówkę albo nazwa okazu zaczyna się od skrótu,
np. `MERspi`). W tym miejscu pasek grupy będzie miał przerwę. Okazy są
**pogrupowane po rodzaju** — jeden wiersz w słowniku naprawia cały rodzaj,
więc nawet setka brakujących okazów to zwykle kilka rodzajów. Przy rodzaju
z jednym okazem widać od razu jego pełną nazwę. Gdy lista jest długa, okno
ją przewija zamiast ucinać.

Jak uzupełnić braki:
- **Save missing genera…** (przycisk w tym oknie) — zapisuje brakujące rodzaje
  do tabelki z tymi samymi kolumnami, co importowany plik (np. `genus`,
  `family`, `order`), z pustymi komórkami. Uzupełnij ją w Excelu albo
  Notatniku i **wklej wiersze do słownika** (`sluzowce.tsv`) — przydadzą się
  przy następnych drzewach. Potem zaimportuj słownik jeszcze raz. (Można też
  wczytać samą uzupełnioną tabelkę przez *Import Annotations…* — dopisze
  brakujące rodziny bez ruszania reszty.)
- albo zaznacz okazy w drzewie i użyj *Group Bars → Assign to selection…* —
  wygodne przy jednym, dwóch okazach.

```
51 genera from the file do not occur in this tree.
```
Rodzaje ze słownika, których nie ma w tym drzewie. **Nic nie trzeba robić.**

Nazwy wypisane pod nagłówkiem *Not found in the tree (check the spelling)*
to pełne nazwy okazów z pliku, których nie ma w drzewie — tu zwykle chodzi
o literówkę albo o inną wersję nazwy.

> Wersje programu sprzed 2026-10-01 wypisywały te 51 rodzajów jako listę
> „Not found in the tree" z żółtym trójkątem ostrzeżenia, a 4 okazów bez grupy
> nie wymieniała z nazwy. Wyglądało to na błąd, choć import się udał.
- Atrybuty zapisują się przy *Save* w pliku `.tree`, więc następnym razem są już
  na miejscu.

### Menu *File → Export Group Template…*

Odwrotność importu: wypisuje z otwartego drzewa **listę rodzajów** (alfabetycznie,
bez powtórzeń) do pliku z pustymi kolumnami `family` i `order`. Plik uzupełnia się
w Excelu albo Notatniku i wczytuje z powrotem przez *Import Annotations…* — nie
trzeba przepisywać ani jednej nazwy. Nagłówki kolumn można zmieniać i dodawać
własne (np. `substrat`); więcej w [slowniki/README.md](slowniki/README.md).

## 10. Eksport ryciny

- **Export PDF** — okno z wyborem **Page size** (*Fit to tree / A4 portrait /
  A4 landscape*), **Margin (mm)** i **Embed fonts** (osadza czcionki w pliku;
  pierwsze użycie trwa kilka sekund).
- Kursywa, pogrubienia i kolory z panelu *Tip Labels* trafiają do PDF i SVG.

## 11. Co się zapisuje

Wszystkie ustawienia z paneli zapisują się przy *Save* w pliku `.tree` (w bloku
FigTree wewnątrz NEXUS-a) razem z atrybutami liści — w tym ręczne poprawki
z **Style selected tips...** (`!labelItalic`, `!labelBold` przy nazwie liścia). Pliki zapisane zwykłym
FigTree otwierają się bez zmian, a pliki z tego programu otwarte w zwykłym
FigTree po prostu zignorują nieznane ustawienia.

## 12. Zmiana podpisu liścia ręcznie

Gdy na rycinie ma być coś, czego w nazwie z pliku w ogóle nie ma (np. `holotypus`
zamieniony na `HOLOTYPE`, żeby pasowało do `PARATYPE` z pozostałych okazów):

1. Kliknij w drzewie **etykietę tego liścia** (sam tekst nazwy) — podświetli się.
2. Menu **Tree → Annotate…** (skrót Ctrl + `'`).
3. Z listy *Annotation* wybierz **Name**.
4. Wpisz tekst, który ma się wyświetlać, i zatwierdź.

Zmienia się tylko podpis na rycinie — oryginalna nazwa zostaje w pliku, a nowa
dopisuje się obok jako `!name` i zapisuje razem z drzewem. Reguły kursywy działają
potem na **nowym** tekście, więc `Trichia sordida HOLOTYPE` wyjdzie jako
*Trichia sordida* HOLOTYPE (bo `HOLOTYPE` to same wielkie litery, a opcja
**ALL CAPS** kończy na nich kursywę).

Jeśli menu *Annotate…* jest wyszarzone — nic nie jest zaznaczone w drzewie.

## 12a. Kolorowanie kladów, okazów i gałęzi

### Trzy sposoby na kolor dla kladu

Żaden z nich nie wymaga słownika. Różnią się tym, co dostaje kolor.

| Co chcesz uzyskać | Jak | Gdzie cofnąć |
|---|---|---|
| **Kolorowe gałęzie i nazwy** okazów w kladzie | *Selection Mode: Clade*, kliknij gałąź kladu, przycisk **Colour** na pasku narzędzi, wybierz kolor | zaznacz klad i *Tree → Clear Colouring…* |
| **Jednolity prostokąt za kladem**, bez napisu | zaznacz klad, przycisk **Hilight**, wybierz kolor | *Tree → Clear Hilighting…* |
| **Tło z nazwą kladu**, gradientem i paskiem po prawej (jak w artykułach) | zaznacz klad, *Group Bars → Assign to selection…*, wpisz atrybut (np. `family`) i nazwę grupy (np. `Clade A`), włącz *Backgrounds*; kolor w *Colours…* | *Assign to selection… → Restore original* albo wyłącz *Backgrounds* |

- Pierwsze dwa sposoby to szybkie pokolorowanie bez żadnych nazw — dobre do
  zaznaczenia kilku okazów albo jednego kladu.
- Trzeci daje rycinę „jak w artykule” (rozdział 8) i można go mieszać ze
  słownikiem: część okazów ma rodzinę z importu, część nazwę wpisaną ręcznie
  przez *Assign to selection…* (np. `Outgroup`).
- Wszystkie trzy zapisują się w pliku `.tree`.

### Ten sam odcień co wcześniej

Przyciski **Colour** i **Hilight** na pasku narzędzi otwierają okno koloru,
które nad zwykłą paletą ma pasek **Already on this tree**: wszystkie kolory
nadane już ręcznie na tym drzewie, od najczęściej użytego. Po najechaniu myszą
na próbkę dymek pokazuje kod koloru i przykładowe okazy — to pomaga odróżnić
dwa podobne odcienie.

- **Dokładanie okazu do grupy:** zaznacz nowy okaz, *Colour*, kliknij kolor
  grupy na pasku, *OK*. Odcień jest dokładnie ten sam — nie trzeba szukać go
  w palecie ani kolorować całej grupy od nowa.
- **Jaki kolor ma ten okaz?** Zaznacz go i kliknij *Colour*: okno otwiera się
  na jego kolorze, a na pasku jest on obwiedziony czarną ramką. *Cancel*
  zamyka okno bez zmian.
- Gdy zaznaczone okazy mają różne kolory (albo żadnego), okno startuje od
  ostatnio wybranego koloru, jak dotąd.

Pasek pokazuje kolory nadane przyciskiem *Colour* / *Hilight*; kolory teł i
pasków grup ustawia się osobno w *Group Bars → Colours…*.

## 13. Zmiana oryginalnej nazwy okazu w pliku

Rozdział 12 podmienia tylko **podpis na rycinie** — oryginalna nazwa zostaje.
Gdy zmienić ma się sama nazwa okazu w pliku drzewa (np. robocze `MERspi_…`
na docelowe `Meriderma_…`), robi się to Notatnikiem, bo plik drzewa to zwykły
tekst:

1. **Zrób kopię pliku** (Ctrl+C, Ctrl+V w Eksploratorze) — gdyby coś poszło
   nie tak, oryginał zostaje.
2. Otwórz plik w Notatniku i wciśnij **Ctrl+H** (Zamień). W „Znajdź" stara
   nazwa (wystarczy wspólny początek, np. `MERspi`), w „Zamień na" — nowa.
3. Kliknij **Zamień wszystko** — koniecznie *wszystko*: w pliku zapisanym przez
   program (NEXUS) ta sama nazwa występuje w kilku miejscach (lista taksonów,
   tabela tłumaczeń, czasem adnotacje) i wszystkie muszą się zgadzać. W surowym
   pliku z RAxML-a (`.support`) nazwa jest tylko raz.
4. Zapisz i otwórz w programie.

Pułapki:

- **Nowa nazwa bez znaków specjalnych** — tylko litery, cyfry, podkreślniki,
  ewentualnie kropki i myślniki. Spacje, przecinki, dwukropki, średniki
  i nawiasy to znaki sterujące formatu drzewa — **rozwalą plik**.
- **Każdy okaz musi zachować unikalną nazwę.** Zamiana samego przedrostka jest
  bezpieczna (końcówki z numerami zostają różne); nie zamieniać pełnych nazw
  dwóch okazów na jedną i tę samą.
- **Krótka szukana fraza może złapać za dużo** — „Zamień wszystko" nie pyta.
  Przy nietypowym przedrostku w stylu `MERspi` nic nie grozi, przy krótszych
  warto najpierw poklikać *Znajdź następny* i obejrzeć trafienia.
- **Przyrównanie zostaje po staremu.** Zmiana w pliku drzewa nie zmienia nazw
  w FASTA, z którego drzewo policzono — przy ponownej analizie nazwy wrócą
  stare. Na rycinę bez znaczenia; dla porządku można zmienić w obu miejscach
  albo świadomie zostawić rozjazd.
- Po zmianie nazwy na taką z prawdziwym rodzajem (np. `Meriderma_…`) słownik
  łapie okaz zwykłym wierszem rodzaju — wpis roboczy (np. `MERspi`) w słowniku
  przestaje być potrzebny.

## 14. Liczby zawsze z kropką

Program świadomie używa **kropki** jako separatora dziesiętnego, niezależnie od
ustawień Windowsa — i w plikach, i w polach w panelach (`0.5`, nie `0,5`).
Inaczej zapisane drzewo ma długości gałęzi typu `0,001243`, a przecinek w pliku
NEXUS oddziela gałęzie — taki plik nie otwiera się ani tu, ani w żadnym innym
programie filogenetycznym.

Jeśli masz **starszy plik** zapisany z przecinkami (objaw: przy otwieraniu
*Error reading tree file: Taxon in tree, '00124300' is unknown*), wystarczy
zamienić w nim przecinki **stojące między cyframi** na kropki — przecinki
oddzielające gałęzie stoją zawsze przed literą albo nawiasem, więc ich to nie
dotyczy.

## 15. Gdy zapis się nie uda

Od etapu 6.13 nieudany zapis **nie rusza pliku, który już masz na dysku** — drzewo
idzie najpierw do pliku tymczasowego i podmienia stary dopiero, gdy zapisze się
w całości. Jeśli coś pójdzie nie tak, zobaczysz okienko *Save Failed* z treścią
błędu.

Gdyby program zaczął się dziwnie zachowywać — menu nie otwiera okienek, zapis
milczy — najczęstsza przyczyna jest prozaiczna: **program był otwarty, gdy został
przebudowany**. Zamknij go i uruchom ponownie.

Drzewo ML z RAxML-a zawsze da się odtworzyć z plików, które zostają po analizie:

```
raxml-ng --support --tree NAZWA.raxml.bestTree --bs-trees NAZWA.raxml.bootstraps --prefix NAZWA_odtworzony
```

(użytkowniczka ma `raxml-ng` w WSL-u, w `/home/magda/raxml-ng/bin/`). Powstaje
`NAZWA_odtworzony.raxml.support` — to samo drzewo z tymi samymi wartościami
bootstrap. Giną tylko ustawienia wyglądu zapisane wcześniej w pliku.
