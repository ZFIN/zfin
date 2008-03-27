package org.zfin.anatomy.presentation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * This interface is designed to be implemented by
 * UI components that can have errors associated with them.
 */
public abstract class ErrorAssociable implements EntryPoint {

    Label errorLabel = null ;
    VerticalPanel rootPanel = new VerticalPanel() ;
    private String errorString = "" ;

    public ErrorAssociable(){
        rootPanel = new VerticalPanel() ;
        errorLabel = new Label() ;
        loadErrorPanel();
    }

    public void setErrorString(String error) {
        this.errorString = error ;
        errorLabel.setText(errorString);
        errorLabel.setVisible(true);
    }

    public String getErrorString() {
        return errorString ; 
    }

    public void clearError(){
        errorString = "" ; 
        errorLabel.setVisible(false);
    }

    public void showError(){
        errorLabel.setVisible(true);
    }

    protected void loadErrorPanel(){
        errorLabel.setStyleName("gwt-lookup-error");
        errorLabel.setVisible(false);
        errorLabel.setWordWrap(true);
        rootPanel.add(errorLabel);
    }

}
