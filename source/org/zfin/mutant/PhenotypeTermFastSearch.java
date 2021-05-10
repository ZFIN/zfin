package org.zfin.mutant;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PHENO_TERM_FAST_SEARCH")
public class PhenotypeTermFastSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ptfs_pk_id")
    private long id;
    @ManyToOne()
    @JoinColumn(name = "ptfs_psg_id")
    private PhenotypeStatementWarehouse phenotypeObserved;
    @ManyToOne()
    @JoinColumn(name = "ptfs_term_zdb_id")
    private GenericTerm term;
    @Column(name = "ptfs_tag")
    private String tag;
    @Column(name = "ptfs_phenos_created_date")
    private Date dateCreated;
    @Column(name = "ptfs_is_direct_annotation")
    private boolean directAnnotation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PhenotypeStatementWarehouse getPhenotypeObserved() {
        return phenotypeObserved;
    }

    public void setPhenotypeObserved(PhenotypeStatementWarehouse phenotypeObserved) {
        this.phenotypeObserved = phenotypeObserved;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isDirectAnnotation() {
        return directAnnotation;
    }

    public void setDirectAnnotation(boolean directAnnotation) {
        this.directAnnotation = directAnnotation;
    }
}
