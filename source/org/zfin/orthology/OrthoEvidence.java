package org.zfin.orthology;

import org.zfin.publication.Publication;

import java.io.Serializable;

/**
 * Not related to orthology search code - maps directly (if exotically)
 * to orthology_evidence table
 * todo: add hibernate tests & documentation
 */
public class OrthoEvidence implements Serializable {

    private String orthologueZdbID;
    private Code orthologueEvidenceCode;
    private Publication publication;


    public Code getOrthologueEvidenceCode() {
        return orthologueEvidenceCode;
    }

    public void setOrthologueEvidenceCode(Code orthologueEvidenceCode) {
        this.orthologueEvidenceCode = orthologueEvidenceCode;
    }


    public String getOrthologueZdbID() {
        return orthologueZdbID;
    }

    public void setOrthologueZdbID(String orthologueZdbID) {
        this.orthologueZdbID = orthologueZdbID;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("ORHTOLOGY EVIDENCE");
        sb.append("orthologueZdbID: ").append(orthologueZdbID);
        sb.append(newline);
        sb.append("orthologueEvidenceCode: ").append(orthologueEvidenceCode);
        sb.append(newline);
        sb.append("Publication: ").append(publication);
        return sb.toString();
    }

    public int hashCode() {
        int num = 39;
        if (orthologueZdbID != null)
            num += orthologueZdbID.hashCode();
        if (orthologueEvidenceCode != null)
            num += orthologueEvidenceCode.hashCode();
        if (publication != null)
            num += publication.hashCode();
        return num;
    }

    /**
     * Assumes that orthologueZdbID, orthologueEvidenceCode and publication are not null.
     *
     * @param o object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (!(o instanceof OrthoEvidence))
            return false;
        OrthoEvidence ortho = (OrthoEvidence) o;

        return orthologueZdbID.equals(ortho.orthologueZdbID) &&
                orthologueEvidenceCode == ortho.orthologueEvidenceCode &&
                publication.equals(ortho.publication);
    }


    public enum Code {
        AA,
        CE,
        CL,
        FC,
        FH,
        IX,
        NS,
        NT,
        SI,
        SL,
        SS,
        SU,
        XH;

        public String toString() {
            return name();
        }

        public String getString(){
            return name() ;
        }

    }
}
