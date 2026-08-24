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
| `doc/slowniki/sluzowce.tsv` | słownik rodzaj → rodzina → rząd (63 rodzaje) | tabela pomocnicza w Excelu, patrz [slowniki/README.md](slowniki/README.md) |

Wszystkie te pliki to zwykły tekst — otwierają się w Notatniku i w Excelu.

## 3. Panel *Layout*

- **Min tip spacing** — najmniejszy odstęp w pionie między nazwami liści (w punktach).
  Drzewo samo się wydłuża, żeby nazwy się nie nakładały. Działa też przy eksporcie
  do PDF. Uwaga: pilnuje odstępu **liści**, więc przy drzewach „drabinkowych"
  etykiety poparcia na węzłach mogą nadal kolidować (zadanie 6.6).
- **Shorten branches longer than** — bardzo długie gałęzie (np. do outgrupy) są
  skracane i oznaczane `//`. Uwaga: skrócona gałąź nie odpowiada już skali.

## 4. Panel *Appearance*

- **Publication preset** — jedno kliknięcie ustawia wygląd „jak z publikacji":
  białe tło, linie 1 pt, Times New Roman 10 pt, kursywa nazw gatunkowych,
  podkreślniki zamienione na spacje, poparcie pod gałęzią z progiem 50, czarne
  kropki przy poparciu ≥ 95, układ prostokątny z wyrównanymi nazwami.
- Po jego użyciu przycisk zmienia się w **Undo preset** — cofa wszystkie
  ustawienia do stanu sprzed kliknięcia. Zapamiętany stan dotyczy tego jednego
  drzewa; po wczytaniu innego przycisk wraca do zwykłej postaci.
- **Colour by** — kolorowanie gałęzi według atrybutu **węzłów** (np. `bootstrap`).
  Uwaga: nie widać tu atrybutów przypisanych do liści (jak `family` z importu) —
  te są na razie dostępne tylko w panelach etykiet i w *Group Bars* (zadanie 6.8).

## 5. Panel *Tip Labels* (nazwy okazów)

Czyszczenie nazwy:
- **Replace '_' with space** — podkreślniki z nagłówków FASTA zamieniane na spacje.
  Zmienia tylko to, co widać na ekranie i na rycinie; plik z drzewem zostaje
  nietknięty. Domyślnie włączone.
- **Hide parts** — fragmenty do wycięcia z nazwy, po przecinku (np. `EBOV|,_contig1`).
- **Hide regex** — to samo, ale wyrażeniem regularnym. Błędne wyrażenie jest
  po prostu ignorowane.

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
  kropek zwykle nie ma). Znak krzyżówki `x` kropki nie dostaje.
- **ALL CAPS** — kończy kursywę także na członie pisanym samymi wielkimi literami
  bez cyfr, np. `KRAM` w numerze `KRAM M-1234`.
- **Join number** — skleja numer okazu rozbity przez podkreślniki w postać, w której
  się go cytuje: `KRAM M 1156` → `KRAM M-1156`, `UK100 1b` → `UK100-1b`,
  `Ron324 2 3` → `Ron324 2/3`. Nazwy, które mają już myślnik, zostają bez zmian.
- **Dot after codes** — lista skrótów kolekcji cytowanych z kropką, domyślnie `CA`
  (`UARK CA. 6-131`). Puste pole = żadnych kropek.

Wygląd:
- **Case (italic parts)** / **Case (other parts)** — wielkość liter osobno dla
  części kursywnych i pozostałych: *As is / UPPER / lower / Sentence*.
- **Bold** i kolor — osobno dla części kursywnych i pozostałych. Przycisk **x**
  czyści kolor, czyli wraca do zwykłego koloru etykiety.
- **Highlight names containing** — lista fragmentów nazw po przecinku; pasujące
  liście dostają wyróżnienie (pogrubienie / kolor) ustawione w **Highlight style**.

## 6. Panele *Node Labels* i *Branch Labels* (poparcie)

- **Position** (tylko *Node Labels*) — *At node / Above branch / Below branch*.
- **Offset X / Offset Y** — odsunięcie etykiety od węzła i od linii gałęzi
  (w punktach, mogą być ujemne).
