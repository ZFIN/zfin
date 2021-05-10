package org.zfin.util;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a DB script and create a collection of instructions.
 */
public class TermStageUpdateFileParser {

    private static final Logger LOG = LogManager.getLogger(TermStageUpdateFileParser.class);

    private File termStageUpdateFile;
    public static final String COMMENTS = "--";

    public TermStageUpdateFileParser(File updateFile) {
        if (updateFile == null)
            throw new NullPointerException("No file provided");
        if (!updateFile.exists())
            throw new NullPointerException("Could not find file: " + updateFile.getAbsoluteFile());
        this.termStageUpdateFile = updateFile;
    }

    public List<TermStageSplitStatement> parseFile() {
        List<TermStageSplitStatement> queries = new ArrayList<TermStageSplitStatement>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(termStageUpdateFile);
            LineNumberReader reader = new LineNumberReader(fileReader);
            String line;
            String filePath = termStageUpdateFile.getAbsolutePath();
            TermStageSplitStatement splitStatement = new TermStageSplitStatement(filePath);
            boolean newStatementStarted = true;
            while ((line = reader.readLine()) != null) {
                int lineNumber = reader.getLineNumber();
                // ignore lines with comments
                if (line.trim().startsWith(COMMENTS))
                    continue;
                // empty line means the end of a split statement
                if (line.trim().length() == 0) {
                    if (splitStatement.isValid()) {
                        queries.add(splitStatement);
                        splitStatement = new TermStageSplitStatement(filePath);
                        newStatementStarted = true;
                    }
                    continue;
                }
                if (line.trim().length() > 5) {
                    if (newStatementStarted)
                        splitStatement = new TermStageSplitStatement(filePath);
                    splitStatement.addTermStageUpdateLine(line, lineNumber);
                    newStatementStarted = false;
                }
            }
            // add last statement if it has not been added yet.
            if (splitStatement.isValid()) {
                queries.add(splitStatement);
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
