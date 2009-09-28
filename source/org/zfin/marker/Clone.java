package org.zfin.marker;

import org.zfin.expression.ExpressionExperiment;

import java.util.Set;

/**
 */
public class Clone extends Marker{

    private Integer rating;
    private ProblemType problem;
    private Set<ExpressionExperiment> expressionExperiments;
    
    private Vector vector;
    private ProbeLibrary probeLibrary ;
    private String digest ;
    private Integer insertSize ;
    private String polymeraseName ;
    private String pcrAmplification ;
    private String cloneComments;
    private String cloningSite ;


    public enum ProblemType{
        CHIMERIC("Chimeric"),
        GENOMIC_CONTAMINATION__INTRON_CONTAINING_("Genomic Contamination (intron containing)"),
        PARTIALLY_PROCESSED("partially processed"),
        POLY_A_PRIMED("poly A primed"),
        USES_NON_CANONICAL_SPLICING("Uses non-canonical splicing"),
        NONSENSE_MEDIATED_DECAY__NMD_("nonsense mediated decay (NMD)"),
        UNSPECIFIED("Unspecified");

        private String value;

        ProblemType(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }

        public static ProblemType getProblemType(String type) {
            for (ProblemType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No clone problem type of string " + type + " found.");
        }
    }


    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
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

    public void setProblem(ProblemType problemType) {
        this.problem = problemType;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public ProbeLibrary getProbeLibrary() {
        return probeLibrary;
    }

    public void setProbeLibrary(ProbeLibrary probeLibrary) {
        this.probeLibrary = probeLibrary;
    }

    // note: Must not have "isProblem" method as it will assume that 'problem' is a BooleanType


    public boolean isChimeric(){
        return ( problem!=null && problem.equals(ProblemType.CHIMERIC) ) ;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CLONE");
        sb.append("\n");
        sb.append("zdbID: ").append(getZdbID());
        sb.append("\n");
        sb.append("name: ").append(getName());
        sb.append("\n");
        sb.append("symbol: ").append(getAbbreviation());
        sb.append("\n");
        sb.append("type: ").append(getMarkerType().getType().toString());
        sb.append("\n");
        sb.append("rating: ").append(rating);
        sb.append("\n");
        sb.append("problem: ").append(problem);
        sb.append("\n");
        return sb.toString();
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public Integer getInsertSize() {
        return insertSize;
    }

    public void setInsertSize(Integer insertSize) {
        this.insertSize = insertSize;
    }

    public String getPolymeraseName() {
        return polymeraseName;
    }

    public void setPolymeraseName(String polymeraseName) {
        this.polymeraseName = polymeraseName;
    }

    public String getPcrAmplification() {
        return pcrAmplification;
    }

    public void setPcrAmplification(String pcrAmplification) {
        this.pcrAmplification = pcrAmplification;
    }

    public String getCloneComments() {
        return cloneComments;
    }

    public void setCloneComments(String cloneComments) {
        this.cloneComments = cloneComments;
    }

    public String getCloningSite() {
        return cloningSite;
    }

    public void setCloningSite(String cloningSite) {
        this.cloningSite = cloningSite;
    }
}
