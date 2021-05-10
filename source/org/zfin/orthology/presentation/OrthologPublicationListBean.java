package org.zfin.orthology.presentation;

import org.zfin.orthology.EvidenceCode;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.HashSet;
import java.util.Set;

public class OrthologPublicationListBean extends PublicationListBean {

    private Ortholog ortholog;
    private EvidenceCode evidenceCode;

    public Ortholog getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(Ortholog ortholog) {
        this.ortholog = ortholog;
    }

    public EvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(EvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    @Override
    public Set<Publication> getPublications() {
        Set<Publication> publications = new HashSet<>();
        if (evidenceCode != null) {
            for (OrthologEvidence evidence : ortholog.getEvidenceSet()) {
                if (evidence.getEvidenceCode().equals(evidenceCode)) {
                    publications.add(evidence.getPublication());
                }
            }
        }
        return publications;
    }
}
