/*
 * LabelPainter.java
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

import java.awt.*;
import java.text.NumberFormat;

import figtree.treeviewer.decorators.Decorator;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public abstract class LabelPainter<T> extends AbstractPainter<T> {
    public static final String NAMES = "Names";
    public static final String NODE_AGES = "Node ages";
    public static final String NODE_HEIGHTS = "Node heights (raw)";
    public static final String BRANCH_TIMES = "Branch times";
    public static final String BRANCH_LENGTHS = "Branch lengths (raw)";
    public static final String SOLID_BOX = "Solid box";
    public static final String SOLID_BOX_ENCODED = "_SOLID_BOX_";

    public enum PainterIntent {
        NODE,
        BRANCH,
        TIP,
        RANGE
    };

    // MyFigTree: where a node label sits relative to the branch leading to the node
    public enum LabelPosition {
        AT_NODE("At node"),
        ABOVE_BRANCH("Above branch"),
        BELOW_BRANCH("Below branch");

        LabelPosition(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static LabelPosition fromString(String s) {
            for (LabelPosition p : values()) {
                if (p.name.equals(s) || p.name().equals(s)) return p;
            }
            return AT_NODE;
        }

        private final String name;
    }

    // MyFigTree: how a second value is combined with the first one
    public enum SecondValueLayout {
        SAME_LINE("a / b"),
        STACKED("stacked");

        SecondValueLayout(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static SecondValueLayout fromString(String s) {
            for (SecondValueLayout l : values()) {
                if (l.name.equals(s) || l.name().equals(s)) return l;
            }
            return SAME_LINE;
        }

        private final String name;
    }

    protected LabelPainter(PainterIntent intent) {
        this.intent = intent;
	}

	// Abstract

    public abstract String getDisplayAttribute();

    public abstract void setDisplayAttribute(String displayAttribute);

    public abstract void setTextDecorator(Decorator textDecorator);

    // Getters

	public Paint getForeground() {
		return foreground;
	}

	public Paint getBackground() {
		return background;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public Font getFont() {
		return font;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public boolean isVisible() {
	    return visible;
	}

    // Setters

	public void setBackground(Paint background) {
	    this.background = background;
	    firePainterChanged();
	}

	public void setBorder(Paint borderPaint, Stroke borderStroke) {
	    this.borderPaint = borderPaint;
	    this.borderStroke = borderStroke;
	    firePainterChanged();
	}

	public void setFont(Font font) {
		this.font = font;
	    firePainterChanged();
	}

	public void setForeground(Paint foreground) {
	    this.foreground = foreground;
	    firePainterChanged();
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	    firePainterChanged();
	}

	public void setBoxSize(int size) {
		// Do nothing
	}

	// Display-only name formatting (MyFigTree): overridden by BasicLabelPainter

	public void setReplaceUnderscores(boolean replaceUnderscores) {
		// Do nothing
	}

	public void setHideParts(String hideParts) {
		// Do nothing
	}

	public void setHideRegex(String hideRegex) {
		// Do nothing
	}

	// MyFigTree: label placement (Etap 2)

	public double getXPadding() {
		return xPadding;
	}

	public double getYPadding() {
		return yPadding;
	}

	public void setPadding(double xPadding, double yPadding) {
		this.xPadding = xPadding;
		this.yPadding = yPadding;
		firePainterChanged();
	}

	public LabelPosition getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(LabelPosition labelPosition) {
		this.labelPosition = labelPosition;
		firePainterChanged();
	}

	public void setShowThreshold(Double showThreshold) {
		// Do nothing
	}

	/**
	 * MyFigTree (Etap 6.6): when on, labels that would be drawn on top of each
	 * other are nudged apart vertically. Used for node (support) labels, which
	 * bunch up wherever a clade adds one taxon at a time.
	 */
	public boolean isAvoidOverlap() {
		return avoidOverlap;
	}

	public void setAvoidOverlap(boolean avoidOverlap) {
		this.avoidOverlap = avoidOverlap;
		firePainterChanged();
	}

	public void setSecondAttribute(String secondAttribute) {
		// Do nothing
	}

	public void setSecondValueLayout(SecondValueLayout layout) {
		// Do nothing
	}

	// MyFigTree: per-part label styling (Etap 3); overridden by BasicLabelPainter

	public LabelStyle getLabelStyle() {
		return null;
	}

	public void labelStyleChanged() {
		// Do nothing
	}

	public void setVisible(boolean visible) {
	    this.visible = visible;
	    firePainterChanged();
	}

    public PainterIntent getIntent() {
        return intent;
    }

    private Paint foreground = Color.BLACK;
	private Paint background = null;
	private Paint borderPaint = null;
	private Stroke borderStroke = null;

	private Font font;
	private boolean visible = true;

	private NumberFormat numberFormat = null;

	private double xPadding = 0.0;
	private double yPadding = 0.0;
	private LabelPosition labelPosition = LabelPosition.AT_NODE;
	private boolean avoidOverlap = false;

    private final PainterIntent intent;

}
