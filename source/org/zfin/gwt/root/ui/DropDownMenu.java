package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

public class DropDownMenu extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    @UiField
    Button button;
    @UiField
    HTMLPanel menuDiv;

    @UiTemplate("DropDownMenu.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, DropDownMenu> {
    }

    public DropDownMenu() {
        initWidget(binder.createAndBindUi(this));
        button.getElement().setAttribute("data-toggle","dropdown");
        button.getElement().setAttribute("aria-expanded","false");
    }

    @UiHandler("button")
    public void handleClick(ClickEvent event) {
        Window.alert("Hi");
    }
}
