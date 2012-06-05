package org.zfin.util.downloads;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.database.UnloadInfo;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileInfo;
import org.zfin.util.FileUtil;
import org.zfin.util.downloads.jaxb.DownloadCategory;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;
import org.zfin.util.downloads.jaxb.DownloadFileRegistry;
import org.zfin.util.downloads.presentation.DownloadFileInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;


/**
 * Service class facilitating the download files.
 * <p/>
 * Note: Since it is such a hassle to inject the instance of this class into a quartz job the cache is update as follows:
 * the time the last update was done is stored and whenever all download files are requested it is checked
 * if the current time and date is more than 20 hours past the last caching.
 */
@Service
public class DownloadFileService {

    private static Logger LOG = Logger.getLogger(DownloadFileService.class);

    // hard-coded location of the download archive
    // one archive per instance
    private String downloadDirectory = "/research/zunloads/download-files/" + ZfinPropertiesEnum.DB_NAME.toString();
    // store the date the cache was last updated
    // every time a download request comes in we check the current date against this value.
    private Calendar lastUpdatedCacheDate = new GregorianCalendar(2000, 1, 1);

    // registry file name. One per download archive
    private String downloadRegistry = "download-registry.xml";

    public DownloadFileService() {
    }

    // index single table up-to-date
    public DownloadFileService(String unloadDirectory) throws IOException {
        if (unloadDirectory != null)
            this.downloadDirectory = unloadDirectory;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    private List<File> downloadFileDirectories;
    private int numberOfUnloadFiles;

    private void checkUnloadDirectory() {
        numberOfUnloadFiles = getLatestNumberUnloadFiles();
        File unloadDir = new File(downloadDirectory);
        File[] files = unloadDir.listFiles();
        if (files == null)
            return;
        downloadFileDirectories = Arrays.asList(files);
        Collections.sort(downloadFileDirectories);

        List<File> cleanedUpFiles = new ArrayList<File>();
        for (File file : downloadFileDirectories) {
            String name = file.getName();
            boolean lastCharacterIsNumber = Character.isDigit(name.charAt(name.length() - 1));
            if (name.startsWith("20") && lastCharacterIsNumber)
                //if (file.listFiles().length > 100)
                cleanedUpFiles.add(file);
        }
        downloadFileDirectories = cleanedUpFiles;
    }

    private int getLatestNumberUnloadFiles() {
        File unloadDir = new File(downloadDirectory);
        if (!unloadDir.exists())
            throw new RuntimeException("Unload directory not found: " + unloadDir.getAbsolutePath());
        LOG.info("Found " + unloadDir.list().length + " backups.");
        return unloadDir.list().length;
    }

    /**
     * The list of files is in ascending order of time.
     *
     * @return list of unload files.
     */
    public List<File> getDownloadFileDirectories() {
        if (downloadFileDirectories == null)
            checkUnloadDirectory();
        return downloadFileDirectories;
    }

    public String getFirstDownloadDate() {
        // if the current number of files found in the unload directory is greater than
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() > numberOfUnloadFiles)
            getDownloadFileDirectories();
        return downloadFileDirectories.get(0).getName();
    }

