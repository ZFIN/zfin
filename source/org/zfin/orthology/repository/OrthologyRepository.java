package org.zfin.orthology.repository;

import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.CachedRepository;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.orthology.EvidenceCode;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.SpeciesCriteria;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public interface OrthologyRepository extends CachedRepository {

    Object[] getOrthologies(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria);

    void saveOrthology(Ortholog ortholog, Publication publication);

    /**
     * Update a fast-search table to hold info about evidence codes. In order
     * to display them correctly evidence codes have to be stored in a particular way
     *
     * @param ortholog
     */
    void updateFastSearchEvidenceCodes(Set<Ortholog> ortholog);

    List<OrthologyPresentationRow> getOrthologyForGene(Marker gene);

    /**
     * Retrieve Orthologues for given gene and publication
     *
     * @param gene        gene
     * @param publication publication
     * @return
     */
    List<OrthologyPresentationRow> getOrthologyForGene(Marker gene, Publication publication);


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
}
