/*
 * LabelStyle.java
 *
 * Part of the MyFigTree fork. Per-part styling of a label: which parts of a
 * name are italic/bold/coloured and how their case is changed (Etap 3).
 */

package figtree.treeviewer.painters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes how the parts of a (tip) name are styled, and turns a list of parts
 * into a list of styled runs to draw: which parts are italic (3.1 - 3.3, 6.1),
 * how their case is changed, and how the collection number is put back together.
 * On top of that, "highlight": tips whose RAW name contains one of the given
 * substrings get bold and/or a colour on the whole label (3.4).
 *
 * <p>The "advanced template" of 3.5 was removed in 6.2 - the italic modes of 6.1
 * cover what it was there for.</p>
 */
public class LabelStyle {

    public enum Case {
        AS_IS("As is"), UPPER("UPPER"), LOWER("lower"), SENTENCE("Sentence");

        Case(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static Case fromString(String s) {
            for (Case c : values()) {
                if (c.name().equalsIgnoreCase(s) || c.name.equalsIgnoreCase(s)) {
                    return c;
                }
            }
            return AS_IS;
        }

        public String apply(String text) {
            switch (this) {
                case UPPER:
                    return text.toUpperCase();
                case LOWER:
                    return text.toLowerCase();
                case SENTENCE:
                    if (text.length() == 0) return text;
                    return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
                default:
                    return text;
            }
        }

        private final String name;
    }

    /**
     * How the italic part of a name is chosen (Etap 6.1).
     */
    public enum ItalicMode {
        OFF("Off"),
        FIRST_N("First N parts"),
        UNTIL_NUMBER("Until first number");

        ItalicMode(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static ItalicMode fromString(String s) {
            for (ItalicMode m : values()) {
                if (m.name().equalsIgnoreCase(s) || m.name.equalsIgnoreCase(s)) {
                    return m;
                }
            }
            return FIRST_N;
        }

        private final String name;
    }

    /**
     * The style of one group of parts.
     */
    public static class PartStyle {
        public boolean italic = false;
        public boolean bold = false;
        public Case caseMode = Case.AS_IS;
        public Color colour = null;

        public boolean isPlain() {
            return !italic && !bold && caseMode == Case.AS_IS && colour == null;
        }
    }

    /**
     * A piece of text drawn with one font style and colour.
     */
    public static class Run {
        public Run(String text, boolean italic, boolean bold, Color colour) {
            this.text = text;
            this.italic = italic;
            this.bold = bold;
            this.colour = colour;
        }

        public final String text;
        public final boolean italic;
        public final boolean bold;
        /** null = use the painter's normal label colour */
        public final Color colour;
    }

    // ---- simple settings (3.1 - 3.3)

    public int getItalicParts() {
        return italicParts;
    }

    public void setItalicParts(int italicParts) {
        this.italicParts = Math.max(0, italicParts);
    }

    // ---- smarter italics (6.1)

    public ItalicMode getItalicMode() {
        return italicMode;
    }

    public void setItalicMode(ItalicMode italicMode) {
        this.italicMode = (italicMode == null ? ItalicMode.FIRST_N : italicMode);
    }

    public String getNonItalicWords() {
        return nonItalicWords;
    }

    /**
     * @param words rank/qualifier words that stay upright inside an italic name,
     *              separated by spaces or commas. A trailing dot and the case are
     *              ignored when matching, so "var", "Var" and "var." are the same
     *              word.
     */
    public void setNonItalicWords(String words) {
        this.nonItalicWords = (words == null ? "" : words);
        nonItalicWordSet.clear();
        for (String w : this.nonItalicWords.split("[\\s,]+")) {
            String key = normaliseWord(w);
            if (key.length() > 0) {
                nonItalicWordSet.add(key);
            }
        }
    }

    public boolean isAddRankDots() {
        return addRankDots;
    }

    /**
     * @param addRankDots write "var." / "sp." even when the raw name has no dot
     */
    public void setAddRankDots(boolean addRankDots) {
        this.addRankDots = addRankDots;
    }

