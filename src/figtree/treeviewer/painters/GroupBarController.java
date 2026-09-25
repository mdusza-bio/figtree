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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

public class GroupBarController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "groupBars";

    public static final String ATTRIBUTE_KEY = "attribute";
    public static final String BAR_WIDTH_KEY = "barWidth";
    public static final String GAP_KEY = "gap";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String BACKGROUNDS_KEY = "backgrounds";
    public static final String BACKGROUND_ATTRIBUTE_KEY = "backgroundAttribute";
    public static final String BACKGROUND_ALPHA_KEY = "backgroundAlpha";

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
        final JLabel label2 = optionsPanel.addComponentWithLabel("Bar width:", barWidthSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Gap from labels:", gapSpinner);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Font size:", fontSizeSpinner);
        optionsPanel.addSeparator();
        optionsPanel.addSpanningComponent(backgroundsCheckBox);
        backgroundLabel1 = optionsPanel.addComponentWithLabel("Attribute:", backgroundAttributeCombo);
        backgroundLabel2 = optionsPanel.addComponentWithLabel("Opacity (%):", backgroundAlphaSpinner);

        addComponent(label1);
        addComponent(attributeCombo);
        addComponent(label2);
        addComponent(barWidthSpinner);
        addComponent(label3);
        addComponent(gapSpinner);
        addComponent(label4);
        addComponent(fontSizeSpinner);
        enableComponents(titleCheckBox.isSelected());
        enableBackgroundComponents();
        updateHint();

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
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

        JComboBox nameCombo = new JComboBox();
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

        JTextField valueField = new JTextField(lastAssignedValue, 16);

        OptionsPanel dialogPanel = new OptionsPanel(6, 6);
        dialogPanel.addComponentWithLabel("Attribute:", nameCombo);
        dialogPanel.addComponentWithLabel("Value:", valueField);

        int result = JOptionPane.showConfirmDialog(optionsPanel, dialogPanel,
                "Assign to Selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Object nameItem = nameCombo.getSelectedItem();
        String name = nameItem == null ? "" : nameItem.toString().trim();
        String value = valueField.getText().trim();

        if (name.length() == 0 || value.length() == 0) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "Both an attribute name (such as family) and a value\n" +
                            "(such as Trichiaceae) are needed.",
                    "Assign to Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int count = ((ExtendedTreeViewer) treeViewer).annotateSelectedTaxa(name, value);

        if (count == 0) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "Nothing is selected in the tree.\n\n" +
                            "Select a clade (or some tip labels) first - every tip of the\n" +
                            "selection then gets " + name + " = " + value + ".",
                    "Assign to Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        lastAssignedName = name;
        lastAssignedValue = value;

        attributeCombo.setSelectedItem(name);
        if (!titleCheckBox.isSelected()) {
            titleCheckBox.setSelected(true);
        }
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
        backgroundsCheckBox.setSelected(getBool(settings, BACKGROUNDS_KEY, false));
        String bgAttribute = getString(settings, BACKGROUND_ATTRIBUTE_KEY);
        if (bgAttribute != null) backgroundAttributeCombo.setSelectedItem(bgAttribute);
        backgroundAlphaSpinner.setValue((int) Math.round(getDouble(settings, BACKGROUND_ALPHA_KEY, 25.0)));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ATTRIBUTE_KEY, attributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + BAR_WIDTH_KEY, barWidthSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + GAP_KEY, gapSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BACKGROUNDS_KEY, backgroundsCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_ATTRIBUTE_KEY, backgroundAttributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_ALPHA_KEY, backgroundAlphaSpinner.getValue());
    }

    public String getTitle() {
        return "Group Bars";
    }

    private final GroupBarPainter painter;
    private final AttributeColourController colourController;

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JButton assignButton;
    private final JLabel hintLabel;
    private String lastAssignedName = "group";
    private String lastAssignedValue = "";

    private final JComboBox attributeCombo;
    private final JSpinner barWidthSpinner;
    private final JSpinner gapSpinner;
    private final JSpinner fontSizeSpinner;

    private final JCheckBox backgroundsCheckBox;
    private final JComboBox backgroundAttributeCombo;
    private final JSpinner backgroundAlphaSpinner;
    private final JLabel backgroundLabel1;
    private final JLabel backgroundLabel2;
}
