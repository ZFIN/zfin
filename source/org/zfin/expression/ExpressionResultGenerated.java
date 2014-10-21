package org.zfin.expression;


import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.PostComposedEntity;

public class ExpressionResultGenerated implements Comparable<ExpressionResultGenerated> {
    private long id;
    private ExpressionDetailsGenerated expressionExperiment;
    private DevelopmentStage start;
    private DevelopmentStage end;
    protected PostComposedEntity entity;
    private boolean expressionFound;
    private String comment;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExpressionDetailsGenerated getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionDetailsGenerated expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Override
    public int compareTo(ExpressionResultGenerated o) {
        if (o == null)
            return 1;

        if (!start.equals(o.getStart()))
            return start.compareTo(o.getStart());
        if (!end.equals(o.getEnd()))
            return end.compareTo(o.getEnd());
        if (!entity.getSuperterm().equals(o.getEntity().getSuperterm()))
            return entity.getSuperterm().compareTo(o.getEntity().getSuperterm());
        if (entity.getSubterm() == null)
            return -1;
        if (o.getEntity().getSubterm() == null)
            return 1;
        if (!entity.getSubterm().equals(o.getEntity().getSubterm()))
            return entity.getSubterm().compareTo(o.getEntity().getSubterm());

        return 0;
    }

}
