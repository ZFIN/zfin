package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "expression_phenotype_term")
public class ExpressionPhenotypeTerm {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "ept_pk_id")
    private long id;
    @ManyToOne()
    @JoinColumn(name = "ept_xpatres_id")
    private ExpressionResult2 expressionResult;
    @ManyToOne()
    @JoinColumn(name = "ept_quality_term_zdb_id")
    private GenericTerm qualityTerm;
    @Column(name = "ept_tag")
    private String tag;
    @Column(name = "ept_relational_term")
    private String relationalTerm = "expressed_in";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExpressionResult2 getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(ExpressionResult2 expressionResult) {
        this.expressionResult = expressionResult;
    }

    public GenericTerm getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(GenericTerm qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public String getRelationalTerm() {
        return relationalTerm;
    }

    public void setRelationalTerm(String relationalTerm) {
        this.relationalTerm = relationalTerm;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isAbsentPhenotypic() {
        return qualityTerm.getOboID().equals("PATO:0000462");
    }

    @Override
    public String toString() {
        return qualityTerm +": "+tag;
    }
}

