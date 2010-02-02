package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the "view" link at the top of the page.
 * This composite provides additional validation and provides a place for "page"
 * level errors.
 *
 * The ignore button proceeds to where you are planning to go without revalidating.
 */
public class ViewMarkerLabel<T extends MarkerDTO> extends AbstractComposite<T> {

    private final String viewText ;
    private final String linkPrefix ;

    // listeners
    private final List<ViewMarkerListener> viewMarkerListeners = new ArrayList<ViewMarkerListener>();

    private HTML label = new HTML("[View Transcript]");
    private final HorizontalPanel buttonPanel = new HorizontalPanel();
    private final Button ignoreButton = new Button("Ignore");

    public ViewMarkerLabel(String viewText, String linkPrefix, String ignoreButtonText) {
        this.viewText = viewText ;
        this.linkPrefix = linkPrefix ;
        ignoreButton.setText(ignoreButtonText);
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(StandardMarkerDivNames.viewDiv).add(this);
    }

    protected void initGUI() {
        label = new HTML(viewText);
        label.setStyleName("relatedEntityPubLink");
        panel.add(label);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);

        buttonPanel.add(ignoreButton);
//        buttonPanel.add(continueButton);
        buttonPanel.setVisible(false);
        panel.add(buttonPanel);
    }


    public void continueToViewTranscript() {
        String url = linkPrefix + dto.getZdbID();
        Window.open(url,
                "_self", "");
    }

    @Override
    protected void revertGUI() { }


    public void clearError() {
        errorLabel.setText("");
        buttonPanel.setVisible(false);
    }

    public void setError(String message) {
        errorLabel.setText(message);
        buttonPanel.setVisible(true);
    }

    protected void addInternalListeners(HandlesError handlesError) {
        label.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fireViewTranscript();
            }
        });

        ignoreButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                continueToViewTranscript();
            }
        });

//        continueButton.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                buttonPanel.setVisible(false);
//                clearError();
//                fireViewTranscript();
//            }
//        });
    }

    public void addViewMarkerListeners(ViewMarkerListener viewMarkerListener) {
        viewMarkerListeners.add(viewMarkerListener);
    }

    void fireViewTranscript() {
        for (ViewMarkerListener viewMarkerListener : viewMarkerListeners) {
            viewMarkerListener.finishedView();
        }
    }

}