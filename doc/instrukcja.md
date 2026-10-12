# MyFigTree MD — instrukcja

Krótki opis tego, co robi każda opcja dodana. Opcje odziedziczone
po zwykłym FigTree działają jak dotąd i nie są tu opisane.

Wersja angielska: [manual.md](manual.md). Pełne notatki robocze (historia pracy,
odzyskiwanie plików): [instrukcja_pelna.md](instrukcja_pelna.md).

## O programie

MyFigTree MD jest forkiem programu **FigTree** Andrew Rambauta (licencja LGPL,
https://github.com/rambaut/figtree). Dodatkowe funkcje zaprojektowała **Magdalena Dusza**
i przetestowała na rzeczywistych drzewach filogenetycznych ML (RAxML) i bayesowskich (MrBayes);
kod napisano z pomocą asystenta AI (Claude, Anthropic) pod jej kierunkiem. Program jest
udostępniany bez gwarancji. Ryciny warto sprawdzić przed publikacją.

Kod i nowe wersje: https://github.com/mdusza-bio/figtree


---
Zapisywanie zmian wprowadzonych na drzewie:
  1. Otwarcie pliku w MyFigTree.
  2. Od razu zrobić File → Save As i nadaj nową nazwę z końcówką .tree, na przykład ML_SSU.tree, w tym samym folderze.
  3. Od tej chwili pracować w pliku .tree. Ustawienia paneli, Style selected tips, skrócone gałęzie i inne edycje będą zapisywać się właśnie tam.

Co oferują nowe narzędzia?

## 1. Panel *Layout*

- **Min tip spacing** — najmniejszy odstęp w pionie między nazwami liści (w punktach). Podobne działanie jak Expansion, ale Min tip spacing ustawiasz raz pod docelową czcionkę ryciny. Drzewo samo się wydłuża, żeby nazwy się nie nakładały. Działa też przy eksporcie
  do PDF. Uwaga: pilnuje odstępu między nazwami, więc wartości poparcia na węzłach
  wewnętrznych mogą nadal kolidować — na to jest osobna opcja **Avoid overlap**
  w panelu *Node Labels* (rozdział 4).
- **Shorten branches longer than** — bardzo długie gałęzie (np. do outgrupy) są
  skracane i oznaczane `//`. Uwaga: skrócona gałąź nie odpowiada już skali. Wartość wpisać z kropka, przecinek nie działa, np. 0.2
- **Shorten selected branch: 2x / 3x / Full** — skraca jedną wybraną gałąź
  dokładnie dwa albo trzy razy i dodaje nad znakiem `//` „2x” albo „3x”
  - Skrócenie jest zapisane przy samej gałęzi w pliku `.tree`, więc zostaje po
    ponownym otwarciu.
  - Różnica wobec *Shorten branches longer than*: tamta opcja przycina
    wszystkie długie gałęzie do jednej długości i nie podaje, ile razy;

## 2. Panel *Appearance*

- **Publication preset** — jedno kliknięcie ustawia cały (albo większość?) Twój stały zestaw do drzewa
- Po jego użyciu przycisk zmienia się w **Undo preset** — cofa wszystkie
  ustawienia do stanu sprzed kliknięcia. Zapamiętany stan dotyczy tego jednego
  drzewa; po wczytaniu innego przycisk wraca do zwykłej postaci.
- **Save current as preset** —  **Publication preset** będzie przywracał
  właśnie ten zapamiętany stan.
  Szczegóły:
  - zapamiętywane jest wszystko **oprócz ukorzenienia** (*Rooting*) — ono zależy
    od konkretnego drzewa, więc preset go nie przestawia; podobnie nazwa
    atrybutu z poparciem (`bootstrap`/`label`) jest za każdym razem wykrywana
    na nowo w otwartym drzewie,
  - zapis trafia do małego pliku `MyFigTree_publication_preset.txt` w Twoim
    folderze — można go skopiować na inny komputer albo dołączyć do kopii
    zapasowej,
  - jeśli zapisany zestaw już istnieje, okienko zapytania ma dodatkowy przycisk
    **Back to original preset** — usuwa zapamiętany zestaw i przywraca
    wbudowany styl,
  - najechanie myszą na **Publication preset** podpowiada, który zestaw
    zadziała: własny zapisany czy wbudowany.
 
## 3. Panel *Tip Labels*

Czyszczenie nazwy:
- **Replace '_' with space** — podkreślniki z nagłówków FASTA zamieniane na spacje.
  Zmienia tylko to, co widać na ekranie i na rycinie; plik z drzewem zostaje
  nietknięty. Eksport do pdf utrzymuje brak '_'
- **Hide parts** — fragmenty do wycięcia z nazwy, po przecinku (np. `_contig1`).
  Tnie dosłownie i **wszędzie** — każde wystąpienie wpisanego tekstu znika,
  także ze środka nazwy, dlatego trzeba uważać.
- **Hide regex** — to samo, ale wyrażeniem regularnym (wzorcem), którym można
  doprecyzować, **gdzie** wycinać. Błędne wyrażenie jest po prostu ignorowane
  (nazwa zostaje nietknięta).
- Który wybrać — na przykładzie ukrycia `_new` z końcówki nazwy:
  - *Hide parts* z wpisem `_new` utnie `_new` też ze środka — z
    `Arcyria_newtoniana_MA123` zrobi się po cichu `Arcyriatoniana MA123`;
  - *Hide regex* z wpisem `_new$` usunie `_new` **tylko na końcu nazwy**
    (znak `$` znaczy „koniec nazwy").
  W obu polach pisz z podkreślnikiem (`_new`, nie ` new`) — wycinanie działa na surowej nazwie
  z pliku, zanim podkreślniki zamienią się w spacje. Uwaga: w regexie znaki
  `. | ( ) [ ] $ ^ * + ?` mają specjalne znaczenie (np. `|` znaczy „albo") —
  dlatego fragment z kreską, jak `EBOV|`, wycina się przez *Hide parts*.

Kursywa i skład nazwy:
- **Italic mode** — sposób wybierania, co ma być kursywą:
  - *Off* — nic,
  - *First N parts* — pierwsze N członów nazwy (pole **Italic first N parts**),
  - *Until first number* (domyślne) — kursywa aż do pierwszego członu wyglądającego
    na numer okazu.
- **Not italic words** — słowa, które zostają pismem prostym w środku nazwy
  (domyślnie `var subsp ssp f sp cf aff nov x`). 
- **Dot after** — dopisuje kropkę: `var` → `var.`, `sp` → `sp.` 
- **ALL CAPS** — kończy kursywę także na członie pisanym samymi wielkimi literami
  bez cyfr, np. `KRAM` w numerze.
- **Join number** — skleja numer okazu rozbity przez podkreślniki w postać, w której
  się go cytuje: `KRAM M 1156` → `KRAM M-1156`, `UK100 1b` → `UK100-1b`,
  `Ron324 2 3` → `Ron324 2/3`. Działa na podstawie kilku ogólnych reguł, a to, czego nie rozpoznaje, zostawia bez zmian. 

Ręczna poprawka pojedynczego liścia (gdy reguły się mylą):
- **Style selected tips...** — klikasz w drzewie liść, który wyszedł źle
  (Shift+klik, żeby zaznaczyć kilka), naciskasz ten przycisk i w okienku
  odznaczasz **italic** przy tych członach nazwy, które mają być pismem prostym.
  Klasyczny przypadek: `Trichia sordida holotypus` — reguła robi kursywą wszystko,
  bo w nazwie nie ma numeru, a `holotypus` powinien zostać prosto. Można też dodać superscript T.
- **bold whole label** w tym samym okienku pogrubia całą nazwę tego liścia
- **type specimen (superscript T)** w tym samym okienku dopisuje za nazwą T w indeksie górnym.

**Oznaczanie typów:**
- *hurtem, po nazwie*: w **Highlight names containing** wpisz to, co w nazwach
  z FASTA oznacza typ (np. `HOLOTYPE`), a potem zaznacz **Bold** przy
  *Highlight style* (pogrubienie) albo **superscript T** przy *Type mark*
  (małe „T”) — albo oba naraz. Samo słowo `HOLOTYPE` można schować polem
  **Hide parts**, wtedy na rycinie zostaje tylko „T”;
- *ręcznie, okaz po okazie*: zaznacz liść, **Style selected tips...**, i zaznacz
  *bold whole label* albo *type specimen (superscript T)*.

Wygląd:
- **Case (italic parts)** / **Case (other parts)** — wielkość liter osobno dla
  części kursywnych i pozostałych: *As is / UPPER / lower / Sentence*.
- **Bold** i kolor — osobno dla części kursywnych i pozostałych.
- **Highlight names containing** — lista fragmentów nazw po przecinku; pasujące
  liście dostają wyróżnienie (pogrubienie / kolor) ustawione w **Highlight style**.

## 4. Panele *Node Labels* i *Branch Labels*

- **Position** — *At node / Above branch / Below branch*.
- **Offset X / Offset Y** — odsunięcie etykiet od węzła i od gałęzi
  (w punktach, mogą być ujemne).
- **Show only if >=** — wartości poniżej nie są pokazywane.
- **Second value** + **Layout** — druga wartość obok pierwszej (np. PP obok
  bootstrapu), w formie `a / b` albo jedna nad drugą (*stacked*).
- **Second only if >=** — osobny próg dla drugiej wartości (np. `0.95` dla PP).
  Puste pole = druga wartość pokazywana zawsze.
- **Too low shows as** — wpisz `-`, gdy wartość, jest poniżej progu albo której w ogóle nie ma.
- **Crowded values → Avoid overlap** — rozprowadza poparcia, gdy wartości wypadają na innych. Czasem ta opcja nie działa dobrze, ale są też inne narzędzia. Jeśli liczba musi odjechać dalej niż o własną wysokość, od liczby do jej węzła rysowana jest
  cienka szara **linia wskazująca**, żeby nie było wątpliwości, do której
  gałęzi należy. Samo drzewo się nie zmienia — przesuwa się wyłącznie tekst;
  wartości, które się nie gryzą, zostają na miejscu.

  Obok jest drugi checkbox **White backing** — każda
  wartość poparcia dostaje pod spodem **prostokąt w kolorze tła**, do wyboru wedle gustu.

- **Przeciąganie liczby myszą** — najprostszy sposób na przestawienie
  **jednej** wartości, gdy wyląduje w złym miejscu, a reszta drzewa wygląda
  dobrze: najedź kursorem na liczbę (kursor zmienia się w strzałki ✥), wciśnij
  lewy przycisk i **przeciągnij ją tam, gdzie ma być**. Przeciągnięcie z powrotem w okolicę pierwotnego miejsca
  przywraca automatyczne ustawianie. Możliwe jest również bardziej precyzyjne przesuwanie za pomocą strzałek na klawiaturze.
  Jedno naciśnięcie przesuwa o **1 punkt**, z przytrzymanym **Shift** — o **10
  punktów**. Działa też na kilku zaznaczonych wartościach naraz (przesuwają się
  razem).
  **Support Dots** — kropka oznaczająca pełne poparcie

## 5. Panele *Node Shapes* / *Tip Shapes*

- **Threshold attribute** + **Show only if >=** — kropka pojawia się tylko tam,
  gdzie wybrany atrybut osiąga próg, np. czarne kropki na węzłach z bootstrap ≥ 95.
  To starsze, prostsze narzędzie: kropka zawsze na węźle i tylko według jednej
  wartości. Do kropek pełnego poparcia lepszy jest panel *Support Dots* (wyżej).

## 6. Panel *Group Bars* (paski grup)

Pionowe paski po prawej stronie ryciny z nazwą grupy (rodzina, rząd…) obróconą
o 90°. Rysują się według **atrybutu liści** — patrz punkt 9, skąd go wziąć.

- **Attribute** — który atrybut liści wyznacza grupy (np. `family`). Lista
  pokazuje tylko atrybuty wczytane z pliku albo nadane przez *Assign to
  selection…*.
- **Assign to selection…** — zaznacz klad (tryb *Clade*) albo kilka nazw liści,
  kliknij, podaj nazwę atrybutu i wartość. Wszystkie liście z zaznaczenia dostają
  ją naraz, a panel od razu przełącza się na ten atrybut. Wygodne dla kilku grup lub podpisu outgroupy;
  przy większej liczbie lepszy jest import z pliku.
- **Bar width** — grubość paska (pt).
- **Gap from labels** — odstęp paska od najdłuższej nazwy (pt).
- **Font size** — wielkość napisu na pasku.
- **Text reads** — kierunek napisu z nazwą grupy:
- **Łamanie napisu na pasku**: `|` w wartości zaczyna nową linię
  (`ECHINO-|STELIALES`)
- **Hide names that do not fit** — napis dłuższy niż wysokość grupy jest
  pomijany zamiast wchodzić na sąsiadów (pasek zostaje). Dla takich grup:
  złamać nazwę przez `|` albo dać gwiazdkę (wyjaśnienie niżej).
- **Italic names** — napisy na paskach kursywą
- **Napis, który się nie mieści, gwiazdka zamiast nazwy.** Dla małej grupy
  zaznacz jej okazy, *Assign to selection…*, atrybut pasków (np. `order`),
  wartość `*` albo `**`. Gwiazdki rysują się prosto i większe niż napisy.
  Objaśnienie gwiazdek można dodać w legendzie w panelu Figure text
- **Backgrounds** — pastelowe tła za kladami, **Attribute** i **Opacity (%)** osobno dla tła.
  - **Gap between (pt)** — biała przerwa między sąsiednimi tłami
  - **Gradient** 
- **Clade names in backgrounds**
  - **Name size** — wielkość napisu (domyślnie 11). Przy 10 i mniej Java na
    Windowsie rysuje pogrubienie tak samo jak zwykły tekst — to nie błąd
    programu
  - **Bold names** — pogrubia wszystkie napisy naraz
  - **Italic names** — wszystkie napisy w tłach kursywą (patrz uwaga o
    kursywie przy paskach).
  - **Not italic words** — słowa, które przy kursywie zostają proste, po
    przecinku lub spacji, wielkość liter bez znaczenia; domyślnie `Outgroup`,
    więc `Outgroup Trichiales` ma pochylone tylko *Trichiales*. 
  - **Napis „Outgroup”.** Zaznacz klad z outgroupem (tryb *Clade*), *Assign to
    selection…*, atrybut teł (np. `family`), wartość `Outgroup` albo
    `Outgroup|Trichiales` na dwie linie.
- **Colours…** — własny kolor każdej grupy.
  - **Save colours…** zapisuje kolory z okna do małej tabelki (domyślnie
    `kolory_grup.tsv`): trzy kolumny rozdzielone tabulatorem — atrybut, grupa,
    kolor `#RRGGBB`, jedna grupa w wierszu. Da się ją otworzyć i poprawić w
    Excelu albo Notatniku.
  - **Load colours…** wczytuje taką tabelkę na innym drzewie: grupy o tej
    samej nazwie atrybutu i grupy dostają te same kolory, reszta zostaje jak
    była. Okno mówi, ile grup tego drzewa dostało kolor. Tak przenosi się kolory z ryciny na rycinę — słownik rodzajów zostaje bez
    zmian.

## 7. Panel *Figure Text* (tekst w rogu ryciny)

Kilka linii własnego tekstu nałożonych na gotową rycinę — np. objaśnienie
gwiazdek z pasków grup.

Tekst zapisuje się w `.tree` (`figureText.*`; łamania linii jako `|`). Trafia
do PDF/SVG/PNG tak jak reszta ryciny.

## 8. Menu *File → Import Annotations…* (Ctrl+I)

Wczytuje tabelkę, która przypisuje okazom dodatkowe informacje — stąd biorą
się rodziny do *Group Bars*.

Plik: pierwszy wiersz to nagłówki (stają się nazwami atrybutów), pierwsza kolumna
to nazwy rodzajów, rodzina (można samemu ustalić co się chce)
- Kolumny rozdzielone tabulatorem, przecinkiem albo średnikiem — w Excelu
  wystarczy *Zapisz jako → Tekst rozdzielany tabulatorami* (działa też *Tekst
  Unicode* i *CSV*). 
- Samego pliku `.xlsx` program nie czyta — trzeba go
  najpierw zapisać jako tekst.

### Menu *File → Export Group Template…*

Odwrotność importu: wypisuje z otwartego drzewa **listę rodzajów** (alfabetycznie,
bez powtórzeń) do pliku z pustymi kolumnami `family` i `order`. Plik uzupełnia się
w Excelu albo Notatniku i wczytuje z powrotem przez *Import Annotations…*

## 9. Eksport ryciny

- **Export PDF** — okno z wyborem **Page size** (*Fit to tree / A4 portrait /
  A4 landscape*), **Margin (mm)** i **Embed fonts** (osadza czcionki w pliku;
  pierwsze użycie trwa kilka sekund).
- Kursywa, pogrubienia i kolory z panelu *Tip Labels* trafiają do PDF i SVG. Od wersji 1.1, gdy wybrana czcionka nie ma na komputerze osobnego pliku kursywy lub pogrubienia, PDF bierze je z czcionki standardowej (Times-Italic / Helvetica-Oblique) zamiast gubić styl. Gdyby kursywa mimo to nie była widoczna: odznaczyć *Embed fonts* albo wybrać Times New Roman lub Arial (przycisk **Font** w wierszu *Setup* panelu *Tip Labels*).

## 10. Zmiana oryginalnej nazwy okazu w pliku

Funkcja Annotate zmienia podpis tylko na rycinie — oryginalna nazwa zostaje w pliku, a nowa
dopisuje się obok jako `!name` i zapisuje razem z drzewem. Nazwę można też zmienić w pliku drzewa, np. w notatniku.

1. Zrób kopię pliku — tak na wszelki wypadek
2. Otwórz plik w Notatniku i wciśnij **Ctrl+H** (Zamień). W „Znajdź" wpisz stara
   nazwę i „Zamień na" — nowa.
3. Kliknij **Zamień wszystko** — koniecznie *wszystko*: w pliku zapisanym przez
   program (NEXUS) ta sama nazwa występuje w kilku miejscach.
4. Zapisz i otwórz w programie.

O czym należy pamiętać:

- **Nowa nazwa bez znaków specjalnych** — tylko litery, cyfry, podkreślniki,
  ewentualnie kropki i myślniki. Spacje, przecinki, dwukropki, średniki
  i nawiasy to znaki sterujące formatu drzewa — **rozwalą plik**.
- **Każdy okaz musi zachować unikalną nazwę.** 

