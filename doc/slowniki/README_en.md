# Dictionaries of organism groups

Tables "genus → family → order" for describing a tree (the *Group Bars* panel,
menu *File → Import Annotations…*).

- [myxomycetes.tsv](myxomycetes.tsv) — slime moulds (Myxomycetes)

A new file for another group (fungi, lichens, plants) is made the same way — just
keep the format. The file name is up to you, e.g. `fungi.tsv`, `lichens.tsv`.

## Format

A plain text file with tab-separated columns (in Excel: *Save as → Text (tab
delimited)*). The first row holds the headers:

```
genus	family	order
Trichia	Trichiaceae	Trichiales
Arcyria	Arcyriaceae	Trichiales
```
- **The first column** is the key — the name we look up (here: the genus).
- **The other columns are free.** A column header becomes an attribute name in
  the program, so `family` and `order` can be extended with anything that should
  appear on the figure or in the colours: `substrate`, `region`, `trophic_group`.
- Lines starting with `#` are comments — the program skips them. A good place
  for notes on disputed classifications.
- Empty cells are allowed: such a specimen simply gets no attribute (the group
  bar breaks at that point).

## How to use it

Load the dictionary with *File → Import Annotations…* and pick the file.
A one-word name in the first column (e.g. `Trichia`) is treated as a genus — its
family and order go to **all** specimens whose name starts with that genus.

When the tree contains a genus the dictionary does not know, the easiest way to
add it is *File → Export Group Template…* — the program writes an alphabetical
list of the genera of the open tree with empty columns; the missing rows can be
pasted into the dictionary. This way the dictionary gets more complete with every
tree.

Exceptions to the dictionary (e.g. one specimen with a disputed placement) are
handled by an extra row with the **full specimen name** — a full name always
wins over the genus, and both kinds of rows can live in one file.
