package org.zfin.fish.repository;

import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.WarehouseSummary;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Fish;

import java.util.List;
import java.util.Set;

/**
 * Basic repository to handle fish search requests.
 */
public interface FishRepository {

    /**
     * Retrieve figures for a given fish.
     *
     * @param fishID fish ID
     * @return set of figures.
     */
   Set<ZfinFigureEntity> getAllFigures(String fishID);

    Fish getFishByName(String name);

    /**
     * retrieve all figures for given fish id
     *
     * @param fishID fish ID
     * @return set of figures
     */
    Set<ZfinFigureEntity> getPhenotypeFigures(String fishID);

    /**
     * Retrieve the longest genotype experiment group id for all fish
     *
     * @return String
     */
    

    /**
     * Retrieve the Warehouse summary info for a given mart.
     *
     * @param mart mart
     * @return warehouse summary
     */
    WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart);

    /**
     * Retrieve the status of the fish mart:
     * true: fish mart ready for usage
     * false: fish mart is being rebuilt.
     *
     * @return status
     */
    ZdbFlag getFishMartStatus();
}
