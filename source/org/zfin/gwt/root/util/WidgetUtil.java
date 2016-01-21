package org.zfin.gwt.root.util;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Some convenience methods to handle widgets.
 */
public final class WidgetUtil {

    // css classes
    public static final String BOLD = "bold";
    public static final String RED = "red";
    public static final String ERROR = "error";
    public static final String RED_MODIFIER = "red-modifier";
    public static final String GREEN = "green";
    public static final String PHENOTYPE_NORMAL = "phenotype-normal";
    public static final String CSS_CLASS_DELIMITER = " ";
    public static final String RED_HYPERLINK = "red-modifier-hyperlink";
    public static final String CSS_CURATION_BOX_CONTROL_TEXT = "curation-box-control-text";
    public static final String RIGHT_ALIGN_BOX = "right-align-box";

    public static final String NO_WRAP = "nowrap";
    public static final String AJAX_LOADER_GIF = "/images/ajax-loader.gif";

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
        StringBuilder cssNames = new StringBuilder(15);
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
            cssNames.deleteCharAt(cssNames.length() - 1);
        }
        return cssNames.toString();
    }

    public static HTML getNbsp() {
        return new HTML("&nbsp;");
    }

    public static void selectListBox(ListBox listBox, String value) {
        int totalEnvironments = listBox.getItemCount();
        for (int index = 0; index < totalEnvironments; index++) {
            String boxValue = listBox.getValue(index);
            if (boxValue.equals(value))
                listBox.setSelectedIndex(index);
        }
    }

    // Returns the group index
    public static int setRowStyle(int rowIndex, String currentID, String previousID, int currentGroupIndex, Grid grid) {
        StringBuilder sb = new StringBuilder(50);
        // check even/odd row
        if (rowIndex % 2 == 0)
            sb.append(CssStyles.EVEN.toString());
        else
            sb.append(CssStyles.ODD.toString());

        sb.append(" ");
        if (previousID == null && currentID == null) {
            sb.append(CssStyles.OLDGROUP.toString());
        } else if (previousID == null) {
            sb.append(CssStyles.NEWGROUP.toString());
            currentGroupIndex++;
        } else if (previousID.equals(currentID)) {
            sb.append(CssStyles.OLDGROUP.toString());
        } else {
            sb.append(CssStyles.NEWGROUP.toString());
            currentGroupIndex++;
        }

        // check if odd group or even group
        sb.append(" ");
        if (currentGroupIndex % 2 == 0)
            sb.append(CssStyles.EVENGROUP.toString());
        else
            sb.append(CssStyles.ODDGROUP.toString());

        // add row
        sb.append(" ");
        sb.append(CssStyles.EXPERIMENT_ROW.toString());
        grid.getRowFormatter().setStyleName(rowIndex, sb.toString());
        return currentGroupIndex;
    }

    public static void setAlternateRowStyle(int row, Grid grid) {
        StringBuilder sb = new StringBuilder(50);
        sb.append(CssStyles.NEWGROUP.toString());
        sb.append(" ");
        if (row % 2 == 0)
            sb.append(CssStyles.EVENGROUP.toString());
        else
            sb.append(CssStyles.ODDGROUP.toString());
        grid.getRowFormatter().setStyleName(row, sb.toString());
    }

    public enum CssStyles {

        EVEN("even"),
        ODD("odd"),
        EVENGROUP("evengroup"),
        ODDGROUP("oddgroup"),
        NEWGROUP("newgroup"),
        OLDGROUP("oldgroup"),
        EXPERIMENT_ROW("experiment-row"),
        SEARCHRESULTS("searchresults"),
        GROUPSTRIPES_HOVER("groupstripes-hover"),
        TABLE_HEADER("table-header");

        private final String value;

        private CssStyles(String name) {
            this.value = name;
        }

        public String toString() {
            return this.value;
        }

        public static CssStyles getType(String type) {
            for (CssStyles t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No Css class named " + type + " found.");
        }

    }

}
