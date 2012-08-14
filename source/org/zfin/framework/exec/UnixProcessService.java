package org.zfin.framework.exec;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.zfin.util.ZfinStringUtils;

import java.io.ByteArrayOutputStream;
import java.util.*;


/**
 * Convenience class that lists certain unix processes.
 */
public class UnixProcessService {

    public static List<UnixProcess> getProcesses() {
        CommandLine commandLine = new CommandLine("ps");
        commandLine.addArgument("-ef");
        String output = getProcessOutput(commandLine);
        return createListOfProcesses(output);
    }

    private static List<UnixProcess> createListOfProcesses(String output) {
        if (output == null)
            return null;
        String[] lines = output.split("\n");
        if (lines == null)
            return null;
        Map<String, LineSyntax> syntaxMap = new HashMap<String, LineSyntax>(8);
//        if (CollectionUtils.isEmpty(ProcessEfColumns.getLeftRightJoiningColumns()))
        createSyntax(syntaxMap, output);
        int index = 0;

        List<UnixProcess> processes = new ArrayList<UnixProcess>(lines.length - 1);
        for (String line : lines) {
            if (index++ == 0) {
                continue;
            }
            UnixProcess process = new UnixProcess();
            for (ProcessEfColumns column : ProcessEfColumns.getSortedColumns()) {
                LineSyntax lineSyntax = syntaxMap.get(column.name());
                String value;
                if (column.isLastColumn())
                    value = line.substring(lineSyntax.getStart());
                else
                    value = line.substring(lineSyntax.getStart(), lineSyntax.getStop() + 1);
                process.addColumn(column, value);
            }
            processes.add(process);
        }
        return processes;
    }

    private static void createSyntax(Map<String, LineSyntax> syntaxMap, String output) {
        String[] lines = output.split("\n");
        String headerLine = lines[0];
        ProcessEfColumns.validateHeaderLine(headerLine);

        List<ProcessEfColumns> columns = ProcessEfColumns.getSortedColumns();
        LineSyntax previousSyntax = null;
        int index = 0;
        for (ProcessEfColumns column : columns) {
            String name = " " + column.name() + " ";
            int indexOf = headerLine.indexOf(name);
            if (column.ordinal() == 0) {
                LineSyntax lineSyntax = new LineSyntax(0, indexOf + column.name().length(), column.name());
                syntaxMap.put(column.name(), lineSyntax);
                previousSyntax = lineSyntax;
            } else if (column.ordinal() == columns.size() - 1) {
                int startIndex = previousSyntax.getStop() + 2;
                LineSyntax lineSyntax = new LineSyntax(startIndex, column.name());
                syntaxMap.put(column.name(), lineSyntax);
            } else {
                ProcessEfColumns nextColumn = columns.get(index + 1);
                int startIndex = indexOf + 1;
                if (column.isRightAdjusted()) {
                    if (columns.get(index - 1).isRightAdjusted())
                        startIndex = previousSyntax.getStop() + 2;
                    else
                        startIndex = -1;
                }
                int stopIndex = indexOf + column.name().length();
                if (column.isLeftAdjusted()) {
                    if (nextColumn.isLeftAdjusted())
                        stopIndex = headerLine.indexOf(nextColumn.name()) - 1;
                    else
                        stopIndex = -1;
                }
                LineSyntax lineSyntax = new LineSyntax(startIndex, stopIndex, column.name());
                syntaxMap.put(column.name(), lineSyntax);
                previousSyntax = lineSyntax;
            }
            index++;
        }
        checkForUndeterminedColumns(syntaxMap, lines);
        printSyntax(syntaxMap);
    }

    private static void checkForUndeterminedColumns(Map<String, LineSyntax> syntaxMap, String[] lines) {
        if (CollectionUtils.isEmpty(ProcessEfColumns.getLeftRightJoiningColumns()))
            return;
        for (ProcessEfColumns column : ProcessEfColumns.getLeftRightJoiningColumns()) {
            int index = 0;
            int startPosition = syntaxMap.get(column.name()).getStart();
            ProcessEfColumns nextColumn = ProcessEfColumns.getNextColumn(column);
            int endPosition = syntaxMap.get(nextColumn.name()).getStop();
            List<Integer> whiteSpacePositions = new ArrayList<Integer>(5);
            for (String line : lines) {
                if (index++ == 0)
                    continue;
                List<Integer> whiteSpaces = ZfinStringUtils.detectWhiteSpaces(line.substring(startPosition, endPosition + 1), startPosition);
                if (whiteSpacePositions.isEmpty())
                    whiteSpacePositions.addAll(whiteSpaces);
                else {
                    if (CollectionUtils.isEmpty(whiteSpacePositions))
                        throw new RuntimeException("Could not find a common white space column");
                    whiteSpacePositions = (List<Integer>) CollectionUtils.intersection(whiteSpacePositions, whiteSpaces);
                }
            }
            // calculate the stop and start position
            Collections.sort(whiteSpacePositions);
            LineSyntax lineSyntaxLeft = syntaxMap.get(column.name());
            if (lineSyntaxLeft.getStop() != -1)
                throw new RuntimeException("Wrong column");
            int end = whiteSpacePositions.get(0);
            lineSyntaxLeft.setStop(end);

            LineSyntax lineSyntaxRight = syntaxMap.get(nextColumn.name());
            if (lineSyntaxRight.getStart() != -1)
                throw new RuntimeException("Wrong column");
            int start = whiteSpacePositions.get(whiteSpacePositions.size() - 1);
            lineSyntaxRight.setStart(start);
        }
    }

