package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class ViewTranscriptLabel extends Composite implements HandlesError {

    // internal data
    private String zdbID ;

    // GUI elements
    private VerticalPanel panel = new VerticalPanel() ;

    // listeners
    private List<ViewTranscriptListener> viewTranscriptListeners = new ArrayList<ViewTranscriptListener>();
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>() ;

    private HTML label = new HTML("[View Transcript]") ;
    private Label errorLabel = new Label() ;
    private HorizontalPanel buttonPanel = new HorizontalPanel();
    private Button continueButton = new Button("Fix & Continue") ;
    private Button ignoreButton = new Button("Ignore") ;

    public ViewTranscriptLabel(String div){
        initGui() ;
        initWidget(panel);
        addInternalListeners() ;
        RootPanel.get(div).add(this) ;
    }

    private void initGui() {
        label.setStyleName("relatedEntityPubLink");
        panel.add(label);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);

        buttonPanel.add(ignoreButton);
        buttonPanel.add(continueButton);
        buttonPanel.setVisible(false);
        panel.add(buttonPanel);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public void setError(String message) {
        errorLabel.setText(message);
        buttonPanel.setVisible(true);
    }

    public void continueToViewTranscript(){
        Window.open("/action/marker/transcript-view?zdbID="+zdbID,
                "_self", "");
    }

    private void addInternalListeners() {
        label.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                fireViewTranscript() ;
            }
        });

        ignoreButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                continueToViewTranscript();
            }
        });

        continueButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                buttonPanel.setVisible(false);
                clearError();
                fireViewTranscript() ;
            }
        });
    }

    public void addViewTranscriptListeners(ViewTranscriptListener viewTranscriptListener){
        viewTranscriptListeners.add(viewTranscriptListener) ;
    }

    private void fireViewTranscript() {
        for(ViewTranscriptListener viewTranscriptListener : viewTranscriptListeners){
            viewTranscriptListener.finishedView();
        }
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public void addHandlesErrorListener(HandlesError handlesError){
        handlesErrorListeners.add(handlesError) ;
    }

    public void fireEventSuccess(){
        for(HandlesError handlesError: handlesErrorListeners){
            handlesError.clearError();
        }
    }
}
