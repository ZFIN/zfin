package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.NullpointerException;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

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
    private TermInfoComposite termInfoComposite = null ;

    // ontologies used
    private List<OntologyDTO> ontologies;
    private PostComposedPart termPart;

    public TermEntry(List<OntologyDTO> ontologies, PostComposedPart termPart,TermInfoComposite termInfoComposite) {
        this(ontologies,termPart);
        this.termInfoComposite = termInfoComposite ;
        addInternalListeners() ;
    }

    public TermEntry(List<OntologyDTO> ontologies, PostComposedPart termPart) {
        if (ontologies == null || ontologies.isEmpty())
            throw new NullpointerException("no ontology provided");
        if (termPart == null)
            throw new NullpointerException("no term part provided");
        this.ontologies = ontologies;
        this.termPart = termPart;
        init();
    }

    private void addInternalListeners() {
        if(termInfoComposite!=null){
            termTextBox.setHighlightAction(new HighlightAction(){
                @Override
                public void onHighlight(String termID) {
                    if(termID!=null && false== termID.startsWith(ItemSuggestCallback.END_ELLIPSE)){
                        LookupRPCService.App.getInstance().getTermInfo(termTextBox.getOntology(), termID, new TermInfoCallBack(termInfoComposite, termID));
                    }
                }
            });
        }
    }


    private void init() {
        addOntologySelector();
        addLookupTermBox();
        addCopyFromTermInfoButton();
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
        OntologyDTO defaultOntology = getDefaultOntology();
        termTextBox.setOntology(defaultOntology);
        termTextBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
        termTextBox.setInputName(termPart.name());
        termTextBox.setShowError(true);
        termTextBox.setWildCard(false);
        termTextBox.setUseIdAsValue(true);
        termTextBox.setAction(new SubmitAction(){

            private boolean isSubmitting = false ;
            @Override
            public void doSubmit(final String value) {
                if(isSubmitting){
                    return ;
                }
                isSubmitting = true ;
                termTextBox.setEnabled(false);
                termTextBox.setNoteString("Validating ["+value+"]...");

                LookupRPCService.App.getInstance().getTermByName(OntologyDTO.GO,value,
                        new MarkerEditCallBack<TermDTO>("Failed to retrieve GO value ["+value+"]") {

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                termTextBox.setEnabled(true);
                                termTextBox.clearNote();
                                isSubmitting =  false;
                            }

                            @Override
                            public void onSuccess(TermDTO result) {
                                termTextBox.setEnabled(true);
                                termTextBox.clearNote();
                                if(result==null) {
                                    termTextBox.setErrorString("Unable to find term["+value+"]");
                                    return ;
                                }
                                termTextBox.setText(result.getTermName());
                                isSubmitting =  false;
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
        OntologyDTO selectedOntologyYou = termEntryUnit.getSelectedOntology();
        String termNameYou = termEntryUnit.getTermText();
        // set me
        termTextBox.setText(termNameYou);
        setOntologySelector(selectedOntologyYou);
        // set you
        termEntryUnit.getTermTextBox().setText(termNameMe);
        termEntryUnit.setOntologySelector(selectedOntologyMe);
    }

    /**
     * Set the ontology selector to specified ontology
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

    public boolean hasOntology(OntologyDTO selectedSubtermOntology) {
        return ontologies.contains(selectedSubtermOntology);
    }

    public boolean isSuggestionListShowing(){
        return termTextBox.isSuggestionListShowing();
    }

    public void addOnFocusHandler(FocusHandler autocompleteFocusHandler) {
        termTextBox.addOnFocusHandler(autocompleteFocusHandler);
    }

    public void addOnOntologyChangeHandler(ChangeHandler handler){
        ontologySelector.addChangeHandler(handler);
    }

    /**
     * Set a given term info on the term entry, i.e.
     * set the term name and select the correct ontology.
     * If successful return true, otherwise false.
     * @param termInfo term info
     * @return boolean
     */
    public boolean setTerm(TermInfo termInfo) {
        if (hasOntology(termInfo.getOntology())) {
            termTextBox.setText(termInfo.getName());
            setOntologySelector(termInfo.getOntology());
            return true;
        } else {
            return false;
        }
    }

    private class OntologyChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            termTextBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
            termTextBox.setOntology(getSelectedOntology());
        }

    }

    /**
     * If this term is a superterm than is cannot be empty.
     *
     * @return true or false
     */
    public boolean isValidEntry() {
        if (termPart == PostComposedPart.SUPERTERM)
            return StringUtils.isNotEmpty(termTextBox.getText());
        return true;
    }

}
