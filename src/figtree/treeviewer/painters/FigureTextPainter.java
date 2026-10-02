/*
 * FigureTextPainter.java
 *
 * MyFigTree addition (Etap 7.7): a few lines of free text in a corner of the figure -
 * a legend for the asterisks on the group bars ("* ARGENTODERMATALES"), a figure
 * caption, anything the journal wants on the picture itself.
 */

package figtree.treeviewer.painters;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the text over the finished figure, in the chosen corner. The text is kept with
 * "|" between the lines, so that it survives the FigTree block of the .tree file.
 */
public class FigureTextPainter {

    public enum Corner {
        TOP_LEFT("Top left"),
        TOP_RIGHT("Top right"),
        BOTTOM_LEFT("Bottom left"),
        BOTTOM_RIGHT("Bottom right");

        Corner(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    /** Lines separated by "|" (a real line break in the panel is turned into "|" by the controller). */
    public void setText(String text) {
        this.text = text == null ? "" : text;
        firePainterChanged();
    }

    public String getText() {
        return text;
    }

    public void setCorner(Corner corner) {
        this.corner = corner == null ? Corner.BOTTOM_LEFT : corner;
        firePainterChanged();
    }

    public void setFontSize(float fontSize) {
        this.font = font.deriveFont(fontSize);
        firePainterChanged();
    }

    public void setBold(boolean bold) {
        this.bold = bold;
        firePainterChanged();
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
        firePainterChanged();
    }

    public void setMargin(double margin) {
        this.margin = margin;
        firePainterChanged();
    }

    private Font styledFont() {
        int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
        return font.deriveFont(style);
    }

    private List<String> lines() {
        List<String> lines = new ArrayList<String>();
        for (String line : text.split("\\|")) {
            lines.add(line.trim());
        }
        return lines;
    }

    /** Call last, over the whole figure; width and height are the figure's. */
    public void paint(Graphics2D g2, double width, double height) {
        if (!visible || text.trim().length() == 0) return;

        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();

        Font styled = styledFont();
        g2.setFont(styled);
        g2.setPaint(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics(styled);
        double lineHeight = fm.getAscent() + fm.getDescent();
        List<String> lines = lines();

        double widest = 0.0;
        for (String line : lines) {
            widest = Math.max(widest, fm.stringWidth(line));
        }
        double blockHeight = lineHeight * lines.size();

        boolean left = corner == Corner.TOP_LEFT || corner == Corner.BOTTOM_LEFT;
        boolean top = corner == Corner.TOP_LEFT || corner == Corner.TOP_RIGHT;
        double y = top ? margin : height - margin - blockHeight;

        for (String line : lines) {
            double x = left ? margin : width - margin - fm.stringWidth(line);
            g2.drawString(line, (float) x, (float) (y + fm.getAscent()));
            y += lineHeight;
        }

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
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
    private String text = "";
    private Corner corner = Corner.BOTTOM_LEFT;
    private Font font = new Font("sansserif", Font.PLAIN, 10);
    private boolean bold = false;
    private boolean italic = false;
    private double margin = 6.0;

    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}
