package org.zfin.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.zfin.database.DatabaseService;
import org.zfin.database.presentation.Table;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.log4jFileOption;
import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.webrootDirectory;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Inspect Informix Lucene index. Find out how many max clauses are needed.
 */
public class InformixLuceneIndexInspection extends AbstractScriptWrapper {

    private Table table;
    private String columnName;
    private Map<String, Integer> wordFrequency;
    private Set<String> usedTokens;
    private List<String> values;

    public InformixLuceneIndexInspection(Table table, String columnName) {
        super();
        this.table = table;
        this.columnName = columnName;
        initStatistics();
    }

    /**
     * Retrieve the single letter character with the most token matches.
     *
     * @return letter
     */
    public String getMaxTokenQueryString() {
        initStatistics();
        if (wordFrequency == null)
            return "NULL";
        Map.Entry<String, Integer> first = sortedWordFrequency.first();
        return first.getKey();
    }

    /**
     * Retrieve the number of tokens in the bts index matching the single letter character.
     *
     * @return number of matches
     */
    public int getMaxTokenQueryStringCount() {
        initStatistics();
        if (wordFrequency == null)
            return 0;
        Map.Entry<String, Integer> first = sortedWordFrequency.first();
        return first.getValue();
    }

    private void initStatistics() {
        if (wordFrequency != null)
            return;
        retrieveValues();
        tokenizeValues();
        createStringMap(1);
        sortedWordFrequency = TreeMapUtil.entriesSortedByValues(wordFrequency);
    }

    private SortedSet<Map.Entry<String, Integer>> sortedWordFrequency;

    public void printSingleLetterStatistics() {
        initStatistics();
        System.out.println("Number of Words counted by white spaces: " + numberOfWords);
        System.out.println("Number of tokens: " + usedTokens.size());
        System.out.println("Number of duplicates: " + numberOfDuplicateTokens);
        for (Map.Entry<String, Integer> entry : sortedWordFrequency) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public SortedSet<Map.Entry<String, Integer>> getSortedMapStatistics() {
        return sortedWordFrequency;
    }

    public Set<String> getUsedTokens() {
        return usedTokens;
    }

    /**
     * Check all one and two-letter words how many matching tokens there are.
     *
     * @param maximumNumberOfLetters letters
     */
    private void createStringMap(int maximumNumberOfLetters) {
        char firstLetter = 'a';
        char lastLetter = 'z';
        char letter = firstLetter;
        wordFrequency = new TreeMap<String, Integer>();
        for (int index = firstLetter; index <= lastLetter; index++) {
            int sum = 0;
            String letterString = String.valueOf(letter);
            for (String value : usedTokens) {
                if (value.startsWith(letterString)) {
                    sum++;
                }
            }
            wordFrequency.put(letterString, sum);
            letter++;
        }

        firstLetter = 'A';
        lastLetter = 'Z';
        letter = firstLetter;
        for (int index = firstLetter; index <= lastLetter; index++) {
            int sum = 0;
            String letterString = String.valueOf(letter);
            for (String value : usedTokens) {
                if (value.startsWith(letterString)) {
                    sum++;
                }
            }
            wordFrequency.put(letterString, sum);
            letter++;
        }
    }

    int numberOfWords = 0;
    int numberOfDuplicateTokens = 0;

    private void tokenizeValues() {
        usedTokens = new TreeSet<String>();

        for (String value : values) {
            numberOfWords += value.split(" ").length + 1;
            WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(new StringReader(value));
            Token token;
            boolean foundMoreToken = true;
            while (foundMoreToken) {
                try {
                    token = tokenizer.next();
                    if (token == null)
                        foundMoreToken = false;
                    else if (!usedTokens.add(token.termText()))
                        numberOfDuplicateTokens++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void retrieveValues() {
        DatabaseJdbcStatement statement = DatabaseService.createJdbcStatement(table, columnName);
        List<List<String>> result = getInfrastructureRepository().executeNativeQuery(statement);
        if (result == null)
            return;
        values = new ArrayList<String>(result.size());
        for (List<String> columns : result) {
            String value = columns.get(0);
            values.add(value);
        }
    }

    /**
     * Used from an ant target:
     * -tableName <>
     * -columnName <>
     *
     * @param arguments arguments
     */
    public static void main(String[] arguments) {
        Logger.getRootLogger().setLevel(Level.ERROR);
        //LOG.info("Start Informix BTS index: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        if (commandLine.getOptionValue(log4jFileOption.getOpt()) != null)
            initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String tableName = commandLine.getOptionValue(tableNameOption.getOpt());
        String columnName = commandLine.getOptionValue(columnNameOption.getOpt());
        String propertyDirectory = commandLine.getOptionValue(webrootDirectory.getOpt());

        Table table = Table.getEntityTableByTableName(tableName);
        InformixLuceneIndexInspection inspection = new InformixLuceneIndexInspection(table, columnName, propertyDirectory);
        System.out.println("Letter with the most token matches");
        System.out.println(inspection.getMaxTokenQueryString() + ": " + inspection.getMaxTokenQueryStringCount());
    }

    /**
     * Only used from the internal main() method.
     *
     * @param table             table
     * @param columnName        bts column
     * @param propertyDirectory zfin.properties directory
     */
    public InformixLuceneIndexInspection(Table table, String columnName, String propertyDirectory) {
        super();
        this.table = table;
        this.columnName = columnName;
        if (propertyDirectory == null)
            initAll();
        else
            initAll(propertyDirectory + "/WEB-INF/zfin.properties");
    }


    private static Logger LOG = Logger.getLogger(InformixLuceneIndexInspection.class);

    public static final Option tableNameOption = OptionBuilder.withArgName("tableName").hasArg().withDescription("the table name").create("tableName");
    public static final Option columnNameOption = OptionBuilder.withArgName("columnName").hasArg().withDescription("column of bts index belonging to the table").create("columnName");

    static {
        options.addOption(log4jFileOption);
        options.addOption(tableNameOption);
        options.addOption(columnNameOption);
    }

}

