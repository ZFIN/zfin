package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * GWT UI composite that create an <li> element.
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
