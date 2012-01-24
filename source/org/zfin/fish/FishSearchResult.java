package org.zfin.fish;

import org.zfin.fish.presentation.Fish;

import java.util.List;

/**
 * Class to pass search results back to the controller,
 * along with necessary stats about the search
 */
public class FishSearchResult {
    private List<Fish> results;
    private int resultsFound;
    private int start;

    public List<Fish> getResults() {
        return results;
    }

    public void setResults(List<Fish> results) {
        this.results = results;
    }

    public int getResultsFound() {
        return resultsFound;
    }

    public void setResultsFound(int resultsFound) {
        this.resultsFound = resultsFound;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
