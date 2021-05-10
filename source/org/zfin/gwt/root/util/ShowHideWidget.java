package org.zfin.gwt.root.util;

import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility widget to handle toggling visibility of panels.
 */
public class ShowHideWidget {

    private boolean show;
    private Widget widget;
    private Hyperlink showHideTextPanel;

    public ShowHideWidget(Hyperlink showHideTextPanel, Widget widget) {
        this(showHideTextPanel, widget, false);
    }

    public ShowHideWidget(Hyperlink showHideTextPanel, Widget widget, boolean show) {
        this.show = show;
        this.showHideTextPanel = showHideTextPanel;
        this.widget = widget;
    }

    public void toggleVisibility() {
        if (show) {
            show = false;
            showHideTextPanel.setText("Show");
        } else {
            show = true;
            showHideTextPanel.setText("Hide");
        }
        widget.setVisible(show);
    }
}
