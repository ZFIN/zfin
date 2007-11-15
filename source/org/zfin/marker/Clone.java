package org.zfin.marker;

import org.zfin.expression.ExpressionExperiment;

import java.util.Set;

/**
 * ToDO: include info
 */
public class Clone {

    private String zdbID;
    private int rating;
    private Set<ExpressionExperiment> expressionExperiments;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
    }

}
