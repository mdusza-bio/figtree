/*
 * TreeAppearanceController.java
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

package figtree.treeviewer;

import figtree.treeviewer.painters.AttributeComboHelper;
import figtree.treeviewer.painters.LabelStyle;
import figtree.treeviewer.painters.AttributeComboHelperListener;
import figtree.treeviewer.painters.NodeShapePainter;
import jam.controlpalettes.ControlPalette;
import jam.controlpalettes.ControllerListener;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.Preferences;

import figtree.treeviewer.decorators.*;
import jebl.evolution.trees.RootedTree;

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
public class TreeAppearanceController extends AbstractController {

    private static final String CONTROLLER_TITLE = "Appearance";

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "appearance";

    public static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    public static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    public static final String SELECTION_COLOUR_KEY = "selectionColour";
    public static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
    public static final String BRANCH_COLOR_GRADIENT_KEY = "branchColorGradient";
    public static final String HILIGHTING_GRADIENT_KEY = "hilightingGradient";
    public static final String BACKGROUND_COLOR_ATTRIBUTE_KEY = "backgroundColorAttribute";
    public static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";
    public static final String BRANCH_MIN_LINE_WIDTH_KEY = "branchMinLineWidth";
    public static final String BRANCH_WIDTH_ATTRIBUTE_KEY = "branchWidthAttribute";

    // The defaults if there is nothing in the preferences
    public static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    public static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    public static Color DEFAULT_SELECTION_COLOUR = new Color(45, 54, 128);
    public static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public static final String FIXED = "Fixed";

    public TreeAppearanceController(final TreeViewer treeViewer, final JFrame frame,
                                    final AttributeColourController colourController) {
        this.treeViewer = treeViewer;
        this.colourController = colourController;

        userBranchColourDecorator = new AttributableDecorator();
        userBranchColourDecorator.setPaintAttributeName("!color");
        userBranchColourDecorator.setStrokeAttributeName("!stroke");
        treeViewer.setBranchDecorator(userBranchColourDecorator, false);

        int foregroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
        int backgroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
        int selectionRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
        float branchLineWidth = TreeAppearanceController.PREFS.getFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

        treeViewer.setForeground(new Color(foregroundRGB));
        treeViewer.setBackground(new Color(backgroundRGB));
        treeViewer.setSelectionColor(new Color(selectionRGB));
        treeViewer.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel(CONTROLLER_TITLE);

        optionsPanel = new ControllerOptionsPanel(2, 0);

        branchWidthAttributeCombo = new JComboBox(new String[] { "No attributes" });
        branchColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
        backgroundColourAttributeCombo = new JComboBox(new String[] { "No attributes" });

//        setupAttributes(treeViewer.getTrees());

//        branchColourSettings.autoRange  = true;
//        branchColourSettings.fromValue = 0.0;
//        branchColourSettings.toValue = 1.0;
//        branchColourSettings.fromColour = new Color(0, 16, 192);
//        branchColourSettings.toColour = new Color(192, 16, 0);
//        branchColourSettings.middleColour = new Color(0, 0, 0);

        branchColourIsGradient = TreeAppearanceController.PREFS.getBoolean(CONTROLLER_KEY + "." + BRANCH_COLOR_GRADIENT_KEY, false);

        final JButton setupColourButton = new JButton("Colours");

        colourController.setupControls(branchColourAttributeCombo, setupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupBranchDecorators();
            }
        });

        branchColourGradientCheck = new JCheckBox("Gradient");
        branchColourGradientCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                branchColourIsGradient = branchColourGradientCheck.isSelected();
                setupBranchDecorators();
            }
        });
        optionsPanel.addComponentWithLabel("Colour by:", branchColourAttributeCombo);
        final JLabel setupColourButtonLabel = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);
        optionsPanel.addComponent(branchColourGradientCheck);
        optionsPanel.addSeparator();

        boolean hilightingGradient = TreeAppearanceController.PREFS.getBoolean(CONTROLLER_KEY + "." + HILIGHTING_GRADIENT_KEY, false);

        hilightingGradientCheck = new JCheckBox("Hilight with gradient");
        hilightingGradientCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeViewer.setHilightingGradient(hilightingGradientCheck.isSelected());
            }
        });
        optionsPanel.addComponent(hilightingGradientCheck);
        hilightingGradientCheck.setSelected(hilightingGradient);
        optionsPanel.addSeparator();

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);
        optionsPanel.addComponentWithLabel("Width by:", branchWidthAttributeCombo);

        branchMinLineWidthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 48.0, 1.0));
        final JLabel label = optionsPanel.addComponentWithLabel("Min Weight:", branchMinLineWidthSpinner);

        optionsPanel.addSeparator();

//        backgroundColourSettings.autoRange  = true;
//        backgroundColourSettings.fromValue = 0.0;
//        backgroundColourSettings.toValue = 1.0;
//        backgroundColourSettings.fromColour = new Color(255, 255, 255);
//        backgroundColourSettings.toColour = new Color(192, 16, 0);
//        backgroundColourSettings.middleColour = null;

        final JButton bgSetupColourButton = new JButton("Colours");
        colourController.setupControls(backgroundColourAttributeCombo, bgSetupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupBranchDecorators();
            }
        });

        optionsPanel.addComponentWithLabel("Background:", backgroundColourAttributeCombo);
        final JLabel bgSetupColourButtonLabel = optionsPanel.addComponentWithLabel("Setup:", bgSetupColourButton);

        new AttributeComboHelper(branchColourAttributeCombo, treeViewer, "User selection", false, true).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = branchColourAttributeCombo.getSelectedIndex() != 0;
                setupColourButtonLabel.setEnabled(isSelected);
                setupColourButton.setEnabled(isSelected);

                setupBranchDecorators();
            }
        });

        new AttributeComboHelper(backgroundColourAttributeCombo, treeViewer, "Default").addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = backgroundColourAttributeCombo.getSelectedIndex() != 0;
                bgSetupColourButtonLabel.setEnabled(isSelected);
                bgSetupColourButton.setEnabled(isSelected);

                setupBranchDecorators();
            }
        });

        new AttributeComboHelper(branchWidthAttributeCombo, treeViewer, FIXED, true, false).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = branchColourAttributeCombo.getSelectedIndex() != 0;
                label.setEnabled(isSelected);
                branchMinLineWidthSpinner.setEnabled(isSelected);
                setupBranchDecorators();
            }
        });

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treeViewer.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                setupBranchDecorators();
            }
        });
        branchMinLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setupBranchDecorators();
            }
        });

        // MyFigTree: one-click "publication style" preset (needs the control palette,
        // see setControlPalette(); the button stays disabled until it is provided)
        optionsPanel.addSeparator();
        publicationPresetButton = new JButton("Publication preset");
        publicationPresetButton.setToolTipText("Apply publication-style settings: serif 10pt tip labels with italic genus/species, " +
                "underscores as spaces, aligned tips, 1pt branches, support values >= 50 below branch, dots on nodes >= 95, white background");
        publicationPresetButton.setEnabled(false);
        publicationPresetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                togglePublicationPreset();
            }
        });
        optionsPanel.addSpanningComponent(publicationPresetButton);

        // MyFigTree: the "undo" copy only makes sense for the tree it was taken
        // from, so drop it (and revert the button) as soon as a different tree
        // is loaded.
        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                beforePreset = null;
                publicationPresetButton.setText("Publication preset");
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
    }

    /**
     * Gives this controller access to the whole palette so the preset button can
     * change settings of all the other panels (via their setSettings()).
     */
    public void setControlPalette(ControlPalette controlPalette) {
        this.controlPalette = controlPalette;
        publicationPresetButton.setEnabled(controlPalette != null);
    }

    /** Name of the support-value attribute (internal node "label"), or null if the tree has none. */
    private String findSupportAttribute() {
        java.util.List<Tree> trees = treeViewer.getTrees();
        if (trees == null) {
            return null;
        }
        for (Tree tree : trees) {
            for (Node node : tree.getInternalNodes()) {
                if (node.getAttribute("label") != null) {
                    return "label";
                }
            }
        }
        return null;
    }

    /**
     * MyFigTree: the preset button doubles as its own undo. First click saves a
     * full copy of every panel's settings and applies the preset; second click
     * restores that copy. The copy is discarded (see the TreeViewerListener
     * above) as soon as it is used or a different tree is loaded.
     */
    private void togglePublicationPreset() {
        if (controlPalette == null) {
            return;
        }
        if (beforePreset != null) {
            controlPalette.setSettings(beforePreset);
            beforePreset = null;
            publicationPresetButton.setText("Publication preset");
            return;
        }

        // Start from the current values of every panel so all keys are present,
        // then override the ones that make up the "publication style". This
        // full map is also what "Undo preset" restores afterwards.
        Map<String, Object> settings = new HashMap<String, Object>();
        controlPalette.getSettings(settings);
        beforePreset = new HashMap<String, Object>(settings);

        String supportAttribute = findSupportAttribute();

        // Appearance
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, Color.WHITE);
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, Color.BLACK);
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, 1.0);

        // Tip labels: serif 10pt, italic name up to the collection number, underscores -> spaces
        settings.put("tipLabels.isShown", Boolean.TRUE);
        settings.put("tipLabels.fontName", "Times New Roman");
        settings.put("tipLabels.fontSize", 10);
        settings.put("tipLabels.fontStyle", Font.PLAIN);
        settings.put("tipLabels.italicMode", LabelStyle.ItalicMode.UNTIL_NUMBER.name());
        settings.put("tipLabels.nonItalicWords", LabelStyle.DEFAULT_NON_ITALIC_WORDS);
        settings.put("tipLabels.addRankDots", Boolean.TRUE);
        settings.put("tipLabels.upperCaseIsNumber", Boolean.TRUE);
        settings.put("tipLabels.formatCollectionNumber", Boolean.TRUE);
        settings.put("tipLabels.dotCodes", LabelStyle.DEFAULT_DOT_CODES);
        settings.put("tipLabels.italicParts", 2);
        settings.put("tipLabels.replaceUnderscores", Boolean.TRUE);

        // Node labels: support values >= 50, below the branch, 8pt
        if (supportAttribute != null) {
            settings.put("nodeLabels.isShown", Boolean.TRUE);
            settings.put("nodeLabels.displayAttribute", supportAttribute);
        }
        settings.put("nodeLabels.fontName", "Times New Roman");
        settings.put("nodeLabels.fontSize", 8);
        settings.put("nodeLabels.fontStyle", Font.PLAIN);
        settings.put("nodeLabels.showThreshold", "50");
        settings.put("nodeLabels.position", "BELOW_BRANCH");

        // Node shapes: black dots on nodes with support >= 95 (off when there is no support value)
        settings.put("nodeShapeInternal.isShown", supportAttribute != null);
        settings.put("nodeShapeInternal.shapeType", "CIRCLE");
        settings.put("nodeShapeInternal.size", 4.0);
        settings.put("nodeShapeInternal.thresholdAttribute", supportAttribute == null ? "None" : supportAttribute);
        settings.put("nodeShapeInternal.showThreshold", "95");
        settings.put("nodeShapeExternal.isShown", Boolean.FALSE);

        // Layout: rectangular with aligned tip labels
        settings.put("layout.layoutType", "RECTILINEAR");
        settings.put("rectilinearLayout.alignTipLabels", Boolean.TRUE);

        controlPalette.setSettings(settings);
        publicationPresetButton.setText("Undo preset");
    }

    private void setupBranchDecorators() {

        Decorator colourDecorator = colourController.getColourDecorator(branchColourAttributeCombo, userBranchColourDecorator);
//        DiversityContinuousColourDecorator colourDecorator = new DiversityContinuousColourDecorator();
//        colourDecorator.setTree((RootedTree)treeViewer.getCurrentTree());

//                if (colourDecorator == null) {
//                    if (attribute.endsWith("*")) {
//                        // This is a branch colouring (i.e., the colour can change
//                        // along the length of the branch...
//                        treeViewer.setBranchColouringDecorator(
//                                attribute.substring(0, attribute.length() - 2),
//                                new DiscreteColourDecorator());
//                        return;

//        if (colourDecorator != null && colourDecorator.isGradient()) {
//            // At present using a gradient precludes the use of the compoundDecorator
//            // and thus the branch width..
//            treeViewer.setBranchDecorator(colourDecorator);
//            return;
//        }

        CompoundDecorator compoundDecorator = new CompoundDecorator();

        if (colourDecorator != null) {
            treeViewer.setBranchColouringDecorator(null, null);
            compoundDecorator.addDecorator(colourDecorator);
        }

        if (branchWidthAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) branchWidthAttributeCombo.getSelectedItem();
            ContinuousScale widthScale = new ContinuousScale(attribute, treeViewer.getTrees().get(0).getNodes());

            double fromWidth = (Double) branchMinLineWidthSpinner.getValue();
            double toWidth = (Double)branchLineWidthSpinner.getValue() + fromWidth;
            compoundDecorator.addDecorator(new ContinuousStrokeDecorator(
                    widthScale, (float)fromWidth, (float)toWidth)
            );
        }

        treeViewer.setBranchDecorator(compoundDecorator, branchColourIsGradient);

        Decorator backgroundDecorator = null;
        if (backgroundColourAttributeCombo.getSelectedIndex() > 1) {
            backgroundDecorator = colourController.getColourDecorator(backgroundColourAttributeCombo, null);
        }
        treeViewer.setNodeBackgroundDecorator(backgroundDecorator);

    }


    public JComponent getTitleComponent() {
        return titleLabel;
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

    public void setSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        treeViewer.setForeground((Color)settings.get(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY));
        treeViewer.setBackground((Color)settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY));
        treeViewer.setSelectionColor((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
        branchColourGradientCheck.setSelected((Boolean)settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_GRADIENT_KEY));
        hilightingGradientCheck.setSelected((Boolean)settings.get(CONTROLLER_KEY+"."+HILIGHTING_GRADIENT_KEY));
        backgroundColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOR_ATTRIBUTE_KEY));
        branchLineWidthSpinner.setValue((Double) settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
        branchWidthAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_WIDTH_ATTRIBUTE_KEY));
        branchMinLineWidthSpinner.setValue((Double) settings.get(CONTROLLER_KEY + "." + BRANCH_MIN_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_GRADIENT_KEY, branchColourGradientCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + HILIGHTING_GRADIENT_KEY, hilightingGradientCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOR_ATTRIBUTE_KEY, backgroundColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BRANCH_WIDTH_ATTRIBUTE_KEY, branchWidthAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_MIN_LINE_WIDTH_KEY, branchMinLineWidthSpinner.getValue());
    }

    private final AttributeColourController colourController;

    private final AttributableDecorator userBranchColourDecorator;

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JButton publicationPresetButton;
    private ControlPalette controlPalette = null;
    /** Full settings snapshot from before the preset was applied, or null if no undo is pending. */
    private Map<String, Object> beforePreset = null;

    private final JComboBox branchColourAttributeCombo;
    private final JCheckBox branchColourGradientCheck;
    private final JCheckBox hilightingGradientCheck;
    private final JComboBox backgroundColourAttributeCombo;
    private final JSpinner branchLineWidthSpinner;
    private final JSpinner branchMinLineWidthSpinner;

    private final JComboBox branchWidthAttributeCombo;

    private final TreeViewer treeViewer;

    private boolean branchColourIsGradient = false;


}
