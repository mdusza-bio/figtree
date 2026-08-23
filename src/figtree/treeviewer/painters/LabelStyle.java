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
 * into a list of styled runs to draw. Two ways of describing the style:
 * <ul>
 *     <li><b>simple</b>: "first N parts italic" with one style for the italic
 *         group and one for the remaining parts (3.1 - 3.3);</li>
 *     <li><b>template</b> (3.5): a string like <code>{1-2:i} {3:U}</code> where
 *         each <code>{...}</code> picks parts (<code>3</code>, <code>1-2</code>,
 *         <code>3-</code> = from 3 to the end, <code>*</code> = all) and flags:
 *         <code>i</code> italic, <code>b</code> bold, <code>U</code> upper case,
 *         <code>L</code> lower case, <code>S</code> sentence case,
 *         <code>#rrggbb</code> colour. Text outside braces is copied literally.
 *         A valid template overrides the simple settings; an invalid one is ignored.</li>
 * </ul>
 * On top of both, "highlight": tips whose RAW name contains one of the given
 * substrings get bold and/or a colour on the whole label (3.4).
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

    public boolean isHyphenCollectionNumber() {
        return hyphenCollectionNumber;
    }

    /**
     * @param hyphenCollectionNumber when true, a collection number split over
     *                               three parts is put back together the way it is
     *                               cited: "KRAM M 1234" -> "KRAM M-1234"
     */
    public void setHyphenCollectionNumber(boolean hyphenCollectionNumber) {
        this.hyphenCollectionNumber = hyphenCollectionNumber;
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
     * Puts a collection number that got split by the underscores back together:
     * a herbarium code, a single capital letter and a number ("KRAM M 1234")
     * are cited as "KRAM M-1234". Only used in {@link ItalicMode#UNTIL_NUMBER};
     * a name that already has the hyphen is left alone.
     *
     * @return the parts to draw (the same array when nothing was joined)
     */
    private String[] joinCollectionNumber(String[] parts) {
        if (!hyphenCollectionNumber || italicMode != ItalicMode.UNTIL_NUMBER || parts.length < 3) {
            return parts;
        }
        List<String> joined = new ArrayList<String>(parts.length);
        int i = 0;
        while (i < parts.length) {
            if (i + 2 < parts.length
                    && isCollectionCode(parts[i])
                    && isCollectionLetter(parts[i + 1])
                    && startsWithDigit(parts[i + 2])) {
                joined.add(parts[i]);
                joined.add(parts[i + 1] + "-" + parts[i + 2]);
                i += 3;
            } else {
                joined.add(parts[i]);
                i++;
            }
        }
        return joined.toArray(new String[joined.size()]);
    }

    /** a herbarium code such as KRAM, MA, BR: at least two letters, all capitals */
    private static boolean isCollectionCode(String part) {
        if (part.length() < 2) {
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

    /** the single capital letter of a sub-collection, e.g. the M of "KRAM M" */
    private static boolean isCollectionLetter(String part) {
        return part.length() == 1 && Character.isUpperCase(part.charAt(0));
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

    // ---- template (3.5)

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = (template == null ? "" : template);
        templateTokens = parseTemplate(this.template.trim());
    }

    /**
     * @return true if the current template text is non-empty and valid
     */
    public boolean isTemplateActive() {
        return templateTokens != null;
    }

    /**
     * @return true if nothing here would change how a label is drawn, so the
     *         painter can use its plain single-string drawing path
     */
    public boolean isPlain() {
        if (templateTokens != null || !highlightList.isEmpty() || !otherGroup.isPlain()) {
            return false;
        }
        switch (italicMode) {
            case OFF:
                return true;
            case UNTIL_NUMBER:
                return italicGroup.isPlain() && !addRankDots && !hyphenCollectionNumber;
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
        List<Run> runs = new ArrayList<Run>();

        if (templateTokens != null) {
            for (Token token : templateTokens) {
                if (token.literal != null) {
                    runs.add(new Run(token.literal, false, false, null));
                } else {
                    int from = token.from;
                    int to = (token.to < 0 ? parts.length : Math.min(token.to, parts.length));
                    StringBuilder text = new StringBuilder();
                    for (int i = from; i < to; i++) {
                        if (text.length() > 0) text.append(' ');
                        text.append(token.style.caseMode.apply(parts[i]));
                    }
                    if (text.length() > 0) {
                        runs.add(new Run(text.toString(), token.style.italic, token.style.bold, token.style.colour));
                    }
                }
            }
        } else {
            String[] items = joinCollectionNumber(parts);
            // one run per stretch of parts that share the same italic/upright state
            boolean[] mask = italicMask(items);
            int i = 0;
            while (i < items.length) {
                int j = i;
                while (j < items.length && mask[j] == mask[i]) {
                    j++;
                }
                runs.add(makeRun(items, i, j, mask[i] ? italicGroup : otherGroup, j < items.length));
                i = j;
            }
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

    private Run makeRun(String[] parts, int from, int to, PartStyle style, boolean trailingSpace) {
        StringBuilder text = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) text.append(' ');
            text.append(style.caseMode.apply(displayPart(parts[i])));
        }
        if (trailingSpace) text.append(' ');
        return new Run(text.toString(), style.italic, style.bold, style.colour);
    }

    // ---- template parsing

    private static class Token {
        String literal;     // non-null for literal text
        int from, to;       // 0-based, to = -1 means "to the end"
        PartStyle style;
    }

    /**
     * @return the parsed tokens, or null if the template is empty or invalid
     */
    private static List<Token> parseTemplate(String template) {
        if (template.length() == 0) {
            return null;
        }
        List<Token> tokens = new ArrayList<Token>();
        int pos = 0;
        boolean anyParts = false;
        while (pos < template.length()) {
            int open = template.indexOf('{', pos);
            if (open < 0) {
                tokens.add(literal(template.substring(pos)));
                break;
            }
            if (open > pos) {
                tokens.add(literal(template.substring(pos, open)));
            }
            int close = template.indexOf('}', open);
            if (close < 0) {
                return null;
            }
            Token token = parsePartToken(template.substring(open + 1, close));
            if (token == null) {
                return null;
            }
            tokens.add(token);
            anyParts = true;
            pos = close + 1;
        }
        return anyParts ? tokens : null;
    }

    private static Token literal(String text) {
        Token t = new Token();
        t.literal = text;
        return t;
    }

    /**
     * Parses the inside of "{...}": "range" or "range:flags".
     */
    private static Token parsePartToken(String body) {
        body = body.trim();
        String range = body;
        String flags = "";
        int colon = body.indexOf(':');
        if (colon >= 0) {
            range = body.substring(0, colon).trim();
            flags = body.substring(colon + 1).trim();
        }

        Token token = new Token();
        token.style = new PartStyle();
        try {
            if (range.equals("*")) {
                token.from = 0;
                token.to = -1;
            } else {
                int dash = range.indexOf('-');
                if (dash < 0) {
                    token.from = Integer.parseInt(range) - 1;
                    token.to = token.from + 1;
                } else {
                    token.from = Integer.parseInt(range.substring(0, dash).trim()) - 1;
                    String toText = range.substring(dash + 1).trim();
                    token.to = (toText.length() == 0 ? -1 : Integer.parseInt(toText));
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        if (token.from < 0 || (token.to >= 0 && token.to <= token.from)) {
            return null;
        }

        int i = 0;
        while (i < flags.length()) {
            char c = flags.charAt(i);
            if (c == '#') {
                if (i + 7 > flags.length()) return null;
                try {
                    token.style.colour = Color.decode(flags.substring(i, i + 7));
                } catch (NumberFormatException e) {
                    return null;
                }
                i += 7;
                continue;
            }
            switch (c) {
                case 'i': token.style.italic = true; break;
                case 'b': token.style.bold = true; break;
                case 'U': token.style.caseMode = Case.UPPER; break;
                case 'L': token.style.caseMode = Case.LOWER; break;
                case 'S': token.style.caseMode = Case.SENTENCE; break;
                case ' ': case ',': break;
                default: return null;
            }
            i++;
        }
        return token;
    }

    /** the rank/qualifier words that stay upright unless the user changes them */
    public static final String DEFAULT_NON_ITALIC_WORDS = "var subsp ssp f sp cf aff nov x";

    private int italicParts = 0;
    private ItalicMode italicMode = ItalicMode.UNTIL_NUMBER;
    private String nonItalicWords = "";
    private final Set<String> nonItalicWordSet = new HashSet<String>();
    private boolean addRankDots = false;
    private boolean upperCaseIsNumber = true;
    private boolean hyphenCollectionNumber = true;
    private final PartStyle italicGroup = new PartStyle();
    private final PartStyle otherGroup = new PartStyle();

    {
        italicGroup.italic = true;
        setNonItalicWords(DEFAULT_NON_ITALIC_WORDS);
    }

    private String highlight = "";
    private final List<String> highlightList = new ArrayList<String>();
    private boolean highlightBold = false;
    private Color highlightColour = null;

    private String template = "";
    private List<Token> templateTokens = null;
}
