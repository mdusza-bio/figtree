/*
 * GroupBarPainter.java
 *
 * MyFigTree addition (Etap 4.3 / 4.4): vertical group bars to the right of the
 * tip labels and pale clade backgrounds behind contiguous runs of tips that
 * share the same attribute value. Only drawn for the rectilinear layout.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.ColourDecorator;
import figtree.treeviewer.treelayouts.RectilinearTreeLayout;
import figtree.treeviewer.treelayouts.TreeLayoutCache;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * Draws group bars (with rotated value text) and clade backgrounds.
 * The geometry (tip positions, label extents) comes from the TreePane at paint time,
 * so nothing is cached here apart from the user settings.
 */
public class GroupBarPainter {

    private static final Color[] PASTEL_PALETTE = new Color[] {
            new Color(141, 160, 203), new Color(252, 141, 98), new Color(102, 194, 165),
            new Color(231, 138, 195), new Color(166, 216, 84), new Color(255, 217, 47),
            new Color(229, 196, 148), new Color(179, 179, 179)
    };

    /** One contiguous run of tips (sorted top to bottom) sharing an attribute value. */
    private static class Run {
        Object value;
        List<Node> tips = new ArrayList<Node>();
        double minY, maxY;
    }

    // ---- settings -------------------------------------------------------------------------

    public boolean isBarsVisible() {
        return barsVisible;
    }

    public void setBarsVisible(boolean barsVisible) {
        this.barsVisible = barsVisible;
        firePainterChanged();
    }

    public boolean isBackgroundsVisible() {
        return backgroundsVisible;
    }

    public void setBackgroundsVisible(boolean backgroundsVisible) {
        this.backgroundsVisible = backgroundsVisible;
        firePainterChanged();
    }

    public void setBarAttribute(String attribute, ColourDecorator decorator) {
        this.barAttribute = attribute;
        this.barDecorator = decorator;
        firePainterChanged();
    }

    public void setBackgroundAttribute(String attribute, ColourDecorator decorator) {
        this.backgroundAttribute = attribute;
        this.backgroundDecorator = decorator;
        firePainterChanged();
    }

    public void setBarWidth(double barWidth) {
        this.barWidth = barWidth;
        firePainterChanged();
    }

    public void setGap(double gap) {
        this.gap = gap;
        firePainterChanged();
    }

    public void setFontSize(float fontSize) {
        this.font = font.deriveFont(fontSize);
        firePainterChanged();
    }

    public void setBackgroundAlpha(double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        firePainterChanged();
    }

    /** True if anything is going to be drawn for this layout. */
    public boolean isVisible(TreePane treePane) {
        if (!(treePane.getTreeLayout() instanceof RectilinearTreeLayout)) return false;
        return (barsVisible && barAttribute != null) || (backgroundsVisible && backgroundAttribute != null);
    }

    /**
     * Horizontal space (screen pixels / points) that must be reserved to the right of
     * the tip labels for the bars and their rotated text.
     */
    public double getRequiredRightWidth(Graphics2D g2, TreePane treePane) {
        if (!(treePane.getTreeLayout() instanceof RectilinearTreeLayout)) return 0.0;
        if (!barsVisible || barAttribute == null) return 0.0;
        FontMetrics fm = g2.getFontMetrics(font);
        return gap + barWidth + TEXT_GAP + fm.getAscent() + fm.getDescent();
    }

    // ---- painting -------------------------------------------------------------------------

