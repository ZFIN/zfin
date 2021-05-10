package org.zfin.util.database.presentation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Person;
import org.zfin.util.FileInfo;
import org.zfin.util.database.UnloadIndexingService;
import org.zfin.util.database.UnloadService;
import org.zfin.util.downloads.DownloadFileService;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;
import org.zfin.util.downloads.presentation.DownloadFileInfo;

import java.io.IOException;
import java.util.*;

/**
 * Convenience form bean to hold info for the summary pages.
 */
@Component
@Scope("prototype")
public class UnloadBean {

    private TreeMap<String, Map<String, Integer>> dataTableMap;

    private UnloadService unloadService;
    private UnloadIndexingService unloadIndexingService;
    private String tableName;
    private String date;
    private String entityID;
    private DownloadFileService downloadFileService;

    private String sortBy;
    private String downloadFileName;
    private String fileType;
    private String request;
    // relative to web root
    private String fileName;
    private boolean useCurrentFiles;
    private boolean currentDate;


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDownloadFileName() {
        return downloadFileName;
    }

    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
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

    /**
     * Retrieve the list of dates that are older than the database data,
     * unless user has root-level access
     *
     * @return map of download date strings
     */
    public Map<String, String> getDownloadDateList() {
        LinkedHashMap<String, String> dateList = new LinkedHashMap<String, String>();
        List<String> dates = null;
        if (Person.isCurrentSecurityUserRoot())
            dates = downloadFileService.getAllArchiveDateStrings();
        else
            dates = downloadFileService.getDataMatchingUnloadedDateStrings();
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

    public Date getArchiveDate() {
        return downloadFileService.getArchiveDate(date);
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

    public void setDownloadFileService(DownloadFileService downloadFileService) {
        this.downloadFileService = downloadFileService;
    }

    public DownloadFileService getDownloadFileService() {
        return downloadFileService;
    }

    public List<String> getAllDownloadFiles() {
        return downloadFileService.getDownloadFileNames(date);
    }

    private SortBy getSorting() {
        if (StringUtils.isEmpty(sortBy))
            return SortBy.DEFAULT;
        return SortBy.getSortBy(sortBy);
    }

    public List<DownloadFileInfo> getOfficialDownloadInfoFiles() throws IOException {
        List<DownloadFileInfo> downloadFileInfo = downloadFileService.getOfficialDownloadFileInfo(date);
        return getSortedDownloadFileList(downloadFileInfo);
    }

    public List<DownloadFileInfo> getUnofficialDownloadInfoFiles() throws IOException {
        List<DownloadFileInfo> downloadFileInfo = downloadFileService.getUnofficialDownloadFileInfo(date);
        return getSortedDownloadFileList(downloadFileInfo);
    }

    private List<DownloadFileInfo> getSortedDownloadFileList(List<DownloadFileInfo> downloadFileInfo) {
        switch (getSorting()) {
            case SIZE:
                Collections.sort(downloadFileInfo, new FileSizeComparator());
                break;
            case FILE_NAME:
                Collections.sort(downloadFileInfo);
                break;
            case CATEGORY:
                Collections.sort(downloadFileInfo, new CategoryComparator());
                break;
            default:
                Collections.sort(downloadFileInfo, new CategoryComparator());
                // popularity sorting
//                Collections.sort(downloadFileInfo, new DefaultComparator(downloadFileService,date));
        }
        return downloadFileInfo;
    }

    public List<FileInfo> getUnusedDownloadInfoFiles() throws IOException {
        return downloadFileService.getUnusedDownloadFileInfo(date);
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUseCurrentFiles() {
        return useCurrentFiles;
    }

    public void setUseCurrentFiles(boolean useCurrentFiles) {
        this.useCurrentFiles = useCurrentFiles;
    }

    public boolean isCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(boolean currentDate) {
        this.currentDate = currentDate;
    }
}

enum SortBy {
    SIZE, FILE_NAME, CATEGORY, DEFAULT;

    public static SortBy getSortBy(String name) {
        if (name == null)
            return FILE_NAME;
        for (SortBy sort : values())
            if (sort.name().equals(name))
                return sort;
        return FILE_NAME;
    }
}

class FileSizeComparator implements Comparator<FileInfo> {

    @Override
    public int compare(FileInfo o1, FileInfo o2) {
        if (o1.getSize() == o2.getSize())
            return 0;
        return o1.getSize() > o2.getSize() ? -1 : +1;
    }

}

class DefaultComparator implements Comparator<DownloadFileInfo> {

    private DownloadFileService downloadFileService;
    private String date;

    DefaultComparator(DownloadFileService downloadFileService, String date) {
        this.downloadFileService = downloadFileService;
        this.date = date;
    }

    @Override
    public int compare(DownloadFileInfo o1, DownloadFileInfo o2) {
        return DownloadFileEntry.compare(o1.getDownloadFile(), o2.getDownloadFile(), downloadFileService.getCategoriesByDate(date), downloadFileService.getDownloadFileInfo(date));
    }
}

class CategoryComparator implements Comparator<DownloadFileInfo> {

    @Override
    public int compare(DownloadFileInfo o1, DownloadFileInfo o2) {
        String category = o1.getDownloadFile().getCategory();
        String category1 = o2.getDownloadFile().getCategory();
        if (category.equalsIgnoreCase(category1))
            return o1.getDownloadFile().getName().compareToIgnoreCase(o2.getDownloadFile().getName());
        return category.compareToIgnoreCase(category1);
    }
}

