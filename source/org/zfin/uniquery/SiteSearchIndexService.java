package org.zfin.uniquery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.zfin.database.UnloadInfo;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.database.LuceneQueryService;
import org.zfin.util.downloads.DownloadFilesException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;


/**
 * Go through all unload files of a given database (typically production) and find a given string in one or more tables.
 * It will return the first appearance of a string and when it disappeared by chronologically inspecting the unload files.
 * It uses a class Grep which is marginally changed to our purposes as it uses the Java optimized nio library to speedy
 * reads of files.
 */
@Service
public class SiteSearchIndexService {

    private static Logger LOG = Logger.getLogger(SiteSearchIndexService.class);

    private String indexDirectory = "/research/zunloads/indexes/production";
    private Analyzer analyzer = new KeywordAnalyzer();

    private LuceneQueryService luceneQueryService;

    public SiteSearchIndexService() {
        checkUnloadDirectory();
    }

    // index single table up-to-date
    public SiteSearchIndexService(String indexDirectory) throws IOException {
        if (indexDirectory != null)
            this.indexDirectory = indexDirectory;
    }

    private List<File> unloadFiles;
    private int numberOfUnloadFiles;

    public void checkUnloadDirectory() {
        try {
            numberOfUnloadFiles = getLatestNumberUnloadFiles();
        } catch (Exception e) {
            LOG.error(e);
            return;
        }
        File unloadDir = new File(indexDirectory);
        File[] files = unloadDir.listFiles();
        unloadFiles = Arrays.asList(files);
        Collections.sort(unloadFiles);
        Collections.reverse(unloadFiles);
        List<File> cleanedUpFiles = new ArrayList<File>();
        for (File file : unloadFiles) {
            String name = file.getName();
            boolean lastCharacterIsNumber = Character.isDigit(name.charAt(name.length() - 1));
            if (name.startsWith("20") && lastCharacterIsNumber)
                //if (file.listFiles().length > 100)
                cleanedUpFiles.add(file);
        }
        unloadFiles = cleanedUpFiles;
        if (luceneQueryService == null)
            luceneQueryService = new LuceneQueryService(unloadFiles.get(0).getAbsolutePath());
    }

    private int getLatestNumberUnloadFiles() {
        File unloadDir = new File(indexDirectory);
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
    public List<File> getUnloadFiles() {
        return getUnloadFiles(false);
    }

    public List<File> getUnloadFiles(boolean forceReload) {
        if (unloadFiles == null || forceReload)
            checkUnloadDirectory();
        return unloadFiles;
    }

    public String getLatestUnloadDate() {
        // if the current number of files found in the unload directory is different from
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() != numberOfUnloadFiles)
            getUnloadFiles(true);
        return unloadFiles.get(0).getName();
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    /**
     * Checks which index files matches most closely the current data.
     *
     * @return
     */
    public String getMatchingIndexDirectory() {
        if (CollectionUtils.isEmpty(unloadFiles))
            throw new NullPointerException("No index files found");
        Date unloadDate = getUnloadDate().getDate();
        for (File file : unloadFiles) {
            String date = file.getName();

            if (unloadDate.after(getDateString(date))) {
                return date;
            }
        }
        throw new DownloadFilesException("No download file found that for " + unloadDate.toString() + " or earlier");
    }

    public UnloadInfo getUnloadDate() {
        return getInfrastructureRepository().getUnloadDate();
    }

    public String getFullPathToIndex() {
        return indexDirectory + "/" + getMatchingIndexDirectory();
    }

    public Date getDateString(String dataString) {
        DateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
        try {
            return dt.parse(dataString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfDocuments() {
        return luceneQueryService.getNumberOfDocuments();
    }


}

