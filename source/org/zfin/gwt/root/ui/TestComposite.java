package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This class is here in order to test the composites with JWebUnit.
 */
public class TestComposite {

    PopupPanel popup;

    public void initGUI() {
        Button b1 = new Button("b1");
        Button b2 = new Button("b2");
        Button b3 = new Button("b3");
        final Label l1 = new Label("dogz");
        RootPanel.get("slot1").add(b1);
        RootPanel.get("slot2").add(b2);
        RootPanel.get("slot3").add(b3);
        RootPanel.get("slot4").add(l1);

        b1.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                l1.setText("catz");
            }
        });

        b2.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                l1.setText("dogz");
            }
        });

        b3.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("red alert");
//                popup = new PopupPanel(false);
//                popup.setTitle("dogz");
//                popup.setWidget(new Label("inner label"));
//                popup.show();
            }
        });
    }

    public class APopup extends PopupPanel {
        public APopup() {
            super(true);
            setWidget(new Label("Popup Open"));
            show();
        }
    }
}
