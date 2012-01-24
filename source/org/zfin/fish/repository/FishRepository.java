package org.zfin.fish.repository;

import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.presentation.Fish;
import org.zfin.infrastructure.ZfinFigureEntity;

import java.util.List;
import java.util.Set;

/**
 * Basic repository to handle fish search requests.
 */
public interface FishRepository {

    public FishSearchResult getFish(FishSearchCriteria criteria);

    /**
     * Retrieve figures for a given fish.
     *
     * @param fishID fish ID
     * @return set of figures.
     */
    Set<ZfinFigureEntity> getAllFigures(String fishID);

    /**
     * Retrieve fish by primary key
     *
     * @param fishID ID
     * @return fish
     */
    Fish getFish(Long fishID);

    /**
     * Retrieve fish by genotype experiment ids
     *
     * @param genoxIDs IDs
     * @return fish
     */
    Fish getFish(String genoxIDs);

    /**
     * retrieve all figures for given fish id
     *
     * @param fishID fish ID
     * @return set of figures
     */
    Set<ZfinFigureEntity> getPhenotypeFigures(String fishID);


    /**
     * Retrieve all figures for given fish id
     * that have phenotypes associated with the termID list
     * directly or indirectly through a substructure.
     *
     * @param fishID  fish ID
     * @param termIDs  term ID list
     * @return set of figures
     */
    Set<ZfinFigureEntity> getFiguresByFishAndTerms(String fishID, List<String> termIDs);

    /**
     * Retrieve the longest genotype experiment group id for all fish
     * @return String
     */
    String getGenoxMaxLength();
}
