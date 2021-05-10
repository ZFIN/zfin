
package org.zfin.marker;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "so_zfin_mapping")
public class ZfinSoTerm {

    @Id
    @Column(name = "szm_pk_id")
    private long ID;
    @ManyToOne()
    @JoinColumn(name = "szm_term_ont_id", referencedColumnName = "term_ont_id")
    private GenericTerm soTerm;
    @Column(name = "szm_object_type")
    private String entityName;
    transient private String oboID;
    @Column(name = "szm_term_name")
    private String termName;

    public GenericTerm getSoTerm() {
        return soTerm;
    }

    public void setSoTerm(GenericTerm soTerm) {
        this.soTerm = soTerm;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }
}
