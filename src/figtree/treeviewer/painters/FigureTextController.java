/*
 * FigureTextController.java
 *
 * MyFigTree addition (Etap 7.7): the "Figure Text" control panel.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.prefs.Preferences;

public class FigureTextController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "figureText";

    public static final String TEXT_KEY = "text";
    public static final String CORNER_KEY = "corner";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String BOLD_KEY = "bold";
    public static final String ITALIC_KEY = "italic";
    public static final String MARGIN_KEY = "margin";

    public FigureTextController(final FigureTextPainter painter, final TreeViewer treeViewer) {
        this.painter = painter;

        final double defaultFontSize = PREFS.getDouble(CONTROLLER_KEY + "." + FONT_SIZE_KEY, 10.0);

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(painter.isVisible());
        titleCheckBox.setToolTipText("<html>A few lines of your own text in a corner of the figure -<br>" +
                "e.g. what the asterisks on the group bars stand for.</html>");

        textArea = new JTextArea(3, 14);
        textArea.setLineWrap(false);
        textArea.setToolTipText("One line of the figure text per line here.");
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
            private void apply() {
                painter.setText(toStored(textArea.getText()));
            }
        });
        JScrollPane textScroll = new JScrollPane(textArea);

        cornerCombo = new JComboBox(FigureTextPainter.Corner.values());
        cornerCombo.setSelectedItem(FigureTextPainter.Corner.BOTTOM_LEFT);
        cornerCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                painter.setCorner((FigureTextPainter.Corner) cornerCombo.getSelectedItem());
            }
        });

        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultFontSize, 1.0, 72.0, 1.0));
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setFontSize(((Number) fontSizeSpinner.getValue()).floatValue());
            }
        });
        painter.setFontSize((float) defaultFontSize);

        marginSpinner = new JSpinner(new SpinnerNumberModel(6.0, 0.0, 200.0, 1.0));
        marginSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setMargin(((Number) marginSpinner.getValue()).doubleValue());
            }
        });

        boldCheckBox = new JCheckBox("Bold");
        boldCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBold(boldCheckBox.isSelected());
            }
        });

        italicCheckBox = new JCheckBox("Italic");
        italicCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setItalic(italicCheckBox.isSelected());
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Text:", textScroll);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Corner:", cornerCombo);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Font size:", fontSizeSpinner);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Margin (pt):", marginSpinner);
        optionsPanel.addSpanningComponent(boldCheckBox);
        optionsPanel.addSpanningComponent(italicCheckBox);

        addComponent(label1);
        addComponent(textScroll);
        addComponent(textArea);
        addComponent(label2);
        addComponent(cornerCombo);
        addComponent(label3);
        addComponent(fontSizeSpinner);
        addComponent(label4);
        addComponent(marginSpinner);
        addComponent(boldCheckBox);
        addComponent(italicCheckBox);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                painter.setVisible(titleCheckBox.isSelected());
            }
        });
    }

    /** The panel shows real line breaks; the painter and the .tree file keep "|" instead. */
    private static String toStored(String shown) {
        return shown.replace("\r\n", "\n").replace('\n', '|');
    }

    private static String toShown(String stored) {
        return stored.replace('|', '\n');
    }

    public JComponent getTitleComponent() {
        return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void initialize() {
        // nothing to do
    }

    private static Object get(Map<String, Object> settings, String key) {
        return settings.get(CONTROLLER_KEY + "." + key);
    }

    public void setSettings(Map<String, Object> settings) {
        // older .tree files have none of these keys - keep the defaults then
        Object shown = get(settings, IS_SHOWN);
        titleCheckBox.setSelected(shown instanceof Boolean && (Boolean) shown);
        Object text = get(settings, TEXT_KEY);
        textArea.setText(text == null ? "" : toShown(text.toString()));
        Object corner = get(settings, CORNER_KEY);
        if (corner != null) {
            try {
                cornerCombo.setSelectedItem(FigureTextPainter.Corner.valueOf(corner.toString()));
            } catch (IllegalArgumentException e) {
                // unknown word in the file - keep the current corner
            }
        }
        Object size = get(settings, FONT_SIZE_KEY);
        if (size instanceof Number) fontSizeSpinner.setValue(((Number) size).doubleValue());
        Object margin = get(settings, MARGIN_KEY);
        if (margin instanceof Number) marginSpinner.setValue(((Number) margin).doubleValue());
        Object bold = get(settings, BOLD_KEY);
        boldCheckBox.setSelected(bold instanceof Boolean && (Boolean) bold);
        Object italic = get(settings, ITALIC_KEY);
        italicCheckBox.setSelected(italic instanceof Boolean && (Boolean) italic);
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + TEXT_KEY, toStored(textArea.getText()));
        settings.put(CONTROLLER_KEY + "." + CORNER_KEY, ((FigureTextPainter.Corner) cornerCombo.getSelectedItem()).name());
        settings.put(CONTROLLER_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + MARGIN_KEY, marginSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BOLD_KEY, boldCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ITALIC_KEY, italicCheckBox.isSelected());
    }

    public String getTitle() {
        return "Figure Text";
    }

    private final FigureTextPainter painter;

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JTextArea textArea;
    private final JComboBox cornerCombo;
    private final JSpinner fontSizeSpinner;
    private final JSpinner marginSpinner;
    private final JCheckBox boldCheckBox;
    private final JCheckBox italicCheckBox;
}
