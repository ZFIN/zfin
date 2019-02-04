package org.zfin.util.downloads;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.database.UnloadInfo;
import org.zfin.properties.ZfinProperties;
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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;


/**
 * Service class facilitating the download files.
 */
@Service
public class DownloadFileService extends ArchiveService {

    private static Logger LOG = Logger.getLogger(DownloadFileService.class);

    // store the date the cache was last updated
    // every time a download request comes in we check the current date against this value.
    private Calendar lastUpdatedCacheDate = new GregorianCalendar(2000, 1, 1);

    // registry file name. One per download archive
    public final String DOWNLOAD_REGISTRY = "download-registry.xml";

    public DownloadFileService() {
        // hard-coded location of the root download archive
        // one archive per instance
        rootArchiveDirectory = ZfinPropertiesEnum.DOWNLOAD_DIRECTORY.toString();
        startDownloadsWatcherService();
    }

    private void startDownloadsWatcherService() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // run a task within a separate thread of this thread pool
        executor.submit(() -> {
            Path dir = ZfinProperties.getDownloadReloadStatusDirectory();
            File file = dir.toFile();
            if (!file.exists())
                file.mkdir();
            try {
                new WatchDownloadRefresh(this, dir).processEvents();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Download re-fresh thread started: "+Thread.currentThread().getName());
        });

    }

    // index single table up-to-date
    public DownloadFileService(String unloadDirectory) {
        if (unloadDirectory != null)
            rootArchiveDirectory = unloadDirectory;
    }

    public String getDownloadDirectory() {
        return getRootArchiveDirectory();
    }

    public boolean isDownloadArchiveExists() {
        File unloadDir = new File(rootArchiveDirectory);
        return unloadDir.exists();
    }

    public String getFirstDownloadDate() {
        // if the current number of files found in the unload directory is greater than
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() > numberOfArchiveDirectories)
            getArchiveDirectories();
        return archiveDirectories.get(archiveDirectories.size() - 1).getName();
    }

    public String getLatestDownloadDate() {
        // if the current number of files found in the unload directory is greater than
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() > numberOfArchiveDirectories)
            getArchiveDirectories();
        return archiveDirectories.get(0).getName();
    }

    public List<String> getDownloadFileNames(String date) {
        File directory = null;
        for (File file : archiveDirectories) {
            if (file.getName().equals(date))
                directory = file;
        }
        if (directory == null)
            return null;
        return Arrays.asList(directory.list());
    }

    // <date,List<FileInfo>>
    private Map<String, List<DownloadFileInfo>> allDownloadFileInfoList = new HashMap<>();
    private Map<String, List<DownloadFileInfo>> downloadFileInfoList = new HashMap<>();
    private Map<String, List<DownloadFileInfo>> unofficialDownloadFileInfoList = new HashMap<>();
    private Map<String, List<FileInfo>> unusedDownloadFileInfoMap = new HashMap<>();
    // cache the different categories
    private Map<String, List<DownloadCategory>> registryMap = new HashMap<>();

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
        for (File file : archiveDirectories) {
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
        for (File file : archiveDirectories) {
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
        downloads = new ArrayList<>();
        List<FileInfo> unusedDownloadFiles = new ArrayList<>();
        List<DownloadFileInfo> unofficialDownloads = new ArrayList<>();
        DownloadFileRegistry registry;
        try {
            // read registry file
            registry = readRegistryFile(directory, DOWNLOAD_REGISTRY);
            for (String fileName : Objects.requireNonNull(directory.list())) {
                File dFile = new File(directory, fileName);
                //
                DownloadFileEntry downloadFile = registry.getDownloadFileEntryByName(fileName);
                // ignore files that are not registered.
                if (downloadFile == null) {
                    if (!fileName.equalsIgnoreCase("download-registry.xml")) {
                        if (!dFile.isDirectory())
                            unusedDownloadFiles.add(FileUtil.getFileInfo(dFile));
                    }
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
        List<DownloadFileInfo> allDownloadFiles = new ArrayList<>(downloads);
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

    public boolean isValidArchiveFound() {
        Date unloadDate = getInfrastructureRepository().getUnloadInfo().getDate();
        List<Date> allUnloadDates = getAllArchiveDates();
        for (Date date : allUnloadDates) {
            if (unloadDate.after(date)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isValidArchive(File archiveDirectory) {
        return true;
    }

    /**
     * Checks which index files matches most closely the current data.
     *
     * @return date matching most recently with current date.
     */
    public String getMatchingIndexDirectory() {
        return super.getMatchingIndexDirectory();
    }

    @Override
    public void updateCache() {
        archiveDateList = new ArrayList<>();
        archiveDirectories = null;
        downloadFileInfoList = new HashMap<>();
        // forces to reread download files and create
        // file collection and
        // date collection
        super.updateCache();
    }

    public List<DownloadFileInfo> getUnofficialDownloadFileInfo(String date) {
        File directory = null;
        for (File file : archiveDirectories) {
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
        UnloadInfo unloadInfo = getInfrastructureRepository().getUnloadInfo();
        Date unloadDate = unloadInfo.getDate();
        List<Date> allUnloadDates = getAllArchiveDates();
        List<String> dateList = new ArrayList<>(allUnloadDates.size());
        for (Date date : allUnloadDates) {
            if (unloadDate.after(date)) {
                dateList.add(getFormattedDate(date));
            }
        }
        return dateList;
    }
}

