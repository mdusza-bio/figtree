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

### Etap 7.1 — własne kolory teł i pasków (2026-10-05)
*Group Bars → Colours…*: okno z listą grup (osobno tła i paski, w kolejności z
ryciny) i przyciskiem koloru przy każdej. Zmiana widoczna od razu, *Cancel*
cofa, **Back to built-in colours** przywraca paletę. Zapis w `.tree` jako
`groupBars.colours`. Zamyka też stare zadanie 6.8.

**Save colours… / Load colours…** w tym samym oknie: kolory jako tabelka
`atrybut / grupa / #RRGGBB`, do przenoszenia między drzewami (czyta UTF-8 i
„Tekst Unicode” z Excela).

### Etap 7.7 — tekst w rogu ryciny, kierunek gradientu, słowa bez kursywy (2026-10-02)
- Nowy panel **Figure Text**: kilka linii tekstu w wybranym rogu ryciny
  (legenda gwiazdek, podpis), z wielkością, marginesem, pogrubieniem i
  kursywą. Zapis `figureText.*`, łamania linii jako `|`.
- *Group Bars → Gradient* ma teraz trzy ustawienia: *None*, *White at node,
  colour at names*, *Colour at node, white at names*. Pliki zapisane z
  wcześniejszym tak/nie otwierają się poprawnie.
- *Group Bars → Not italic words* (domyślnie `Outgroup`): słowa, które przy
  kursywie zostają proste, w tłach i na paskach.

### Etap 7.2 / 7.3 — przerwa i gradient teł, Restore original (2026-10-02)
*Group Bars → Backgrounds*:
- **Gap between (pt)** — biała przerwa między sąsiednimi tłami (domyślnie 2).
- **Gradient (white at the node)** — tło od bieli przy węźle do pełnego koloru
  przy prawej krawędzi; bez przezroczystości, więc PDF wychodzi tak samo.

*Assign to selection…* pokazuje role atrybutów (paski / tła), obecne wartości
zaznaczenia i zaczyna pole *Value* od wspólnej wartości. Nowy przycisk
**Restore original** przywraca wartość sprzed zmiany (pamiętana w ukrytym
atrybucie `!orig.<nazwa>`, zapisuje się w `.tree`).

Zapis: `groupBars.backgroundGap`, `groupBars.backgroundGradient`.

### Etap 7.6 — łamanie i chowanie napisów na paskach (2026-10-02)
- `|` w wartości łamie napis na pasku na kilka linii (`ECHINO-|STELIALES`),
  linie od paska na zewnątrz; rycina rezerwuje miejsce na najszerszy napis.
- **Hide names that do not fit** (*Group Bars*, `groupBars.hideUnfitting`) —
  napis dłuższy niż grupa jest pomijany, pasek zostaje.

### Etap 7.4b — kursywa napisów, większe gwiazdki (2026-10-02)
- *Group Bars*: **Italic names** osobno dla pasków i dla teł (`groupBars.italic`,
  `groupBars.backgroundLabelItalic`).
- Grupa oznaczona samą gwiazdką (`*`, `**` przez *Assign to selection…*) rysuje
  się na pasku prosto i 1,8× większą czcionką, wyśrodkowana — 10-punktowa
  gwiazdka była plamką.
- Instrukcja §8: jak zrobić napis „Outgroup” i gwiazdki bez ruszania słownika.
- Okno po imporcie bez dopisku „normal for a dictionary, nothing to do about it”.

### Etap 7.4 — napisy kladów w tle (2026-10-01)
Pierwszy punkt ryciny „jak w artykule o Physarales". *Group Bars*, pod
*Backgrounds*:
- **Clade names in backgrounds** — nazwa kladu w jego tle, przy prawej
  krawędzi, do prawej, wyśrodkowana w pionie. Tekst = wartość atrybutu teł;
  `|` w wartości łamie linię (`Clade 11|Diacheaceae`), `<b>…</b>` pogrubia
  jeden napis;
