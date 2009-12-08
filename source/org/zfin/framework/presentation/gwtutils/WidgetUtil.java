package org.zfin.framework.presentation.gwtutils;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ListBox;
import org.zfin.curation.client.FxExperimentModule;

/**
 * Some convenience methods to handle widgets.
 */
public final class WidgetUtil {

    public static final String BOLD = "bold";
    public static final String RED = "red";
    public static final String ERROR = "error";
    public static final String RED_MOLDIFIER = "red-modifier";
    public static final String GREEN= "green";
    public static final String CSS_CLASS_DELIMITER = " ";
    public static final String RED_HYPERLINK = "red-modifier-hyperlink";

    /**
     * Adds or removes a css class name to the class attribute of the given widget.
     *
     * @param widget    GWT widget
     * @param className css class name to be added or removed
     * @param addClass  boolean: add (true) the class or remove (false) it
     */
    public static void addOrRemoveCssStyle(Widget widget, String className, boolean addClass) {
        String currentClasses = widget.getStyleName();
        String cssNames = addOrRemoveCssClass(className, addClass, currentClasses);
        if (cssNames == null)
            return;

        widget.setStyleName(cssNames);
    }

    public static String addOrRemoveCssClass(String className, boolean addClass, String currentClasses) {
        StringBuilder cssNames = new StringBuilder();
        if (addClass) {
            cssNames.append(currentClasses);
            if (currentClasses.length() > 0)
                cssNames.append(CSS_CLASS_DELIMITER);
            cssNames.append(className);
        } else {
            String[] classes = currentClasses.split(CSS_CLASS_DELIMITER);
            if (classes == null)
                return null;
            for (String cssName : classes) {
                if (!cssName.equals(className))
                    cssNames.append(cssName);
                cssNames.append(CSS_CLASS_DELIMITER);
            }
            cssNames.deleteCharAt(cssNames.length()-1);
        }
        return cssNames.toString();
    }

}
