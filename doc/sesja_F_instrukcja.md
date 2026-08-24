# Sesja F — instrukcja obsługi (zadanie 6.10)

Niezależna od pozostałych sesji: nie rusza kodu, tylko `doc/instrukcja.md`.
Można ją robić w kawałkach, najlepiej z otwartym programem obok.

## Co już jest

`doc/instrukcja.md` — pierwsza wersja, napisana z kodu i CHANGELOG-a: uruchamianie,
pliki towarzyszące, panele *Layout*, *Appearance*, *Tip Labels*, *Node/Branch
Labels*, *Node/Tip Shapes*, *Group Bars*, import adnotacji, eksport, co się
zapisuje w `.tree`.

Czego brakuje: zrzutów ekranu, przykładów „przed / po", przepisu na typową pracę
od otwarcia drzewa do gotowego PDF-a, i sprawdzenia, czy opisy zgadzają się z tym,
co użytkowniczka faktycznie widzi na ekranie.

## Kroki

1. **Przejść instrukcję z programem obok**, panel po panelu. Przy każdej opcji
   sprawdzić: czy nazwa w instrukcji zgadza się z napisem w programie, czy opis
   mówi to, co opcja naprawdę robi, i czy zdanie jest zrozumiałe bez znajomości
   kodu. Poprawki nanosić od razu.
2. **Zrzuty ekranu.** Konwencja jak dotąd: `doc/shot1.png`, `shot2.png`… Przydatne:
   panel *Tip Labels* z rozwiniętym *Italic mode*, panel *Group Bars* z paskami
   i tłami, okno podsumowania po *Import Annotations…*, okno eksportu PDF.
   Wstawiać w markdownie jako `![opis](shot9.png)`.
3. **Przykłady „przed / po"** dla rzeczy, które trudno opisać słowami — zwłaszcza
   *Join number* (`KRAM M 1156` → `KRAM M-1156`) i *Until first number*. Wystarczy
   tabelka z dwiema kolumnami tekstu, bez obrazków.
4. **Rozdział „typowa ścieżka pracy"** — przepis od początku do końca: otwórz
   drzewo → *Publication preset* → popraw nazwy w *Tip Labels* → wczytaj rodziny
   → włącz *Group Bars* → *Export PDF*. Napisany tak, żeby dało się go wykonać
   bez czytania reszty instrukcji.
5. **Rozdział „częste problemy"** — na razie znane: „Matched 0 of…" przy imporcie
   (nazwy w innej postaci niż w drzewie), puste *Attribute* w *Group Bars* (brak
   zaimportowanych atrybutów), brak `family` w *Colour by* (zadanie 6.8),
   nakładające się etykiety poparcia przy drzewach drabinkowych (zadanie 6.6).
6. **Zasada na przyszłość:** każdy skończony etap dopisuje swój kawałek do
   `doc/instrukcja.md` w tym samym commicie, w którym zmienia kod. Wtedy
   instrukcja nie zostaje w tyle. Dopisać tę zasadę do `CLAUDE.md`.

## Uwagi

- Instrukcja jest po polsku, ale **nazwy opcji zostają po angielsku**, bo takie są
  w programie (`Italic mode`, `Group Bars`). Tłumaczenie nazw utrudniłoby
  szukanie ich na ekranie.
- Nie opisujemy opcji odziedziczonych po zwykłym FigTree — do nich jest oryginalna
  dokumentacja. Instrukcja dotyczy tego, co dołożone w forku.
