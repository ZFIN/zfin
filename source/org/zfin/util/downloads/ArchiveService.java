package org.zfin.util.downloads;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.database.UnloadInfo;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Service class facilitating archive access.
 */
public abstract class ArchiveService {

    private static Logger LOG = LogManager.getLogger(ArchiveService.class);
    public static final String DATE_PREFIX = "yyyy.MM.dd";

    // store the date the cache was last updated
    // every time a download request comes in we check the current date against this value.
    private Calendar lastUpdatedCacheDate = new GregorianCalendar(2000, 1, 1);

    // this is root directory in which archives of a given entity are kept for a given instance.
    protected String rootArchiveDirectory;
    // list of dated archive directories
    protected List<File> archiveDirectories;
    // archive Date string list
    protected List<String> archiveDateList = new ArrayList<>();

    // number of dated archive directories
    protected int numberOfArchiveDirectories;

    // read all dated archive directories that
    // are named according to the following date syntax: yyyy.mm.dd.n
    // should be read only at start up or when cache is updated.
    protected void readAllArchiveDirectories() {
        File rootArchiveDir = new File(rootArchiveDirectory);
        File[] archiveDirs = rootArchiveDir.listFiles();
        if (archiveDirs != null)
            archiveDirectories = Arrays.asList(archiveDirs);
        else
            archiveDirectories = new ArrayList<>();
        Collections.sort(archiveDirectories);
        Collections.reverse(archiveDirectories);

        List<File> relevantDirectories = new ArrayList<>();
        for (File file : archiveDirectories) {
            String archiveDirectoryName = file.getName();
            boolean lastCharacterIsNumber = Character.isDigit(archiveDirectoryName.charAt(archiveDirectoryName.length() - 1));
            if (archiveDirectoryName.startsWith("20") && lastCharacterIsNumber)
                if (isValidArchive(file))
                    relevantDirectories.add(file);
        }
        archiveDirectories = relevantDirectories;
        numberOfArchiveDirectories = archiveDirectories.size();
    }

    /**
     * This method defines if a given directory is a valid archive directory.
     * Override this method accordingly.
     *
     * @return boolean
     */
    protected abstract boolean isValidArchive(File archiveDirectory);

    /**
     * Goes out and reads the filesystem again (not using the cached values).
     *
     * @return number of directories found right now.
     */
    protected int getLatestNumberUnloadFiles() {
        File unloadDir = new File(rootArchiveDirectory);
        if (!unloadDir.exists())
            throw new RuntimeException("Root archive directory not found: " + unloadDir.getAbsolutePath());
        LOG.info("Found " + unloadDir.list().length + " backups.");
        return unloadDir.list().length;
    }

    public Date getArchiveDate(String dataString) {
        DateFormat dt = new SimpleDateFormat(DATE_PREFIX);
        try {
            return dt.parse(dataString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The list of files is in ascending order of time.
     *
     * @return list of unload files.
     */
    public List<File> getArchiveDirectories() {
        if (archiveDirectories == null)
            readAllArchiveDirectories();
        return archiveDirectories;
    }

    public String getRootArchiveDirectory() {
        return rootArchiveDirectory;
    }

    public List<String> getAllArchiveDateStrings() {
        List<String> unloadDates;
        if (CollectionUtils.isNotEmpty(archiveDirectories)) {
            unloadDates = getArchiveDateList();
            return unloadDates;
        }
        if (archiveDirectories == null)
            readAllArchiveDirectories();
        return getArchiveDateList();
    }

    private List<String> getArchiveDateList() {
        checkCacheStatus();
        List<String> unloadDates;
        unloadDates = new ArrayList<>(archiveDirectories.size());
        for (File file : archiveDirectories)
            unloadDates.add(file.getName());
        return unloadDates;
    }

    public String getDateString(Date date) {
        DateFormat dt = new SimpleDateFormat(DATE_PREFIX);
        return dt.format(date);
    }


    protected String getPaddedDateNumber(int number) {
        if (number < 10)
            return "0" + number;
        else
            return number + "";
    }

    public String getFormattedDate(Date date) {
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


    /**
     * Checks which index files matches most closely the current data.
     *
     * @return date matching most recently with current date.
     */
    public String getMatchingIndexDirectory() {
        Date unloadDate = getInfrastructureRepository().getUnloadInfo().getDate();
        return getMatchingIndexDirectory(unloadDate);
    }

    /**
     * Checks which index files matches most closely the current data.
     *
     * @return date matching most recently with current date.
     */
    public String getFullPathMatchingIndexDirectory() {
        Date unloadDate = getInfrastructureRepository().getUnloadInfo().getDate();
        String matchingIndexDirectory = getMatchingIndexDirectory(unloadDate);
        return getFullPath(matchingIndexDirectory);
    }

    protected String getFullPath(String datedArchiveDirectory) {
        File indexDirectory = new File(rootArchiveDirectory, datedArchiveDirectory);
        return indexDirectory.getAbsolutePath();
    }

    public String getMatchingIndexDirectory(Date mostRecentEventDate) {
        checkCacheStatus();
        List<Date> allUnloadDates = getAllArchiveDates();
        if (CollectionUtils.isEmpty(archiveDirectories)) {
            LOG.error("No archive files found");
            return null;
        }


        for (Date date : allUnloadDates) {
/*
            // Logic to get the most recent archive date before a given date
            // need to enable this when we go into AB/C land
            if (mostRecentEventDate.after(date)) {
                return getDateString(date);
            }
*/
        }
        if (CollectionUtils.isEmpty(allUnloadDates)) {
            String message = "No archive directory found for " + mostRecentEventDate.toString() + " or earlier";
            LOG.error(message);
            return null;
        }
        //return the latest date archive
        return getDateString(allUnloadDates.get(0));
    }

    /**
     * Sorted list of unload files in descending order, i.e. most current date first.
     *
     * @return list of download file dates
     */
    protected List<Date> getAllArchiveDates() {
        checkCacheStatus();
        List<String> allUnloadedDateStrings = getAllArchiveDateStrings();
        List<Date> dates = new ArrayList<>(allUnloadedDateStrings.size());
        for (String dataString : allUnloadedDateStrings) {
            dates.add(getArchiveDate(dataString));
        }
        dates.sort((o1, o2) -> -o1.compareTo(o2));
        return dates;
    }

    // checks if the last date the cache was updated is more than one day old.
    // If so update cache and set lastCacheUpdate to now.
    protected void checkCacheStatus() {
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

    protected boolean cacheIsBeingUpdated;

    /**
     * Should be implemented by sub classes.
     * Make sure to call this as well...
     */
    protected void updateCache() {
        readAllArchiveDirectories();
    }

    public UnloadInfo getUnloadInfo() {
        return getInfrastructureRepository().getUnloadInfo();
    }

    public String getFutureArchive() {
        Date unloadDate = getUnloadInfo().getDate();
        List<Date> allUnloadDates = getAllArchiveDates();
        if (CollectionUtils.isEmpty(archiveDirectories))
            LOG.error("No archive files found");

        for (Date date : allUnloadDates) {
            if (unloadDate.before(date)) {
                return getDateString(date);
            }
        }
        return null;
    }

    public boolean isFutureArchivesAvailable() {
        return getFutureArchive() != null;
    }

    public Date getDateString(String dataString) {
        DateFormat dt = new SimpleDateFormat(DATE_PREFIX);
        try {
            return dt.parse(dataString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFullPathToIndex() {
        return rootArchiveDirectory + "/" + getMatchingIndexDirectory();
    }

}

