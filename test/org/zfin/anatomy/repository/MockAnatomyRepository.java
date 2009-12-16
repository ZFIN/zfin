package org.zfin.anatomy.repository;

import org.zfin.anatomy.*;
import org.zfin.expression.ExpressionStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation to support unit tests.
 */
public class MockAnatomyRepository implements AnatomyRepository {

    private Map<java.lang.String, DevelopmentStage> stageMap = new HashMap<java.lang.String, DevelopmentStage>();
    private Map<java.lang.String, AnatomyItem> itemMap = new HashMap<java.lang.String, AnatomyItem>();

    public List<DevelopmentStage> getAllStages() {
        List<DevelopmentStage> stages = new ArrayList<DevelopmentStage>();
        stages.addAll(stageMap.values());
        return stages;
    }

    public List<DevelopmentStage> getAllStagesWithoutUnknown() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyItem> getAllAnatomyItems() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<java.lang.String> getAllAnatomyNamesAndSynonyms() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyItem> getAnatomyItemsByName(String searchString, boolean includeObsoletes) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getAnatomyItemsByName(String searchString, int limit) {
        if(limit<=0){
            return getAnatomyItemsByName(searchString,false) ;
        }
        return null ; 
    }


    public List<AnatomyStatistics> getAllAnatomyItemStatistics() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyStatistics> getAnatomyItemStatistics(java.lang.String searchTerm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AnatomyItem loadAnatomyItem(AnatomyItem anatomyItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AnatomyItem getAnatomyTermByID(String aoZdbID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyRelationship> getAnatomyRelationships(AnatomyItem anatomyItem) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAllAnatomyRelationshipTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DevelopmentStage getStage(DevelopmentStage stage) {
        java.lang.String id = stage.getZdbID();
        return stageMap.get(id);
    }

    public DevelopmentStage getStageByID(String stageID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DevelopmentStage getStageByName(java.lang.String stageName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void insertDevelopmentStage(DevelopmentStage stage) {
        java.lang.String id = stage.getZdbID();
        if (stageMap.containsKey(id))
            throw new RuntimeException("Tried to insert stage twice: " + stage.toString());
        stageMap.put(id, stage);
    }

    public void insertAnatomyItem(AnatomyItem item) {
        java.lang.String id = item.getZdbID();
        if (itemMap.containsKey(id))
            throw new RuntimeException("Tried to insert stage twice: " + item.toString());
        itemMap.put(id, item);
    }

    public AnatomyStatistics getAnatomyStatistics(java.lang.String anatomyZdbID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Retrieve an anatomy term for a given name.
     *
     * @param name ao term name
     * @return AnatomyItem
     */
    public AnatomyItem getAnatomyItem(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomySynonym> getAnatomyTermsBySynonymName(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSubstructureOf(AnatomyItem term, AnatomyItem substructureTerm) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAnatomyTermsForAutoComplete() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyItem> getTermsDevelopingFromWithOverlap(String termID, double startHours, double endHours) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AnatomyItem> getTermsDevelopingIntoWithOverlap(String termID, double startHours, double endHours) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createPileStructure(ExpressionStructure structure) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public AnatomyItem getAnatomyTermByOboID(String termID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void invalidateCachedObjects() {
        throw new RuntimeException("Not implemented yet");
    }
}
