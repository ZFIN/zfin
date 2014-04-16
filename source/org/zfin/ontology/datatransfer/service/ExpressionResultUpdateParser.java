package org.zfin.ontology.datatransfer.service;

import org.apache.log4j.Logger;
import org.zfin.util.TermStageUpdateFileParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse a expression_Result update file
 * xpatID startStageName endStageName TermOboID
 */
public class ExpressionResultUpdateParser {

    private static final Logger LOG = Logger.getLogger(TermStageUpdateFileParser.class);
    public static final String XPATRES_ID = "Xpatres ID";
    public static final String XPAT_START_STAGE = "Xpat Start Stage";
    public static final String XPAT_END_STAGE = "Xpat End Stage";
    public static final String TERM_OBO_ID = "REPLACEMENT TERM ID";
    public static final String SUB_TERM_OBO_ID = "Sub Term Obo ID";

    private File expressionResultUpdateFile;
    public static final String COMMENTS = "--";

    public ExpressionResultUpdateParser(File updateFile) {
        if (updateFile == null)
            throw new NullPointerException("No file provided");
        if (!updateFile.exists())
            throw new NullPointerException("Could not find file: " + updateFile.getAbsoluteFile());
        this.expressionResultUpdateFile = updateFile;

    }

    private Map<String, Integer> map = new HashMap<>(8);

    public List<ExpressionResultUpdateRecord> parseFile() {
        List<ExpressionResultUpdateRecord> queries = new ArrayList<>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(expressionResultUpdateFile);
            LineNumberReader reader = new LineNumberReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                int lineNumber = reader.getLineNumber();
                // ignore lines with comments
                if (line.trim().startsWith(COMMENTS) || line.trim().length() == 0)
                    continue;
                String[] tokens = line.split(",");
                if (tokens.length < 4)
                    continue;
                if (lineNumber == 1) {
                    initColumnAssociation(line);
                    if (isHeaderLine(line))
                        continue;
                }
                ExpressionResultUpdateRecord record = new ExpressionResultUpdateRecord();
                record.setExpressionResultID(tokens[map.get(XPATRES_ID)]);
                record.setStartStageID(tokens[map.get(XPAT_START_STAGE)]);
                record.setEndStageID(tokens[map.get(XPAT_END_STAGE)]);
                record.setSuperTermOboID(tokens[map.get(TERM_OBO_ID)]);
                if (tokens.length == 5)
                    record.setSubTermOboID(tokens[map.get(SUB_TERM_OBO_ID)]);
                queries.add(record);
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

    private void initColumnAssociation(String line) {
        if (isHeaderLine(line)) {
            String[] tokens = line.split(",");
            int index = 0;
            for (String token : tokens) {
                map.put(token, index++);
            }
        } else {
            map.put(XPATRES_ID, 0);
            map.put(XPAT_START_STAGE, 1);
            map.put(XPAT_END_STAGE, 2);
            map.put(TERM_OBO_ID, 3);
            map.put(SUB_TERM_OBO_ID, 4);
        }
    }

    private boolean isHeaderLine(String line) {
        return line.contains(XPATRES_ID) && line.contains(XPAT_START_STAGE) && line.contains(XPAT_END_STAGE)
                && line.contains(TERM_OBO_ID);
    }

}
