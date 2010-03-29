package org.zfin.gwt.root.util;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.dto.OntologyDTO;

/**
 * Handler that sets the Quality Term ontology upon changes of the superterm ontology
 * according to business logic defined in the OntologyDTO class.
 */
public class OntologyDependencyHandler implements ChangeHandler {

    private TermEntry superTerm;
    private TermEntry quality;

    public OntologyDependencyHandler(TermEntry superTerm, TermEntry quality) {
        this.superTerm = superTerm;
        this.quality = quality;
    }

    public void onChange(ChangeEvent changeEvent) {
        OntologyDTO selectedSupertermOntology = superTerm.getSelectedOntology();
        OntologyDTO qualityOntology = selectedSupertermOntology.getAssociatedQualityOntology();
        quality.setOntologySelector(qualityOntology);
        quality.getTermTextBox().setOntology(qualityOntology);
    }
}
