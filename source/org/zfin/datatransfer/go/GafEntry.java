package org.zfin.datatransfer.go;

/**
 * for Gaf Processing . . . may loosely translate into a MarkerGoTermEvidence record (or may be replaced by)
 * <p/>
 * should have 17 columns
 */
public class GafEntry {

    private String entryId; // 2
    private String qualifier; // 4
    private String goTermId;  // 5
    private String pubmedId;  // 6
    private String evidenceCode;  // 7
    private String inferences; // 8
    private String taxonId; //13
    private String createdDate; //14
    private String createdBy; //15
    private String annotExtn; //16

    public int getCol8pipes() {
        return col8pipes;
    }

    public void setCol8pipes(int col8pipes) {
        this.col8pipes = col8pipes;
    }

    private String geneProductFormID;

    public int getCol8both() {
        return col8both;
    }

    public void setCol8both(int col8both) {
        this.col8both = col8both;
    }

    private int col8pipes;
    private int col8commas;
    private int col8both;

    public String getGeneProductFormID() {
        return geneProductFormID;
    }

    public void setGeneProductFormID(String geneProductFormID) {
        this.geneProductFormID = geneProductFormID;
    }

    public int getCol8commas() {
        return col8commas;
    }

    public void setCol8commas(int col8commas) {
        this.col8commas = col8commas;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String uniprotId) {
        this.entryId = uniprotId;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getGoTermId() {
        return goTermId;
    }

    public void setGoTermId(String goTermId) {
        this.goTermId = goTermId;
    }

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getInferences() {
        return inferences;
    }

    public void setInferences(String inferences) {
        this.inferences = inferences;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAnnotExtn() {
        return annotExtn;
    }

    public void setAnnotExtn(String annotExtn) {
        this.annotExtn = annotExtn;
    }

    /*public String getGeneProductFormID() {
        return geneProductFormID;
    }

    public void setGeneProductFormID(String geneProductFormID) {
        this.geneProductFormID = geneProductFormID;
    }
*/
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GafEntry");
        sb.append("{entryId='").append(entryId).append('\'');
        sb.append(", qualifier='").append(qualifier).append('\'');
        sb.append(", goid='").append(goTermId).append('\'');
        sb.append(", pmid='").append(pubmedId).append('\'');
        sb.append(", evidenceCode='").append(evidenceCode).append('\'');
        sb.append(", inferences='").append(inferences).append('\'');
        sb.append(", taxonID='").append(taxonId).append('\'');
        sb.append(", createdDate='").append(createdDate).append('\'');
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", annotExtn='").append(annotExtn).append('\'');
        sb.append(", geneProducFormID='").append(geneProductFormID).append('\'');
        sb.append('}').append("\n");
        return sb.toString();
    }

    //FB case 8432 prevent GO annotation to GO:0005623 from FP-Inf. GAF load
    public boolean isCell () {
        return goTermId.equalsIgnoreCase("GO:0005623");
    }


}
