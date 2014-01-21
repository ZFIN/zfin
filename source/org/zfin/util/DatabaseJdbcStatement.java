package org.zfin.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Single DB query instruction that will be issued through JDBC connection.
 */
public class DatabaseJdbcStatement implements SqlQueryKeywords {

    public static final String BEGIN_WORK = "BEGIN WORK";
    public static final String COMMIT_WORK = "COMMIT WORK";
    public static final String ROLLBACK_WORK = "ROLLBACK WORK";
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
    public static final String SINGLE_LOAD = "SINGLE-LOAD";
    public static final String SEMICOLON = ";";

    public String comment;
    private boolean unload;
    private boolean trace;
    private boolean load;
    private boolean singleLoad;
    private boolean echo;
    public static final String UNLOAD = "UNLOAD";
    public static final String TO = "TO";
    public static final String ECHO = "!ECHO";
    public static final String DEBUG = "DEBUG";
    public static final String TEST = "TEST";
    public static final String TRACE = "TRACE";

    private String booleanOperator;
    private int comparisonValue;
    private String errorMessage;
    private boolean debugStatement;
    private boolean subquery;
    private DatabaseJdbcStatement subQueryStatement;
    private DatabaseJdbcStatement parentStatement;

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
        if (statementStart.toUpperCase().startsWith(LOAD)) {
            checkIfLoadStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(SINGLE_LOAD)) {
            checkIfLoadStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(UNLOAD)) {
            checkIfUnloadStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(ECHO)) {
            checkIfEchoStatement();
        }
        if (query != null && statementStart.toUpperCase().startsWith(TEST)) {
            parseTestLine();
        }
        if (query != null && statementStart.toUpperCase().startsWith(TRACE)) {
            parseTrace();
        }
    }

    private void parseTrace() {
        trace = true;
        String[] tokens = query.toString().split("'");
        if (tokens.length == 3)
            comment = tokens[1];
        // remove line from query string
        query.delete(0, query.length() - 1);
    }

    private void parseTestLine() {
        int startComparison = query.indexOf("(");
        int endComparison = query.indexOf(")");
        String testLine = query.substring(startComparison + 1, endComparison);
        String[] tokens = testLine.split(" ");
        // first element is the value variable
        if (tokens.length != 3)
            throw new RuntimeException("Need exactly three elements for comparison");
        dataKey = tokens[0];
        booleanOperator = tokens[1].trim();
        comparisonValue = Integer.valueOf(tokens[2].trim());
        int startMessage = query.indexOf("'");
        String message = query.substring(startMessage + 1);
        int endQuote = message.indexOf("'");
        message = message.substring(0, endQuote);
        errorMessage = message;
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
        comment = query.toString();
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
            if (indexOfQueryStart > -1)
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
        if (token[0].toUpperCase().equals(LOAD))
            load = true;
        if (token[0].toUpperCase().equals(SINGLE_LOAD))
            singleLoad = true;
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

    public boolean isTrace() {
        return trace;
    }

    public boolean isTest() {
        return (query != null && query.toString().toUpperCase().startsWith(TEST));
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

    public boolean isSingleLoadStatement() {
        return singleLoad;
    }

    public boolean isInformixWorkStatement() {
        if (query == null)
            return false;
        String queryString = query.toString().trim().toUpperCase();
        return queryString.startsWith(BEGIN_WORK) || queryString.startsWith(COMMIT_WORK) || queryString.startsWith(ROLLBACK_WORK);
    }

    public String getDataKey() {
        return dataKey;
    }

    public String getLocationInfo() {
        StringBuilder buffer = new StringBuilder(10);
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
    public DatabaseJdbcStatement completeInsertStatement(int numberOfColumns) {
        DatabaseJdbcStatement modifiedStatement = new DatabaseJdbcStatement(scriptFile);
        modifiedStatement.load = load;
        modifiedStatement.comment = comment;
        modifiedStatement.startLine = startLine;
        modifiedStatement.endLine = endLine;
        modifiedStatement.query = new StringBuffer(query);
        StringBuffer modifiedQuery = modifiedStatement.query;
        int indexOfSemicolon = modifiedQuery.toString().indexOf(SEMICOLON);
        modifiedQuery.deleteCharAt(indexOfSemicolon);
        modifiedQuery.append("values (");
        for (int index = 0; index < numberOfColumns; index++) {
            modifiedQuery.append("?");
            if (index < numberOfColumns - 1)
                modifiedQuery.append(",");
        }
        modifiedQuery.append(")");
        modifiedQuery.append(SEMICOLON);
        return modifiedStatement;
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

    public boolean isSelectStatement() {
        return unload || isReadOnlyStatement() || (!isComment() && isTrace());
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

    public boolean isTestTrue(int value) {
        if (booleanOperator.equals(">"))
            return value > comparisonValue;
        if (booleanOperator.equals("<"))
            return value < comparisonValue;
        if (booleanOperator.equals("="))
            return value == comparisonValue;
        return false;
    }

    public String getErrorMessage(int value) {
        return StringUtils.replace(errorMessage, "$x", "" + value);
    }

    public boolean isComment() {
        return StringUtils.isEmpty(query.toString().trim());
    }

    public String getComment() {
        return comment;
    }

    public DatabaseJdbcStatement getDebugDeleteStatement() {
        if (!isDeleteStatement())
            return null;
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement(scriptFile);
        statement.startLine = startLine;
        statement.endLine = endLine;
        statement.debugStatement = true;
        statement.load = load;
        statement.comment = DELETE + " " + FROM + " " + getDeleteTable().toUpperCase();
        statement.query = new StringBuffer(SELECT + " " + STAR + " " + FROM + " " + getDeleteTable());
        statement.parentStatement = this;
        return statement;
    }

    public DatabaseJdbcStatement getDebugStatement() {
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement(scriptFile);
        statement.startLine = startLine;
        statement.endLine = endLine;
        statement.debugStatement = true;
        statement.load = load;
        // strip off insert statement -> comment
        if (isInsertStatement()) {
            if (isLoadStatement()) {
                statement.comment = "Load records from file / memory into " + getInsertTable().toUpperCase();
                statement.query = new StringBuffer(getQuery().replace("insert into", SELECT + " " + STAR + " " + FROM));
            } else {
                int startOfSelect = getQuery().indexOf(SELECT.toLowerCase());
                statement.comment = getQuery().substring(0, startOfSelect).trim();
                statement.query = new StringBuffer(getQuery().substring(startOfSelect));
            }
        } else if (isDeleteStatement()) {
            statement.comment = "DELETE from " + getDeleteTable().toUpperCase();
            statement.query = new StringBuffer(getQuery().replace("delete", SELECT + " " + STAR));
        }
        statement.parentStatement = this;
        return statement;
    }

    private String getInsertTable() {
        return getTableName(INTO);
    }

    private String getDeleteTable() {
        return getTableName(FROM);
    }

    private String getTableName(String keywordBeforeTable) {
        String[] tokens = query.toString().split(" ");
        boolean foundFromToken = false;
        for (String token : tokens) {
            if (foundFromToken)
                return token;
            if (token.toUpperCase().equals(keywordBeforeTable))
                foundFromToken = true;
        }
        return null;
    }

    public boolean isInsertStatement() {
        return getQuery().toLowerCase().startsWith(INSERT.toLowerCase());
    }

    public boolean isDeleteStatement() {
        return getQuery().toLowerCase().startsWith(DELETE.toLowerCase());
    }

    public boolean isDebugStatement() {
        return debugStatement;
    }

    public DatabaseJdbcStatement getParentStatement() {
        return parentStatement;
    }

    public boolean isDynamicQuery() {
        return subQueryStatement != null;
    }

    public DatabaseJdbcStatement getSubQueryStatement() {
        return subQueryStatement;
    }

    public void setSubQueryStatement(DatabaseJdbcStatement subQueryStatement) {
        this.subQueryStatement = subQueryStatement;
    }

    public boolean isSubquery() {
        return subquery;
    }

    public void setSubquery(boolean subquery) {
        this.subquery = subquery;
    }

    private String subQuery;


    public String getSubQuery() {
        return subQuery;
    }

    public void bindVariables(List<String> resultRecord) {
        int index = 0;
        subQuery = query.toString();
        for (String value : resultRecord) {
            subQuery = subQuery.replace("$" + index, value);
            index++;
        }
    }
}
