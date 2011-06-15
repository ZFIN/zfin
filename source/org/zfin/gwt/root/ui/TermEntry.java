package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.CheckSubsetEventHandler;
import org.zfin.gwt.root.event.SingleOntologySelectionEventHandler;
import org.zfin.gwt.root.util.*;

import java.util.List;

/**
 * This composite comprises a term entry field that has auto-complete and a button that allows to add
 * a term from the term info box.
 */
public class TermEntry extends HorizontalPanel {

    // GUI elements
    private ZfinListBox ontologySelector = new ZfinListBox();
    private LookupComposite termTextBox = new LookupComposite();
    private Button copyFromTerminfoToTextButton = new Button("&larr;");
    private TermInfoComposite termInfoComposite = null;

    // ontologies used
    private List<OntologyDTO> ontologies;
    private EntityPart termPart;

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public TermEntry(List<OntologyDTO> ontologies, EntityPart termPart, TermInfoComposite termInfoComposite) {
        this(ontologies, termPart);
        this.termInfoComposite = termInfoComposite;
        addInternalListeners();
        termTextBox.markUnValidateText();
    }

    public TermEntry(List<OntologyDTO> ontologies, EntityPart termPart) {
        if (ontologies == null || ontologies.isEmpty())
            throw new NullpointerException("no ontology provided");
        if (termPart == null)
            throw new NullpointerException("no term part provided");
        this.ontologies = ontologies;
        this.termPart = termPart;
        init();
    }

