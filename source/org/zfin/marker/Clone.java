package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ActiveData;

import java.util.Set;

@Entity
@Table(name = "clone")
@PrimaryKeyJoinColumn(name = "clone_mrkr_zdb_id")
@Setter
@Getter
public class Clone extends Marker {

    @Column(name = "clone_rating")
    @JsonView({View.API.class, View.UI.class})
    private Integer rating;

    @Column(name = "clone_problem_type")
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class,
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.marker.Clone$ProblemType")})
    private ProblemType problem;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatex_probe_feature_zdb_id")
    private Set<ExpressionExperiment2> expressionExperiments2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clone_vector_name")
    private Vector vector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clone_probelib_zdb_id")
    private ProbeLibrary probeLibrary;

    @Column(name = "clone_digest")
    private String digest;

    @Column(name = "clone_insert_size")
    private Integer insertSize;

    @Column(name = "clone_polymerase_name")
    private String polymeraseName;

    @Column(name = "clone_pcr_amplification")
    private String pcrAmplification;

    @Column(name = "clone_comments")
    private String cloneComments;

    @Column(name = "clone_cloning_site")
    private String cloningSite;

    @Column(name = "clone_sequence_type", nullable = false)
    private String sequenceType;

    public boolean isRnaClone() {
        return
            getMarkerType().getType().equals(Marker.Type.CDNA)
            ||
            getMarkerType().getType().equals(Marker.Type.EST)
            ;
    }


    public enum ProblemType {
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

        public String toString() {
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


    public Set<ExpressionExperiment2> getExpressionExperiments2() {
        return expressionExperiments2;
    }

    public void setExpressionExperiments2(Set<ExpressionExperiment2> expressionExperiments) {
        this.expressionExperiments2 = expressionExperiments;
    }

    // note: Must not have "isProblem" method as it will assume that 'problem' is a BooleanType


    public boolean isChimeric() {
        return (problem != null && problem.equals(ProblemType.CHIMERIC));
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
    public String getCloneType() {
        return ActiveData.getType(zdbID).name();
    }
}
