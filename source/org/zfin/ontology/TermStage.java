package org.zfin.ontology;

import org.zfin.anatomy.DevelopmentStage;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "term_stage")
public class TermStage implements Serializable {

    @Id
    @Column(name = "ts_term_zdb_id")
    private String id;
    @OneToOne
    @JoinColumn(name = "ts_term_zdb_id")
    private GenericTerm term;
    @OneToOne
    @JoinColumn(name = "ts_start_stg_zdb_id")
    private DevelopmentStage start;
    @OneToOne
    @JoinColumn(name = "ts_end_stg_zdb_id")
    private DevelopmentStage end;

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermStage termStage = (TermStage) o;

        if (term != null ? !term.equals(termStage.term) : termStage.term != null) return false;
        if (start != null ? !start.equals(termStage.start) : termStage.start != null) return false;
        return !(end != null ? !end.equals(termStage.end) : termStage.end != null);

    }

    @Override
    public int hashCode() {
        int result = term != null ? term.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
