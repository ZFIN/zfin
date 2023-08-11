package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ActiveData;

import java.util.Set;

@Setter
@Getter
public class Clone extends Marker {

    @JsonView({View.API.class, View.UI.class})
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
    private String sequenceType;

    public boolean isRnaClone(){
        return
                getMarkerType().getType().equals(Marker.Type.CDNA)
                        ||
                        getMarkerType().getType().equals(Marker.Type.EST)
                ;
    }


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


    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
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

    @JsonView({View.UI.class})
    public String getCloneType(){
        return ActiveData.getType(zdbID).name();
    }
}
