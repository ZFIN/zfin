package org.zfin.util.database.presentation;

import org.zfin.util.database.UnloadIndexingService;
import org.zfin.util.database.UnloadService;

import java.util.*;

/**
 * Convenience form bean to hold info for the summary pages.
 */
public class UnloadBean {

    private TreeMap<String, Map<String, Integer>> dataTableMap;

    private UnloadService unloadService;
    private UnloadIndexingService unloadIndexingService;
    private String tableName;
    private String date;
    private String entityID;

    public void setUnloadService(UnloadService unloadService) {
        this.unloadService = unloadService;
    }

    public UnloadService getUnloadService() {
        return unloadService;
    }

    public TreeMap<String, Map<String, Integer>> getDataTableMap() {
        return dataTableMap;
    }

    public List<UnloadService.TableHistogram> getTableHistogram() {
        return unloadService.getTableHistogram(tableName);
    }

    public void setDataTableMap(TreeMap<String, Map<String, Integer>> dataTableMap) {
        this.dataTableMap = dataTableMap;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, List<UnloadService.EntityMatch>> getDeletedEntityMap() {
        return unloadService.getDeletedEntityMap(tableName);
    }

    public Map<String, List<UnloadService.EntityMatch>> getAddedEntityMap() {
        return unloadService.getAddedEntityMap(tableName);
    }

    public Map<String, List<UnloadService.EntityMatch>> getModifiedEntityMap() {
        return unloadService.getModifiedEntityMap(tableName);
    }

    public List<UnloadService.EntityTrace> getEntityHistory() {
        return unloadService.getEntityHistory(entityID, tableName);
    }

    public Map<String, String> getDateList() {
        LinkedHashMap<String, String> dateList = new LinkedHashMap<String, String>();
        List<String> dates = unloadService.getAllUnloadedDates();
        Collections.sort(dates);
        Collections.reverse(dates);
        for (String date : dates) {
            dateList.put(date, date);
        }
        return dateList;
    }

    public Map<String, String> getUnIndexedTables() {
        LinkedHashMap<String, String> dateList = new LinkedHashMap<String, String>();
        List<String> dates = unloadService.getUnIndexedTables();
        Collections.sort(dates);
        for (String date : dates) {
            dateList.put(date, date);
        }
        return dateList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public UnloadIndexingService getUnloadIndexingService() {
        return unloadIndexingService;
    }

    public void setUnloadIndexingService(UnloadIndexingService unloadIndexingService) {
        this.unloadIndexingService = unloadIndexingService;
    }
}
