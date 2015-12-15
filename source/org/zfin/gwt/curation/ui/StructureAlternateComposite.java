package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class StructureAlternateComposite extends VerticalPanel {

    private Anchor hideSuggestions = new Anchor("Hide Suggestions");

    public StructureAlternateComposite() {
        setVisible(false);
        setStyleName("red");
        add(new Label(""));
        hideSuggestions.setVisible(false);
        hideSuggestions.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                setVisible(false);
            }
        });
    }

    public Anchor getHideSuggestions() {
        return hideSuggestions;
    }

    public void addToPanel(Widget verticalPanel) {
        add(verticalPanel);
        add(hideSuggestions);
    }
}
