/*
 * FigTreeFrame.java
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

package figtree.application;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;
import jam.framework.DocumentFrame;
import jam.panels.*;
import jam.toolbar.*;
import jam.util.IconUtils;
import figtree.application.menus.TreeMenuHandler;
import figtree.application.menus.FigTreeFileMenuHandler;
import figtree.treeviewer.*;
import figtree.treeviewer.TreeSelectionListener;
import figtree.treeviewer.annotations.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Primary window for FigTree. The window contains a toolbar, a control panel and a panel displaying the
 * tree. It responds to menu commands and hot keys specific to the focused window.
 * @author Andrew Rambaut
 * @version $Id: FigTreeApplication.java 232 2014-03-02 15:47:10Z rambaut $
 *
 * $HeadURL: https://figtree.googlecode.com/svn/trunk/src/figtree/application/FigTreeApplication.java $
 *
 * $LastChangedBy: rambaut $
 * $LastChangedDate: 2014-03-02 15:47:10 +0000 (Sun, 02 Mar 2014) $
 * $LastChangedRevision: 232 $
 */
public class FigTreeFrame extends DocumentFrame implements FigTreeFileMenuHandler, TreeMenuHandler {

    private final static boolean FAST_MODE = false;
    private static final boolean WARN_ANNOTATE_MULTIPLE = false;

    private final ExtendedTreeViewer treeViewer;
    private final ControlPalette controlPalette;
    private final FigTreePanel figTreePanel;

    private StatusBar statusBar;

    private SearchPanel filterPanel;
    private JPopupMenu filterPopup;

    public FigTreeFrame(String title, boolean fastMode) {
        super();

        setTitle(title);

        setImportAction(importAction);
        //       setImportAction(importCharactersAction);
        setExportAction(exportTreesAction);

        treeViewer = new ExtendedTreeViewer(this, fastMode);
        controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

        figTreePanel = new FigTreePanel(this, treeViewer, controlPalette);

        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                treeViewer.requestFocusInWindow();
            }
        });
    }

    public void initializeComponents() {

        setSize(new java.awt.Dimension(1024, 768));

        Toolbar toolBar = new Toolbar();
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.darkGray));

        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        Icon rerootToolIcon = IconUtils.getIcon(this.getClass(), "images/rerootTool.png");
        Icon rotateToolIcon = IconUtils.getIcon(this.getClass(), "images/rotateTool.png");
        Icon cartoonNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/cartoonNodeTool.png");
        Icon collapseNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/collapseNodeTool.png");
        Icon hilightToolIcon = IconUtils.getIcon(this.getClass(), "images/hilightTool.png");
        Icon annotationToolIcon = IconUtils.getIcon(this.getClass(), "images/annotationTool.png");
        Icon findToolIcon = IconUtils.getIcon(this.getClass(), "images/findTool.png");
        Icon infoToolIcon = IconUtils.getIcon(this.getClass(), "images/infoTool.png");
        Icon statisticsToolIcon = IconUtils.getIcon(this.getClass(), "images/statisticsTool.png");
        Icon settingsToolIcon = IconUtils.getIcon(this.getClass(), "images/projectTool.png");
        Icon colourToolIcon = IconUtils.getIcon(this.getClass(), "images/coloursTool.png");

        Icon nextIcon = IconUtils.getIcon(this.getClass(), "images/next.png");
        Icon prevIcon = IconUtils.getIcon(this.getClass(), "images/prev.png");

        final ToolbarAction cartoonToolbarAction = new ToolbarAction("Cartoon", CARTOON_NODE, cartoonNodeToolIcon) {
            public void actionPerformed(ActionEvent e){
                cartoonAction.actionPerformed(e);
            }
        };

        ToolbarButton cartoonToolButton = new ToolbarButton(cartoonToolbarAction, true);
        cartoonToolButton.setFocusable(false);
        toolBar.addComponent(cartoonToolButton);

        final ToolbarAction collapseToolbarAction = new ToolbarAction("Collapse", COLLAPSE_NODE, collapseNodeToolIcon) {
            public void actionPerformed(ActionEvent e){
                collapseAction.actionPerformed(e);
            }
        };
        ToolbarButton collapseToolButton = new ToolbarButton(collapseToolbarAction, true);
        collapseToolButton.setFocusable(false);
        toolBar.addComponent(collapseToolButton);

        final ToolbarAction rerootToolbarAction = new ToolbarAction("Reroot", ROOT_ON_BRANCH, rerootToolIcon) {
            public void actionPerformed(ActionEvent e){
                rerootAction.actionPerformed(e);
            }
        };
        ToolbarButton rerootToolButton = new ToolbarButton(rerootToolbarAction, true);
        rerootToolButton.setFocusable(false);
        toolBar.addComponent(rerootToolButton);

        final ToolbarAction rotateToolbarAction = new ToolbarAction("Rotate", ROTATE_NODE, rotateToolIcon) {
            public void actionPerformed(ActionEvent e){
                rotateAction.actionPerformed(e);
            }
        };
        ToolbarButton rotateToolButton = new ToolbarButton(rotateToolbarAction, true);
        rotateToolButton.setFocusable(false);
        toolBar.addComponent(rotateToolButton);

        final ToolbarAction annotateToolbarAction =  new ToolbarAction("Annotate", ANNOTATE, annotationToolIcon) {
            public void actionPerformed(ActionEvent e){
                annotateAction.actionPerformed(e);
            }
        };
        ToolbarButton annotationToolButton = new ToolbarButton(annotateToolbarAction, true);
        annotationToolButton.setFocusable(false);
        toolBar.addComponent(annotationToolButton);

        final ToolbarAction colourToolbarAction = new ToolbarAction("Colour", COLOUR, colourToolIcon) {
            public void actionPerformed(ActionEvent e){
                colourAction.actionPerformed(e);
            }
        };
        ToolbarButton colourToolButton = new ToolbarButton(colourToolbarAction, true);
        colourToolButton.setFocusable(false);
        toolBar.addComponent(colourToolButton);

        final ToolbarAction hilightToolbarAction = new ToolbarAction("Hilight", HILIGHT, hilightToolIcon) {
            public void actionPerformed(ActionEvent e){
                hilightAction.actionPerformed(e);
            }
        };
        ToolbarButton hilightToolButton = new ToolbarButton(hilightToolbarAction, true);
        hilightToolButton.setFocusable(false);
        toolBar.addComponent(hilightToolButton);

        toolBar.addSeparator();

        final ToolbarAction findToolbarAction = new ToolbarAction("Find", "Find Taxa...", findToolIcon) {
            public void actionPerformed(ActionEvent e){
                findAction.actionPerformed(e);
            }
        };
        JButton findToolButton = new ToolbarButton(findToolbarAction);
        findToolButton.setFocusable(false);
        toolBar.addComponent(findToolButton);
        findToolButton.setEnabled(true);

//		final ToolbarAction infoToolbarAction = new ToolbarAction("Get Info", "Get Info...", infoToolIcon) {
//			public void actionPerformed(ActionEvent e){
//				getInfoAction.actionPerformed(e);
//			}
//		};
//		JButton infoToolButton = new ToolbarButton(infoToolbarAction);
//		toolBar.addComponent(infoToolButton);

