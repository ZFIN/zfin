package org.zfin.fish;

import java.util.List;

import org.zfin.fish.presentation.FishResult;
import org.zfin.mutant.Fish;

/**
 * Class to pass search results back to the controller,
 * along with necessary stats about the search
 */
public class FishSearchResult {
    private List<FishResult> results;
    private int resultsFound;
    private int start;

    public List<FishResult> getResults() {
        return results;
    }

    public void setResults(List<FishResult> results) {
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