- **Name size** (domyślnie 11 — przy 10 pt i mniej Java na Windowsie nie
  odróżnia pogrubienia) i **Bold names** (wszystkie naraz);
- tła sięgają pod kolumnę napisów, paski rzędów przesuwają się za nią.

Zapis w `.tree`: `groupBars.backgroundLabels`, `backgroundLabelSize`,
`backgroundLabelBold`. Kolejka etapu 7 przejrzana ze wzorem: kropki *At node*,
7.4 bez osobnej kolumny, 7.6 bez `order_label`, nowa kolejność.

### Etap 6.22 — okno po imporcie przy wielu brakach (2026-10-01)
Gdy słownik nie obejmie wielu okazów, okno po *Import Annotations* nie ucina
już listy po 10 nazwach:
- okazy bez grupy są **pogrupowane po rodzaju** (`MERspi: 12 tips`), bo jeden
  wiersz słownika naprawia cały rodzaj; przy rodzaju z jednym okazem widać
  jego pełną nazwę;
- długi raport przewija się w oknie;
- przycisk **Save missing genera…** zapisuje brakujące rodzaje do tabelki
  z kolumnami importowanego pliku — do uzupełnienia i wklejenia do słownika.

### Etap 6.20 / 6.21 — kropki pełnego poparcia, kierunek napisów grup (2026-10-01)
Nowy panel **Support Dots** (zaraz pod *Node Labels*):
- czarna kropka na gałęzi, która osiągnęła **maksymalne poparcie** — w jednej
  analizie (*Support* + *Dot if >=*, domyślnie 100) albo w obu (*and support*,
  np. PP ≥ 1). Kropki nie trafiają na liście ani na korzeń;
- **Position**: *Middle of branch* (jak w większości artykułów) albo *At node*;
- **Dot size** — średnica w punktach;
- **Hide values at dots** — liczby w *Node Labels* / *Branch Labels* znikają przy
  gałęziach z kropką, zostają tylko przy słabszych kladach.

*Group Bars*: nowe pole **Text reads** — napis z nazwą grupy od dołu do góry
(litery zwrócone do drzewa, jak dotąd) albo od góry do dołu (litery od drzewa).

*Node Labels* / *Branch Labels*: **Replace '_' with space**, **Hide parts**
i **Hide regex** widać tylko przy *Display = Names* — przy liczbach poparcia
te opcje nic nie robiły, a zajmowały miejsce. *Tip Labels* bez zmian.

Okno po *Import Annotations* czytelniejsze: z nazwy wymienia **okazy, które nie
dostały grupy** (to jedyna rzecz do poprawienia), a rodzaje ze słownika, których
nie ma w drzewie, podaje tylko liczbą, z dopiskiem, że to normalne. Ostrzeżenie
(żółty trójkąt) tylko wtedy, gdy rzeczywiście jest coś do zrobienia.

Wszystkie nowe ustawienia zapisują się w `.tree` (`supportDots.*`,
`groupBars.textDirection`); starsze pliki otwierają się jak dotąd.

### Etap 6.19 — Group Bars i Import Annotations „nie działały" (2026-09-25)
**Skąd się wzięło.** Przy pokazywaniu programu współpracownikowi import i paski
grup sprawiały wrażenie zepsutych. Test automatyczny (import → Group Bars →
zapis → ponowne otwarcie) pokazał, że samo dopasowanie działa, ale były trzy
pułapki bez żadnego komunikatu:
- **Lista *Attribute* w Group Bars zaczynała się od „Names"** (i innych
  wbudowanych pozycji: *Solid box*, *Node ages*…), które do grupowania się nie
  nadają. Po imporcie zaznaczenie *Group Bars* nic nie rysowało, dopóki ktoś
  sam nie wybrał `family`. Teraz lista zawiera **tylko prawdziwe atrybuty**
  (`family`, `order`…), pierwszy z nich jest od razu wybrany, a paski od razu
  go rysują. Gdy atrybutów jeszcze nie ma, panel podpowiada: *File → Import
  Annotations…* albo *Assign to selection…*. To samo w liście tła
  (*Backgrounds*) i w oknie *Assign to selection…*.
