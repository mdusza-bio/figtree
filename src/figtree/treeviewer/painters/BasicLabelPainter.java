/*
 * BasicLabelPainter.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.TimeScale;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.*;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * A simple implementation of LabelPainter that can be used to display
 * tip, node or branch labels. It can display, taxon names, branch lengths,
 * node heights or other attributeNames of nodes.
 *
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class BasicLabelPainter extends LabelPainter<Node> {

    public BasicLabelPainter(PainterIntent intent) {
        super(intent);

        this.displayAttribute = "";
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Decorator getBorderDecorator() {
        return borderDecorator;
    }

    public void setBorderDecorator(Decorator borderDecorator) {
        this.borderDecorator = borderDecorator;
        firePainterSettingsChanged();
    }

    public Decorator getTextDecorator() {
        return textDecorator;
    }

    public void setTextDecorator(Decorator textDecorator) {
        this.textDecorator = textDecorator;
        firePainterSettingsChanged();
    }

    public Tree getTree() {
        return treePane.getTree();
    }

    /**
     * The raw (unformatted) name of a node/taxon, exactly as it is in the tree.
     * Returns null if there is no name to display.
     */
    protected String getRawName(Tree tree, Node node) {
        if (getIntent() == PainterIntent.TIP) {
            Taxon taxon = tree.getTaxon(node);
            if (taxon != null) {
                if (textDecorator != null) {
                    textDecorator.setItem(taxon);
                }
                String name = (String)taxon.getAttribute("!name");
                if (name != null) {
                    return name;
                }
                // MyFigTree: Tree > Annotate puts "!name" on the taxon when a tip
                // LABEL is selected but on the node when the tip NODE is selected.
                // The original only looked at the taxon, so renaming a tip did
                // nothing at all whenever the node was what got picked.
                name = (String)node.getAttribute("!name");
                if (name != null) {
                    return name;
                }
                return taxon.getName();
            }
        }
        return (String)node.getAttribute("!name");
    }

    /**
     * The displayed name split into parts (on whitespace, after hide patterns and
     * underscore replacement). Foundation for styling individual parts of a name.
     */
    public String[] getLabelParts(Tree tree, Node node) {
        return labelFormatter.getParts(getRawName(tree, node));
    }

    /**
     * MyFigTree (Etap 3): the label as styled runs, or null when the plain
     * single-string drawing path should be used (styling off, attribute other
     * than Names, or a second value is shown).
     */
    private List<LabelStyle.Run> getStyledRuns(Tree tree, Node node) {
        if (!displayAttribute.equalsIgnoreCase(NAMES)) {
            return null;
        }
        if (secondAttribute != null && secondAttribute.length() > 0 && !secondAttribute.equals(NONE)) {
            return null;
        }

        // MyFigTree (Etap 6.11): what the user set by hand on this one tip wins
        boolean[] italicOverride = parseItalicOverride(node.getAttribute(ITALIC_OVERRIDE_ATTRIBUTE));
        boolean boldOverride = isTrue(node.getAttribute(BOLD_OVERRIDE_ATTRIBUTE));
        // MyFigTree (Etap 7.12): a tip marked by hand as a type gets the raised "T"
        boolean typeOverride = isTrue(node.getAttribute(TYPE_OVERRIDE_ATTRIBUTE));

        if (labelStyle.isPlain() && italicOverride == null && !boldOverride && !typeOverride) {
            return null;
        }

        String rawName = getRawName(tree, node);
        if (rawName == null) {
            return null;
        }
        String[] parts = labelFormatter.getParts(rawName);
        if (parts.length == 0) {
            return null;
        }
        return labelStyle.getRuns(parts, rawName, italicOverride, boldOverride, typeOverride);
    }

    /**
     * MyFigTree (Etap 6.11): reads "1,1,0" (one flag per drawn piece of the name)
     * off a tip; null when the tip has no hand-made setting.
     */
    public static boolean[] parseItalicOverride(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        if (text.length() == 0) {
            return null;
        }
        String[] flags = text.split(",");
        boolean[] mask = new boolean[flags.length];
        for (int i = 0; i < flags.length; i++) {
            mask[i] = flags[i].trim().equals("1");
        }
        return mask;
    }

    public static String italicOverrideText(boolean[] mask) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < mask.length; i++) {
            if (i > 0) text.append(',');
            text.append(mask[i] ? '1' : '0');
        }
        return text.toString();
    }

    public static boolean isTrue(Object value) {
        return value != null && value.toString().trim().equalsIgnoreCase("true");
    }

    /** How much smaller the raised "T" is than the name, and how far up it sits (share of the ascent). */
    private static final float SUPERSCRIPT_SCALE = 0.7f;
    private static final double SUPERSCRIPT_RISE = 0.38;

    private static Font runFont(Font base, LabelStyle.Run run) {
        if (run.superscript) {
            return base.deriveFont(Font.PLAIN, base.getSize2D() * SUPERSCRIPT_SCALE);
        }
        int style = base.getStyle();
        if (run.italic) style |= Font.ITALIC;
        if (run.bold) style |= Font.BOLD;
        return style == base.getStyle() ? base : base.deriveFont(style);
    }

    /**
     * The label text. May contain a '\n' when a second value is shown stacked
     * below the first one. Returns null if nothing should be drawn (no value, or
     * the first value is below the "show only if >=" threshold).
     */
    protected String getLabel(Tree tree, Node node) {
        // MyFigTree (Etap 6.20): the dot already says "full support" - no number needed there
        if (supportDotPainter != null && supportDotPainter.isHidingLabels() &&
                supportDotPainter.hasDot(tree, node)) {
            return null;
        }

        Object value1 = getValue(tree, node, displayAttribute);

        boolean below1 = false;
        if (showThreshold != null) {
            Double number = asNumber(value1);
            below1 = number != null && number < showThreshold;
        }

        String label1 = formatValue(value1);

        if (secondAttribute == null || secondAttribute.length() == 0 || secondAttribute.equals(NONE)) {
            return below1 ? null : label1;
        }

        Object value2 = getValue(tree, node, secondAttribute);
        String label2 = formatValue(value2);
        boolean below2 = false;
        if (secondThreshold != null) {
            Double number = asNumber(value2);
            below2 = number != null && number < secondThreshold;
        }

        if (belowPlaceholder.length() > 0 && (label1 == null || !label1.startsWith(SOLID_BOX_ENCODED))) {
            // MyFigTree (Etap 7.11): two analyses on one node, journal style - a value that is too
            // low (or not there at all) is replaced by a dash, "-/1" or "76/-"; the label goes
            // only when neither value is worth showing
            boolean show1 = label1 != null && !below1;
            boolean show2 = label2 != null && !below2;
            if (!show1 && !show2) {
                return null;
            }
            label1 = show1 ? label1 : belowPlaceholder;
            label2 = show2 ? label2 : belowPlaceholder;
        } else {
            // without a placeholder: as before, a first value below its threshold hides the label
            if (below1) {
                return null;
            }
            if (label2 == null || below2) {
                return label1;
            }
            if (label1 == null || label1.startsWith(SOLID_BOX_ENCODED)) {
                return label2;
            }
        }

        if (secondValueLayout == SecondValueLayout.STACKED) {
            return label1 + "\n" + label2;
        }
        return label1 + " / " + label2;
    }

    /**
     * The raw value for a given attribute name: a formatted name string, a Double
     * for heights/lengths, or whatever object the node/taxon attribute holds.
     */
    private Object getValue(Tree tree, Node node, String attribute) {
        if (attribute == null || attribute.length() == 0) {
            return null;
        }

        if (attribute.equalsIgnoreCase(NAMES)) {
            return labelFormatter.format(getRawName(tree, node));
        }

        if (attribute.equalsIgnoreCase(SOLID_BOX)) {
            return SOLID_BOX_ENCODED + boxSize;
        }

        if ( tree instanceof RootedTree) {
            final RootedTree rtree = (RootedTree) tree;

            if (textDecorator != null) {
                textDecorator.setItem(node);
            }

            if (attribute.equalsIgnoreCase(NODE_AGES) ) {
                TimeScale timeScale = treePane.getTimeScale();
                return timeScale.getAge(rtree.getHeight(node), rtree);
            } else if (attribute.equalsIgnoreCase(NODE_HEIGHTS) ) {
                return rtree.getHeight(node);
            } else if (attribute.equalsIgnoreCase(BRANCH_TIMES) ) {
                TimeScale timeScale = treePane.getTimeScale();
                return timeScale.getTime(rtree.getLength(node), rtree);
            } else if (attribute.equalsIgnoreCase(BRANCH_LENGTHS) ) {
                return rtree.getLength(node);
            }
        }

        Object value = null;

        if (getIntent() == PainterIntent.TIP) {
            Taxon taxon = tree.getTaxon(node);
            if (taxon != null) {
                value = taxon.getAttribute(attribute);
            } else {
                value = node.getAttribute(attribute);
            }
        }

        if (value == null) {
            value = node.getAttribute(attribute);
        }

        return value;
    }

    private static Double asNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String formatValue(Object value) {
        if (value != null) {
            if (value instanceof Double || value instanceof Float) {
                return getNumberFormat().format(value);
            } else if (value instanceof Object[]) {
                Object[] values = (Object[])value;

                if (values.length == 0) return null;
                if (values.length == 1) return formatValue(values[0]);

                StringBuilder builder = new StringBuilder("[");
                builder.append(formatValue(values[0]));
                for (int i = 1; i < values.length; i++) {
                    builder.append(",");
                    builder.append(formatValue(values[i]));
                }
                builder.append("]");
                return builder.toString();
            }
            return value.toString();
        }
        return null;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node item) {
        Tree tree = treePane.getTree();

        String label = getLabel(tree, item);

        final Font oldFont = g2.getFont();
        if (textDecorator != null) {
            g2.setFont(textDecorator.getFont(getFont()));
        } else {
            g2.setFont(getFont());
        }

        FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight();
        preferredWidth = 0;

        List<LabelStyle.Run> runs = (label == null ? null : getStyledRuns(tree, item));

        if (runs != null) {
            Font baseFont = g2.getFont();
            for (LabelStyle.Run run : runs) {
                FontMetrics rfm = g2.getFontMetrics(runFont(baseFont, run));
                preferredWidth += rfm.getStringBounds(run.text, g2).getWidth();
                preferredHeight = Math.max(preferredHeight, rfm.getHeight());
            }
        } else if (label != null) {

            if (label.startsWith(SOLID_BOX_ENCODED)) {
                int boxLength = Integer.parseInt(
                        label.substring(SOLID_BOX_ENCODED.length()));
                preferredWidth = boxLength;
            } else {
                String[] lines = label.split("\n");
                for (String line : lines) {
                    Rectangle2D rect = fm.getStringBounds(line, g2);
                    preferredWidth = Math.max(preferredWidth, rect.getWidth());
                }
                preferredHeight = fm.getHeight() * lines.length;
            }
        }

        yOffset = (float)fm.getAscent();

        g2.setFont(oldFont);

        return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        Tree tree = treePane.getTree();

        if (TreePane.DEBUG_OUTLINE) {
            g2.setPaint(Color.red);
            g2.draw(bounds);
        }

        String label = getLabel(tree, item);

        Font oldFont = g2.getFont();

        Paint backgroundPaint = getBackground();
        Paint borderPaint = getBorderPaint();
        Stroke borderStroke = getBorderStroke();

        if (borderDecorator != null) {
            backgroundPaint = borderDecorator.getPaint(backgroundPaint);
            borderPaint = borderDecorator.getPaint(borderPaint);
            borderStroke = borderDecorator.getStroke(borderStroke);
        }

        if (backgroundPaint != null) {
            g2.setPaint(backgroundPaint);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        if (textDecorator != null) {
            // this is a bit of a hack to detect whether we should be getting the
            // colour attribute from the taxon, or some other attribute from the
            // node:
            if (getIntent() == PainterIntent.TIP) {
                textDecorator.setItem(tree.getTaxon(item), (Node)item);
            } else {
                textDecorator.setItem(item);
            }

            g2.setPaint(textDecorator.getPaint(getForeground()));
            g2.setFont(textDecorator.getFont(getFont()));
        } else {
            g2.setPaint(getForeground());
            g2.setFont(getFont());
        }

        List<LabelStyle.Run> runs = (label == null ? null : getStyledRuns(tree, item));

        if (runs != null) {
            // MyFigTree (Etap 3): draw the styled parts one after another
            Font baseFont = g2.getFont();
            Paint basePaint = g2.getPaint();

            double totalWidth = 0;
            for (LabelStyle.Run run : runs) {
                totalWidth += g2.getFontMetrics(runFont(baseFont, run)).getStringBounds(run.text, g2).getWidth();
            }

            float x;
            float y = yOffset + (float) bounds.getY();
            switch (justification) {
                case CENTER:
                    x = (float) (-totalWidth / 2.0);
                    y = yOffset + (float) g2.getFontMetrics(baseFont).getStringBounds(label, g2).getY();
                    break;
                case FLUSH:
                case LEFT:
                    x = (float) bounds.getX();
                    break;
                case RIGHT:
                    x = (float) (bounds.getX() + bounds.getWidth() - totalWidth);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            for (LabelStyle.Run run : runs) {
                Font f = runFont(baseFont, run);
                g2.setFont(f);
                g2.setPaint(run.colour != null ? run.colour : basePaint);
                float rise = run.superscript ?
                        (float) (g2.getFontMetrics(baseFont).getAscent() * SUPERSCRIPT_RISE) : 0f;
                g2.drawString(run.text, x, y - rise);
                x += g2.getFontMetrics(f).getStringBounds(run.text, g2).getWidth();
            }
            g2.setPaint(basePaint);
        } else if (label != null) {

            if (label.startsWith(SOLID_BOX_ENCODED)) {
                g2.fill(bounds);
            } else {
                // MyFigTree: a label may have several lines (stacked second value)
                String[] lines = label.split("\n");
                FontMetrics fm = g2.getFontMetrics();
                float lineHeight = fm.getHeight();

                for (int i = 0; i < lines.length; i++) {
                    Rectangle2D rect = fm.getStringBounds(lines[i], g2);

                    float xOffset;
                    float y = yOffset + (float) bounds.getY() + i * lineHeight;
                    switch (justification) {
                        case CENTER:
                            xOffset = (float)(-rect.getWidth()/2.0);
                            // the block is anchored by its last line so that extra
                            // lines grow upwards, away from the branch
                            y = yOffset + (float) rect.getY() - (lines.length - 1 - i) * lineHeight;
                            break;
                        case FLUSH:
                        case LEFT:
                            xOffset = (float) bounds.getX();
                            break;
                        case RIGHT:
                            xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                            break;
                        default:
                            throw new IllegalArgumentException("Unrecognized alignment enum option");
                    }

                    g2.drawString(lines[i], xOffset, y);
                }
            }
        }

        g2.setFont(oldFont);
    }

    public String getDisplayAttribute() {
        if (displayAttribute.equalsIgnoreCase(NAMES) ||
                displayAttribute.equalsIgnoreCase(NODE_AGES) ||
                displayAttribute.equalsIgnoreCase(NODE_HEIGHTS) ||
                displayAttribute.equalsIgnoreCase(BRANCH_TIMES) ||
                displayAttribute.equalsIgnoreCase(BRANCH_LENGTHS)) {
            return null;
        }

        return displayAttribute;
    }

    public void setDisplayAttribute(String displayAttribute) {
        this.displayAttribute = displayAttribute;
        firePainterChanged();
    }

    public void setBoxSize(int size) {
        boxSize = size;
        firePainterChanged();
    }

    public LabelFormatter getLabelFormatter() {
        return labelFormatter;
    }

    public void setReplaceUnderscores(boolean replaceUnderscores) {
        labelFormatter.setReplaceUnderscores(replaceUnderscores);
        firePainterChanged();
    }

    public void setHideParts(String hideParts) {
        labelFormatter.setHideParts(hideParts);
        firePainterChanged();
    }

    public void setHideRegex(String hideRegex) {
        labelFormatter.setHideRegex(hideRegex);
        firePainterChanged();
    }

    // MyFigTree: per-part label styling (Etap 3)

    /** MyFigTree (Etap 6.11): per-tip settings, saved with the tree */
    public static final String ITALIC_OVERRIDE_ATTRIBUTE = "!labelItalic";
    public static final String BOLD_OVERRIDE_ATTRIBUTE = "!labelBold";
    public static final String TYPE_OVERRIDE_ATTRIBUTE = "!labelType";

    public LabelStyle getLabelStyle() {
        return labelStyle;
    }

    /**
     * Call after changing anything in {@link #getLabelStyle()} so the tree is redrawn.
     */
    public void labelStyleChanged() {
        firePainterChanged();
    }

    private final LabelStyle labelStyle = new LabelStyle();

    // MyFigTree: support-value display options (Etap 2)

    public static final String NONE = "None";

    /** MyFigTree (Etap 6.20): labels of branches with a support dot can be left out. */
    public void setSupportDotPainter(SupportDotPainter supportDotPainter) {
        this.supportDotPainter = supportDotPainter;
    }

    public void setShowThreshold(Double showThreshold) {
        this.showThreshold = showThreshold;
        firePainterChanged();
    }

    public void setSecondAttribute(String secondAttribute) {
        this.secondAttribute = secondAttribute;
        firePainterChanged();
    }

    public void setSecondValueLayout(SecondValueLayout layout) {
        this.secondValueLayout = layout;
        firePainterChanged();
    }

    public void setSecondThreshold(Double secondThreshold) {
        this.secondThreshold = secondThreshold;
        firePainterChanged();
    }

    public void setBelowPlaceholder(String placeholder) {
        this.belowPlaceholder = placeholder == null ? "" : placeholder.trim();
        firePainterChanged();
    }

    private Double secondThreshold = null;
    private String belowPlaceholder = "";
    private Double showThreshold = null;
    private SupportDotPainter supportDotPainter = null;
    private String secondAttribute = NONE;
    private SecondValueLayout secondValueLayout = SecondValueLayout.SAME_LINE;

    private final LabelFormatter labelFormatter = new LabelFormatter();

    private double preferredWidth;
    private double preferredHeight;
    private int boxSize = 10;
    private float yOffset;

    protected String displayAttribute;

    protected TreePane treePane;

    private Decorator textDecorator = null;
    private Decorator borderDecorator = null;


}
