package org.zfin.marker;

import org.zfin.expression.ExpressionExperiment;

import java.util.Set;

/**
 * ToDO: include info
 */
public class Clone {

    private String zdbID;
    private int rating;
    private ProblemType problem ;
    private Set<ExpressionExperiment> expressionExperiments;

    public enum ProblemType{
        CHIMERIC("Chimeric"),
        GENOMIC_CONTAMINATION_INTRON("Genomic Contamination (intron containing)"),
        PARTIALLY_PROCESSED("partially processed"),
        POLY_A_PRIMED("poly A primed"),
        NON_CANONICAL_SPLICING("Uses non-canonical splicing"),
        NMD("nonsense mediated decay (NMD)"),
        UNSPECIFIED("Unspecified");

        private String value;

        ProblemType(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }
    }


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

    public ProblemType getProblem() {
        return problem;
    }

    public void setProblem(ProblemType problem) {
        this.problem = problem;
    }

    public boolean isProblem(){
        if(problem==null){
            return false;
        }
        else{
            return true ;
        }
    }

    public boolean isChimeric(){
        if(problem!=null
                &&
                problem.value.equals(ProblemType.CHIMERIC)
                ){
            return true ;
        }
        else{
            return false ;
        }
    }

}
