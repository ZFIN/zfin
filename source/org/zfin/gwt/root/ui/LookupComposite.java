package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.event.CheckSubsetEventHandler;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.event.SingleOntologySelectionEventHandler;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a lookup box composite.  It allows the following options:
 * - name of input box
 * - show errors
 * - show button
 * - type (GO, PATO, QUALITY)
 * - show synonyms
 * - ontology the ontology used for lookup purposes
 */
public class LookupComposite extends Composite implements Revertible {

    public static final String TERM_INFO_USED = "termInfoUsed";
    // gui components
    private HorizontalPanel lookupPanel = new HorizontalPanel();
    private ItemSuggestOracle oracle = new ItemSuggestOracle(this);
    private TextBox textBox = new TextBox();
    private ListBox ontologySelector = new ListBox();
    private CheckBox ontologyChecker = new CheckBox();
    private CheckBox allTermsCheckbox = new CheckBox();
    private SuggestBox suggestBox;
    private SuggestOracle.Suggestion suggestion = null;

    private Button submitButton;
    private String currentText = null;
    private HTML noteLabel = new HTML("", true);
    private VerticalPanel rootPanel = new VerticalPanel();
    private TermInfoComposite termInfoTable;


    // internal ui data
    private String noteString = "";
    private String errorString = "";
    private boolean suggestBoxHasFocus = true;
    private boolean useTermInfoUpdates = true;

    // lookup types
    public final static String GENEDOM_AND_EFG = "GENEDOM_AND_EFG_LOOKUP";
    public final static String GENEDOM = "GENEDOM";
    public final static String MARKER_LOOKUP = "MARKER_LOOKUP";
    public final static String ANTIBODY_LOOKUP = "ANTIBODY_LOOKUP";
    public final static String TYPE_SUPPLIER = "SUPPLIER";
    public final static String FEATURE_LOOKUP = "FEATURE_LOOKUP";
    public final static String GDAG_TERM_LOOKUP = "GDAG_TERM_LOOKUP";
    public static final String MARKER_LOOKUP_AND_TYPE = "MARKER_LOOKUP_AND_TYPE";
    public static final String CONSTRUCT_LOOKUP = "CONSTRUCT_LOOKUP";
    private Collection<String> types = new ArrayList<String>(10);
    private OntologyDTO ontology;
    private String pubZdb;
    private boolean validatedTerm = true;

    // variables
    private final static String EMPTY_STRING = "&nbsp;";

    // actions
    public final static String ACTION_ANATOMY_SEARCH = "ANATOMY_SEARCH";
    public final static String ACTION_TERM_SEARCH = "TERM_SEARCH";
    public final static String ACTION_GENEDOM_AND_EFG_SEARCH = "GENEDOM_AND_EFG_SEARCH";
    public final static String ACTION_MARKER_ATTRIBUTE = "MARKER_ATTRIBUTE";
    public final static String ACTION_FEATURE_ATTRIBUTE = "FEATURE_ATTRIBUTE";
    private SubmitAction action = null;
    private HighlightAction highlightAction = null;
    private String onclick;
    private boolean checkForRelationalSubset;
    private CheckSubsetEventHandler subsetEventHandler;
    private SingleOntologySelectionEventHandler singleOntologySelectionHandler;

    // options
    protected String inputName = "search";
    protected boolean showError = true;
    protected String buttonText = null;
    protected String type = GDAG_TERM_LOOKUP;
    protected boolean wildCard = true;
    protected int suggestBoxWidth = 30;
    protected String oId = null;
    protected int limit = ItemSuggestOracle.NO_LIMIT;
    protected boolean submitOnEnter = false;

    // later option
    private int minLookupLength = 2;
    private static final String TERM_INFO = "term-info";

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    public static final String SHOW_TYPE = "SHOW_TYPE";
    private boolean useIdAsValue = false;
    private boolean useTermsWithDataOnly = false;
    private boolean useAnatomyTermsOnly = false;

    private String suggestPopupStyleName = "gwt-SuggestBoxPopup";

    private TermDTO selectedTerm;

    public LookupComposite() {
        types.add(TYPE_SUPPLIER);
        types.add(MARKER_LOOKUP);
        types.add(MARKER_LOOKUP_AND_TYPE);
        types.add(ANTIBODY_LOOKUP);
        types.add(GENEDOM_AND_EFG);
        types.add(FEATURE_LOOKUP);
        types.add(GDAG_TERM_LOOKUP);
        types.add(CONSTRUCT_LOOKUP);
    }

