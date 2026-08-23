# Sesja E — kolizje etykiet poparcia przy drzewach „drabinkowych" (zadanie 6.6)

Niezależna od sesji A–D. Dotyka:
`src/figtree/treeviewer/treelayouts/RectilinearTreeLayout.java` (pozycje y
węzłów), ewentualnie `TreePane.java` (rysowanie node labels).

## Diagnoza (na podstawie zrzutu ekranu z 2026-08-23)

Drzewo ma długi ciąg kladów, gdzie każdy kolejny węzeł dokłada tylko jeden
takson (`Lamproderma aeneum Ron2281` → `Ron1658` → `Ron2295` → …). To
tzw. układ **drabinkowy / grzebieniasty (pectinate)**. Przy takiej topologii:

- **Min tip spacing** (dodane w etapie 2.5) pilnuje tylko odstępu między
  sąsiednimi **liśćmi** — i działa poprawnie, liście są równo rozstawione
  (widać to na zrzucie: nazwy po prawej mają równe odstępy).
- Ale **węzeł wewnętrzny** dla pary (liść, reszta poddrzewa) leży w pionie
  mniej więcej **w połowie** między swoimi dziećmi. Gdy drzewo jest
  drabinkowe, wiele takich węzłów wewnętrznych wypada blisko siebie w bardzo
  wąskim pasie — bo różnica między „środkiem 2 elementów" a „środkiem 3
  elementów" to ułamek odstępu między liśćmi, nie cały odstęp.
- Efekt: same liście się nie nakładają, ale numery poparcia (12pt, dwucyfrowe)
  na kolejnych węzłach wewnętrznych zachodzą na siebie, bo odległości między
  tymi węzłami są mniejsze niż wysokość jednej etykiety.

Czyli **Min tip spacing rozwiązuje problem liści, ale nie problem gęsto
upakowanych węzłów wewnętrznych** — to inny mechanizm i wymaga osobnej
poprawki.

## Możliwe rozwiązania (od najprostszego)

### E1. 🟢 Threshold + wyłączenie etykiet o niskim poparciu w gęstych miejscach

Najszybszy plaster: **Show only if >=** (już jest z etapu 2.3) ustawiony np. na
70–80 usunie większość drobnych wartości w drabince i naturalnie przerzedzi
kolizje, bo nie każdy węzeł ma wysokie poparcie. To nie jest „naprawa", tylko
obejście — ale możliwe, że wystarczające, i nie wymaga zmian w kodzie (do
przetestowania najpierw, zanim zaczniemy cokolwiek programować).

### E2. 🟡 Minimalny odstęp między węzłami wewnętrznymi (analogicznie do Min tip spacing)

Rozszerzyć logikę z 2.5: policzyć nie tylko odstępy między sąsiednimi liśćmi,
ale też między sąsiednimi **węzłami wewnętrznymi** (posortowanymi po y) i
dociągnąć ich minimalny odstęp do wysokości etykiety node label (albo do
osobnego ustawienia „Min node label spacing"). To wymaga w
`RectilinearTreeLayout` przeliczenia pozycji y węzłów wewnętrznych tak, żeby
zachować monotoniczność (żaden węzeł nie „wyprzedza" sąsiada) przy jednoczesnym
wymuszeniu minimalnego odstępu — klasyczny problem „isotonic regression" /
przesuwania nakładających się przedziałów, ale da się zrobić prostym
przejściem: posortować węzły po obecnym y, iterować od góry do dołu i dla
każdego węzła `y[i] = max(y[i], y[i-1] + minSpacing)`, potem przeskalować,
żeby ostatni liść nadal wypadał w tym samym miejscu co przy Min tip spacing
(albo po prostu pozwolić drzewu urosnąć jeszcze bardziej w pionie — konsekwencja
akceptowalna, to i tak się dzieje przy dużym Min tip spacing).

Uwaga: to zmienia proporcje drzewa (odległości między węzłami przestają być
ściśle „geometryczne"), więc powinno być **osobnym, wyłączanym ustawieniem**
(`nodeLabels` panel: „Avoid node label overlap" checkbox), a nie zmianą
domyślnego zachowania.

### E3. 🔴 Prawdziwe unikanie kolizji (czyli punkt 2.6 z etapu 2, świadomie odłożony)

Sprawdzać realne prostokąty tekstu (nie tylko pozycje y) i przesuwać
kolidujące etykiety na bok / zmieniać ich Position (Above/Below branch) tam,
gdzie to pomaga. Najbardziej ogólne, ale najbardziej złożone — rozważyć dopiero
jeśli E1 i E2 nie wystarczą.

## Kroki dla Claude'a

1. Najpierw **nie zmieniać kodu** — poprosić o test E1 (ustawić *Show only
   if >=* na 70 na tym samym drzewie) i zapytać, czy to wystarczy. Jeśli tak,
   zamknąć zadanie bez zmian w kodzie, tylko z notatką w CHANGELOG „jak
   radzić sobie z drabinkowymi drzewami".
2. Jeśli nie wystarczy — zaimplementować E2:
   - w `RectilinearTreeLayout` znaleźć miejsce, gdzie liczone są pozycje y
     węzłów wewnętrznych (prawdopodobnie średnia/zakres dzieci),
   - dodać checkbox **Avoid node label overlap** w panelu *Node Labels*
     (`LabelPainterController`) + spinner **Min node spacing** (pt, domyślnie
     = wysokość czcionki node label + 2pt),
   - po standardowym layoucie, jeśli checkbox włączony: posortować węzły
     wewnętrzne po y, wymusić `y[i] >= y[i-1] + minSpacing` przechodząc od
     góry, potem od dołu (dwa przejścia, żeby rozjechać w obie strony zamiast
     tylko w dół) — a jeśli po tym całość „urosła" ponad wysokość drzewa,
     przeskalować deskryptor `treeHeight`, żeby scroll/eksport PDF to
     uwzględniły (jak już robi Min tip spacing).
   - klucz ustawień: `nodeLabels.avoidOverlap`, `nodeLabels.minNodeSpacing`.
3. `/compile`, sprawdzić na realnym drzewie użytkowniczki z drabinkowym kladem.
4. Commit „Etap 6.6: unikanie kolizji etykiet poparcia w drzewach
   drabinkowych", odhaczyć 6.6, dopisać do CHANGELOG.

## Pytanie do użytkowniczki (zadać na starcie sesji)

Czy próg *Show only if >=* ustawiony na np. 70 (E1) rozwiązuje problem
wystarczająco, czy koniecznie chcesz widzieć **wszystkie** wartości poparcia
nawet w gęstych miejscach? Odpowiedź decyduje, czy w ogóle trzeba robić E2.