    public boolean isFormatCollectionNumber() {
        return formatCollectionNumber;
    }

    /**
     * @param formatCollectionNumber when true, the collection number that the
     *                               underscores broke into separate parts is put
     *                               back together the way it is cited, see
     *                               {@link #collectionNumber}
     */
    public void setFormatCollectionNumber(boolean formatCollectionNumber) {
        this.formatCollectionNumber = formatCollectionNumber;
    }

    public String getDotCodes() {
        return dotCodes;
    }

    /**
     * @param dotCodes collection abbreviations that are cited with a dot, e.g. the
     *                 CA of "UARK CA. 6-131" (separated by spaces or commas)
     */
    public void setDotCodes(String dotCodes) {
        this.dotCodes = (dotCodes == null ? "" : dotCodes);
        dotCodeSet.clear();
        for (String c : this.dotCodes.split("[\\s,]+")) {
            String code = normaliseWord(c);
            if (code.length() > 0) {
                dotCodeSet.add(code);
            }
        }
    }

    public boolean isUpperCaseIsNumber() {
        return upperCaseIsNumber;
    }

    /**
     * @param upperCaseIsNumber when true, an ALL-CAPS part without any digit (a
     *                          collection code such as "BR") also ends the italics
     *                          in {@link ItalicMode#UNTIL_NUMBER}
     */
    public void setUpperCaseIsNumber(boolean upperCaseIsNumber) {
        this.upperCaseIsNumber = upperCaseIsNumber;
    }

    /**
     * Decides, part by part, what is drawn in italics:
     * <ul>
     *     <li>{@link ItalicMode#OFF} - nothing;</li>
     *     <li>{@link ItalicMode#FIRST_N} - the first {@link #getItalicParts()} parts;</li>
     *     <li>{@link ItalicMode#UNTIL_NUMBER} - everything up to (but not including)
     *         the first "number-like" part, i.e. the collection number. Words from
     *         {@link #getNonItalicWords()} (var, sp, ...) stay upright but do NOT
     *         end the italics for the parts after them.</li>
     * </ul>
     */
    public boolean[] italicMask(String[] parts) {
        boolean[] mask = new boolean[parts.length];
        if (italicMode == ItalicMode.FIRST_N) {
            int n = Math.min(italicParts, parts.length);
            for (int i = 0; i < n; i++) {
                mask[i] = true;
            }
        } else if (italicMode == ItalicMode.UNTIL_NUMBER) {
            for (int i = 0; i < parts.length; i++) {
                if (isNumberLike(parts[i])) {
                    break;
                }
                mask[i] = !isNonItalicWord(parts[i]);
            }
        }
        return mask;
    }

    /**
     * @return true if the part looks like a collection number rather than a piece
     *         of the name: it contains a digit (or, optionally, is ALL CAPS)
     */
    public boolean isNumberLike(String part) {
        if (part == null || part.length() == 0) {
            return false;
        }
        for (int i = 0; i < part.length(); i++) {
            if (Character.isDigit(part.charAt(i))) {
                return true;
            }
        }
        if (upperCaseIsNumber && part.length() > 1) {
            boolean anyLetter = false;
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                if (Character.isLetter(c)) {
                    anyLetter = true;
                    if (!Character.isUpperCase(c)) {
                        return false;
                    }
                }
            }
            return anyLetter;
        }
        return false;
    }

    public boolean isNonItalicWord(String part) {
        return nonItalicWordSet.contains(normaliseWord(part));
    }

    private static String normaliseWord(String word) {
        if (word == null) {
            return "";
        }
        String w = word.trim();
        while (w.endsWith(".")) {
            w = w.substring(0, w.length() - 1);
        }
        return w.toLowerCase();
    }

