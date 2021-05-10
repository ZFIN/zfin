package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI composite to generate an unordered list in the HTML.
 */
public class UnorderedList extends ComplexPanel {
    public UnorderedList() {
        setElement(DOM.createElement("ul"));
    }

    public void add(Widget w) {
        super.add(w, getElement());
    }

    public void insert(Widget w, int beforeIndex) {
        super.insert(w, getElement(), beforeIndex, true);
    }

}

