package org.zfin.marker.presentation;

import org.zfin.orthology.presentation.OrthologyPresentationRow;

import java.util.Collections;
import java.util.List;

/**
 * Represents the entire section
 */
public class OrthologyPresentationBean {

    private List<OrthologyPresentationRow> orthologs;
    private String note;

    public List<OrthologyPresentationRow> getOrthologs() {
        if (orthologs == null) {
            return Collections.emptyList();
        }
        return orthologs;
    }

    public void setOrthologs(List<OrthologyPresentationRow> orthologs) {
        this.orthologs = orthologs;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