    /**
     * @return the text of one part as it is drawn (with the dot that FASTA headers
     *         usually lack, when that option is on)
     */
    private String displayPart(String part) {
        if (addRankDots
                && italicMode == ItalicMode.UNTIL_NUMBER
                && isNonItalicWord(part)
                && !part.endsWith(".")
                && !isHybridMarker(part)) {
            return part + ".";
        }
        return part;
    }

    /**
     * @return the index of the first part that belongs to the collection number
     *         (parts.length when the name has no number at all)
     */
    public int numberStart(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (isNumberLike(parts[i])) {
                return i;
            }
        }
        return parts.length;
    }

    /**
     * Puts the collection number back together the way it is cited, undoing the
     * split that the underscores of a FASTA header cause. The parts from
     * {@code start} on are read as: herbarium codes, then the number itself, then
     * anything left over (a word such as "new"):
     * <ul>
     *     <li>a code that is a single capital letter binds to the number that
     *         follows it: <code>KRAM M 1156</code> -&gt; <code>KRAM M-1156</code>;</li>
     *     <li>a code from {@link #getDotCodes()} is written with a dot:
     *         <code>UARK CA 6 131</code> -&gt; <code>UARK CA. 6-131</code>;</li>
     *     <li>the pieces of a herbarium number are joined with hyphens:
     *         <code>UK100 1b</code> -&gt; <code>UK100-1b</code>;</li>
     *     <li>after a collector's number (a name with capital, small letters and
     *         digits) the numbers that follow are the parts one collection was
     *         split into, so they are joined with a slash:
     *         <code>Ron324 2 3</code> -&gt; <code>Ron324 2/3</code>.</li>
     * </ul>
     *
     * @return the pieces of the number as they are drawn
     */
    private List<String> collectionNumber(String[] parts, int start) {
        List<String> number = new ArrayList<String>();
        int n = parts.length;
        if (!formatCollectionNumber) {
            for (int i = start; i < n; i++) {
                number.add(parts[i]);
            }
            return number;
        }

        int i = start;
        // the herbarium codes in front of the number: KRAM, UARK CA, KRAM M-1156
        while (i < n && isLetterCode(parts[i])) {
            if (parts[i].length() == 1 && i + 1 < n && startsWithDigit(parts[i + 1])) {
                number.add(parts[i] + "-" + parts[i + 1]);
                i += 2;
            } else {
                number.add(withDot(parts[i]));
                i++;
            }
        }

        // the number itself: everything that still has a digit in it
        List<String> digits = new ArrayList<String>();
        while (i < n && containsDigit(parts[i])) {
            digits.add(parts[i]);
            i++;
        }
        if (!digits.isEmpty()) {
            int from = 0;
            String separator = "-";
            if (isCollectorNumber(digits.get(0))) {
                // a field number stands on its own; what follows is "2 of 3"
                number.add(digits.get(0));
                from = 1;
                separator = "/";
            }
            if (from < digits.size()) {
                StringBuilder joined = new StringBuilder();
                for (int k = from; k < digits.size(); k++) {
                    if (joined.length() > 0) {
                        joined.append(separator);
                    }
                    joined.append(digits.get(k));
                }
                number.add(joined.toString());
            }
        }

        // whatever is left (e.g. "new") is drawn as it is
        while (i < n) {
            number.add(parts[i]);
            i++;
        }
        return number;
    }

