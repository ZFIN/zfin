package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.*;

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
    //    protected Label noteLabel = new Label();
    protected HTML noteLabel = new HTML("",true);
    protected VerticalPanel rootPanel = new VerticalPanel() ;

    // internal ui data
    protected String noteString = "" ;
    protected String errorString = "" ;
    protected boolean suggetBoxHasFocus = true ;

    // lookup types
    public final static String TYPE_ANATOMY_ONTOLOGY = "ANATOMY_ONTOLOGY" ;
    public final static String TYPE_GENE_ONTOLOGY= "GENE_ONTOLOGY" ;
    public final static String TYPE_QUALITY = "QUALITY_ONTOLOGY" ;
    public final static String GENEDOM_AND_EFG = "GENEDOM_AND_EFG_LOOKUP" ;
    public final static String MARKER_LOOKUP = "MARKER_LOOKUP" ;
    public final static String TYPE_SUPPLIER = "SUPPLIER" ;
    public final static String FEATURE_LOOKUP = "FEATURE_LOOKUP" ;
    private List types = new ArrayList() ;

    // variables
    private final static String EMPTY_STRING  = "&nbsp;" ;

    // actions
    public final static String ACTION_ANATOMY_SEARCH= "ANATOMY_SEARCH" ;
    public final static String ACTION_GENEDOM_AND_EFG_SEARCH= "GENEDOM_AND_EFG_SEARCH" ;
    public final static String ACTION_MARKER_ATTRIBUTE = "MARKER_ATTRIBUTE" ;
    public final static String ACTION_FEATURE_ATTRIBUTE = "FEATURE_ATTRIBUTE" ;
    private SubmitAction action = null ;
    private String onclick;

    // options
    protected String inputName = "search";
    protected boolean showError = true;
    protected String buttonText = null ;
    protected String type = TYPE_ANATOMY_ONTOLOGY ;
    protected boolean wildCard = true ;
    protected int suggestBoxWidth = 30 ;
    protected String OID = null ;

    // later option
    protected int minLookupLenth = 3 ;

    public LookupComposite(){
        types.add(TYPE_ANATOMY_ONTOLOGY) ;
        types.add(TYPE_GENE_ONTOLOGY) ;
        types.add(TYPE_QUALITY) ;
        types.add(TYPE_SUPPLIER) ;
        types.add(MARKER_LOOKUP) ;
        types.add(GENEDOM_AND_EFG) ;
        types.add(FEATURE_LOOKUP) ;
    }

    public void initGui(){
        textBox.setName(inputName);
        textBox.setTitle(inputName);
        DOM.setElementProperty(textBox.getElement(), "id", inputName);
        textBox.setVisibleLength(suggestBoxWidth);
        DOM.setElementAttribute(textBox.getElement(), "autocomplete", "off");
        suggestBox = new SuggestBox(oracle, textBox);

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
            public void onKeyDown(Widget w, char c, int i) {
                if(c== KeyboardListener.KEY_ESCAPE){
                    suggestBox.setText("");
                }
                if (textBox.getText() != null && textBox.getText().length() > 0 &&
                        (
                                c != KeyboardListener.KEY_DELETE
                                        &&
                                        c != KeyboardListener.KEY_BACKSPACE
                        )
                        ){
                    currentText = textBox.getText()  ;
                    if( Character.isLetterOrDigit(c)){
                        currentText += c ;
                    }

                    if( c == KeyboardListener.KEY_ENTER
                            &&
                            textBox.getText().length()>= getMinLookupLenth()
                            ){
                        // must defer the command in case selection are entered concurrently
                        DeferredCommand.addCommand(new Command(){
                            public void execute() {
                                if(textBox.getText().length()>= getMinLookupLenth()){
                                    doSubmit(textBox.getText());
                                }
                                //To change body of implemented methods use File | Settings | File Templates.
                            }
                        });
                    }
                }
                else
                if (textBox.getText() == null || textBox.getText().length() == 0 ||
                        ( textBox.getText().length()==1
                                &&
                                (c== KeyboardListener.KEY_DELETE || c== KeyboardListener.KEY_BACKSPACE)
                        )
                        )
                {
                    handleNoText();
                }
            }
        });

        suggestBox.addFocusListener(new FocusListener(){
            public void onLostFocus(Widget widget) {
                clearNote();
                suggetBoxHasFocus = false ;
            }

            public void onFocus(Widget widget) {
                clearError();
                suggetBoxHasFocus = true; 
            }
        });


    }

    protected void handleNoText(){
        setNoteString("Enter "+getMinLookupLenth()+" or more letters");
    }

    protected void initNoteGui(){
        noteLabel.setVisible(true);
        noteLabel.setWordWrap(true);
        rootPanel.add(noteLabel);
        handleNoText();
    }


    public void setText(String newText){
        textBox.setText(newText);
    }

    protected void doSubmit(String text) {
        if(action != null){
            suggestBox.setFocus(false);
            action.doSubmit(text);
        }
        if(onclick != null)
            runOnclickJavaScriptMethod(onclick);
    }
    private native void runOnclickJavaScriptMethod(String string)/*-{
            $wnd.submitForm(string);
    }-*/;

    public void setErrorString(String text){
        noteLabel.setStyleName("gwt-lookup-error");
        errorString = text ;
        noteString="" ;
        noteLabel.setHTML(errorString);
        noteLabel.setVisible(true);
    }

    public void setNoteString(String note) {
        noteLabel.setStyleName("gwt-lookup-note");
        noteString = note;
        errorString= "" ;
        noteLabel.setHTML(noteString);
        noteLabel.setVisible(true);
    }

    public String getNoteString() {
        return noteString;
    }

    public void clearError(){
        if(errorString.length()>0 && false==errorString.equals(EMPTY_STRING)){
            setErrorString(EMPTY_STRING) ;
        }
    }

    public void clearNote(){
        if(noteString.length()>0 && false==noteString.equals(EMPTY_STRING)){
            setNoteString(EMPTY_STRING) ;
        }
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

    public int getSuggestBoxWidth() {
        return suggestBoxWidth;
    }

    public void setSuggestBoxWidth(int suggestBoxWidth) {
        this.suggestBoxWidth = suggestBoxWidth;
    }


    public SubmitAction getAction() {
        return action;
    }

    public void setAction(SubmitAction action) {
        this.action = action;
    }

    public boolean isSuggetBoxHasFocus() {
        return suggetBoxHasFocus;
    }

    public void setSuggetBoxHasFocus(boolean suggetBoxHasFocus) {
        this.suggetBoxHasFocus = suggetBoxHasFocus;
    }

    public String getOID() {
        return OID;
    }

    public void setOID(String OID) {
        this.OID = OID;
    }

    public String getOnclick() {
        return onclick;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }
}
