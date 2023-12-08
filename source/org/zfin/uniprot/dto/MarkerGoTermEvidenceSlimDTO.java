package org.zfin.uniprot.dto;

import lombok.*;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.uniprot.UniProtTools;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkerGoTermEvidenceSlimDTO {
    private GoID goID;
    private String markerZdbID;
    private String goTermZdbID;
    private String publicationID;

    public static List<MarkerGoTermEvidenceSlimDTO> fromMarkerGoTermEvidences(List<MarkerGoTermEvidence> markerGoTermEvidencesForPubZdbID) {
        return markerGoTermEvidencesForPubZdbID.stream()
                .map(MarkerGoTermEvidenceSlimDTO::fromMarkerGoTermEvidence)
                .toList();
    }
    public static MarkerGoTermEvidenceSlimDTO fromMarkerGoTermEvidence(MarkerGoTermEvidence markerGoTermEvidence) {
        return MarkerGoTermEvidenceSlimDTO.builder()
            .goID(markerGoTermEvidence.getGoTerm().getOboID())
            .markerZdbID(markerGoTermEvidence.getMarker().getZdbID())
            .goTermZdbID(markerGoTermEvidence.getGoTerm().getZdbID())
            .publicationID(markerGoTermEvidence.getSource().getZdbID())
            .build();
    }

    public static MarkerGoTermEvidenceSlimDTO fromMap(Map<String, String> relatedEntityFields) {
        return MarkerGoTermEvidenceSlimDTO.builder()
                .goID(relatedEntityFields.get("goID"))
                .markerZdbID(relatedEntityFields.get("markerZdbID"))
                .goTermZdbID(relatedEntityFields.get("goTermZdbID"))
                .publicationID(relatedEntityFields.get("publicationID"))
                .build();
    }

    public Map<String, String> toMap() {
        return Map.of(
                "goID", goID.toString(),
                "markerZdbID", markerZdbID,
                "goTermZdbID", goTermZdbID,
                "publicationID", publicationID
        );
    }

    public String toString() {
        return toMap()
                .entrySet()
                .stream()
                .map(es -> es.getKey() + ": " + es.getValue() + "\n")
                .reduce("", String::concat);
    }

    public String getGoID() {
        return goID.toString();
    }

    /**
     * This is a wrapper class for the GO ID to make sure it is always prefixed with GO:
     */
    private static class GoID {
        private String goID;

        public GoID(String goID) {
            if (goID.startsWith("GO:")) {
                this.goID = goID;
            } else {
                this.goID = "GO:" + goID;
            }
            if (!this.isValidGoID(this.goID)) {
                throw new RuntimeException("Invalid GO ID: " + this.goID);
            }
        }

        private boolean isValidGoID(String goID) {
            return goID.matches("GO:[0-9]{7}");
        }

        public String toString() {
            return goID;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof GoID)) {
                return false;
            }
            GoID other = (GoID) o;
            return this.goID.equals(other.goID);
        }
    }

    /**
     * Override builder method for the go term
     */
    public static class MarkerGoTermEvidenceSlimDTOBuilder {
        public MarkerGoTermEvidenceSlimDTOBuilder goID(String goID) {
            this.goID = new GoID(goID);
            return this;
        }
    }
}
