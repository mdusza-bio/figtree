# Sesja D — mysz i klawiatura (zadania 5.1–5.3, 6.5)

Niezależna od A, B, C. Dotyka `src/figtree/treeviewer/TreeViewerController.java`
(skróty, akcje) i `DefaultTreeViewer.java` (nasłuch kółka przy `JScrollPane`).

## Stan dziś

- Kółko myszy tylko przewija.
- Skróty zoom/rozsuwanie (`TreeViewerController.java`, linie ~221–229) używają
  klawisza `meta` (Cmd z Maca) → na Windowsie nie działają. Zostaje suwak
  w panelu *Layout*, co wymusza ciągłe przeskakiwanie kursorem.

## Do zrobienia

### D1. Ctrl + kółko = rozsuwanie (Expansion), Ctrl + Shift + kółko = zoom

`MouseWheelListener` na komponencie drzewa (w `DefaultTreeViewer`, tam gdzie
powstaje `JScrollPane` z `TreePane`):

```java
if (e.isControlDown()) {
    e.consume();                       // nie przewijaj
    boolean in = e.getWheelRotation() < 0;
    if (e.isShiftDown()) zoom(in); else expand(in);
}
```

Akcje `increase/decreaseVerticalExpansionAction` i `increase/decreaseZoomAction`
już istnieją w `TreeViewerController` i ruszają suwakami (dzięki temu suwak
i zapis do pliku zostają spójne). Najprościej: listener kółka dodaje sam
`TreeViewerController` (ma `treeViewer`, a przez niego komponent).

W układzie polarnym/radialnym Expansion nie istnieje — wtedy Ctrl+kółko = zoom.

### D2. Skróty klawiszowe działające na Windowsie

Zamienić `KeyStroke.getKeyStroke("meta EQUALS")` itd. na
`KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())`
(Ctrl na Windows, Cmd na Macu). Rejestrować w `WHEN_IN_FOCUSED_WINDOW`, żeby
działały niezależnie od fokusu. Analogicznie w
`src/figtree/panel/TreeViewerController.java` (wersja panelowa, mniej ważna).

### D3. Rozsuwanie „wokół kursora"

Po zmianie Expansion drzewo się wydłuża, a widok zostaje u góry — gałąź spod
kursora ucieka. Poprawka: przed zmianą zapamiętać położenie kursora jako ułamek
wysokości panelu, po zmianie przewinąć `JScrollPane` tak, żeby ten sam ułamek
był pod kursorem. ~15 linii w listenerze kółka.

## Kroki dla Claude'a

1. D2 (najprostsze) → `/compile` → sprawdzić Ctrl+= / Ctrl+-.
2. D1 → `/compile` → sprawdzić Ctrl+kółko na drzewie.
3. D3 → `/compile`.
4. Commit „Etap 5: Ctrl+kółko, skróty na Windowsie, rozsuwanie wokół kursora";
   odhaczyć 5.1–5.3 i 6.5 w kolejce; dopisać do CHANGELOG.
