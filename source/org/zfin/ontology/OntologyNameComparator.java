package org.zfin.ontology;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Order ontologies by their internal name.
 */
public class OntologyNameComparator implements Comparator<Ontology>, Serializable {

    @Override
    public int compare(Ontology o1, Ontology o2) {
        if(o1 == null)
            return -1;
        if(o2 == null)
            return 1;

        return o1.getOntologyName().compareTo(o2.getOntologyName());
    }
}
