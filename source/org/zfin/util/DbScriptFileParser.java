package org.zfin.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a DB script and create a collection of instructions.
 */
public class DbScriptFileParser {

    private static final Logger LOG = Logger.getLogger(DbScriptFileParser.class);

    private File dbScriptFile;
    public static final String QUERY_ENDS_SEMICOLON = ";";
    public static final String COMMENTS = "--";

    public DbScriptFileParser(File dbScriptFile) {
        if (dbScriptFile == null)
            throw new NullPointerException("No file provided");
        this.dbScriptFile = dbScriptFile;
    }

    public List<DatabaseJdbcStatement> parseFile() {
        List<DatabaseJdbcStatement> queries = new ArrayList<DatabaseJdbcStatement>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(dbScriptFile);
            LineNumberReader reader = new LineNumberReader(fileReader);
            String line;
            DatabaseJdbcStatement query = new DatabaseJdbcStatement(dbScriptFile.getAbsolutePath());
            while ((line = reader.readLine()) != null) {
                // ignore empty lines or lines with comments
                if (line.trim().length() == 0 || line.trim().startsWith(COMMENTS))
                    continue;
                query.addQueryPart(line, reader.getLineNumber());
                if (line.trim().endsWith(QUERY_ENDS_SEMICOLON)) {
                    queries.add(query);
                    query = new DatabaseJdbcStatement(dbScriptFile.getAbsolutePath());
                }
            }
        } catch (FileNotFoundException e) {
            // should not happen as this is caught in the constructor.
        } catch (IOException ioe) {
            LOG.error(ioe);
        }
        finally {
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        return queries;
    }

}