    /** Pale rectangles behind clades. Call BEFORE the branches are drawn. */
    public void paintBackgrounds(Graphics2D g2, TreePane treePane) {
        if (!backgroundsVisible || backgroundAttribute == null) return;
        if (!(treePane.getTreeLayout() instanceof RectilinearTreeLayout)) return;

        RootedTree tree = treePane.getTree();
        AffineTransform transform = treePane.getTreeTransform();
        if (tree == null || transform == null) return;

        List<Run> runs = computeRuns(treePane, backgroundAttribute);
        if (runs.isEmpty()) return;

        double rightX = getLabelRightX(treePane);
        // extend under the bars if they are shown, otherwise just a little past the labels
        double extra = (barsVisible && barAttribute != null) ? gap / 2.0 : 2.0;

        Paint oldPaint = g2.getPaint();
        for (Run run : runs) {
            if (run.value == null) continue;

            Node mrca;
            if (run.tips.size() == 1) {
                mrca = run.tips.get(0);
            } else {
                mrca = RootedTreeUtils.getCommonAncestorNode(tree, new HashSet<Node>(run.tips));
            }
            Point2D nodePoint = treePane.getTreeLayoutCache().getNodePoint(mrca);
            if (nodePoint == null) continue;
            Point2D p = transform.transform(nodePoint, null);
            double x0 = p.getX() - 2.0;

            Color c = getColourFor(run.value, backgroundDecorator, backgroundAttribute);
            g2.setPaint(new Color(c.getRed(), c.getGreen(), c.getBlue(),
                    (int) Math.round(255 * Math.max(0.0, Math.min(1.0, backgroundAlpha)))));
            g2.fill(new Rectangle2D.Double(x0, run.minY, Math.max(0.0, rightX + extra - x0), run.maxY - run.minY));
        }
        g2.setPaint(oldPaint);
    }

    /** Vertical bars with rotated text to the right of the tip labels. Call AFTER the tip labels. */
    public void paintBars(Graphics2D g2, TreePane treePane) {
        if (!barsVisible || barAttribute == null) return;
        if (!(treePane.getTreeLayout() instanceof RectilinearTreeLayout)) return;

        List<Run> runs = computeRuns(treePane, barAttribute);
        if (runs.isEmpty()) return;

        double barX = getLabelRightX(treePane) + gap;

        Paint oldPaint = g2.getPaint();
        Font oldFont = g2.getFont();
        AffineTransform oldTransform = g2.getTransform();

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics(font);

        for (Run run : runs) {
            if (run.value == null) continue;

            Color c = getColourFor(run.value, barDecorator, barAttribute);
            g2.setPaint(c);
            g2.fill(new Rectangle2D.Double(barX, run.minY, barWidth, run.maxY - run.minY));

            String text = String.valueOf(run.value);
            double textWidth = fm.stringWidth(text);
            double centreY = (run.minY + run.maxY) / 2.0;
            // baseline of the rotated text sits to the right of the bar
            double textX = barX + barWidth + TEXT_GAP + fm.getAscent();

            g2.setPaint(Color.BLACK);
            AffineTransform t = new AffineTransform(oldTransform);
            t.translate(textX, centreY + textWidth / 2.0);
            t.rotate(-Math.PI / 2.0);
            g2.setTransform(t);
            g2.drawString(text, 0, 0);
            g2.setTransform(oldTransform);
        }

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
    }

    // ---- helpers --------------------------------------------------------------------------

    private Object getTipValue(RootedTree tree, Node node, String attribute) {
        Object value = null;
        Taxon taxon = tree.getTaxon(node);
        if (taxon != null) {
            value = taxon.getAttribute(attribute);
        }
        if (value == null) {
            value = node.getAttribute(attribute);
        }
        return value;
    }

    /** Screen x of the right-hand edge of the widest tip label (or of the tips if labels are hidden). */
    private double getLabelRightX(TreePane treePane) {
        double rightX = Double.NEGATIVE_INFINITY;
        Map<Node, Shape> bounds = treePane.getTipLabelBounds();
        if (treePane.getTipLabelPainter() != null && treePane.getTipLabelPainter().isVisible() && !bounds.isEmpty()) {
            for (Shape s : bounds.values()) {
                rightX = Math.max(rightX, s.getBounds2D().getMaxX());
            }
        } else {
            AffineTransform transform = treePane.getTreeTransform();
            for (Line2D path : treePane.getTreeLayoutCache().getTipLabelPathMap().values()) {
                Point2D p = transform.transform(path.getP1(), null);
                rightX = Math.max(rightX, p.getX());
            }
        }
        if (rightX == Double.NEGATIVE_INFINITY) {
            rightX = treePane.getTreeBounds().getMaxX();
        }
        return rightX;
    }

