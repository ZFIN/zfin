package org.zfin.solr.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Shared Solr admin operations — the "hit an endpoint, then poll an async
 * command to completion" plumbing used by both the reindex orchestrator and
 * the backup/restore tool.
 *
 * <p>Consolidates logic that used to live in three places: the bespoke
 * {@code HttpClient} + {@code getJson} + {@code reloadCore} +
 * {@code waitForDihIdle} + {@code deleteAll} methods on
 * {@link org.zfin.solr.indexer.SolrReindexOrchestrator}, and the
 * {@code solr-backup.groovy} / {@code solr-restore.groovy} scripts.
 *
 * <p>Endpoint resolution mirrors {@code DiagContext}: the {@code SOLR} and
 * {@code CORE} environment variables win, falling back to
 * {@link ZfinPropertiesEnum}.
 *
 * <p>All calls use the JDK HTTP client (not SolrJ) so the synchronous-RELOAD
 * timeout and the Solr 9 {@code stream.body} delete workaround stay under
 * explicit control.
 */
public final class SolrAdminClient {

    private static final Logger logger = LogManager.getLogger(SolrAdminClient.class);

    /** Poll interval while waiting for a DIH full-import to return to idle. */
    private static final Duration DIH_IDLE_POLL_INTERVAL = Duration.ofSeconds(15);
    /** DIH needs a beat to flip idle→busy before the first status poll. */
    private static final Duration DIH_SETTLE = Duration.ofSeconds(3);
    /**
     * Budget for a core RELOAD. Bounds both the synchronous RELOAD request
     * (Solr holds the response open until the core has reopened its
     * IndexWriter — minutes on a multi-GB index) and the subsequent
     * ping-readiness poll.
     */
    private static final Duration RELOAD_TIMEOUT = Duration.ofMinutes(5);
    /** Default per-request timeout for quick admin GETs (status, ping, trigger). */
    private static final Duration GET_TIMEOUT = Duration.ofSeconds(30);
    /** Upper bound on an async backup/restore before we give up polling. */
    private static final Duration SNAPSHOT_TIMEOUT = Duration.ofHours(1);
    /** Poll interval while waiting for an async backup/restore. */
    private static final Duration SNAPSHOT_POLL_INTERVAL = Duration.ofSeconds(5);

    private final String coreBaseUrl;   // http://host:port/solr/<core>/
    private final String adminBaseUrl;  // http://host:port/solr/admin/
    private final String core;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private SolrAdminClient(String solrBase, String core) {
        this.core         = core;
        this.coreBaseUrl  = solrBase + "/" + core + "/";
        this.adminBaseUrl = solrBase + "/admin/";
    }

    /** {@code SOLR}/{@code CORE} env vars win, else fall back to {@link ZfinPropertiesEnum}. */
    public static SolrAdminClient fromEnvOrProperties() {
        String solr = System.getenv("SOLR");
        if (solr == null || solr.isBlank()) {
            solr = "http://" + ZfinPropertiesEnum.SOLR_HOST.value()
                + ":" + ZfinPropertiesEnum.SOLR_PORT.value()
                + "/" + ZfinPropertiesEnum.SOLR_CONTEXT.value();
        }
        String core = System.getenv("CORE");
        if (core == null || core.isBlank()) {
            core = ZfinPropertiesEnum.SOLR_CORE.value();
        }
        logger.info("SolrAdminClient: solr={}, core={}", solr, core);
        return new SolrAdminClient(solr, core);
    }

    // ---------- index lifecycle --------------------------------------------

