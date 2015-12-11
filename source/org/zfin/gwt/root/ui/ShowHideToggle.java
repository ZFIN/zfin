package org.zfin.gwt.root.ui;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility widget to handle toggling visibility of panels.
 */
public class ShowHideToggle extends Hyperlink {

    private boolean show;
    private Widget widget;

    public ShowHideToggle(Widget widget) {
        this(widget, false);
    }

    @UiConstructor
    public ShowHideToggle(Widget widget, boolean show) {
        this.show = show;
        this.widget = widget;
        setText("Hide");
    }

    public void toggleVisibility() {
        if (show) {
            show = false;
            setText("Show");
        } else {
            show = true;
            setText("Hide");
        }
        widget.setVisible(show);
    }
}
