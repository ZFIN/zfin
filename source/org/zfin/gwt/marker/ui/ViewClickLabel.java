package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
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
public class ViewClickLabel<T extends RelatedEntityDTO> extends AbstractComposite<T> {

    private final String viewText ;
    private final String linkPrefix ;

    // listeners
    private final List<ViewClickedListener> viewClickedListeners = new ArrayList<ViewClickedListener>();

    private HTML label = new HTML("[View]");
    protected final HorizontalPanel buttonPanel = new HorizontalPanel();
    private final Button ignoreButton = new Button("Ignore");
    protected final HTML messageLabel = new HTML("");

    public ViewClickLabel(String viewText, String linkPrefix, String ignoreButtonText,String div) {
        this.viewText = viewText ;
        this.linkPrefix = linkPrefix ;
        ignoreButton.setText(ignoreButtonText);
        initGUI();
        initWidget(panel);
        addInternalListeners(this);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    public ViewClickLabel(String viewText, String linkPrefix, String ignoreButtonText) {
        this(viewText,linkPrefix,ignoreButtonText,StandardDivNames.viewDiv) ;
    }

    protected void initGUI() {
        label = new HTML(viewText);
        label.setStyleName("relatedEntityPubLink");
        panel.add(label);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);
        messageLabel.setVisible(false);
        panel.add(messageLabel);

        buttonPanel.add(ignoreButton);
        buttonPanel.setVisible(false);
        panel.add(buttonPanel);
    }


    public void continueToViewTranscript() {
        if(false==linkPrefix.isEmpty()){
            String url = linkPrefix + dto.getZdbID();
            Window.open(url,
                    "_self", "");
        }
    }

    @Override
    protected void revertGUI() { }

    @Override
    protected void setValues() { }


    public void clearError() {
        errorLabel.setText("");
        buttonPanel.setVisible(false);
    }

    public void setError(String message) {
        errorLabel.setText(message);
        buttonPanel.setVisible(true);
        messageLabel.setHTML("");
        messageLabel.setVisible(false);
    }

    public void setMessage(String s) {
        clearError();
        messageLabel.setHTML(s);
        messageLabel.setVisible(true);
    }


    protected void addInternalListeners(HandlesError handlesError) {
        label.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fireViewClicked();
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

    public void addViewClickedListeners(ViewClickedListener viewClickedListener) {
        viewClickedListeners.add(viewClickedListener);
    }

    void fireViewClicked() {
        for (ViewClickedListener viewClickedListener : viewClickedListeners) {
            viewClickedListener.finishedView();
        }
    }

}