    /**
     * Initial wipe. POST the delete-by-query as the request body — Solr 9
     * disabled {@code stream.body} by default, so the query-param form returns
     * HTTP 400 without {@code -Dsolr.enableStreamBody=true}.
     */
    public void deleteAll() throws Exception {
        String url = coreBaseUrl + "update?commit=true&wt=json";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "text/xml")
            .POST(HttpRequest.BodyPublishers.ofString("<delete><query>*:*</query></delete>"))
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            String body = resp.body();
            if (body.length() > 500) body = body.substring(0, 500) + "…";
            throw new RuntimeException("delete-all failed: HTTP " + resp.statusCode() + " — " + body);
        }
    }

    /**
     * Run a DIH full-import for one entity and block until it finishes.
     * {@code clean=false} (the run already wiped once), {@code commit=true}
     * (a later step may read these docs), {@code optimize=false} (per-step
     * optimize on a multi-GB index is murderous and pointless mid-run).
     */
    public void runDihImport(String entity) throws Exception {
        String url = coreBaseUrl + "dataimport?"
            + "command=full-import"
            + "&entity=" + enc(entity)
            + "&clean=false"
            + "&commit=true"
            + "&optimize=false"
            + "&wt=json";
        get(url, GET_TIMEOUT);
        // DIH takes a moment to flip from idle to busy; without the settle the
        // first status poll can catch it still idle from the prior step.
        Thread.sleep(DIH_SETTLE.toMillis());
        waitForDihIdle(entity);
    }

    /**
     * Poll {@code dataimport?command=status} until DIH returns to idle. Three
     * outcomes: idle + success → return; idle + "Full Import failed" → throw;
     * still busy → keep polling. No outer timeout — a heavy entity can
     * legitimately run for many minutes, and a spurious timeout would mask
     * real memory-pressure events.
     */
    private void waitForDihIdle(String entity) throws Exception {
        while (true) {
            String body = get(coreBaseUrl + "dataimport?command=status&wt=json", GET_TIMEOUT);
            if (body.contains("\"status\":\"idle\"")) {
                if (body.contains("Full Import failed")) {
                    String snippet = body.length() > 500 ? body.substring(0, 500) : body;
                    throw new RuntimeException(
                        "DIH reported 'Full Import failed' for entity=" + entity + ". Response: " + snippet);
                }
                return;
            }
            Thread.sleep(DIH_IDLE_POLL_INTERVAL.toMillis());
        }
    }

    /**
     * Reload the SolrCore to release IndexWriter buffers, caches, and
     * per-handler state inside the same JVM. The RELOAD is synchronous — Solr
     * holds the response open until the core has reopened, which on a
     * multi-GB index runs past the default GET budget, so it gets
     * {@link #RELOAD_TIMEOUT}.
     */
    public void reloadCore() throws Exception {
        logger.info("  ... reloading core to release Lucene state");
        String url = adminBaseUrl + "cores?action=RELOAD&core=" + enc(core) + "&wt=json";
        get(url, RELOAD_TIMEOUT);
        waitForCorePing();
    }

    private void waitForCorePing() throws Exception {
        long deadline = System.currentTimeMillis() + RELOAD_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                String body = get(coreBaseUrl + "admin/ping?wt=json", GET_TIMEOUT);
                if (body.contains("\"status\":\"OK\"") || body.contains("\"status\":0")) return;
            } catch (Exception ignored) { /* core mid-reload — retry */ }
            Thread.sleep(5000);
        }
        throw new RuntimeException("Solr core never came back up after RELOAD");
    }

    // ---------- backup / restore -------------------------------------------

    /**
     * Trigger an async replication backup and poll {@code details.backup}
     * until it reports success/failed for this snapshot. The snapshot lands at
     * {@code <location>/snapshot.<name>/}; {@code location} must be under a
     * path Solr is allowed to write to (the {@code solr.allowPaths} sysprop).
     */
    public void backup(String location, String name) throws Exception {
        name = stripSnapshotPrefix(name);
        new File(location).mkdirs();
        get(coreBaseUrl + "replication?command=backup"
            + "&location=" + enc(location) + "&name=" + enc(name) + "&wt=json", GET_TIMEOUT);
        logger.info("backup triggered: {}/snapshot.{}", location, name);
        // Solr reports the backup snapshotName as the bare NAME (no prefix).
        awaitSnapshot("details", new String[]{"details", "backup"}, name, "backup", location, name);
    }

    /**
     * Trigger an async replication restore and poll {@code restorestatus}
     * until it reports success/failed for this snapshot. Reads
     * {@code <location>/snapshot.<name>/}; replaces the core's index in place.
     */
    public void restore(String location, String name) throws Exception {
        name = stripSnapshotPrefix(name);
        get(coreBaseUrl + "replication?command=restore"
            + "&location=" + enc(location) + "&name=" + enc(name) + "&wt=json", GET_TIMEOUT);
        logger.info("restore triggered: {}/snapshot.{}", location, name);
        // Solr reports the restore snapshotName WITH the "snapshot." prefix.
        awaitSnapshot("restorestatus", new String[]{"restorestatus"}, "snapshot." + name, "restore", location, name);
    }

    /**
     * Poll a replication status command until the named block reports
     * {@code success} (return) or {@code failed} (throw) for the expected
     * snapshot, or {@link #SNAPSHOT_TIMEOUT} elapses. Transient fetch/parse
     * errors mid-operation are swallowed and retried (mirrors the old
     * {@code curl ... || true} poll loop).
     */
    private void awaitSnapshot(String command, String[] blockPath, String expectedSnapshotName,
                               String label, String location, String name) throws Exception {
        long deadline = System.currentTimeMillis() + SNAPSHOT_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            String status = "";
            String snapshotName = "";
            try {
                String body = get(coreBaseUrl + "replication?command=" + command + "&wt=json", GET_TIMEOUT);
                JsonNode block = mapper.readTree(body);
                for (String seg : blockPath) block = block.path(seg);
                snapshotName = block.path("snapshotName").asText("");
                status = block.path("status").asText("");
            } catch (Exception transientErr) {
                /* core mid-operation / transient HTTP — keep polling */
            }
            if (snapshotName.equals(expectedSnapshotName)) {
                if ("success".equals(status)) {
                    logger.info("{} complete: {}/snapshot.{}", label, location, name);
                    return;
                }
                if ("failed".equals(status)) {
                    throw new RuntimeException(label + " failed for snapshot." + name);
                }
            }
            Thread.sleep(SNAPSHOT_POLL_INTERVAL.toMillis());
        }
        throw new RuntimeException(label + " timed out after " + SNAPSHOT_TIMEOUT.toSeconds() + "s");
    }

    // ---------- HTTP plumbing ----------------------------------------------

    private String get(String url, Duration timeout) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(timeout).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            String body = resp.body();
            if (body.length() > 500) body = body.substring(0, 500) + "…";
            throw new RuntimeException("HTTP " + resp.statusCode() + " for " + url + " — body: " + body);
        }
        return resp.body();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Normalize a snapshot name: Solr's replication API takes the bare name
     * and creates/reads the {@code snapshot.<name>} directory, so a caller
     * who passes the on-disk directory name (with the {@code snapshot.}
     * prefix) means the same snapshot. Strip the prefix so both forms work.
     */
    private static String stripSnapshotPrefix(String name) {
        return name.startsWith("snapshot.") ? name.substring("snapshot.".length()) : name;
    }
}
