package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


/**
 * Class MarkerRelationship.
 */
public class MarkerRelationship implements Comparable, EntityAttribution, AbstractMarkerRelationshipInterface {

    public enum Type {
	BAC_CONTAINS_GENEDOM("BAC contains GENEDOM"),
	BAC_CONTAINS_NTR("BAC contains NTR"),
	GENEDOM_CONTAINS_NTR("GENEDOM contains NTR"),
        CLONE_CONTAINS_GENE("clone contains gene"),
        CLONE_CONTAINS_SMALL_SEGMENT("clone contains small segment"),
        CLONE_CONTAINS_TRANSCRIPT("clone contains transcript"),
        CLONE_OVERLAP("clone overlap"),
        CODING_SEQUENCE_OF("coding sequence of"),
        CONTAINS_POLYMORPHISM("contains polymorphism"),
        GENE_CONTAINS_SMALL_SEGMENT("gene contains small segment"),
        GENE_PRODUCES_TRANSCRIPT("gene produces transcript"),
        GENE_ENCODES_SMALL_SEGMENT("gene encodes small segment"),
        GENE_HAS_ARTIFACT("gene has artifact"),
        GENE_HYBRIDIZED_BY_SMALL_SEGMENT("gene hybridized by small segment"),
        GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY("gene product recognized by antibody"),
        KNOCKDOWN_REAGENT_TARGETS_GENE("knockdown reagent targets gene"),
        TRANSCRIPT_TARGETS_GENE("transcript targets gene"),
        PROMOTER_OF("promoter of"),
        CRISPR_TARGETS_REGION("crispr targets region"),
        TALEN_TARGETS_REGION("talen targets region"),
        CONTAINS_REGION("contains region"),
	NTR_INTERACTS_WITH_GENE("NTR interacts with GENE"),
        NTR_INTERACTS_WITH_GENEP("NTR interacts with GENEP"),
	NTR_INTERACTS_WITH_NTR("NTR interacts with NTR"),
	RNAGENE_INTERACTS_WITH_GENE("RNAGENE interacts with GENE"),
	RNAGENE_INTERACTS_WITH_GENEP("RNAGENE interacts with GENEP"),
	RNAGENE_INTERACTS_WITH_NTR("RNAGENE interacts with NTR"),
	RNAGENE_INTERACTS_WITH_RNAGENE("RNAGENE interacts with RNAGENE"),
	PAC_CONTAINS_NTR("PAC contains NTR"),
	PAC_CONTAINS_GENEDOM("PAC contains GENEDOM"),
	FOSMID_CONTAINS_GENEDOM("FOSMID contains GENEDOM"),
	FOSMID_CONTAINS_NTR("FOSMID contains NTR")
;
        @JsonView(View.MarkerRelationshipAPI.class)
        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No MarkerRelationship type of string " + type + " found.");
        }
    }
    @JsonView(View.MarkerRelationshipAPI.class)
    private String zdbID;
    @JsonView(View.MarkerRelationshipAPI.class)
    private Type type;
    private Marker firstMarker;
    private Marker secondMarker;

    private MarkerRelationshipType markerRelationshipType;
    @JsonView(View.MarkerRelationshipAPI.class)
    private Set<PublicationAttribution> publications;

    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID() {
        return zdbID;
    }

    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public MarkerRelationshipType getMarkerRelationshipType() {
        return markerRelationshipType;
    }

    public void setMarkerRelationshipType(MarkerRelationshipType markerRelationshipType) {
        this.markerRelationshipType = markerRelationshipType;
    }

    /**
     * Get firstMarker.
     *
     * @return firstMarker as Marker.
     */
    public Marker getFirstMarker() {
        return firstMarker;
    }

    /**
     * Set firstMarker.
     *
     * @param firstMarker the value to set.
     */
    public void setFirstMarker(Marker firstMarker) {
        this.firstMarker = firstMarker;
    }

    /**
     * Get secondMarker.
     *
     * @return secondMarker as Marker.
     */
    public Marker getSecondMarker() {
        return secondMarker;
    }

    /**
     * Set secondMarker.
     *
     * @param secondMarker the value to set.
     */
    public void setSecondMarker(Marker secondMarker) {
        this.secondMarker = secondMarker;
    }

    public Set<PublicationAttribution> getPublications() {
        if (publications == null)
            return new HashSet<PublicationAttribution>();
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        } else {
            return null;
        }
    }

    public int compareTo(Object anotherMarkerRelationship) {
        return firstMarker.compareTo(((MarkerRelationship) anotherMarkerRelationship).getFirstMarker());
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("MARKER_RELATIONSHIP");
        sb.append("zdbID: ").append(zdbID);
        sb.append(newline);
        sb.append("type: ").append(type);
        sb.append(newline);
        sb.append("firstMarker: ").append(firstMarker);
        sb.append(newline);
        sb.append("secondMarker: ").append(secondMarker);
        sb.append(newline);
        return sb.toString();

    }
}


