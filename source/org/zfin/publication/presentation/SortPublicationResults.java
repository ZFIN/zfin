package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.publication.Publication;

import java.util.Comparator;

/**
 * This comparator sorts two publications by a specified type.
 */
//ToDo: more generic to allow sorting by multiple parameters and using the ESP notation
// to define a sorting attribute
class SortPublicationResults implements Comparator {

    // ToDo: Turn into enumerator
    private String sortingType;
    private boolean ascending = true;


    public SortPublicationResults(String sortingType, boolean ascending) {
        if (StringUtils.isEmpty(sortingType))
            throw new RuntimeException("Cannot create a comparator without a type to sort by");
        this.sortingType = sortingType;
        this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Publication))
            throw new RuntimeException("Incorrect Classs type <");
        if (!(o2 instanceof Publication))
            throw new RuntimeException("Incorrect Classs type <");

        Publication pubOne = (Publication) o1;
        Publication pubTwo = (Publication) o2;


        if (sortingType.equals("date")) {
            if (ascending)
                return pubOne.getPublicationDate().compareTo(pubTwo.getPublicationDate());
            else
                return pubTwo.getPublicationDate().compareTo(pubOne.getPublicationDate());
        }
        return 0;
    }

}
