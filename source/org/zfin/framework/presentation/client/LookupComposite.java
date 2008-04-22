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
    protected HorizontalPanel lookupPanel = new HorizontalPanel();
    protected ItemSuggestOracle oracle = new ItemSuggestOracle(this);
    protected TextBox textBox = new TextBox();
    protected SuggestBox suggestBox;
    protected SuggestOracle.Suggestion suggestion = null;
    protected Button submitButton ;
    protected String currentText = null;
    protected Label noteLabel = new Label();
    protected VerticalPanel rootPanel = new VerticalPanel() ;
    protected String noteString = "" ;

    // lookup types
    public final static String TYPE_ANATOMY_ONTOLOGY = "ANATOMY_ONTOLOGY" ;
    public final static String TYPE_GENE_ONTOLOGY= "GENE_ONTOLOGY" ;
    public final static String TYPE_QUALITY = "QUALITY" ;
    private List types = new ArrayList() ;

    // options
    protected String inputName = "search";
    protected boolean showError = true;
    protected String buttonText = null ;
    protected String type = TYPE_ANATOMY_ONTOLOGY ;
    protected boolean wildCard = true ;

    // later option
    protected int minLookupLenth = 3 ;

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

        addSuggestBoxHandlers() ;


        lookupPanel.add(suggestBox);

        if(buttonText!=null){
            submitButton = new Button(buttonText) ;
            addSubmitButtonHandler();
            lookupPanel.add(submitButton);
        }
        rootPanel.add(lookupPanel);
        if(showError){
            initNoteGui();
        }
        textBox.setFocus(true);
        initWidget(rootPanel);
    }

    protected void addSubmitButtonHandler(){
        submitButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                doSubmit(textBox.getText());
            }
        });

    }

    protected void addSuggestBoxHandlers(){
        suggestBox.addEventHandler(new SuggestionHandler() {
            public void onSuggestionSelected(SuggestionEvent event) {
                suggestion = event.getSelectedSuggestion();
                if (suggestion.getReplacementString() == null) {
                    suggestBox.setText(currentText);
                    doSubmit(currentText);
                } else if (suggestion.getReplacementString() != null) {
                    doSubmit(suggestion.getReplacementString());
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

    }

    protected void initNoteGui(){
        noteLabel.setVisible(false);
        noteLabel.setWordWrap(true);
        rootPanel.add(noteLabel);
    }


    public void setText(String newText){
        textBox.setText(newText);
    }

    protected void doSubmit(String text) {
        if (text != null) {
            Window.open("/action/anatomy/search?action=term-search&searchTerm=" + text.replaceAll(" ", "%20"), "_self",
                    "");
        }
    }

    public void setErrorString(String text){
        noteLabel.setStyleName("gwt-lookup-error");
        this.noteString = text ;
        noteLabel.setText(noteString);
        noteLabel.setVisible(true);
    }

    public void setNoteString(String note) {
        noteLabel.setStyleName("gwt-lookup-note");
        this.noteString = note;
        noteLabel.setText(noteString);
        noteLabel.setVisible(true);
    }

    public String getNoteString() {
        return noteString;
    }

    public void clearError(){
        setNoteString("") ;
    }

    public void hideError(){
        noteLabel.setVisible(false);
    }

    public void showError(){
        noteLabel.setVisible(true);
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

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
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

    public Button getSubmitButton() {
        return submitButton;
    }

    public boolean isWildCard() {
        return wildCard;
    }

    public void setWildCard(boolean wildCard) {
        this.wildCard = wildCard;
    }

    public int getMinLookupLenth() {
        return minLookupLenth;
    }

    public void setMinLookupLenth(int minLookupLenth) {
        this.minLookupLenth = minLookupLenth;
    }
}
