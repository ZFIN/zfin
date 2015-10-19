package org.zfin.orthology.service;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;

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

    public Ortholog createOrthologEntity(Marker gene, NcbiOtherSpeciesGene ncbiGene) {
        Ortholog ortholog = new Ortholog();
        ortholog.setZebrafishGene(gene);
        ortholog.setNcbiOtherSpeciesGene(ncbiGene);
        List<NcbiOrthoExternalReference> ncbiOrthoExternalReferenceList = getOrthologyRepository().getNcbiExternalReferenceList(ncbiGene.getID());
        Set<OrthologExternalReference> referenceList = new HashSet<>(ncbiOrthoExternalReferenceList.size());
        for (NcbiOrthoExternalReference ref : ncbiOrthoExternalReferenceList) {
            OrthologExternalReference orthoRef = new OrthologExternalReference();
            orthoRef.setAccessionNumber(ref.getAccessionNumber());
            orthoRef.setOrtholog(ortholog);
            orthoRef.setReferenceDatabase(ref.getReferenceDatabase());
            referenceList.add(orthoRef);
        }
        ortholog.setExternalReferenceList(referenceList);
        return ortholog;
    }
}
