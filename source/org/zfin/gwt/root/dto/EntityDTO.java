package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Data Transfer Object for a post-composed term, i.e. super term : sub term
 */
public class EntityDTO implements IsSerializable, Comparable<EntityDTO> {

    private TermDTO superTerm;
    private TermDTO subTerm;

    public TermDTO getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(TermDTO superTerm) {
        this.superTerm = superTerm;
    }

    public TermDTO getSubTerm() {
        return subTerm;
    }

    public void setSubTerm(TermDTO subTerm) {
        this.subTerm = subTerm;
    }

    public String getDisplayName() {
        if (superTerm == null)
            return "";

        StringBuffer composedTerm = new StringBuffer(30);
        composedTerm.append(superTerm.getTermName());
        if (subTerm != null) {
            composedTerm.append(":");
            composedTerm.append(subTerm.getTermName());
        }
        return composedTerm.toString();
    }

    public String getUniqueID() {
        String composedID = superTerm.getOboID();
        if (subTerm != null)
            composedID += ":" + subTerm.getOboID();
        return composedID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;

        if (!(o instanceof EntityDTO))
            return false;

        EntityDTO entityDTO = (EntityDTO) o;
        if (!superTerm.equals(entityDTO.getSuperTerm()))
            return false;
        if (subTerm != null)
            return subTerm.equals(entityDTO.getSubTerm());
        else
            return entityDTO.getSuperTerm() == null;
    }

    @Override
    public int hashCode() {
        int code = 31 + (superTerm != null ? superTerm.hashCode() : 0);
        code += subTerm != null ? subTerm.hashCode() : 0;
        return code;
    }

    @Override
    public int compareTo(EntityDTO o) {
        if (o == null)
            return 1;
        // if the superterms are different sort by superterm
        if (!superTerm.getTermName().equals(o.getSuperTerm().getTermName()))
            return superTerm.getTermName().compareToIgnoreCase(o.getSuperTerm().getTermName());

        // if superterms are the same sort by subterm.
        if (subTerm == null && o.getSubTerm() != null)
            return -1;
        if (subTerm != null && o.getSubTerm() == null)
            return 1;
        if (subTerm == null && o.getSubTerm() == null)
            return 0;

        if (subTerm.getTermName().equalsIgnoreCase(o.getSubTerm().getTermName())) {
            return -1;
        }
        return 0;
    }

    public boolean equalsByNameAndOntologyOnly(EntityDTO relatedEntity) {
        if (relatedEntity == null && superTerm != null)
            return false;
        if (superTerm == null && subTerm == null && relatedEntity == null)
            return true;
        if (superTerm == null && relatedEntity != null && relatedEntity.getSuperTerm() != null)
            return false;
        if (subTerm != null && !subTerm.equalsByName(relatedEntity.getSubTerm()))
            return false;
        return superTerm.equalsByName(relatedEntity.getSuperTerm());
    }
}