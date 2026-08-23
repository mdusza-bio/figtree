/*
 * LabelFormatter.java
 *
 * Part of the MyFigTree fork. Turns a raw taxon/node name into the text that is
 * actually displayed on the figure, without touching the tree itself.
 */

package figtree.treeviewer.painters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Formats a raw label (usually a taxon name coming straight from a FASTA header)
 * into the text that gets painted. The pipeline is:
 * <ol>
 *     <li>remove the literal "hide parts" substrings (applied to the RAW name),</li>
 *     <li>remove anything matching the "hide regex" (also on the raw name),</li>
 *     <li>optionally replace underscores with spaces,</li>
 *     <li>split the result into parts on whitespace (foundation for per-part styling).</li>
 * </ol>
 * Only the displayed text is affected; the tree and taxon objects are never modified.
 */
public class LabelFormatter {

    public boolean isReplaceUnderscores() {
        return replaceUnderscores;
    }

    public void setReplaceUnderscores(boolean replaceUnderscores) {
        this.replaceUnderscores = replaceUnderscores;
    }

    public String getHideParts() {
        return hideParts;
    }

    /**
     * @param hideParts comma-separated literal substrings to strip, e.g. "EBOV|,_contig1"
     */
    public void setHideParts(String hideParts) {
        this.hideParts = (hideParts == null ? "" : hideParts);
        hidePartsList.clear();
        for (String part : this.hideParts.split(",")) {
            if (part.length() > 0) {
                hidePartsList.add(part);
            }
        }
    }

    public String getHideRegex() {
        return hideRegex;
    }

    /**
     * @param hideRegex a Java regular expression; all matches are removed. An invalid
     *                  expression is silently ignored (nothing is removed).
     */
    public void setHideRegex(String hideRegex) {
        this.hideRegex = (hideRegex == null ? "" : hideRegex);
        hidePattern = null;
        if (this.hideRegex.length() > 0) {
            try {
                hidePattern = Pattern.compile(this.hideRegex);
            } catch (PatternSyntaxException pse) {
                // invalid regex - ignore it rather than crash the painter
                hidePattern = null;
            }
        }
    }

    /**
     * @return the full display string for the raw name (or null if the name is null)
     */
    public String format(String rawName) {
        if (rawName == null) {
            return null;
        }

        String name = rawName;

        for (String part : hidePartsList) {
            name = name.replace(part, "");
        }

        if (hidePattern != null) {
            name = hidePattern.matcher(name).replaceAll("");
        }

        if (replaceUnderscores) {
            name = name.replace('_', ' ');
        }

        return name;
    }

    /**
     * @return the display string split into parts on whitespace (after the hide
     *         patterns and underscore replacement have been applied). Never null;
     *         an empty array for a null or blank name.
     */
    public String[] getParts(String rawName) {
        String name = format(rawName);
        if (name == null) {
            return new String[0];
        }
        name = name.trim();
        if (name.length() == 0) {
            return new String[0];
        }
        return name.split("\\s+");
    }

    private boolean replaceUnderscores = false;
    private String hideParts = "";
    private String hideRegex = "";

    private final List<String> hidePartsList = new ArrayList<String>();
    private Pattern hidePattern = null;
}
