# Sesja C — cofanie presetu publikacyjnego (zadanie 6.4)

Niezależna od A, B, D. Dotyka tylko
`src/figtree/treeviewer/TreeAppearanceController.java`.

## Problem

Przycisk *Publication preset* nadpisuje kilkanaście ustawień naraz i nie ma
drogi powrotu poza ręcznym odklikaniem.

## Rozwiązanie

Preset działa już przez `controlPalette.getSettings(map)` → nadpisanie →
`controlPalette.setSettings(map)`. Wystarczy:

1. Przed nadpisaniem **zachować kopię** pełnej mapy ustawień
   (`Map<String,Object> beforePreset`).
2. Po zastosowaniu zmienić napis przycisku na **Undo preset**; kliknięcie
   wywołuje `controlPalette.setSettings(beforePreset)` i przywraca napis.
3. Kopia żyje tylko do zamknięcia okna / wczytania innego drzewa.
4. Bonus (jeśli proste): przycisk **Save as my preset** zapisujący bieżące
   ustawienia do pliku `~/.myfigtree_preset.txt` (klucz=wartość);
   *Publication preset* najpierw próbuje wczytać ten plik, a jak go nie ma —
   używa wbudowanych wartości. Preset staje się wtedy „Twoim" presetem.

## Kroki dla Claude'a

1. Pole `beforePreset` w `TreeAppearanceController`.
2. Akcja przycisku: jeśli `beforePreset == null` → zapisz kopię, zastosuj
   preset, napis „Undo preset"; w przeciwnym razie → przywróć, wyzeruj, napis
   „Publication preset".
3. Uwaga: `setSettings` niektórych kontrolerów pomija brakujące klucze — przy
   przywracaniu przekazać **pełną** mapę sprzed presetu.
4. (Bonus) zapis/odczyt przez `java.util.Properties`; kolory i enumy jako tekst,
   tak jak robi to `FigTreeNexusExporter`.
5. `/compile`, commit „Etap 6.4: Undo presetu publikacyjnego", odhaczyć 6.4,
   dopisać do CHANGELOG.
