/*
 * LabelPainterController.java
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

package figtree.treeviewer.painters;

import figtree.ui.ToolTips;

import figtree.ui.PercentFormat;
import figtree.ui.components.ColorWellButton;
import figtree.ui.RomanFormat;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.prefs.Preferences;

import figtree.treeviewer.*;
import figtree.treeviewer.decorators.*;

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
public class LabelPainterController extends AbstractController {

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    private static final String USER_SELECTION = "User Selection";

    public static final String FONT_NAME_KEY = "fontName";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String FONT_STYLE_KEY = "fontStyle";

    public static final String COLOR_ATTRIBUTE_KEY = "colorAttribute";

    public static final String NUMBER_FORMATTING_KEY = "numberFormatting";

    public static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";
    public static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";
    public static final String BOX_SIZE = "boxSize";
    public static final String TIP_PATH = "tipPath";

    // MyFigTree: display-only name formatting
    public static final String REPLACE_UNDERSCORES_KEY = "replaceUnderscores";
    public static final String HIDE_PARTS_KEY = "hideParts";
    public static final String HIDE_REGEX_KEY = "hideRegex";

    // MyFigTree: placement and support-value options (Etap 2)
    public static final String X_PADDING_KEY = "xPadding";
    public static final String Y_PADDING_KEY = "yPadding";
    public static final String POSITION_KEY = "position";
    public static final String SHOW_THRESHOLD_KEY = "showThreshold";
    public static final String SECOND_ATTRIBUTE_KEY = "secondAttribute";
    public static final String SECOND_LAYOUT_KEY = "secondLayout";
    public static final String SECOND_THRESHOLD_KEY = "secondThreshold";
    public static final String BELOW_PLACEHOLDER_KEY = "belowPlaceholder";

    // MyFigTree: pulling crowded support values apart (Etap 6.6)
    public static final String AVOID_OVERLAP_KEY = "avoidOverlap";
    public static final String LABEL_BACKING_KEY = "labelBacking";

    // MyFigTree: per-part styling of tip names (Etap 3)
    public static final String ITALIC_PARTS_KEY = "italicParts";
    public static final String ITALIC_CASE_KEY = "italicCase";
    public static final String ITALIC_BOLD_KEY = "italicBold";
    public static final String ITALIC_COLOUR_KEY = "italicColour";
    public static final String OTHER_CASE_KEY = "otherCase";
    public static final String OTHER_BOLD_KEY = "otherBold";
    public static final String OTHER_COLOUR_KEY = "otherColour";
    public static final String HIGHLIGHT_KEY = "highlight";
    public static final String HIGHLIGHT_BOLD_KEY = "highlightBold";
    public static final String HIGHLIGHT_SUPERSCRIPT_KEY = "highlightSuperscript";
    public static final String HIGHLIGHT_COLOUR_KEY = "highlightColour";

    // MyFigTree: smarter italics (Etap 6.1)
    public static final String ITALIC_MODE_KEY = "italicMode";
    public static final String NON_ITALIC_WORDS_KEY = "nonItalicWords";
    public static final String DOT_WORDS_KEY = "dotWords";
    /** files from before Etap 8.4: an "Add dot" checkbox plus a separate list of collection codes */
    private static final String OLD_ADD_RANK_DOTS_KEY = "addRankDots";
    private static final String OLD_DOT_CODES_KEY = "dotCodes";
    public static final String UPPER_CASE_NUMBER_KEY = "upperCaseIsNumber";
    public static final String FORMAT_NUMBER_KEY = "formatCollectionNumber";

    private static final String NO_COLOUR = "none";

    // The defaults if there is nothing in the preferences
    public static String DEFAULT_FONT_NAME = "sansserif";
    public static int DEFAULT_FONT_SIZE = 8;
    public static int DEFAULT_FONT_STYLE = Font.PLAIN;

    public static String DECIMAL_NUMBER_FORMATTING = "#.####";
    public static String SCIENTIFIC_NUMBER_FORMATTING = "0.###E0";

    public static int DEFAULT_SIGNIFICANT_DIGITS = 2;
    public static String DEFAULT_NUMBER_FORMATTING = DECIMAL_NUMBER_FORMATTING;

    public LabelPainterController(String title, String key, final LabelPainter labelPainter,
                                  final JFrame frame,
                                  final AttributeColourController colourController,
                                  final TreeViewer treeViewer) {

        this.title = title;
        this.key = key;
        this.labelPainter = labelPainter;
        this.intent = labelPainter.getIntent();

        userLabelDecorator = new AttributableDecorator();
        userLabelDecorator.setPaintAttributeName("!color");
        userLabelDecorator.setFontAttributeName("!font");
        labelPainter.setTextDecorator(userLabelDecorator);

        final String defaultFontName = PREFS.get(key + "." + FONT_NAME_KEY, DEFAULT_FONT_NAME);
        final int defaultFontStyle = PREFS.getInt(key + "." + FONT_STYLE_KEY, DEFAULT_FONT_STYLE);
        final int defaultFontSize = PREFS.getInt(key + "." + FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        final int defaultSignificantDigits = PREFS.getInt(key + "." + SIGNIFICANT_DIGITS_KEY, DEFAULT_SIGNIFICANT_DIGITS);
        final String defaultNumberFormatting = PREFS.get(key + "." + NUMBER_FORMATTING_KEY, DEFAULT_NUMBER_FORMATTING);

        labelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
        labelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(labelPainter.isVisible());

        displayAttributeCombo = new JComboBox(new String[] { "No attributes" });
        new AttributeComboHelper(displayAttributeCombo, treeViewer, intent).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                String attribute = (String) displayAttributeCombo.getSelectedItem();
                labelPainter.setDisplayAttribute(attribute);
                updateNameOptions();
            }
        });

        colourAttributeCombo = new JComboBox(new String[] { "No attributes" });
        new AttributeComboHelper(colourAttributeCombo, treeViewer, "User selection", intent).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                String attribute = (String) colourAttributeCombo.getSelectedItem();
                setupLabelDecorator();
            }
        });

        final JButton setupColourButton = new JButton("Colour");

        this.colourController = colourController;
        colourController.setupControls(colourAttributeCombo, setupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupLabelDecorator();
            }
        });

        final JButton fontButton = new JButton(new AbstractAction("Font") {
            public void actionPerformed(ActionEvent e) {
                final Font font = labelPainter.getFont();
                if (fontDialog == null) {
                    fontDialog = new FontDialog(frame);
                }
                int result = fontDialog.showDialog(font);
                if (result != JOptionPane.CANCEL_OPTION) {
                    labelPainter.setFont(fontDialog.getFont());
                }
            }
        });

        Font font = labelPainter.getFont();
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 72, 1));

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = labelPainter.getFont().deriveFont(size);
                labelPainter.setFont(font);
            }
        });

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        ControllerOptionsPanel.setComponentLook(setupColourButton);
        ControllerOptionsPanel.setComponentLook(fontButton);
        panel.add(setupColourButton);
        panel.add(fontButton);

        NumberFormat format = labelPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();

        numericalFormatCombo = new JComboBox(new String[] { "Decimal", "Scientific", "Percent", "Roman"});
        numericalFormatCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String formatType = (String) numericalFormatCombo.getSelectedItem();
                final int digits = (Integer) digitsSpinner.getValue();
                NumberFormat format = null;
                if (formatType.equals("Decimal")) {
                    format = new DecimalFormat(DECIMAL_NUMBER_FORMATTING);
                } else if (formatType.equals("Scientific")) {
                    format = new DecimalFormat(SCIENTIFIC_NUMBER_FORMATTING);
                } else if (formatType.equals("Percent")) {
                    format = new PercentFormat();
                } else if (formatType.equals("Roman")) {
                    format = new RomanFormat();
                }
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, defaultSignificantDigits, 14, 1));
        digitsSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = labelPainter.getNumberFormat();
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        if (intent == LabelPainter.PainterIntent.TIP) {

            boxSizeSpinner = new JSpinner(new SpinnerNumberModel(digits, defaultSignificantDigits, 99, 1));
            boxSizeSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final int size = (Integer) boxSizeSpinner.getValue();
                    labelPainter.setBoxSize(size);
                }
            });

            tipPathCheck = new JCheckBox("Show tip callouts");
            tipPathCheck.setToolTipText("Dotted lines from the tips to the aligned names");
            tipPathCheck.setSelected(true);
            tipPathCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean tipPath = tipPathCheck.isSelected();
                    treeViewer.setShowingTipCallouts(tipPathCheck.isSelected());
                }
            });
        } else {
            boxSizeSpinner = null;
            tipPathCheck = null;
        }

        // MyFigTree: display-only name formatting controls
        replaceUnderscoresCheck = new JCheckBox("Replace '_' with space");
        replaceUnderscoresCheck.setSelected(intent == LabelPainter.PainterIntent.TIP);
        labelPainter.setReplaceUnderscores(replaceUnderscoresCheck.isSelected());
        replaceUnderscoresCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                labelPainter.setReplaceUnderscores(replaceUnderscoresCheck.isSelected());
            }
        });

        hidePartsText = new JTextField("", 10);
        hidePartsText.setToolTipText("e.g. _contig1, HOLOTYPE - cut wherever it occurs");
        hidePartsText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
            public void removeUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
            public void changedUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
        });

        hideRegexText = new JTextField("", 10);
        hideRegexText.setToolTipText("e.g. _new$ cuts _new at the end of the name only");
        hideRegexText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
            public void removeUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
            public void changedUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
        });

        // MyFigTree: placement and support-value controls (node and branch labels only)
        if (intent == LabelPainter.PainterIntent.NODE || intent == LabelPainter.PainterIntent.BRANCH) {
            xPaddingSpinner = new JSpinner(new SpinnerNumberModel(0.0, -500.0, 500.0, 1.0));
            yPaddingSpinner = new JSpinner(new SpinnerNumberModel(0.0, -500.0, 500.0, 1.0));
            ChangeListener paddingListener = new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    labelPainter.setPadding(
                            ((Number) xPaddingSpinner.getValue()).doubleValue(),
                            ((Number) yPaddingSpinner.getValue()).doubleValue());
                }
            };
            xPaddingSpinner.addChangeListener(paddingListener);
            yPaddingSpinner.addChangeListener(paddingListener);

            if (intent == LabelPainter.PainterIntent.NODE) {
                positionCombo = new JComboBox(LabelPainter.LabelPosition.values());
                positionCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        labelPainter.setLabelPosition((LabelPainter.LabelPosition) positionCombo.getSelectedItem());
                    }
                });

                // MyFigTree (Etap 6.6)
                avoidOverlapCheck = new JCheckBox("Avoid overlap");
                avoidOverlapCheck.setToolTipText("Spreads overlapping support values. Not perfect - the rest can be dragged with the mouse or arrow keys.");
                avoidOverlapCheck.setSelected(labelPainter.isAvoidOverlap());
                avoidOverlapCheck.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        labelPainter.setAvoidOverlap(avoidOverlapCheck.isSelected());
                    }
                });

                labelBackingCheck = new JCheckBox("White backing");
                labelBackingCheck.setSelected(labelPainter.isLabelBacking());
                labelBackingCheck.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        labelPainter.setLabelBacking(labelBackingCheck.isSelected());
                    }
                });
            } else {
                positionCombo = null;
                avoidOverlapCheck = null;
                labelBackingCheck = null;
            }

            thresholdText = new JTextField("", 6);
            thresholdText.setToolTipText("e.g. 70");
            thresholdText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyThreshold(); }
                public void removeUpdate(DocumentEvent e) { applyThreshold(); }
                public void changedUpdate(DocumentEvent e) { applyThreshold(); }
            });

            secondAttributeCombo = new JComboBox(new String[] { BasicLabelPainter.NONE });
            new AttributeComboHelper(secondAttributeCombo, treeViewer, BasicLabelPainter.NONE, intent).addListener(new AttributeComboHelperListener() {
                @Override
                public void attributeComboChanged() {
                    labelPainter.setSecondAttribute((String) secondAttributeCombo.getSelectedItem());
                }
            });

            // MyFigTree (Etap 7.11): the second value's own threshold, and the dash of "-/1"
            secondThresholdText = new JTextField("", 6);
            secondThresholdText.setToolTipText("e.g. 0.95");
            placeholderText = new JTextField("", 6);
            placeholderText.setToolTipText("e.g. - gives -/1 or 75/-");
            DocumentListener secondListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applySecondThreshold(); }
                public void removeUpdate(DocumentEvent e) { applySecondThreshold(); }
                public void changedUpdate(DocumentEvent e) { applySecondThreshold(); }
            };
            secondThresholdText.getDocument().addDocumentListener(secondListener);
            placeholderText.getDocument().addDocumentListener(secondListener);

            secondLayoutCombo = new JComboBox(LabelPainter.SecondValueLayout.values());
            secondLayoutCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    labelPainter.setSecondValueLayout((LabelPainter.SecondValueLayout) secondLayoutCombo.getSelectedItem());
                }
            });
        } else {
            xPaddingSpinner = null;
            yPaddingSpinner = null;
            positionCombo = null;
            avoidOverlapCheck = null;
            labelBackingCheck = null;
            thresholdText = null;
            secondAttributeCombo = null;
            secondLayoutCombo = null;
            secondThresholdText = null;
            placeholderText = null;
        }

        // MyFigTree: per-part styling of tip names (Etap 3)
        final LabelStyle style = labelPainter.getLabelStyle();
        if (intent == LabelPainter.PainterIntent.TIP && style != null) {
            italicPartsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
            italicPartsSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    style.setItalicParts((Integer) italicPartsSpinner.getValue());
                    labelPainter.labelStyleChanged();
                }
            });

            // MyFigTree (Etap 6.1): italics that stop at the collection number
            italicModeCombo = new JComboBox(LabelStyle.ItalicMode.values());
            italicModeCombo.setSelectedItem(style.getItalicMode());
            italicModeCombo.setToolTipText("Until first number: italics up to the collection number");

            nonItalicWordsText = new JTextField(style.getNonItalicWords(), 10);
            nonItalicWordsText.setToolTipText("e.g. var subsp sp cf aff x");
            nonItalicWordsText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyItalicMode(); }
                public void removeUpdate(DocumentEvent e) { applyItalicMode(); }
                public void changedUpdate(DocumentEvent e) { applyItalicMode(); }
            });

            upperCaseNumberCheck = new JCheckBox("ALL CAPS");
            upperCaseNumberCheck.setToolTipText("A part in capitals (KRAM, BR) also ends the italics");
            upperCaseNumberCheck.setSelected(style.isUpperCaseIsNumber());
            formatNumberCheck = new JCheckBox("Join number");
            formatNumberCheck.setToolTipText("<html>KRAM M 1156 &rarr; KRAM M-1156, UK100 1b &rarr; UK100-1b</html>");
            formatNumberCheck.setSelected(style.isFormatCollectionNumber());

            // MyFigTree (Etap 8.4): one list of abbreviations that get a dot, in the
            // name (var, sp) and in the collection number (CA) alike
            dotWordsText = new JTextField(style.getDotWords(), 10);
            dotWordsText.setToolTipText("e.g. var sp f");
            dotWordsText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyItalicMode(); }
                public void removeUpdate(DocumentEvent e) { applyItalicMode(); }
                public void changedUpdate(DocumentEvent e) { applyItalicMode(); }
            });

            styleTipsButton = new JButton("Style selected tips...");
            styleTipsButton.putClientProperty("JComponent.sizeVariant", "small");
            styleTipsButton.setToolTipText("Italics and bold by hand for the selected tips");
            styleTipsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    styleSelectedTips(treeViewer);
                }
            });

            ActionListener italicModeListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyItalicMode();
                }
            };
            italicModeCombo.addActionListener(italicModeListener);
            upperCaseNumberCheck.addActionListener(italicModeListener);
            formatNumberCheck.addActionListener(italicModeListener);
            updateItalicEnabled();

            italicCaseCombo = new JComboBox(LabelStyle.Case.values());
            otherCaseCombo = new JComboBox(LabelStyle.Case.values());
            italicBoldCheck = new JCheckBox("Bold");
            otherBoldCheck = new JCheckBox("Bold");
            italicColourButton = new OptionalColourButton("Colour of italic parts");
            otherColourButton = new OptionalColourButton("Colour of other parts");

            ActionListener groupListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyGroupStyles();
                }
            };
            italicCaseCombo.addActionListener(groupListener);
            otherCaseCombo.addActionListener(groupListener);
            italicBoldCheck.addActionListener(groupListener);
            otherBoldCheck.addActionListener(groupListener);
            italicColourButton.addActionListener(groupListener);
            otherColourButton.addActionListener(groupListener);

            highlightText = new JTextField("", 10);
            highlightText.setToolTipText("e.g. HOLOTYPE PARATYPE");
            highlightText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyHighlight(); }
                public void removeUpdate(DocumentEvent e) { applyHighlight(); }
                public void changedUpdate(DocumentEvent e) { applyHighlight(); }
            });
            highlightBoldCheck = new JCheckBox("Bold");
            highlightColourButton = new OptionalColourButton("Highlight colour");
            ActionListener highlightListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyHighlight();
                }
            };
            highlightBoldCheck.addActionListener(highlightListener);
            highlightColourButton.addActionListener(highlightListener);
            // MyFigTree (Etap 7.12): the mycologists' way of marking a type - a small raised T
            highlightSuperCheck = new JCheckBox("superscript T");
            highlightSuperCheck.setOpaque(false);
            highlightSuperCheck.setToolTipText("A small raised T after the highlighted names");
            highlightSuperCheck.addActionListener(highlightListener);

        } else {
            italicPartsSpinner = null;
            italicModeCombo = null;
            styleTipsButton = null;
            nonItalicWordsText = null;
            upperCaseNumberCheck = null;
            formatNumberCheck = null;
            dotWordsText = null;
            italicCaseCombo = null;
            otherCaseCombo = null;
            italicBoldCheck = null;
            otherBoldCheck = null;
            italicColourButton = null;
            otherColourButton = null;
            highlightText = null;
            highlightBoldCheck = null;
            highlightColourButton = null;
            highlightSuperCheck = null;
        }

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Setup:", panel);
        final JLabel label5 = optionsPanel.addComponentWithLabel("Format:", numericalFormatCombo);
        final JLabel label6 = optionsPanel.addComponentWithLabel("Sig. Digits:", digitsSpinner);

        JLabel label7 = null;
        if (intent == LabelPainter.PainterIntent.TIP) {
            label7 = optionsPanel.addComponentWithLabel("Box Size:", boxSizeSpinner);
            optionsPanel.addComponent(tipPathCheck, true);
        }
        // MyFigTree: added with an (empty) label of its own rather than through
        // addComponent(), which puts an anonymous spacer panel in the label column.
        // That spacer stayed visible when the checkbox was hidden for node/branch
        // labels, so it held the start of a row and the next visible row - the
        // "Position:" label and its combo - got pushed one column to the right.
        replaceUnderscoresLabel = optionsPanel.addComponentWithLabel("", replaceUnderscoresCheck, true);
        final JLabel label8 = optionsPanel.addComponentWithLabel("Hide parts:", hidePartsText);
        final JLabel label9 = optionsPanel.addComponentWithLabel("Hide regex:", hideRegexText);
        hidePartsLabel = ToolTips.copy(label8, hidePartsText);
        hideRegexLabel = ToolTips.copy(label9, hideRegexText);
        updateNameOptions();

        if (italicPartsSpinner != null) {
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Italic mode:", italicModeCombo), italicModeCombo));
            addComponent(italicModeCombo);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Italic first N parts:", italicPartsSpinner), italicPartsSpinner));
            addComponent(italicPartsSpinner);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Not italic words:", nonItalicWordsText), nonItalicWordsText));
            addComponent(nonItalicWordsText);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Dot after:", dotWordsText), dotWordsText));
            addComponent(dotWordsText);
            final JLabel collectionLabel = optionsPanel.addComponentWithLabel("Collection number:", checkPanel(upperCaseNumberCheck, formatNumberCheck));
            collectionLabel.setToolTipText("The first part of the name with a digit; the italics stop there");
            addComponent(collectionLabel);
            addComponent(upperCaseNumberCheck);
            addComponent(formatNumberCheck);
            optionsPanel.addSpanningComponent(styleTipsButton);
            addComponent(styleTipsButton);
            addComponent(optionsPanel.addComponentWithLabel("Case (italic parts):", italicCaseCombo));
            addComponent(italicCaseCombo);
            addComponent(optionsPanel.addComponentWithLabel("Italic parts style:", stylePanel(italicBoldCheck, italicColourButton)));
            addComponent(italicBoldCheck);
            addComponent(italicColourButton);
            addComponent(optionsPanel.addComponentWithLabel("Case (other parts):", otherCaseCombo));
            addComponent(otherCaseCombo);
            addComponent(optionsPanel.addComponentWithLabel("Other parts style:", stylePanel(otherBoldCheck, otherColourButton)));
            addComponent(otherBoldCheck);
            addComponent(otherColourButton);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Highlight names containing:", highlightText), highlightText));
            addComponent(highlightText);
            addComponent(optionsPanel.addComponentWithLabel("Highlight style:", stylePanel(highlightBoldCheck, highlightColourButton)));
            addComponent(highlightBoldCheck);
            addComponent(highlightColourButton);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Type mark:", highlightSuperCheck), highlightSuperCheck));
            addComponent(highlightSuperCheck);
        }

        if (xPaddingSpinner != null) {
            if (positionCombo != null) {
                addComponent(optionsPanel.addComponentWithLabel("Position:", positionCombo));
                addComponent(positionCombo);
            }
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Offset X:", xPaddingSpinner), xPaddingSpinner));
            addComponent(xPaddingSpinner);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Offset Y:", yPaddingSpinner), yPaddingSpinner));
            addComponent(yPaddingSpinner);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Show only if >=:", thresholdText), thresholdText));
            addComponent(thresholdText);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Second value:", secondAttributeCombo), secondAttributeCombo));
            addComponent(secondAttributeCombo);
            addComponent(optionsPanel.addComponentWithLabel("Layout:", secondLayoutCombo));
            addComponent(secondLayoutCombo);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Second only if >=:", secondThresholdText), secondThresholdText));
            addComponent(secondThresholdText);
            addComponent(ToolTips.copy(optionsPanel.addComponentWithLabel("Too low shows as:", placeholderText), placeholderText));
            addComponent(placeholderText);
            if (avoidOverlapCheck != null) {
                addComponent(optionsPanel.addComponentWithLabel("Crowded values:",
                        checkPanel(avoidOverlapCheck, labelBackingCheck)));
                addComponent(avoidOverlapCheck);
                addComponent(labelBackingCheck);
            }
        }

        addComponent(label1);
        addComponent(displayAttributeCombo);
        addComponent(label2);
        addComponent(colourAttributeCombo);
        addComponent(label3);
        addComponent(setupColourButton);
        addComponent(fontButton);
        addComponent(label4);
        addComponent(fontSizeSpinner);
        addComponent(label5);
        addComponent(numericalFormatCombo);
        addComponent(label6);
        addComponent(digitsSpinner);
        if (intent == LabelPainter.PainterIntent.TIP) {
            addComponent(label7);
            addComponent(boxSizeSpinner);
            addComponent(tipPathCheck);
        }
        addComponent(replaceUnderscoresCheck);
        addComponent(label8);
        addComponent(hidePartsText);
        addComponent(label9);
        addComponent(hideRegexText);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                labelPainter.setVisible(titleCheckBox.isSelected());
            }
        });
    }

    private static JPanel stylePanel(JCheckBox boldCheck, OptionalColourButton colourButton) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        boldCheck.setOpaque(false);
        panel.add(boldCheck);
        panel.add(Box.createHorizontalStrut(6));
        panel.add(colourButton);
        return panel;
    }

    private static JPanel checkPanel(JCheckBox check1, JCheckBox check2) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        check1.setOpaque(false);
        check2.setOpaque(false);
        panel.add(check1);
        panel.add(Box.createHorizontalStrut(6));
        panel.add(check2);
        return panel;
    }

    /**
     * MyFigTree (Etap 8.4): a file saved before the "Dot after" list existed had
     * an "Add dot" checkbox (a dot after every short word of the not-italic list,
     * except the hybrid "x") and a separate list of collection codes. Both are
     * folded into the one list, so an old figure draws exactly as it did.
     */
    private String dotWordsFromOldSettings(Map<String, Object> settings) {
        StringBuilder words = new StringBuilder();
        if (Boolean.TRUE.equals(settings.get(key + "." + OLD_ADD_RANK_DOTS_KEY))) {
            for (String w : nonItalicWordsText.getText().split("[\\s,]+")) {
                String word = w.trim();
                while (word.endsWith(".")) {
                    word = word.substring(0, word.length() - 1);
                }
                if (word.length() > 0 && word.length() <= 5
                        && !word.equalsIgnoreCase("x") && !word.equals("\u00d7")) {
                    if (words.length() > 0) {
                        words.append(' ');
                    }
                    words.append(word);
                }
            }
        }
        Object codes = settings.get(key + "." + OLD_DOT_CODES_KEY);
        if (codes != null && codes.toString().trim().length() > 0) {
            if (words.length() > 0) {
                words.append(' ');
            }
            words.append(codes.toString().trim());
        }
        return words.toString();
    }

    /** MyFigTree (Etap 6.1) */
    private void applyItalicMode() {
        LabelStyle style = labelPainter.getLabelStyle();
        style.setItalicMode((LabelStyle.ItalicMode) italicModeCombo.getSelectedItem());
        style.setNonItalicWords(nonItalicWordsText.getText());
        style.setUpperCaseIsNumber(upperCaseNumberCheck.isSelected());
        style.setFormatCollectionNumber(formatNumberCheck.isSelected());
        style.setDotWords(dotWordsText.getText());
        updateItalicEnabled();
        labelPainter.labelStyleChanged();
    }

    /** greys out the controls that the chosen italic mode does not use */
    private void updateItalicEnabled() {
        LabelStyle.ItalicMode mode = (LabelStyle.ItalicMode) italicModeCombo.getSelectedItem();
        italicPartsSpinner.setEnabled(mode == LabelStyle.ItalicMode.FIRST_N);
        boolean untilNumber = (mode == LabelStyle.ItalicMode.UNTIL_NUMBER);
        nonItalicWordsText.setEnabled(untilNumber);
        upperCaseNumberCheck.setEnabled(untilNumber);
        formatNumberCheck.setEnabled(untilNumber);
    }

    /**
     * MyFigTree (Etap 6.11): italics and bold set by hand for the tips that are
     * selected in the tree. The escape hatch for names the general rules get
     * wrong - e.g. "Trichia sordida holotypus", where the whole name goes italic
     * but "holotypus" should stay upright. Saved on the tip itself (attributes
     * !labelItalic / !labelBold), so it travels with the tree file.
     */
    private void styleSelectedTips(TreeViewer treeViewer) {

        if (!(treeViewer instanceof ExtendedTreeViewer) || !(labelPainter instanceof BasicLabelPainter)) {
            return;
        }

        java.util.Set<Node> tips = ((ExtendedTreeViewer) treeViewer).getSelectedTipNodes();
        if (tips.isEmpty()) {
            JOptionPane.showMessageDialog(optionsPanel,
                    "Nothing is selected in the tree.\n\n" +
                            "Click the tip you want to fix first (shift-click for more\n" +
                            "than one), then press this button again.",
                    "Style Selected Tips",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Node first = tips.iterator().next();
        LabelStyle style = labelPainter.getLabelStyle();
        Tree tree = treeViewer.getCurrentTree();
        Taxon taxon = (tree == null ? null : tree.getTaxon(first));
        String rawName = (String) first.getAttribute("!name");
        if (rawName == null && taxon != null) {
            rawName = (String) taxon.getAttribute("!name");
            if (rawName == null) {
                rawName = taxon.getName();
            }
        }
        if (rawName == null) {
            return;
        }
        String[] parts = ((BasicLabelPainter) labelPainter).getLabelFormatter().getParts(rawName);
        java.util.List<String> pieces = style.displayItems(parts);
        if (pieces.isEmpty()) {
            return;
        }

        boolean[] current = BasicLabelPainter.parseItalicOverride(
                first.getAttribute(BasicLabelPainter.ITALIC_OVERRIDE_ATTRIBUTE));
        if (current == null) {
            current = style.displayItalic(parts);
        }

        OptionsPanel dialogPanel = new OptionsPanel(6, 6);
        if (tips.size() > 1) {
            dialogPanel.addSpanningComponent(new JLabel("<html>The pieces below come from <b>"
                    + rawName + "</b>;<br>the same setting goes on all "
                    + tips.size() + " selected tips.</html>"));
        }
        JCheckBox[] checks = new JCheckBox[pieces.size()];
        for (int i = 0; i < pieces.size(); i++) {
            checks[i] = new JCheckBox("italic", i < current.length && current[i]);
            checks[i].setOpaque(false);
            dialogPanel.addComponentWithLabel(pieces.get(i) + ":", checks[i]);
        }
        JCheckBox boldCheck = new JCheckBox("bold whole label",
                BasicLabelPainter.isTrue(first.getAttribute(BasicLabelPainter.BOLD_OVERRIDE_ATTRIBUTE)));
        boldCheck.setOpaque(false);
        dialogPanel.addSpanningComponent(boldCheck);
        JCheckBox typeCheck = new JCheckBox("type specimen (superscript T)",
                BasicLabelPainter.isTrue(first.getAttribute(BasicLabelPainter.TYPE_OVERRIDE_ATTRIBUTE)));
        typeCheck.setOpaque(false);
        dialogPanel.addSpanningComponent(typeCheck);

        Object[] buttons = {"Apply", "Back to rules", "Cancel"};
        int result = JOptionPane.showOptionDialog(optionsPanel, dialogPanel,
                tips.size() == 1 ? "Style Selected Tip" : "Style Selected Tips",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                buttons, buttons[0]);

        if (result == 0) {
            boolean[] mask = new boolean[checks.length];
            for (int i = 0; i < checks.length; i++) {
                mask[i] = checks[i].isSelected();
            }
            String text = BasicLabelPainter.italicOverrideText(mask);
            for (Node tip : tips) {
                tip.setAttribute(BasicLabelPainter.ITALIC_OVERRIDE_ATTRIBUTE, text);
                if (boldCheck.isSelected()) {
                    tip.setAttribute(BasicLabelPainter.BOLD_OVERRIDE_ATTRIBUTE, "true");
                } else {
                    tip.removeAttribute(BasicLabelPainter.BOLD_OVERRIDE_ATTRIBUTE);
                }
                if (typeCheck.isSelected()) {
                    tip.setAttribute(BasicLabelPainter.TYPE_OVERRIDE_ATTRIBUTE, "true");
                } else {
                    tip.removeAttribute(BasicLabelPainter.TYPE_OVERRIDE_ATTRIBUTE);
                }
            }
        } else if (result == 1) {
            // "Back to rules" - forget the hand-made setting on these tips
            for (Node tip : tips) {
                tip.removeAttribute(BasicLabelPainter.ITALIC_OVERRIDE_ATTRIBUTE);
                tip.removeAttribute(BasicLabelPainter.BOLD_OVERRIDE_ATTRIBUTE);
                tip.removeAttribute(BasicLabelPainter.TYPE_OVERRIDE_ATTRIBUTE);
            }
        } else {
            return;
        }

        labelPainter.labelStyleChanged();
    }

    private void applyGroupStyles() {
        LabelStyle style = labelPainter.getLabelStyle();
        LabelStyle.PartStyle italic = style.getItalicGroup();
        italic.caseMode = (LabelStyle.Case) italicCaseCombo.getSelectedItem();
        italic.bold = italicBoldCheck.isSelected();
        italic.colour = italicColourButton.getColour();
        LabelStyle.PartStyle other = style.getOtherGroup();
        other.caseMode = (LabelStyle.Case) otherCaseCombo.getSelectedItem();
        other.bold = otherBoldCheck.isSelected();
        other.colour = otherColourButton.getColour();
        labelPainter.labelStyleChanged();
    }

    private void applyHighlight() {
        LabelStyle style = labelPainter.getLabelStyle();
        style.setHighlight(highlightText.getText());
        style.setHighlightBold(highlightBoldCheck.isSelected());
        style.setHighlightSuperscript(highlightSuperCheck.isSelected());
        style.setHighlightColour(highlightColourButton.getColour());
        labelPainter.labelStyleChanged();
    }

    private static Object colourSetting(Color colour) {
        return colour == null ? NO_COLOUR : colour;
    }

    private static Color colourFromSetting(Object value) {
        return value instanceof Color ? (Color) value : null;
    }

    /**
     * A colour well that may also be "unset" (null = use the normal label colour),
     * with a small "x" button to clear it. Fires an ActionEvent on every change.
     */
    private static class OptionalColourButton extends JPanel {
        OptionalColourButton(String title) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            well = new ColorWellButton(null, title) {
                public void setSelectedColor(Color color) {
                    super.setSelectedColor(color);
                    fireChanged();
                }
            };
            ControllerOptionsPanel.setComponentLook(well);
            JButton clearButton = new JButton("x");
            ControllerOptionsPanel.setComponentLook(clearButton);
            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setColour(null);
                }
            });
            add(well);
            add(clearButton);
        }

        Color getColour() {
            return well.getSelectedColor();
        }

        void setColour(Color colour) {
            well.setSelectedColor(colour);
        }

        void addActionListener(ActionListener listener) {
            listeners.add(listener);
        }

        private void fireChanged() {
            if (listeners == null) return; // called from the super constructor
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "colour");
            for (ActionListener listener : listeners) {
                listener.actionPerformed(event);
            }
        }

        private final ColorWellButton well;
        private final java.util.List<ActionListener> listeners = new ArrayList<ActionListener>();
    }

    /**
     * MyFigTree (Etap 6.20): Replace '_', Hide parts and Hide regex only ever change names. In the
     * node and branch label panels the values shown are almost always numbers (support), where
     * these did nothing at all - so there they appear only while Display is set to Names.
     */
    private void updateNameOptions() {
        if (intent == LabelPainter.PainterIntent.TIP || hidePartsLabel == null) {
            return;
        }
        boolean names = LabelPainter.NAMES.equals(displayAttributeCombo.getSelectedItem());
        replaceUnderscoresLabel.setVisible(names);
        replaceUnderscoresCheck.setVisible(names);
        hidePartsLabel.setVisible(names);
        hidePartsText.setVisible(names);
        hideRegexLabel.setVisible(names);
        hideRegexText.setVisible(names);
        optionsPanel.revalidate();
    }

    private void applySecondThreshold() {
        String text = secondThresholdText.getText().trim().replace(',', '.');
        Double threshold = null;
        if (text.length() > 0) {
            try {
                threshold = Double.valueOf(text);
            } catch (NumberFormatException e) {
                threshold = null;
            }
        }
        labelPainter.setSecondThreshold(threshold);
        labelPainter.setBelowPlaceholder(placeholderText.getText());
    }

    private void applyThreshold() {
        String text = thresholdText.getText().trim().replace(',', '.');
        Double threshold = null;
        if (text.length() > 0) {
            try {
                threshold = Double.valueOf(text);
            } catch (NumberFormatException e) {
                threshold = null;
            }
        }
        labelPainter.setShowThreshold(threshold);
    }

    private void setupLabelDecorator() {

        Decorator colourDecorator = colourController.getColourDecorator(colourAttributeCombo, userLabelDecorator);

        CompoundDecorator compoundDecorator = new CompoundDecorator();
        compoundDecorator.addDecorator(colourDecorator);

        AttributableDecorator fontDecorator = new AttributableDecorator();
        userLabelDecorator.setFontAttributeName("!font");
        compoundDecorator.addDecorator(fontDecorator);

        labelPainter.setTextDecorator(compoundDecorator);
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

    public void setSettings(Map<String,Object> settings) {
        titleCheckBox.setSelected((Boolean) settings.get(key + "." + IS_SHOWN));
        displayAttributeCombo.setSelectedItem(settings.get(key + "." + DISPLAY_ATTRIBUTE_KEY));
        colourAttributeCombo.setSelectedItem(settings.get(key + "." + COLOR_ATTRIBUTE_KEY));
        String name = (String)settings.get(key + "." + FONT_NAME_KEY);
        int size = ((Number)settings.get(key + "." + FONT_SIZE_KEY)).intValue();
        int style = (Integer)settings.get(key + "." + FONT_STYLE_KEY);
        labelPainter.setFont(new Font(name, style, size));
        digitsSpinner.setValue((Integer) settings.get(key + "." + SIGNIFICANT_DIGITS_KEY));
        if (intent == LabelPainter.PainterIntent.TIP) {
            boxSizeSpinner.setValue((Integer) settings.get(key + "." + BOX_SIZE));
            tipPathCheck.setSelected((Boolean) settings.get(key + "." + TIP_PATH));
        }
        // MyFigTree keys: may be absent in files saved by the original FigTree
        Object replace = settings.get(key + "." + REPLACE_UNDERSCORES_KEY);
        if (replace instanceof Boolean) {
            replaceUnderscoresCheck.setSelected((Boolean) replace);
        }
        Object hideParts = settings.get(key + "." + HIDE_PARTS_KEY);
        if (hideParts != null) {
            hidePartsText.setText(hideParts.toString());
        }
        Object hideRegex = settings.get(key + "." + HIDE_REGEX_KEY);
        if (hideRegex != null) {
            hideRegexText.setText(hideRegex.toString());
        }
        if (xPaddingSpinner != null) {
            Object xPad = settings.get(key + "." + X_PADDING_KEY);
            if (xPad instanceof Number) {
                xPaddingSpinner.setValue(((Number) xPad).doubleValue());
            }
            Object yPad = settings.get(key + "." + Y_PADDING_KEY);
            if (yPad instanceof Number) {
                yPaddingSpinner.setValue(((Number) yPad).doubleValue());
            }
            Object position = settings.get(key + "." + POSITION_KEY);
            if (positionCombo != null && position != null) {
                positionCombo.setSelectedItem(LabelPainter.LabelPosition.fromString(position.toString()));
            }
            Object threshold = settings.get(key + "." + SHOW_THRESHOLD_KEY);
            if (threshold != null) {
                thresholdText.setText(threshold.toString());
            }
            Object second = settings.get(key + "." + SECOND_ATTRIBUTE_KEY);
            if (second != null) {
                secondAttributeCombo.setSelectedItem(second.toString());
            }
            Object layout = settings.get(key + "." + SECOND_LAYOUT_KEY);
            if (layout != null) {
                secondLayoutCombo.setSelectedItem(LabelPainter.SecondValueLayout.fromString(layout.toString()));
            }
            // absent in files saved before 7.11: no second threshold, no placeholder - they look as they did
            Object secondThreshold = settings.get(key + "." + SECOND_THRESHOLD_KEY);
            secondThresholdText.setText(secondThreshold == null ? "" : secondThreshold.toString());
            Object placeholder = settings.get(key + "." + BELOW_PLACEHOLDER_KEY);
            placeholderText.setText(placeholder == null ? "" : placeholder.toString());
            Object avoid = settings.get(key + "." + AVOID_OVERLAP_KEY);
            if (avoidOverlapCheck != null && avoid instanceof Boolean) {
                avoidOverlapCheck.setSelected((Boolean) avoid);
                labelPainter.setAvoidOverlap((Boolean) avoid);
            }
            Object backing = settings.get(key + "." + LABEL_BACKING_KEY);
            if (labelBackingCheck != null && backing instanceof Boolean) {
                labelBackingCheck.setSelected((Boolean) backing);
                labelPainter.setLabelBacking((Boolean) backing);
            }
        }
        if (italicPartsSpinner != null) {
            setStyleSettings(settings);
        }
    }

    private void setStyleSettings(Map<String, Object> settings) {
        // MyFigTree (Etap 6.1): files saved before this option have no mode key,
        // and they meant "italic first N parts"
        Object mode = settings.get(key + "." + ITALIC_MODE_KEY);
        italicModeCombo.setSelectedItem(mode == null
                ? LabelStyle.ItalicMode.FIRST_N
                : LabelStyle.ItalicMode.fromString(mode.toString()));
        Object words = settings.get(key + "." + NON_ITALIC_WORDS_KEY);
        if (words != null) {
            nonItalicWordsText.setText(words.toString());
        }
        Object caps = settings.get(key + "." + UPPER_CASE_NUMBER_KEY);
        if (caps instanceof Boolean) {
            upperCaseNumberCheck.setSelected((Boolean) caps);
        }
        Object join = settings.get(key + "." + FORMAT_NUMBER_KEY);
        if (join instanceof Boolean) {
            formatNumberCheck.setSelected((Boolean) join);
        }
        // the old keys win when they are there: they come from a file (or a saved
        // preset) written before the one list existed, so they are what it meant
        if (settings.containsKey(key + "." + OLD_ADD_RANK_DOTS_KEY)
                || settings.containsKey(key + "." + OLD_DOT_CODES_KEY)) {
            dotWordsText.setText(dotWordsFromOldSettings(settings));
        } else {
            Object dotWords = settings.get(key + "." + DOT_WORDS_KEY);
            if (dotWords != null) {
                dotWordsText.setText(dotWords.toString());
            }
        }
        applyItalicMode();

        Object v = settings.get(key + "." + ITALIC_PARTS_KEY);
        if (v instanceof Number) {
            italicPartsSpinner.setValue(((Number) v).intValue());
        }
        v = settings.get(key + "." + ITALIC_CASE_KEY);
        if (v != null) {
            italicCaseCombo.setSelectedItem(LabelStyle.Case.fromString(v.toString()));
        }
        v = settings.get(key + "." + ITALIC_BOLD_KEY);
        if (v instanceof Boolean) {
            italicBoldCheck.setSelected((Boolean) v);
        }
        if (settings.containsKey(key + "." + ITALIC_COLOUR_KEY)) {
            italicColourButton.setColour(colourFromSetting(settings.get(key + "." + ITALIC_COLOUR_KEY)));
        }
        v = settings.get(key + "." + OTHER_CASE_KEY);
        if (v != null) {
            otherCaseCombo.setSelectedItem(LabelStyle.Case.fromString(v.toString()));
        }
        v = settings.get(key + "." + OTHER_BOLD_KEY);
        if (v instanceof Boolean) {
            otherBoldCheck.setSelected((Boolean) v);
        }
        if (settings.containsKey(key + "." + OTHER_COLOUR_KEY)) {
            otherColourButton.setColour(colourFromSetting(settings.get(key + "." + OTHER_COLOUR_KEY)));
        }
        applyGroupStyles();

        v = settings.get(key + "." + HIGHLIGHT_KEY);
        if (v != null) {
            highlightText.setText(v.toString());
        }
        v = settings.get(key + "." + HIGHLIGHT_BOLD_KEY);
        if (v instanceof Boolean) {
            highlightBoldCheck.setSelected((Boolean) v);
        }
        v = settings.get(key + "." + HIGHLIGHT_SUPERSCRIPT_KEY);
        highlightSuperCheck.setSelected(v instanceof Boolean && (Boolean) v);
        if (settings.containsKey(key + "." + HIGHLIGHT_COLOUR_KEY)) {
            highlightColourButton.setColour(colourFromSetting(settings.get(key + "." + HIGHLIGHT_COLOUR_KEY)));
        }
        applyHighlight();
    }

    private void getStyleSettings(Map<String, Object> settings) {
        settings.put(key + "." + ITALIC_MODE_KEY, ((LabelStyle.ItalicMode) italicModeCombo.getSelectedItem()).name());
        settings.put(key + "." + NON_ITALIC_WORDS_KEY, nonItalicWordsText.getText());
        settings.put(key + "." + UPPER_CASE_NUMBER_KEY, upperCaseNumberCheck.isSelected());
        settings.put(key + "." + FORMAT_NUMBER_KEY, formatNumberCheck.isSelected());
        settings.put(key + "." + DOT_WORDS_KEY, dotWordsText.getText());
        settings.put(key + "." + ITALIC_PARTS_KEY, italicPartsSpinner.getValue());
        settings.put(key + "." + ITALIC_CASE_KEY, ((LabelStyle.Case) italicCaseCombo.getSelectedItem()).name());
        settings.put(key + "." + ITALIC_BOLD_KEY, italicBoldCheck.isSelected());
        settings.put(key + "." + ITALIC_COLOUR_KEY, colourSetting(italicColourButton.getColour()));
        settings.put(key + "." + OTHER_CASE_KEY, ((LabelStyle.Case) otherCaseCombo.getSelectedItem()).name());
        settings.put(key + "." + OTHER_BOLD_KEY, otherBoldCheck.isSelected());
        settings.put(key + "." + OTHER_COLOUR_KEY, colourSetting(otherColourButton.getColour()));
        settings.put(key + "." + HIGHLIGHT_KEY, highlightText.getText());
        settings.put(key + "." + HIGHLIGHT_BOLD_KEY, highlightBoldCheck.isSelected());
        settings.put(key + "." + HIGHLIGHT_SUPERSCRIPT_KEY, highlightSuperCheck.isSelected());
        settings.put(key + "." + HIGHLIGHT_COLOUR_KEY, colourSetting(highlightColourButton.getColour()));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(key+"."+IS_SHOWN, titleCheckBox.isSelected());
        settings.put(key+"."+DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem().toString());
        settings.put(key+"."+COLOR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem().toString());
        Font font = labelPainter.getFont();
        settings.put(key+"."+FONT_NAME_KEY, font.getName());
        settings.put(key+"."+FONT_SIZE_KEY, font.getSize());
        settings.put(key+"."+FONT_STYLE_KEY, font.getStyle());
        settings.put(key+"."+SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
        if (intent == LabelPainter.PainterIntent.TIP) {
            settings.put(key + "." + BOX_SIZE, boxSizeSpinner.getValue());
            settings.put(key + "." + TIP_PATH, tipPathCheck.isSelected());
        }
        settings.put(key + "." + REPLACE_UNDERSCORES_KEY, replaceUnderscoresCheck.isSelected());
        settings.put(key + "." + HIDE_PARTS_KEY, hidePartsText.getText());
        settings.put(key + "." + HIDE_REGEX_KEY, hideRegexText.getText());
        if (xPaddingSpinner != null) {
            settings.put(key + "." + X_PADDING_KEY, ((Number) xPaddingSpinner.getValue()).doubleValue());
            settings.put(key + "." + Y_PADDING_KEY, ((Number) yPaddingSpinner.getValue()).doubleValue());
            if (positionCombo != null) {
                settings.put(key + "." + POSITION_KEY, ((LabelPainter.LabelPosition) positionCombo.getSelectedItem()).name());
            }
            settings.put(key + "." + SHOW_THRESHOLD_KEY, thresholdText.getText().trim());
            Object second = secondAttributeCombo.getSelectedItem();
            settings.put(key + "." + SECOND_ATTRIBUTE_KEY, second == null ? BasicLabelPainter.NONE : second.toString());
            settings.put(key + "." + SECOND_LAYOUT_KEY, ((LabelPainter.SecondValueLayout) secondLayoutCombo.getSelectedItem()).name());
            // written only when set: an empty quoted value is not worth the risk in the settings block
            if (secondThresholdText.getText().trim().length() > 0) {
                settings.put(key + "." + SECOND_THRESHOLD_KEY, secondThresholdText.getText().trim());
            }
            if (placeholderText.getText().trim().length() > 0) {
                settings.put(key + "." + BELOW_PLACEHOLDER_KEY, placeholderText.getText().trim());
            }
            if (avoidOverlapCheck != null) {
                settings.put(key + "." + AVOID_OVERLAP_KEY, avoidOverlapCheck.isSelected());
                settings.put(key + "." + LABEL_BACKING_KEY, labelBackingCheck.isSelected());
            }
        }
        if (italicPartsSpinner != null) {
            getStyleSettings(settings);
        }
    }

    public String getTitle() {
        return title;
    }


    private final AttributeColourController colourController;

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox displayAttributeCombo;
    private final JSpinner fontSizeSpinner;
    private FontDialog fontDialog = null;

    private final JComboBox numericalFormatCombo;
    private final JSpinner digitsSpinner;
    private final JSpinner boxSizeSpinner;
    private final JCheckBox tipPathCheck;

    private final JCheckBox replaceUnderscoresCheck;
    private JLabel replaceUnderscoresLabel = null;
    private JLabel hidePartsLabel = null;
    private JLabel hideRegexLabel = null;
    private final JTextField hidePartsText;
    private final JTextField hideRegexText;

    private final JSpinner xPaddingSpinner;
    private final JSpinner yPaddingSpinner;
    private final JComboBox positionCombo;
    private final JCheckBox avoidOverlapCheck;
    private final JCheckBox labelBackingCheck;
    private final JTextField thresholdText;
    private final JComboBox secondAttributeCombo;
    private final JComboBox secondLayoutCombo;
    private final JTextField secondThresholdText;
    private final JTextField placeholderText;

    private final JSpinner italicPartsSpinner;
    private final JComboBox italicModeCombo;
    private final JButton styleTipsButton;
    private final JTextField nonItalicWordsText;
    private final JCheckBox upperCaseNumberCheck;
    private final JCheckBox formatNumberCheck;
    private final JTextField dotWordsText;
    private final JComboBox italicCaseCombo;
    private final JComboBox otherCaseCombo;
    private final JCheckBox italicBoldCheck;
    private final JCheckBox otherBoldCheck;
    private final OptionalColourButton italicColourButton;
    private final OptionalColourButton otherColourButton;
    private final JTextField highlightText;
    private final JCheckBox highlightBoldCheck;
    private final JCheckBox highlightSuperCheck;
    private final OptionalColourButton highlightColourButton;

    private final String title;
    private final String key;
    private final LabelPainter.PainterIntent intent;

    private final LabelPainter labelPainter;

    private final AttributableDecorator userLabelDecorator;

    private final JComboBox colourAttributeCombo;
}
