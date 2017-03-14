package org.zfin.uniquery;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.database.LuceneQueryService;
import org.zfin.util.downloads.ArchiveService;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Go through all unload files of a given database (typically production) and find a given string in one or more tables.
 * It will return the first appearance of a string and when it disappeared by chronologically inspecting the unload files.
 * It uses a class Grep which is marginally changed to our purposes as it uses the Java optimized nio library to speedy
 * reads of files.
 */
@Service
public class SiteSearchIndexService extends ArchiveService {

    private static Logger LOG = Logger.getLogger(SiteSearchIndexService.class);
    private LuceneQueryService luceneQueryService;

    public SiteSearchIndexService() {
        rootArchiveDirectory = ZfinPropertiesEnum.INDEXER_DIRECTORY.value();
        readAllArchiveDirectories();
        luceneQueryService = new LuceneQueryService(getFullPathMatchingIndexDirectory());
        HibernateUtil.closeSession();
    }

    public SiteSearchIndexService(String indexDirectory) throws IOException {
        if (indexDirectory != null)
            rootArchiveDirectory = indexDirectory;
        readAllArchiveDirectories();
        luceneQueryService = new LuceneQueryService(getFullPathMatchingIndexDirectory());
    }

    public void checkUnloadDirectory() {
        try {
            numberOfArchiveDirectories = getLatestNumberUnloadFiles();
        } catch (Exception e) {
            LOG.error(e);
            return;
        }
        File unloadDir = new File(rootArchiveDirectory);
        File[] files = unloadDir.listFiles();
        if (files == null) {
            LOG.error("No files found in archive directory: " + rootArchiveDirectory);
            return;
        }

        archiveDirectories = Arrays.asList(files);
        Collections.sort(archiveDirectories);
        Collections.reverse(archiveDirectories);
        List<File> cleanedUpFiles = new ArrayList<File>();
        for (File file : archiveDirectories) {
            String name = file.getName();
            boolean lastCharacterIsNumber = Character.isDigit(name.charAt(name.length() - 1));
            if (name.startsWith("20") && lastCharacterIsNumber)
                //if (file.listFiles().length > 100)
                cleanedUpFiles.add(file);
        }
        archiveDirectories = cleanedUpFiles;
    }

    public int getNumberOfDocuments() {
        return luceneQueryService.getNumberOfDocuments();
    }

    /**
     * We cannot keep track of when an indexer was created for production as we generate the sites search index from almost.
     * I.e. we cannot update database_info to hold the correct date.
     * To allow running the indexer on dev sites we have the following logic:
     * If the property INDEXER_DIRECTORY points to production, i.e use production indexer for site searches
     * then check the unload date and obtain the most recent matching dated archive.
     * If the property INDEXER_DIRECTORY points to a different site than production
     * use the latest dated archive that is available. When you run the indexer generation on a dev site you most likely
     * want to use that index for the UI. When pointing to production then you are 'only' reusing the production
     * index and that should match with the data, i.e. with the unload date di_date_unloaded.
     *
     * @return most current archive directory to be used.
     */
    public List<File> getUnloadFiles() {
        return getUnloadFiles(false);
    }

    public List<File> getUnloadFiles(boolean forceReload) {
        if (archiveDirectories == null || forceReload)
            checkUnloadDirectory();
        return archiveDirectories;
    }

    @Override
    public String getFullPathMatchingIndexDirectory() {
        String matchingIndexDirectory = getMatchingIndexDirectory();
        if (matchingIndexDirectory == null) {
            LOG.error("Could not find matching index directories: ");
            return null;
        }
        return getFullPath(matchingIndexDirectory);
    }

    public String getLatestUnloadDate() {
        // if the current number of files found in the unload directory is different from
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() != numberOfArchiveDirectories)
            getUnloadFiles(true);
        return archiveDirectories.get(0).getName();
    }

    /**
     * Checks which index files matches most closely the current data.
     * <p/>
     * Index consists of three files:
     * 1) <xxx>.cfs
     * 2) <yyy>.gen
     * 3) segments_<iii>
     * Note that this is specific to the lucene API we are using
     *
     * @return boolean
     */
    @Override
    protected boolean isValidArchive(File archiveDirectory) {
        if (archiveDirectory == null)
            return false;
        File[] indexFiles = archiveDirectory.listFiles();
        if (indexFiles == null || indexFiles.length == 0)
            return false;
        indexFiles = archiveDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("gen") || name.endsWith("cfs");
            }
        });
        return indexFiles != null && indexFiles.length >= 2;
    }

    @Override
    public void updateCache() {
        if (cacheIsBeingUpdated)
            return;
        cacheIsBeingUpdated = true;
        super.updateCache();
        // point lucene service to new directory
        if (luceneQueryService != null)
            luceneQueryService.changeIndex(getFullPathMatchingIndexDirectory());
        cacheIsBeingUpdated = false;
    }

    public LuceneQueryService getLuceneQueryService() {
        return luceneQueryService;
    }
}

