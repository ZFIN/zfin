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
        this.widget = widget;
        setVisibility(show);
    }

    public void toggleVisibility() {
        if (show) {
            setVisibilityToHide();
        } else {
            setVisibilityToShow();
        }
    }

    public void setVisibilityToHide() {
        show = false;
        setText("Show");
        widget.setVisible(show);
    }

    public void setVisibilityToShow() {
        show = true;
        setText("Hide");
        widget.setVisible(show);
    }

    public void setVisibility(boolean show) {
        if (show)
            setVisibilityToShow();
        else
            setVisibilityToHide();
    }

    public boolean isVisible(){
        return widget.isVisible();
    }
}
