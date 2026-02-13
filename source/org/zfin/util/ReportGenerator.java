package org.zfin.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.*;
import java.util.*;

import static java.lang.System.getenv;

public class ReportGenerator {

    public enum Format { HTML, TXT, XLSX, CSV }

    private final static Logger log = LogManager.getLogger(ReportGenerator.class);

    private final static String REPORT_TITLE = "reportTitle";
    private final static String TIME_STAMP = "timeStamp";
    private final static String DOMAIN_NAME = "domainName";
    private final static String INTRO_PARAGRAPHS = "introParagraphs";
    private final static String DATA_TABLES = "dataTables";
    private final static String SUMMARY_TABLES = "summaryTables";
    private final static String TABLE_CAPTION = "caption";
    private final static String TABLE_HEADER = "head";
    private final static String TABLE_DATA = "data";
    private final static String TABLE_WIDTHS = "colWidths";
    private final static String ERROR_MESSAGES = "errorMessages";
    private final static String CODE_SNIPPETS = "codeSnippets";

    private Map<String, Object> root;

    public ReportGenerator() {
        root = new HashMap<>();
        root.put(REPORT_TITLE, "");
        root.put(DOMAIN_NAME, ZfinPropertiesEnum.DOMAIN_NAME);
        root.put(INTRO_PARAGRAPHS, new ArrayList());
        root.put(DATA_TABLES, new ArrayList());
        root.put(SUMMARY_TABLES, new ArrayList());
        root.put(ERROR_MESSAGES, new ArrayList());
        root.put(CODE_SNIPPETS, new ArrayList());
    }

    public void setReportTitle(String title) {
        root.put(REPORT_TITLE, title);
    }

    public void includeTimestamp() {
        root.put(TIME_STAMP, new Date());
    }

    public void addIntroParagraph(String paragraph) {
        addToList(INTRO_PARAGRAPHS, paragraph);
    }

    public void addSummaryTable(String caption, List<List<String>> data) {
        Map<String, Object> table = new HashMap<>();
        table.put(TABLE_CAPTION, caption);
        table.put(TABLE_DATA, data);
        table.put(TABLE_WIDTHS, getColumnWidths(data));
        addToList(SUMMARY_TABLES, table);
    }

    public void addSummaryTable(List<List<String>> data) {
        addSummaryTable("", data);
    }

