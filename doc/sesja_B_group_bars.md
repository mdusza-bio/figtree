# Sesja B — Group Bars: skąd program ma znać rodziny (zadanie 6.3)

Niezależna od sesji A, C, D. Dotyka: `src/figtree/application/FigTreeFrame.java`
(import adnotacji), ewentualnie nowy importer, `GroupBarController.java`.

## Dlaczego nic się nie dzieje

Panel *Group Bars* rysuje paski według **atrybutu liści** — dodatkowej
informacji przypiętej do każdego okazu (np. `family=Trichiaceae`). FigTree sam
nie wie, do jakiej rodziny należy gatunek; w zwykłym pliku z drzewem (Newick /
NEXUS z IQ-TREE, RAxML, MrBayes) takich informacji nie ma. Lista *Attribute*
w panelu jest więc pusta (albo ma tylko techniczne rzeczy) i paski nie mają
z czego powstać.

Dziś jedyny sposób w FigTree: zaznaczyć liście → *Annotate* → wpisać nazwę
atrybutu i wartość. Działa, ale dla 100 okazów to katorga.

## Rozwiązanie: plik z przypisaniem + wygodne przypisywanie

### B1. Format pliku (do zrobienia w Excelu → „zapisz jako tekst rozdzielany tabulatorami")

```
name	family	order
Trichia_lutescens_MA83355	Trichiaceae	Trichiales
Arcyria_ferruginea_MA58962	Arcyriaceae	Trichiales
Dianema_depressum_MA80673	Dianemataceae	Trichiales
```

Pierwszy wiersz = nagłówki (nazwy atrybutów). Dopasowanie po **surowej** nazwie
z pliku drzewa (z podkreślnikami). Okazy nieobecne w pliku → bez atrybutu
(pasek się tam przerywa).

### B2. Menu *File → Import Annotations…*

FigTree **już ma** taką funkcję — sprawdzić w `FigTreeFrame.java`
(`importAnnotations` / `doImport…`) jaki format plik musi mieć. Możliwe, że
wystarczy istniejąca funkcja, tylko nikt o niej nie wie. Wtedy sesja sprowadza
się do: przetestować, opisać w CHANGELOG, poprawić komunikat (ile nazw
dopasowano, które nie pasują).

### B3. Przypisz z zaznaczenia

W panelu *Group Bars* przycisk **Assign to selected…**: zaznaczasz klad (tryb
*Clade*), klikasz, wpisujesz „Trichiaceae" → zaznaczone liście dostają wybrany
w combo atrybut. To samo co *Annotate*, tylko od razu z właściwym atrybutem.

### B4. Zapis

Atrybuty liści FigTree zapisuje w `.tree` automatycznie — wczytane raz,
następnym razem są od razu.

## Kroki dla Claude'a

1. Sprawdzić istniejący import adnotacji w `FigTreeFrame.java` (szukać
   `Annotations`, `importCharacters`). Przetestować na pliku jak w B1.
2. Jeśli działa — poprawić komunikat (dopasowano X/Y, lista niedopasowanych),
   dopisać instrukcję do `CHANGELOG.md`. Jeśli nie — napisać mały importer TSV
   i podpiąć pod menu File.
3. B3: przycisk w `GroupBarController` (zaznaczenie: sprawdzić jak robi to
   *Annotate* w `FigTreeFrame`).
4. Przygotować `doc/przyklad.tree` (5–10 liści) i pasujący
   `doc/przyklad_rodziny.tsv` do testów — albo poprosić użytkowniczkę o własne
   drzewo testowe.
5. `/compile`, commit „Etap 6.3: import rodzin z pliku + przypisz z zaznaczenia",
   odhaczyć 6.3, dopisać do CHANGELOG.
