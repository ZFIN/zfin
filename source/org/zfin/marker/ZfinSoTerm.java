
package org.zfin.marker;

import org.zfin.ontology.GenericTerm;

public class ZfinSoTerm {

    private GenericTerm soTerm;
    private String entityName;
    private String oboID;
    private String termName;
    private long ID;

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
