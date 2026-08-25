# MyFigTree MD

## Czym jest to repozytorium

To **prywatny fork FigTree** (`mdusza-bio/figtree`, fork `rambaut/figtree`) robiony
**wyłącznie na własne potrzeby użytkowniczki** — badaczki filogenetyki, która używa
programu do przygotowywania rycin drzew do publikacji.

**To nie jest wkład rozwojowy do oryginalnego projektu.** Zmiany nie będą zgłaszane
jako pull requesty do `rambaut/figtree` i nie muszą spełniać oczekiwań upstreamu —
mają po prostu sprawiać, że program lepiej pasuje do jej sposobu pracy. Nie proponuj
więc kontaktu z autorem oryginału, otwierania issue upstream ani "czystości" zmian pod
kątem przyszłego merge'a. Priorytetem jest praktyczna wygoda użytkowniczki.

Oryginalny FigTree jest projektem archiwalnym (autor odsyła do następcy, PearTree),
więc nie ma sensu czekać na poprawki z góry — wszystko robimy tutaj.

## Kontekst użytkowniczki

Pracuje po polsku, **pierwszy raz używa gita i nie jest osobą techniczną**. Wyjaśniaj
kroki prostym językiem, bez żargonu (albo z krótkim tłumaczeniem terminu przy pierwszym
użyciu). Preferuje małe, pojedyncze zmiany, które da się od razu obejrzeć w działającym
programie, zamiast dużych refaktorów, których nie jest w stanie zweryfikować.

### Problemy, które chce rozwiązać

Te dwie rzeczy są powodem powstania forka — to naturalne pierwsze kandydatki do zmian:

1. **Podkreślniki w nazwach okazów.** Nazwy taksonów pochodzą z nagłówków FASTA i mają
   `_` zamiast spacji. FigTree nie daje sposobu, żeby to wyczyścić na potrzeby ryciny.
2. **Nachodzące na siebie etykiety.** Nazwy gałęzi i wartości bootstrap (ML) kolidują
   ze sobą na gęstszych drzewach.

Dotychczasowe obejście: eksport do PDF i ręczne poprawianie każdej etykiety w CorelDRAW
na pożyczonym komputerze (program płatny, nie ma go na własność). Celem jest, żeby to
obejście przestało być potrzebne.

## Budowanie

W systemie **nie ma** zainstalowanego JDK ani Anta na stałe — oba są rozpakowane
lokalnie w `C:\Users\Magdalena Dusza\tools\` i trzeba na nie wskazać zmiennymi
środowiskowymi. W systemie jest tylko JRE (bez kompilatora), więc `JAVA_HOME` musi
wskazywać na rozpakowany JDK, nie na `C:\Program Files (x86)\Java\jre-1.8`.

Najprościej użyć skilla **`/compile`** (zdefiniowanego w `.claude/skills/compile/`)
albo skryptu `build.bat`. Ręcznie, w bashu:

```bash
export JAVA_HOME="/c/Users/Magdalena Dusza/tools/jdk8u502-b07"
export ANT_HOME="/c/Users/Magdalena Dusza/tools/apache-ant-1.10.15"
cd "/c/Users/Magdalena Dusza/workspeace/my_figtree"
"$ANT_HOME/bin/ant"
```

Wynik: `dist/figtree.jar` (to jest plik do uruchamiania; `figtreepanel.jar` i
`figtree-pdf.jar` to komponenty poboczne).

Uruchomienie:

```bash
"$JAVA_HOME/bin/java" -jar dist/figtree.jar
```

Użytkowniczka woli kompilować **raz na koniec sesji pracy**, po kilku zmianach naraz —
nie po każdej pojedynczej edycji.

### ⚠ Przed kompilacją poproś o zamknięcie działającego programu

Ant kasuje i tworzy `dist/figtree.jar` od nowa. Jeśli w tym czasie **program jest
uruchomiony**, działająca kopia traci dostęp do klas, których jeszcze nie zdążyła
wczytać — i od tej chwili wszystko, co sięga po nową klasę (okna dialogowe,
*Tree → Annotate*, *File → Export Trees*, **zapis pliku**), umiera bez żadnego
komunikatu.

**To nie jest teoria: 2026-08-25 kosztowało to plik z drzewem** — nieudany *Save*
zdążył wyczyścić plik użytkowniczki, zanim się wywalił (`ML_SmE.raxml.support`,
0 bajtów). Drzewo udało się odtworzyć RAxML-em z `.bestTree` + `.bootstraps`,
a sam zapis został uodporniony (zapis do pliku tymczasowego i podmiana dopiero po
sukcesie — etap 6.13), ale **stara wersja programu, którą użytkowniczka ma
akurat otwartą, nadal jest podatna**.

Zasada: zanim uruchomisz `/compile`, napisz żeby zamknęła program; po kompilacji
podaj komendę do ponownego uruchomienia.

## Struktura kodu

Java 8, Swing/AWT, budowane Antem (`build.xml`). Biblioteki jako jary w `lib/`
(m.in. `jebl.jar` — biblioteka filogenetyczna dostarczająca `Tree`, `Node`, `Taxon`).

- `src/figtree/application/` — start programu, okno główne, import/eksport
  (`FigTreeApplication.java` zawiera `main()` i nazwę programu wyświetlaną w tytule okna;
  `FigTreeNexusImporter/Exporter` obsługują format NEXUS z blokiem FigTree)
- `src/figtree/treeviewer/` — rdzeń wyświetlania drzewa (`TreePane.java` to główny
  komponent rysujący)
  - `painters/` — rysowanie etykiet, węzłów, skali. **`BasicLabelPainter.java` decyduje,
    jaki tekst trafia na etykietę** (metoda `getLabel()`) — tu wchodzi temat podkreślników
    i wartości bootstrap
  - `treelayouts/` — rozmieszczenie gałęzi (prostokątne, polarne, radialne) — tu wchodzi
    temat nachodzących etykiet
  - `decorators/` — kolory i style zależne od atrybutów
- `src/figtree/panel/`, `src/figtree/ui/` — komponenty interfejsu

## Konwencje

Nazwy klas i pakietów zostają jak w oryginale (`FigTree*`, `figtree.*`) — zmieniana
jest tylko **wyświetlana** nazwa programu, żeby odróżnić ją od zwykłego FigTree
zainstalowanego równolegle na tym komputerze. Zmiana nazw klas byłaby dużym, ryzykownym
refaktorem bez korzyści dla użytkowniczki.
