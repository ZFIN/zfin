package org.zfin.gwt.root.dto;

/**
 * GWT domain object matching PhenotypeStructure.
 */
public class PhenotypeTermDTO extends ExpressedTermDTO {

    private TermDTO quality;
    private String tag;

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

    @Override
    public String getDisplayName() {
        StringBuilder composedTerm = new StringBuilder(super.getDisplayName());
        if (quality != null)
            composedTerm.append(" - ").append(quality.getTermName());
        return composedTerm.toString();
    }

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param phenotypeTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(PhenotypeTermDTO phenotypeTerm) {
        if (!superterm.equalsByNameOnlyAndOntology(phenotypeTerm.getSuperterm()))
            return false;
        if ((subterm != null && phenotypeTerm.getSubterm() == null) ||
                (subterm == null && phenotypeTerm.getSubterm() != null))
            return false;
        if ((subterm != null && phenotypeTerm.getSubterm() != null) && !subterm.equalsByNameOnlyAndOntology(phenotypeTerm.getSubterm()))
            return false;
        if (!quality.equalsByNameOnlyAndOntology(phenotypeTerm.getQuality()))
            return false;
        if (!tag.equals(phenotypeTerm.getTag()))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeTermDTO termDTO = (PhenotypeTermDTO) o;

        String supertermID = superterm.getTermID();
        if (supertermID != null ? !supertermID.equals(termDTO.getSuperterm().getTermID()) : termDTO.getSuperterm().getTermID() != null)
            return false;
        if (subterm != null) {
            String subtermID = subterm.getTermID();
            if (subtermID != null ? !subtermID.equals(termDTO.getSubterm().getTermID()) : termDTO.getSubterm().getTermID() != null)
                return false;
        }
        String qualityID = quality.getTermID();
        if (qualityID != null ? !qualityID.equals(termDTO.getQuality().getTermID()) : termDTO.getQuality().getTermID() != null)
            return false;
        if (tag != null ? !tag.equals(termDTO.getTag()) : termDTO.getTag() != null)
            return false;
        return true;
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        int result = (superterm.getTermID() != null ? superterm.getTermID().hashCode() : 0);
        if (subterm != null)
            result += subterm.getTermID() != null ? subterm.getTermID().hashCode() : 0;
        result += quality.getTermID() != null ? quality.getTermID().hashCode() : 0;
        result += tag != null ? tag.hashCode() : 0;
        return result;
    }

    /**
     * Create a unique ID for a phenotype structure:
     * supertermID:subterm:quality:tag
     * @return unique key
     */
    @Override
    public String getUniqueID() {
        String composedID = superterm.getTermOboID();
        if (subterm != null)
            composedID += ":" + subterm.getTermOboID();
        composedID += ":" + quality.getTermOboID();
        composedID += ":" + tag;
        return composedID;
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        if (!(o instanceof PhenotypeTermDTO))
            throw new RuntimeException("Incorrect Class type");

        PhenotypeTermDTO term = (PhenotypeTermDTO) o;
        if (super.compareTo(term) != 0)
            return super.compareTo(term);
        return quality.getTermName().compareTo(term.getQuality().getTermName());
    }

    public String toString() {
        return getDisplayName();
    }
}
