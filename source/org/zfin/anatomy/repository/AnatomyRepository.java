package org.zfin.anatomy.repository;

import org.zfin.anatomy.*;
import org.zfin.framework.CachedRepository;

import java.util.List;

/**
 * This is the interface that provides access to the persistence layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public interface AnatomyRepository extends CachedRepository {

    /**
     * This returns the complete list of all developmental stages.
     * @return A list of DevelopmentalStage objects.
     */
    List<DevelopmentStage> getAllStages();

    /**
     * This returns the complete list of all developmental stages
     * except the 'Unknown' stage.
     * @return A list of DevelopmentalStage objects.
     */
    List<DevelopmentStage> getAllStagesWithoutUnknown();

    /**
     * Load the stage identified by its identifier.
     * First, it tries to find by stageID if available (fastest wasy since it
     * is the primary key). Second, it tries to find the stage by zdbID if
     * available. Lastly, it tries to search by name if available. Each of them needs to
     * find a unique record otherwise a Runtime exception is being thrown.
     * @param stage development stage
     * @return stage
     */
    DevelopmentStage getStage(DevelopmentStage stage);

    /**
     * Find the stage by stage name.
     * @param stageName stage name
     * @return stage
     */
    DevelopmentStage getStageByName(String stageName);

    /**
     * This returns the complete list of all anatomy Item objects.
     * @return A list of AnatomyItem objects.
     */
    List<AnatomyItem> getAllAnatomyItems();

    /**
     * This returns the complete list of all anatomy names and synonym names,
     * sorted alphabetically
     * @return A list of strings.
     */
    List<String> getAllAnatomyNamesAndSynonyms();

    /**
     * Retrieve a list of anatomy terms that match a search string.
     * Matching is done via
     * 1) 'contains'
     * 2) searching all synonyms as 'contains'
     * 3) Case insensitive in both cases
     *  
     * @param searchString string
     * @return list of anatomy terms
     */
    List<AnatomyItem> getAnatomyItemsByName(String searchString);
    /**
     * Retrieves the statistics for all anatomy items.
     * See the BO for more info.
     * @return AnatomyStatistics objects.
     */
    List<AnatomyStatistics> getAllAnatomyItemStatistics();

    /**
     * Retrieve statistics for all anatomy terms that match the
     * search term name. The search is case-insensitive.
     *
     * @param searchTerm search term string
     * @return list of AnatomyStatistics object. Null if no term was found or the search
     * term is null.
     */
    List<AnatomyStatistics> getAnatomyItemStatistics(String searchTerm);

    /**
     * Retrieve an AnatomyItem identified by one of the IDs. The provided
     * object should have one of the possibel IDs populated.
     * //toDo: Specify which IDs are used.
     *
     * @param anatomyItem Anatomy Term
     * @return AnatomyItem
     */
    AnatomyItem loadAnatomyItem(AnatomyItem anatomyItem);

    List<AnatomyRelationship> getAnatomyRelationships(AnatomyItem anatomyItem);

    /**
     * Retrieve all relationship types (small number) for anatomical items.
     * @return AnatomyRelationshipType
     */
    List<String> getAllAnatomyRelationshipTypes();

    /**
     * Retrieve anatomy items grouped by hierarchy and only those items
     * that fall into the given stage, i.e. the item whose start and end
     * includes the given stage. 
     * @param stage development stage
     * @return AnatomyStatistics
     */
    List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage);

    /**
     * Persist a developmental stage.
     * @param stage development stage
     */
    void insertDevelopmentStage(DevelopmentStage stage);

    /**
     * Persist an anatomical item. This includes the relationship with other
     * terms and its synonyms if any are available.
     * @param item Anatomy Term
     */
    void insertAnatomyItem(AnatomyItem item);


    /**
     * Retrieve the statistics about expressed genes in a given
     * anatomical term. 
     * @param anatomyZdbID zdbID
     * @return AnatomyStatistics
     */
    AnatomyStatistics getAnatomyStatistics(String anatomyZdbID);

    /**
     * Retrieve the statistics about expressed genes in a given
     * anatomical term.
     * @param anatomyZdbID zdbID
     * @return AnatomyStatistics
     */
    AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID);

    /**
     * Retrieve an anatomy term for a given name.
     * The lookup is case-insensitive.
     * Returns null if no term is found or the search name is null

     * @param name ao term name
     * @return AnatomyItem
     */
    AnatomyItem getAnatomyItem(String name);

    /**
     * Retrieve a list of anatomy terms for a given synonym name
     * The lookup is case-insensitive.
     * Returns null if no term is found or the search name is null.

     * @param name ao synonym name
     * @return AnatomyItem
     */
    List<AnatomySynonym> getAnatomyTermsBySynonymName(String name);
}
