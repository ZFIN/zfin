package org.zfin.anatomy.repository;

import org.zfin.anatomy.*;
import org.zfin.expression.ExpressionStructure;
import org.zfin.framework.CachedRepository;
import org.zfin.ontology.GenericTerm;

import java.util.List;

/**
 * This is the interface that provides access to the persistence layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public interface AnatomyRepository extends CachedRepository {

    /**
     * This returns the complete list of all developmental stages.
     *
     * @return A list of DevelopmentalStage objects.
     */
    List<DevelopmentStage> getAllStages();

    /**
     * This returns the complete list of all developmental stages
     * except the 'Unknown' stage.
     *
     * @return A list of DevelopmentalStage objects.
     */
    List<DevelopmentStage> getAllStagesWithoutUnknown();

    /**
     * Load the stage identified by its identifier.
     * First, it tries to find by stageID if available (fastest wasy since it
     * is the primary key). Second, it tries to find the stage by zdbID if
     * available. Lastly, it tries to search by name if available. Each of them needs to
     * find a unique record otherwise a Runtime exception is being thrown.
     *
     * @param stage development stage
     * @return stage
     */
    DevelopmentStage getStage(DevelopmentStage stage);

    DevelopmentStage getStageByID(String stageID);

    /**
     * Find the stage by stage name.
     *
     * @param stageName stage name
     * @return stage
     */
    DevelopmentStage getStageByName(String stageName);

    /**
     * Retrieve anatomy items grouped by hierarchy and only those items
     * that fall into the given stage, i.e. the item whose start and end
     * includes the given stage.
     *
     * @param stage development stage
     * @return AnatomyStatistics
     */
    List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage);

    /**
     * Retrieve the statistics about expressed genes in a given
     * anatomical term.
     *
     * @param anatomyZdbID zdbID
     * @return AnatomyStatistics
     */
    AnatomyStatistics getAnatomyStatistics(String anatomyZdbID);

    /**
     * Retrieve the statistics about expressed genes in a given
     * anatomical term.
     *
     * @param anatomyZdbID zdbID
     * @return AnatomyStatistics
     */
    AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID);

    /**
     * Retrieve a list of terms that develops_from the given term and are defined in the
     * stage range given.
     * In other words, the given structure develops_into the list of terms retrieved.
     * E.g.
     * 'adaxial cell' develops_into 'migratory slow muscle precursor cell'
     *
     * @param termID     Term id
     * @param startHours start
     * @param endHours   end
     * @return list of anatomy terms
     */
    List<GenericTerm> getTermsDevelopingFromWithOverlap(String termID, double startHours, double endHours);

    /**
     * Create a new structure - post-composed - for the structure pile.
     *
     * @param structure structure
     */
    void createPileStructure(ExpressionStructure structure);

    DevelopmentStage getStageByOboID(String ID);

    DevelopmentStage getStageByStartHours(float start);

    DevelopmentStage getStageByEndHours(float end);
}
