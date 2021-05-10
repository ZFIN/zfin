package org.zfin.gwt.root.util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Delete image with URL
 */
public class DeleteLink extends Anchor {

    public static final String DELETE_TEXT = "(X)";

    public DeleteLink(final String urlString, String title) {
        super(DELETE_TEXT);
        setTitle(title);
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ev) {
                Window.Location.assign(urlString);
            }
        });
    }

    @UiConstructor
    public DeleteLink(String title) {
        super(DELETE_TEXT);
        setTitle(title);
    }

}