- **Show only if >=** — próg; wartości poniżej nie są rysowane (np. bootstrap < 50).
- **Second value** + **Layout** — druga wartość obok pierwszej (np. PP obok
  bootstrapu), w formie `a / b` albo jedna nad drugą (*stacked*).

## 7. Panele *Node Shapes* / *Tip Shapes* (kropki)

- **Threshold attribute** + **Show only if >=** — kropka pojawia się tylko tam,
  gdzie wybrany atrybut osiąga próg, np. czarne kropki na węzłach z bootstrap ≥ 95.

## 8. Panel *Group Bars* (paski grup)

Pionowe paski po prawej stronie ryciny z nazwą grupy (rodzina, rząd…) obróconą
o 90°. Rysują się według **atrybutu liści** — patrz punkt 9, skąd go wziąć.
Działa tylko w układzie prostokątnym.

- **Attribute** — który atrybut liści wyznacza grupy (np. `family`).
- **Assign to selection…** — zaznacz klad (tryb *Clade*) albo kilka nazw liści,
  kliknij, podaj nazwę atrybutu i wartość. Wszystkie liście z zaznaczenia dostają
  ją naraz, a panel od razu przełącza się na ten atrybut. Wygodne dla kilku grup;
  przy większej liczbie lepszy jest import z pliku.
- **Bar width** — grubość paska (pt).
- **Gap from labels** — odstęp paska od najdłuższej nazwy (pt).
- **Font size** — wielkość napisu na pasku.
- **Backgrounds** — pastelowe tła za kladami, od wspólnego przodka do krawędzi
  etykiet; **Attribute** i **Opacity (%)** osobno dla tła.
- Kolory: na razie **nie da się ich wybrać** — paski i tła biorą wbudowaną paletę
  pastelową, w kolejności pojawiania się grup. Lista *Colour by* w *Appearance*
  nie pokazuje atrybutów liści, więc tamtą drogą się do nich nie dostaniesz.
  Wybór kolorów grupy to zadanie 6.8.

## 9. Menu *File → Import Annotations…* (Ctrl+I)

Wczytuje tabelkę, która przypisuje okazom dodatkowe informacje — to stąd biorą
się rodziny do *Group Bars*.

Plik: pierwszy wiersz to nagłówki (stają się nazwami atrybutów), pierwsza kolumna
to nazwy okazów **dokładnie jak w pliku drzewa**, z podkreślnikami:

```
name	family	order
Trichia_lutescens_MA83355	Trichiaceae	Trichiales
Arcyria_ferruginea_MA58962	Arcyriaceae	Trichiales
```

- Kolumny rozdzielone tabulatorem, przecinkiem albo średnikiem — w Excelu
  wystarczy *Zapisz jako → Tekst rozdzielany tabulatorami*.
- Puste wiersze, cudzysłowy i wiersze zaczynające się od `#` nie przeszkadzają.
- Pusta komórka = brak atrybutu, czyli pasek grupy się w tym miejscu przerywa.
- Po wczytaniu okno mówi, **ile nazw udało się dopasować** i wypisuje te, których
  w drzewie nie ma. Jeśli widzisz „Matched 0 of…", prawie zawsze znaczy to, że
  w pierwszej kolumnie są nazwy w innej postaci niż w drzewie.
- Atrybuty zapisują się przy *Save* w pliku `.tree`, więc następnym razem są już
  na miejscu.

Jak zrobić taki plik dla drzewa z setkami okazów, nie przepisując nazw ręcznie —
patrz [slowniki/README.md](slowniki/README.md).

## 10. Eksport ryciny

- **Export PDF** — okno z wyborem **Page size** (*Fit to tree / A4 portrait /
  A4 landscape*), **Margin (mm)** i **Embed fonts** (osadza czcionki w pliku;
  pierwsze użycie trwa kilka sekund).
- Kursywa, pogrubienia i kolory z panelu *Tip Labels* trafiają do PDF i SVG.

## 11. Co się zapisuje

Wszystkie ustawienia z paneli zapisują się przy *Save* w pliku `.tree` (w bloku
FigTree wewnątrz NEXUS-a) razem z atrybutami liści. Pliki zapisane zwykłym
FigTree otwierają się bez zmian, a pliki z tego programu otwarte w zwykłym
FigTree po prostu zignorują nieznane ustawienia.
