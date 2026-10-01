/*
 * SupportDotController.java
 *
 * MyFigTree addition (Etap 6.20): the "Support Dots" control panel.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class SupportDotController extends AbstractController {

    public static final String CONTROLLER_KEY = "supportDots";

    public static final String ATTRIBUTE_KEY = "attribute";
    public static final String THRESHOLD_KEY = "threshold";
    public static final String ATTRIBUTE2_KEY = "attribute2";
    public static final String THRESHOLD2_KEY = "threshold2";
    public static final String POSITION_KEY = "position";
    public static final String SIZE_KEY = "size";
    public static final String HIDE_LABELS_KEY = "hideLabels";

    public static final String NONE = "None";

    public SupportDotController(final SupportDotPainter painter, final TreeViewer treeViewer) {
        this.painter = painter;

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(painter.isVisible());
        titleCheckBox.setToolTipText("<html>A black dot on every branch that reached the maximum support -<br>" +
                "in one analysis, or in both when a second one is chosen.</html>");

        attributeCombo = new JComboBox();
        AttributeComboHelper.attributesOnly(attributeCombo, treeViewer, LabelPainter.PainterIntent.NODE).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateSupport();
            }
        });

        attribute2Combo = new JComboBox();
        AttributeComboHelper.attributesOnly(attribute2Combo, treeViewer, NONE, LabelPainter.PainterIntent.NODE).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                updateSupport();
            }
        });

        // registered after the combo helpers: the dots follow whatever the lists show once refilled
        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                updateSupport();
            }

            public void treeSettingsChanged() {
            }
        });

        // 100 for bootstrap, 1 for posterior probability; the arrows step by 1 and 0.01
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 1000.0, 1.0));
        thresholdSpinner.setToolTipText("<html>Values at or above this get a dot - 100 for bootstrap,<br>" +
                "1 for posterior probability. Type decimals with a dot (0.95).</html>");
        threshold2Spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.01));
        threshold2Spinner.setToolTipText(thresholdSpinner.getToolTipText());
        ChangeListener thresholdListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateSupport();
            }
        };
        thresholdSpinner.addChangeListener(thresholdListener);
        threshold2Spinner.addChangeListener(thresholdListener);

        positionCombo = new JComboBox(SupportDotPainter.Position.values());
        positionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                painter.setPosition((SupportDotPainter.Position) positionCombo.getSelectedItem());
            }
        });

        sizeSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
        sizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                painter.setDotSize(((Number) sizeSpinner.getValue()).doubleValue());
            }
        });

        hideLabelsCheck = new JCheckBox("Hide values at dots");
        hideLabelsCheck.setToolTipText("<html>Node and branch labels leave out the values of branches<br>" +
                "with a dot, so a fully supported branch shows only the dot.</html>");
        hideLabelsCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                painter.setHidingLabels(hideLabelsCheck.isSelected());
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Support:", attributeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Dot if >=:", thresholdSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("and support:", attribute2Combo);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Dot if >=:", threshold2Spinner);
        final JLabel label5 = optionsPanel.addComponentWithLabel("Position:", positionCombo);
        final JLabel label6 = optionsPanel.addComponentWithLabel("Dot size:", sizeSpinner);
        optionsPanel.addComponent(hideLabelsCheck, true);

        addComponent(label1);
        addComponent(attributeCombo);
        addComponent(label2);
        addComponent(thresholdSpinner);
        addComponent(label3);
        addComponent(attribute2Combo);
        addComponent(label4);
        addComponent(threshold2Spinner);
        addComponent(label5);
        addComponent(positionCombo);
        addComponent(label6);
        addComponent(sizeSpinner);
        addComponent(hideLabelsCheck);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                painter.setVisible(titleCheckBox.isSelected());
                updateSupport();
            }
        });
    }

    private void updateSupport() {
        Object attribute = attributeCombo.getSelectedItem();
        painter.setFirstSupport(attribute == null ? null : attribute.toString(),
                ((Number) thresholdSpinner.getValue()).doubleValue());

        Object attribute2 = attribute2Combo.getSelectedItem();
        boolean none = attribute2 == null || attribute2.toString().equals(NONE);
        painter.setSecondSupport(none ? null : attribute2.toString(), ((Number) threshold2Spinner.getValue()).doubleValue());
        threshold2Spinner.setEnabled(titleCheckBox.isSelected() && !none);
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

    private static void setThreshold(JSpinner spinner, Object value) {
        try {
            if (value instanceof Number) {
                spinner.setValue(((Number) value).doubleValue());
            } else if (value != null) {
                spinner.setValue(Double.parseDouble(value.toString().trim()));
            }
        } catch (RuntimeException e) {
            // not a number or out of range - keep the current value
        }
    }

    private static Object get(Map<String, Object> settings, String key) {
        return settings.get(CONTROLLER_KEY + "." + key);
    }

    public void setSettings(Map<String, Object> settings) {
        // trees saved before this panel existed have none of these keys - keep the defaults then
        Object shown = get(settings, IS_SHOWN);
        titleCheckBox.setSelected(shown instanceof Boolean && (Boolean) shown);
        Object attribute = get(settings, ATTRIBUTE_KEY);
        if (attribute != null) attributeCombo.setSelectedItem(attribute.toString());
        Object threshold = get(settings, THRESHOLD_KEY);
        setThreshold(thresholdSpinner, threshold);
        Object attribute2 = get(settings, ATTRIBUTE2_KEY);
        if (attribute2 != null) attribute2Combo.setSelectedItem(attribute2.toString());
        Object threshold2 = get(settings, THRESHOLD2_KEY);
        setThreshold(threshold2Spinner, threshold2);
        Object position = get(settings, POSITION_KEY);
        if (position != null) {
            try {
                positionCombo.setSelectedItem(SupportDotPainter.Position.valueOf(position.toString()));
            } catch (IllegalArgumentException iae) {
                // unknown value - keep the current one
            }
        }
        Object size = get(settings, SIZE_KEY);
        if (size instanceof Number) sizeSpinner.setValue(((Number) size).doubleValue());
        Object hide = get(settings, HIDE_LABELS_KEY);
        hideLabelsCheck.setSelected(hide instanceof Boolean && (Boolean) hide);
        painter.setHidingLabels(hideLabelsCheck.isSelected());
        updateSupport();
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ATTRIBUTE_KEY, attributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + THRESHOLD_KEY, thresholdSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + ATTRIBUTE2_KEY, attribute2Combo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + THRESHOLD2_KEY, threshold2Spinner.getValue());
        settings.put(CONTROLLER_KEY + "." + POSITION_KEY, ((SupportDotPainter.Position) positionCombo.getSelectedItem()).name());
        settings.put(CONTROLLER_KEY + "." + SIZE_KEY, sizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + HIDE_LABELS_KEY, hideLabelsCheck.isSelected());
    }

    public String getTitle() {
        return "Support Dots";
    }

    private final SupportDotPainter painter;

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox attributeCombo;
    private final JSpinner thresholdSpinner;
    private final JComboBox attribute2Combo;
    private final JSpinner threshold2Spinner;
    private final JComboBox positionCombo;
    private final JSpinner sizeSpinner;
    private final JCheckBox hideLabelsCheck;
}
