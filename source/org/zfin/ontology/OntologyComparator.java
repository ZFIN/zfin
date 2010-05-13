package org.zfin.ontology;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for Terms.
 */
public class OntologyComparator implements Comparator<String>, Serializable {

    public int compare(String termOne, String termTwo) {
        return termOne.compareToIgnoreCase(termTwo);
    }

}
