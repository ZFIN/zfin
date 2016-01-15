package org.zfin.util.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.*;
import org.hibernate.Session;
import org.nocrala.tools.texttablefmt.Table;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateComparisonSessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.DatabaseJdbcStatement;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.log4jFileOption;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Compare a given list of tables in two running databases
 */
public class CompareDatabases extends AbstractScriptWrapper {

    private static Logger LOG;

    public static final Option databaseNameOption = OptionBuilder.withArgName("databaseNameOption").hasArg().withDescription("the name of the comparison comparisonDatabase ").create("databaseName");
    public static final Option tableNameOption = OptionBuilder.withArgName("tableNameOption").hasArg().withDescription("the table names to be checked").create("tableNames");

    static {
        options.addOption(databaseNameOption);
        options.addOption(tableNameOption);
        options.addOption(log4jFileOption);
    }

    private String comparisonDatabase;
    private String database;
    private String[] tableNames;

    public CompareDatabases(String database, String tableNames) {
        this.comparisonDatabase = database;
        this.tableNames = tableNames.split(",");
        LOG.info("Compare Databases: " + database);
    }

    public static void main(String[] arguments) {
//        Logger.getRootLogger().setLevel(Level.ERROR);
        LOG = Logger.getLogger("compare");
        LOG.setLevel(Level.INFO);
/*
        Logger.getLogger("org.hibernate").setLevel(Level.ERROR);
        Appender appender = new ConsoleAppender(new SimpleLayout());
        LOG.addAppender(appender);
*/
        LOG.info("Start Data Comparison Inspector: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        String optionValue = commandLine.getOptionValue(log4jFileOption.getOpt());
        if (optionValue == null)
            optionValue = "test/log4j.xml";
        //initializeLogger(optionValue);
        String database = commandLine.getOptionValue(databaseNameOption.getOpt());
        String tableNames = commandLine.getOptionValue(tableNameOption.getOpt());
        LOG.info("Compare tables: " + tableNames.toUpperCase() + " in " + database);

        CompareDatabases comparator = new CompareDatabases(database, tableNames);
        comparator.initAll();
        comparator.database = ZfinPropertiesEnum.DB_NAME.value();
        comparator.run();
    }

    private void run() {
        CronJobReport report = new CronJobReport("Check Entity Occurrence: ", cronJobUtil);
        report.start();
        initComparisonDatabase();
        compare();
        closeComparisonDatabase();
        report.finish();
        createReport();
        LOG.info(report.getDuration());
    }

    private Session session;

    private void initComparisonDatabase() {
        HibernateComparisonSessionFactory sessionCreator = new HibernateComparisonSessionFactory(comparisonDatabase, true);
        session = sessionCreator.getSession();
    }

    private void closeComparisonDatabase() {
        session.close();
    }

    private void compare() {
        for (String tableName : tableNames) {
            LOG.info("Comparison for table " + tableName.toUpperCase());
            DatabaseJdbcStatement statement = DatabaseService.createJdbcStatementAllSortedRecords(tableName);
            LOG.info(statement.getQuery());

            List<List<String>> leftResults = getLeftValues(statement);
            List<List<String>> rightResults = getRightValues(statement);
            printSummaryReport(leftResults, database, rightResults, comparisonDatabase, tableName);

            int numOfDifferences = compareResults(leftResults, rightResults);
            if (numOfDifferences > 0)
                LOG.info(numOfDifferences + " different rows found");
            else
                LOG.info("No differences in table " + tableName);

        }
    }

    private void printSummaryReport(List<List<String>> leftResults, String database, List<List<String>> rightResults, String comparisonDatabase, String tableName) {
        Table output = new Table(3);
        output.addCell("Database");
        output.addCell("Table Name");
        output.addCell("Number of Records");
        output.addCell(database);
        output.addCell(tableName);
        output.addCell("" + leftResults.size());
        output.addCell(comparisonDatabase);
        output.addCell(tableName);
        output.addCell("" + rightResults.size());
        LOG.info(NEWLINE + output.render());

    }
    
    private int compareResults(List<List<String>> leftResults, List<List<String>> rightResults) {
        List<String> leftRecords = getConcatenatedStringList(leftResults);
        List<String> rightRecords = getConcatenatedStringList(rightResults);
        List<String> leftOverLeftRecords = new ArrayList<String>(10);
        for (String left : leftRecords) {
            if (rightRecords.contains(left))
                rightRecords.remove(left);
            else
                leftOverLeftRecords.add(left);
        }
        printDifferentRecords(comparisonDatabase, rightRecords);
        printDifferentRecords(database, leftOverLeftRecords);
        return rightRecords.size() + leftOverLeftRecords.size();
    }

    private void printDifferentRecords(String database, List<String> records) {
        StringBuilder builder = new StringBuilder("Different Records for " + database + NEWLINE);
        for (String row : records) {
            builder.append(row);
            builder.append(NEWLINE);
        }
        LOG.info(builder.toString());
        //System.out.print(builder.toString());
    }

    private List<String> getConcatenatedStringList(List<List<String>> results) {
        List<String> concatenatedStringList = new ArrayList<String>(results.size());
        for (List<String> row : results) {
            String concatenatedRow = getConcatenatedString(row);
            concatenatedStringList.add(concatenatedRow);
        }
        return concatenatedStringList;
    }

    private String getConcatenatedString(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String name : strings) {
            builder.append(name);
            builder.append(" | ");
        }
        return builder.toString();
    }

