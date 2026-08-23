/*
 * GroupBarController.java
 *
 * MyFigTree addition (Etap 4.3 / 4.4): the "Group Bars" control panel.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.decorators.ColourDecorator;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;
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

        attributeCombo = new JComboBox();
        new AttributeComboHelper(attributeCombo, treeViewer, LabelPainter.PainterIntent.TIP).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateBarAttribute();
            }
        });

        backgroundAttributeCombo = new JComboBox();
        new AttributeComboHelper(backgroundAttributeCombo, treeViewer, LabelPainter.PainterIntent.TIP).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateBackgroundAttribute();
            }
        });

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

        final JLabel label1 = optionsPanel.addComponentWithLabel("Attribute:", attributeCombo);
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

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                painter.setBarsVisible(titleCheckBox.isSelected());
            }
        });
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
