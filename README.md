# MyFigTree MD

Fork [FigTree](https://github.com/rambaut/figtree) z poprawkami dla rycin drzew
filogenetycznych: czyszczenie nazw taksonów (podkreślniki, kursywa), rozsuwanie
nachodzących etykiet, słupki grup, presety publikacyjne i inne. Opis wszystkich
dodanych opcji: [doc/instrukcja.md](doc/instrukcja.md) (po polsku) i
[doc/manual.md](doc/manual.md) (po angielsku).

**Pobieranie gotowego programu** (Windows / Mac / Linux, wymaga Javy 8 lub nowszej,
np. z [adoptium.net](https://adoptium.net)): zakładka
[Releases](https://github.com/mdusza-bio/figtree/releases) → najnowsza wersja →
plik `MyFigTree-MD-*.zip`. Rozpakuj i kliknij `MyFigTree.bat` (Windows) albo
uruchom `java -jar figtree.jar`.

*A fork of FigTree with fixes for publication figures (taxon-name cleanup,
overlapping labels, group bars, publication presets). Ready-to-run jar under
Releases; the manual is [doc/manual.md](doc/manual.md) (English) and
[doc/instrukcja.md](doc/instrukcja.md) (Polish).*

## O programie / About

Fork **FigTree** Andrew Rambauta (LGPL). Dodatkowe funkcje zaprojektowała **Magdalena Dusza**
i przetestowała na drzewach ML (RAxML) i bayesowskich; kod napisano z pomocą asystenta AI
(Claude, Anthropic) pod jej kierunkiem.
Program powstał w czasie prywatnym i ze środków prywatnych, bez finansowania instytucjonalnego
ani grantowego. Udostępniany bez gwarancji.

*A fork of FigTree by Andrew Rambaut (LGPL). The additional features were designed by Magdalena
Dusza and tested on maximum-likelihood (RAxML) and Bayesian trees; the code was written with the
help of an AI assistant (Claude, Anthropic)
under her direction. Developed in her own time with private resources, without institutional
or grant funding. Provided without warranty.*

---

## Original FigTree README

_FigTree_

FigTree is designed as a graphical viewer of phylogenetic trees and as a program for producing publication-ready figures. As with most of my programs, it was written for my own needs so may not be as polished and feature-complete as a commercial program. In particular it is designed to display summarized and annotated trees produced by BEAST.

**NOTE** FigTree has been superseded by PearTree which is available as a desktop app at http://github.com/artic-network/peartree and as a webapp at http://peartree.live

