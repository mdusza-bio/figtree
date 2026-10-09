package figtree.ui;

import javax.swing.*;

/**
 * MyFigTree (Etap 8.7): the pop-up explanations of the options in the control
 * panels, so that the manual is not needed for every option.
 *
 * <p>Two things Swing does not do on its own:
 * <ul>
 *     <li>a tooltip set on a {@link JSpinner} is never shown over the number
 *     itself, because the editor's text field covers the spinner and has no
 *     tooltip of its own - {@link #set} gives it the same text;</li>
 *     <li>a tooltip vanishes after four seconds, too short for a few lines -
 *     {@link #install} keeps it for a minute.</li>
 * </ul>
 */
public final class ToolTips {

    private ToolTips() {
    }

    /** Call once at start-up: tooltips show a little sooner and stay for a minute. */
    public static void install() {
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setInitialDelay(400);
        manager.setDismissDelay(60000);
        manager.setReshowDelay(100);
    }

    /**
     * The same text on every component given (nulls are skipped). A spinner
     * also passes it on to its editor and text field, which cover it.
     */
    public static void set(String text, JComponent... components) {
        for (JComponent component : components) {
            if (component == null) continue;
            component.setToolTipText(text);
            if (component instanceof JSpinner) {
                JComponent editor = ((JSpinner) component).getEditor();
                editor.setToolTipText(text);
                if (editor instanceof JSpinner.DefaultEditor) {
                    ((JSpinner.DefaultEditor) editor).getTextField().setToolTipText(text);
                }
            }
        }
    }

    /**
     * Gives the row label the tooltip of its field, so that hovering over the
     * name of the option shows the explanation too.
     *
     * @return the label, for chaining into addComponent()
     */
    public static JLabel copy(JLabel label, JComponent source) {
        set(source.getToolTipText(), label, source);
        return label;
    }
}
