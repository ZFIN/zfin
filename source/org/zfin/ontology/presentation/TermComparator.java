package org.zfin.ontology.presentation;

import org.zfin.ontology.OntologyTerm;

import java.util.Comparator;

/**
 * Comparator that compares AnatomyStatistics objects:
 * 1) By Non-obsolete - Obsolete
 * 2) By name
 */
public class TermComparator implements Comparator<OntologyTerm> {

    private String searchTerm;

    public TermComparator(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int compare(OntologyTerm termOne, OntologyTerm termTwo) {

        String nameOne = termOne.getTermName();
        String nameTwo = termTwo.getTermName();
        boolean isObsOne = termOne.isObsolete();
        boolean isObsTwo = termTwo.isObsolete();

        // Both names start with the search term: sort alphabetically
        if (nameOne.startsWith(searchTerm) && nameTwo.startsWith(searchTerm)) {
            if ((isObsTwo && isObsOne) || (!isObsOne && !isObsTwo))
                return nameOne.compareToIgnoreCase(nameTwo);
            else if (!isObsOne)
                return -1;
            else
                return 1;
        }
        // neither name starts with the search term: sort alphabetically
        if (!nameOne.startsWith(searchTerm) && !nameTwo.startsWith(searchTerm)) {
            if ((isObsTwo && isObsOne) || (!isObsOne && !isObsTwo))
                return nameOne.compareToIgnoreCase(nameTwo);
            else if (!isObsOne)
                return -1;
            else
                return 1;
        }
        if (nameOne.startsWith(searchTerm) && !nameTwo.startsWith(searchTerm)) {
            if ((isObsTwo && isObsOne) || (!isObsOne && !isObsTwo))
                return -1;
            else if (!isObsOne)
                return -1;
            else
                return 1;
        }
        if (!nameOne.startsWith(searchTerm) && nameTwo.startsWith(searchTerm)) {
            if ((isObsTwo && isObsOne) || (!isObsOne && !isObsTwo))
                return 1;
            else if (!isObsOne)
                return -1;
            else
                return 1;
        }
        return 0;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}