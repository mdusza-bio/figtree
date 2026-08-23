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
        minTipSpacingSpinner.setToolTipText("Minimum vertical distance between tips in points (0 = off)");
        minTipSpacingSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = ((Number) minTipSpacingSpinner.getValue()).doubleValue();
                treeViewer.setMinTipSpacing(value);
            }
        });
        optionsPanel.addComponentWithLabel("Min tip spacing:", minTipSpacingSpinner);

        // MyFigTree: cap very long branches and draw a "//" break mark on them
        maxBranchLengthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1.0E9, 0.01));
        maxBranchLengthSpinner.setToolTipText("Branches longer than this (tree-length units) are drawn shortened with a // mark (0 = off)");
        maxBranchLengthSpinner.setEditor(new JSpinner.NumberEditor(maxBranchLengthSpinner, "0.####"));
        maxBranchLengthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = ((Number) maxBranchLengthSpinner.getValue()).doubleValue();
                treeLayout.setMaxBranchLength(value);
            }
        });
        optionsPanel.addComponentWithLabel("Shorten branches longer than:", maxBranchLengthSpinner);
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
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY, rootLengthSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + CURVATURE_KEY, curvatureSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + MIN_TIP_SPACING_KEY, ((Number) minTipSpacingSpinner.getValue()).doubleValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + MAX_BRANCH_LENGTH_KEY, ((Number) maxBranchLengthSpinner.getValue()).doubleValue());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JSlider rootLengthSlider;
    private final JSlider curvatureSlider;
    private final JCheckBox alignTipLabelsCheck;
    private final JSpinner minTipSpacingSpinner;
    private final JSpinner maxBranchLengthSpinner;

    private final RectilinearTreeLayout treeLayout;

}
