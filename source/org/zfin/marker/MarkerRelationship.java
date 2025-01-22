package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;

import java.util.Set;


@Setter
@Getter
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
        FOSMID_CONTAINS_NTR("FOSMID contains NTR");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String toString() {
            return value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type)) {
                    return t;
                }
            }
            throw new RuntimeException("No MarkerRelationship type of string " + type + " found.");
        }
    }

    private String zdbID;
    private Type type;
    private Marker firstMarker;
    private Marker secondMarker;
    private MarkerRelationshipType markerRelationshipType;

    private Set<PublicationAttribution> publications;

    public int getPublicationCount() {
        if (publications == null) {
            return 0;
        } else {
            return publications.size();
        }
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


