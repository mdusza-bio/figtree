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

import figtree.ui.PercentFormat;
import figtree.ui.components.ColorWellButton;
import figtree.ui.RomanFormat;
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

    // MyFigTree: pulling crowded support values apart (Etap 6.6)
    public static final String AVOID_OVERLAP_KEY = "avoidOverlap";

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
    public static final String HIGHLIGHT_COLOUR_KEY = "highlightColour";

    // MyFigTree: smarter italics (Etap 6.1)
    public static final String ITALIC_MODE_KEY = "italicMode";
    public static final String NON_ITALIC_WORDS_KEY = "nonItalicWords";
    public static final String ADD_RANK_DOTS_KEY = "addRankDots";
    public static final String UPPER_CASE_NUMBER_KEY = "upperCaseIsNumber";
    public static final String FORMAT_NUMBER_KEY = "formatCollectionNumber";
    public static final String DOT_CODES_KEY = "dotCodes";

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
        hidePartsText.setToolTipText("Comma-separated text to remove from names, e.g. EBOV|,_contig1");
        hidePartsText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
            public void removeUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
            public void changedUpdate(DocumentEvent e) { labelPainter.setHideParts(hidePartsText.getText()); }
        });

        hideRegexText = new JTextField("", 10);
        hideRegexText.setToolTipText("Regular expression; matching text is removed from names");
        hideRegexText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
            public void removeUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
            public void changedUpdate(DocumentEvent e) { labelPainter.setHideRegex(hideRegexText.getText()); }
        });

        // MyFigTree: placement and support-value controls (node and branch labels only)
        if (intent == LabelPainter.PainterIntent.NODE || intent == LabelPainter.PainterIntent.BRANCH) {
            xPaddingSpinner = new JSpinner(new SpinnerNumberModel(0.0, -500.0, 500.0, 1.0));
            xPaddingSpinner.setToolTipText("Horizontal distance from the node/branch in points (may be negative)");
            yPaddingSpinner = new JSpinner(new SpinnerNumberModel(0.0, -500.0, 500.0, 1.0));
            yPaddingSpinner.setToolTipText("Vertical distance from the branch line in points (may be negative)");
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
                avoidOverlapCheck.setToolTipText("<html>Nudge support values apart where they would be drawn<br>" +
                        "on top of each other (clades that add one taxon at a time).<br>" +
                        "Only the labels move - the tree itself is not redrawn.</html>");
                avoidOverlapCheck.setSelected(labelPainter.isAvoidOverlap());
                avoidOverlapCheck.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        labelPainter.setAvoidOverlap(avoidOverlapCheck.isSelected());
                    }
                });
            } else {
                positionCombo = null;
                avoidOverlapCheck = null;
            }

            thresholdText = new JTextField("", 6);
            thresholdText.setToolTipText("Show numeric values only if >= this number; empty = show all");
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
            thresholdText = null;
            secondAttributeCombo = null;
            secondLayoutCombo = null;
        }

        // MyFigTree: per-part styling of tip names (Etap 3)
        final LabelStyle style = labelPainter.getLabelStyle();
        if (intent == LabelPainter.PainterIntent.TIP && style != null) {
            italicPartsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
            italicPartsSpinner.setToolTipText("How many leading parts of the name (e.g. genus + species = 2) are drawn in italics; 0 = off");
            italicPartsSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    style.setItalicParts((Integer) italicPartsSpinner.getValue());
                    labelPainter.labelStyleChanged();
                }
            });

            // MyFigTree (Etap 6.1): italics that stop at the collection number
            italicModeCombo = new JComboBox(LabelStyle.ItalicMode.values());
            italicModeCombo.setSelectedItem(style.getItalicMode());
            italicModeCombo.setToolTipText("<html>How much of the name is italic:<br>" +
                    "<b>Off</b> - nothing;<br>" +
                    "<b>First N parts</b> - always the same number of parts;<br>" +
                    "<b>Until first number</b> - everything before the collection number.</html>");

            nonItalicWordsText = new JTextField(style.getNonItalicWords(), 10);
            nonItalicWordsText.setToolTipText("<html>Words that stay upright inside an italic name (var, sp, ...),<br>" +
                    "separated by spaces. They do NOT end the italics.</html>");
            nonItalicWordsText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyItalicMode(); }
                public void removeUpdate(DocumentEvent e) { applyItalicMode(); }
                public void changedUpdate(DocumentEvent e) { applyItalicMode(); }
            });

            addRankDotsCheck = new JCheckBox("Add dot");
            addRankDotsCheck.setToolTipText("Draw \"var.\" / \"sp.\" even when the name has no dot");
            upperCaseNumberCheck = new JCheckBox("ALL CAPS");
            upperCaseNumberCheck.setToolTipText("<html>Also stop the italics at an ALL-CAPS part with no digits<br>" +
                    "(a herbarium code such as KRAM or BR).</html>");
            upperCaseNumberCheck.setSelected(style.isUpperCaseIsNumber());
            formatNumberCheck = new JCheckBox("Join number");
            formatNumberCheck.setToolTipText("<html>Put the collection number that the underscores broke apart<br>" +
                    "back together the way it is cited:<br>" +
                    "<b>KRAM M 1156</b> &rarr; KRAM M-1156<br>" +
                    "<b>UK100 1b</b> &rarr; UK100-1b<br>" +
                    "<b>UARK CA 6 131</b> &rarr; UARK CA. 6-131<br>" +
                    "<b>Ron324 2 3</b> &rarr; Ron324 2/3 (a field collection split in three)</html>");
            formatNumberCheck.setSelected(style.isFormatCollectionNumber());

            dotCodesText = new JTextField(style.getDotCodes(), 6);
            dotCodesText.setToolTipText("<html>Collection abbreviations written with a dot, e.g. the CA of<br>" +
                    "UARK CA. 6-131 (separated by spaces). Leave empty for none.</html>");
            dotCodesText.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyItalicMode(); }
                public void removeUpdate(DocumentEvent e) { applyItalicMode(); }
                public void changedUpdate(DocumentEvent e) { applyItalicMode(); }
            });

            ActionListener italicModeListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyItalicMode();
                }
            };
            italicModeCombo.addActionListener(italicModeListener);
            addRankDotsCheck.addActionListener(italicModeListener);
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
            highlightText.setToolTipText("Comma-separated text; tips whose (raw) name contains any of them get the highlight style");
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

        } else {
            italicPartsSpinner = null;
            italicModeCombo = null;
            nonItalicWordsText = null;
            addRankDotsCheck = null;
            upperCaseNumberCheck = null;
            formatNumberCheck = null;
            dotCodesText = null;
            italicCaseCombo = null;
            otherCaseCombo = null;
            italicBoldCheck = null;
            otherBoldCheck = null;
            italicColourButton = null;
            otherColourButton = null;
            highlightText = null;
            highlightBoldCheck = null;
            highlightColourButton = null;
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
        optionsPanel.addComponent(replaceUnderscoresCheck, true);
        final JLabel label8 = optionsPanel.addComponentWithLabel("Hide parts:", hidePartsText);
        final JLabel label9 = optionsPanel.addComponentWithLabel("Hide regex:", hideRegexText);

        if (italicPartsSpinner != null) {
            addComponent(optionsPanel.addComponentWithLabel("Italic mode:", italicModeCombo));
            addComponent(italicModeCombo);
            addComponent(optionsPanel.addComponentWithLabel("Italic first N parts:", italicPartsSpinner));
            addComponent(italicPartsSpinner);
            addComponent(optionsPanel.addComponentWithLabel("Not italic words:", nonItalicWordsText));
            addComponent(nonItalicWordsText);
            addComponent(optionsPanel.addComponentWithLabel("Rank words:", addRankDotsCheck));
            addComponent(addRankDotsCheck);
            addComponent(optionsPanel.addComponentWithLabel("Collection number:", checkPanel(upperCaseNumberCheck, formatNumberCheck)));
            addComponent(upperCaseNumberCheck);
            addComponent(formatNumberCheck);
            addComponent(optionsPanel.addComponentWithLabel("Dot after codes:", dotCodesText));
            addComponent(dotCodesText);
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
            addComponent(optionsPanel.addComponentWithLabel("Highlight names containing:", highlightText));
            addComponent(highlightText);
            addComponent(optionsPanel.addComponentWithLabel("Highlight style:", stylePanel(highlightBoldCheck, highlightColourButton)));
            addComponent(highlightBoldCheck);
            addComponent(highlightColourButton);
        }

        if (xPaddingSpinner != null) {
            if (positionCombo != null) {
                addComponent(optionsPanel.addComponentWithLabel("Position:", positionCombo));
                addComponent(positionCombo);
            }
            addComponent(optionsPanel.addComponentWithLabel("Offset X:", xPaddingSpinner));
            addComponent(xPaddingSpinner);
            addComponent(optionsPanel.addComponentWithLabel("Offset Y:", yPaddingSpinner));
            addComponent(yPaddingSpinner);
            addComponent(optionsPanel.addComponentWithLabel("Show only if >=:", thresholdText));
            addComponent(thresholdText);
            addComponent(optionsPanel.addComponentWithLabel("Second value:", secondAttributeCombo));
            addComponent(secondAttributeCombo);
            addComponent(optionsPanel.addComponentWithLabel("Layout:", secondLayoutCombo));
            addComponent(secondLayoutCombo);
            if (avoidOverlapCheck != null) {
                addComponent(optionsPanel.addComponentWithLabel("Crowded values:", avoidOverlapCheck));
                addComponent(avoidOverlapCheck);
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

    /** MyFigTree (Etap 6.1) */
    private void applyItalicMode() {
        LabelStyle style = labelPainter.getLabelStyle();
        style.setItalicMode((LabelStyle.ItalicMode) italicModeCombo.getSelectedItem());
        style.setNonItalicWords(nonItalicWordsText.getText());
        style.setAddRankDots(addRankDotsCheck.isSelected());
        style.setUpperCaseIsNumber(upperCaseNumberCheck.isSelected());
        style.setFormatCollectionNumber(formatNumberCheck.isSelected());
        style.setDotCodes(dotCodesText.getText());
        updateItalicEnabled();
        labelPainter.labelStyleChanged();
    }

    /** greys out the controls that the chosen italic mode does not use */
    private void updateItalicEnabled() {
        LabelStyle.ItalicMode mode = (LabelStyle.ItalicMode) italicModeCombo.getSelectedItem();
        italicPartsSpinner.setEnabled(mode == LabelStyle.ItalicMode.FIRST_N);
        boolean untilNumber = (mode == LabelStyle.ItalicMode.UNTIL_NUMBER);
        nonItalicWordsText.setEnabled(untilNumber);
        addRankDotsCheck.setEnabled(untilNumber);
        upperCaseNumberCheck.setEnabled(untilNumber);
        formatNumberCheck.setEnabled(untilNumber);
        dotCodesText.setEnabled(untilNumber && formatNumberCheck.isSelected());
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
            well.setToolTipText(title + " (empty = same as the label colour)");
            ControllerOptionsPanel.setComponentLook(well);
            JButton clearButton = new JButton("x");
            clearButton.setToolTipText("Use the normal label colour");
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
            Object avoid = settings.get(key + "." + AVOID_OVERLAP_KEY);
            if (avoidOverlapCheck != null && avoid instanceof Boolean) {
                avoidOverlapCheck.setSelected((Boolean) avoid);
                labelPainter.setAvoidOverlap((Boolean) avoid);
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
        Object dots = settings.get(key + "." + ADD_RANK_DOTS_KEY);
        if (dots instanceof Boolean) {
            addRankDotsCheck.setSelected((Boolean) dots);
        }
        Object caps = settings.get(key + "." + UPPER_CASE_NUMBER_KEY);
        if (caps instanceof Boolean) {
            upperCaseNumberCheck.setSelected((Boolean) caps);
        }
        Object join = settings.get(key + "." + FORMAT_NUMBER_KEY);
        if (join instanceof Boolean) {
            formatNumberCheck.setSelected((Boolean) join);
        }
        Object dots2 = settings.get(key + "." + DOT_CODES_KEY);
        if (dots2 != null) {
            dotCodesText.setText(dots2.toString());
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
        if (settings.containsKey(key + "." + HIGHLIGHT_COLOUR_KEY)) {
            highlightColourButton.setColour(colourFromSetting(settings.get(key + "." + HIGHLIGHT_COLOUR_KEY)));
        }
        applyHighlight();
    }

    private void getStyleSettings(Map<String, Object> settings) {
        settings.put(key + "." + ITALIC_MODE_KEY, ((LabelStyle.ItalicMode) italicModeCombo.getSelectedItem()).name());
        settings.put(key + "." + NON_ITALIC_WORDS_KEY, nonItalicWordsText.getText());
        settings.put(key + "." + ADD_RANK_DOTS_KEY, addRankDotsCheck.isSelected());
        settings.put(key + "." + UPPER_CASE_NUMBER_KEY, upperCaseNumberCheck.isSelected());
        settings.put(key + "." + FORMAT_NUMBER_KEY, formatNumberCheck.isSelected());
        settings.put(key + "." + DOT_CODES_KEY, dotCodesText.getText());
        settings.put(key + "." + ITALIC_PARTS_KEY, italicPartsSpinner.getValue());
        settings.put(key + "." + ITALIC_CASE_KEY, ((LabelStyle.Case) italicCaseCombo.getSelectedItem()).name());
        settings.put(key + "." + ITALIC_BOLD_KEY, italicBoldCheck.isSelected());
        settings.put(key + "." + ITALIC_COLOUR_KEY, colourSetting(italicColourButton.getColour()));
        settings.put(key + "." + OTHER_CASE_KEY, ((LabelStyle.Case) otherCaseCombo.getSelectedItem()).name());
        settings.put(key + "." + OTHER_BOLD_KEY, otherBoldCheck.isSelected());
        settings.put(key + "." + OTHER_COLOUR_KEY, colourSetting(otherColourButton.getColour()));
        settings.put(key + "." + HIGHLIGHT_KEY, highlightText.getText());
        settings.put(key + "." + HIGHLIGHT_BOLD_KEY, highlightBoldCheck.isSelected());
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
            if (avoidOverlapCheck != null) {
                settings.put(key + "." + AVOID_OVERLAP_KEY, avoidOverlapCheck.isSelected());
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
    private final JTextField hidePartsText;
    private final JTextField hideRegexText;

    private final JSpinner xPaddingSpinner;
    private final JSpinner yPaddingSpinner;
    private final JComboBox positionCombo;
    private final JCheckBox avoidOverlapCheck;
    private final JTextField thresholdText;
    private final JComboBox secondAttributeCombo;
    private final JComboBox secondLayoutCombo;

    private final JSpinner italicPartsSpinner;
    private final JComboBox italicModeCombo;
    private final JTextField nonItalicWordsText;
    private final JCheckBox addRankDotsCheck;
    private final JCheckBox upperCaseNumberCheck;
    private final JCheckBox formatNumberCheck;
    private final JTextField dotCodesText;
    private final JComboBox italicCaseCombo;
    private final JComboBox otherCaseCombo;
    private final JCheckBox italicBoldCheck;
    private final JCheckBox otherBoldCheck;
    private final OptionalColourButton italicColourButton;
    private final OptionalColourButton otherColourButton;
    private final JTextField highlightText;
    private final JCheckBox highlightBoldCheck;
    private final OptionalColourButton highlightColourButton;

    private final String title;
    private final String key;
    private final LabelPainter.PainterIntent intent;

    private final LabelPainter labelPainter;

    private final AttributableDecorator userLabelDecorator;

    private final JComboBox colourAttributeCombo;
}
