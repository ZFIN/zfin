package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * Data Transfer Object individual structures on the pile.
 */
public abstract class AbstractPileStructureDTO implements IsSerializable{

    private ExpressedTermDTO expressedTerm;
    protected String zdbID;
    private String creator;
    private Date date;

    public ExpressedTermDTO getExpressedTerm() {
        return expressedTerm;
    }

    public void setExpressedTerm(ExpressedTermDTO expressedTerm) {
        this.expressedTerm = expressedTerm;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public abstract AbstractPileStructureDTO copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPileStructureDTO structureDTO = (AbstractPileStructureDTO) o;

        if (zdbID != null ? !zdbID.equals(structureDTO.zdbID) : structureDTO.zdbID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = 31 + (zdbID != null ? zdbID.hashCode() : 0);
        return result;
    }

    public int compareTo(AbstractPileStructureDTO o) {
        if (o == null)
            return -1;
        if (o.getExpressedTerm() == null)
            return -1;
        if (expressedTerm == null)
            return 1;
        return expressedTerm.compareTo(o.getExpressedTerm());
    }
}