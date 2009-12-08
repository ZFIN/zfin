package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DOM;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ListItem extends ComplexPanel implements HasText {
    public ListItem() {
        setElement(DOM.createElement("li"));
    }

    public void add(Widget w) {
        super.add(w, getElement());
    }

    public void insert(Widget w, int beforeIndex) {
        super.insert(w, getElement(), beforeIndex, true);
    }

    public String getText() {
        return DOM.getInnerText(getElement());
    }

    public void setText(String text) {
        DOM.setInnerText(getElement(), (text == null) ? "" : text);
    }

}
