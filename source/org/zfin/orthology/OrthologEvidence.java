package org.zfin.orthology;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ortholog_evidence")
@Getter
@Setter
public class OrthologEvidence implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oev_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "oev_ortho_zdb_id", nullable = false)
    private Ortholog ortholog;

    @ManyToOne
    @JoinColumn(name = "oev_evidence_code", nullable = false)
    private EvidenceCode evidenceCode;

    @ManyToOne
    @JoinColumn(name = "oev_pub_zdb_id", nullable = false)
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "oev_evidence_term_zdb_id")
    private GenericTerm evidenceTerm;

    public OrthologEvidence() {
    }

    public OrthologEvidence(EvidenceCode evidenceCode, Ortholog ortholog, Publication publication) {
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
}