//        JButton settingsToolButton = new ToolbarButton(
//                new ToolbarAction("Statistics", "Statistics...", statisticsToolIcon) {
//                    public void actionPerformed(ActionEvent e){
//                        treeViewer.showStatistics();
//                    }
//                });
//        settingsToolButton.putClientProperty("Quaqua.Button.style", "toolBarRollover");
//        toolBar.addComponent(settingsToolButton);
//        settingsToolButton.setEnabled(false);

        toolBar.addSeparator();

        Box box1 = Box.createHorizontalBox();
        final JToggleButton toggle1 = new JToggleButton("Clade");
        toggle1.setFocusable(false);
        toggle1.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle1.putClientProperty("JButton.segmentPosition", "first");
        toggle1.putClientProperty( "Quaqua.Button.style", "toggleWest");

        final JToggleButton toggle2 = new JToggleButton("Node");
        toggle2.setFocusable(false);
        toggle2.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle2.putClientProperty("JButton.segmentPosition", "middle");
        toggle2.putClientProperty( "Quaqua.Button.style", "toggleCenter");

        final JToggleButton toggle3 = new JToggleButton("Tips");
        toggle3.setFocusable(false);
        toggle3.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle3.putClientProperty("JButton.segmentPosition", "middle");
        toggle3.putClientProperty( "Quaqua.Button.style", "toggleCenter");

        final JToggleButton toggle4 = new JToggleButton("Taxa");
        toggle4.setFocusable(false);
        toggle4.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle4.putClientProperty("JButton.segmentPosition", "last");
        toggle4.putClientProperty( "Quaqua.Button.style", "toggleEast");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(toggle1);
        buttonGroup.add(toggle2);
        buttonGroup.add(toggle3);
        buttonGroup.add(toggle4);
        toggle1.setSelected(true);
        toggle1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.CLADE);
            }
        });
        toggle2.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.NODES);
            }
        });
        toggle3.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.TIPS);
            }
        });
        toggle4.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.TAXA);
            }
        });
        box1.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
        box1.add(toggle1);
        box1.add(toggle2);
        box1.add(toggle3);
        box1.add(toggle4);
        toolBar.addComponent(new GenericToolbarItem("Selection Mode", "What aspect of the tree is selected when it is clicked", box1));

        toolBar.addFlexibleSpace();

        final ToolbarAction prevTreeToolbarAction =
                new ToolbarAction(null, "Previous Tree...", prevIcon) {
                    public void actionPerformed(ActionEvent e){
                        if (treeViewer.isRootingOn() && treeViewer.getRootingType() == TreePane.RootingType.USER_ROOTING) {
                            JOptionPane.showMessageDialog(FigTreeFrame.this, "Cannot switch trees when user rooting option is on.\n" +
                                            "Turn this option off to switch trees",
                                    "Unable to switch trees",
                                    JOptionPane.ERROR_MESSAGE);

                        } else {
                            treeViewer.showPreviousTree();
                        }
                    }
                };
        JButton prevTreeToolButton = new ToolbarButton(prevTreeToolbarAction, true);
        prevTreeToolButton.setFocusable(false);
        prevTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        prevTreeToolButton.putClientProperty("JButton.segmentPosition", "first");
        prevTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleWest");

        final ToolbarAction nextTreeToolbarAction =
                new ToolbarAction(null, "Next Tree...", nextIcon) {
                    public void actionPerformed(ActionEvent e){
                        if (treeViewer.isRootingOn() && treeViewer.getRootingType() == TreePane.RootingType.USER_ROOTING) {
                            JOptionPane.showMessageDialog(FigTreeFrame.this, "Cannot switch trees when user rooting option is on.\n" +
                                            "Turn this option off to switch trees",
                                    "Unable to switch trees",
                                    JOptionPane.ERROR_MESSAGE);

                        } else {
                            treeViewer.showNextTree();
                        }
                    }
                };
        JButton nextTreeToolButton = new ToolbarButton(nextTreeToolbarAction, true);
        nextTreeToolButton.setFocusable(false);
        nextTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        nextTreeToolButton.putClientProperty("JButton.segmentPosition", "last");
        nextTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleEast");

        nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
        prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
        box2.add(prevTreeToolButton);
        box2.add(nextTreeToolButton);
        toolBar.addComponent(new GenericToolbarItem("Prev/Next", "Navigate through the trees", box2));

        TreeViewerListener l = new TreeViewerListener() {
            public void treeChanged() {
                boolean nextTreeEnabled = treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1;
                nextTreeAction.setEnabled(nextTreeEnabled);
                nextTreeToolbarAction.setEnabled(nextTreeEnabled);

                boolean prevTreeEnabled = treeViewer.getCurrentTreeIndex() > 0;
                previousTreeAction.setEnabled(prevTreeEnabled);
                prevTreeToolbarAction.setEnabled(prevTreeEnabled);
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        };
        treeViewer.addTreeViewerListener(l);
        l.treeChanged();

        toolBar.addFlexibleSpace();

        filterPopup = new JPopupMenu();

        final ButtonGroup bg = new ButtonGroup();
        boolean first = true;
        for (TreeViewer.TextSearchType searchType : TreeViewer.TextSearchType.values()) {
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(searchType.toString());
            if (first) {
                menuItem.setSelected(true);
                first = false;
            }
            filterPopup.add(menuItem);
            bg.add(menuItem);
        }
        filterPanel = new SearchPanel("Filter", filterPopup, true);
//        filterPanel.getSearchText().requestFocus();
        filterPanel.addSearchPanelListener(new SearchPanelListener() {

            /**
             * Called when the user requests a search by pressing return having
             * typed a search string into the text field. If the continuousUpdate
             * flag is true then this method is called when the user types into
             * the text field.
             *
             * @param searchString the user's search string
             */
            public void searchStarted(String searchString) {
                Enumeration e = bg.getElements();
                String value = null;
                while (e.hasMoreElements()) {
                    AbstractButton button = (AbstractButton)e.nextElement();
                    if (button.isSelected()) {
                        value = button.getText();
                    }
                }

                for (TreeViewer.TextSearchType searchType : TreeViewer.TextSearchType.values()) {
                    if (searchType.toString().equals(value)) {
                        treeViewer.selectTaxa(searchType, searchString, false);
                    }
                }
            }

            /**
             * Called when the user presses the cancel search button or presses
             * escape while the search is in focus.
             */
            public void searchStopped() {
                treeViewer.clearSelectedTaxa();
            }
        });

        JPanel panel3 = new JPanel(new FlowLayout());

        panel3.add(filterPanel);

        toolBar.addComponent(panel3);

        statusBar = new StatusBar("FigTree");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray),
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray)),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));
        statusBar.setOpaque(false);
        statusBar.setStatusProvider(treeViewer.getStatusProvider());

        JPanel topPanel = new JPanel(new BorderLayout(0,0));
        topPanel.add(toolBar, BorderLayout.NORTH);

        getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        getContentPane().add(topPanel, BorderLayout.NORTH);

        getContentPane().add(figTreePanel, BorderLayout.CENTER);

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        TreeSelectionListener l2 = new TreeSelectionListener() {
            public void selectionChanged() {
                boolean hasSelection = treeViewer.hasSelection();
                cartoonToolbarAction.setEnabled(hasSelection);
                cartoonAction.setEnabled(hasSelection);
                collapseToolbarAction.setEnabled(hasSelection);
                collapseAction.setEnabled(hasSelection);
//                clearCollapsedAction.setEnabled(hasSelection);
                hilightToolbarAction.setEnabled(hasSelection);
                hilightAction.setEnabled(hasSelection);
                clearHilightingAction.setEnabled(hasSelection);
//                midpointRootAction.setEnabled(hasSelection);
                rerootToolbarAction.setEnabled(hasSelection);
                rerootAction.setEnabled(hasSelection);
                clearRootingAction.setEnabled(hasSelection);
                rotateToolbarAction.setEnabled(hasSelection);
                rotateAction.setEnabled(hasSelection);
//                increasingNodeOrderAction.setEnabled(hasSelection);
//                decreasingNodeOrderAction.setEnabled(hasSelection);
//                clearRotationsAction.setEnabled(hasSelection);
                annotateToolbarAction.setEnabled(hasSelection);
//                annotateNodesFromTipsAction.setEnabled(hasSelection);
//                annotateTipsFromNodesAction.setEnabled(hasSelection);
                annotateAction.setEnabled(hasSelection);
//                clearAnnotationsAction.setEnabled(hasSelection);
                colourToolbarAction.setEnabled(hasSelection);
                colourAction.setEnabled(hasSelection);
                clearColouringAction.setEnabled(hasSelection);
            }
        };
        treeViewer.addTreeSelectionListener(l2);
        l2.selectionChanged();

        getCutAction().setEnabled(false);
        getCopyAction().setEnabled(true);
        getDeleteAction().setEnabled(false);
        getSelectAllAction().setEnabled(true);
        getFindAction().setEnabled(true);

        getZoomWindowAction().setEnabled(false);
    }

    private void defineAnnotations() {

        Collection<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions().values();

        if (annotationDefinitionsDialog == null) {
            annotationDefinitionsDialog = new AnnotationDefinitionsDialog(this);
        }
        annotationDefinitionsDialog.showDialog(definitions);

        treeViewer.setAnnotationDefinitions(annotationDefinitionsDialog.getAnnotations());

        treeViewer.fireAnnotationsChanged();
    }

    private void annotateNodesFromTips() {
        List<String> annotationNames = new ArrayList<String>();
        annotationNames.add("Colour");
        annotationNames.addAll(treeViewer.getAnnotationDefinitions().keySet());
        annotationNames.add("Name");

        if (selectAnnotationDialog == null) {
            selectAnnotationDialog = new SelectAnnotationDialog(this);
        }

        int result = selectAnnotationDialog.showDialog(annotationNames);
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return;
        }

        String annotationName = selectAnnotationDialog.getAnnotationName();
        if (annotationName.equals("Colour")) {
            annotationName = "!color";
        }

        treeViewer.annotateNodesFromTips(annotationName);
        setDirty();
    }

    private void annotateTipsFromNodes() {
        List<String> annotationNames = new ArrayList<String>();
        annotationNames.add("Colour");
        annotationNames.addAll(treeViewer.getAnnotationDefinitions().keySet());

        if (selectAnnotationDialog == null) {
            selectAnnotationDialog = new SelectAnnotationDialog(this);
        }

        if (selectAnnotationDialog.showDialog(annotationNames) != JOptionPane.CANCEL_OPTION) {
            String annotationName = selectAnnotationDialog.getAnnotationName();
            if (annotationName.equals("Colour")) {
                annotationName = "!color";
            }

            treeViewer.annotateTipsFromNodes(annotationName);
            setDirty();
        }
    }

    private void cartoonSelected() {
        treeViewer.cartoonSelectedNodes();
    }

    private void collapseSelected() {
        treeViewer.collapseSelectedNodes();
    }

    private void hilightSelected() {
        Color color = JColorChooser.showDialog(this, "Select Colour", lastColor);
        if (color != null) {
            treeViewer.hilightSelectedNodes(color);
            setDirty();
            lastColor = color;
        }
    }

    private void rerootTree() {
        Set<Node> nodes = treeViewer.getSelectedNodes();

        if (nodes.size() != 1 ) {
            JOptionPane.showMessageDialog(this,
                    "Exactly one branch must be selected to re-root the tree." ,
                    "Annotating Tree",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        treeViewer.rerootOnSelectedBranch();
    }

    private void rotateTree() {
        treeViewer.rotateSelectedNode();
    }

    private void annotateSelected() {
        try {
            annotateSelectedImpl();
        } catch (Throwable t) {
            // MyFigTree: this used to die without opening anything and without
            // saying why - see doSave for the same problem
            JOptionPane.showMessageDialog(this, "Could not open the annotation dialog: " + t,
                    "Annotate", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void annotateSelectedImpl() {
        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

        List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>();
        definitions.add(new AnnotationDefinition("Name", "!name", AnnotationDefinition.Type.STRING));
        definitions.addAll(treeViewer.getAnnotationDefinitions().values());

        if (annotationDialog == null) {
            annotationDialog = new AnnotationDialog(this);
        }

        Set<Node> nodes = treeViewer.getSelectedNodes();
        Set<Node> tips = treeViewer.getSelectedTips();

        Attributable item = null;
        if (nodes.size() + tips.size() == 1 ) {
            if (nodes.size() == 1) {
                item = nodes.iterator().next();
            }else if (tips.size() == 1) {
                item = tips.iterator().next();
            }
        } else {
            if (WARN_ANNOTATE_MULTIPLE) {
                int result = JOptionPane.showConfirmDialog(this,
                        "More than one node selected for annotation. This operation\n" +
                                "may overwrite existing annotations. Do you wish to continue?",
                        "Annotating Tree",
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                    return;
                }
            }
        }
        if (annotationDialog.showDialog(definitions, item) != JOptionPane.CANCEL_OPTION) {
            AnnotationDefinition newDefinition = annotationDialog.getDefinition();

            List<AnnotationDefinition> defs = new ArrayList<AnnotationDefinition>(treeViewer.getAnnotationDefinitions().values());
            if (!defs.contains(newDefinition)) {
                defs.add(newDefinition);
                treeViewer.setAnnotationDefinitions(defs);
            }

            String code = newDefinition.getCode();
            Object value = annotationDialog.getValue();

            treeViewer.annotateSelected(code, value);
            setDirty();
        }
    }

    private void copySelectedAnnotations() {
        // todo This function is disabled as it is not completely implemented
//        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
//
//        List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>();
//        definitions.add(new AnnotationDefinition("Name", "!name", AnnotationDefinition.Type.STRING));
//        definitions.addAll(treeViewer.getAnnotationDefinitions().values());
//
//        if (copyAnnotationDialog == null) {
//            copyAnnotationDialog = new AnnotationDialog(this, true);
//        }
//
//        Set<Node> nodes = treeViewer.getSelectedNodes();
//        Set<Node> tips = treeViewer.getSelectedTips();
//
//        Attributable item = null;
//        if (nodes.size() + tips.size() == 1 ) {
//            if (nodes.size() == 1) {
//                item = nodes.iterator().next();
//            }else if (tips.size() == 1) {
//                item = tips.iterator().next();
//            }
//        } else {
//            int result = JOptionPane.showConfirmDialog(this,
//                    "More than one node selected for annotation. This operation\n" +
//                            "may overwrite existing annotations. Do you wish to continue?" ,
//                    "Annotating Tree",
//                    JOptionPane.WARNING_MESSAGE);
//            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
//                return;
//            }
//        }
//        if (annotationDialog.showDialog(definitions, item) != JOptionPane.CANCEL_OPTION) {
//            String code = annotationDialog.getDefinition().getCode();
//            String code2 = annotationDialog.getDestinationDefinition().getCode();
//
//            treeViewer.copySelected(code, value);
//            setDirty();
//        }
    }

    private static Color lastColor = Color.GRAY;

    private void colourSelected() {
        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

        Color color = JColorChooser.showDialog(this, "Select Colour", lastColor);
        if (color != null) {
            treeViewer.annotateSelected("!color", color);
            setDirty();
            lastColor = color;
        }
    }

    public boolean readFromFile(File file) throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(file);

            boolean isNexus = isNexus(reader);

            ProgressMonitorInputStream in = new ProgressMonitorInputStream(
                    this,
                    "Reading " + file.getName(),
                    new FileInputStream(file));
            in.getProgressMonitor().setMillisToDecideToPopup(1000);
            in.getProgressMonitor().setMillisToPopup(1000);

            reader = new InputStreamReader(in);

            boolean success = readData(reader, isNexus);

            reader.close();

            return success;

        } catch (IOException ioe) {
            if (reader != null) {
                reader.close();
            }
            throw ioe;
        }
    }

    public boolean readFromURL(URL url) throws IOException {
        InputStream in = url.openStream();
        Reader reader = new InputStreamReader(url.openStream());

        boolean isNexus = isNexus(reader);

        reader.close();

        reader = new InputStreamReader(url.openStream());

        return readData(reader, isNexus);
    }

    private boolean isNexus(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        while (line != null && line.length() == 0) {
            line = bufferedReader.readLine();
        }

        return (line != null && line.toUpperCase().contains("#NEXUS"));
    }

    public boolean readFromString(String string) throws IOException {
        boolean isNexus = string.substring(0, 80).toUpperCase().contains("#NEXUS");
        return readData(new StringReader(string), isNexus);
    }

    protected boolean readData(Reader reader, boolean isNexus) throws IOException {

        List<Tree> trees = new ArrayList<Tree>();

        boolean hasSettings = false;

        try {
            Map<String, Object> settings = new HashMap<String, Object>();
            // First of all, fully populate the settings map so that
            // all the settings have defaults
            controlPalette.getSettings(settings);

            if (isNexus) {
                FigTreeNexusImporter importer = new FigTreeNexusImporter(reader);
                while (importer.hasTree()) {
                    Tree tree = importer.importNextTree();
                    trees.add(tree);
                }
                // Try to find a figtree block and if found, parse the settings
                while (true) {
                    try {
                        importer.findNextBlock();
                        if (importer.getNextBlockName().equalsIgnoreCase("FIGTREE")) {
                            importer.parseFigTreeBlock(settings);
                            hasSettings = true;
                        }
                    } catch (EOFException ex) {
                        break;
                    }
                }
            } else {
                NewickImporter importer = new NewickImporter(reader, true);
                while (importer.hasTree()) {
                    Tree tree = importer.importNextTree();
                    trees.add(tree);
                }
            }

            if (trees.isEmpty()) {
                throw new ImportException("This file contained no trees.");
            }

            checkLabelAttribute(trees);

            treeViewer.setTrees(trees);
            controlPalette.setSettings(settings);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading tree file: \n" + ie.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!hasSettings) {
            // If there weren't settings in the file then this wasn't a TreeDraw
            // created document so we don't want to be able to overwrite it without
            // explicit action of the user...
            setDirty();
            clearFile();
        }

        return true;
    }

    /**
     * This version loads the trees in a thread but this needs more thought in order
     * to tie in to the JAM framework correctly
     */
//    protected boolean readData(final Reader reader, final boolean isNexus) {
//
//        final JFrame frame = this;
//        Thread readThread = new Thread () {
//            public void run() {
//                try {
//
//                    final List<Tree> trees = new ArrayList<Tree>();
//
//                    boolean hasSettings = false;
//
//                    final Map<String, Object> settings = new HashMap<String, Object>();
//                    // First of all, fully populate the settings map so that
//                    // all the settings have defaults
//                    controlPalette.getSettings(settings);
//
//                    if (isNexus) {
//                        FigTreeNexusImporter importer = new FigTreeNexusImporter(reader);
//                        while (importer.hasTree()) {
//                            Tree tree = importer.importNextTree();
//                            trees.add(tree);
//                        }
//                        // Try to find a figtree block and if found, parse the settings
//                        while (true) {
//                            try {
//                                importer.findNextBlock();
//                                if (importer.getNextBlockName().equalsIgnoreCase("FIGTREE")) {
//                                    importer.parseFigTreeBlock(settings);
//                                    hasSettings = true;
//                                }
//                            } catch (EOFException ex) {
//                                break;
//                            }
//                        }
//                    } else {
//                        NewickImporter importer = new NewickImporter(reader, true);
//                        while (importer.hasTree()) {
//                            Tree tree = importer.importNextTree();
//                            trees.add(tree);
//                        }
//                    }
//
//                    if (trees.size() == 0) {
//                        throw new ImportException("This file contained no trees.");
//                    }
//
//                    final boolean hasSettings2 = hasSettings;
//
//                    EventQueue.invokeLater (
//                            new Runnable () {
//                                public void run () {
//                                    treeViewer.setTrees(trees);
//                                    controlPalette.setSettings(settings);
//
//                                    if (!hasSettings2) {
//                                        // If there weren't settings in the file then this wasn't a TreeDraw
//                                        // created document so we don't want to be able to overwrite it without
//                                        // explicit action of the user...
//                                        setDirty();
//                                        clearFile();
//                                    }
//                                }
//                            });
//
//                } catch (final ImportException ie) {
//                    EventQueue.invokeLater (
//                            new Runnable () {
//                                public void run () {
//                                    JOptionPane.showMessageDialog(frame, "Error reading tree file: " + ie.getMessage(),
//                                            "Import Error",
//                                            JOptionPane.ERROR_MESSAGE);
//                                }
//                            });
//                } catch (final InterruptedIOException iioex) {
//                    // The cancel dialog button was pressed - do nothing
//                } catch (final IOException ioex) {
//                    EventQueue.invokeLater (
//                            new Runnable () {
//                                public void run () {
//                                    JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
//                                            "File I/O Error",
//                                            JOptionPane.ERROR_MESSAGE);
//                                }
//                            });
//                }
//
//            }
//        };
//        readThread.start();
//
//        return true;
//    }


    private void checkLabelAttribute(List<Tree> trees) {

        boolean hasLabel = false;

        for (Tree tree : trees) {
            for (Node node : tree.getNodes()) {
                if (node.getAttribute("label") != null) {
                    hasLabel = true;
                }
            }
        }

        if (hasLabel) {
            String labelName = null;

            do {
                labelName = JOptionPane.showInputDialog(
                        "The node/branches of the tree are labelled\n" +
                                "(i.e., with bootstrap values or posterior probabilities).\n\n" +
                                "Please select a name for these values.", "label");
                if (labelName == null) {
                    labelName = "label";
                }
                labelName = labelName.trim();

                if (labelName.length() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
            } while (labelName == null || labelName.length() == 0);

            if (!labelName.equals("label")) {
                for (Tree tree : trees) {
                    for (Node node : tree.getNodes()) {
                        Object value = node.getAttribute("label");
                        if (value != null) {
                            node.removeAttribute("label");
                            node.setAttribute(labelName, value);
                        }
                    }
                }
            }
        }
    }


    protected boolean writeToFile(File file) throws IOException {

        Map<String, Object> settings = new TreeMap<String, Object>();
        controlPalette.getSettings(settings);

        // MyFigTree: write the whole file to a temporary one first and put it in
        // place only once it is complete. The original opened the real file
        // straight away, so anything going wrong half way through left an EMPTY
        // file and the tree was gone - which is exactly what happened on
        // 2026-08-25, and without a word of warning.
        File folder = file.getAbsoluteFile().getParentFile();
        File temp = File.createTempFile("figtree", ".part", folder);

        try {
            Writer writer = new FileWriter(temp);
            try {
                FigTreeNexusExporter exporter = new FigTreeNexusExporter(writer, true);
                exporter.exportTrees(treeViewer.getTrees(), true);
                exporter.writeFigTreeBlock(settings);
            } finally {
                writer.close();
            }
        } catch (Throwable t) {
            temp.delete();
            reportSaveFailure(t, file);
            throw new IOException("Could not write the tree file: " + t, t);
        }

        try {
            moveIntoPlace(temp, file);
        } catch (IOException ioe) {
            temp.delete();
            reportSaveFailure(ioe, file);
            throw ioe;
        }

        return true;
    }

    private static void moveIntoPlace(File temp, File file) throws IOException {
        try {
            Files.move(temp.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            // not every file system can do an atomic move - fall back to a plain one
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * MyFigTree: a save that dies has to say so. It used to fail in complete
     * silence, which is how an empty file could look like a successful save.
     */
    private void reportSaveFailure(Throwable t, File file) {
        JOptionPane.showMessageDialog(this,
                "<html>The tree could <b>not</b> be saved.<br><br>" +
                        "Your existing file has been left exactly as it was:<br>" +
                        file.getPath() + "<br><br>" +
                        "What went wrong:<br><tt>" + t + "</tt></html>",
                "Save Failed",
                JOptionPane.ERROR_MESSAGE);
    }

    public final void doImport() {

        FileDialog dialog = new FileDialog(this,
                "Import Annotations File...",
                FileDialog.LOAD);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            try {
                AnnotationTable table = readAnnotationTable(file);

                if (table.rowNames.isEmpty() || table.columns.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No annotations were found in this file.\n\n" +
                                    "The first line should be a header giving the name of each column\n" +
                                    "and the first column should hold the taxon names, exactly as they\n" +
                                    "appear in the tree file. Columns may be separated by tabs, commas\n" +
                                    "or semicolons.",
                            "Import Annotations",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                applyAnnotationTable(table);

                setDirty();

            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
                        "Unable to read file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /**
     * MyFigTree (Etap 6.3): applies an imported table of annotations to the taxa of the loaded trees
     * and reports how many names were actually matched - a silent import that matched nothing used to
     * look exactly like one that worked.
     */
    private void applyAnnotationTable(AnnotationTable table) {

        // index the tips of the loaded trees by name; the 'loose' index (lower case, underscores and
        // spaces treated alike) catches names that were tidied up in the spreadsheet
        Map<String, Taxon> taxonByName = new LinkedHashMap<String, Taxon>();
        Map<String, Taxon> taxonByLooseName = new HashMap<String, Taxon>();
        List<Tree> trees = treeViewer.getTrees();
        if (trees != null) {
            for (Tree tree : trees) {
                for (Taxon taxon : tree.getTaxa()) {
                    taxonByName.put(taxon.getName(), taxon);
                    taxonByLooseName.put(looseName(taxon.getName()), taxon);
                }
            }
        }

        Map<String, Taxon> resolved = new LinkedHashMap<String, Taxon>();
        List<String> unmatched = new ArrayList<String>();
        int looseMatchCount = 0;

        for (String rowName : table.rowNames) {
            Taxon taxon = taxonByName.get(rowName);
            if (taxon == null) {
                taxon = taxonByLooseName.get(looseName(rowName));
                if (taxon != null) {
                    looseMatchCount++;
                }
            }
            if (taxon == null) {
                unmatched.add(rowName);
                // keep it anyway - it may match a tree opened later in this window
                taxon = Taxon.getTaxon(rowName);
            }
            resolved.put(rowName, taxon);
        }

        Map<AnnotationDefinition, Map<Taxon, Object>> annotations =
                new TreeMap<AnnotationDefinition, Map<Taxon, Object>>();
        for (AnnotationDefinition definition : table.columns.keySet()) {
            Map<String, Object> column = table.columns.get(definition);
            Map<Taxon, Object> values = new HashMap<Taxon, Object>();
            for (String rowName : column.keySet()) {
                values.put(resolved.get(rowName), column.get(rowName));
            }
            annotations.put(definition, values);
        }

        treeViewer.setTaxonAnnotations(annotations);

        int matchCount = table.rowNames.size() - unmatched.size();

        Set<Taxon> annotatedTaxa = new HashSet<Taxon>(resolved.values());
        int tipsWithoutAnnotation = 0;
        for (Taxon taxon : taxonByName.values()) {
            if (!annotatedTaxa.contains(taxon)) {
                tipsWithoutAnnotation++;
            }
        }

        StringBuilder message = new StringBuilder();
        message.append("Imported ").append(table.columns.size())
                .append(table.columns.size() == 1 ? " annotation: " : " annotations: ");
        boolean first = true;
        for (AnnotationDefinition definition : table.columns.keySet()) {
            if (!first) {
                message.append(", ");
            }
            message.append(definition.getName());
            first = false;
        }
        message.append("\n\n");

        message.append("Matched ").append(matchCount).append(" of ").append(table.rowNames.size())
                .append(table.rowNames.size() == 1 ? " name" : " names")
                .append(" in the file to tips of the tree");
        if (looseMatchCount > 0) {
            message.append(" (").append(looseMatchCount)
                    .append(" of them ignoring case, spaces and underscores)");
        }
        message.append(".\n");

        if (tipsWithoutAnnotation > 0) {
            message.append(tipsWithoutAnnotation)
                    .append(tipsWithoutAnnotation == 1 ? " tip of the tree is" : " tips of the tree are")
                    .append(" not listed in the file - group bars will be broken there.\n");
        }

        if (!unmatched.isEmpty()) {
            message.append("\nNot found in the tree:\n");
            int shown = Math.min(unmatched.size(), 10);
            for (int i = 0; i < shown; i++) {
                message.append("    ").append(unmatched.get(i)).append("\n");
            }
            if (unmatched.size() > shown) {
                message.append("    ... and ").append(unmatched.size() - shown).append(" more\n");
            }
        }

        if (matchCount == 0) {
            message.append("\nNothing was annotated. Check that the first column of the file holds the\n")
                    .append("taxon names exactly as they appear in the tree file (with underscores).");
        }

        JOptionPane.showMessageDialog(this, message.toString(),
                "Import Annotations",
                matchCount == 0 || !unmatched.isEmpty() ?
                        JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    /** Name reduced to a form that survives tidying up in a spreadsheet: lower case, one space per gap. */
    private static String looseName(String name) {
        return name.trim().toLowerCase().replaceAll("[\\s_]+", " ");
    }

    /**
     * MyFigTree (Etap 6.3): the contents of an annotation file, keyed by the taxon name as written in
     * the file - matching those names against the tree is done separately, in applyAnnotationTable().
     */
    protected static class AnnotationTable {
        final List<String> rowNames = new ArrayList<String>();
        final Map<AnnotationDefinition, Map<String, Object>> columns =
                new LinkedHashMap<AnnotationDefinition, Map<String, Object>>();
    }

    /**
     * Reads a table of annotations: a header line of column names followed by one line per taxon, the
     * first column holding the taxon name. Tab, comma and semicolon delimited files are all accepted
     * (a Polish Excel writes CSV with semicolons), as are UTF-8 and the system encoding, quoted cells,
     * blank lines and lines starting with a hash.
     */
    protected AnnotationTable readAnnotationTable(File file) throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException(file.getName());
        }

        List<String> lines = readLines(file);

        String[] labels = null;
        String delimiter = null;
        List<String[]> rows = new ArrayList<String[]>();

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                continue;
            }
            if (labels == null) {
                delimiter = guessDelimiter(line);
                labels = trimEmptyCells(splitLine(line, delimiter));
                if (labels.length < 2) {
                    throw new IOException("the header line (line " + lineNumber + ") has only one column;" +
                            " expected the taxon name and at least one annotation");
                }
            } else {
                String[] values = splitLine(line, delimiter);
                for (int i = labels.length; i < values.length; i++) {
                    if (values[i].length() > 0) {
                        throw new IOException("line " + lineNumber + " has more columns than the header line");
                    }
                }
                String[] row = new String[labels.length];
                for (int i = 0; i < labels.length; i++) {
                    row[i] = i < values.length ? values[i] : "";
                }
                if (row[0].length() > 0) {
                    rows.add(row);
                }
            }
        }

        AnnotationTable table = new AnnotationTable();
        if (labels == null) {
            return table;
        }

        for (String[] row : rows) {
            if (!table.rowNames.contains(row[0])) {
                table.rowNames.add(row[0]);
            }
        }

        for (int i = 1; i < labels.length; i++) {
            if (labels[i].length() == 0) {
                continue;
            }

            // blank cells are left out altogether so that a tip without a value simply has no annotation
            Map<String, String> strings = new LinkedHashMap<String, String>();
            for (String[] row : rows) {
                if (row[i].length() > 0) {
                    strings.put(row[0], row[i]);
                }
            }
            if (strings.isEmpty()) {
                continue;
            }

            boolean isBoolean = true;
            boolean isInteger = true;
            boolean isNumber = true;
            for (String valueString : strings.values()) {
                if (!valueString.equalsIgnoreCase("TRUE") && !valueString.equalsIgnoreCase("FALSE")) {
                    isBoolean = false;
                }
                try {
                    double number = Double.parseDouble(valueString);
                    if (Math.round(number) != number) {
                        isInteger = false;
                    }
                } catch (NumberFormatException nfe) {
                    isInteger = false;
                    isNumber = false;
                }
            }

            AnnotationDefinition.Type type;
            if (isBoolean) {
                type = AnnotationDefinition.Type.BOOLEAN;
            } else if (isInteger) {
                type = AnnotationDefinition.Type.INTEGER;
            } else if (isNumber) {
                type = AnnotationDefinition.Type.REAL;
            } else {
                type = AnnotationDefinition.Type.STRING;
            }

            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, String> entry : strings.entrySet()) {
                String valueString = entry.getValue();
                Object value;
                if (type == AnnotationDefinition.Type.BOOLEAN) {
                    value = Boolean.valueOf(valueString);
                } else if (type == AnnotationDefinition.Type.INTEGER) {
                    value = Integer.valueOf((int) Math.round(Double.parseDouble(valueString)));
                } else if (type == AnnotationDefinition.Type.REAL) {
                    value = Double.valueOf(valueString);
                } else {
                    value = valueString;
                }
                values.put(entry.getKey(), value);
            }

            table.columns.put(new AnnotationDefinition(labels[i], type), values);
        }

        return table;
    }

    /** Whichever of tab, semicolon or comma appears most often in the header line. */
    private static String guessDelimiter(String headerLine) {
        String best = ",";
        int bestCount = 0;
        for (String candidate : new String[]{"\t", ";", ","}) {
            int count = headerLine.length() - headerLine.replace(candidate, "").length();
            if (count > bestCount) {
                best = candidate;
                bestCount = count;
            }
        }
        return best;
    }

    private static String[] splitLine(String line, String delimiter) {
        String[] cells = line.split(java.util.regex.Pattern.quote(delimiter), -1);
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i].trim();
            if (cell.length() > 1 && cell.startsWith("\"") && cell.endsWith("\"")) {
                cell = cell.substring(1, cell.length() - 1).trim();
            }
            cells[i] = cell;
        }
        return cells;
    }

    /** Excel likes to write trailing empty columns; drop them from the header. */
    private static String[] trimEmptyCells(String[] cells) {
        int length = cells.length;
        while (length > 0 && cells[length - 1].length() == 0) {
            length--;
        }
        return Arrays.copyOf(cells, length);
    }

    /** Reads a text file as UTF-8 if it is valid UTF-8 and in the system encoding otherwise. */
    private static List<String> readLines(File file) throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String text;
        try {
            java.nio.charset.CharsetDecoder decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
            text = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException cce) {
            text = new String(bytes, java.nio.charset.Charset.defaultCharset());
        }
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        return Arrays.asList(text.split("\r\n|\r|\n", -1));
    }


    private void doImportCharacters() {

        FileDialog dialog = new FileDialog(this,
                "Import Characters File...",
                FileDialog.LOAD);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            try {
                importCharactersFromFile(file);
            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
                        "Unable to read file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    protected boolean importCharactersFromFile(File file) throws FileNotFoundException, IOException {

        final String fileName = file.getName();
        SequenceImporter importer = new NexusImporter(new FileReader(file));
        try {
            Alignment alignment = new BasicAlignment(importer.importSequences());

            treeViewer.setCharacters(alignment);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading characters file: " + ie.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void doImportColourScheme() {

        FileDialog dialog = new FileDialog(this,
                "Import Colour Scheme...",
                FileDialog.LOAD);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            try {
                importColourSchemeFromFile(file);
            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
                        "Unable to read file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    protected boolean importColourSchemeFromFile(File file) throws FileNotFoundException, IOException {

        final String fileName = file.getName();
        SequenceImporter importer = new NexusImporter(new FileReader(file));
        try {
            Alignment alignment = new BasicAlignment(importer.importSequences());

            treeViewer.setCharacters(alignment);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading characters file: " + ie.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
    public final void doExport() {

        if (exportTreeDialog == null) {
            exportTreeDialog = new ExportTreeDialog(this);
        }

        if (exportTreeDialog.showDialog() == JOptionPane.OK_OPTION) {

            FileDialog dialog = new FileDialog(this,
                    "Export Tree File...",
                    FileDialog.SAVE);

            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                File file = new File(dialog.getDirectory(), dialog.getFile());

                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writeTreeFile(writer,
                            exportTreeDialog.getFormat(),
                            exportTreeDialog.allTrees(),
                            exportTreeDialog.asDisplayed(),
                            exportTreeDialog.includeFigTreeBlock(),
                            exportTreeDialog.includeAnnotations(),
                            false);
                    writer.close();
                } catch (Throwable t) {
                    // MyFigTree: an Error (not just an IOException) used to kill this
                    // silently, leaving the user staring at a menu that did nothing
                    JOptionPane.showMessageDialog(this, "Error writing tree file: " + t,
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }


    public final void doExtract() {
        RootedTree tree = treeViewer.getSelectedSubtree();
        FigTreeFrame frame = (FigTreeFrame)FigTreeApplication.application.doNew();
        frame.treeViewer.addTrees(Collections.singletonList(tree));
        frame.treeViewer.showNextTree();
    }

    public final void doExportGraphic(GraphicFormat format) {
        FileDialog dialog = new FileDialog(this,
                "Export " + format.getName() + " File...",
                FileDialog.SAVE);

        String name = (this.getFile() != null ? this.getFile().getName() : "untitled") + "." + format.getName().toLowerCase();
        dialog.setFile(name);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            // MyFigTree (Etap 4.6): page size / margins / embedded fonts for PDF
            PdfExportOptions pdfOptions = null;
            if (format == GraphicFormat.PDF) {
                pdfOptions = showPdfExportOptionsDialog();
                if (pdfOptions == null) {
                    return; // cancelled
                }
            }

            try {
                OutputStream stream = new FileOutputStream(file);

                if (format == GraphicFormat.PDF) {
                    exportPDFFile(treeViewer.getContentPane(), stream, pdfOptions);
                } else {
                    exportGraphics(format, treeViewer.getContentPane(), stream);
                }

                stream.flush();
                stream.close();
            } catch(DocumentException de) {
                JOptionPane.showMessageDialog(this, "Error writing PDF file: " + de,
                        "Export PDF Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Error writing tree file: " + ioe.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        }

    }

    public final static void exportGraphics(GraphicFormat format, JComponent comp, OutputStream stream) throws IOException, DocumentException {
        switch (format) {

            case PNG:
            case GIF:
            case BMP:
            case JPEG:
                exportGraphicsFile(format, comp, stream);
                break;
            case EPS:
                throw new UnsupportedOperationException("EPS not handled");
            case SVG:
                exportSVGFile(comp, stream);
                break;
            case PDF:
                exportPDFFile(comp, stream);
                break;
            default:
                throw new UnsupportedOperationException("Format not handled: " + format);

        }

    }
    private final static void exportGraphicsFile(GraphicFormat format, JComponent component, OutputStream stream) throws IOException {
        int imageType = BufferedImage.TYPE_INT_RGB;

        if (format == GraphicFormat.PNG) {
            // PNG allows an alpha channel
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage bi = new BufferedImage(component.getSize().width, component.getSize().height, imageType);
        Graphics g = bi.createGraphics();

        if (format != GraphicFormat.PNG) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        }
        component.paint(g);
        g.dispose();
        ImageIO.write(bi, format.getName(), stream);
    }

    private final static void exportSVGFile(JComponent component, OutputStream stream) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        component.paint(svgGenerator);
        Element svgRoot = svgGenerator.getRoot();
        Rectangle2D bounds = component.getBounds();
        String viewBox = "0 0 " + bounds.getWidth() + " " + bounds.getHeight();
        svgRoot.setAttributeNS(null, svgGenerator.SVG_VIEW_BOX_ATTRIBUTE, viewBox);
        svgRoot.setAttributeNS(null, svgGenerator.SVG_WIDTH_ATTRIBUTE, Double.toString(bounds.getWidth()));
        svgRoot.setAttributeNS(null, svgGenerator.SVG_HEIGHT_ATTRIBUTE, Double.toString(bounds.getHeight()));

        // Write svg file
        Writer out = new OutputStreamWriter(stream, "UTF-8");
        svgGenerator.stream(svgRoot, out, true /* use css */, false /* escaped */);
    }

    public final static void exportPDFFile(JComponent component, OutputStream stream) throws DocumentException {
        exportPDFFile(component, stream, new PdfExportOptions());
    }

    // MyFigTree (Etap 4.6) -------------------------------------------------------------------

    /** Options chosen in the small dialog shown before a PDF export. */
    public static class PdfExportOptions {
        public enum PageSize {
            FIT_TO_TREE("Fit to tree"),
            A4_PORTRAIT("A4 portrait"),
            A4_LANDSCAPE("A4 landscape");

            PageSize(String name) { this.name = name; }
            public String toString() { return name; }
            private final String name;
        }

        public PageSize pageSize = PageSize.FIT_TO_TREE;
        public double marginMm = 10.0;
        public boolean embedFonts = false;
    }

    private static PdfExportOptions lastPdfOptions = new PdfExportOptions();

    private PdfExportOptions showPdfExportOptionsDialog() {
        final JComboBox pageSizeCombo = new JComboBox(PdfExportOptions.PageSize.values());
        pageSizeCombo.setSelectedItem(lastPdfOptions.pageSize);
        final JSpinner marginSpinner = new JSpinner(new SpinnerNumberModel(lastPdfOptions.marginMm, 0.0, 100.0, 1.0));
        final JCheckBox embedFontsCheck = new JCheckBox("Embed fonts (TrueType from the system font folder)");
        embedFontsCheck.setSelected(lastPdfOptions.embedFonts);

        final JLabel marginLabel = new JLabel("Margin (mm):");
        pageSizeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean a4 = pageSizeCombo.getSelectedItem() != PdfExportOptions.PageSize.FIT_TO_TREE;
                marginSpinner.setEnabled(a4);
                marginLabel.setEnabled(a4);
            }
        });
        boolean a4 = lastPdfOptions.pageSize != PdfExportOptions.PageSize.FIT_TO_TREE;
        marginSpinner.setEnabled(a4);
        marginLabel.setEnabled(a4);

        jam.panels.OptionsPanel panel = new jam.panels.OptionsPanel(6, 6);
        panel.addComponentWithLabel("Page size:", pageSizeCombo);
        panel.addComponents(marginLabel, false, marginSpinner, false);
        panel.addSpanningComponent(embedFontsCheck);

        int result = JOptionPane.showConfirmDialog(this, panel, "Export PDF options",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        PdfExportOptions options = new PdfExportOptions();
        options.pageSize = (PdfExportOptions.PageSize) pageSizeCombo.getSelectedItem();
        options.marginMm = ((Number) marginSpinner.getValue()).doubleValue();
        options.embedFonts = embedFontsCheck.isSelected();
        lastPdfOptions = options;
        return options;
    }

    private static DefaultFontMapper embeddingFontMapper = null;

    /** A font mapper that knows the TrueType fonts installed on this machine, so they get embedded. */
    private static synchronized DefaultFontMapper getEmbeddingFontMapper() {
        if (embeddingFontMapper == null) {
            DefaultFontMapper mapper = new DefaultFontMapper();
            java.util.List<String> dirs = new ArrayList<String>();
            String windir = System.getenv("WINDIR");
            if (windir != null) dirs.add(windir + File.separator + "Fonts");
            dirs.add(System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + File.separator + "Microsoft" + File.separator + "Windows" + File.separator + "Fonts");
            dirs.add("/Library/Fonts");
            dirs.add("/System/Library/Fonts");
            dirs.add("/usr/share/fonts/truetype");
            dirs.add(System.getProperty("java.home") + File.separator + "lib" + File.separator + "fonts");
            for (String dir : dirs) {
                try {
                    if (new File(dir).isDirectory()) {
                        mapper.insertDirectory(dir);
                    }
                } catch (RuntimeException e) {
                    // ignore a broken font directory
                }
            }
            embeddingFontMapper = mapper;
        }
        return embeddingFontMapper;
    }

    public final static void exportPDFFile(JComponent component, OutputStream stream, PdfExportOptions options) throws DocumentException {
        Rectangle2D bounds = component.getBounds();
        final float w = (float) bounds.getWidth();
        final float h = (float) bounds.getHeight();

        DefaultFontMapper fontMapper = options.embedFonts ? getEmbeddingFontMapper() : new DefaultFontMapper();

        float pageW, pageH, scale, offsetX, offsetY;
        if (options.pageSize == PdfExportOptions.PageSize.FIT_TO_TREE) {
            // the original behaviour: the page is exactly the size of the tree panel
            pageW = w;
            pageH = h;
            scale = 1.0f;
            offsetX = 0.0f;
            offsetY = 0.0f;
        } else {
            com.itextpdf.text.Rectangle a4 = com.itextpdf.text.PageSize.A4;
            if (options.pageSize == PdfExportOptions.PageSize.A4_LANDSCAPE) {
                a4 = a4.rotate();
            }
            pageW = a4.getWidth();
            pageH = a4.getHeight();
            float margin = (float) (options.marginMm * 72.0 / 25.4);
            float availW = Math.max(1.0f, pageW - 2 * margin);
            float availH = Math.max(1.0f, pageH - 2 * margin);
            scale = Math.min(availW / w, availH / h);
            // centre the tree inside the margins (PDF origin is bottom-left)
            offsetX = margin + (availW - w * scale) / 2.0f;
            offsetY = margin + (availH - h * scale) / 2.0f;
        }

        Document document = new Document(new com.itextpdf.text.Rectangle(pageW, pageH));
        // step 2
        PdfWriter writer;
        writer = PdfWriter.getInstance(document, stream);
        // step 3
        document.open();
        // step 4
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(w, h);
        Graphics2D g2d = tp.createGraphics(w, h, fontMapper);
        component.print(g2d);
        g2d.dispose();
        cb.addTemplate(tp, scale, 0, 0, scale, offsetX, offsetY);

        document.close();
    }

    public void doCopy() {
        StringWriter writer = new StringWriter();
        try {
            if (treeViewer.getSelectionMode() == TreePaneSelector.SelectionMode.TAXA) {
                writeTaxa(writer);
            } else {
                writeTreeFile(writer, ExportTreeDialog.Format.NEXUS, true, false, false, true, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection selection = new StringSelection(writer.toString());

        clipboard.setContents(selection, selection);
    }

    public void doPaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable transfer = clipboard.getContents(null);
        if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clip = (String)transfer.getTransferData(DataFlavor.stringFlavor);
                boolean isNexus = clip.substring(0, Math.min(80, clip.length())).toUpperCase().contains("#NEXUS");
                Reader reader = new StringReader(clip);

                List<Tree> trees = new ArrayList<Tree>();

                TreeImporter importer;
                if (isNexus) {
                    importer = new FigTreeNexusImporter(reader);
                } else {
                    importer = new NewickImporter(reader, true);
                }
                while (importer.hasTree()) {
                    Tree tree = importer.importNextTree();
                    trees.add(tree);
                }

                if (trees.size() == 0) {
                    throw new ImportException("This clipboard contained no trees.");
                }
                treeViewer.addTrees(trees);

// Show the first of the new trees
                treeViewer.showNextTree();
                setDirty();

            } catch (ImportException ie) {
                JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ie.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ioe.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedFlavorException e) {
                JOptionPane.showMessageDialog(this, "Clipboard data is not compatible",
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void doSelectAll() {
        treeViewer.selectAll();
    }

    protected void writeTaxa(Writer writer) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        for (Taxon taxon : treeViewer.getSelectedTaxa()) {
            printWriter.println(taxon.getName());
        }
        writer.close();
    }

    protected void writeTreeFile(Writer writer, ExportTreeDialog.Format format,
                                 boolean writeAllTrees,
                                 boolean writeAsDisplayed,
                                 boolean writeFigTreeBlock,
                                 boolean writeAnnotations,
                                 boolean writeSelectedSubtree) throws IOException {

        Map<String, Object> settings = null;
        if (writeFigTreeBlock) {
            settings = new TreeMap<String, Object>();
            controlPalette.getSettings(settings);
        }

        List<Tree> trees = new ArrayList<Tree>();

        if (writeSelectedSubtree) {
            RootedTree tree = treeViewer.getSelectedSubtree();
            if (tree != null) {
                trees.add(tree);
            }
        } else {
            trees.addAll(treeViewer.getTreesAsViewed());
        }

        if (trees.size() > 0) {
            switch (format) {
                case NEWICK:
                    NewickExporter newickExporter = new NewickExporter(writer);
                    newickExporter.exportTrees(trees);
                    newickExporter.close();
                    break;
                case NEXUS:
                    FigTreeNexusExporter nexusExporter = new FigTreeNexusExporter(writer, writeAnnotations);
                    nexusExporter.exportTrees(trees);
                    if (settings != null) {
                        nexusExporter.writeFigTreeBlock(settings);
                    }
                    nexusExporter.close();
                    break;
                case JSON:
                    JSONTreeExporter jsonExporter = new JSONTreeExporter(writer, writeAnnotations);
                    jsonExporter.exportTrees(trees);
                    jsonExporter.close();
                    break;
            }
        }
    }

    public final void doFind() {
        if (findPanel == null) {
            findPanel = new FindPanel(findAllAction, null);
            findPanel.setOpaque(false);

            treeViewer.addAnnotationsListener(new AnnotationsListener() {
                public void annotationsChanged() {
                    List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>(
                            treeViewer.getAnnotationDefinitions().values());
                    findPanel.setup(definitions);
                }
            });

            List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>(
                    treeViewer.getAnnotationDefinitions().values());
            findPanel.setup(definitions);
        }

        if (figTreePanel.getUtilityPanel() != findPanel) {
            figTreePanel.showUtilityPanel(findPanel);
        } else {
            figTreePanel.hideUtilityPanel();
        }
    }

    public final void doFindAll() {

        FindPanel.Target target = findPanel.getSearchTarget();
        String targetString = findPanel.getSearchTargetString();

        if (findPanel.isNumericSearchType()) {
            TreeViewer.NumberSearchType searchType = findPanel.getNumberSearchType();
            Number searchValue = findPanel.getSearchValue();
            if (target == FindPanel.Target.TAXON_LABEL) {
                throw new IllegalArgumentException("Can't do numeric search on taxon labels");
            } else if (target == FindPanel.Target.BRANCH_LENGTH) {
                treeViewer.selectNodes("!length", searchType, searchValue);
            } else if (target == FindPanel.Target.NODE_AGE) {
                treeViewer.selectNodes("!height", searchType, searchValue);
            } else if (target == FindPanel.Target.ANY_ANNOTATION) {
                throw new IllegalArgumentException("Can't do numeric search on all annotations");
            } else {
                treeViewer.selectNodes(targetString, searchType, searchValue);
            }

        } else {
            TreeViewer.TextSearchType searchType = findPanel.getTextSearchType();
            String searchText = findPanel.getSearchText();
            boolean caseSensitive = findPanel.isCaseSensitive();

            if (target == FindPanel.Target.TAXON_LABEL) {
                treeViewer.selectTaxa("!name", searchType, searchText, caseSensitive);
            } else if (target == FindPanel.Target.BRANCH_LENGTH) {
                throw new IllegalArgumentException("Can't do text search on branch lengths");
            } else if (target == FindPanel.Target.NODE_AGE) {
                throw new IllegalArgumentException("Can't do text search on node ages");
            } else if (target == FindPanel.Target.ANY_ANNOTATION) {
                treeViewer.selectNodes(null, searchType, searchText, caseSensitive);
            } else {
                treeViewer.selectNodes(targetString, searchType, searchText, caseSensitive);
            }
        }
    }

    public final void doFindNext() {

    }

    public final void doGetInfo() {
//        List<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions();
//        JPanel panel = new FindPanel(definitions);
//        panel.setOpaque(false);
//        figTreePanel.showUtilityPanel(panel);
    }

    public JComponent getExportableComponent() {
        return treeViewer.getContentPane();
    }

    @Override
    public Action getImportColourSchemeAction() {
        return importColourSchemeAction;
    }

    @Override
    public Action getExportColourSchemeAction() {
        return exportColourSchemeAction;
    }

    @Override
    public Action getExtractTreeAction() {
        return extractTreeAction;
    }

    public Action getExtactTreeAction() {
        return exportTreesAction;
    }

    public Action getExportTreesAction() {
        return exportTreesAction;
    }

//    public Action getExportGraphicAction() {
//        return exportGraphicAction;
//    }

    public Action getExportPDFAction() {
        return exportPDFAction;
    }

    public Action getExportPNGGraphicAction() {
        return exportPNGGraphicAction;
    }

    public Action getExportJPEGGraphicAction() {
        return exportJPEGGraphicAction;
    }

    public Action getExportSVGGraphicAction() {
        return exportSVGAction;
    }

    public Action getNextTreeAction() {
        return nextTreeAction;
    }

    public Action getPreviousTreeAction() {
        return previousTreeAction;
    }

    public Action getCartoonAction() {
        return cartoonAction;
    }

    public Action getCollapseAction() {
        return collapseAction;
    }

    public Action getClearCollapsedAction() {
        return clearCollapsedAction;
    }

    public Action getMidpointRootAction() {
        return midpointRootAction;
    }

    public Action getRerootAction() {
        return rerootAction;
    }

    public Action getClearRootingAction() {
        return clearRootingAction;
    }

    public Action getIncreasingNodeOrderAction() {
        return increasingNodeOrderAction;
    }

    public Action getDecreasingNodeOrderAction() {
        return decreasingNodeOrderAction;
    }

    public Action getRotateAction() {
        return rotateAction;
    }

    public Action getClearRotationsAction() {
        return clearRotationsAction;
    }

    public Action getAnnotateAction() {
        return annotateAction;
    }

    public Action getCopyAnnotationsAction() {
        return copyAnnotationsAction;
    }

    public Action getAnnotateNodesFromTipsAction() {
        return annotateNodesFromTipsAction;
    }

    public Action getAnnotateTipsFromNodesAction() {
        return annotateTipsFromNodesAction;
    }

    public AbstractAction getClearAnnotationsAction() {
        return clearAnnotationsAction;
    }

    public AbstractAction getDefineAnnotationsAction() {
        return defineAnnotationsAction;
    }

    public Action getColourAction() {
        return colourAction;
    }

    public Action getClearColouringAction() {
        return clearColouringAction;
    }

    public Action getHilightAction() {
        return hilightAction;
    }

    public Action getClearHilightingAction() {
        return clearHilightingAction;
    }

    public Action getFindAction() {
        return findAction;
    }

    private AbstractAction importAction = new AbstractAction("Import Annotations...") {
        public void actionPerformed(ActionEvent ae) {
            doImport();
        }
    };

    private AbstractAction importCharactersAction = new AbstractAction("Import Characters...") {
        public void actionPerformed(ActionEvent ae) {
            doImportCharacters();
        }
    };

    private AbstractAction importColourSchemeAction = new AbstractAction("Import Colour Scheme...") {
        public void actionPerformed(ActionEvent ae) {
            doImportColourScheme();
        }
    };

    private AbstractAction exportColourSchemeAction = new AbstractAction("Export Colour Scheme...") {
        public void actionPerformed(ActionEvent ae) {
//            doExportColourScheme();
        }
    };

    private AbstractAction extractTreeAction = new AbstractAction("Extract Selected Tree...") {
        public void actionPerformed(ActionEvent ae) {
            doExtract();
        }
    };


    private AbstractAction exportTreesAction = new AbstractAction("Export Trees...") {
        public void actionPerformed(ActionEvent ae) {
            doExport();
        }
    };

//    private AbstractAction exportGraphicAction = new AbstractAction("Export Graphic...") {
//        public void actionPerformed(ActionEvent ae) {
//            doExportGraphic(GraphicFormat.PNG);
//        }
//    };

    private AbstractAction exportPNGGraphicAction = new AbstractAction("Export PNG...") {
        public void actionPerformed(ActionEvent ae) {
            doExportGraphic(GraphicFormat.PNG);
        }
    };

    private AbstractAction exportJPEGGraphicAction = new AbstractAction("Export JPEG...") {
        public void actionPerformed(ActionEvent ae) {
            doExportGraphic(GraphicFormat.JPEG);
        }
    };

    private AbstractAction exportSVGAction = new AbstractAction("Export SVG...") {
        public void actionPerformed(ActionEvent ae) {
            doExportGraphic(GraphicFormat.SVG);
        }
    };

    private AbstractAction exportPDFAction = new AbstractAction("Export PDF...") {
        public void actionPerformed(ActionEvent ae) {
            doExportGraphic(GraphicFormat.PDF);
        }
    };

    private AbstractAction nextTreeAction =
            new AbstractAction(NEXT_TREE) {
                public void actionPerformed(ActionEvent e){
                    treeViewer.showNextTree();
                }
            };

    private AbstractAction previousTreeAction =
            new AbstractAction(PREVIOUS_TREE) {
                public void actionPerformed(ActionEvent e){
                    treeViewer.showPreviousTree();
                }
            };

    private AbstractAction cartoonAction = new AbstractAction(CARTOON_NODE) {
        public void actionPerformed(ActionEvent e){
            cartoonSelected();
        }
    };
    private AbstractAction collapseAction = new AbstractAction(COLLAPSE_NODE) {
        public void actionPerformed(ActionEvent e){
            collapseSelected();
        }
    };
    private AbstractAction clearCollapsedAction = new AbstractAction(CLEAR_COLLAPSED) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearCollapsedNodes();
        }
    };

    private AbstractAction rerootAction = new AbstractAction(ROOT_ON_BRANCH) {
        public void actionPerformed(ActionEvent e){
            rerootTree();
        }
    };
    private AbstractAction midpointRootAction = new AbstractAction(MIDPOINT_ROOT) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleMidpointRoot();
        }
    };
    private AbstractAction clearRootingAction = new AbstractAction(CLEAR_ROOTING) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearRooting();
        }
    };


    private AbstractAction rotateAction = new AbstractAction(ROTATE_NODE) {
        public void actionPerformed(ActionEvent e){
            rotateTree();
        }
    };
    private AbstractAction clearRotationsAction = new AbstractAction(CLEAR_ROTATIONS) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearRotations();
        }
    };
    private AbstractAction increasingNodeOrderAction = new AbstractAction(INCREASING_NODE_ORDER) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleIncreasingNodeOrder();
        }
    };
    private AbstractAction decreasingNodeOrderAction = new AbstractAction(DECREASING_NODE_ORDER) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleDecreasingNodeOrder();
        }
    };

    private AbstractAction annotateAction = new AbstractAction(ANNOTATE) {
        public void actionPerformed(ActionEvent ae) {
            annotateSelected();
        }
    };

    private AbstractAction copyAnnotationsAction = new AbstractAction(COPY_ANNOTATION_VALUES) {
        public void actionPerformed(ActionEvent ae) {
            copySelectedAnnotations();
        }
    };

    private AbstractAction annotateNodesFromTipsAction = new AbstractAction(ANNOTATE_NODES_FROM_TIPS) {
        public void actionPerformed(ActionEvent ae) {
            annotateNodesFromTips();
        }
    };

    private AbstractAction annotateTipsFromNodesAction = new AbstractAction(ANNOTATE_TIPS_FROM_NODES) {
        public void actionPerformed(ActionEvent ae) {
            annotateTipsFromNodes();
        }
    };

    private AbstractAction clearAnnotationsAction = new AbstractAction(CLEAR_ANNOTATIONS) {
        public void actionPerformed(ActionEvent ae) {
            // treeViewer.clearAnnotation();
        }
    };

    private AbstractAction defineAnnotationsAction = new AbstractAction(DEFINE_ANNOTATIONS) {
        public void actionPerformed(ActionEvent ae) {
            defineAnnotations();
        }
    };

    private AbstractAction colourAction = new AbstractAction(COLOUR) {
        public void actionPerformed(ActionEvent ae) {
            colourSelected();
        }
    };

    private AbstractAction clearColouringAction = new AbstractAction(CLEAR_COLOURING) {
        public void actionPerformed(ActionEvent ae) {
            treeViewer.clearColouring();
        }
    };

    private AbstractAction hilightAction = new AbstractAction(HILIGHT) {
        public void actionPerformed(ActionEvent ae) {
            hilightSelected();
        }
    };

    private AbstractAction clearHilightingAction = new AbstractAction(CLEAR_HILIGHTING) {
        public void actionPerformed(ActionEvent ae) {
            treeViewer.clearHilighting();
        }
    };

    private AbstractAction findAction = new AbstractAction("Find...") {
        public void actionPerformed(ActionEvent ae) {
            doFind();
        }
    };

    private AbstractAction findAllAction = new AbstractAction("Find") {
        public void actionPerformed(ActionEvent ae) {
            doFindAll();
        }
    };

//	private AbstractAction findNextAction = new AbstractAction("Find Next") {
//		public void actionPerformed(ActionEvent ae) {
//			doFindNext();
//		}
//	};

    private AbstractAction getInfoAction = new AbstractAction("Get Info...") {
        public void actionPerformed(ActionEvent ae) {
            doGetInfo();
        }
    };

    private ExportTreeDialog exportTreeDialog = null;
    private FindPanel findPanel = null;
    private AnnotationDefinitionsDialog annotationDefinitionsDialog = null;
    private AnnotationDialog annotationDialog = null;
    private AnnotationDialog copyAnnotationDialog = null;
    private SelectAnnotationDialog selectAnnotationDialog = null;
}