    private static void printSyntax(Map<String, LineSyntax> syntaxMap) {
        for (ProcessEfColumns column : ProcessEfColumns.getSortedColumns()) {
            for (LineSyntax syntax : syntaxMap.values()) {
                if (syntax.getName().equals(column.name()))
                    System.out.println(column.name() + ": [" + syntax.getStart() + "," + syntax.getStop() + "]");
            }
        }
    }

    private static String getProcessOutput(CommandLine commandLine) {
        DefaultExecutor defaultExecutor = new DefaultExecutor();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
        try {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream, byteArrayErrorStream);
            defaultExecutor.setStreamHandler(pumpStreamHandler);
            int exitValue = defaultExecutor.execute(commandLine);
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<UnixProcess> getProcesses(Type type) {
        return null;
    }

    static class LineSyntax {
        private int start;
        private int stop;
        private String name;

        LineSyntax(int start, String name) {
            this.start = start;
            this.name = name;
        }

        LineSyntax(int start, int stop, String name) {
            this.start = start;
            this.stop = stop;
            this.name = name;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setStop(int stop) {
            this.stop = stop;
        }

        public String getName() {
            return name;
        }
    }

    enum Type {
        TOMCAT("Bootstrap"),
        LOAD_DATABASE("loaddb.pl");

        private String identifier;

        Type(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public enum ProcessEfColumns {
        UID("r", false), PID("r", false), PPID("r", false), C("r", false), STIME("l", true), TTY("l", true), TIME("r", false), CMD("l", true);

        private String adjustment;
        // does the column values consists of more than one alpha-numeric string
        private boolean allowMultiString;

        private ProcessEfColumns(String adjustment, boolean allowMultiString) {
            this.adjustment = adjustment;
            this.allowMultiString = allowMultiString;
        }

        public String getAdjustment() {
            return adjustment;
        }

        public boolean isRightAdjusted() {
            return adjustment.equals("r");
        }

        public boolean isLeftAdjusted() {
            return adjustment.equals("l");
        }

        public static List<ProcessEfColumns> getSortedColumns() {
            List<ProcessEfColumns> cols = new ArrayList<ProcessEfColumns>(values().length);
            for (ProcessEfColumns col : values())
                cols.add(col);
            return cols;
        }

        /**
         * Returns the left-adjusted columns of a left-right combination.
         *
         * @return
         */
        public static List<ProcessEfColumns> getLeftRightJoiningColumns() {
            List<ProcessEfColumns> cols = new ArrayList<ProcessEfColumns>(2);
            ProcessEfColumns previousProcessEfColumns = null;
            for (ProcessEfColumns column : values()) {
                if (previousProcessEfColumns != null) {
                    if (previousProcessEfColumns.isLeftAdjusted() && column.isRightAdjusted()) {
                        cols.add(previousProcessEfColumns);
                    }
                }
                previousProcessEfColumns = column;
            }
            return cols;
        }

        public static void validateHeaderLine(String headerLine) {
            for (ProcessEfColumns col : getSortedColumns()) {
                if (!headerLine.contains(col.name()))
                    throw new RuntimeException("No " + col.name() + " found in header line!");
            }
        }

        public static ProcessEfColumns getNextColumn(ProcessEfColumns column) {

            List<ProcessEfColumns> sortedColumns = getSortedColumns();
            for (int index = 0; index < sortedColumns.size() - 1; index++) {
                ProcessEfColumns col = sortedColumns.get(index);
                if (column.equals(col))
                    return sortedColumns.get(index + 1);
            }
            return null;
        }

        public boolean isLastColumn() {
            return getSortedColumns().get(getSortedColumns().size() - 1).equals(this);
        }
    }

}
