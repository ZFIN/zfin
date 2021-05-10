package org.zfin.gwt.root.util;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;

/**
 * Delete image with URL
 */
public class DeleteImage extends Anchor {

    public static final String DELETE_IMAGE = "/images/delete-button.png";

    private HandlerRegistration registration;

    public DeleteImage(final String urlString, String title) {
        setTitle(title);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        registration = addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ev) {
                Window.Location.assign(urlString);
            }
        });
        Image img = new Image(DELETE_IMAGE);
        getElement().appendChild(img.getElement());
    }

    @UiConstructor
    public DeleteImage(String title) {
        setTitle(title);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        Image img = new Image(DELETE_IMAGE);
        getElement().appendChild(img.getElement());
    }

    public void setUrl(final String urlString) {
        if (registration != null)
            registration.removeHandler();
        registration = addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ev) {
                Window.Location.assign(urlString);
            }
        });
    }

}
