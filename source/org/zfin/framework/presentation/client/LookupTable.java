package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.Dictionary;

import java.util.*;


/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class LookupTable extends Lookup{

    private FlexTable table = new FlexTable() ;
    private Label testLabel = new Label() ;
    private Element hiddenList = null ;
    private String separator = "," ;
    private List termList = new ArrayList() ;
    public static final String JSREF_HIDDEN_NAME ="hiddenName" ;
    public static final String JSREF_PREVIOUS_TABLE_VALUES ="previousTableValues" ;
    public static final String JSREF_IMAGE_URL ="imageURL" ;

    private String hiddenName = "hiddenName";
    private String imageURL = "/gwt/org.zfin.framework.presentation.LookupTable/";

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
            setPreviousTableNames(lookupProperties.get(JSREF_PREVIOUS_TABLE_VALUES));
        }


        exposeMethodToJavascript(this);
    }

    public void addTermToTable(String term){
        if(false==termList.contains(term)){
            termList.add(term) ;

            table.insertRow( table.getRowCount()) ;
            int currentRow = table.getRowCount() -1 ;
            table.setText(currentRow,1,term);
            Image removeImage = new Image(getImageURL()+"action_delete.png") ;
            table.setWidget(currentRow,0,new RemoveButton(removeImage,this,term));

            DOM.setElementProperty(hiddenList, "value", generateListValues());

            lookup.getTextBox().setText("");
        }
    }


    public void clearTable(){
        termList.clear();
        while(table.getRowCount()>0){
            table.removeRow(0);
        }
        DOM.setElementProperty(hiddenList, "value", "");
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
          }
    }-*/;

    /**
     * A comma separated list of terms
     * @param termList
     */
    private void setPreviousTableNames(String termList){
        String[] terms  = termList.split(",") ;
        String term ;
        for(int i = 0 ; i < terms.length ; i++){
            term = terms[i] ;
            addTermToTable(term);
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
}
