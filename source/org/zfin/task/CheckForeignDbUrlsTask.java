package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDbUrlCheckRow;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds one example URL per foreign_db row (dbUrlPrefix + an accession that
 * actually uses it + dbUrlSuffix) and requests it, flagging rows whose URL
 * no longer resolves cleanly: a non-2xx/connection failure, or a redirect
 * that lands on a different host (often a sign the stored prefix is stale,
 * e.g. ZFIN-10545's PMID row still pointing at a retired NCBI endpoint).
 * Foreign_db rows with no accession anywhere in db_link are skipped — there's
 * nothing to build a real URL from. Read-only — no DB writes.
 */
public class CheckForeignDbUrlsTask extends AbstractValidateDataReportTask {

    private static final Logger LOG = LogManager.getLogger(CheckForeignDbUrlsTask.class);
    private static final int MAX_REDIRECTS = 5;
    private static final int TIMEOUT_MILLIS = 15000;

    public CheckForeignDbUrlsTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    @Override
    public int execute() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        LOG.info("Job Name: " + jobName);

        clearReportDirectory();
        setLoggerFile();

        List<List<String>> problems = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int examined = 0;
        int skippedNoAccession = 0;

        try {
            List<ForeignDbUrlCheckRow> rows = RepositoryFactory.getSequenceRepository()
                    .getForeignDbUrlCheckCandidates();

            for (ForeignDbUrlCheckRow row : rows) {
                if (row.exampleAccession() == null) {
                    skippedNoAccession++;
                    continue;
                }
                examined++;

                String url = row.dbUrlPrefix() + row.exampleAccession()
                        + (row.dbUrlSuffix() == null ? "" : row.dbUrlSuffix());

                UrlCheckResult result = checkUrl(url);
                if (!result.success()) {
                    List<String> outRow = new ArrayList<>(5);
                    outRow.add(String.valueOf(row.foreignDbId()));
                    outRow.add(row.dbName());
                    outRow.add(url);
                    outRow.add(row.exampleAccession());
                    outRow.add(result.problem());
                    problems.add(outRow);
                }
            }
        } catch (Exception e) {
            LOG.error("Foreign-db URL check failed", e);
            errorMessages.add(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }

        LOG.info("Examined " + examined + " foreign_db rows; "
                + problems.size() + " problems; "
                + skippedNoAccession + " skipped (no accession to build a URL from)");

        createErrorReport(errorMessages, problems);
        return 0;
    }

    private record UrlCheckResult(boolean success, String problem) {
        static UrlCheckResult ok() {
            return new UrlCheckResult(true, null);
        }

        static UrlCheckResult problem(String message) {
            return new UrlCheckResult(false, message);
        }
    }

    /**
     * Follows redirects by hand (rather than HttpURLConnection's automatic
     * following) so a redirect that lands on a different host can be reported
     * as its own kind of problem instead of silently resolving to a 200.
     */
    private UrlCheckResult checkUrl(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return UrlCheckResult.problem("Malformed URL: " + e.getMessage());
        }

        String originalHost = normalizeHost(url.getHost());

        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            HttpURLConnection connection;
            int responseCode;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(TIMEOUT_MILLIS);
                connection.setReadTimeout(TIMEOUT_MILLIS);
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();
            } catch (IOException e) {
                return UrlCheckResult.problem("Connection failed: " + e.getMessage());
            }
            connection.disconnect();

            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                if (location == null) {
                    return UrlCheckResult.problem("HTTP " + responseCode + " with no Location header");
                }
                URL next;
                try {
                    next = new URL(url, location);
                } catch (MalformedURLException e) {
                    return UrlCheckResult.problem("HTTP " + responseCode + " redirected to malformed URL: " + location);
                }
                if (!normalizeHost(next.getHost()).equals(originalHost)) {
                    return UrlCheckResult.problem("Redirected from " + url.getHost() + " to a different host: " + next.getHost());
                }
                url = next;
                continue;
            }

            if (responseCode < 200 || responseCode >= 300) {
                return UrlCheckResult.problem("HTTP " + responseCode);
            }
            return UrlCheckResult.ok();
        }
        return UrlCheckResult.problem("Too many redirects (>" + MAX_REDIRECTS + ")");
    }

    private static String normalizeHost(String host) {
        if (host == null) {
            return "";
        }
        String lower = host.toLowerCase();
        return lower.startsWith("www.") ? lower.substring(4) : lower;
    }

    /**
     * Invoked via the checkForeignDbUrlsTask Gradle task (console.gradle), not Ant -- reads
     * its configuration from system properties, forwarded there as -D flags, rather than
     * positional args (the house style for gradle-invoked report/maintenance tasks).
     */
    public static void main(String[] args) {
        String jobName = System.getProperty("jobName", "Check-Foreign-Db-Urls_m");
        String propertyFilePath = System.getProperty("propertyFilePath", "home/WEB-INF/zfin.properties");
        String directory = System.getProperty("dataDirectory",
                (System.getenv("TARGETROOT") != null ? System.getenv("TARGETROOT") : System.getProperty("user.dir"))
                        + "/server_apps/DB_maintenance/validatedata");
        CheckForeignDbUrlsTask task = new CheckForeignDbUrlsTask(jobName, propertyFilePath, directory);
        task.initDatabase();
        System.exit(task.execute());
    }
}