    public void addSummaryTable(String caption, Map<String, Object> data) {
        List<List<String>> listifiedData = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            List<String> row = new ArrayList<>();
            row.add(entry.getKey());
            row.add(entry.getValue().toString());
            listifiedData.add(row);
        }
        addSummaryTable(caption, listifiedData);
    }

    public void addSummaryTable(Map<String, Object> data) {
        addSummaryTable("", data);
    }

    public void addDataTable(String caption, List<String> header, List<List<String>> data) {
        Map<String, Object> table = new HashMap<>();
        table.put(TABLE_CAPTION, caption);
        table.put(TABLE_HEADER, header);
        table.put(TABLE_DATA, data);
        table.put(TABLE_WIDTHS, getColumnWidths(header, data));
        addToList(DATA_TABLES, table);
    }

    public void addDataTable(List<String> header, List<List<String>> data) {
        addDataTable("", header, data);
    }

    public void addDataTable(String caption, List<List<String>> data) {
        addDataTable(caption, new ArrayList<String>(), data);
    }

    public void addDataTable(List<List<String>> data) {
        addDataTable("", data);
    }

    public void addErrorMessage(String message) {
        addToList(ERROR_MESSAGES, message);
    }

    public void addErrorMessage(Throwable throwable) {
        addErrorMessage(ExceptionUtils.getStackTrace(throwable));
    }

    public void addCodeSnippet(String snippet) {
        addToList(CODE_SNIPPETS, snippet);
    }

    public void write(Writer output, Format format) throws IOException, TemplateException {
        Configuration config = ZfinProperties.getTemplateConfiguration();
        Template template;
        try {
            template = config.getTemplate("report-generator." + format.toString().toLowerCase() + ".ftl");
        } catch (IOException e) {
            throw new IOException("Could not find Report Generator template for format: " + format, e);
        }
        template.process(root, output);
    }

    public void writeFiles(File directory, String baseName) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        for (Format format : Format.values()) {
            try {
                File outputFile = new File(directory, baseName + "." + format.toString().toLowerCase());
                log.info("Writing report to " + outputFile.getAbsolutePath());
                if (format == Format.XLSX) {
                    writeXlsx(outputFile);
                } else if (format == Format.CSV) {
                    writeCsv(outputFile);
                } else {
                    FileWriter writer = new FileWriter(outputFile);
                    write(writer, format);
                }
            } catch (TemplateException | IOException e) {
                log.error("Error writing " + baseName + "." + format + " to " + directory.getAbsolutePath(), e);
            }
        }
    }

    public void addArtifactDiffLink(File reportDirectory, String reportName) {
        //requires HUDSON_URL and BUILD_DISPLAY_NAME environment variables
        if (getenv("HUDSON_URL") == null || getenv("BUILD_DISPLAY_NAME") == null) {
            log.error("HUDSON_URL and BUILD_DISPLAY_NAME environment variables must be set to add artifact diff link to report");
            return;
        }
        String jenkinsBaseUrl = getenv("HUDSON_URL");

        Integer buildNumber = null;
        try {
            buildNumber = Integer.parseInt(getenv("BUILD_DISPLAY_NAME").replace("#", ""));
        } catch (NumberFormatException e) {
            log.error("BUILD_DISPLAY_NAME environment variable must be a number (eg. #123) to add artifact diff link to report");
            return;
        }
        if (buildNumber == 1) {
            log.info("No previous build to compare to");
            return;
        }
        Integer previousBuildNumber = buildNumber - 1;

        String targetRoot = new File(ZfinPropertiesEnum.TARGETROOT.value()).getAbsolutePath();
        String artifactRelativePath = reportDirectory.getAbsolutePath().replace(targetRoot, "");

        String jobName = reportName;
        String[] artifactDiffUrlParts = {
            jenkinsBaseUrl,
            "job",
            jobName,
            previousBuildNumber.toString(),
            "artifact-diff",
            buildNumber.toString(),
            artifactRelativePath,
            reportName + ".txt"
        };
        String artifactDiffUrl = String.join("/", artifactDiffUrlParts);
        log.info("Artifact diff URL: " + artifactDiffUrl);

        addIntroParagraph("Artifact diff: <a href=\"" + artifactDiffUrl + "\">" + artifactDiffUrl + "</a>");
    }


    @SuppressWarnings("unchecked")
    private void writeXlsx(File outputFile) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);

            List<Map<String, Object>> dataTables = (List<Map<String, Object>>) root.get(DATA_TABLES);
            if (dataTables.isEmpty()) {
                Sheet sheet = workbook.createSheet("Report");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("No data");
            }
            int sheetIndex = 0;
            for (Map<String, Object> table : dataTables) {
                String caption = (String) table.get(TABLE_CAPTION);
                String sheetName = caption != null && !caption.isEmpty() ? caption : "Data";
                // Sheet names must be <= 31 chars and unique
                if (sheetName.length() > 31) {
                    sheetName = sheetName.substring(0, 31);
                }
                if (sheetIndex > 0) {
                    sheetName = sheetName.substring(0, Math.min(sheetName.length(), 28)) + "_" + sheetIndex;
                }
                Sheet sheet = workbook.createSheet(sheetName);
                int rowIndex = 0;

                List<String> header = (List<String>) table.get(TABLE_HEADER);
                if (header != null && !header.isEmpty()) {
                    Row headerRow = sheet.createRow(rowIndex++);
                    for (int i = 0; i < header.size(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(header.get(i));
                        cell.setCellStyle(headerStyle);
                    }
                }

                List<List<String>> data = (List<List<String>>) table.get(TABLE_DATA);
                if (data != null) {
                    for (List<String> dataRow : data) {
                        Row row = sheet.createRow(rowIndex++);
                        for (int i = 0; i < dataRow.size(); i++) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(dataRow.get(i));
                        }
                    }
                }
                sheetIndex++;
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
            workbook.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    private void writeCsv(File outputFile) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT)) {
            List<Map<String, Object>> dataTables = (List<Map<String, Object>>) root.get(DATA_TABLES);
            for (Map<String, Object> table : dataTables) {
                List<String> header = (List<String>) table.get(TABLE_HEADER);
                if (header != null && !header.isEmpty()) {
                    printer.printRecord(header);
                }
                List<List<String>> data = (List<List<String>>) table.get(TABLE_DATA);
                if (data != null) {
                    for (List<String> row : data) {
                        printer.printRecord(row);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addToList(String field, Object toAdd) {
        ((List) root.get(field)).add(toAdd);
    }

    private static int[] getColumnWidths(List<String> header, List<List<String>> data) {
        int longestRow = header.size();
        for (List<String> row : data) {
            longestRow = Math.max(longestRow, row.size());
        }
        int[] widths = new int[longestRow];
        Arrays.fill(widths, 0);
        for (int i = 0; i < header.size(); i++) {
            widths[i] = header.get(i).length();
        }
        for (List<String> row : data) {
            int rowSize = row.size();
            for (int i = 0; i < longestRow; i++) {
                if (i >= rowSize) {
                    continue;
                }
                String value = row.get(i);
                widths[i] = Math.max(widths[i], value == null ? 0 : value.length());
            }
        }
        return widths;
    }

    private static int[] getColumnWidths(List<List<String>> data) {
        return getColumnWidths(new ArrayList<String>(), data);
    }
}
