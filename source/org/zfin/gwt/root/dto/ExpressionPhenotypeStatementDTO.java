package org.zfin.gwt.root.dto;

/**
 * GWT domain object matching PhenotypeStructure.
 */
public class ExpressionPhenotypeStatementDTO extends ExpressedTermDTO {

    private TermDTO quality;
    private String tag;
    private String geneName;
    private String antibodyName;

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

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getAntibodyName() {
        return antibodyName;
    }

    public void setAntibodyName(String antibodyName) {
        this.antibodyName = antibodyName;
    }

    @Override
    public String getDisplayName() {
        StringBuilder composedTerm = new StringBuilder(entity.getDisplayName());
        if (geneName != null) {
            composedTerm.append(" " + geneName + " ");
            composedTerm.append(" expression ");
        } else {
            composedTerm.append(" " + antibodyName + " ");
            composedTerm.append(" labeling ");
        }
        if (quality != null)
            composedTerm.append(" - ").append(quality.getTermName());
        composedTerm.append(" " + tag);
        return composedTerm.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionPhenotypeStatementDTO termDTO = (ExpressionPhenotypeStatementDTO) o;
        if (tag != null ? !tag.equals(termDTO.getTag()) : termDTO.getTag() != null)
            return false;
        if (!quality.equals(termDTO.getQuality()))
            return false;
        return entity.equals(termDTO.getEntity());
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
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
        composedID += ":" + tag;
        return composedID;
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        if (!(o instanceof ExpressionPhenotypeStatementDTO))
            throw new RuntimeException("Incorrect Class type");

        ExpressionPhenotypeStatementDTO term = (ExpressionPhenotypeStatementDTO) o;
        if (entity.compareTo(term.getEntity()) != 0)
            return entity.compareTo(term.getEntity());
        return quality.compareTo(term.getQuality());
    }

    public String toString() {
        return getDisplayName();
    }
}
