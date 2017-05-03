package org.zfin.database;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TraceFileService {

    private final Logger LOG = Logger.getLogger(TraceFileService.class);

    private FileWriter traceFileWriter;

    public TraceFileService(String fileName) {
        openTraceFile(fileName);
    }

    void openTraceFile(String name) {
        String traceFileName = "trace-" + FileUtil.getFileNameFromPath(name) + ".log";
        File file = new File("logs");
        file = new File(file, traceFileName);
        try {
            LOG.info("Opening trace file: " + file.getAbsolutePath());
            traceFileWriter = new FileWriter(file);
        } catch (IOException e) {
            LOG.error("Could not open file " + file.getAbsolutePath());
        }
    }

    public void closeTraceFile() {
        if (traceFileWriter == null)
            return;

        try {
            traceFileWriter.close();
        } catch (IOException e) {
            LOG.error("Could not close file " + traceFileWriter.toString());
        }
    }


    public void writeToFile(String string) {
        if (traceFileWriter == null)
            return;
        try {
            traceFileWriter.write(string);
            traceFileWriter.flush();
        } catch (IOException e) {
            LOG.error("Could not write to trace file: ");
        }
    }

    public void writeToTraceFile(List<List<String>> rows) {
        if (CollectionUtils.isEmpty(rows))
            return;
        int maxNumber = 10;
        int index = 0;
        for (List<String> row : rows) {
            if (index >= maxNumber) {
                writeToFile("... " + rows.size() + " total records");
                break;
            }
            StringBuilder builder = new StringBuilder();
            for (String column : row) {
                builder.append(column);
                builder.append("|");
            }
            builder.append("\n");
            writeToFile(builder.toString());
            index++;
        }
        writeToFile("\n");
    }


    public void writeToTraceFile(String comment) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(comment))
            return;
        writeToFile(comment);
        writeToFile("\n");
    }

    private void writeToTraceFileIndendented(String comment) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(comment))
            return;
        comment = comment.replace("\n", "\n          ");
        writeToFile(comment);
        writeToFile("\n");
    }

    public void writeToTraceFile(DatabaseJdbcStatement statement, List<List<String>> rows) {
        writeToTraceFile(statement, rows, true);
    }

    public void writeToTraceFile(DatabaseJdbcStatement statement, List<List<String>> rows, boolean showLocation) {
        if (statement.isDebugStatement()) {
            int numberOfRows = 0;
            if (CollectionUtils.isNotEmpty(rows))
                numberOfRows = rows.size();
            if (showLocation) {
                writeToTraceFile(statement.getLocationInfo() + " " + FileUtil.getFileNameFromPath(statement.getScriptFile()));
                writeToTraceFile(statement.getComment());
                writeToTraceFile("(" + numberOfRows + ")");
                if (statement.getParentStatement() != null)
                    writeToTraceFileIndendented(statement.getParentStatement().getHumanReadableQueryString());
            } else {
                writeToTraceFileIndendented(statement.getHumanReadableQueryString());
                writeToTraceFileIndendented(" No. of Records: " + numberOfRows);
            }
            if (numberOfRows == 0)
                writeToTraceFileIndendented("NONE");
            else {
                writeToTraceFile(rows);
            }
        }
    }



}
