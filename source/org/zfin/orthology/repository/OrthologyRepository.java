package org.zfin.orthology.repository;

import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.CachedRepository;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public interface OrthologyRepository extends CachedRepository {

    void saveOrthology(Ortholog ortholog, Publication publication);

    List<String> getEvidenceCodes(Marker gene);

    List<OrthologySlimPresentation> getOrthologySlimForGeneId(String geneId);

    /**
     * Retrieve Evidence codes for given gene and publication
     *
     * @param gene        marker
     * @param publication publication
     * @return list of evidence codes.
     */
    List<String> getEvidenceCodes(Marker gene, Publication publication);

    /**
     * Retireve list of orthologs for given zebrafish gene
     *
     * @param zdbID gene zdb ID
     * @return list of orthologs
     */
    List<Ortholog> getOrthologs(String zdbID);

    /**
     * Retireve list of orthologs for given zebrafish gene
     *
     * @param gene gene
     * @return list of orthologs
     */
    List<Ortholog> getOrthologs(Marker gene);

    /**
     * Retrieve NcbiOtherSpeciesGene object
     *
     * @param ncbiID NCBI Gene
     * @return
     */
    NcbiOtherSpeciesGene getNcbiGene(String ncbiID);

    List<EvidenceCode> getEvidenceCodes();

    EvidenceCode getEvidenceCode(String string);

    /**
     * Retrieve Ortholog by ID
     *
     * @param orthID Ortholog ID
     * @return Ortholog
     */
    Ortholog getOrtholog(String orthID);

    /**
     * Delete an Ortholog
     *
     * @param ortholog Ortholog
     */
    void deleteOrtholog(Ortholog ortholog);

    Ortholog getOrthologByGeneAndNcbi(Marker gene, NcbiOtherSpeciesGene ncbiGene);

    List<NcbiOrthoExternalReference> getNcbiExternalReferenceList(String ncbiID);
}