    public LookupComposite(boolean useTermInfoUpdates) {
        this();
        this.useTermInfoUpdates = useTermInfoUpdates;
        if (useTermInfoUpdates)
            suggestPopupStyleName += " " + TERM_INFO_USED;
    }

    public LookupComposite(boolean useTermInfoUpdates, String entityName) {
        this(useTermInfoUpdates);
        this.useTermInfoUpdates = useTermInfoUpdates;
        suggestPopupStyleName += " " + entityName;
    }

    public void initGui() {
        textBox.setName(inputName);
        textBox.setTitle(inputName);
        textBox.getElement().setPropertyString("id", inputName);
        textBox.getElement().setPropertyString("autocomplete", "off");
        textBox.setVisibleLength(suggestBoxWidth);
        textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                markUnValidateText();
            }
        });


        if (RootPanel.get("ontologySelector") != null) {
            initializeOntologySelector();
        }
        if (RootPanel.get("ontologyChecker") != null) {
            initializeOntologyChecker();
        }
        if (RootPanel.get("useAllTerms") != null) {
            initializeAllTermsCheckbox();
        }
/*
        DeferredCommand.addCommand(new Command() {
            @Override
            public void execute() {
                textBox.setFocus(true);
            }
        });
*/

        ontologySelector.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                String selectedOntology = ontologySelector.getValue(ontologySelector.getSelectedIndex());
                setOntologyName(selectedOntology);
                // set focus back on the entry box
                textBox.setFocus(true);
            }
        });

        ontologyChecker.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                if (booleanValueChangeEvent.getValue())
                    setOntologyName(OntologyDTO.ANATOMY.getOntologyName());
                else
                    setOntologyName(OntologyDTO.AOGO.getOntologyName());
                // set focus back on the entry box
                textBox.setFocus(true);
            }
        });
        if (useAnatomyTermsOnly)
            ontologyChecker.setValue(true);

        allTermsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                //Window.alert("Check: "+booleanValueChangeEvent.getValue());
                setUseTermsWithDataOnly(booleanValueChangeEvent.getValue());
                // set focus back on the entry box
                textBox.setFocus(true);
            }
        });

        /*textBox.addFocusHandler((new FocusHandler() {
            public void onFocus(FocusEvent event) {
                textBox.setValue("");
                textBox.setStyleName("gwt-lookup-error");

            }
        }); */

        SuggestBox.DefaultSuggestionDisplay suggestDisplay = new SuggestBox.DefaultSuggestionDisplay();
        suggestDisplay.setPopupStyleName(suggestPopupStyleName);
        suggestBox = new SuggestBox(oracle, textBox, suggestDisplay);
        addSuggestBoxHandlers();

        lookupPanel.add(suggestBox);

        if (buttonText != null) {
            submitButton = new Button(buttonText);
            addSubmitButtonHandler();
            lookupPanel.add(submitButton);
        }
        rootPanel.add(lookupPanel);
        if (showError) {
            initNoteGui();
        }
        //textBox.setFocus(true);
        initWidget(rootPanel);
        //Window.alert("use Term info: "+useTermInfoUpdates);
        if (useTermInfoUpdates) {
            RootPanel panel = RootPanel.get(TERM_INFO);
            if (termInfoTable == null) {
                termInfoTable = new TermInfoComposite(false);
                termInfoTable.clear();
            }
            if (panel != null)
                panel.add(termInfoTable);

            if (highlightAction == null) {
                MyHighlightAction highlightAction1 = new MyHighlightAction();
                setHighlightAction(highlightAction1);
            }
            exposeTermInfoUpdateToJavascript(this);
        }
    }

    public void setTermInfoComposite(TermInfoComposite termInfoComposite) {
        this.termInfoTable = termInfoComposite;
    }

    private void initializeOntologySelector() {
        ontologySelector = new ZfinListBox(false);
        ontologySelector.addItem("Select Ontology");
        ontologySelector.addItem("Anatomy", OntologyDTO.ANATOMY.getOntologyName());
        ontologySelector.addItem("GO: Cellular Components", OntologyDTO.GO_CC.getOntologyName());
        ontologySelector.addItem("GO: Biological Processes", OntologyDTO.GO_BP.getOntologyName());
        ontologySelector.addItem("GO: Molecular Function", OntologyDTO.GO_MF.getOntologyName());
        // hacky as you have no way to find the entries in the widget any more...
        // could hard-code the list and know the index from there as well.
        for (int index = 0; index < 5; index++) {
            if (ontologySelector.getValue(index).equals(getOntology().getOntologyName()))
                ontologySelector.setSelectedIndex(index);
        }
        RootPanel.get("ontologySelector").add(ontologySelector);
    }

    private void initializeOntologyChecker() {
        RootPanel.get("ontologyChecker").add(ontologyChecker);
    }

    private void initializeAllTermsCheckbox() {
        // by default it is enabled
        allTermsCheckbox.setValue(true);
        RootPanel.get("useAllTerms").add(allTermsCheckbox);
    }

    void addSubmitButtonHandler() {
        submitButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                doSubmit(ontology.getOntologyName() + ":" + textBox.getText());
            }
        });

    }

    void addSuggestBoxHandlers() {
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent event) {
                suggestion = (SuggestOracle.Suggestion) event.getSelectedItem();
                final String termID = suggestion.getReplacementString();
                String displayString = suggestion.getDisplayString();
                if (ontology != null) {
                    SelectAutoCompleteEvent selectEvent = new SelectAutoCompleteEvent(termID, displayString, ontology);
                    AppUtils.EVENT_BUS.fireEvent(selectEvent);
                    selectedTerm = new TermDTO();
                    selectedTerm.setOboID(termID);
                    selectedTerm.setOntology(ontology);
                    selectedTerm.setName(extractPureTermNameHtml(displayString).trim());
                }
                if (displayString == null) {
                    suggestBox.setText(extractPureTermNameHtml(displayString));
                    doSubmit(termID);
                } else if (termID != null) {
                    if (useIdAsValue && termInfoTable != null) {
                        lookupRPC.getTermInfo(ontology, termID,
                                new TermInfoCallBack(termInfoTable, termID) {
                                    @Override
                                    public void onSuccess(TermDTO termInfoDTO) {
                                        super.onSuccess(termInfoDTO);
                                        doSubmit(termInfoDTO.getName());
                                    }
                                }
                        );
                    } else {
                        String text = extractPureTermNameHtml(displayString).trim();
                        suggestBox.setText(text);
                        // handle wildcard cases
                        // ToDo: This only works for ontology term auto-completes. Not for other entity lookups that
                        // are used with this logic.
                        if (displayString.contains("*")) {
                            String queryString = "term?name=" + termID + "&ontologyName=" + ontology.getDBName();
                            doSubmit(queryString);
                        } else
                            doSubmit(termID);
                    }
                }
            }
        });

        suggestBox.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent keyDownEvent) {
                if (keyDownEvent.isDownArrow())
                    if (textBox.getText() != null && textBox.getText().length() > 0) {
                        currentText = textBox.getText();
                    }
            }
        });

        suggestBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                if (keyUpEvent.isDownArrow()) {
                    if (textBox.getText() != null && textBox.getText().length() > 0) {
                        currentText = textBox.getText();
                    }
                } else if (submitOnEnter && keyUpEvent.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (suggestBox.getText() != null && suggestBox.getText().trim().length() > 2) {
                        doSubmit(suggestBox.getText().trim());
                    }
                }
            }
        });

        suggestBox.getTextBox().addFocusHandler((new FocusHandler() {
            public void onFocus(FocusEvent event) {
                clearError();
                suggestBoxHasFocus = true;
                // Add logic to popup suggestion upon focus 
                String text = suggestBox.getText();
                if (text == null || text.trim().length() > getMinLookupLength()) {
                    /// do nothing?
                }

            }
        }));

        suggestBox.getTextBox().addBlurHandler((new BlurHandler() {
            public void onBlur(BlurEvent event) {
                clearNote();
                suggestBoxHasFocus = false;
            }
        }));

    }

    void initNoteGui() {
        noteLabel.setVisible(false);
        noteLabel.setWordWrap(true);
        rootPanel.add(noteLabel);
    }


    public void setText(String newText) {
        textBox.setText(newText);
    }

    protected void doOnHighlight(String text) {
        if (highlightAction != null) {
            suggestBox.setFocus(true);
            highlightAction.onHighlight(text);
        }
    }

    public static String extractPureTermNameHtml(String suggestion) {
        if (suggestion == null)
            return "";
        suggestion = suggestion.substring(0, suggestion.indexOf("-->"));
        suggestion = suggestion.replace("<!--", "");
        suggestion = suggestion.replace("-->", "");
        return suggestion;
    }

    protected void doSubmit(final String text) {
        if (action != null) {
            suggestBox.setFocus(false);
            action.doSubmit(text);
        }
        if (onclick != null)
            runOnclickJavaScriptMethod(onclick);
        if (checkForRelationalSubset) {
            // Need to delay this check to allow for the action.submit()
            // to finish first. Otherwise that ajax call is not being processed for some unknown reason!
            // First: populate the term entry field with the correct term
            // Second: check for relational term type.
            DeferredCommand.addCommand(new Command() {
                @Override
                public void execute() {
                    subsetEventHandler.onEvent(text);
                }
            });
        }
        //setToSingleOntologySelection(text);
        unsetUnValidatedTextMarkup();
    }

    private void setToSingleOntologySelection(final String termID) {
        DeferredCommand.addCommand(new Command() {
            @Override
            public void execute() {
                singleOntologySelectionHandler.onEvent(termID);
            }
        });
    }

    private native void runOnclickJavaScriptMethod(String name)/*-{
        $wnd.submitForm(string);
    }-*/;

    private native void exposeTermInfoUpdateToJavascript(LookupComposite lookupComposite)/*-{
        $wnd.updateTermInfo = function (termName, ontologyName) {
            lookupComposite.@org.zfin.gwt.root.ui.LookupComposite::updateTermInfo(Ljava/lang/String;Ljava/lang/String;)(termName, ontologyName);
        };
    }-*/;

    public void setErrorString(String text) {
        noteLabel.setStyleName("gwt-lookup-error");
        errorString = text;
        noteString = "";
        noteLabel.setHTML(errorString);
        if (StringUtils.isNotEmptyTrim(text))
            noteLabel.setVisible(true);
        else
            noteLabel.setVisible(false);
    }

    public void setNoteString(String note) {
        noteLabel.setStyleName("gwt-lookup-note");
        noteString = note;
        errorString = "";
        noteLabel.setHTML(noteString);
        if (StringUtils.isNotEmptyTrim(note))
            noteLabel.setVisible(true);
        else
            noteLabel.setVisible(false);
    }

    public String getNoteString() {
        return noteString;
    }

    public void clearError() {
        if (errorString.length() > 0 && false == errorString.equals(EMPTY_STRING)) {
            setErrorString("");
        }
    }

    public void clearNote() {
        if (noteString.length() > 0 && false == noteString.equals(EMPTY_STRING)) {
            setNoteString("");
        }
    }

    public void hideError() {
        noteLabel.setVisible(false);
    }

    public void showError() {
        noteLabel.setVisible(true);
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (false == types.contains(type)) {
            StringBuilder typeList = new StringBuilder(10);

            for (Object type1 : types) {
                typeList.append(type1.toString());
            }

            throw new RuntimeException("Type " + type + " not recognized.  Try: \n" + typeList);
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


    public String getCurrentText() {
        return currentText;
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

    public int getMinLookupLength() {
        return minLookupLength;
    }

    public void setMinLookupLength(int minLookupLength) {
        this.minLookupLength = minLookupLength;
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

    public boolean isSuggestBoxHasFocus() {
        return suggestBoxHasFocus;
    }

    public void setSuggestBoxHasFocus(boolean suggestBoxHasFocus) {
        this.suggestBoxHasFocus = suggestBoxHasFocus;
    }

    public String getOId() {
        return oId;
    }

    public void setOId(String oId) {
        this.oId = oId;
    }

    public String getOnclick() {
        return onclick;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public String getText() {
        return suggestBox.getText();
    }

    public void setOntologyName(String ontologyName) {
        ontology = OntologyDTO.getOntologyByDescriptor(ontologyName);
    }

    public void setOntology(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public OntologyDTO getOntology() {
        return ontology;
    }

    public void setPubZdb(String pubZdb) {
        this.pubZdb = pubZdb;
    }

    public String getPubZdb() {
        return this.pubZdb;
    }

    public void addOnFocusHandler(FocusHandler autocompleteFocusHandler) {
        suggestBox.getTextBox().addFocusHandler(autocompleteFocusHandler);
    }

    public void addOnClickHandler(ClickHandler clickHandler) {
        suggestBox.getTextBox().addClickHandler(clickHandler);
    }

    public void setLimit(int thisLimit) {
        oracle.setLimit(thisLimit);
    }

    public void setEnabled(boolean enabled) {
        textBox.setEnabled(enabled);
    }

    /**
     * Display the term info for a given term ID in a given ontology.
     *
     * @param ontologyName Ontology Name
     * @param termID       term ID: zdb ID or obo ID
     */
    public void showTermInfo(String ontologyName, String termID) {
        //Window.alert("Show Term:: " + ontology + ":" + termID);
        OntologyDTO ontology = OntologyDTO.getOntologyByDescriptor(ontologyName);
        lookupRPC.getTermInfo(ontology, termID, new TermInfoCallBack(termInfoTable, termID));
    }


    public void setHighlightAction(HighlightAction highlightAction) {
        this.highlightAction = highlightAction;
    }

    @Override
    public boolean isDirty() {
        return (textBox.getText() != null && textBox.getText().trim().length() > 0);
    }

    @Override
    public boolean handleDirty() {
        return false;
    }

    public boolean isSubmitOnEnter() {
        return submitOnEnter;
    }

    public void setSubmitOnEnter(boolean submitOnEnter) {
        this.submitOnEnter = submitOnEnter;
    }

    @Override
    public void working() {
        textBox.setEnabled(false);
        submitButton.setEnabled(false);
    }

    @Override
    public void notWorking() {
        textBox.setEnabled(true);
        submitButton.setEnabled(true);
    }

    public void setTabIndex(int tabIndex) {
        textBox.setTabIndex(tabIndex);
    }

    public void setUseIdAsValue(boolean useIdAsValue) {
        this.useIdAsValue = useIdAsValue;
    }

    public boolean getUseIdAsValue() {
        return useIdAsValue;
    }

    public boolean isUseTermsWithDataOnly() {
        return useTermsWithDataOnly;
    }

    public void setUseTermsWithDataOnly(boolean useTermsWithDataOnly) {
        this.useTermsWithDataOnly = useTermsWithDataOnly;
    }

    public boolean isUseAnatomyTermsOnly() {
        return useAnatomyTermsOnly;
    }

    public void setUseAnatomyTermsOnly(boolean useAnatomyTermsOnly) {
        this.useAnatomyTermsOnly = useAnatomyTermsOnly;
    }

    public TermInfoComposite getTermInfoTable() {
        return termInfoTable;
    }

    public void setTermInfoTable(TermInfoComposite termInfoTable) {
        this.termInfoTable = termInfoTable;
    }

    public boolean isSuggestionListShowing() {
        return suggestBox.isSuggestionListShowing();
    }

    public void addOnBlurHandler(BlurHandler blurHandler) {
        textBox.addBlurHandler(blurHandler);
    }

    public void setCheckForRelationalSubset(boolean checkForRelationalSubset) {
        this.checkForRelationalSubset = checkForRelationalSubset;
    }

    public void setSubsetEventHandler(CheckSubsetEventHandler subsetEventHandler) {
        this.subsetEventHandler = subsetEventHandler;
    }

    public void setSingleOntologySelectionEventHandler(SingleOntologySelectionEventHandler singleOntologySelectionEventHandler) {
        this.singleOntologySelectionHandler = singleOntologySelectionEventHandler;
    }

    public void markUnValidateText() {
        textBox.setStyleName("error");
        validatedTerm = false;
    }

    public void unsetUnValidatedTextMarkup() {
        String styleClass = textBox.getStyleName();
        styleClass = styleClass.replace("error", "");
        textBox.setStyleName(styleClass);
        validatedTerm = true;
    }

    // Check if the term is validated (black) or un-validated (red)
    public boolean hasValidateTerm() {
        if (textBox.getText().trim().equals(""))
            return true;
        return validatedTerm;
    }

    public boolean hasValidNonNullTerm() {
        if (textBox.getText().trim().equals(""))
            return false;
        return validatedTerm;
    }

    public void setValidationStyle(boolean isValidated) {
        String styleClass = textBox.getStyleName();
        if (isValidated) {
            unsetUnValidatedTextMarkup();
        } else {
            markUnValidateText();
        }
    }

    public void updateTermInfo(String termName, String ontologyName) {
        if (termName != null && false == termName.startsWith(ItemSuggestCallback.END_ELLIPSIS)) {
            OntologyDTO ontology = OntologyDTO.getOntologyByName(ontologyName);
            lookupRPC.getTermByName(ontology, termName, new TermInfoCallBack(termInfoTable, termName));
        }
    }

    public void resetSelectedTerm() {
        selectedTerm = null;
    }

    public void setSelectedTerm(TermDTO selectedTerm) {
        this.selectedTerm = selectedTerm;
    }

    private class MyHighlightAction implements HighlightAction {
        @Override
        public void onHighlight(String termID) {
            if (termID != null && false == termID.startsWith(ItemSuggestCallback.END_ELLIPSIS)) {
                lookupRPC.getTermInfo(ontology, termID, new TermInfoCallBack(termInfoTable, termID));
            }
        }
    }

    public TermDTO getSelectedTerm() {
        return selectedTerm;
    }
}
