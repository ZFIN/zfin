package org.zfin.orthology;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

/**
 * ToDo: Please add documentation for this class.
 */
public class OrthologyEvidenceFastSearch {

    private String zdbID;
    private Marker marker;
    private OrthoEvidence.Code code;
    private String organism;
    private Publication publication;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public OrthoEvidence.Code getCode() {
        return code;
    }

    public void setCode(OrthoEvidence.Code code) {
        this.code = code;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }


    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public int hashCode() {
        int result = 37;
        if (marker != null)
            result = 37 * result + marker.hashCode();
        if (publication != null)
            result = 39 * result + publication.hashCode();
        if (code != null)
            result = 13 * result + code.hashCode();
        return result;
    }

    /**
     * A fast search record is considered equal all attributes are the same
     * except the organism
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (!(o instanceof OrthologyEvidenceFastSearch))
            return false;
        OrthologyEvidenceFastSearch fast = (OrthologyEvidenceFastSearch) o;

        if (!ObjectUtils.equals(fast.getMarker(), marker))
            return false;

        if (!ObjectUtils.equals(fast.getPublication(), publication))
            return false;

        if (!ObjectUtils.equals(fast.getCode(), code))
            return false;

        return true;
    }

}
