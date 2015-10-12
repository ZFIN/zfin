package org.zfin.orthology.service;

import org.springframework.stereotype.Service;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.publication.Publication;

import java.util.Iterator;
import java.util.Set;

@Service
public class OrthologService {

    public void replaceEvidenceCodes(Ortholog ortholog, Set<OrthologEvidence> evidenceSet, Publication publication) {
        if (ortholog == null || evidenceSet == null)
            return;

        Set<OrthologEvidence> existingCodes = ortholog.getEvidenceSet();
        Iterator<OrthologEvidence> it = existingCodes.iterator();
        while (it.hasNext()) {
            OrthologEvidence evidence = it.next();
            if (evidence.getPublication().equals(publication))
                it.remove();
        }
        existingCodes.addAll(evidenceSet);
    }
}
