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
public class NoctuaLink extends Anchor {

    public static final String NOCTUA_IMAGE = "/images/noctua-icon.png";

    private HandlerRegistration registration;

    public NoctuaLink(final String urlString, String title) {
        setTitle(title);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        registration = addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ev) {
                Window.Location.assign(urlString);
            }
        });
        Image img = new Image(NOCTUA_IMAGE);
        getElement().appendChild(img.getElement());
    }

    @UiConstructor
    public NoctuaLink(String title) {
        setTitle(title);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        Image img = new Image(NOCTUA_IMAGE);
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
