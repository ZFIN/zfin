package org.zfin.orthology;

import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.io.Serializable;

public class OrthologEvidence implements Serializable {

    private Ortholog ortholog;
    private EvidenceCode evidenceCode;
    private Publication publication;
    private GenericTerm evidenceTerm;
    public OrthologEvidence() {
    }

    public OrthologEvidence(EvidenceCode evidenceCode, Ortholog ortholog, Publication publication) {
        this.evidenceCode = evidenceCode;
        this.ortholog = ortholog;
        this.publication = publication;
    }

    public EvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(EvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public Ortholog getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(Ortholog ortholog) {
        this.ortholog = ortholog;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public GenericTerm getEvidenceTerm() {
        return evidenceTerm;
    }

    public void setEvidenceTerm(GenericTerm evidenceTerm) {
        this.evidenceTerm = evidenceTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrthologEvidence that = (OrthologEvidence) o;

        if (!ortholog.equals(that.ortholog)) return false;
        if (!evidenceCode.equals(that.evidenceCode)) return false;
        return publication.equals(that.publication);

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
