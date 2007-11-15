package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyStatistics;

import java.util.Comparator;

/**
 * Comparator that compares AnatomyStatistics objects:
 * 1) By Non-obsolete - Obsolete
 * 2) By name
 */
public class SortAnatomyResults implements Comparator<AnatomyStatistics> {

    private String searchTerm;

    public SortAnatomyResults(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int compare(AnatomyStatistics termOne, AnatomyStatistics termTwo) {
        SortAnatomySearchTerm sort = new SortAnatomySearchTerm(searchTerm);
        return sort.compare(termOne.getAnatomyItem(), termTwo.getAnatomyItem());
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
