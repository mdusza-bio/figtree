/*
 * TreePaneSelector.java
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

import jebl.evolution.graphs.Node;
import jam.mac.Utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

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
public class TreePaneSelector implements MouseListener, MouseMotionListener, KeyListener {
    public enum SelectionMode {
        CLADE,
        NODES,
        TIPS,
        TAXA
    };

    public enum DragMode {
        SELECT,
        SCROLL
    };

    public enum ToolMode {
        SELECT,
        ROOTING,
        CARTOONING,
        COLLAPSING,
        ROTATING,
        ANNOTATING,
        COLOURING
    };

    public TreePaneSelector(TreePane treePane) {
        this.treePane = treePane;
        treePane.addMouseListener(this);
        treePane.addMouseMotionListener(this);
        treePane.addKeyListener(this);
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public DragMode getDragMode() {
        return dragMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        defaultSelectionMode = selectionMode;
        this.selectionMode = selectionMode;
    }

    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    public void setToolMode(ToolMode toolMode) {
        this.toolMode = toolMode;
        setupCursor();
    }

    public boolean isCrossHairCursor() {
        return crossHairCursor;
    }

    public void setCrossHairCursor(boolean crossHairCursor) {
        this.crossHairCursor = crossHairCursor;
    }

    private void setupCursor() {
        if (toolMode != ToolMode.SELECT) {
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
            treePane.setCrosshairShown(crossHairCursor);
        } else if (dragMode == DragMode.SELECT) {
            if (crossHairCursor) {
                treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
            } else {
                treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
            treePane.setCrosshairShown(crossHairCursor);
        } else {
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            treePane.setCrosshairShown(false);

        }
        treePane.repaint();
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (treePane.getTree() == null) {
            return;
        }

        if (toolMode == ToolMode.ROOTING) {
            Node node = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());
            if (node != null) {
                treePane.setRootLocation(node, 0.5);
            }
        } else if (toolMode == ToolMode.ROTATING) {
            Node node = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());
            treePane.rotateNode(node);
        } else if (dragMode == DragMode.SELECT) {
            boolean isCrossHairShown = treePane.isCrosshairShown();

            treePane.setCrosshairShown(false);

            Node selectedNode = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());

            boolean extendSelection = mouseEvent.isShiftDown();
            boolean invertSelection = isCommandKeyDown(mouseEvent);

            if (!extendSelection && !invertSelection) {
                treePane.clearSelection();
            }

            SelectionMode mode = selectionMode;
            if (mouseEvent.isAltDown()) {
                if (mode == SelectionMode.NODES) {
                    mode = SelectionMode.CLADE;
                } else if (mode == SelectionMode.CLADE) {
                    mode = SelectionMode.NODES;
                }
            }

            // MyFigTree (Etap 6.16): a click landing on a node label (e.g. a
            // support value) selects exactly that one node, whatever the
            // selection mode - so "Move selected label..." can target a single
            // number even when the mode is Clade.
            Node labelNode = treePane.getNodeLabelAt(mouseEvent.getPoint());
            if (labelNode != null) {
                selectedNode = labelNode;
                mode = SelectionMode.NODES;
            }

            switch (mode) {
                case NODES:
                    treePane.addSelectedNode(selectedNode, invertSelection, extendSelection);
                    break;
                case CLADE:
                    treePane.addSelectedClade(selectedNode, invertSelection, extendSelection);
                    break;
                case TIPS:
                    treePane.addSelectedTip(selectedNode, invertSelection, extendSelection);
                    break;
                case TAXA:
                    treePane.addSelectedTipLabel(selectedNode, invertSelection, extendSelection);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
            }

            treePane.setCrosshairShown(isCrossHairShown);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        treePane.requestFocus();

        // This is used for dragging in combination with mouseDragged
        // in the MouseMotionListener, below.
        dragPoint = new Point2D.Double(mouseEvent.getPoint().getX(), mouseEvent.getPoint().getY());

        // MyFigTree (Etap 6.16): pressing on a node label (a support value) arms
        // a label drag - if the mouse then moves, the label follows it instead of
        // a selection rectangle appearing. A plain click still just selects.
        draggedLabelNode = null;
        labelDragActive = false;
        if (toolMode == ToolMode.SELECT && dragMode == DragMode.SELECT
                && treePane.getTree() != null) {
            Node labelNode = treePane.getNodeLabelAt(mouseEvent.getPoint());
            if (labelNode != null) {
                draggedLabelNode = labelNode;
                // start from where the label is actually drawn now: a hand-made
                // offset if there is one, else the slide "Avoid overlap" applied
                double[] offset = TreePane.getLabelOffset(labelNode);
                if (offset == null) {
                    double[] auto = treePane.getNodeLabelAutoShift(labelNode);
                    offset = (auto == null) ? new double[] { 0.0, 0.0 }
                            : treePane.screenDeltaToLabelDelta(labelNode, auto[0], auto[1]);
                }
                labelDragBase = offset;
            }
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (treePane.getTree() == null) {
            return;
        }

        // MyFigTree (Etap 6.16): end of a label drag - write the new position
        // onto the node. Without an actual drag this falls through, so a plain
        // click on the value still selects its node as before.
        if (draggedLabelNode != null) {
            boolean dragged = labelDragActive;
            draggedLabelNode = null;
            labelDragActive = false;
            labelDragBase = null;
            if (dragged) {
                treePane.commitLabelDrag();
                treePane.setDragRectangle(null);
                return;
            }
        }

        if (dragMode == DragMode.SELECT) {
            if (treePane.getDragRectangle() != null) {
                Set<Node> selectedNodes = treePane.getNodesAt((Graphics2D) treePane.getGraphics(), treePane.getDragRectangle().getBounds());

                boolean extendSelection = mouseEvent.isShiftDown();
                boolean invertSelection = isCommandKeyDown(mouseEvent);

                if (!extendSelection && !invertSelection) {
                    treePane.clearSelection();
                }

                SelectionMode mode = selectionMode;
                if (mouseEvent.isAltDown()) {
                    if (mode == SelectionMode.NODES) {
                        mode = SelectionMode.CLADE;
                    } else if (mode == SelectionMode.CLADE) {
                        mode = SelectionMode.NODES;
                    }
                }

                for (Node selectedNode : selectedNodes) {
                    switch (mode) {
                        case NODES:
                            treePane.addSelectedNode(selectedNode, invertSelection, extendSelection);
                            break;
                        case CLADE:
                            treePane.addSelectedClade(selectedNode, invertSelection, extendSelection);
                            break;
                        case TIPS:
                            treePane.addSelectedTip(selectedNode, invertSelection, extendSelection);
                            break;
                        case TAXA:
                            treePane.addSelectedTipLabel(selectedNode, invertSelection, extendSelection);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
                    }
                }
            }
        }
        treePane.setDragRectangle(null);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
//        treePane.requestFocusInWindow();
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }
    }

    public void mouseExited(MouseEvent mouseEvent) {
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }

        // MyFigTree (Etap 6.16): show the four-arrow cursor over a support value,
        // as a hint that the number can be picked up and dragged
        if (toolMode == ToolMode.SELECT && dragMode == DragMode.SELECT
                && !crossHairCursor && treePane.getTree() != null) {
            boolean overLabel = treePane.getNodeLabelAt(mouseEvent.getPoint()) != null;
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(
                    overLabel ? java.awt.Cursor.MOVE_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * On Mac, check for the 'Command' key, otherwise use the 'Control' key
     * @param event
     * @return is it pressed
     */
    private boolean isCommandKeyDown(InputEvent event) {
        return Utils.isMacOSX() ? event.isMetaDown() : event.isControlDown();
    }

    /**
     * On Mac, check for the 'Option' key, otherwise use the 'Alt' key
     * @param event
     * @return is it pressed
     */
    private boolean isOptionKeyDown(InputEvent event) {
        return event.isAltDown();
    }

    public void mouseDragged(MouseEvent mouseEvent) {

        if (toolMode != ToolMode.SELECT || dragPoint == null) {
            return;
        }

        // MyFigTree (Etap 6.16): drag a support value with the mouse. A tiny
        // wobble within a couple of pixels still counts as a click.
        if (draggedLabelNode != null) {
            final double dxScreen = mouseEvent.getX() - dragPoint.getX();
            final double dyScreen = mouseEvent.getY() - dragPoint.getY();
            if (!labelDragActive
                    && Math.abs(dxScreen) < 3.0 && Math.abs(dyScreen) < 3.0) {
                return;
            }
            labelDragActive = true;
            double[] delta = treePane.screenDeltaToLabelDelta(draggedLabelNode, dxScreen, dyScreen);
            treePane.setLabelDragOffset(draggedLabelNode,
                    labelDragBase[0] + delta[0], labelDragBase[1] + delta[1]);
            return;
        }

        if (dragMode == DragMode.SCROLL) {
            // Calculate how far the mouse has been dragged from the point clicked in
            // mousePressed, above.
            int deltaX = (int) (mouseEvent.getX() - dragPoint.getX());
            int deltaY = (int) (mouseEvent.getY() - dragPoint.getY());

            // Get the currently visible window
            Rectangle visRect = treePane.getVisibleRect();

            // Calculate how much we need to scroll
            if (deltaX > 0) {
                deltaX = visRect.x - deltaX;
            } else {
                deltaX = visRect.x + visRect.width - deltaX;
            }

            if (deltaY > 0) {
                deltaY = visRect.y - deltaY;
            } else {
                deltaY = visRect.y + visRect.height - deltaY;
            }

            // Scroll the visible region
            Rectangle r = new Rectangle(deltaX, deltaY, 1, 1);
            treePane.scrollRectToVisible(r);
        } else {
            double x1 = Math.min(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y1 = Math.min(dragPoint.getY(), mouseEvent.getPoint().getY());
            double x2 = Math.max(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y2 = Math.max(dragPoint.getY(), mouseEvent.getPoint().getY());
            treePane.setDragRectangle(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
            treePane.scrollPointToVisible(mouseEvent.getPoint());
        }
    }

    public void keyTyped(KeyEvent event) {
    }

    public void keyPressed(KeyEvent event) {
        // MyFigTree (Etap 6.16): arrow keys move the labels of the selected
        // nodes - the precise alternative to dragging with the mouse. One press
        // is 1 pt, with Shift 10 pt. Click the value first (that selects its
        // node), then steer. With nothing selected the arrows keep their old
        // job of scrolling the view.
        double stepX = 0.0;
        double stepY = 0.0;
        switch (event.getKeyCode()) {
            case KeyEvent.VK_LEFT:  stepX = -1.0; break;
            case KeyEvent.VK_RIGHT: stepX = 1.0; break;
            case KeyEvent.VK_UP:    stepY = -1.0; break;
            case KeyEvent.VK_DOWN:  stepY = 1.0; break;
        }
        if (stepX != 0.0 || stepY != 0.0) {
            final double size = event.isShiftDown() ? 10.0 : 1.0;
            if (treePane.getTree() != null
                    && treePane.nudgeSelectedNodeLabels(stepX * size, stepY * size)) {
                event.consume();
                return;
            }
        }

        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            dragMode = DragMode.SCROLL;
        }
        if (isOptionKeyDown(event)) {
            switch (defaultSelectionMode) {
                case NODES:
                case CLADE:
                    selectionMode = SelectionMode.TAXA;
                    break;
                case TAXA:
                    selectionMode = SelectionMode.NODES;
                    break;
            }
        } else {
            selectionMode = defaultSelectionMode;
        }
        crossHairCursor = isCommandKeyDown(event);
        setupCursor();
    }

    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            dragMode = DragMode.SELECT;
        }
        crossHairCursor = isCommandKeyDown(event);
        setupCursor();
    }

    private TreePane treePane;

    private SelectionMode defaultSelectionMode = SelectionMode.NODES;
    private SelectionMode selectionMode = SelectionMode.NODES;

    private ToolMode toolMode = ToolMode.SELECT;

    private DragMode dragMode = DragMode.SELECT;
    private Point2D dragPoint = null;

    // MyFigTree (Etap 6.16): state of a node-label drag in progress
    private Node draggedLabelNode = null;
    private double[] labelDragBase = null;
    private boolean labelDragActive = false;

    private boolean crossHairCursor = false;
}
