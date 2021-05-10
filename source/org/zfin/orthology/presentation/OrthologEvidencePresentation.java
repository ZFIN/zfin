package org.zfin.orthology.presentation;

import org.zfin.orthology.EvidenceCode;
import org.zfin.publication.Publication;

import java.util.Set;
import java.util.TreeSet;

public class OrthologEvidencePresentation {

    private EvidenceCode code;
    private Set<Publication> publications;

    public EvidenceCode getCode() {
        return code;
    }

    public void setCode(EvidenceCode code) {
        this.code = code;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void addPublication(Publication publication) {
        if (publications == null) {
            publications = new TreeSet<>();
        }
        publications.add(publication);
    }

}
