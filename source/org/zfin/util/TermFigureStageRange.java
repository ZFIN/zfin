package org.zfin.util;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;

/**
 * Entity that holds a superterm of an individual expression result record
 */
public class TermFigureStageRange {

    private GenericTerm superTerm;
    private DevelopmentStage start;
    private DevelopmentStage end;

    public GenericTerm getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(GenericTerm superTerm) {
        this.superTerm = superTerm;
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
}
