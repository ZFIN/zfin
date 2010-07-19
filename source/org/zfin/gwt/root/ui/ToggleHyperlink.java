package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * This Hyperlink class can be used in case you need to toggle between two
 * states, called true and false.
 * By default the hyperlink will display the true-state and upon clicking it toggle to the false-state.
 * It has the default click handler for providing this service.
 * Call addClickHandler() on this widget to execute whatever logic has to happen in addition to changing the text.
 */
public class ToggleHyperlink extends Hyperlink {

    private String toggleTrue;
    private String toggleFalse;

    public ToggleHyperlink(String toggleTrue, String toggleFalse) {
        super(toggleTrue, toggleTrue);
        this.toggleTrue = toggleTrue;
        this.toggleFalse = toggleFalse;
        addClickHandler(new ToggleHyperlinkClickHandler(toggleTrue, toggleFalse));
        setVisible(false);
    }


    public boolean getToggleStatus() {
        return getText().equals(toggleTrue);
    }

    public String getToggleTrue() {
        return toggleTrue;
    }

    public String getToggleFalse() {
        return toggleFalse;
    }

    public void setToggleStatus(boolean toggleTrueBoolean) {
        if (toggleTrueBoolean)
            setText(toggleTrue);
        else
            setText(toggleFalse);
    }

    /**
     * This method set the link to visible or invisible.
     * This allows you to toggle the visiblity as well.
     *
     * @param hideLink hide the link: true of false
     */
    public void hideHyperlink(boolean hideLink) {
        if (hideLink && getToggleStatus()) {
            setVisible(false);
        } else {
            setVisible(true);
        }

    }

    public void uncheckAllRecords() {
        if (getToggleStatus())
            setVisible(false);
    }

    // ************* Handler

    protected class ToggleHyperlinkClickHandler implements ClickHandler {
        private String textTrue;
        private String textFalse;

        public ToggleHyperlinkClickHandler(String textTrue, String textFalse) {
            this.textTrue = textTrue;
            this.textFalse = textFalse;
        }

        public void onClick(ClickEvent clickEvent) {
            //Window.alert("ToggleClickHandler "+getText());
            if (getText().equals(textTrue)) {
                setText(textFalse);
                setStyleName("red");
            } else {
                setText(textTrue);
                setStyleName("black");
            }
        }
    }


}
