package org.zfin.datatransfer.go;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * for Gaf Processing . . . may loosely translate into a MarkerGoTermEvidence record (or may be replaced by)
 * <p/>
 * should have 17 columns
 */
@Getter
@Setter
public class GafEntry {

    ////////////////////////// // 1-based indexes
    private String entryId; // 2
    private String markerAbbrev; // 3 //Added for ZFIN-8035
    private String qualifier; // 4
    private String goTermId;  // 5
    private String pubmedId;  // 6
    private String evidenceCode;  // 7
    private String inferences; // 8
    private String dbObjectName; // 10 //Added for ZFIN-8035 debugging
    private String dbObjectSynonym; // 11 //Added for ZFIN-8035 debugging
    private String taxonId; //13
    private String createdDate; //14
    private String createdBy; //15
    private String annotExtn; //16
    private String annotationProperties;
    private String modelID;
    private List<GafAnnotationGroup> annotationGroups;
    private String geneProductFormID; //17

    private int col8pipes;
    private int col8commas;
    private int col8both;


    @Override
    public String toString() {
        return "GafEntry" +
                "{entryId='" + entryId + '\'' +
                ", qualifier='" + qualifier + '\'' +
                ", goid='" + goTermId + '\'' +
                ", pmid='" + pubmedId + '\'' +
                ", evidenceCode='" + evidenceCode + '\'' +
                ", inferences='" + inferences + '\'' +
                ", taxonID='" + taxonId + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", annotExtn='" + annotExtn + '\'' +
                ", geneProducFormID='" + geneProductFormID + '\'' +
                '}' + "\n";
    }

    //FB case 8432 prevent GO annotation to GO:0005623 from FP-Inf. GAF load
    public boolean isCell () {
        return goTermId.equalsIgnoreCase("GO:0005623");
    }

    /**
     * Creates a key for a gafEntry for indexing in a hash. We use this to determine if a gaf entry
     * that we are adding is essentially a duplicate of an existing gaf entry.  We then retain the gaf
     * entry that has the most information (geneProductFormID).
     *
     * @return Key for gafEntry
     */
    public String getSimilarityHash() {
        return (getQualifier() == null ? "NULL" : getQualifier()) +
                (getMarkerAbbrev() == null ? "NULL" : getMarkerAbbrev()) +
                (getGoTermId() == null ? "NULL" : getGoTermId()) +
                (getEvidenceCode() == null ? "NULL" : getEvidenceCode()) +
                (getPubmedId() == null ? "NULL" : getPubmedId()) +
                (getInferences() == null ? "NULL" : getInferences()) +
                (getDbObjectName() == null ? "NULL" : getDbObjectName()) +
                (getDbObjectSynonym() == null ? "NULL" : getDbObjectSynonym()) +
                (getTaxonId() == null ? "NULL" : getTaxonId()) +
                (getCreatedBy() == null ? "NULL" : getCreatedBy()) +
                (getCreatedDate() == null ? "NULL" : getCreatedDate());
    }    

}
