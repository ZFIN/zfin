package org.zfin.uniprot.dto;

import lombok.*;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String inferredFrom;

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
            .inferredFrom(markerGoTermEvidence.getInferredFrom())
            .build();
    }

    public static MarkerGoTermEvidenceSlimDTO fromMap(Map<String, String> relatedEntityFields) {
        return MarkerGoTermEvidenceSlimDTO.builder()
                .goID(relatedEntityFields.get("goID"))
                .markerZdbID(relatedEntityFields.get("markerZdbID"))
                .goTermZdbID(relatedEntityFields.get("goTermZdbID"))
                .publicationID(relatedEntityFields.get("publicationID"))
                .inferredFrom(relatedEntityFields.get("inferredFrom"))
                .build();
    }

    public Map<String, String> toMap() {
        return Map.of(
                "goID", goID.toString(),
                "markerZdbID", markerZdbID,
                "goTermZdbID", goTermZdbID,
                "publicationID", publicationID,
                "inferredFrom", inferredFrom
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
        private final String goID;

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
            if (!(o instanceof GoID other)) {
                return false;
            }
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

        public MarkerGoTermEvidenceSlimDTOBuilder inferredFrom(String inferredFrom) {
            this.inferredFrom = inferredFrom;
            return this;
        }

        public MarkerGoTermEvidenceSlimDTOBuilder inferredFrom(Set<InferenceGroupMember> inferredFrom) {
            //inferredFrom can be a set of multiple values per mgte, but it is only ** one-to-one ** in the context of uniprot load
            assert inferredFrom.size() <= 1;

            if (inferredFrom.isEmpty()) {
                this.inferredFrom = null;
                return this;
            }

            //get first element
            InferenceGroupMember inferenceGroupMember = inferredFrom.stream().findFirst().get();
            this.inferredFrom = inferenceGroupMember.getInferredFrom();

            return this;
        }
    }
}
