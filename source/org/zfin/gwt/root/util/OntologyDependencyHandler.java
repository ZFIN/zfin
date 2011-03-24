package org.zfin.gwt.root.util;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermStatus;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinListBox;

import java.util.Map;

/**
 * Handler that sets the Quality Term ontology upon changes of the superterm ontology
 * according to business logic defined in the OntologyDTO class.
 */
public class OntologyDependencyHandler implements ChangeHandler {

    private TermEntry selectedTermEntry;
    private TermEntry relatedSuperTerm;
    private TermEntry subterm;
    private TermEntry quality;

    public OntologyDependencyHandler(TermEntry termEntry, Map<EntityPart, TermEntry> termEntryMap) {
        selectedTermEntry = termEntry;
        for (Map.Entry<EntityPart, TermEntry> entry : termEntryMap.entrySet()) {
            switch (entry.getKey()) {
                case QUALITY:
                    quality = entry.getValue();
                    break;
                case ENTITY_SUBTERM:
                    subterm = entry.getValue();
                    break;
                case RELATED_ENTITY_SUPERTERM:
                    relatedSuperTerm = entry.getValue();
                    break;
            }
        }
    }

    public void onChange(ChangeEvent changeEvent) {
        OntologyDTO selectedSupertermOntology = selectedTermEntry.getSelectedOntology();
        boolean changeQualityOntologySelector = true;
        // if entity superterm then check if subterm has some (un-validated) entry
        // Note: Even better to check in if combination is valid but that becomes somewhat excessive testing...
        if (selectedTermEntry.getTermPart() == EntityPart.ENTITY_SUPERTERM) {
            final LookupComposite termTextBox = subterm.getTermTextBox();
            if (StringUtils.isNotEmpty(termTextBox.getTextBox().getText())) {
                changeQualityOntologySelector = false;
            }
        }
        if (changeQualityOntologySelector) {
            OntologyDTO qualityOntology = selectedSupertermOntology.getAssociatedQualityOntology();
            quality.setOntologySelector(qualityOntology);
            quality.getOntologySelector().fireChangeHandlers();
        }
    }
}
