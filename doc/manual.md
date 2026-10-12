# MyFigTree MD — manual

A short description of every option added in this fork. Options inherited from
the original FigTree work as before and are not described here.

Polish version: [instrukcja.md](instrukcja.md).

---
Saving the changes you make to a tree:
  1. Open file containing the tree using the MyFigTree programme.
  2. Choose *File → Save As* and give the file a new name ending in
     `.tree`, e.g. `ML_SSU.tree`, in the same folder.
  3. From now on work in the `.tree` file. Panel settings, *Style selected tips*,
     shortened branches and all other edits are saved there.

What do the new tools offer?

## 1. *Layout* panel

- **Min tip spacing** — the smallest vertical distance between tip names (in
  points). It works like *Expansion*, but you set *Min tip spacing* once for the
  final font of the figure and the tree stretches by itself so that names do not
  overlap. It also applies to PDF export. Note: it only guards the distance
  between tip names, so support values on internal nodes may still collide — for
  those there is a separate **Avoid overlap** option in the *Node Labels* panel
  (section 4).
- **Shorten branches longer than** — very long branches (e.g. to the outgroup)
  are shortened and marked with `//`. Note: a shortened branch no longer matches
  the scale. Type the value with a dot, a comma does not work, e.g. `0.2`.
- **Shorten selected branch: 2x / 3x / Full** — shortens one selected branch
  exactly two or three times and writes "2x" or "3x" above the `//` mark.
  - The shortening is stored with that branch in the `.tree` file, so it
    survives reopening.
  - Difference from *Shorten branches longer than*: that option trims all long
    branches to one length and does not say by how much.

## 2. *Appearance* panel

- **Publication preset** — one click applies your whole standard set-up to the
  tree.
- After you use it the button turns into **Undo preset** — it restores all
  settings to the state before the click. The remembered state belongs to this
  one tree; after loading another tree the button returns to its normal form.
- **Save current as preset** — from now on **Publication preset** restores
  exactly this saved state.
  Details:
  - everything is remembered **except rooting** — it depends on the particular
    tree, so the preset does not touch it; likewise the name of the support
    attribute (`bootstrap`/`label`) is detected afresh in each open tree,
  - the preset is saved to a small file `MyFigTree_publication_preset.txt` in
    your home folder — you can copy it to another computer or include it in a
    backup,
  - if a saved preset already exists, the confirmation dialog has an extra
    button **Back to original preset** — it deletes the saved preset and
    restores the built-in style,
  - hovering over **Publication preset** tells you which set-up will be used:
    your saved one or the built-in one.

## 3. *Tip Labels* panel

Cleaning up the name:
- **Replace '_' with space** — underscores from FASTA headers are shown as
  spaces. Only what you see on screen and on the figure changes; the tree file
  stays untouched. PDF export keeps the underscores hidden.
- **Hide parts** — fragments to cut out of the name, comma-separated (e.g.
  `_contig1`). It cuts literally and **everywhere** — every occurrence of the
  text disappears, also from the middle of a name, so be careful.
- **Hide regex** — the same, but with a regular expression (a pattern) that lets
  you say **where** to cut. An invalid expression is simply ignored (the name
  stays untouched).
- Which one to use — example: hiding `_new` from the end of a name:
  - *Hide parts* with `_new` also cuts `_new` from the middle — it silently
    turns `Arcyria_newtoniana_MA123` into `Arcyriatoniana MA123`;
  - *Hide regex* with `_new$` removes `_new` **only at the end of the name**
    (`$` means "end of name").
  In both fields type the underscore (`_new`, not ` new`) — cutting works on the
  raw name from the file, before underscores become spaces. Note: in a regex the
  characters `. | ( ) [ ] $ ^ * + ?` have special meanings (e.g. `|` means
  "or") — so a fragment with a bar, such as `EBOV|`, is better removed with
  *Hide parts*.

Italics and the structure of the name:
- **Italic mode** — how the italic part is chosen:
  - *Off* — nothing,
  - *First N parts* — the first N parts of the name (field **Italic first N
    parts**),
  - *Until first number* (default) — italics up to the first part that looks
    like a specimen number.
- **Not italic words** — words that stay upright inside the name (default
  `var subsp ssp f sp cf aff nov x`).
- **Dot after** — adds a dot: `var` → `var.`, `sp` → `sp.`
- **ALL CAPS** — also ends the italics at a part written in capital letters
  only, without digits, e.g. `KRAM` in a specimen number.
- **Join number** — joins a specimen number split by underscores into the form
  in which it is cited: `KRAM M 1156` → `KRAM M-1156`, `UK100 1b` → `UK100-1b`,
  `Ron324 2 3` → `Ron324 2/3`. It follows a few general rules and leaves
  anything it does not recognise unchanged.

