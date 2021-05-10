package org.zfin.gwt.lookup.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.TermStatus;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.*;


/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class LookupTable extends Lookup implements LookupFieldValidator, HasRemoveTerm {

    // Div elements
    public static final String SEPARATOR = ",";
    public static final String NAME_SEPARATOR = "|";
    public static final String NAME_SEPARATOR_SPLIT = "\\|";

    // GUI elements
    private FlexTable table = new FlexTable();
    private Label label = new Label();
    private Element hiddenNameList = null;
    private Element hiddenIDList = null;
    private Hidden hiddenIDListAsHidden = null;
    private LookupPopup lookupPopup = null;
    private List<TermStatus> termsList = new ArrayList<TermStatus>();

    private String hiddenNames = "hiddenNames";
    private String hiddenIds = "hiddenIDs";
    private String imageURL = "/images/";
    private TermStatus termStatus = new TermStatus();
    private String noTerms = "Enter search terms";
    private String divName;
    private String inputDiv;
    private String termListDiv;
    private boolean useTermTable;

    private LookupComposite lookup;

    public void onModuleLoad(Dictionary dictionary) {

        lookup = new LookupInTableComposite(this);
        lookup.setSubmitOnEnter(true);
        handleProperties(dictionary);
        if (!useTermTable) {
            super.onModuleLoad(dictionary);
            return;
        }
        // init gui
        lookup.initGui();
        hiddenNameList = DOM.getElementById(hiddenNames);
        hiddenIDList = DOM.getElementById(hiddenIds);
        hiddenIDListAsHidden = Hidden.wrap(DOM.getElementById(hiddenIds));
        initTable();

//        table.setBorderWidth(1);
        //If the individual divs are defined, don't use the full gwt layout
        if (termListDiv != null && inputDiv != null) {
            RootPanel.get(inputDiv).add(lookup);
            RootPanel.get(termListDiv).add(table);
            //table.setStyleName("purpletable");
        } else {
            RootPanel.get(divName).add(table);
            HorizontalPanel horizontalPanel = new HorizontalPanel();
            horizontalPanel.add(lookup);
            RootPanel.get(divName).add(horizontalPanel);

        }


        exposeMethodToJavascript(this);
    }

    private void handleProperties(Dictionary lookupProperties) {
        Set keySet = lookupProperties.keySet();
        if (keySet.contains(LookupStrings.JSREF_INPUT_NAME)) {
            lookup.setInputName(lookupProperties.get(LookupStrings.JSREF_INPUT_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_INPUT_DIV)) {
            inputDiv = lookupProperties.get(LookupStrings.JSREF_INPUT_DIV);
        }
        if (keySet.contains(LookupStrings.JSREF_TERM_LIST_DIV)) {
            termListDiv = lookupProperties.get(LookupStrings.JSREF_TERM_LIST_DIV);
        }
        if (keySet.contains(LookupStrings.JSREF_TYPE)) {
            lookup.setType(lookupProperties.get(LookupStrings.JSREF_TYPE));
        }
        if (keySet.contains(LookupStrings.JSREF_ONTOLOGY_NAME)) {
            lookup.setOntologyName(lookupProperties.get(LookupStrings.JSREF_ONTOLOGY_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_BUTTONTEXT)) {
            lookup.setButtonText(lookupProperties.get(LookupStrings.JSREF_BUTTONTEXT));
        }
        if (keySet.contains(LookupStrings.JSREF_SHOWERROR)) {
            lookup.setShowError(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_SHOWERROR)));
        }
        if (keySet.contains(LookupStrings.JSREF_WILDCARD)) {
            lookup.setWildCard(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_WILDCARD)));
        }
        if (keySet.contains(LookupStrings.JSREF_WIDTH)) {
            lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(LookupStrings.JSREF_WIDTH)));
        }
        if (keySet.contains(LookupStrings.JSREF_LIMIT)) {
            lookup.setLimit(Integer.parseInt(lookupProperties.get(LookupStrings.JSREF_LIMIT)));
        }
        if (keySet.contains(LookupStrings.JSREF_DIV_NAME)) {
            setDivName(lookupProperties.get(LookupStrings.JSREF_DIV_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_HIDDEN_NAME)) {
            setHiddenNames(lookupProperties.get(LookupStrings.JSREF_HIDDEN_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_HIDDEN_IDS)) {
            setHiddenIDs(lookupProperties.get(LookupStrings.JSREF_HIDDEN_IDS));
        }
        if (keySet.contains(LookupStrings.JSREF_IMAGE_URL)) {
            setImageURL(lookupProperties.get(LookupStrings.JSREF_IMAGE_URL));
        }

        if (keySet.contains(LookupStrings.JSREF_PREVIOUS_TABLE_VALUES)) {
            prepopulateTable(lookupProperties.get(LookupStrings.JSREF_PREVIOUS_TABLE_VALUES));
        }
        if (keySet.contains(LookupStrings.JSREF_USE_TERM_TABLE)) {
            setUseTermTable(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_USE_TERM_TABLE)));
        }
        if (keySet.contains(LookupStrings.JSREF_TERMS_WITH_DATA_ONLY)) {
            lookup.setUseTermsWithDataOnly(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_TERMS_WITH_DATA_ONLY)));
        }

    }

    private void initTable() {
        table.setStyleName("gwt-table-empty");
//        table.insertRow(0);
//        table.setText(0, 0, noTerms);
    }

    public void addTermToTable(final TermStatus term) {
        if (!termsList.contains(term)) {
            if (termsList.size() == 0 && table.getRowCount() > 0) {
//                table.removeRow(0);
                table.setStyleName("gwt-table-full");
            }
            termsList.add(term);
            table.insertRow(table.getRowCount());
            int currentRow = table.getRowCount() - 1;

            final Hyperlink link = new Hyperlink(term.getTerm(), false, term.getTerm() + "#bottom");
            link.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    // this is new each time . . I think so that we never inadvertently browse to new events
                    lookupPopup = new LookupPopup(lookup.getType(), term.getZdbID());
                    lookupPopup.setAutoHideOnHistoryEventsEnabled(false);
                }
            });

            table.setWidget(currentRow, 1, link);
            Image removeImage = new Image(imageURL + "action_delete.png");
            table.setWidget(currentRow, 0, new RemoveButton(removeImage, this, term.getTerm()));
            DOM.setElementProperty(hiddenIDList, "value", generateIDList());
            String list = generateNameList();
            DOM.setElementProperty(hiddenNameList, "value", list);
            lookup.getTextBox().setText("");
            lookup.clearError();
            lookup.clearNote();

        }
        // if contained, need to mention
        else {
            lookup.setErrorString("Term '" + term.getTerm() + "' already added");
        }
    }


    /**
     * Exposed javascript method to remove table contents and reflect in hidden.
     */
    public void clearTable() {
        termsList.clear();
        while (table.getRowCount() > 0) {
            table.removeRow(0);
        }
        DOM.setElementProperty(hiddenNameList, "value", "");
        DOM.setElementProperty(hiddenIDList, "value", "");
        lookup.clearError();
        lookup.clearNote();
        lookup.setText("");
        initTable();
    }

    /**
     * Exposed javascript method to remove table contents.
     */
    public String validateLookup() {
        final String term = lookup.getTextBox().getText();
        //Window.alert("term: "+ term);
        //Window.alert("term status: "+ termStatus.getStatus());
        if (term != null && term.length() >= lookup.getMinLookupLength() && false == termStatus.isLooking()) {
//            Window.alert("term status making call: "+ termStatus.getStatus());
            termStatus.setStatus(TermStatus.Status.LOOKING);
            LookupRPCService.App.getInstance().validateTerm(term, lookup.getOntology(), new AsyncCallback<TermStatus>() {
                public void onFailure(Throwable throwable) {
//                    lookup.setErrorString(throwable.toString());
                    termStatus.setStatus(TermStatus.Status.FAILURE);
//                    Window.alert("term failed: "+ term);
                    //To change body of implemented mvn st -qthods use File | Settings | File Templates.
                }

                public void onSuccess(TermStatus newTermStatus) {
                    termStatus = newTermStatus;
//                    Window.alert("term success: "+ termStatus.getTerm()+ " - "+ termStatus.getStatus());
                    String textBoxTerm = lookup.getTextBox().getText();
                    // checking length catching any asynchronous updates
                    if (termStatus.isExactMatch() && textBoxTerm.length() > 0) {
                        addTermToTable(termStatus);
                        fireChangeEvent(hiddenIds);
                    } else if (termStatus.isFoundMany()) {
                        termStatus.setStatus(TermStatus.Status.FOUND_MANY);
                        lookup.setErrorString("Multiple terms match '" + textBoxTerm + "'" +
                                "<br>Please select a single term");
                    } else if (termStatus.isNotFound()) {
                        termStatus.setStatus(TermStatus.Status.FOUND_NONE);
                        lookup.setErrorString("No match for term '" + textBoxTerm + "'");
                    }
                }
            });
        } else if (false == termStatus.isLooking()) {
            lookup.getTextBox().setText("");
            termStatus.setStatus(TermStatus.Status.FOUND_NONE);
        }

        if (termStatus != null) {
            return termStatus.getStatus().toString();
        } else {
            return null;
        }
    }


    /**
     * Terms show be on the same row
     *
     * @param term term name
     */
    public void removeTermFromTable(String term) {

        int rowIndex = getRowIndexForTerm(term);
        if (rowIndex < 0) {
            Window.alert("term not found to delete: " + term);
        } else {
            table.removeRow(rowIndex);
            termsList.remove(rowIndex);
        }

        DOM.setElementAttribute(hiddenIDList, "value", generateIDList());
        DOM.setElementAttribute(hiddenNameList, "value", generateNameList());
        if (termsList.isEmpty()) {
            initTable();
        }
        fireChangeEvent(hiddenIds);
        
    }

    private int getRowIndexForTerm(String term) {

        int selectedRow = -1;
        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getText(i, 1).equalsIgnoreCase(term)) {
                return i;
            }
        }
        return selectedRow;

    }

    public String generateIDList() {

        StringBuffer returnList = new StringBuffer(25);

        for (TermStatus value : termsList) {
            returnList.append(value.getZdbID());
            returnList.append(SEPARATOR);
        }
        returnList.deleteCharAt(returnList.length());
        return returnList.toString();
    }

    public String generateNameList() {

        StringBuffer returnList = new StringBuffer(50);

        for (TermStatus value : termsList) {
            returnList.append(value.getTerm());
            returnList.append(NAME_SEPARATOR);
        }
        returnList.deleteCharAt(returnList.length());
        return returnList.toString();
    }

    public static native void fireChangeEvent(String id) /*-{
        $wnd.jQuery('#' + id).change();
    }-*/;


    private native void exposeMethodToJavascript(LookupTable lookupTable)/*-{
        $wnd.clearTable = function(){
            lookupTable.@org.zfin.gwt.lookup.ui.LookupTable::clearTable()();
        };

        $wnd.validateLookup = function(){
            lookupTable.@org.zfin.gwt.lookup.ui.LookupTable::validateLookup()();
        };

        $wnd.getValidationStatus = function(){
            return lookupTable.@org.zfin.gwt.lookup.ui.LookupTable::getValidationStatus()();
        };

        $wnd.useTerm = function(term){
            return lookupTable.@org.zfin.gwt.lookup.ui.LookupTable::useTerm(Ljava/lang/String;)(term);
        };

        $wnd.hideTerm = function(term){
            return lookupTable.@org.zfin.gwt.lookup.ui.LookupTable::hideTerm()();
        };
        
    }-*/;


    /**
     * Called externally to hide the phenote lookup.
     */
    public void hideTerm() {
        lookupPopup.hide();
        lookupPopup = null;
    }


    /**
     * Called externally to use the current Phenote term.
     *
     * @param term term name
     */
    public void useTerm(String term) {
        lookup.getTextBox().setText(term);
        validateLookup();
    }

    public String getValidationStatus() {
        if (termStatus == null) {
            return null;
        } else {
            return termStatus.getStatus().toString();
        }
    }

    /**
     * A comma separated list of terms
     *
     * @param termList term names
     */
    private void prepopulateTable(String termList) {
        String[] terms = termList.split(NAME_SEPARATOR_SPLIT);
        final Set<String> termsNotFound = new HashSet<String>(5);
        final Set<String> termsFoundMany = new HashSet<String>(5);

        for (String tokenizedTerm : terms) {
            LookupRPCService.App.getInstance().validateTerm(tokenizedTerm, lookup.getOntology(), new AsyncCallback<TermStatus>() {
                public void onFailure(Throwable throwable) {
                    lookup.setErrorString(throwable.toString());
                    termStatus.setStatus(TermStatus.Status.FAILURE);
                }

                public void onSuccess(TermStatus newTermStatus) {
                    termStatus = newTermStatus;
                    String term = termStatus.getTerm();
                    // checking length catching any asynchronous updates
                    if (termStatus.isExactMatch()) {
                        termStatus.setStatus(TermStatus.Status.FOUND_EXACT);
                        addTermToTable(termStatus);
                    } else if (termStatus.isFoundMany()) {
                        termStatus.setStatus(TermStatus.Status.FOUND_MANY);
                        termsNotFound.add(term);
                    } else if (termStatus.isNotFound()) {
                        termStatus.setStatus(TermStatus.Status.FOUND_NONE);
                        termsFoundMany.add(term);
                    }
                }
            });

            String errorString = "Error adding terms<br>";
            String specialTerm = "";
            if (!termsFoundMany.isEmpty()) {
                errorString += "Found too many: ";
                for (Iterator iter = termsNotFound.iterator();
                     iter.hasNext();
                     specialTerm = iter.next().toString()) {
                    errorString += specialTerm + " ";
                }
                errorString += "<br>";
            }


            if (!termsNotFound.isEmpty()) {
                errorString += "Not found: ";
                for (Iterator iter = termsNotFound.iterator();
                     iter.hasNext();
                     specialTerm = iter.next().toString()) {
                    errorString += specialTerm + " ";
                }
                errorString += "<br>";
            }

            if (!termsFoundMany.isEmpty() || !termsNotFound.isEmpty()) {
                lookup.setErrorString(errorString);
            }
        }
    }

    public void setHiddenNames(String hiddenNames) {
        this.hiddenNames = hiddenNames;
    }

    public void setHiddenIDs(String hiddenIDs) {
        this.hiddenIds = hiddenIDs;
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

    public boolean isUseTermTable() {
        return useTermTable;
    }

    public void setUseTermTable(boolean useTermTable) {
        this.useTermTable = useTermTable;
    }


}