    /** a herbarium code such as KRAM, UARK, CA or the M of "KRAM M": capitals only */
    private static boolean isLetterCode(String part) {
        if (part.length() == 0) {
            return false;
        }
        for (int i = 0; i < part.length(); i++) {
            char c = part.charAt(i);
            if (!Character.isLetter(c) || !Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true for a collector's number such as "Ron324" or "Lado25434":
     *         a capital, small letters and digits (a herbarium number such as
     *         UK100 or MA83355 has no small letters)
     */
    private static boolean isCollectorNumber(String part) {
        if (part.length() == 0 || !Character.isUpperCase(part.charAt(0))) {
            return false;
        }
        boolean small = false;
        boolean digit = false;
        for (int i = 1; i < part.length(); i++) {
            char c = part.charAt(i);
            if (Character.isLowerCase(c)) {
                small = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            }
        }
        return small && digit;
    }

    private String withDot(String code) {
        if (code.endsWith(".") || !dotCodeSet.contains(normaliseWord(code))) {
            return code;
        }
        return code + ".";
    }

    private static boolean containsDigit(String part) {
        for (int i = 0; i < part.length(); i++) {
            if (Character.isDigit(part.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithDigit(String part) {
        return part.length() > 0 && Character.isDigit(part.charAt(0));
    }

    /** the hybrid sign is never followed by a dot */
    private static boolean isHybridMarker(String part) {
        return part.equalsIgnoreCase("x") || part.equals("\u00d7");
    }

    public PartStyle getItalicGroup() {
        return italicGroup;
    }

    public PartStyle getOtherGroup() {
        return otherGroup;
    }

    // ---- highlight (3.4)

    public String getHighlight() {
        return highlight;
    }

    /**
     * @param highlight comma-separated substrings; a tip whose raw name contains
     *                  any of them is highlighted
     */
    public void setHighlight(String highlight) {
        this.highlight = (highlight == null ? "" : highlight);
        highlightList.clear();
        for (String s : this.highlight.split(",")) {
            s = s.trim();
            if (s.length() > 0) {
                highlightList.add(s);
            }
        }
    }

    public boolean isHighlightBold() {
        return highlightBold;
    }

    public void setHighlightBold(boolean highlightBold) {
        this.highlightBold = highlightBold;
    }

    public Color getHighlightColour() {
        return highlightColour;
    }

    public void setHighlightColour(Color highlightColour) {
        this.highlightColour = highlightColour;
    }

    public boolean isHighlighted(String rawName) {
        if (rawName == null || highlightList.isEmpty()) {
            return false;
        }
        for (String s : highlightList) {
            if (rawName.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if nothing here would change how a label is drawn, so the
     *         painter can use its plain single-string drawing path
     */
    public boolean isPlain() {
        if (!highlightList.isEmpty() || !otherGroup.isPlain()) {
            return false;
        }
        switch (italicMode) {
            case OFF:
                return true;
            case UNTIL_NUMBER:
                return italicGroup.isPlain() && !addRankDots && !formatCollectionNumber;
            default:
                return italicParts == 0 || italicGroup.isPlain();
        }
    }

    /**
     * Builds the styled runs for a name. Parts come from {@link LabelFormatter#getParts}.
     * Runs carry their own separating spaces so they can simply be drawn one after
     * the other.
     */
    public List<Run> getRuns(String[] parts, String rawName) {
        return getRuns(parts, rawName, null, false);
    }

    /**
     * The pieces of text a name is drawn as, in the order they appear on the
     * figure - one entry per checkbox in the "Style selected tips" dialog (6.11).
     * The collection number counts as one piece, so "KRAM M-2749" is not split.
     */
    public List<String> displayItems(String[] parts) {
        List<String> items = new ArrayList<String>(parts.length);
        List<Boolean> italic = new ArrayList<Boolean>(parts.length);
        buildItems(parts, items, italic);
        return items;
    }

    /**
     * @return what the general rules make italic, one flag per {@link #displayItems}
     *         entry - the starting point the dialog shows before the user edits it
     */
    public boolean[] displayItalic(String[] parts) {
        List<String> items = new ArrayList<String>(parts.length);
        List<Boolean> italic = new ArrayList<Boolean>(parts.length);
        buildItems(parts, items, italic);
        boolean[] mask = new boolean[italic.size()];
        for (int i = 0; i < italic.size(); i++) {
            mask[i] = italic.get(i);
        }
        return mask;
    }

    /**
     * @param italicOverride one flag per {@link #displayItems} entry, set by hand
     *                       for this one tip; null = follow the general rules
     * @param boldOverride   draw the whole label bold (set by hand for this tip)
     */
    public List<Run> getRuns(String[] parts, String rawName, boolean[] italicOverride, boolean boldOverride) {
        List<Run> runs = new ArrayList<Run>();

        {
            List<String> items = new ArrayList<String>(parts.length);
            List<Boolean> italic = new ArrayList<Boolean>(parts.length);
            buildItems(parts, items, italic);
            if (italicOverride != null) {
                // the user decided piece by piece for this tip
                for (int i = 0; i < italic.size(); i++) {
                    italic.set(i, i < italicOverride.length && italicOverride[i]);
                }
            }
            // one run per stretch of pieces that share the same italic/upright state
            int i = 0;
            while (i < items.size()) {
                int j = i;
                while (j < items.size() && italic.get(j).equals(italic.get(i))) {
                    j++;
                }
                runs.add(makeRun(items, i, j, italic.get(i) ? italicGroup : otherGroup, j < items.size()));
                i = j;
            }
        }

        if (boldOverride) {
            List<Run> bolded = new ArrayList<Run>(runs.size());
            for (Run run : runs) {
                bolded.add(new Run(run.text, run.italic, true, run.colour));
            }
            runs = bolded;
        }

        if (isHighlighted(rawName) && (highlightBold || highlightColour != null)) {
            List<Run> highlighted = new ArrayList<Run>(runs.size());
            for (Run run : runs) {
                highlighted.add(new Run(run.text,
                        run.italic,
                        run.bold || highlightBold,
                        highlightColour != null ? highlightColour : run.colour));
            }
            runs = highlighted;
        }

        return runs;
    }

    /**
     * Splits a name into the pieces of text that are drawn ({@code items}) and
     * says which of them are italic ({@code italic}). In
     * {@link ItalicMode#UNTIL_NUMBER} the name and the collection number are
     * handled separately, so that the number can be put back together.
     */
    private void buildItems(String[] parts, List<String> items, List<Boolean> italic) {
        if (italicMode != ItalicMode.UNTIL_NUMBER) {
            boolean[] mask = italicMask(parts);
            for (int i = 0; i < parts.length; i++) {
                items.add(parts[i]);
                italic.add(mask[i]);
            }
            return;
        }
        int start = numberStart(parts);
        for (int i = 0; i < start; i++) {
            items.add(displayPart(parts[i]));
            italic.add(!isNonItalicWord(parts[i]));
        }
        for (String piece : collectionNumber(parts, start)) {
            items.add(piece);
            italic.add(Boolean.FALSE);
        }
    }

    private Run makeRun(List<String> items, int from, int to, PartStyle style, boolean trailingSpace) {
        StringBuilder text = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) text.append(' ');
            text.append(style.caseMode.apply(items.get(i)));
        }
        if (trailingSpace) text.append(' ');
        return new Run(text.toString(), style.italic, style.bold, style.colour);
    }

    /** the rank/qualifier words that stay upright unless the user changes them */
    public static final String DEFAULT_NON_ITALIC_WORDS = "var subsp ssp f sp cf aff nov x";

    /** the collection abbreviations that are cited with a dot */
    public static final String DEFAULT_DOT_CODES = "CA";

    private int italicParts = 0;
    private ItalicMode italicMode = ItalicMode.UNTIL_NUMBER;
    private String nonItalicWords = "";
    private final Set<String> nonItalicWordSet = new HashSet<String>();
    private boolean addRankDots = false;
    private boolean upperCaseIsNumber = true;
    private boolean formatCollectionNumber = true;
    private String dotCodes = "";
    private final Set<String> dotCodeSet = new HashSet<String>();
    private final PartStyle italicGroup = new PartStyle();
    private final PartStyle otherGroup = new PartStyle();

    {
        italicGroup.italic = true;
        setNonItalicWords(DEFAULT_NON_ITALIC_WORDS);
        setDotCodes(DEFAULT_DOT_CODES);
    }

    private String highlight = "";
    private final List<String> highlightList = new ArrayList<String>();
    private boolean highlightBold = false;
    private Color highlightColour = null;
}
