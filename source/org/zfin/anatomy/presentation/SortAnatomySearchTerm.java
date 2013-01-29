package org.zfin.anatomy.presentation;

import org.zfin.ontology.Term;

import java.util.Comparator;

/**
 * Comparator that compares AnatomyStatistics objects:
 * 1) By Non-obsolete - Obsolete
 * 2) By name
 */
public class SortAnatomySearchTerm implements Comparator<Term> {

    private String searchTerm;

    public SortAnatomySearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int compare(Term termOne, Term termTwo) {

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