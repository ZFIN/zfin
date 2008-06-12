package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.i18n.client.Dictionary;

import java.util.*;


/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class LookupTable extends Lookup {

    private FlexTable table = new FlexTable() ;
    private Label testLabel = new Label() ;
    private Element hiddenList = null ;
    private PhenotePopup phenotePopup = null ;
    private String separator = "," ;
    private List termList = new ArrayList() ;
    public static final String JSREF_HIDDEN_NAME ="hiddenName" ;
    public static final String JSREF_PREVIOUS_TABLE_VALUES ="previousTableValues" ;
    public static final String JSREF_IMAGE_URL ="imageURL" ;

    private String hiddenName = "hiddenName";
    private String imageURL = "/gwt/org.zfin.framework.presentation.LookupTable/";
    private TermStatus termStatus = new TermStatus();
    private String noTerms = "Enter search terms" ;
    private String divName ;

    private LookupComposite lookup ;

    public void onModuleLoad() {

        lookup = new LookupInTableComposite(this) ;
        handleProperties() ; 
        Dictionary lookupProperties = Dictionary.getDictionary("LookupProperties") ;
        // set options
        Set keySet = lookupProperties.keySet() ;
        if(keySet.contains(JSREF_HIDDEN_NAME)){
            setHiddenName(lookupProperties.get(JSREF_HIDDEN_NAME));
        }

        // init gui
        lookup.initGui();


        hiddenList = DOM.getElementById(hiddenName) ;

        initTable() ;

//        table.setBorderWidth(1);
        RootPanel.get(getDivName()).add(table);

        HorizontalPanel horizPanel = new HorizontalPanel() ;
        horizPanel.add(lookup) ;
        horizPanel.add(testLabel) ;
//        horizPanel.add(hiddenList) ;
        RootPanel.get(getDivName()).add(horizPanel);

        if(keySet.contains(JSREF_IMAGE_URL)){
            setImageURL(lookupProperties.get(JSREF_IMAGE_URL));
        }

        if(keySet.contains(JSREF_PREVIOUS_TABLE_VALUES)){
            prepopulateTable(lookupProperties.get(JSREF_PREVIOUS_TABLE_VALUES));
        }

        lookup.handleNoText();


        exposeMethodToJavascript(this);
    }

    private void handleProperties(){
        Dictionary lookupProperties = Dictionary.getDictionary(LOOKUP_STRING) ;
        Set keySet = lookupProperties.keySet() ;
        if(keySet.contains(JSREF_INPUT_NAME)){
            lookup.setInputName(lookupProperties.get(JSREF_INPUT_NAME));
        }
        if(keySet.contains(JSREF_TYPE)){
            lookup.setType(lookupProperties.get(JSREF_TYPE));
        }
        if(keySet.contains(JSREF_BUTTONTEXT)){
            lookup.setButtonText(lookupProperties.get(JSREF_BUTTONTEXT));
        }
        if(keySet.contains(JSREF_SHOWERROR)){
            lookup.setShowError(Boolean.valueOf(lookupProperties.get(JSREF_SHOWERROR)).booleanValue());
        }
        if(keySet.contains(JSREF_WILDCARD)){
            lookup.setWildCard(Boolean.valueOf(lookupProperties.get(JSREF_WILDCARD)).booleanValue());
        }
        if(keySet.contains(JSREF_WIDTH)){
            lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(JSREF_WIDTH)));
        }
        if(keySet.contains(JSREF_DIV_NAME)){
            setDivName(lookupProperties.get(JSREF_DIV_NAME));
        }


    }

    private void initTable(){
        table.setStyleName("gwt-table-empty");
        table.insertRow(0) ;
        table.setText(0,0,noTerms);
    }

    public void addTermToTable(final TermStatus term){
        if(false==termList.contains(term.getTerm())){
            if(termList.size()==0 && table.getRowCount()>0){
                table.removeRow(0);
                table.setStyleName("gwt-table-full");
            }
            termList.add(term.getTerm()) ;
            table.insertRow( table.getRowCount()) ;
            int currentRow = table.getRowCount() -1 ;

            final Hyperlink link = new Hyperlink(term.getTerm(),false,term.getTerm()+"#bottom") ;
            link.addClickListener(new ClickListener(){
                public void onClick(Widget widget) {
                // this is new each time . . I think so that we never inadvertenly browse to new events
                    phenotePopup = new PhenotePopup(term.getZdbID()) ;
                }
            });

            table.setWidget(currentRow,1,link);
            Image removeImage = new Image(getImageURL()+"action_delete.png") ;
            table.setWidget(currentRow,0,new RemoveButton(removeImage,this,term.getTerm()));
            DOM.setElementProperty(hiddenList, "value", generateListValues());
            lookup.getTextBox().setText("");
            lookup.clearError();
            lookup.clearNote();
            lookup.handleNoText();
        }
        // if contained, need to mention
        else{
            lookup.setErrorString("Term '"+term.getTerm()+"' already added");
        }
    }


    /**
     * Exposed javascript method to remove table contents and reflect in hidden.
     */
    public void clearTable(){
        termList.clear();
        while(table.getRowCount()>0){
            table.removeRow(0);
        }
        DOM.setElementProperty(hiddenList, "value", "");
        lookup.clearError();
        lookup.clearNote();
        lookup.setText("");
        initTable();
        lookup.handleNoText();
    }

    /**
     * Exposed javascript method to remove table contents.
     */
    public String validateLookup(){
        String term = lookup.getTextBox().getText() ;
        if(term != null && term.length()>=lookup.getMinLookupLenth()){
            LookupService.App.getInstance().validateAnatomyTerm( term, new AsyncCallback(){
                public void onFailure(Throwable throwable) {
//                    lookup.setErrorString(throwable.toString());
                    termStatus.setStatus(TermStatus.TERM_STATUS_FAILURE);
                    //To change body of implemented mvn st -qthods use File | Settings | File Templates.
                }

                public void onSuccess(Object o) {
                    termStatus = (TermStatus) o ;
                    String textBoxTerm = lookup.getTextBox().getText() ;
                    termStatus.setTerm(textBoxTerm);
                    // checking length catching any asynchronous updates
                    if(termStatus.isExactMatch() && textBoxTerm.length()>0){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_EXACT);
                        addTermToTable(termStatus);
                    }
                    else
                    if(termStatus.isFoundMany()){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_MANY);
                        lookup.setErrorString("Multiple terms match '"+textBoxTerm+"'" +
                                "<br>Please select a single term");
                    }
                    else
                    if(termStatus.isNotFound()){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_NONE);
                        lookup.setErrorString("No match for term '"+textBoxTerm+"'");
                    }
                }
            });
        }
        else{
            lookup.getTextBox().setText("");
            termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_NONE);
        }

        if(termStatus!=null){
            return termStatus.getStatus();
        }
        else {
            return null ;
        }
    }


    /**
     * Terms show be on the same row
     * @param term
     */
    public void removeTermFromTable(String term){

        int rowIndex = getRowIndexForTerm(term) ;
        if(rowIndex<0){
            Window.alert("term not found to delete: "+term);
        }
        else{
            table.removeRow(rowIndex);
            termList.remove(rowIndex) ;
        }

        DOM.setElementAttribute(hiddenList, "value", generateListValues());
        if(termList.size()==0){
            initTable();
        }
    }

    private int getRowIndexForTerm(String term){

        int selectedRow = -1 ;
        for(int i = 0 ; i < table.getRowCount() ; i++){
            if(table.getText(i,1).equalsIgnoreCase(term)){
                return i ;
            }
        }
        return selectedRow ;

    }

    public String generateListValues(){

        String returnList = "" ;

        Iterator iter = termList.iterator() ;
        String value ;
        while(iter.hasNext()){
            value = iter.next().toString() ;
            returnList += value ;
            if(iter.hasNext()){
                returnList += separator ;
            }
        }
        return returnList ;
    }


    private native void exposeMethodToJavascript(LookupTable lookupTable)/*-{
        $wnd.clearTable = function(){
            lookupTable.@org.zfin.framework.presentation.client.LookupTable::clearTable()();
            return ; 
        }

        $wnd.validateLookup = function(){
            lookupTable.@org.zfin.framework.presentation.client.LookupTable::validateLookup()();
            return ;
        }

        $wnd.getValidationStatus = function(){
            return lookupTable.@org.zfin.framework.presentation.client.LookupTable::getValidationStatus()();
        }

        $wnd.useTerm = function(term){
            return lookupTable.@org.zfin.framework.presentation.client.LookupTable::useTerm(Ljava/lang/String;)(term);
        }

        $wnd.hideTerm = function(term){
            return lookupTable.@org.zfin.framework.presentation.client.LookupTable::hideTerm()();
        }

    }-*/;


    /**
     *  Called externally to hide the phenote lookup.
     */
    public void hideTerm(){
        phenotePopup.hide() ;
        phenotePopup = null ; 
    }


    /**
     *  Called externally to use the current phenote term.
     */
    public void useTerm(String term){
        lookup.getTextBox().setText(term);
        validateLookup() ;
    }

    public String getValidationStatus(){
        if(termStatus==null){
            return null ;
        }
        else{
            return termStatus.getStatus() ; 
        }
    };

    /**
     * A comma separated list of terms
     * @param termList
     */
    private void prepopulateTable(String termList){
        String[] terms  = termList.split(",") ;
        final Set termsNotFound = new HashSet() ;
        final Set termsFoundMany = new HashSet() ;

        for(int i = 0 ; i < terms.length ; i++){
            String tokenizedTerm = terms[i] ;
            LookupService.App.getInstance().validateAnatomyTerm( tokenizedTerm, new AsyncCallback(){
                public void onFailure(Throwable throwable) {
                    lookup.setErrorString(throwable.toString());
                    termStatus.setStatus(TermStatus.TERM_STATUS_FAILURE);
                    //To change body of implemented mvn st -qthods use File | Settings | File Templates.
                }

                public void onSuccess(Object o) {
                    termStatus = (TermStatus) o ;
                    String term = termStatus.getTerm() ;
                    // checking length catching any asynchronous updates
                    if(termStatus.isExactMatch()){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_EXACT);
                        addTermToTable(termStatus);
                    }
                    else
                    if(termStatus.isFoundMany()){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_MANY);
                        termsNotFound.add(term) ;
                    }
                    else
                    if(termStatus.isNotFound()){
                        termStatus.setStatus(TermStatus.TERM_STATUS_FOUND_NONE);
                        termsFoundMany.add(term) ;
                    }
                }
            });

            String errorString = "Error adding terms<br>" ;
            String specialTerm = "";
            if(termsFoundMany.size()>0){
                errorString += "Found too many: " ;
                for (Iterator iter = termsNotFound.iterator() ;
                     iter.hasNext() ;
                     specialTerm = iter.next().toString()){
                    errorString += specialTerm  + " ";
                }
                errorString += "<br>" ;
            }


            if(termsNotFound.size()>0){
                errorString += "Not found: " ;
                for (Iterator iter = termsNotFound.iterator() ;
                     iter.hasNext() ;
                     specialTerm = iter.next().toString()){
                    errorString += specialTerm  + " ";
                }
                errorString += "<br>" ;
            }

            if(termsFoundMany.size()>0 || termsNotFound.size()>0){
                lookup.setErrorString(errorString);
            }
        }
    }



    public String getHiddenName() {
        return hiddenName;
    }

    public void setHiddenName(String hiddenName) {
        this.hiddenName = hiddenName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDivName() {
        return divName;
    }

    public void setDivName(String divName) {
        this.divName = divName;
    }
}
