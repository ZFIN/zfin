package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *  This is a lookup box composite.  It allows the following options:
 *  - name of input box
  *  - show errors
 *  - show button
 *  - type (GO, PATO, QUALITY)
 *  - showSynons
 */
public class LookupComposite extends Composite {

    // gui components
    private HorizontalPanel lookupPanel = new HorizontalPanel();
    private ItemSuggestOracle oracle = new ItemSuggestOracle(this);
    private TextBox textBox = new TextBox();
    private SuggestBox suggestBox;
    private SuggestOracle.Suggestion suggestion = null;
    private Button submitButton ;
    private String currentText = null;
    private Label errorLabel = new Label();
    private VerticalPanel rootPanel = new VerticalPanel() ;
    private String errorString = "" ;

    // lookup types
    public final static String TYPE_ANATOMY_ONTOLOGY = "ANATOMY_ONTOLOGY" ;
    public final static String TYPE_GENE_ONTOLOGY= "GENE_ONTOLOGY" ;
    public final static String TYPE_QUALITY = "QUALITY" ;
    private List types = new ArrayList() ;

    // options
    private String inputName = "search";
    private boolean showError = true;
    private boolean showButton = false ;
    private String type = TYPE_ANATOMY_ONTOLOGY ;

    public LookupComposite(){
        types.add(TYPE_ANATOMY_ONTOLOGY) ;
        types.add(TYPE_GENE_ONTOLOGY) ;
        types.add(TYPE_QUALITY) ;
    }

    public void initGui(){
        textBox.setName(inputName);
        textBox.setTitle(inputName);
        textBox.setVisibleLength(30);
        DOM.setElementAttribute(textBox.getElement(), "autocomplete", "off");
        suggestBox = new SuggestBox(oracle, textBox);
        suggestBox.setLimit(60);
        suggestBox.addEventHandler(new SuggestionHandler() {
            public void onSuggestionSelected(SuggestionEvent event) {
                suggestion = event.getSelectedSuggestion();
                if (suggestion.getReplacementString() == null) {
                    suggestBox.setText(currentText);
                    submitSearch(currentText);
                } else if (suggestion.getReplacementString() != null) {
                    submitSearch(suggestion.getReplacementString());
                }
            }
        });


        suggestBox.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget w, char c, int i) {
                if (textBox.getText() != null & textBox.getText().length() > 0) {
                    currentText = textBox.getText()  ;
                    if( Character.isLetterOrDigit(c)){
                        currentText += c ;
                    }
                }
            }
        });


        lookupPanel.add(suggestBox);

        if(showButton){
            submitButton = new Button("search") ;
            submitButton.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    submitSearch(textBox.getText());
                }
            });

            lookupPanel.add(submitButton);
        }
        rootPanel.add(lookupPanel);
        if(showError){
            initErrorGui();
        }
        textBox.setFocus(true);
        initWidget(rootPanel);
    }

    protected void initErrorGui(){
        errorLabel.setStyleName("gwt-lookup-error");
        errorLabel.setVisible(false);
        errorLabel.setWordWrap(true);
        rootPanel.add(errorLabel);
    }


    public void setText(String newText){
        textBox.setText(newText);
    }

    private void submitSearch(String text) {
        if (text != null) {
            Window.open("/action/anatomy/search?action=term-search&searchTerm=" + text.replaceAll(" ", "%20"), "_self",
                    "");
        }
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
        setErrorString("") ;
    }

    public void hideError(){
        errorLabel.setVisible(false);
    }

    public void showError(){
        errorLabel.setVisible(true);
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(false==types.contains(type)){
            String typeList = "" ;

            for(Iterator iter = types.iterator() ; iter.hasNext() ;  ){
                typeList += iter.next().toString() + " " ;
            }

            throw new RuntimeException("Type " + type + " not recognized.  Try: \n" + typeList) ; 
        }
        this.type = type;
    }

    public boolean isShowButton() {
        return showButton;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }

    public boolean isShowError() {
        return showError;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }


	public String getCurrentText(){
		return currentText ; 
	}

    public TextBox getTextBox() {
        return textBox;
    }
}
