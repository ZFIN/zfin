package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "inference_group_member")
@IdClass(InferenceGroupMember.InferenceGroupMemberId.class)
public class InferenceGroupMember implements Serializable {

    @Id
    @Column(name = "infgrmem_mrkrgoev_zdb_id")
    private String markerGoTermEvidenceZdbID;

    @Id
    @Column(name = "infgrmem_inferred_from")
    private String inferredFrom;

    @Getter
    @Setter
    public static class InferenceGroupMemberId implements Serializable {

        private String markerGoTermEvidenceZdbID;
        private String inferredFrom;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InferenceGroupMemberId that = (InferenceGroupMemberId) o;

            if (inferredFrom != null ? !inferredFrom.equals(that.inferredFrom) : that.inferredFrom != null)
                return false;
            if (markerGoTermEvidenceZdbID != null ? !markerGoTermEvidenceZdbID.equals(that.markerGoTermEvidenceZdbID) : that.markerGoTermEvidenceZdbID != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = markerGoTermEvidenceZdbID != null ? markerGoTermEvidenceZdbID.hashCode() : 0;
            result = 31 * result + (inferredFrom != null ? inferredFrom.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InferenceGroupMember that = (InferenceGroupMember) o;

        if (inferredFrom != null ? !inferredFrom.equals(that.inferredFrom) : that.inferredFrom != null) return false;
        if (markerGoTermEvidenceZdbID != null ? !markerGoTermEvidenceZdbID.equals(that.markerGoTermEvidenceZdbID) : that.markerGoTermEvidenceZdbID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = markerGoTermEvidenceZdbID != null ? markerGoTermEvidenceZdbID.hashCode() : 0;
        result = 31 * result + (inferredFrom != null ? inferredFrom.hashCode() : 0);
        return result;
    }
}
