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
        StringBuilder sb = new StringBuilder("ORHTOLOGY EVIDENCE");
        sb.append("orthologueZdbID: ").append(orthologueZdbID);
        sb.append("\r\n");
        sb.append("orthologueEvidenceCode: ").append(orthologueEvidenceCode);
        sb.append("\r\n");
        sb.append("Publication: ").append(publication);
        return sb.toString();
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

        public String toString(){
            return name() ; 
        }

    }
}
