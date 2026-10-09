/*
 * RectilinearTreeLayoutController.java
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

package figtree.treeviewer.treelayouts;

import figtree.ui.ToolTips;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;

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
public class RectilinearTreeLayoutController extends AbstractController {

    private static final String RECTILINEAR_LAYOUT_KEY = "rectilinearLayout";

    private static final String ROOT_LENGTH_KEY = "rootLength";
    private static final String CURVATURE_KEY = "curvature";
    private static final String ALIGN_TIP_LABELS_KEY = "alignTipLabels";
    private static final String MIN_TIP_SPACING_KEY = "minTipSpacing";
    private static final String MAX_BRANCH_LENGTH_KEY = "maxBranchLength";
    private static final String SHOW_BREAK_FACTOR_KEY = "showBreakFactor";
    private static final String BREAK_FACTOR_SIZE_KEY = "breakFactorSize";

    public RectilinearTreeLayoutController(final RectilinearTreeLayout treeLayout, final TreeViewer treeViewer) {
        this.treeLayout = treeLayout;

        titleLabel = new JLabel("Rectangular Layout");

	    optionsPanel = new ControllerOptionsPanel(0, 0);

        final int sliderMax = 10000;
        rootLengthSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
        rootLengthSlider.setOpaque(false);
        rootLengthSlider.setValue((int) (treeLayout.getRootLengthProportion() * sliderMax));
        //rootLengthSlider.setMajorTickSpacing(rootLengthSlider.getMaximum() / 5);
//        rootLengthSlider.setPaintTicks(true);

        rootLengthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = rootLengthSlider.getValue();
                treeLayout.setRootLengthProportion(value / sliderMax);
            }
        });
        optionsPanel.addComponentWithLabel("Root Length:", rootLengthSlider, true);

        curvatureSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
        curvatureSlider.setOpaque(false);
        curvatureSlider.setValue((int) (treeLayout.getCurvature() * sliderMax));
        //curvatureSlider.setMajorTickSpacing(curvatureSlider.getMaximum() / 5);
  //      curvatureSlider.setPaintTicks(true);

        curvatureSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = curvatureSlider.getValue();
                treeLayout.setCurvature(value / sliderMax);
            }
        });
        optionsPanel.addComponentWithLabel("Curvature:", curvatureSlider, true);

        alignTipLabelsCheck = new JCheckBox("Align Tip Labels");
        alignTipLabelsCheck.setOpaque(false);

        alignTipLabelsCheck.setSelected(treeLayout.isAlignTipLabels());
        alignTipLabelsCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setAlignTipLabels(alignTipLabelsCheck.isSelected());
            }
        });
        optionsPanel.addSpanningComponent(alignTipLabelsCheck);

        // MyFigTree: automatic vertical expansion so tip labels do not overlap
        minTipSpacingSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 1.0));
        minTipSpacingSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = ((Number) minTipSpacingSpinner.getValue()).doubleValue();
                treeViewer.setMinTipSpacing(value);
            }
        });
        ToolTips.set("<html>Smallest vertical distance between neighbouring tip names, in points (0 = off).<br>" +
                "Set it once for the final font size of the figure: the tree stretches on its own<br>" +
                "until no two names overlap - on screen and in the exported PDF alike.<br>" +
                "It only keeps the tip names apart; support values on the inner nodes can<br>" +
                "still clash - for those use Avoid overlap in the Node Labels panel.</html>",
                optionsPanel.addComponentWithLabel("Min tip spacing:", minTipSpacingSpinner), minTipSpacingSpinner);

        // MyFigTree: cap very long branches and draw a "//" break mark on them
        maxBranchLengthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1.0E9, 0.01));
        maxBranchLengthSpinner.setEditor(new JSpinner.NumberEditor(maxBranchLengthSpinner, "0.####"));
        maxBranchLengthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = ((Number) maxBranchLengthSpinner.getValue()).doubleValue();
                treeLayout.setMaxBranchLength(value);
            }
        });
        ToolTips.set("<html>Every branch longer than this (in the units of the scale bar) is drawn cut<br>" +
                "to this length, with a // mark. Applies to all branches of the tree at once<br>" +
                "and does not say by how much each one was shortened - a cut branch no<br>" +
                "longer matches the scale bar. 0 = off. Type the number with a dot (0.2),<br>" +
                "a comma is not accepted. To shorten one chosen branch by an exact factor<br>" +
                "use Shorten selected branch below.</html>",
                optionsPanel.addComponentWithLabel("Shorten branches longer than:", maxBranchLengthSpinner), maxBranchLengthSpinner);

        // MyFigTree (Etap 7.10): one chosen branch drawn 2 or 3 times shorter, with the factor above its //
        JPanel shortenButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        shortenButtons.setOpaque(false);
        String[] names = {"2x", "3x", "Full"};
        final int[] factors = {2, 3, 1};
        for (int i = 0; i < names.length; i++) {
            final int factor = factors[i];
            JButton button = new JButton(names[i]);
            button.putClientProperty("JComponent.sizeVariant", "small");
            button.setToolTipText(factor > 1 ?
                    "<html>Draws the selected branch " + factor + " times shorter than it is,<br>with a // mark and " + factor + "x above it.</html>" :
                    "Draws the selected branch at its full length again.");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    int count = treeViewer.shortenSelectedBranches(factor);
                    if (count == 0) {
                        JOptionPane.showMessageDialog(optionsPanel,
                                "No branch is selected.\n\n" +
                                        "Click a branch in the tree first (Selection Mode: Node),\n" +
                                        "or the name of a tip to take the branch leading to it.",
                                "Shorten Selected Branch",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        fireControllerChanged();
                    }
                }
            });
            shortenButtons.add(button);
        }
        ToolTips.set("<html>Draws the one branch selected in the tree exactly 2 or 3 times shorter,<br>" +
                "with a // mark and 2x or 3x written above it; Full restores its length.<br>" +
                "Select a branch first (Selection Mode: Node) or click the name of a tip.<br>" +
                "The factor is saved with the branch in the .tree file.</html>",
                optionsPanel.addComponentWithLabel("Shorten selected branch:", shortenButtons), shortenButtons);

        breakLabelsCheck = new JCheckBox("Write 2x / 3x above //");
        breakLabelsCheck.setToolTipText("Shows or hides the 2x / 3x written above the // mark of a shortened branch.");
        breakLabelsCheck.setOpaque(false);
        breakLabelsCheck.setSelected(treeLayout.isBreakLabelsVisible());
        breakLabelsCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setBreakLabelsVisible(breakLabelsCheck.isSelected());
            }
        });
        optionsPanel.addSpanningComponent(breakLabelsCheck);

        breakLabelSizeSpinner = new JSpinner(new SpinnerNumberModel(treeLayout.getBreakLabelFontSize(), 1.0, 72.0, 1.0));
        breakLabelSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setBreakLabelFontSize(((Number) breakLabelSizeSpinner.getValue()).doubleValue());
            }
        });
        ToolTips.set("Font size of the 2x / 3x written above the // mark, in points.",
                optionsPanel.addComponentWithLabel("2x / 3x size:", breakLabelSizeSpinner), breakLabelSizeSpinner);
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

    public void setSettings(Map<String,Object> settings) {
        rootLengthSlider.setValue((Integer) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY));
        curvatureSlider.setValue((Integer) settings.get(RECTILINEAR_LAYOUT_KEY + "." + CURVATURE_KEY));
        alignTipLabelsCheck.setSelected((Boolean) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY));
        // MyFigTree key: may be absent in files saved by the original FigTree
        Object spacing = settings.get(RECTILINEAR_LAYOUT_KEY + "." + MIN_TIP_SPACING_KEY);
        if (spacing instanceof Number) {
            minTipSpacingSpinner.setValue(((Number) spacing).doubleValue());
        }
        Object maxLength = settings.get(RECTILINEAR_LAYOUT_KEY + "." + MAX_BRANCH_LENGTH_KEY);
        if (maxLength instanceof Number) {
            maxBranchLengthSpinner.setValue(((Number) maxLength).doubleValue());
        }
        Object showFactor = settings.get(RECTILINEAR_LAYOUT_KEY + "." + SHOW_BREAK_FACTOR_KEY);
        breakLabelsCheck.setSelected(!(showFactor instanceof Boolean) || (Boolean) showFactor);
        Object factorSize = settings.get(RECTILINEAR_LAYOUT_KEY + "." + BREAK_FACTOR_SIZE_KEY);
        if (factorSize instanceof Number) {
            breakLabelSizeSpinner.setValue(((Number) factorSize).doubleValue());
        }
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY, rootLengthSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + CURVATURE_KEY, curvatureSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + MIN_TIP_SPACING_KEY, ((Number) minTipSpacingSpinner.getValue()).doubleValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + MAX_BRANCH_LENGTH_KEY, ((Number) maxBranchLengthSpinner.getValue()).doubleValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + SHOW_BREAK_FACTOR_KEY, breakLabelsCheck.isSelected());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + BREAK_FACTOR_SIZE_KEY, ((Number) breakLabelSizeSpinner.getValue()).doubleValue());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JSlider rootLengthSlider;
    private final JSlider curvatureSlider;
    private final JCheckBox alignTipLabelsCheck;
    private final JSpinner minTipSpacingSpinner;
    private final JSpinner maxBranchLengthSpinner;
    private final JCheckBox breakLabelsCheck;
    private final JSpinner breakLabelSizeSpinner;

    private final RectilinearTreeLayout treeLayout;

}