    /** Groups tips (ordered top to bottom on screen) into runs of equal attribute value. */
    private List<Run> computeRuns(TreePane treePane, String attribute) {
        List<Run> runs = new ArrayList<Run>();
        RootedTree tree = treePane.getTree();
        AffineTransform transform = treePane.getTreeTransform();
        TreeLayoutCache cache = treePane.getTreeLayoutCache();
        if (tree == null || transform == null) return runs;

        // collect the tips with their screen y
        final Map<Node, Double> tipY = new HashMap<Node, Double>();
        for (Map.Entry<Node, Line2D> entry : cache.getTipLabelPathMap().entrySet()) {
            Point2D p = transform.transform(entry.getValue().getP1(), null);
            tipY.put(entry.getKey(), p.getY());
        }
        if (tipY.isEmpty()) return runs;

        List<Node> tips = new ArrayList<Node>(tipY.keySet());
        Collections.sort(tips, new Comparator<Node>() {
            public int compare(Node a, Node b) {
                return Double.compare(tipY.get(a), tipY.get(b));
            }
        });

        // half the (smallest) spacing between neighbouring tips
        double halfSpacing = Double.MAX_VALUE;
        for (int i = 1; i < tips.size(); i++) {
            double d = tipY.get(tips.get(i)) - tipY.get(tips.get(i - 1));
            if (d > 0.0) halfSpacing = Math.min(halfSpacing, d / 2.0);
        }
        if (halfSpacing == Double.MAX_VALUE) {
            LabelPainter<Node> lp = treePane.getTipLabelPainter();
            halfSpacing = (lp != null && lp.isVisible()) ? lp.getPreferredHeight() / 2.0 : 5.0;
        }

        Run current = null;
        for (Node tip : tips) {
            Object value = getTipValue(tree, tip, attribute);
            double y = tipY.get(tip);
            if (current == null || !Objects.equals(current.value, value)) {
                current = new Run();
                current.value = value;
                current.minY = y - halfSpacing;
                runs.add(current);
            }
            current.tips.add(tip);
            current.maxY = y + halfSpacing;
        }
        return runs;
    }

    private Color getColourFor(Object value, ColourDecorator decorator, String attribute) {
        if (decorator != null) {
            try {
                decorator.setItem(value);
                Paint p = decorator.getPaint(null);
                if (p instanceof Color) {
                    return (Color) p;
                }
            } catch (RuntimeException e) {
                // fall through to the palette
            }
        }
        // simple fallback palette by distinct value
        Map<Object, Color> map = paletteCache.get(attribute);
        if (map == null) {
            map = new LinkedHashMap<Object, Color>();
            paletteCache.put(attribute, map);
        }
        Color c = map.get(value);
        if (c == null) {
            c = PASTEL_PALETTE[map.size() % PASTEL_PALETTE.length];
            map.put(value, c);
        }
        return c;
    }

    // ---- listeners ------------------------------------------------------------------------

    public void addPainterListener(PainterListener listener) {
        listeners.add(listener);
    }

    public void removePainterListener(PainterListener listener) {
        listeners.remove(listener);
    }

    private void firePainterChanged() {
        for (PainterListener listener : listeners) {
            listener.painterChanged();
        }
    }

    private static final double TEXT_GAP = 3.0;

    private boolean barsVisible = false;
    private boolean backgroundsVisible = false;
    private String barAttribute = null;
    private String backgroundAttribute = null;
    private ColourDecorator barDecorator = null;
    private ColourDecorator backgroundDecorator = null;
    private double barWidth = 8.0;
    private double gap = 6.0;
    private double backgroundAlpha = 0.25;
    private Font font = new Font("sansserif", Font.PLAIN, 10);

    private final Map<String, Map<Object, Color>> paletteCache = new HashMap<String, Map<Object, Color>>();
    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}
