package org.zfin.util;

import org.apache.commons.lang.StringUtils;

/**
 * Single DB query instruction that will be issued through JDBC connection.
 */
public class DatabaseJdbcStatement implements SqlQueryKeywords {

    private StringBuffer query = new StringBuffer(50);
    private String scriptFile;
    private int startLine = -1;
    private int endLine;

    // this key string refers to a map in which the data for loading data are stored.
    // syntax is: LOAD <key>:
    // before the actual insert statement, e.g. the following line
    // LOAD syntypedefs_header: insert into table tmp_syndef (namespace, type, def, scoper, syntypedefs) values (?,?,?,?,?);
    // syntypedefs_header is the key for a collection of an array of five values.
    private String dataKey;
    public static final String LOAD = "LOAD";
    public static final String SEMICOLON = ";";

    private boolean unload;
    private boolean load;
    private boolean echo;
    public static final String UNLOAD = "UNLOAD";
    public static final String TO = "TO";
    public static final String ECHO = "!ECHO";
    public static final String DEBUG = "DEBUG";

    public DatabaseJdbcStatement() {
    }

    public DatabaseJdbcStatement(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public void addQueryPart(String part, int lineNumber) {
        query.append(part);
        query.append(" ");
        if (startLine == -1)
            startLine = lineNumber;
        endLine = lineNumber;
        parseLoadingInstruction();
    }

    private void parseLoadingInstruction() {
        String statementStart = query.toString().trim();
        if (query != null && statementStart.toUpperCase().startsWith(LOAD)) {
            checkIfLoadStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(UNLOAD)) {
            checkIfUnloadStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(ECHO)) {
            checkIfEchoStatement();
        }
    }

    private void checkIfEchoStatement() {
        String[] token = query.toString().split(" ");

        echo = true;
        // echo string
        if (token.length < 2) {
            query = new StringBuffer();
        } else {
            // remove echo directive part from query
            query.delete(0, ECHO.length() + 1);
        }
    }

    private void checkIfUnloadStatement() {
        String[] token = query.toString().split(" ");
        if (!token[1].toUpperCase().equals(TO))
            throw new RuntimeException("Incorrect syntax!");

        unload = true;
        // unload file = unload key
        dataKey = token[2].replace("'", "");
        if (token.length < 4) {
            query = new StringBuffer();
        } else {
            // remove unloading directive part from query
            int indexOfQueryStart = query.toString().indexOf("'");
            query.delete(0, indexOfQueryStart);
        }
    }

    private void checkIfLoadStatement() {
        String[] token = query.toString().split(" ");
        if (!token[1].toUpperCase().equals("FROM"))
            throw new RuntimeException("Incorrect syntax!");

        dataKey = token[2];
        if (token.length < 4) {
            query = new StringBuffer();
        } else {
            // remove loading directive part from query
            int indexOfQueryStart = query.toString().toUpperCase().indexOf(INSERT);
            query.delete(0, indexOfQueryStart);
        }
        load = true;
    }

    public void addQueryPart(String part) {
        addQueryPart(part, 1);
    }

    public String getQuery() {
        return query.toString().trim();
    }

    public boolean isReadOnlyStatement() {
        if (query == null)
            return false;
        String queryString = query.toString().trim().toUpperCase();
        return queryString.startsWith(SELECT);
    }

    public boolean isDebug() {
        return (dataKey != null && dataKey.toUpperCase().equals(DEBUG));
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public boolean isLoadStatement() {
        return load;
    }

    public String getDataKey() {
        return dataKey;
    }

    public String getLocationInfo() {
        StringBuffer buffer = new StringBuffer(10);
        buffer.append("[");
        buffer.append(startLine);
        buffer.append(",");
        buffer.append(endLine);
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Add values () statement to query based on the data being provided.
     *
     * @param numberOfColumns list of records
     */
    public void updateInsertStatement(int numberOfColumns) {
        int indexOfSemicolon = query.toString().indexOf(SEMICOLON);
        query.deleteCharAt(indexOfSemicolon);
        query.append("values (");
        for (int index = 0; index < numberOfColumns; index++) {
            query.append("?");
            if (index < numberOfColumns - 1)
                query.append(",");
        }
        query.append(")");
        query.append(SEMICOLON);

    }

    public String getTableName() {
        String[] tokens = query.toString().split(" ");
        boolean foundIntoString = false;
        for (String token : tokens) {
            if (foundIntoString)
                return token;
            if (token.trim().toUpperCase().startsWith(INTO))
                foundIntoString = true;
        }
        return null;
    }

    public boolean isUnloadStatement() {
        return unload;
    }

    public boolean isEcho() {
        return echo;
    }

    @Override
    public String toString() {
        return "DatabaseJdbcStatement{" +
                "query=" + query +
                '}';
    }

    public String getHumanReadableQueryString() {
        return SqlQueryUtil.getHumanReadableQueryString(query.toString());
    }

}
