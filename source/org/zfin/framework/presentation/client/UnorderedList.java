package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DOM;

/**
 * ToDo: ADD DOCUMENTATION!
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