Manual fix of a single tip (when the rules get it wrong):
- **Style selected tips...** — click the tip that came out wrong in the tree
  (Shift+click to select several), press this button and in the dialog untick
  **italic** for the parts of the name that should be upright. The classic
  case: `Trichia sordida holotypus` — the rule italicises everything because
  there is no number in the name, while `holotypus` should stay upright. You can
  also add a superscript T here.
- **bold whole label** in the same dialog makes the whole name of that tip bold.
- **type specimen (superscript T)** in the same dialog adds a superscript T
  after the name.

**Marking type specimens:**
- *in bulk, by name*: in **Highlight names containing** type whatever marks a
  type in your FASTA names (e.g. `HOLOTYPE`), then tick **Bold** under
  *Highlight style* (bold) or **superscript T** under *Type mark* (a small
  "T") — or both. The word `HOLOTYPE` itself can be hidden with **Hide parts**,
  so only the "T" remains on the figure;
- *by hand, specimen by specimen*: select the tip, **Style selected tips...**,
  and tick *bold whole label* or *type specimen (superscript T)*.

Appearance:
- **Case (italic parts)** / **Case (other parts)** — letter case separately for
  the italic parts and the rest: *As is / UPPER / lower / Sentence*.
- **Bold** and colour — separately for the italic parts and the rest.
- **Highlight names containing** — comma-separated list of name fragments;
  matching tips get the highlight (bold / colour) set in **Highlight style**.

## 4. *Node Labels* and *Branch Labels* panels

- **Position** — *At node / Above branch / Below branch*.
- **Offset X / Offset Y** — shift of the labels away from the node and the
  branch (in points, may be negative).
- **Show only if >=** — values below are not shown.
- **Second value** + **Layout** — a second value next to the first (e.g. PP next
  to bootstrap), as `a / b` or one above the other (*stacked*).
- **Second only if >=** — a separate threshold for the second value (e.g. `0.95`
  for PP). Empty field = the second value is always shown.
- **Too low shows as** — type `-` to show it when a value is below the threshold
  or missing altogether.
- **Crowded values → Avoid overlap** — spreads support values out when they land
  on top of each other. Sometimes this does not work well, but there are other
  tools too. If a number has to move further than its own height, a thin grey
  **leader line** is drawn from the number to its node, so there is no doubt
  which branch it belongs to. The tree itself does not change — only the text
  moves; values that do not collide stay where they are.

  Next to it is a second checkbox **White backing** — every support value gets a
  **rectangle in the background colour** underneath, to taste.

- **Dragging a number with the mouse** — the simplest way to move **one** value
  that landed in the wrong place while the rest of the tree looks fine: hover
  over the number (the cursor turns into arrows ✥), press the left button and
  **drag it where it should be**. Dragging it back near its original place
  restores automatic placement. For finer control use the arrow keys: one press
  moves by **1 point**, with **Shift** held by **10 points**. This also works on
  several selected values at once (they move together).
  **Support Dots** — a dot marking full support.

## 5. *Node Shapes* / *Tip Shapes* panels

- **Threshold attribute** + **Show only if >=** — a dot appears only where the
  chosen attribute reaches the threshold, e.g. black dots on nodes with
  bootstrap ≥ 95. This is the older, simpler tool: the dot is always on the node
  and follows a single value. For dots of full support the *Support Dots* panel
  (above) is better.

## 6. *Group Bars* panel

Vertical bars on the right of the figure with the group name (family, order…)
rotated by 90°. They are drawn from a **tip attribute** — see section 8 for
where to get one.

- **Attribute** — which tip attribute defines the groups (e.g. `family`). The
  list shows only attributes loaded from a file or given with *Assign to
  selection…*.
- **Assign to selection…** — select a clade (*Clade* mode) or a few tip names,
  click, give the attribute name and the value. All selected tips get it at
  once and the panel switches to that attribute. Handy for a few groups or for
  labelling the outgroup; for more groups importing from a file is better.
- **Bar width** — thickness of the bar (pt).
- **Gap from labels** — distance of the bar from the longest name (pt).
- **Font size** — size of the text on the bar.
- **Text reads** — direction of the group name.
- **Line breaks in the bar text**: `|` in the value starts a new line
  (`ECHINO-|STELIALES`).
- **Hide names that do not fit** — a name longer than the height of the group is
  omitted instead of running into its neighbours (the bar stays). For such
  groups: break the name with `|` or use an asterisk (explained below).
- **Italic names** — bar names in italics.
- **A name that does not fit: an asterisk instead of the name.** For a small
  group select its specimens, *Assign to selection…*, the attribute of the bars
  (e.g. `order`), value `*` or `**`. Asterisks are drawn upright and larger than
  the names. The asterisks can be explained in a legend in the *Figure Text*
  panel.
