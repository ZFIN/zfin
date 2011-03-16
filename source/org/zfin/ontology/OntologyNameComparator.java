package org.zfin.ontology;

import org.zfin.gwt.root.dto.OntologyDTO;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Order ontologies by their internal name.
 */
public class OntologyNameComparator implements Comparator<OntologyDTO>, Serializable {

    @Override
    public int compare(OntologyDTO o1, OntologyDTO o2) {
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;

        return o1.getOntologyName().compareTo(o2.getOntologyName());
    }
}
