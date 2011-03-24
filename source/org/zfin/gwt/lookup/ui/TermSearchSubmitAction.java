package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.ui.SubmitAction;

/**
 */
public class TermSearchSubmitAction implements SubmitAction {

    private OntologyDTO ontology;

    public TermSearchSubmitAction(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public void doSubmit(String value) {
        if (value!= null) {
            Window.open("/action/ontology/term-detail?termID=" + value.replaceAll(" ", "%20")+"&ontologyName="+ontology.getOntologyName(), "_self",
                    "");
        }
    }
}
