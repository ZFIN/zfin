package org.zfin.gwt.root.dto;

/**
 * GWT domain object matching PhenotypeStructure.
 */
public class PhenotypeStatementDTO extends ExpressedTermDTO {

    private TermDTO quality;
    private String tag;
    private EntityDTO relatedEntity;

    public TermDTO getQuality() {
        return quality;
    }

    public void setQuality(TermDTO quality) {
        this.quality = quality;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public EntityDTO getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(EntityDTO relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public boolean hasRelatedEntity(){
        if(relatedEntity == null)
            return false;
        if(relatedEntity.getSuperTerm() == null && relatedEntity.getSubTerm() == null)
            return false;
        return true;
    }

    @Override
    public String getDisplayName() {
        StringBuilder composedTerm = new StringBuilder(entity.getDisplayName());
        if (quality != null)
            composedTerm.append(" - ").append(quality.getTermName());
        if (relatedEntity != null && relatedEntity.getSuperTerm() != null) {
            composedTerm.append(" - ");
            composedTerm.append(relatedEntity.getDisplayName());
        }
        return composedTerm.toString();
    }

    public TermDTO getTermDTO(EntityPart entityPart) {
        final TermDTO dto = super.getTermDTO(entityPart);
        if (dto != null)
            return dto;

        switch (entityPart) {
            case RELATED_ENTITY_SUPERTERM:
                if (relatedEntity == null)
                    return null;
                return relatedEntity.getSuperTerm();
            case RELATED_ENTITY_SUBTERM:
                if (relatedEntity == null)
                    return null;
                return relatedEntity.getSubTerm();
            case QUALITY:
                if (quality == null)
                    return null;
                return quality;
        }
        return null;
    }

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param phenotypeTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(PhenotypeStatementDTO phenotypeTerm) {
        if (!quality.equalsByNameOnlyAndOntology(phenotypeTerm.getQuality()))
            return false;
        if (!tag.equals(phenotypeTerm.getTag()))
            return false;
        if (relatedEntity == null && phenotypeTerm.getRelatedEntity() != null)
            return false;
        if (relatedEntity != null && !relatedEntity.equalsByNameAndOntologyOnly(phenotypeTerm.getRelatedEntity()))
            return false;
        return entity.equalsByNameAndOntologyOnly(phenotypeTerm.getEntity());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeStatementDTO termDTO = (PhenotypeStatementDTO) o;
        if (tag != null ? !tag.equals(termDTO.getTag()) : termDTO.getTag() != null)
            return false;
        if (!quality.equals(termDTO.getQuality()))
            return false;
        if (relatedEntity != null && !relatedEntity.equals(termDTO.getRelatedEntity()))
            return false;
        return entity.equals(termDTO.getEntity());
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result += relatedEntity != null ? relatedEntity.hashCode() : 0;
        result += quality != null ? quality.hashCode() : 0;
        result += tag != null ? tag.hashCode() : 0;
        return result;
    }

    /**
     * Create a unique ID for a phenotype structure:
     * supertermID:subterm:quality:tag
     *
     * @return unique key
     */
    @Override
    public String getUniqueID() {
        String composedID = entity.getUniqueID();
        composedID += ":" + quality.getOboID();
        if (relatedEntity != null && relatedEntity.getSuperTerm() != null)
            composedID += ":" + relatedEntity.getUniqueID();
        composedID += ":" + tag;
        return composedID;
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        if (!(o instanceof PhenotypeStatementDTO))
            throw new RuntimeException("Incorrect Class type");

        PhenotypeStatementDTO term = (PhenotypeStatementDTO) o;
        if (entity.compareTo(term.getEntity()) != 0)
            return entity.compareTo(term.getEntity());
        if (quality.compareTo(term.getQuality()) != 0)
            return quality.compareTo(term.getQuality());
        if (relatedEntity == null)
            return -1;
        return relatedEntity.compareTo(term.getRelatedEntity());
    }

    public String toString() {
        return getDisplayName();
    }
}
