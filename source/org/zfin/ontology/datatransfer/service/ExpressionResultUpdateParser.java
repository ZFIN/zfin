package org.zfin.ontology.datatransfer.service;

import org.apache.log4j.Logger;
import org.zfin.util.TermStageUpdateFileParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a expression_Result update file
 * xpatID startStageName endStageName TermOboID
 */
public class ExpressionResultUpdateParser {

    private static final Logger LOG = Logger.getLogger(TermStageUpdateFileParser.class);

    private File expressionResultUpdateFile;
    public static final String COMMENTS = "--";

    public ExpressionResultUpdateParser(File updateFile) {
        if (updateFile == null)
            throw new NullPointerException("No file provided");
        if (!updateFile.exists())
            throw new NullPointerException("Could not find file: " + updateFile.getAbsoluteFile());
        this.expressionResultUpdateFile = updateFile;

    }

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
                ExpressionResultUpdateRecord record = new ExpressionResultUpdateRecord();
                record.setExpressionResultID(Long.parseLong(tokens[0]));
                record.setStartStageID(tokens[1]);
                record.setEndStageID(tokens[2]);
                record.setSuperTermOboID(tokens[3]);
                if (tokens.length == 5)
                    record.setSubTermOboID(tokens[4]);
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

}
