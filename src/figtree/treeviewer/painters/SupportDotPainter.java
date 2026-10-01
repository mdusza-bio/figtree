/*
 * SupportDotPainter.java
 *
 * MyFigTree addition (Etap 6.20): a black dot on every branch whose support reached the
 * maximum - in one analysis (e.g. bootstrap = 100) or in both (bootstrap = 100 and
 * posterior probability = 1), the way many published trees mark fully supported clades.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.treelayouts.TreeLayoutCache;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the dots. Holds only the user settings; which branches get a dot is decided at paint
 * time from the node attributes, so it follows rerooting, collapsing and so on.
 */
public class SupportDotPainter {

    public enum Position {
        MID_BRANCH("Middle of branch"),
        AT_NODE("At node");

        Position(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    /** Values are compared with a small tolerance, so 0.99999 written by MrBayes still counts as 1. */
    private static final double TOLERANCE = 1E-6;

    // ---- settings -------------------------------------------------------------------------

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    public void setFirstSupport(String attribute, Double threshold) {
        this.attribute1 = attribute;
        this.threshold1 = threshold;
        firePainterChanged();
    }

    /** The second analysis; a null or empty attribute means a dot needs the first value only. */
    public void setSecondSupport(String attribute, Double threshold) {
        this.attribute2 = attribute;
        this.threshold2 = threshold;
        firePainterChanged();
    }

    public void setPosition(Position position) {
        this.position = position;
        firePainterChanged();
    }

    public void setDotSize(double dotSize) {
        this.dotSize = dotSize;
        firePainterChanged();
    }

    public void setColour(Color colour) {
        this.colour = colour;
        firePainterChanged();
    }

    public boolean isHidingLabels() {
        return visible && hidingLabels;
    }

    /** When on, the node and branch labels leave out the values of branches that got a dot. */
    public void setHidingLabels(boolean hidingLabels) {
        this.hidingLabels = hidingLabels;
        firePainterChanged();
    }

    // ---- the rule -------------------------------------------------------------------------

    /**
     * True if this branch gets a dot: an internal branch whose first value (and the second one,
     * if a second analysis is chosen) reached its threshold.
     */
    public boolean hasDot(Tree tree, Node node) {
        if (!visible || attribute1 == null || attribute1.length() == 0 || threshold1 == null) {
            return false;
        }
        if (tree instanceof RootedTree) {
            RootedTree rootedTree = (RootedTree) tree;
            if (rootedTree.isExternal(node) || rootedTree.getParent(node) == null) {
                return false;
            }
        }
        if (!reaches(node.getAttribute(attribute1), threshold1)) {
            return false;
        }
        if (attribute2 != null && attribute2.length() > 0 && threshold2 != null) {
            return reaches(node.getAttribute(attribute2), threshold2);
        }
        return true;
    }

    private static boolean reaches(Object value, double threshold) {
        Double number = null;
        if (value instanceof Number) {
            number = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                number = Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException nfe) {
                // not a number - no dot
            }
        }
        return number != null && number >= threshold - TOLERANCE;
    }

    // ---- painting -------------------------------------------------------------------------

    /** Call after the branches, before the node labels, so a label is never hidden under a dot. */
    public void paint(Graphics2D g2, TreePane treePane) {
        if (!visible) {
            return;
        }
        RootedTree tree = treePane.getTree();
        AffineTransform transform = treePane.getTreeTransform();
        TreeLayoutCache cache = treePane.getTreeLayoutCache();
        if (tree == null || transform == null) {
            return;
        }

        Paint oldPaint = g2.getPaint();
        Object oldAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(colour);

        for (Node node : tree.getInternalNodes()) {
            if (!hasDot(tree, node)) {
                continue;
            }
            Point2D point = null;
            if (position == Position.MID_BRANCH) {
                // the branch label path is a short line centred on the middle of the branch
                Line2D path = cache.getBranchLabelPath(node);
                if (path != null) {
                    point = new Point2D.Double((path.getX1() + path.getX2()) / 2.0,
                            (path.getY1() + path.getY2()) / 2.0);
                }
            } else {
                point = cache.getNodePoint(node);
            }
            if (point == null) {
                continue; // inside a collapsed clade, for example
            }
            Point2D p = transform.transform(point, null);
            g2.fill(new Ellipse2D.Double(p.getX() - dotSize / 2.0, p.getY() - dotSize / 2.0, dotSize, dotSize));
        }

        g2.setPaint(oldPaint);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
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

    private boolean visible = false;
    private String attribute1 = null;
    private Double threshold1 = 100.0;
    private String attribute2 = null;
    private Double threshold2 = 1.0;
    private Position position = Position.MID_BRANCH;
    private double dotSize = 5.0;
    private Color colour = Color.BLACK;
    private boolean hidingLabels = false;

    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}