    private void addInternalListeners() {
        if (termInfoComposite != null) {
            termTextBox.setHighlightAction(new HighlightAction() {
                @Override
                public void onHighlight(String termID) {
                    if (termID != null && false == termID.startsWith(ItemSuggestCallback.END_ELLIPSIS)) {
                        LookupRPCService.App.getInstance().getTermInfo(termTextBox.getOntology(), termID, new TermInfoCallBack(termInfoComposite, termID));
                    }
                }
            });
        }
        // check if the selected ontology and the text in the text box match
        ontologySelector.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                if (StringUtils.isNotEmpty(termTextBox.getTextBox().getText())) {
                    LookupRPCService.App.getInstance().validateTerm(termTextBox.getTextBox().getText(), termTextBox.getOntology(),
                            new AsyncCallback<TermStatus>() {
                                public void onFailure(Throwable throwable) {
                                    termTextBox.setErrorString(throwable.toString());
                                }

                                public void onSuccess(TermStatus termStatus) {
                                    String term = termStatus.getTerm();
                                    // checking length catching any asynchronous updates
                                    if (termStatus.isExactMatch())
                                        termTextBox.unsetUnValidatedTextMarkup();
                                    else
                                        termTextBox.markUnValidateText();
                                }
                            });
                }
            }
        });
    }


    private void init() {
        addOntologySelector();
        addLookupTermBox();
        addCopyFromTermInfoButton();
        termTextBox.setSingleOntologySelectionEventHandler(new SingleOntologySelectionEventHandler(new SingleOntologySelectionCallBack()));
    }

    private void addCopyFromTermInfoButton() {
        copyFromTerminfoToTextButton.setTitle("Copy term into text box");
        add(copyFromTerminfoToTextButton);
    }

    /**
     * Add a lookup term entry box.
     * Sets the default lookup ontology accordingly:
     * 1) if only one ontology: set it to this one
     * 2) if more than 1 and one of them is the AO: set it to AO
     * 3) if more than 1 and no AO set it to the first one in the list.
     */
    private void addLookupTermBox() {
        // set default ontology
        final OntologyDTO defaultOntology = getDefaultOntology();
        termTextBox.setOntology(defaultOntology);
        termTextBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
        termTextBox.setInputName(termPart.name());
        termTextBox.setShowError(true);
        termTextBox.setWildCard(false);
        termTextBox.setUseIdAsValue(true);
        termTextBox.setAction(new SubmitAction() {
            private boolean isSubmitting = false;
            @Override
            public void doSubmit(final String value) {
                if (isSubmitting) {
                    return;
                }
                isSubmitting = true;
                termTextBox.setEnabled(false);
                termTextBox.setNoteString("Validating [" + value + "]...");
                LookupRPCService.App.getInstance().getTermByName(defaultOntology, value,
                        new MarkerEditCallBack<TermDTO>("Failed to retrieve value [" + value + "]") {

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                termTextBox.setEnabled(true);
                                termTextBox.clearNote();
                                isSubmitting = false;
                            }

                            @Override
                            public void onSuccess(TermDTO result) {
                                termTextBox.setEnabled(true);
                                termTextBox.clearNote();
                                if (result == null) {
                                    termTextBox.setErrorString("Unable to find term[" + value + "]");
                                    isSubmitting = false;
                                    return;
                                }
                                termTextBox.setText(result.getName());
                                isSubmitting = false;
                            }
                        });
            }

        });
        termTextBox.initGui();
        add(termTextBox);
        add(WidgetUtil.getNbsp());
    }

    /**
     * Sets the default lookup ontology accordingly:
     * 1) if only one ontology: set it to this one
     * 2) if more than 1 and one of them is the AO: set it to AO
     * 3) if more than 1 and no AO set it to the first one in the list.
     *
     * @return default ontology to be displayed.
     */
    private OntologyDTO getDefaultOntology() {
        if (ontologies.size() == 1)
            return ontologies.get(0);
        else if (ontologies.size() > 1 && ontologies.contains(OntologyDTO.ANATOMY))
            return OntologyDTO.ANATOMY;
        else
            return ontologies.get(0);
    }

    private void addOntologySelector() {
        if (ontologies.size() > 1) {
            for (OntologyDTO ontology : ontologies)
                ontologySelector.addItem(ontology.getDisplayName(), ontology.getOntologyName());
            add(ontologySelector);
        } else {
            Widget html = new HTML(ontologies.get(0).getDisplayName() + ": ");
            html.setStyleName(WidgetUtil.BOLD);
            add(html);
        }
        add(WidgetUtil.getNbsp());
        ontologySelector.addChangeHandler(new OntologyChangeHandler());
    }

    public LookupComposite getTermTextBox() {
        return termTextBox;
    }

    public ZfinListBox getOntologySelector() {
        return ontologySelector;
    }

    /**
     * It does not check for validity, i.e. if the new ontology is
     * in the possible list. use hasOntology() method to inquire this if you want to set an error message.
     *
     * @param termEntryUnit TermEntryUnit object.
     */
    public void swapTerms(TermEntry termEntryUnit) {
        if (termEntryUnit == null)
            return;

        OntologyDTO selectedOntologyMe = getSelectedOntology();
        String termNameMe = getTermText();
        boolean validateMe = termTextBox.hasValidateTerm();
        OntologyDTO selectedOntologyYou = termEntryUnit.getSelectedOntology();
        String termNameYou = termEntryUnit.getTermText();
        boolean validateYou = termEntryUnit.getTermTextBox().hasValidateTerm();
        // set me
        termTextBox.setText(termNameYou);
        setOntologySelector(selectedOntologyYou);
        // set you
        termEntryUnit.getTermTextBox().setText(termNameMe);
        termEntryUnit.setOntologySelector(selectedOntologyMe);
        // whatever validation state the terms were, swap the state
        termTextBox.setValidationStyle(validateYou);
        termEntryUnit.getTermTextBox().setValidationStyle(validateMe);
    }

    /**
     * Set the ontology selector to specified ontology and
     * also the term text box.
     *
     * @param newOntology the ontology the list should be selected to
     */
    public void setOntologySelector(OntologyDTO newOntology) {
        int numberOfOntologies = ontologySelector.getItemCount();
        for (int index = 0; index < numberOfOntologies; index++) {
            String ontology = ontologySelector.getValue(index);
            if (ontology.equals(newOntology.getOntologyName()))
                ontologySelector.setSelectedIndex(index);
        }
        getTermTextBox().setOntology(newOntology);
        // ToDo: Give a warning message if no ontology match is found.
    }

    public String getTermText() {
        return termTextBox.getText();
    }

    public Button getCopyFromTerminfoToTextButton() {
        return copyFromTerminfoToTextButton;
    }

    public void reset() {
        termTextBox.setText("");
        termTextBox.markUnValidateText();
        String defaultOntology = getDefaultOntology().getDisplayName();
        ontologySelector.selectEntryByDisplayName(defaultOntology);
        termTextBox.setOntology(OntologyDTO.getOntologyByDisplayName(defaultOntology));
    }

    public OntologyDTO getSelectedOntology() {
        int selectedIndex = ontologySelector.getSelectedIndex();
        // if only a single ontology is provided, i.e. no selector
        if (selectedIndex == -1)
            return ontologies.get(0);

        String ontology = ontologySelector.getValue(selectedIndex);
        //Window.alert("Ontology: "+ontology);
        return OntologyDTO.getOntologyByDescriptor(ontology);
    }

    // check if the ontology provided is an option in the
    // ontology selection box.
    // If the ontologies are Quality check if the selection
    // is at least a sub tree of the provided ontology
    public boolean hasOntology(OntologyDTO ontology) {
        if(ontology == OntologyDTO.QUALITY){
            for(OntologyDTO ontologyOption: ontologies)
                if(ontologyOption.isSubtreeOntology(ontology))
                    return true;
        }
        return ontologies.contains(ontology);
    }

    public boolean isSuggestionListShowing() {
        return termTextBox.isSuggestionListShowing();
    }

    public void addOnFocusHandler(FocusHandler autocompleteFocusHandler) {
        termTextBox.addOnFocusHandler(autocompleteFocusHandler);
    }

    public void addOnOntologyChangeHandler(ChangeHandler handler) {
        ontologySelector.addChangeHandler(handler);
    }

    /**
     * Set a given term info on the term entry, i.e.
     * set the term name and select the correct ontology.
     * If successful return true, otherwise false.
     * @param termInfoDTO term info
     * @return boolean
     */
    public boolean setTerm(TermDTO termInfoDTO) {
        if (hasOntology(termInfoDTO.getOntology())) {
            termTextBox.setText(termInfoDTO.getName());
            setOntologySelector(termInfoDTO.getOntology());
            termTextBox.unsetUnValidatedTextMarkup();
            return true;
        } else {
            return false;
        }
    }

    public void addOnBlurHandler(BlurHandler blurHandler) {
        termTextBox.addOnBlurHandler(blurHandler);
    }

    private class OntologyChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            termTextBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
            termTextBox.setOntology(getSelectedOntology());
            if (termInfoComposite != null && termInfoComposite.getCurrentTermInfoDTO() != null && termTextBox.getTextBox().getText() != null) {
                termTextBox.markUnValidateText();
            }
        }

    }

    /**
     * If this term is a superterm than is cannot be empty.
     *
     * @return true or false
     */
    public boolean isValidEntry() {
        if (termPart == EntityPart.ENTITY_SUPERTERM)
            return StringUtils.isNotEmpty(termTextBox.getText());
        return true;
    }

    public void setSubsetCheckHandler(CheckSubsetEventHandler subsetCheckHandler) {
        termTextBox.setCheckForRelationalSubset(true);
        termTextBox.setSubsetEventHandler(subsetCheckHandler);
    }

    public EntityPart getTermPart() {
        return termPart;
    }

    private class SingleOntologySelectionCallBack implements AsyncCallback<OntologyDTO> {

        public void onFailure(Throwable throwable) {
        }

        /**
         * Returns the ontology for a given term ID
         *
         * @param ontologyDTO ontology
         */
        public void onSuccess(OntologyDTO ontologyDTO) {
            //Window.alert("Success");
            setOntologySelector(ontologyDTO);
        }
    }


}
