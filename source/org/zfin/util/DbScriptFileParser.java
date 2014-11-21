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
    public static final String ECHO = "! echo";
    public static final String LOOP = "loop";
    public static final String SUB_QUERY = "subquery";
    public static final String EXISTS = "exists";
    public static final String NOT_EXISTS = "not exists";
    public static final String LIST_SUBQUERY = "list subquery";
    public static final String LIST_QUERY = "list query";

    public DbScriptFileParser(File dbScriptFile) {
        if (dbScriptFile == null)
            throw new NullPointerException("No file provided");
        this.dbScriptFile = dbScriptFile;
    }

    public List<DatabaseJdbcStatement> parseFile() {
        List<DatabaseJdbcStatement> queries = new ArrayList<>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(dbScriptFile);
            LineNumberReader reader = new LineNumberReader(fileReader);
            String line;
            DatabaseJdbcStatement query = new DatabaseJdbcStatement(dbScriptFile.getAbsolutePath());
            boolean queryClosed = false;
            boolean inSubQuery = false;
            DatabaseJdbcStatement subQuery = null;
            while ((line = reader.readLine()) != null) {
                // ignore empty lines or lines with comments or output ! echo
                if (line.trim().length() == 0 || line.trim().startsWith(COMMENTS)|| line.trim().startsWith(ECHO))
                    continue;
                if (line.trim().length() == 0 || line.trim().startsWith(SUB_QUERY)) {
                    inSubQuery = true;
                    subQuery = new DatabaseJdbcStatement(dbScriptFile.getAbsolutePath());
                    subQuery.setSubquery(true);
                    query.setSubQueryStatement(subQuery);
                    continue;
                }
                if (inSubQuery) {
                    if (line.trim().length() == 0 || line.trim().startsWith(EXISTS)) {
                        subQuery.setExistsSubquery(true);
                        continue;
                    } else if (line.trim().length() == 0 || line.trim().startsWith(NOT_EXISTS)) {
                        subQuery.setExistsSubquery(false);
                        continue;
                    }
                    else if (line.trim().length() == 0 || line.trim().startsWith(LIST_SUBQUERY)) {
                        subQuery.setListSubquery(true);
                        continue;
                    } else if (line.trim().length() == 0 || line.trim().startsWith(LIST_QUERY)) {
                        subQuery.setListSubquery(false);
                        continue;
                    }
                    subQuery.addQueryPart(line, reader.getLineNumber());
                } else
                    query.addQueryPart(line, reader.getLineNumber());

                if (line.trim().endsWith(QUERY_ENDS_SEMICOLON)) {
                    if (!inSubQuery){
                        queries.add(query);
                        query.finish();
                    }
                    query = new DatabaseJdbcStatement(dbScriptFile.getAbsolutePath());
                    queryClosed = true;
                }
            }
            if (!queryClosed) {
                queries.add(query);
            }
        } catch (FileNotFoundException e) {
            // should not happen as this is caught in the constructor.
        } catch (IOException ioe) {
            LOG.error(ioe);
        } finally {
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
