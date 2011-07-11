package org.zfin.marker.presentation;

import org.zfin.orthology.repository.OrthologyPresentationRow;

import java.util.List;

/**
 * Represents the entire section
 */
public class OrthologyPresentationBean {

    private List<OrthologyPresentationRow> orthologues;
    private List<String> evidenceCodes ;
    private List<String> notes ;

    public List<OrthologyPresentationRow> getOrthologues() {
        return orthologues;
    }

    public void setOrthologues(List<OrthologyPresentationRow> orthologues) {
        this.orthologues = orthologues;
    }

    public List<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public void setEvidenceCodes(List<String> evidenceCodes) {
        this.evidenceCodes = evidenceCodes;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
}
