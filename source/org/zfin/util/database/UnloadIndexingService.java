package org.zfin.util.database;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.database.presentation.Table;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Go through all unload files of a given database (typically production) and find a given string in one or more tables.
 * It will return the first appearance of a string and when it disappeared by chronologically inspecting the unload files.
 * It uses a class Grep which is marginally changed to our purposes as it uses the Java optimized nio library to speedy
 * reads of files.
 */
@Service
public class UnloadIndexingService {

    private static Logger LOG = LogManager.getLogger(UnloadIndexingService.class);

    private String unloadDirectory = "/research/zunloads/databases/production";
    private List<Table> tableNames;
    private int numberOfUnloadFilesToIndex;

    public UnloadIndexingService() {
        this.unloadDirectory = unloadDirectory;
    }

    // index single table up-to-date
    public UnloadIndexingService(String unloadDirectory) throws IOException {
        if (unloadDirectory != null)
            this.unloadDirectory = unloadDirectory;
    }

    private static List<Table> getTableEntityPattern(String tablesString) {
        String[] tableNames = tablesString.split(",");
        List<Table> tables = new ArrayList<Table>(tableNames.length);
        for (String tableName : tableNames)
            tables.add(Table.getEntityTableByTableName(tableName));
        return tables;
    }

    public void runUpdate(String tableName) {
        Table table = Table.getEntityTableByTableName(tableName);
        tableNames = new ArrayList<Table>(1);
        tableNames.add(table);
//        retrieveUnindexedDates(table);
    }

    private File getTableDirectory(String tableName, File unloadDirectory) {
        return new File(unloadDirectory, tableName.toLowerCase());
    }

    private List<File> unloadFiles;
    private int numberOfUnloadFiles;

    public void checkUnloadDirectory() {
        numberOfUnloadFiles = getLatestNumberUnloadFiles();
        File unloadDir = new File(unloadDirectory);
        File[] files = unloadDir.listFiles();
        unloadFiles = Arrays.asList(files);
        Collections.sort(unloadFiles);

        List<File> cleanedUpFiles = new ArrayList<File>();
        for (File file : unloadFiles) {
            String name = file.getName();
            boolean lastCharacterIsNumber = Character.isDigit(name.charAt(name.length() - 1));
            if (name.startsWith("20") && lastCharacterIsNumber)
                //if (file.listFiles().length > 100)
                cleanedUpFiles.add(file);
        }
        unloadFiles = cleanedUpFiles;
    }

    private int getLatestNumberUnloadFiles() {
        File unloadDir = new File(unloadDirectory);
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
        if (unloadFiles == null)
            checkUnloadDirectory();
        return unloadFiles;
    }

    public String getLatestUnloadDate() {
        // if the current number of files found in the unload directory is greater than
        // the number stored here then update the file list
        if (getLatestNumberUnloadFiles() > numberOfUnloadFiles)
            getUnloadFiles();
        return unloadFiles.get(unloadFiles.size() - 1).getName();
    }
}