    public String getLatestDownloadDate() {
        // if the current number of files found in the unload directory is greater than
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() > numberOfUnloadFiles)
            getDownloadFileDirectories();
        return downloadFileDirectories.get(downloadFileDirectories.size() - 1).getName();
    }

    public List<String> getDownloadFileNames(String date) {
        File directory = null;
        for (File file : downloadFileDirectories) {
            if (file.getName().equals(date))
                directory = file;
        }
        if (directory == null)
            return null;
        List<File> downloads = new ArrayList<File>();
        return (List<String>) Arrays.asList(directory.list());
    }

    // <date,List<FileInfo>>
    private Map<String, List<DownloadFileInfo>> allDownloadFileInfoList = new HashMap<String, List<DownloadFileInfo>>();
    private Map<String, List<DownloadFileInfo>> downloadFileInfoList = new HashMap<String, List<DownloadFileInfo>>();
    private Map<String, List<DownloadFileInfo>> unofficialDownloadFileInfoList = new HashMap<String, List<DownloadFileInfo>>();
    private Map<String, List<FileInfo>> unusedDownloadFileInfoMap = new HashMap<String, List<FileInfo>>();
    // cache the different categories
    private Map<String, List<DownloadCategory>> registryMap = new HashMap<String, List<DownloadCategory>>();

    public List<DownloadCategory> getCategoriesByDate(String date) {
        return registryMap.get(date);
    }

    /**
     * Retrieve list of official DownloadFileInfo objects for a given date.
     * The date format is: yyyy.mm.dd
     *
     * @param date download date
     * @return list of DownloadFileInfo objects
     */
    public List<DownloadFileInfo> getOfficialDownloadFileInfo(String date) {
        File directory = null;
        for (File file : getDownloadFileDirectories()) {
            if (file.getName().equals(date))
                directory = file;
        }
        if (directory == null)
            return null;
        List<DownloadFileInfo> downloads = downloadFileInfoList.get(date);
        if (downloads != null)
            return downloads;
        categorizeDownloadFiles(date, directory);
        return downloadFileInfoList.get(date);
    }

    /**
     * Retrieve list of official and unofficial DownloadFileInfo objects for a given date.
     * The date format is: yyyy.mm.dd
     *
     * @param date download date
     * @return list of DownloadFileInfo objects
     */
    public List<DownloadFileInfo> getDownloadFileInfo(String date) {
        File directory = null;
        for (File file : getDownloadFileDirectories()) {
            if (file.getName().equals(date))
                directory = file;
        }
        if (directory == null)
            return null;
        List<DownloadFileInfo> downloads = allDownloadFileInfoList.get(date);
        if (downloads != null)
            return downloads;
        categorizeDownloadFiles(date, directory);
        return allDownloadFileInfoList.get(date);
    }

    private void categorizeDownloadFiles(String date, File directory) {
        List<DownloadFileInfo> downloads;
        downloads = new ArrayList<DownloadFileInfo>();
        List<FileInfo> unusedDownloadFiles = new ArrayList<FileInfo>();
        List<DownloadFileInfo> unofficialDownloads = new ArrayList<DownloadFileInfo>();
        DownloadFileRegistry registry = null;
        try {
            // read registry file
            registry = readRegistryFile(directory, downloadRegistry);
            for (String fileName : directory.list()) {
                File dFile = new File(directory, fileName);
                //
                DownloadFileEntry downloadFile = registry.getDownloadFileEntryByName(fileName);
                // ignore files that are not registered.
                if (downloadFile == null) {
                    unusedDownloadFiles.add(FileUtil.getFileInfo(dFile));
                    continue;
                }
                if (downloadFile.isPrivateDownload())
                    unofficialDownloads.add(DownloadFileInfo.getFileInfo(dFile, downloadFile));
                else
                    downloads.add(DownloadFileInfo.getFileInfo(dFile, downloadFile));
            }
        } catch (IOException e) {
            LOG.error("Error while retrieving file info", e);
            throw new RuntimeException(e);
        }
        registryMap.put(date, registry.getDownloadCategoryList());
        Collections.sort(downloads);
        downloadFileInfoList.put(date, downloads);
        Collections.sort(unofficialDownloads);
        unofficialDownloadFileInfoList.put(date, unofficialDownloads);
        unusedDownloadFileInfoMap.put(date, unusedDownloadFiles);
        List<DownloadFileInfo> allDownloadFiles = new ArrayList<DownloadFileInfo>(downloads);
        allDownloadFiles.addAll(unofficialDownloads);
        allDownloadFileInfoList.put(date, allDownloadFiles);
    }

    private DownloadFileRegistry readRegistryFile(File directory, String downloadRegistry) throws FileNotFoundException {
        File registry = new File(directory, downloadRegistry);
        try {
            // create JAXB context and instantiate marshaller
            JAXBContext context = JAXBContext.newInstance(DownloadFileRegistry.class);
            Unmarshaller um = context.createUnmarshaller();
            return (DownloadFileRegistry) um.unmarshal(new FileReader(registry));
        } catch (JAXBException e) {
            String message = "Error while reading download registration file";
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
    }


    public File getFile(String fileName, String date) {
        List<DownloadFileInfo> files = getDownloadFileInfo(date);
        for (DownloadFileInfo file : files)
            if (file.getDownloadFile().getFullFileName().equals(fileName))
                return file.getFile();
        // check unused files

        for (FileInfo file : unusedDownloadFileInfoMap.get(date))
            if (file.getName().equals(fileName))
                return file.getFile();
        return null;
    }

    public String getRelativePath(File file) {
        StringBuilder buffer = new StringBuilder("/data-transfer");
        String[] directories = file.getAbsolutePath().split("/");
        boolean isRelativePath = false;
        for (String dir : directories) {
            if (dir.equals("production")) {
                isRelativePath = true;
                continue;
            }
            if (!isRelativePath)
                continue;
            buffer.append("/");
            buffer.append(dir);
        }
        return buffer.toString();
    }

    private List<String> dateList = new ArrayList<String>();

    public List<String> getAllUnloadedDateStrings() {
        if (dateList.size() > 0)
            return dateList;
        if (downloadFileDirectories == null)
            checkUnloadDirectory();
        for (File file : downloadFileDirectories)
            dateList.add(file.getName());
        return dateList;
    }

    public List<FileInfo> getUnusedDownloadFileInfo(String date) {
        if (date == null)
            return null;
        return unusedDownloadFileInfoMap.get(date);
    }

    public DownloadFileEntry getDownloadFile(String fileName, String date) {
        List<DownloadFileInfo> files = getDownloadFileInfo(date);
        for (DownloadFileInfo file : files)
            if (file.getDownloadFile().getFullFileName().equals(fileName))
                return file.getDownloadFile();

        return null;
    }

    public String getMostRecentMatchingDate() {
        UnloadInfo unloadInfo = getInfrastructureRepository().getUnloadDate();
        Date unloadDate = unloadInfo.getDate();
        List<Date> allUnloadDates = getAllUnloadedDate();
        for (Date date : allUnloadDates) {
            if (unloadDate.after(date)) {
                return getDateString(date);
            }
        }
        throw new RuntimeException("No download file found that for " + unloadDate.toString() + " or earlier");
    }

    private String getDateString(Date date) {
        DateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
        return dt.format(date);
    }

    /**
     * Sorted list of unload files in descending order, i.e. most current date first.
     *
     * @return list of download file dates
     */
    private List<Date> getAllUnloadedDate() {
        checkCacheStale();
        List<String> allUnloadedDateStrings = getAllUnloadedDateStrings();
        List<Date> dates = new ArrayList<Date>(allUnloadedDateStrings.size());
        for (String dataString : allUnloadedDateStrings) {
            dates.add(getArchiveDate(dataString));
        }
        Collections.sort(dates, new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return -o1.compareTo(o2);
            }
        });
        return dates;
    }

    // checks if the last date the cache was updated is more than one day old.
    // If so update cache and set lastCacheUpdate to now.
    private void checkCacheStale() {
        Calendar todayCalMinus20 = new GregorianCalendar();
        todayCalMinus20.setTime(new Date());
        todayCalMinus20.add(Calendar.HOUR, -20);
        if (lastUpdatedCacheDate.before(todayCalMinus20)) {
            updateCache();
            Calendar now = new GregorianCalendar();
            now.setTime(new Date());
            lastUpdatedCacheDate = now;
        }
    }

    public Date getArchiveDate(String dataString) {
        DateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
        try {
            return dt.parse(dataString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFormattedDate(Date date) {
        StringBuilder builder = new StringBuilder(12);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        builder.append(cal.get(Calendar.YEAR));
        builder.append(".");
        // months integers start with '0'
        builder.append(getPaddedDateNumber(cal.get(Calendar.MONTH) + 1));
        builder.append(".");
        builder.append(getPaddedDateNumber(cal.get(Calendar.DAY_OF_MONTH)));
        return builder.toString();
    }

    private String getPaddedDateNumber(int number) {
        if (number < 10)
            return "0" + number;
        else
            return number + "";
    }

    public void updateCache() {
        dateList = new ArrayList<String>();
        downloadFileDirectories = null;
        downloadFileInfoList = new HashMap<String, List<DownloadFileInfo>>();
        // forces to reread download files and create
        // file collection and
        // date collection
        getAllUnloadedDateStrings();
    }

    public List<DownloadFileInfo> getUnofficialDownloadFileInfo(String date) {
        File directory = null;
        for (File file : getDownloadFileDirectories()) {
            if (file.getName().equals(date))
                directory = file;
        }
        if (directory == null)
            return null;
        List<DownloadFileInfo> downloads = unofficialDownloadFileInfoList.get(date);
        if (downloads != null)
            return downloads;
        categorizeDownloadFiles(date, directory);
        return unofficialDownloadFileInfoList.get(date);
    }

    /**
     * Retrieve a list of download dates that are older than the current data info.
     *
     * @return list of download dates
     */
    public List<String> getDataMatchingUnloadedDateStrings() {
        UnloadInfo unloadInfo = getInfrastructureRepository().getUnloadDate();
        Date unloadDate = unloadInfo.getDate();
        List<Date> allUnloadDates = getAllUnloadedDate();
        List<String> dateList = new ArrayList<String>(allUnloadDates.size());
        for (Date date : allUnloadDates) {
            if (unloadDate.after(date)) {
                dateList.add(getFormattedDate(date));
            }
        }
        return dateList;
    }
}

