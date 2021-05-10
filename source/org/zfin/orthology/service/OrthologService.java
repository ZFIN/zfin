package org.zfin.orthology.service;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.publication.Publication;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

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
        if (CollectionUtils.isNotEmpty(evidenceSet)) {
            existingCodes.addAll(evidenceSet);
            getInfrastructureRepository().insertRecordAttribution(ortholog.getZdbID(), publication.getZdbID());
            // attribute the gene to the pub as well
            getInfrastructureRepository().insertRecordAttribution(ortholog.getZebrafishGene().getZdbID(), publication.getZdbID());
        } else {
            // remove attribution as all evidence codes are removed for this publication
            getInfrastructureRepository().deleteRecordAttribution(ortholog.getZdbID(), publication.getZdbID());
            // remove the gene from the pub too
            getInfrastructureRepository().deleteRecordAttribution(ortholog.getZebrafishGene().getZdbID(), publication.getZdbID());
        }
    }

    /**
     * Populate a new Ortholog entity from a zebrafish marker and an NCBI Gene.
     * It will copy the external reference collection from the NCBI gene onto the
     * ortholog record. They are not re-used as deletion of the NCBI references
     * through a load should not delete the references on the ortholog.
     * It also copies the name, symbol, chromosome number into the Ortholog record.
     *
     * @param gene     zebrafish gene
     * @param ncbiGene NCBI other species gene
     * @return a new un-persisted Ortholog entity
     */
    public Ortholog createOrthologEntity(Marker gene, NcbiOtherSpeciesGene ncbiGene) {
        Ortholog ortholog = new Ortholog();
        ortholog.setZebrafishGene(gene);
        ortholog.setNcbiOtherSpeciesGene(ncbiGene);
        ortholog.setName(ncbiGene.getName());
        ortholog.setSymbol(ncbiGene.getAbbreviation());
        ortholog.setChromosome(ncbiGene.getChromosome());
        ortholog.setOrganism(ncbiGene.getOrganism());
        SortedSet<OrthologExternalReference> referenceList = new TreeSet<>();
        for (NcbiOrthoExternalReference ref : ncbiGene.getNcbiExternalReferenceList()) {
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
