package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PostComposedPart;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.NullpointerException;
import org.zfin.gwt.root.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This composite comprises a term entry field that has auto-complete and a button that allows to add
 * a term from the term info box. 
 */
public class TermEntry extends HorizontalPanel {

    // GUI elements
    private ZfinListBox ontologySelector = new ZfinListBox();
    private LookupComposite termTextBox = new LookupComposite();
    private Button copyFromTerminfoToTextButton = new Button("&larr;");

    // ontologies used
    private List<Ontology> ontologies;
    private PostComposedPart termPart;

    // this map maps the ontology names from Ontology to the ones use in the Lookup module
    // ToDo: Should unify them!
    private static final Map<Ontology, String> ontologyMap = new HashMap<Ontology, String>(10);

    static {
        ontologyMap.put(Ontology.ANATOMY, LookupComposite.TYPE_ANATOMY_ONTOLOGY);
        ontologyMap.put(Ontology.GO, LookupComposite.GDAG_TERM_LOOKUP);
        ontologyMap.put(Ontology.GO_CC, LookupComposite.GDAG_TERM_LOOKUP);
        ontologyMap.put(Ontology.GO_MF, LookupComposite.GDAG_TERM_LOOKUP);
        ontologyMap.put(Ontology.GO_BP, LookupComposite.GDAG_TERM_LOOKUP);
        ontologyMap.put(Ontology.QUALITY, LookupComposite.TYPE_QUALITY);
    }

    public TermEntry(List<Ontology> ontologies, PostComposedPart termPart) {
        super();
        if (ontologies == null || ontologies.isEmpty())
            throw new NullpointerException("no ontology provided");
        if (termPart == null)
            throw new NullpointerException("no term part provided");
        this.ontologies = ontologies;
        this.termPart = termPart;
        init();
    }

    private void init() {
        addOntologySelector();
        addLookupTermBox();
        addCopyFromTermInfoButton();
    }

    private void addCopyFromTermInfoButton() {
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
        Ontology defaultOntology = getDefaultOntology();
        termTextBox.setType(ontologyMap.get(defaultOntology));
        termTextBox.setInputName(termPart.name());
        termTextBox.setShowError(true);
        termTextBox.setWildCard(false);
        termTextBox.initGui();
        add(termTextBox);
    }

    /**
     * Sets the default lookup ontology accordingly:
     * 1) if only one ontology: set it to this one
     * 2) if more than 1 and one of them is the AO: set it to AO
     * 3) if more than 1 and no AO set it to the first one in the list.
     *
     * @return default ontology to be displayed.
     */
    private Ontology getDefaultOntology() {
        if (ontologies.size() == 1)
            return ontologies.get(0);
        else if (ontologies.size() > 1 && ontologies.contains(Ontology.ANATOMY))
            return Ontology.ANATOMY;
        else
            return ontologies.get(0);
    }

    private void addOntologySelector() {
        if (ontologies.size() > 1) {
            for (Ontology ontology : ontologies)
                ontologySelector.addItem(ontology.getDisplayName(), ontology.getDisplayName());
            add(ontologySelector);
        } else {
            add(new Label(ontologies.get(0).getDisplayName() + ": "));
        }
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

        Ontology selectedOntologyMe = getSelectedOntology();
        String termNameMe = getTermText();
        Ontology selectedOntologyYou = termEntryUnit.getSelectedOntology();
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
    public void setOntologySelector(Ontology newOntology) {
        int numberOfOntologies = ontologySelector.getItemCount();
        for (int index = 0; index < numberOfOntologies; index++) {
            String ontol = ontologySelector.getValue(index);
            if (ontol.equals(newOntology.getDisplayName()))
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
        ontologySelector.selectEntryByDisplayName(getDefaultOntology().getDisplayName());
    }

    public Ontology getSelectedOntology() {
        int selectedIndex = ontologySelector.getSelectedIndex();
        // if only a single ontology is provided, i.e. no selector
        if (selectedIndex == -1)
            return ontologies.get(0);

        String ontology = ontologySelector.getValue(selectedIndex);
        return Ontology.getOntologyByDisplayName(ontology);
    }

    public boolean hasOntology(Ontology selectedSubtermOntology) {
        return ontologies.contains(selectedSubtermOntology);
    }

    public boolean hasOntologyChoices() {
        return ontologies.size() > 1;
    }

    public static String getLookupOntologyName(Ontology ontology){
        return ontologyMap.get(ontology);
    }

    public void addOnFocusHandler(FocusHandler autocompleteFocusHandler) {
        termTextBox.addOnFocusHandler(autocompleteFocusHandler);
    }

    private class OntologyChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            termTextBox.setType(ontologyMap.get(getSelectedOntology()));
            termTextBox.setGoOntology(getSelectedOntology());
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
