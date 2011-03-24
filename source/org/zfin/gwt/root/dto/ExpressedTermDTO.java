package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTO implements IsSerializable, Comparable<ExpressedTermDTO> {

    protected long id;
    protected String zdbID;
    protected EntityDTO entity;

    private boolean expressionFound;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public String getDisplayName() {
        return entity.getDisplayName();
    }

    public EntityDTO getEntity() {
        return entity;
    }

    public void setEntity(EntityDTO entity) {
        this.entity = entity;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public TermDTO getTermDTO(EntityPart entityPart) {
        switch (entityPart) {
            case ENTITY_SUPERTERM:
                if (entity == null)
                    return null;
                return entity.getSuperTerm();
            case ENTITY_SUBTERM:
                if (entity == null)
                    return null;
                return entity.getSubTerm();
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressedTermDTO termDTO = (ExpressedTermDTO) o;
        if (expressionFound != termDTO.isExpressionFound())
            return false;

        return entity.equals(termDTO.getEntity());
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        int result = entity.hashCode();
        result += expressionFound ? 43 : 13;
        return result;
    }

    public String getUniqueID() {
        return entity.getUniqueID();
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        return entity.compareTo(o.getEntity());
    }

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(ExpressedTermDTO expressedTerm) {
        return entity.equalsByNameAndOntologyOnly(expressedTerm.getEntity());
    }
}