- **Plik z Excela zapisany jako „Tekst Unicode (*.txt)"** kończył się komunikatem
  „the header line has only one column". Teraz jest wczytywany normalnie.
  Wskazanie samego skoroszytu (`.xlsx` / `.xls`) daje jasny komunikat, jak
  zapisać go jako tekst.
- **Niewypełniony szablon** z *Export Group Template* (same rodzaje, puste
  kolumny) dawał mylący komunikat o nagłówku — teraz program mówi wprost, że
  kolumny są puste i trzeba je uzupełnić (albo wczytać gotowy słownik).

### Etap 6.18 — własny zapis presetu publikacyjnego (2026-09-02)
**Skąd się wzięło.** Zestaw pod przyciskiem *Publication preset* był wpisany na
stałe w kod (stan ustalony 2026-08-25) — każda korekta wymagała zmiany w kodzie
i kompilacji.

- Nowy przycisk **Save current as preset** w panelu *Appearance*: ustawiasz
  wszystko ręcznie tak, jak ma być, klikasz — i od tej pory *Publication preset*
  przywraca dokładnie ten zapamiętany stan (wszystkie panele, łącznie z kropkami
  na węzłach i wyrównaniem nazw — co widzisz, to się zapisuje).
- Zapamiętane **nie** jest ukorzenienie (*Rooting*) — zależy od konkretnego
  drzewa; nazwa atrybutu z poparciem (`bootstrap`/`label`) jest jak dotąd
  wykrywana na nowo w każdym otwartym drzewie.
- Zestaw ląduje w pliku `MyFigTree_publication_preset.txt` w folderze domowym
  (`C:\Users\Magdalena Dusza`), zapisywanym przez plik tymczasowy z podmianą
  po sukcesie (jak *Save*, etap 6.13). Wartości w tym samym formacie co blok
  FigTree w NEXUS-ie.
- Gdy zapisany zestaw istnieje, okienko zapisu ma dodatkowo **Back to original
  preset** — usuwa własny zestaw i przywraca wbudowany styl z 2026-08-25.
  Podpowiedź na przycisku *Publication preset* mówi, który zestaw zadziała.
- *Undo preset* działa bez zmian — cofa także własny zestaw.

### Etap 6.7 — słowniki grup: dopasowanie po rodzaju (2026-08-28)
**Skąd się wzięło.** Import słownika `doc/slowniki/sluzowce.tsv` do prawdziwego
drzewa kończył się komunikatem *„Matched 0 of 63"* — bo import porównywał tylko
**pełne** nazwy okazów, a słownik ma same rodzaje.

- *File → Import Annotations…*: nazwa **jednowyrazowa** (bez podkreślników
  i spacji), która nie pasuje do żadnej pełnej nazwy okazu, jest traktowana jako
  **rodzaj** — jej wartości dostają wszystkie okazy, których nazwa zaczyna się od
  tego rodzaju. Słownik wczytuje się więc wprost, bez doklejania rodzin w Excelu.
- Podsumowanie po imporcie osobno liczy dopasowania po rodzaju i liczbę objętych
  nimi okazów.
- Wiersz z pełną nazwą okazu zawsze **wygrywa** z wartością odziedziczoną po
  rodzaju, niezależnie od kolejności w pliku — pojedyncze wyjątki od słownika
  załatwia jeden dodatkowy wiersz.
- Nazwy wielowyrazowe (np. okaz, którego nie ma w drzewie) celowo **nie** spadają
  na dopasowanie po rodzaju — brakujący okaz nie rozleje swoich wartości na cały
  rodzaj; taka nazwa jest po prostu wypisana jako niedopasowana.
- Nowe *File → Export Group Template…* — zapisuje alfabetyczną listę rodzajów
  otwartego drzewa z pustymi kolumnami `family`/`order`; plik uzupełnia się
  w Excelu/Notatniku i wczytuje z powrotem. Zapis przez plik tymczasowy, jak
  przy *Save* (etap 6.13).