    private List<List<String>> getLeftValues(DatabaseJdbcStatement statement) {
        List<List<String>> resultList = getInfrastructureRepository().executeNativeQuery(statement);
        return resultList;
    }

    private void printSummary(String database, String tableName, List<List<String>> resultList) {
        Table output = new Table(3);
        output.addCell("Database");
        output.addCell("Table Name");
        output.addCell("Number of Records");
        output.addCell(database);
        output.addCell(tableName);
        output.addCell("" + resultList.size());
        LOG.info(NEWLINE + output.render());
    }

    private List<List<String>> getRightValues(DatabaseJdbcStatement statement) {
        List<List<String>> resultList = getInfrastructureRepository().executeNativeQuery(statement, session);
        printSummary(comparisonDatabase, statement.getTableName(), resultList);
        return resultList;
    }

    private void createReport() {
        Table output = new Table(4);
        output.addCell("Occurrence");
        output.addCell("Date");
        output.addCell("Table");
        output.addCell("Difference");

        int index = -1;
        for (Difference match : occurrences) {
            index++;
            if (index % 2 == 0) {
                output.addCell("First");
            } else {
                output.addCell("Last");
            }
            output.addCell(match.getDate());
            output.addCell(match.getTableName());
            if (match.getMatchedLine().length() > 60)
                output.addCell(match.getMatchedLine().substring(0, 60) + "...");
            else
                output.addCell(match.getMatchedLine());
        }
        LOG.info("Report:\r" + output.render());
    }

    private List<Difference> occurrences = new ArrayList<Difference>(4);

    private void firstOccurrence(String entityId) {

        boolean firstOccurrenceFound = false;
        Difference lastMatch = null;
        for (File file : unloadFiles) {
            LOG.info("Check unload: " + file.getName());
            Grep grep = new Grep(entityId, getSearchFiles(file));
            if (grep.foundMatch()) {
                if (!firstOccurrenceFound) {
                    Difference match = new Difference(grep.getLineMatched(), grep.getMatchingFile().getName(), grep.getLineNumber(), file.getName());
                    occurrences.add(match);
                    firstOccurrenceFound = true;
                } else {
                    lastMatch = new Difference(grep.getLineMatched(), grep.getMatchingFile().getName(), grep.getLineNumber(), file.getName());
                }
            } else {
                if (firstOccurrenceFound) {
                    occurrences.add(lastMatch);
                    firstOccurrenceFound = false;
                }
            }
        }
    }

    private List<File> getSearchFiles(File file) {
        List<File> files = new ArrayList<File>();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                File tableFile = new File(file, tableName);
                if (!tableFile.exists())
                    LOG.info("Table " + tableName + " not found in unload directory " + file.getName());
                else
                    files.add(tableFile);
            }
        }
        return files;
    }

    private List<File> unloadFiles;

}

class Difference {

    private String matchedLine;
    private String tableName;
    private int lineNumber;
    private String date;

    Difference(String matchedLine, String tableName, int lineNumber, String date) {
        this.matchedLine = matchedLine;
        this.tableName = tableName;
        this.lineNumber = lineNumber;
        this.date = date;
    }

    public String getMatchedLine() {
        return matchedLine;
    }

    public String getTableName() {
        return tableName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDate() {
        return date;
    }
}