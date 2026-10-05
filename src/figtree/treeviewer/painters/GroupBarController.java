/*
 * GroupBarController.java
 *
 * MyFigTree addition (Etap 4.3 / 4.4): the "Group Bars" control panel.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.ExtendedTreeViewer;
import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import figtree.treeviewer.decorators.ColourDecorator;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.Color;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jebl.evolution.taxa.Taxon;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

public class GroupBarController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "groupBars";

    public static final String ATTRIBUTE_KEY = "attribute";
    public static final String BAR_WIDTH_KEY = "barWidth";
    public static final String GAP_KEY = "gap";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String TEXT_DIRECTION_KEY = "textDirection";
    private static final String TEXT_UP_KEY = "UP";
    private static final String TEXT_DOWN_KEY = "DOWN";
    private static final String TEXT_UP = "Upwards, facing tree";
    private static final String TEXT_DOWN = "Downwards, facing away";
    public static final String BACKGROUNDS_KEY = "backgrounds";
    public static final String BACKGROUND_ATTRIBUTE_KEY = "backgroundAttribute";
    public static final String BACKGROUND_ALPHA_KEY = "backgroundAlpha";
    public static final String BACKGROUND_LABELS_KEY = "backgroundLabels";
    public static final String BACKGROUND_LABEL_SIZE_KEY = "backgroundLabelSize";
    public static final String BACKGROUND_LABEL_BOLD_KEY = "backgroundLabelBold";
    public static final String BACKGROUND_LABEL_ITALIC_KEY = "backgroundLabelItalic";
    public static final String ITALIC_KEY = "italic";
    public static final String HIDE_UNFITTING_KEY = "hideUnfitting";
    public static final String BACKGROUND_GAP_KEY = "backgroundGap";
    public static final String BACKGROUND_GRADIENT_KEY = "backgroundGradient";
    public static final String NOT_ITALIC_WORDS_KEY = "notItalicWords";
    public static final String COLOURS_KEY = "colours";

    public GroupBarController(final GroupBarPainter painter,
                              final AttributeColourController colourController,
                              final TreeViewer treeViewer) {
        this.painter = painter;
        this.colourController = colourController;

        final double defaultBarWidth = PREFS.getDouble(CONTROLLER_KEY + "." + BAR_WIDTH_KEY, 8.0);
        final double defaultGap = PREFS.getDouble(CONTROLLER_KEY + "." + GAP_KEY, 6.0);
        final double defaultFontSize = PREFS.getDouble(CONTROLLER_KEY + "." + FONT_SIZE_KEY, 10.0);

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(painter.isBarsVisible());

        // only real tip attributes (family, order...) - the built-in label choices such as Names
        // used to come first, so ticking Group Bars after an import silently drew nothing
        attributeCombo = new JComboBox();
        AttributeComboHelper.attributesOnly(attributeCombo, treeViewer, LabelPainter.PainterIntent.TIP).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateBarAttribute();
            }
        });

        backgroundAttributeCombo = new JComboBox();
        AttributeComboHelper.attributesOnly(backgroundAttributeCombo, treeViewer, LabelPainter.PainterIntent.TIP).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateBackgroundAttribute();
            }
        });

        // registered after the combo helpers, so it runs once the lists have been refilled: the first
        // attribute to appear (say family, just imported) is picked by the combo without an event,
        // and the bars have to follow what the combo shows
        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                updateBarAttribute();
                updateBackgroundAttribute();
                updateHint();
            }

            public void treeSettingsChanged() {
                updateHint();
            }
        });

        hintLabel = new JLabel();
        hintLabel.putClientProperty("JComponent.sizeVariant", "small");

        // follow changes to the colour schemes ("Colour by" in Appearance)
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                updateBarAttribute();
                updateBackgroundAttribute();
            }
        });

        barWidthSpinner = new JSpinner(new SpinnerNumberModel(defaultBarWidth, 0.5, 100.0, 1.0));
        barWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBarWidth(((Number) barWidthSpinner.getValue()).doubleValue());
            }
        });
        painter.setBarWidth(defaultBarWidth);

        gapSpinner = new JSpinner(new SpinnerNumberModel(defaultGap, 0.0, 200.0, 1.0));
        gapSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setGap(((Number) gapSpinner.getValue()).doubleValue());
            }
        });
        painter.setGap(defaultGap);

        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultFontSize, 1.0, 72.0, 1.0));
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setFontSize(((Number) fontSizeSpinner.getValue()).floatValue());
            }
        });
        painter.setFontSize((float) defaultFontSize);

        // Etap 6.20: group names running up (facing the tree) or down (facing away)
        textDirectionCombo = new JComboBox(new String[]{TEXT_UP, TEXT_DOWN});
        textDirectionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                painter.setTextDownwards(TEXT_DOWN.equals(textDirectionCombo.getSelectedItem()));
            }
        });

        barItalicCheckBox = new JCheckBox("Italic names");
        barItalicCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBarItalic(barItalicCheckBox.isSelected());
            }
        });

        // Etap 7.6: a name longer than its group is left out instead of running into the neighbours
        hideUnfittingCheckBox = new JCheckBox("Hide names that do not fit");
        hideUnfittingCheckBox.setToolTipText("<html>Leaves out the name of a group that is shorter than its name.<br>" +
                "Break long names with \"|\" (ECHINO-|STELIALES) or mark small groups<br>" +
                "with \"*\" via Assign to selection... to keep them labelled.</html>");
        hideUnfittingCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setHideUnfitting(hideUnfittingCheckBox.isSelected());
            }
        });

        backgroundsCheckBox = new JCheckBox("Backgrounds");
        backgroundsCheckBox.setSelected(painter.isBackgroundsVisible());
        backgroundsCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundsVisible(backgroundsCheckBox.isSelected());
                enableBackgroundComponents();
            }
        });

        backgroundAlphaSpinner = new JSpinner(new SpinnerNumberModel(25, 5, 100, 5));
        backgroundAlphaSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundAlpha(((Number) backgroundAlphaSpinner.getValue()).doubleValue() / 100.0);
            }
        });

        // Etap 7.2 / 7.3: white gap between neighbouring backgrounds, white-to-colour gradient
        final double defaultBackgroundGap = PREFS.getDouble(CONTROLLER_KEY + "." + BACKGROUND_GAP_KEY, 2.0);
        backgroundGapSpinner = new JSpinner(new SpinnerNumberModel(defaultBackgroundGap, 0.0, 20.0, 0.5));
        backgroundGapSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundGap(((Number) backgroundGapSpinner.getValue()).doubleValue());
            }
        });
        painter.setBackgroundGap(defaultBackgroundGap);

        backgroundGradientCombo = new JComboBox(GroupBarPainter.Gradient.values());
        backgroundGradientCombo.setToolTipText("<html>Fades each background between white and its colour,<br>" +
                "from the clade's node to the names - either way round.</html>");
        backgroundGradientCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                painter.setBackgroundGradient((GroupBarPainter.Gradient) backgroundGradientCombo.getSelectedItem());
            }
        });

        // words kept upright inside italic names - "Outgroup Trichiales" has one italic word only
        final String defaultNotItalic = PREFS.get(CONTROLLER_KEY + "." + NOT_ITALIC_WORDS_KEY, "Outgroup");
        notItalicWordsField = new JTextField(defaultNotItalic, 10);
        notItalicWordsField.setToolTipText("<html>Words that stay upright when names are italic, separated by<br>" +
                "commas or spaces (case does not matter). Applies to bars and backgrounds.</html>");
        notItalicWordsField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { apply(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { apply(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { apply(); }
            private void apply() {
                painter.setNotItalicWords(notItalicWordsField.getText());
            }
        });
        painter.setNotItalicWords(defaultNotItalic);

        // Etap 7.4: clade names written inside the backgrounds, at their right-hand edge
        final double defaultLabelSize = PREFS.getDouble(CONTROLLER_KEY + "." + BACKGROUND_LABEL_SIZE_KEY, 11.0);
        backgroundLabelsCheckBox = new JCheckBox("Clade names in backgrounds");
        backgroundLabelsCheckBox.setToolTipText("<html>Writes the value of the attribute at the right-hand edge of its<br>" +
                "background, e.g. <i>Clade 3 Argentodermataceae</i>. In the value, &quot;|&quot; starts<br>" +
                "a new line and &lt;b&gt;...&lt;/b&gt; makes that one name bold.</html>");
        backgroundLabelsCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundLabelsVisible(backgroundLabelsCheckBox.isSelected());
                enableBackgroundComponents();
            }
        });

        backgroundLabelSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultLabelSize, 1.0, 72.0, 1.0));
        backgroundLabelSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundLabelFontSize(((Number) backgroundLabelSizeSpinner.getValue()).floatValue());
            }
        });
        painter.setBackgroundLabelFontSize((float) defaultLabelSize);

        backgroundLabelBoldCheckBox = new JCheckBox("Bold names");
        backgroundLabelBoldCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundLabelBold(backgroundLabelBoldCheckBox.isSelected());
            }
        });

        backgroundLabelItalicCheckBox = new JCheckBox("Italic names");
        backgroundLabelItalicCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                painter.setBackgroundLabelItalic(backgroundLabelItalicCheckBox.isSelected());
            }
        });

        // Etap 6.3: assigning a group to a whole clade at once, instead of Annotate tip by tip
        assignButton = new JButton("Assign to selection...");
        assignButton.putClientProperty("JComponent.sizeVariant", "small");
        assignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                assignToSelection(treeViewer);
            }
        });

        titleCheckBox.setToolTipText("<html>Coloured bars to the right of the tip labels, one per group of<br>" +
                "neighbouring tips sharing a value (e.g. the same family).<br>" +
                "Drawn in the rectangular layout only.</html>");

        final JLabel label1 = optionsPanel.addComponentWithLabel("Attribute:", attributeCombo);
        optionsPanel.addSpanningComponent(hintLabel);
        optionsPanel.addSpanningComponent(assignButton);

        // Etap 7.1: the user's own colour for every group of the bars and of the backgrounds
        coloursButton = new JButton("Colours...");
        coloursButton.putClientProperty("JComponent.sizeVariant", "small");
        coloursButton.setToolTipText("<html>Pick your own colour for each group of the bars and<br>" +
                "of the backgrounds (grey outgroups, one colour per clade...).</html>");
        coloursButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                editColours();
            }
        });
        optionsPanel.addSpanningComponent(coloursButton);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Bar width:", barWidthSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Gap from labels:", gapSpinner);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Font size:", fontSizeSpinner);
        final JLabel label5 = optionsPanel.addComponentWithLabel("Text reads:", textDirectionCombo);
        optionsPanel.addSpanningComponent(barItalicCheckBox);
        optionsPanel.addSpanningComponent(hideUnfittingCheckBox);
        optionsPanel.addSeparator();
        optionsPanel.addSpanningComponent(backgroundsCheckBox);
        backgroundLabel1 = optionsPanel.addComponentWithLabel("Attribute:", backgroundAttributeCombo);
        backgroundLabel2 = optionsPanel.addComponentWithLabel("Opacity (%):", backgroundAlphaSpinner);
        backgroundLabel4 = optionsPanel.addComponentWithLabel("Gap between (pt):", backgroundGapSpinner);
        backgroundLabel5 = optionsPanel.addComponentWithLabel("Gradient:", backgroundGradientCombo);
        optionsPanel.addSpanningComponent(backgroundLabelsCheckBox);
        backgroundLabel3 = optionsPanel.addComponentWithLabel("Name size:", backgroundLabelSizeSpinner);
        optionsPanel.addSpanningComponent(backgroundLabelBoldCheckBox);
        optionsPanel.addSpanningComponent(backgroundLabelItalicCheckBox);
        backgroundLabel6 = optionsPanel.addComponentWithLabel("Not italic words:", notItalicWordsField);

        addComponent(label1);
        addComponent(attributeCombo);
        addComponent(label2);
        addComponent(barWidthSpinner);
        addComponent(label3);
        addComponent(gapSpinner);
        addComponent(label4);
        addComponent(fontSizeSpinner);
        addComponent(label5);
        addComponent(textDirectionCombo);
        addComponent(barItalicCheckBox);
        addComponent(hideUnfittingCheckBox);
        enableComponents(titleCheckBox.isSelected());
        enableBackgroundComponents();
        updateHint();

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                enableBackgroundComponents();
                painter.setBarsVisible(titleCheckBox.isSelected());
            }
        });
    }

    /**
     * MyFigTree (Etap 6.3): asks for an attribute name and a value and puts them on every tip of the
     * current selection - select a clade and the whole group is labelled in one go, instead of using
     * Annotate tip by tip.
     */
    private void assignToSelection(TreeViewer treeViewer) {

        if (!(treeViewer instanceof ExtendedTreeViewer)) {
            return;
        }

        final JComboBox nameCombo = new JComboBox();
        nameCombo.setEditable(true);
        Set<String> names = new LinkedHashSet<String>();
        for (int i = 0; i < attributeCombo.getItemCount(); i++) {
            Object item = attributeCombo.getItemAt(i);
            if (item != null && item.toString().length() > 0 && !item.toString().startsWith("!")) {
                names.add(item.toString());
            }
        }
        names.add(lastAssignedName);
        for (String name : names) {
            nameCombo.addItem(name);
        }
        Object selected = attributeCombo.getSelectedItem();
        nameCombo.setSelectedItem(selected != null && selected.toString().length() > 0 ?
                selected.toString() : lastAssignedName);

        final ExtendedTreeViewer viewer = (ExtendedTreeViewer) treeViewer;
        final Set<Taxon> selectedTaxa = viewer.getSelectedTipTaxa();
        if (selectedTaxa.isEmpty()) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "Nothing is selected in the tree.\n\n" +
                            "Select a clade (or some tip labels) first - every tip of the\n" +
                            "selection then gets the value.",
                    "Assign to Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final JTextField valueField = new JTextField(lastAssignedValue, 16);

        // Etap 7.6b: say which attribute drives what, and what the selection holds right now -
        // so an asterisk lands in "order" and not in "family", and the old value is in sight
        Object barItem = attributeCombo.getSelectedItem();
        Object bgItem = backgroundAttributeCombo.getSelectedItem();
        JLabel rolesLabel = new JLabel("<html><i>Bars: " + (barItem == null ? "-" : barItem) +
                " &nbsp;&middot;&nbsp; Backgrounds: " + (bgItem == null ? "-" : bgItem) +
                " &nbsp;&middot;&nbsp; " + selectedTaxa.size() + (selectedTaxa.size() == 1 ? " tip" : " tips") +
                " selected</i></html>");
        rolesLabel.putClientProperty("JComponent.sizeVariant", "small");
        final JLabel currentLabel = new JLabel();
        currentLabel.putClientProperty("JComponent.sizeVariant", "small");

        final Runnable showCurrent = new Runnable() {
            public void run() {
                Object item = nameCombo.getSelectedItem();
                String attribute = item == null ? "" : item.toString().trim();
                Set<String> values = new TreeSet<String>();
                boolean restorable = false;
                for (Taxon taxon : selectedTaxa) {
                    Object v = taxon.getAttribute(attribute);
                    values.add(v == null ? "(none)" : v.toString());
                    if (taxon.getAttribute(ExtendedTreeViewer.ORIGINAL_PREFIX + attribute) != null) {
                        restorable = true;
                    }
                }
                StringBuilder text = new StringBuilder("<html>Now: ");
                int shown = 0;
                for (String v : values) {
                    if (shown++ == 4) {
                        text.append(", ... (").append(values.size()).append(" different)");
                        break;
                    }
                    if (shown > 1) text.append(", ");
                    text.append(v.replace("<", "&lt;"));
                }
                if (restorable) {
                    text.append("<br>Changed here before - <b>Restore original</b> brings the old value back.");
                }
                text.append("</html>");
                currentLabel.setText(text.toString());
                // one shared value: start from it, so a small edit (or a "|") is enough
                if (values.size() == 1 && !values.contains("(none)")) {
                    valueField.setText(values.iterator().next());
                    valueField.selectAll();
                }
            }
        };
        nameCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                showCurrent.run();
            }
        });
        showCurrent.run();

        OptionsPanel dialogPanel = new OptionsPanel(6, 6);
        dialogPanel.addSpanningComponent(rolesLabel);
        dialogPanel.addComponentWithLabel("Attribute:", nameCombo);
        dialogPanel.addSpanningComponent(currentLabel);
        dialogPanel.addComponentWithLabel("Value:", valueField);

        String[] options = {"Assign", "Restore original", "Cancel"};
        int result = JOptionPane.showOptionDialog(optionsPanel, dialogPanel, "Assign to Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        Object nameItem = nameCombo.getSelectedItem();
        String name = nameItem == null ? "" : nameItem.toString().trim();
        String value = valueField.getText().trim();

        if (result == 1) {
            int restored = name.length() == 0 ? 0 : viewer.restoreSelectedTaxa(name);
            if (restored == 0) {
                JOptionPane.showMessageDialog(optionsPanel,
                        "Nothing to restore for " + (name.length() == 0 ? "this attribute" : name) + ":\n" +
                                "these tips were not changed with Assign to selection (or were restored already).\n\n" +
                                "To get dictionary values back, import the dictionary again\n" +
                                "(File > Import Annotations...).",
                        "Assign to Selection",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }
        if (result != 0) {
            return;
        }

        if (name.length() == 0 || value.length() == 0) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "Both an attribute name (such as family) and a value\n" +
                            "(such as Trichiaceae) are needed.",
                    "Assign to Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        viewer.annotateSelectedTaxa(name, value);

        lastAssignedName = name;
        lastAssignedValue = value;

        attributeCombo.setSelectedItem(name);
        if (!titleCheckBox.isSelected()) {
            titleCheckBox.setSelected(true);
        }
    }

    /** A small colour patch for the buttons of the Colours dialog. */
    private static class Swatch implements Icon {
        Color colour;

        Swatch(Color colour) {
            this.colour = colour;
        }

        public int getIconWidth() {
            return 36;
        }

        public int getIconHeight() {
            return 14;
        }

        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            g.setColor(colour);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.GRAY);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }
    }

    /** How a group's value reads in the Colours dialog: no bold marker, "|" shown as a slash. */
    private static String plainName(Object value) {
        return String.valueOf(value).replaceAll("(?i)</?b>", "").replace("|", " / ").trim();
    }

    /**
     * MyFigTree (Etap 7.1): one row per group with a colour button, for the bars and for the
     * backgrounds. A pick shows on the tree at once; Cancel puts the previous colours back.
     */
    private void editColours() {
        String barAttribute = painter.getBarAttribute();
        String backgroundAttribute = painter.getBackgroundAttribute();

        // attribute -> heading; one section when bars and backgrounds share the attribute
        Map<String, String> sections = new java.util.LinkedHashMap<String, String>();
        if (backgroundAttribute != null && !painter.getValuesInOrder(backgroundAttribute).isEmpty()) {
            sections.put(backgroundAttribute, backgroundAttribute.equals(barAttribute) ?
                    "Bars and backgrounds: " + backgroundAttribute : "Backgrounds: " + backgroundAttribute);
        }
        if (barAttribute != null && !sections.containsKey(barAttribute) &&
                !painter.getValuesInOrder(barAttribute).isEmpty()) {
            sections.put(barAttribute, "Bars: " + barAttribute);
        }
        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "There are no groups on the tree yet.\n\n" +
                            "Turn on Group Bars or Backgrounds and pick an attribute first.",
                    "Group Colours",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final Map<String, Map<String, Color>> before = painter.getCustomColours();
        // every button with what it colours, so that "defaults" can repaint them all
        final java.util.List<Object[]> buttons = new java.util.ArrayList<Object[]>();

        JPanel rows = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(2, 4, 2, 8);

        for (Map.Entry<String, String> section : sections.entrySet()) {
            final String attribute = section.getKey();
            JLabel heading = new JLabel("<html><b>" + section.getValue() + "</b></html>");
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            rows.add(heading, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;

            for (final Object value : painter.getValuesInOrder(attribute)) {
                final Swatch swatch = new Swatch(painter.getColour(attribute, value));
                final JButton button = new JButton(swatch);
                button.setMargin(new java.awt.Insets(2, 2, 2, 2));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Color picked = JColorChooser.showDialog(button,
                                "Colour of " + plainName(value), swatch.colour);
                        if (picked != null) {
                            swatch.colour = picked;
                            button.repaint();
                            painter.setCustomColour(attribute, value, picked);
                        }
                    }
                });
                buttons.add(new Object[]{attribute, value, swatch, button});
                gbc.gridx = 0;
                rows.add(button, gbc);
                gbc.gridx = 1;
                rows.add(new JLabel(plainName(value)), gbc);
                gbc.gridy++;
            }
        }

        JButton defaultsButton = new JButton("Back to built-in colours");
        defaultsButton.putClientProperty("JComponent.sizeVariant", "small");
        defaultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                for (Object[] row : buttons) {
                    painter.setCustomColour((String) row[0], row[1], null);
                }
                for (Object[] row : buttons) {
                    ((Swatch) row[2]).colour = painter.getColour((String) row[0], row[1]);
                    ((JButton) row[3]).repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        java.awt.Dimension size = rows.getPreferredSize();
        scroll.setPreferredSize(new java.awt.Dimension(Math.min(size.width + 40, 520), Math.min(size.height + 10, 420)));

        JPanel content = new JPanel(new java.awt.BorderLayout(0, 8));
        content.add(new JLabel("<html>Click a colour to change it - the tree follows at once.<br>" +
                "<i>With flat backgrounds the colour is paled by Opacity.</i></html>"), java.awt.BorderLayout.NORTH);
        content.add(scroll, java.awt.BorderLayout.CENTER);
        content.add(defaultsButton, java.awt.BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(optionsPanel, content, "Group Colours",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            painter.setCustomColours(before);
        }
    }

    // ---- the colours in the .tree file: attribute:group:rrggbb,... with the names URL-encoded ----

    private static String encodeColours(Map<String, Map<String, Color>> colours) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, Map<String, Color>> attribute : colours.entrySet()) {
                for (Map.Entry<String, Color> group : attribute.getValue().entrySet()) {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(java.net.URLEncoder.encode(attribute.getKey(), "UTF-8")).append(':')
                            .append(java.net.URLEncoder.encode(group.getKey(), "UTF-8")).append(':')
                            .append(String.format("%06x", group.getValue().getRGB() & 0xFFFFFF));
                }
            }
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 is always there
        }
        return sb.toString();
    }

    private static Map<String, Map<String, Color>> decodeColours(String text) {
        Map<String, Map<String, Color>> colours = new java.util.LinkedHashMap<String, Map<String, Color>>();
        if (text == null) return colours;
        for (String item : text.split(",")) {
            String[] parts = item.trim().split(":");
            if (parts.length != 3) continue;
            try {
                String attribute = java.net.URLDecoder.decode(parts[0], "UTF-8");
                String group = java.net.URLDecoder.decode(parts[1], "UTF-8");
                Color colour = new Color(Integer.parseInt(parts[2], 16));
                Map<String, Color> map = colours.get(attribute);
                if (map == null) {
                    map = new java.util.LinkedHashMap<String, Color>();
                    colours.put(attribute, map);
                }
                map.put(group, colour);
            } catch (Exception e) {
                // a damaged entry - skip it, keep the rest
            }
        }
        return colours;
    }

    /** Says what to do when there is nothing to group by yet, instead of showing an empty list. */
    private void updateHint() {
        boolean empty = attributeCombo.getItemCount() == 0;
        hintLabel.setText(empty ?
                "<html><i>No groups yet: use File &gt; Import Annotations...<br>" +
                        "or select a clade and Assign to selection...</i></html>" : "");
        hintLabel.setVisible(empty);
    }

    private void enableBackgroundComponents() {
        boolean on = backgroundsCheckBox.isSelected();
        backgroundLabel1.setEnabled(on);
        backgroundAttributeCombo.setEnabled(on);
        backgroundLabel2.setEnabled(on);
        backgroundAlphaSpinner.setEnabled(on);
        backgroundLabel4.setEnabled(on);
        backgroundGapSpinner.setEnabled(on);
        backgroundLabel5.setEnabled(on);
        backgroundGradientCombo.setEnabled(on);
        backgroundLabelsCheckBox.setEnabled(on);
        boolean names = on && backgroundLabelsCheckBox.isSelected();
        backgroundLabel3.setEnabled(names);
        // the upright words serve the bars too, so the field stays usable whenever either is drawn
        boolean italics = names || titleCheckBox.isSelected();
        backgroundLabel6.setEnabled(italics);
        notItalicWordsField.setEnabled(italics);
        backgroundLabelSizeSpinner.setEnabled(names);
        backgroundLabelBoldCheckBox.setEnabled(names);
        backgroundLabelItalicCheckBox.setEnabled(names);
    }

    private ColourDecorator decoratorFor(String attribute) {
        if (attribute == null || attribute.length() == 0) return null;
        try {
            return colourController.getDecoratorForAttribute(attribute);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void updateBarAttribute() {
        String attribute = (String) attributeCombo.getSelectedItem();
        painter.setBarAttribute(attribute, decoratorFor(attribute));
    }

    private void updateBackgroundAttribute() {
        String attribute = (String) backgroundAttributeCombo.getSelectedItem();
        painter.setBackgroundAttribute(attribute, decoratorFor(attribute));
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

    private static boolean getBool(Map<String, Object> settings, String key, boolean def) {
        Object v = settings.get(CONTROLLER_KEY + "." + key);
        return (v instanceof Boolean) ? (Boolean) v : def;
    }

    private static double getDouble(Map<String, Object> settings, String key, double def) {
        Object v = settings.get(CONTROLLER_KEY + "." + key);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    private static String getString(Map<String, Object> settings, String key) {
        Object v = settings.get(CONTROLLER_KEY + "." + key);
        return (v == null) ? null : v.toString();
    }

    public void setSettings(Map<String, Object> settings) {
        // old .tree files have none of these keys - keep the defaults then
        titleCheckBox.setSelected(getBool(settings, IS_SHOWN, false));
        String attribute = getString(settings, ATTRIBUTE_KEY);
        if (attribute != null) attributeCombo.setSelectedItem(attribute);
        barWidthSpinner.setValue(getDouble(settings, BAR_WIDTH_KEY, 8.0));
        gapSpinner.setValue(getDouble(settings, GAP_KEY, 6.0));
        fontSizeSpinner.setValue(getDouble(settings, FONT_SIZE_KEY, 10.0));
        textDirectionCombo.setSelectedItem(TEXT_DOWN_KEY.equals(getString(settings, TEXT_DIRECTION_KEY)) ? TEXT_DOWN : TEXT_UP);
        backgroundsCheckBox.setSelected(getBool(settings, BACKGROUNDS_KEY, false));
        String bgAttribute = getString(settings, BACKGROUND_ATTRIBUTE_KEY);
        if (bgAttribute != null) backgroundAttributeCombo.setSelectedItem(bgAttribute);
        backgroundAlphaSpinner.setValue((int) Math.round(getDouble(settings, BACKGROUND_ALPHA_KEY, 25.0)));
        backgroundLabelsCheckBox.setSelected(getBool(settings, BACKGROUND_LABELS_KEY, false));
        backgroundLabelSizeSpinner.setValue(getDouble(settings, BACKGROUND_LABEL_SIZE_KEY, 11.0));
        backgroundLabelBoldCheckBox.setSelected(getBool(settings, BACKGROUND_LABEL_BOLD_KEY, false));
        backgroundLabelItalicCheckBox.setSelected(getBool(settings, BACKGROUND_LABEL_ITALIC_KEY, false));
        barItalicCheckBox.setSelected(getBool(settings, ITALIC_KEY, false));
        hideUnfittingCheckBox.setSelected(getBool(settings, HIDE_UNFITTING_KEY, false));
        backgroundGapSpinner.setValue(getDouble(settings, BACKGROUND_GAP_KEY, 2.0));
        // 7.3 first saved a yes/no; since 7.3b it is the direction's name
        Object gradient = settings.get(CONTROLLER_KEY + "." + BACKGROUND_GRADIENT_KEY);
        GroupBarPainter.Gradient direction = GroupBarPainter.Gradient.NONE;
        if (gradient instanceof Boolean) {
            direction = (Boolean) gradient ? GroupBarPainter.Gradient.WHITE_AT_NODE : GroupBarPainter.Gradient.NONE;
        } else if (gradient != null) {
            try {
                direction = GroupBarPainter.Gradient.valueOf(gradient.toString());
            } catch (IllegalArgumentException e) {
                // unknown word in the file - keep "None"
            }
        }
        backgroundGradientCombo.setSelectedItem(direction);
        String words = getString(settings, NOT_ITALIC_WORDS_KEY);
        notItalicWordsField.setText(words == null ? "Outgroup" : words);
        // a tree without the key has no colours of its own - do not keep the previous tree's
        painter.setCustomColours(decodeColours(getString(settings, COLOURS_KEY)));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ATTRIBUTE_KEY, attributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + BAR_WIDTH_KEY, barWidthSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + GAP_KEY, gapSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + TEXT_DIRECTION_KEY, TEXT_DOWN.equals(textDirectionCombo.getSelectedItem()) ? TEXT_DOWN_KEY : TEXT_UP_KEY);
        settings.put(CONTROLLER_KEY + "." + BACKGROUNDS_KEY, backgroundsCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_ATTRIBUTE_KEY, backgroundAttributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_ALPHA_KEY, backgroundAlphaSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_LABELS_KEY, backgroundLabelsCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_LABEL_SIZE_KEY, backgroundLabelSizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_LABEL_BOLD_KEY, backgroundLabelBoldCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_LABEL_ITALIC_KEY, backgroundLabelItalicCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ITALIC_KEY, barItalicCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + HIDE_UNFITTING_KEY, hideUnfittingCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_GAP_KEY, backgroundGapSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_GRADIENT_KEY,
                ((GroupBarPainter.Gradient) backgroundGradientCombo.getSelectedItem()).name());
        settings.put(CONTROLLER_KEY + "." + NOT_ITALIC_WORDS_KEY, notItalicWordsField.getText());
        String colours = encodeColours(painter.getCustomColours());
        if (colours.length() > 0) {
            settings.put(CONTROLLER_KEY + "." + COLOURS_KEY, colours);
        }
    }

    public String getTitle() {
        return "Group Bars";
    }

    private final GroupBarPainter painter;
    private final AttributeColourController colourController;

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JButton assignButton;
    private final JButton coloursButton;
    private final JLabel hintLabel;
    private String lastAssignedName = "group";
    private String lastAssignedValue = "";

    private final JComboBox attributeCombo;
    private final JSpinner barWidthSpinner;
    private final JSpinner gapSpinner;
    private final JSpinner fontSizeSpinner;
    private final JComboBox textDirectionCombo;

    private final JCheckBox backgroundsCheckBox;
    private final JComboBox backgroundAttributeCombo;
    private final JSpinner backgroundAlphaSpinner;
    private final JLabel backgroundLabel1;
    private final JLabel backgroundLabel2;
    private final JCheckBox backgroundLabelsCheckBox;
    private final JSpinner backgroundLabelSizeSpinner;
    private final JCheckBox backgroundLabelBoldCheckBox;
    private final JCheckBox backgroundLabelItalicCheckBox;
    private final JCheckBox barItalicCheckBox;
    private final JCheckBox hideUnfittingCheckBox;
    private final JSpinner backgroundGapSpinner;
    private final JComboBox backgroundGradientCombo;
    private final JTextField notItalicWordsField;
    private final JLabel backgroundLabel5;
    private final JLabel backgroundLabel6;
    private final JLabel backgroundLabel4;
    private final JLabel backgroundLabel3;
}
