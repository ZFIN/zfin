package org.zfin.mutant;

import org.zfin.mutant.presentation.Construct;

import java.util.List;

/**
 * Class to pass search results back to the controller,
 * along with necessary stats about the search
 */
public class ConstructSearchResult {
    private List<Construct> results;
    private int resultsFound;
    private int start;

    public List<Construct> getResults() {
        return results;
    }

    public void setResults(List<Construct> results) {
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
