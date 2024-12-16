package org.zfin.analytics;

import com.google.analytics.data.v1beta.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsReportService {
    public static File runReportWithDates(AnalyticsReportDefinition reportDefinition, String credentials, String start, String end) {
        assertDateFormat(start);
        assertDateFormat(end);
        reportDefinition.setStartDate(start);
        reportDefinition.setEndDate(end);

        return runReport(reportDefinition, credentials);
    }

    private static File runReport(AnalyticsReportDefinition reportDefinition, String credentials) {
        return runReportAndWriteCsvFiles(reportDefinition, credentials);
    }

    private static void assertDateFormat(String start) {
        if (start.length() != 10) {
            throw new IllegalArgumentException("Date format should be YYYY-MM-DD");
        }
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        if (!start.matches(regex)) {
            throw new IllegalArgumentException("Date format should be YYYY-MM-DD");
        }
    }

    public static File runReportAndWriteCsvFiles(AnalyticsReportDefinition reportDefinition, String credentials) {
        RunReportRequest.Builder runReportRequestBuilder = RunReportRequest.newBuilder();
        System.out.println("Getting report for " + reportDefinition.reportName);
        System.out.println("Property ID: " + reportDefinition.propertyId);
        runReportRequestBuilder.setProperty("properties/" + reportDefinition.propertyId);

        // DateRange
        runReportRequestBuilder.addDateRanges(DateRange.newBuilder().setStartDate(reportDefinition.startDate).setEndDate(reportDefinition.endDate));

        // Configure Metrics
        reportDefinition.metrics.forEach(metricName -> {
            runReportRequestBuilder.addMetrics(Metric.newBuilder().setName(metricName));
        });

        // Configure Dimensions
        reportDefinition.dimensions.forEach(dimensionName -> {
            runReportRequestBuilder.addDimensions(Dimension.newBuilder().setName(dimensionName));
        });

        // Configure the sort order
        reportDefinition.sort.forEach(orderFieldName -> {
            runReportRequestBuilder.addOrderBys(OrderBy.newBuilder().setDimension(OrderBy.DimensionOrderBy.newBuilder().setDimensionName(orderFieldName)).build());
        });

        runReportRequestBuilder.setLimit(reportDefinition.limit);
        RunReportRequest request = runReportRequestBuilder.build();
        GoogleCredentials gc = null;
        BetaAnalyticsDataSettings betaAnalyticsDataSettings = null;

        try {
            gc = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(credentials.getBytes()))
                    .createScoped(AnalyticsReportingScopes.all());
            betaAnalyticsDataSettings = BetaAnalyticsDataSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(gc)).build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try(BetaAnalyticsDataClient service = BetaAnalyticsDataClient.create(betaAnalyticsDataSettings)) {
            RunReportResponse response = service.runReport(request);

            int totalRows = response.getRowCount();
            int rowsInResponse = response.getRowsCount();

//            System.out.println("Total rows: " + totalRows);
//            System.out.println("Rows in response: " + rowsInResponse);

            int pageCount = 0;
            while (rowsInResponse < totalRows) {
                request = runReportRequestBuilder.setOffset(rowsInResponse).build();
                response = service.runReport(request);
                rowsInResponse += response.getRowsCount();
                pageCount++;
//                System.out.println("Getting page " + pageCount + " for " + reportDefinition.reportName);
                File outputDir = writeResponseToCsvFile(reportDefinition, response.getRowsList());
                return outputDir;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File writeResponseToCsvFile(AnalyticsReportDefinition reportDefinition, List<Row> response) {
        File tempDir = null;
        try {
            int fileSuffix = 0;

            String randomString = RandomStringUtils.randomAlphabetic(5);
            tempDir = Files.createTempDirectory("analytics_" + randomString).toFile();
            tempDir.deleteOnExit();

            String filename = tempDir.getAbsolutePath() + "/" + reportDefinition.reportName + "-" + fileSuffix + ".csv";
            //while file exists, increment suffix
            while(new File(filename).exists()) {
                fileSuffix++;
                filename = tempDir.getAbsolutePath() + "/" + reportDefinition.reportName + "-" + fileSuffix + ".csv";
            }
            System.out.println("Writing report to " + filename);
            File csvFile = new File(filename);
            csvFile.deleteOnExit();

            //write headers
            List<String> headers = new ArrayList<>();
            headers.addAll(reportDefinition.metrics);
            headers.addAll(reportDefinition.dimensions);

            Path path = Paths.get( filename );
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            CSVPrinter csvWriter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader( headers.toArray(new String[0]) ));

            for (Row row: response) {
                List<DimensionValue> dimensions = row.getDimensionValuesList();
                List<MetricValue> metrics = row.getMetricValuesList();
                List<String> rowData = new ArrayList<>();

                for (int j = 0; j < metrics.size(); j++) {
                    MetricValue value = metrics.get(j);
                    rowData.add(value.getValue());
                }

                for (int i = 0; i < dimensions.size(); i++) {
                    DimensionValue value = dimensions.get(i);
                    rowData.add(value.getValue());
                }

                csvWriter.printRecord(rowData);
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempDir;
    }

}