- **Backgrounds** — pastel backgrounds behind clades, with their own
  **Attribute** and **Opacity (%)**.
  - **Gap between (pt)** — a white gap between neighbouring backgrounds.
  - **Gradient**
- **Clade names in backgrounds**
  - **Name size** — size of the text (default 11). At 10 and below Java on
    Windows draws bold the same as regular text — this is not a bug of the
    program.
  - **Bold names** — makes all names bold at once.
  - **Italic names** — all names in backgrounds in italics (see the note on
    italics for bars).
  - **Not italic words** — words that stay upright in italic names, separated by
    commas or spaces, case does not matter; default `Outgroup`, so in
    `Outgroup Trichiales` only *Trichiales* is slanted.
  - **The "Outgroup" label.** Select the outgroup clade (*Clade* mode), *Assign
    to selection…*, the attribute of the backgrounds (e.g. `family`), value
    `Outgroup` or `Outgroup|Trichiales` for two lines.
- **Colours…** — your own colour for every group.
  - **Save colours…** writes the colours from the dialog to a small table
    (default `kolory_grup.tsv`): three tab-separated columns — attribute,
    group, colour `#RRGGBB`, one group per line. It can be opened and edited in
    Excel or Notepad.
  - **Load colours…** loads such a table on another tree: groups with the same
    attribute name and group name get the same colours, the rest stays as it
    was. The dialog tells you how many groups of this tree received a colour.
    This is how colours travel from figure to figure — the genus dictionary
    stays unchanged.

## 7. *Figure Text* panel (text in a corner of the figure)

A few lines of your own text laid over the finished figure — e.g. an
explanation of the asterisks from the group bars.

The text is saved in the `.tree` file (`figureText.*`; line breaks as `|`). It
goes into PDF/SVG/PNG like the rest of the figure.

## 8. *File → Import Annotations…* menu (Ctrl+I)

Loads a table that assigns extra information to specimens — this is where the
families for *Group Bars* come from.

File: the first row holds the headers (they become attribute names), the first
column holds genus names, then family etc. (you decide what you want).
- Columns separated by tabs, commas or semicolons — in Excel just *Save as →
  Text (tab delimited)* (*Unicode Text* and *CSV* work too).
- The program does not read `.xlsx` itself — save it as text first.

### *File → Export Group Template…* menu

The reverse of the import: it writes the **list of genera** of the open tree
(alphabetical, without duplicates) to a file with empty `family` and `order`
columns. Fill the file in Excel or Notepad and load it back with *Import
Annotations…*

## 9. Exporting the figure

- **Export PDF** — a dialog with **Page size** (*Fit to tree / A4 portrait / A4
  landscape*), **Margin (mm)** and **Embed fonts** (embeds the fonts in the
  file; the first use takes a few seconds).
- Italics, bold and colours from the *Tip Labels* panel go into PDF and SVG.
  Since version 1.1, when the chosen font has no separate italic or bold file on
  this computer, the PDF takes the style from a standard font (Times-Italic /
  Helvetica-Oblique) instead of losing it. Should italics still be missing:
  untick *Embed fonts*, or choose Times New Roman or Arial (the **Font** button
  in the *Setup* row of the *Tip Labels* panel).

## 10. Changing the original specimen name in the file

*Annotate* changes the label only on the figure — the original name stays in
the file and the new one is written next to it as `!name` and saved with the
tree. The name can also be changed in the tree file itself, e.g. in Notepad.

1. Make a copy of the file — just in case.
2. Open the file in Notepad and press **Ctrl+H** (Replace). In "Find what" type
   the old name, in "Replace with" the new one.
3. Click **Replace All** — it has to be *all*: in a file saved by the program
   (NEXUS) the same name appears in several places.
4. Save and open in the program.

Keep in mind:

- **The new name without special characters** — only letters, digits,
  underscores, possibly dots and hyphens. Spaces, commas, colons, semicolons and
  brackets are control characters of the tree format — **they will break the
  file**.
- **Every specimen must keep a unique name.**

## 11. About

MyFigTree MD is a fork of **FigTree** by Andrew Rambaut (LGPL licence, https://github.com/rambaut/figtree).
The additional features were designed by **Magdalena Dusza** and tested on real maximum-likelihood
(RAxML) and Bayesian phylogenetic trees;
the code was written with the help of an AI assistant (Claude, Anthropic) under her direction.
The program was developed in her own time and with private resources, without institutional or grant funding.
It is provided without warranty. Please check figures before publication.

Code and new versions: https://github.com/mdusza-bio/figtree
