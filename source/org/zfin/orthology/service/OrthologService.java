package org.zfin.orthology.service;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.zfin.marker.Marker;
import org.zfin.orthology.NcbiOrthoExternalReference;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.publication.Publication;

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

    public Ortholog createOrtholog(Marker gene, NcbiOtherSpeciesGene ncbiGene) {
        Ortholog ortholog = new Ortholog();
        ortholog.setZebrafishGene(gene);
        ortholog.setNcbiOtherSpeciesGene(ncbiGene);

        List<NcbiOrthoExternalReference> ncbiOrthoExternalReferenceList = getOrthologyRepository().getNcbiExternalReferenceList(ncbiGene.getID());
        if (CollectionUtils.isEmpty(ncbiOrthoExternalReferenceList))
            throw new RuntimeException("No External references found for ncbi Gene " + ncbiGene.getID());
        ortholog.setExternalReferenceListFromNcbiReferenceList(ncbiOrthoExternalReferenceList);
        return ortholog;
    }

    public Ortholog createOrthologWithoutReferences(Marker gene, NcbiOtherSpeciesGene ncbiGene) {
        Ortholog ortholog = new Ortholog();
        ortholog.setZebrafishGene(gene);
        ortholog.setNcbiOtherSpeciesGene(ncbiGene);

        return ortholog;
    }

    public void createReferences(Ortholog ortholog, NcbiOtherSpeciesGene ncbiGene) {
        List<NcbiOrthoExternalReference> ncbiOrthoExternalReferenceList = getOrthologyRepository().getNcbiExternalReferenceList(ncbiGene.getID());
        if (CollectionUtils.isEmpty(ncbiOrthoExternalReferenceList))
            throw new RuntimeException("No External references found for ncbi Gene " + ncbiGene.getID());
        ortholog.setExternalReferenceListFromNcbiReferenceList(ncbiOrthoExternalReferenceList);

    }
}
