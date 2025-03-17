package org.zfin.orthology;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ortholog_evidence")
@Getter
@Setter
public class OrthologEvidence implements Serializable {

    @EmbeddedId
    private OrthologEvidenceId id;

    @ManyToOne
    @MapsId("ortholog")
    @JoinColumn(name = "oev_ortho_zdb_id", nullable = false)
    private Ortholog ortholog;

    @ManyToOne
    @MapsId("evidenceCode")
    @JoinColumn(name = "oev_evidence_code", nullable = false)
    private EvidenceCode evidenceCode;

    @ManyToOne
    @MapsId("publication")
    @JoinColumn(name = "oev_pub_zdb_id", nullable = false)
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "oev_evidence_term_zdb_id")
    private GenericTerm evidenceTerm;

    public OrthologEvidence() {
    }

    public OrthologEvidence(EvidenceCode evidenceCode, Ortholog ortholog, Publication publication) {
        this.id = new OrthologEvidenceId(ortholog.getZdbID(), evidenceCode.getCode(), publication.getZdbID());
        this.evidenceCode = evidenceCode;
        this.ortholog = ortholog;
        this.publication = publication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrthologEvidence that = (OrthologEvidence) o;
        return ortholog.equals(that.ortholog) &&
                evidenceCode.equals(that.evidenceCode) &&
                publication.equals(that.publication);
    }

    @Override
    public int hashCode() {
        int result = ortholog.hashCode();
        result = 31 * result + evidenceCode.hashCode();
        result = 31 * result + publication.hashCode();
        return result;
    }

    public enum Code {
        AA,
        CE,
        CL,
        FC,
        NT,
        PT,
        OT;

        public String toString() {
            return name();
        }

        public String getString() {
            return name();
        }
    }

    @Embeddable
    @Getter
    @Setter
    public static class OrthologEvidenceId implements Serializable {

        private String ortholog; // Maps to oev_ortho_zdb_id
        private String evidenceCode; // Maps to oev_evidence_code
        private String publication; // Maps to oev_pub_zdb_id

        public OrthologEvidenceId() {}

        public OrthologEvidenceId(String ortholog, String evidenceCode, String publication) {
            this.ortholog = ortholog;
            this.evidenceCode = evidenceCode;
            this.publication = publication;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrthologEvidenceId that = (OrthologEvidenceId) o;
            return Objects.equals(ortholog, that.ortholog) &&
                    Objects.equals(evidenceCode, that.evidenceCode) &&
                    Objects.equals(publication, that.publication);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ortholog, evidenceCode, publication);
        }
    }
}