### Poprawka — węższy znak `//` na skróconej gałęzi (2026-09-02)
Znak przerwania w **Shorten branches longer than** miał szerokość zależną od
długości rysowanej gałęzi, więc na bardzo długiej gałęzi (outgrupa) dwa ukośniki
lądowały daleko od siebie. Teraz przerwa ma stałą, małą szerokość liczoną od
szerokości całego drzewa — `//` wygląda jak w publikacjach.

### Etap 6.17 — przecinek dziesiętny psuł zapisane pliki
**Co się stało (2026-08-25).** Zapisany plik nie chciał się otworzyć:
*„Taxon in tree, '00124300' is unknown"*. Przyczyna: na polskim Windowsie Java
formatuje liczby z **przecinkiem**, więc długości gałęzi zapisały się jako
`0,001243`. Przy odczycie przecinek jest separatorem gałęzi, więc `0,001243`
rozpadało się na `0` i nowy „takson" `001243`.

Autor oryginalnego FigTree ten problem znał — w `FigTreeApplication.main()` jest
jego komentarz *„There is a major issue with languages that use the comma as
a decimal separator"* i **zakomentowana** linijka z poprawką. Została włączona,
w wersji łagodniejszej: wymuszona jest tylko kategoria `FORMAT`
(`Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT)`), więc liczby są
zapisywane i czytane z **kropką**, a język interfejsu zostaje systemowy.

Skutek uboczny: pola liczbowe w panelach (odstępy, grubości) przyjmują teraz
**kropkę**, nie przecinek — tak samo jak pliki i jak większość programów
filogenetycznych.

Uszkodzony plik użytkowniczki został naprawiony (763 liczby, zamiana przecinka
na kropkę między cyframi) i sprawdzony importerem programu: 383 taksony,
142 ustawienia wczytują się poprawnie. Przegląd pozostałych plików z drzewami
na Pulpicie nie wykazał innych uszkodzonych.

### Etap 6.16 — przesuwanie pojedynczej wartości poparcia
*Offset X / Offset Y* przesuwają wszystkie etykiety naraz, a w praktyce zawsze
znajdzie się jedna liczba, która wypada źle, choć reszta jest w porządku. Panel
*Node Labels* dostaje przycisk **Move selected label…**: zaznaczasz węzeł
w drzewie, wpisujesz przesunięcie w punktach (w lewo/prawo i góra/dół, ujemne
w lewo i do góry), **Back to default** kasuje.

Etykieta przesunięta ręcznie jest wyłączona z automatycznego rozsuwania
(*Avoid overlap*) — zostaje dokładnie tam, gdzie ją postawisz, a pozostałe
wartości ją omijają. Przesunięcie siedzi na węźle (`!labelDX`, `!labelDY`),
więc zapisuje się razem z drzewem.

Uzupełnienie 2026-09-02: **klik w samą liczbę** w drzewie zaznacza dokładnie
jej węzeł — niezależnie od kafelka trybu zaznaczania (*Clade*/*Node*/…), także
gdy liczba jest odsunięta od węzła. Wcześniej dawało się kliknąć tylko gałąź,
a w trybie *Clade* zaznaczało to cały klad i przesunięcie szło na wszystkie
liczby w kladzie naraz.

### Etap 6.15 — podświetlenie kladu da się cofnąć
*Tree → Hilight…* koloruje tło wybranego kladu, ale *Tree → Clear Hilighting…*
było aktywne **tylko przy zaznaczeniu**. Bez zaznaczenia ta sama funkcja czyści
wszystkie podświetlenia w drzewie — czyli jedyne „cofnij wszystko" było
niedostępne dokładnie w chwili, gdy było potrzebne. Pozycja menu jest teraz
zawsze aktywna.

W starej wersji obejście: zaznaczyć klad ponownie — wtedy menu się odblokowuje
i czyści podświetlenie w obrębie zaznaczenia.

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
