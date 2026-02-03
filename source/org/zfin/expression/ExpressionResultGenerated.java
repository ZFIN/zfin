package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

@Entity
@Table(name = "xpat_results_generated")
@Getter
@Setter
public class ExpressionResultGenerated implements Comparable<ExpressionResultGenerated> {

    @Id
    @Column(name = "xrg_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xrg_xedg_id", nullable = false)
    private ExpressionDetailsGenerated expressionExperiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xrg_start_stg_zdb_id")
    private DevelopmentStage start;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xrg_end_stg_zdb_id")
    private DevelopmentStage end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xrg_subterm_zdb_id")
    private GenericTerm subterm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xrg_superterm_zdb_id")
    private GenericTerm superterm;

    @Transient
    protected PostComposedEntity entity;

    @Column(name = "xrg_expression_found")
    private boolean expressionFound;

    @Column(name = "xrg_comments")
    private String comment;


    public PostComposedEntity getEntity() {
        if (entity == null) {
            entity = new PostComposedEntity();
            entity.setSuperterm(superterm);
            entity.setSubterm(subterm);
        }
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
        if (entity != null) {
            this.superterm = entity.getSuperterm();
            this.subterm = entity.getSubterm();
        }
    }


    @Override
    public int compareTo(ExpressionResultGenerated o) {
        if (o == null)
            return 1;

        if (!start.equals(o.getStart()))
            return start.compareTo(o.getStart());
        if (!end.equals(o.getEnd()))
            return end.compareTo(o.getEnd());
        if (!getEntity().getSuperterm().equals(o.getEntity().getSuperterm()))
            return getEntity().getSuperterm().compareTo(o.getEntity().getSuperterm());
        if (getEntity().getSubterm() == null)
            return -1;
        if (o.getEntity().getSubterm() == null)
            return 1;
        if (!getEntity().getSubterm().equals(o.getEntity().getSubterm()))
            return getEntity().getSubterm().compareTo(o.getEntity().getSubterm());

        return 0;
    }

}
