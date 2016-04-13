package org.zfin.expression;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expression_result2")
public class ExpressionResult2 implements Comparable<ExpressionResult2> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "xpatres_pk_id")
    private long ID;

    @Column(name = "xpatres_expression_found")
    private boolean expressionFound;
    @Transient
    protected PostComposedEntity entity;

    @ManyToOne
    @JoinColumn(name = "xpatres_efs_id")
    private ExpressionFigureStage expressionFigureStage;

    @ManyToOne
    @JoinColumn(name = "xpatres_superterm_zdb_id")
    private GenericTerm superTerm;

    @ManyToOne
    @JoinColumn(name = "xpatres_subterm_zdb_id")
    private GenericTerm subTerm;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "expressionResult", orphanRemoval = true)
    private Set<ExpressionPhenotypeTerm> phenotypeTermSet;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public ExpressionFigureStage getExpressionFigureStage() {
        return expressionFigureStage;
    }

    public void setExpressionFigureStage(ExpressionFigureStage expressionFigureStage) {
        this.expressionFigureStage = expressionFigureStage;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public GenericTerm getSuperTerm() {
/*
        if (entity == null) return null;
        else
            return entity.getSuperterm();
*/
        return superTerm;
    }

    public void setSuperTerm(GenericTerm superTerm) {
        this.superTerm = superTerm;
        if (entity == null)
            entity = new PostComposedEntity();
        entity.setSuperterm(superTerm);
    }

    public GenericTerm getSubTerm() {
/*
        if (entity == null) return null;
        else
            return entity.getSubterm();
*/
        return subTerm;
    }

    public void setSubTerm(GenericTerm subTerm) {
        this.subTerm = subTerm;
        if (entity == null)
            entity = new PostComposedEntity();
        entity.setSubterm(subTerm);
    }

    public Set<ExpressionPhenotypeTerm> getPhenotypeTermSet() {
        return phenotypeTermSet;
    }

    public void setPhenotypeTermSet(Set<ExpressionPhenotypeTerm> phenotypeTermSet) {
        this.phenotypeTermSet = phenotypeTermSet;
    }

    public PostComposedEntity getEntity() {
        if (entity == null) {
            entity = new PostComposedEntity();
            entity.setSuperterm(superTerm);
            if (subTerm != null)
                entity.setSubterm(subTerm);
        }
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }


    @Override
    public int compareTo(ExpressionResult2 o) {
        return 0;
    }

    public void addPhenotypeTerm(ExpressionStructure expressionStructure) {
        if (expressionStructure.getEapQualityTerm() == null)
            return;
        ExpressionPhenotypeTerm term = new ExpressionPhenotypeTerm();
        term.setQualityTerm(expressionStructure.getEapQualityTerm());
        term.setExpressionResult(this);
        term.setTag(expressionStructure.getTag());
        if (phenotypeTermSet == null)
            phenotypeTermSet = new HashSet<>(1);
        phenotypeTermSet.add(term);
    }

    public boolean isEap() {
        return CollectionUtils.isNotEmpty(phenotypeTermSet);
    }

    @Override
    public String toString() {
        String termName = superTerm.getTermName();
        if(subTerm!= null)
            termName += ": "+subTerm.getTermName();
        return termName + ": " + expressionFound;
    }
}
