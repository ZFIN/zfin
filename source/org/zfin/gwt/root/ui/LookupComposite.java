package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

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
public class LookupComposite extends Composite {

    // gui components
    private HorizontalPanel lookupPanel = new HorizontalPanel();
    private ItemSuggestOracle oracle = new ItemSuggestOracle(this);
    private TextBox textBox = new TextBox();
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

    // lookup types
    public final static String GENEDOM_AND_EFG = "GENEDOM_AND_EFG_LOOKUP";
    public final static String MARKER_LOOKUP = "MARKER_LOOKUP";
    public final static String TYPE_SUPPLIER = "SUPPLIER";
    public final static String FEATURE_LOOKUP = "FEATURE_LOOKUP";
    public final static String GDAG_TERM_LOOKUP = "GDAG_TERM_LOOKUP";
    private Collection<String> types = new ArrayList<String>(10);
    private OntologyDTO ontology;

    // variables
    private final static String EMPTY_STRING = "&nbsp;";

    // actions
    public final static String ACTION_ANATOMY_SEARCH = "ANATOMY_SEARCH";
    public final static String ACTION_GENEDOM_AND_EFG_SEARCH = "GENEDOM_AND_EFG_SEARCH";
    public final static String ACTION_MARKER_ATTRIBUTE = "MARKER_ATTRIBUTE";
    public final static String ACTION_FEATURE_ATTRIBUTE = "FEATURE_ATTRIBUTE";
    private SubmitAction action = null;
    private String onclick;

    // options
    protected String inputName = "search";
    protected boolean showError = true;
    protected String buttonText = null;
    protected String type = GDAG_TERM_LOOKUP;
    protected boolean wildCard = true;
    protected int suggestBoxWidth = 30;
    protected String oId = null;
    protected int limit = ItemSuggestOracle.NO_LIMIT;

    // later option
    private int minLookupLength = 3;
    private static final String TERM_INFO = "term-info";

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public LookupComposite() {
        types.add(TYPE_SUPPLIER);
        types.add(MARKER_LOOKUP);
        types.add(GENEDOM_AND_EFG);
        types.add(FEATURE_LOOKUP);
        types.add(GDAG_TERM_LOOKUP);
    }

    public void initGui() {
        textBox.setName(inputName);
        textBox.setTitle(inputName);
        DOM.setElementProperty(textBox.getElement(), "id", inputName);
        textBox.setVisibleLength(suggestBoxWidth);
        DOM.setElementAttribute(textBox.getElement(), "autocomplete", "off");
        suggestBox = new SuggestBox(oracle, textBox);

        addSuggestBoxHandlers();
        //suggestBox.

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
        textBox.setFocus(true);
        initWidget(rootPanel);
        RootPanel panel = RootPanel.get(TERM_INFO);
        if (panel != null) {
            termInfoTable = new TermInfoComposite(false);
            termInfoTable.clear();
            panel.add(termInfoTable);
        }
    }

    void addSubmitButtonHandler() {
        submitButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                doSubmit(textBox.getText());
            }
        });

    }

    void addSuggestBoxHandlers() {
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent event) {
                suggestion = (SuggestOracle.Suggestion) event.getSelectedItem();
                if (suggestion.getReplacementString() == null) {
                    suggestBox.setText(currentText);
                    doSubmit(currentText);
                } else if (suggestion.getReplacementString() != null) {
                    doSubmit(suggestion.getReplacementString());
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
            public void onKeyUp(KeyUpEvent keyDownEvent) {
                if (keyDownEvent.isDownArrow())
                    if (textBox.getText() != null && textBox.getText().length() > 0) {
                        currentText = textBox.getText();
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
                    ///
                }
//                Window.alert("HIOO");

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
        noteLabel.setVisible(true);
        noteLabel.setWordWrap(true);
        rootPanel.add(noteLabel);
    }


    public void setText(String newText) {
        textBox.setText(newText);
    }

    protected void doSubmit(String text) {
        if (action != null) {
            suggestBox.setFocus(false);
            action.doSubmit(text);
        }
        if (onclick != null)
            runOnclickJavaScriptMethod(onclick);
    }

    private native void runOnclickJavaScriptMethod(String name)/*-{
            $wnd.submitForm(string);
    }-*/;

    public void setErrorString(String text) {
        noteLabel.setStyleName("gwt-lookup-error");
        errorString = text;
        noteString = "";
        noteLabel.setHTML(errorString);
        noteLabel.setVisible(true);
    }

    public void setNoteString(String note) {
        noteLabel.setStyleName("gwt-lookup-note");
        noteString = note;
        errorString = "";
        noteLabel.setHTML(noteString);
        noteLabel.setVisible(true);
    }

    public String getNoteString() {
        return noteString;
    }

    public void clearError() {
        if (errorString.length() > 0 && false == errorString.equals(EMPTY_STRING)) {
            setErrorString(EMPTY_STRING);
        }
    }

    public void clearNote() {
        if (noteString.length() > 0 && false == noteString.equals(EMPTY_STRING)) {
            setNoteString(EMPTY_STRING);
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

    public void addOnFocusHandler(FocusHandler autocompleteFocusHandler) {
        suggestBox.getTextBox().addFocusHandler(autocompleteFocusHandler);
    }

    public void setLimit(int limit) {
        oracle.setLimit(limit);
    }

    public void setEnabled(boolean enabled){
        textBox.setEnabled(enabled);
    }

    /**
     * Display the term info for a given term ID in a given ontology.
     *
     * @param ontologyName Ontology Name
     * @param termID   term ID: zdb ID or obo ID
     */
    public void showTermInfo(String ontologyName, String termID) {
        //Window.alert("Show Term:: " + ontology + ":" + termID);
        OntologyDTO ontology = OntologyDTO.getOntologyByDescriptor(ontologyName);
        lookupRPC.getTermInfo(ontology, termID, new TermInfoCallBack(termInfoTable, termID));
    }


}
