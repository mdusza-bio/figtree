/*
 * UsedColourChooser.java
 *
 * MyFigTree addition (Etap 7.9): the colour window with the colours that are already on
 * the tree shown above the palette - so adding one more specimen to a coloured group
 * is one click on "its" colour instead of hunting for the same shade.
 */

package figtree.application;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsedColourChooser {

    /** What one colour is used for on the tree. */
    public static class Usage {
        public int names = 0;
        public int branches = 0;
        public int highlights = 0;
        public final List<String> examples = new ArrayList<String>();

        String caption() {
            List<String> parts = new ArrayList<String>();
            if (names > 0) parts.add(names + (names == 1 ? " name" : " names"));
            if (branches > 0) parts.add(branches + (branches == 1 ? " branch" : " branches"));
            if (highlights > 0) parts.add(highlights + (highlights == 1 ? " highlight" : " highlights"));
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part);
            }
            return sb.toString();
        }
    }

    private static class Swatch implements Icon {
        final Color colour;

        Swatch(Color colour) {
            this.colour = colour;
        }

        public int getIconWidth() {
            return 34;
        }

        public int getIconHeight() {
            return 16;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(colour);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.GRAY);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }
    }

    private static String hex(Color colour) {
        return String.format("#%06X", colour.getRGB() & 0xFFFFFF);
    }

    /**
     * Shows the colour window. Returns the chosen colour, or null when cancelled.
     *
     * @param initial the colour the window starts on (the selection's own, when it has one)
     * @param used    the colours already on the tree, in the order they should be listed
     */
    public static Color showDialog(Component parent, String title, Color initial, Map<Color, Usage> used) {

        final JColorChooser chooser = new JColorChooser(initial == null ? Color.GRAY : initial);
        JPanel content = buildContent(chooser, used);

        int result = JOptionPane.showConfirmDialog(parent, content, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION ? chooser.getColor() : null;
    }

    /** The palette with the strip of colours already on the tree above it. */
    public static JPanel buildContent(final JColorChooser chooser, Map<Color, Usage> used) {

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(chooser, BorderLayout.CENTER);

        if (used != null && !used.isEmpty()) {
            final List<JButton> buttons = new ArrayList<JButton>();
            final List<Color> colours = new ArrayList<Color>();

            JPanel grid = new JPanel(new GridLayout(0, 3, 10, 4));
            for (Map.Entry<Color, Usage> entry : used.entrySet()) {
                final Color colour = entry.getKey();
                Usage usage = entry.getValue();

                JButton button = new JButton(usage.caption(), new Swatch(colour));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setMargin(new Insets(2, 4, 2, 4));
                button.putClientProperty("JComponent.sizeVariant", "small");

                StringBuilder tip = new StringBuilder("<html><b>" + hex(colour) + "</b> - " + usage.caption());
                for (String example : usage.examples) {
                    tip.append("<br>").append(example.replace('_', ' '));
                }
                int more = usage.names - usage.examples.size();
                if (more > 0) tip.append("<br>... and ").append(more).append(" more");
                tip.append("</html>");
                button.setToolTipText(tip.toString());

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        chooser.setColor(colour);
                    }
                });
                buttons.add(button);
                colours.add(colour);
                grid.add(button);
            }

            // the colour that is chosen right now gets a frame - at the start that is the
            // colour of whatever was selected in the tree
            final Runnable mark = new Runnable() {
                public void run() {
                    Color current = chooser.getColor();
                    for (int i = 0; i < buttons.size(); i++) {
                        boolean on = current != null && (current.getRGB() & 0xFFFFFF) == (colours.get(i).getRGB() & 0xFFFFFF);
                        buttons.get(i).setBorder(on ?
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(Color.BLACK, 2),
                                        BorderFactory.createEmptyBorder(2, 4, 2, 4)) :
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                                        BorderFactory.createEmptyBorder(3, 5, 3, 5)));
                    }
                }
            };
            chooser.getSelectionModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    mark.run();
                }
            });
            mark.run();

            JScrollPane scroll = new JScrollPane(grid);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            Dimension size = grid.getPreferredSize();
            scroll.setPreferredSize(new Dimension(size.width + 20, Math.min(size.height + 6, 130)));

            JPanel top = new JPanel(new BorderLayout(0, 4));
            top.add(new JLabel("<html><b>Already on this tree</b> - click one to use exactly the same colour " +
                    "(the framed one is chosen now):</html>"), BorderLayout.NORTH);
            top.add(scroll, BorderLayout.CENTER);
            content.add(top, BorderLayout.NORTH);
        }

        return content;
    }
}
