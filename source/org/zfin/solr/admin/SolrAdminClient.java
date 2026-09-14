package org.zfin.solr.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    /** Grace for a backup's snapshot dir to appear; if it never does, the backup couldn't start, so fail fast rather than poll out {@link #SNAPSHOT_TIMEOUT}. */
    private static final Duration BACKUP_DIR_APPEAR_GRACE = Duration.ofMinutes(2);
    /** Emit an in-progress progress line at most this often while polling. */
    private static final Duration SNAPSHOT_PROGRESS_LOG_INTERVAL = Duration.ofSeconds(60);

    private final String solrBase;      // http://host:port/solr
    private final String coreBaseUrl;   // http://host:port/solr/<core>/
    private final String adminBaseUrl;  // http://host:port/solr/admin/
    private final String core;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private SolrAdminClient(String solrBase, String core) {
        this.solrBase     = solrBase;
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

    /**
     * Same Solr endpoint, different core. The reindex orchestrator builds one
     * of these for the staging core while keeping the live-core client for the
     * final SWAP, so the two never get confused for one another.
     */
    public SolrAdminClient forCore(String otherCore) {
        return new SolrAdminClient(solrBase, otherCore);
    }

    /** The core this client acts on. */
    public String core() {
        return core;
    }

    /** {@code http://host:port/solr/<core>/} — what SolrJ wants as its base URL. */
    public String coreBaseUrl() {
        return coreBaseUrl;
    }

    // ---------- index lifecycle --------------------------------------------

    /**
     * Delete every document in this core. No longer part of the reindex run,
     * which builds into a staging core instead (ZFIN-10497); kept as an admin
     * primitive for ad-hoc use. POST the delete-by-query as the request body — Solr 9
     * disabled {@code stream.body} by default, so the query-param form returns
     * HTTP 400 without {@code -Dsolr.enableStreamBody=true}.
     */
    public void deleteAll() throws Exception {
        URI uri = new URIBuilder(coreBaseUrl + "update")
            .addParameter("commit", "true")
            .addParameter("wt", "json")
            .build();
        HttpRequest req = HttpRequest.newBuilder(uri)
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
        URI uri = new URIBuilder(coreBaseUrl + "dataimport")
            .addParameter("command", "full-import")
            .addParameter("entity", entity)
            .addParameter("clean", "false")
            .addParameter("commit", "true")
            .addParameter("optimize", "false")
            .addParameter("wt", "json")
            .build();
        get(uri, GET_TIMEOUT);
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
        URI uri = new URIBuilder(coreBaseUrl + "dataimport")
            .addParameter("command", "status")
            .addParameter("wt", "json")
            .build();
        while (true) {
            String body = get(uri, GET_TIMEOUT);
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
        URI uri = new URIBuilder(adminBaseUrl + "cores")
            .addParameter("action", "RELOAD")
            .addParameter("core", core)
            .addParameter("wt", "json")
            .build();
        get(uri, RELOAD_TIMEOUT);
        waitForCorePing();
    }

    private void waitForCorePing() throws Exception {
        URI uri = new URIBuilder(coreBaseUrl + "admin/ping")
            .addParameter("wt", "json")
            .build();
        long deadline = System.currentTimeMillis() + RELOAD_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                String body = get(uri, GET_TIMEOUT);
                if (body.contains("\"status\":\"OK\"") || body.contains("\"status\":0")) return;
            } catch (Exception ignored) { /* core mid-reload — retry */ }
            Thread.sleep(5000);
        }
        throw new RuntimeException("Solr core never came back up after RELOAD");
    }

    // ---------- core lifecycle (staging / swap) -----------------------------

    /**
     * Whether a core of this name is loaded. Uses {@code STATUS} for a single
     * core: Solr answers 200 with an empty {@code status.<name>} block for an
     * unknown core rather than erroring, so presence is decided on the block
     * having contents (an {@code instanceDir}), not on the HTTP code.
     */
    public boolean coreExists(String name) throws Exception {
        return !instanceDir(name).isBlank();
    }

    /**
     * The directory a loaded core occupies, or "" if no core of that name is
     * loaded. Trailing slash stripped so two paths for the same directory
     * compare equal.
     *
     * <p>The reindex needs this because a core's name and its directory stop
     * corresponding the moment a SWAP happens: the swap exchanges names, not
     * contents, so after one publish the core named {@code site_index} sits in
     * the directory named {@code site_index_staging}. Anything that derives a
     * directory from a name -- which is what CoreAdmin CREATE does by default
     * -- is then pointing at the wrong index, and in the worst case at the
     * live one.
     */
    public String instanceDir(String name) throws Exception {
        URI uri = new URIBuilder(adminBaseUrl + "cores")
            .addParameter("action", "STATUS")
            .addParameter("core", name)
            .addParameter("wt", "json")
            .build();
        String dir = mapper.readTree(get(uri, GET_TIMEOUT))
            .path("status").path(name).path("instanceDir").asText("");
        while (dir.endsWith("/")) {dir = dir.substring(0, dir.length() - 1);}
        return dir;
    }

    /**
     * Unload a core, swallowing "no such core". Used to clear a registration
     * left behind by a CREATE that failed partway -- Solr reports those under
     * {@code initFailures} rather than {@code status}, so they are invisible
     * to {@link #coreExists} but still block the name.
     */
    public void unloadCoreQuietly(String name, boolean deleteIndex) {
        try {
            unloadCore(name, deleteIndex);
        } catch (Exception e) {
            logger.info("  ... no core '{}' to unload ({})", name, e.getMessage());
        }
    }

    /**
     * Create a core from a named configset. The configset ships in the image
     * ({@code /opt/solr/server/solr/configsets/<configSet>}) and carries the
     * DIH jar and the JDBC driver in its {@code lib}, so a core created this
     * way can run the same imports as the live one.
     */
    public void createCore(String name, String configSet, String instanceDir) throws Exception {
        logger.info("  ... creating core '{}' from configset '{}'{}", name, configSet,
            instanceDir == null ? "" : " at " + instanceDir);
        URIBuilder b = new URIBuilder(adminBaseUrl + "cores")
            .addParameter("action", "CREATE")
            .addParameter("name", name)
            .addParameter("configSet", configSet);
        // Without this Solr derives the directory from the name, which is only
        // correct until the first SWAP. See instanceDir(String).
        if (instanceDir != null && !instanceDir.isBlank()) {
            b.addParameter("instanceDir", instanceDir);
        }
        get(b.addParameter("wt", "json").build(), RELOAD_TIMEOUT);
    }

    /**
     * Unload a core, optionally deleting its data directory. Deleting is the
     * point when recycling a staging core — without it the next run would
     * index on top of the previous run's documents.
     */
    public void unloadCore(String name, boolean deleteIndex) throws Exception {
        logger.info("  ... unloading core '{}' (deleteIndex={})", name, deleteIndex);
        URI uri = new URIBuilder(adminBaseUrl + "cores")
            .addParameter("action", "UNLOAD")
            .addParameter("core", name)
            .addParameter("deleteIndex", String.valueOf(deleteIndex))
            .addParameter("deleteDataDir", String.valueOf(deleteIndex))
            .addParameter("deleteInstanceDir", String.valueOf(deleteIndex))
            .addParameter("wt", "json")
            .build();
        get(uri, RELOAD_TIMEOUT);
    }

    /**
     * Atomically exchange the names of two cores. This is the publish step: the
     * freshly built staging index takes over the live name, and the index that
     * was live moves aside under the staging name, where it stays as a rollback
     * until the next run recycles it.
     *
     * <p>Standalone-only. There is no SolrCloud collection alias here to flip —
     * see the class comment on the reindex orchestrator.
     */
    public void swapCores(String a, String b) throws Exception {
        logger.info("  ... swapping cores '{}' <-> '{}'", a, b);
        URI uri = new URIBuilder(adminBaseUrl + "cores")
            .addParameter("action", "SWAP")
            .addParameter("core", a)
            .addParameter("other", b)
            .addParameter("wt", "json")
            .build();
        get(uri, RELOAD_TIMEOUT);
    }

    /**
     * Document count per {@code category} for this core, as a facet over the
     * whole index. The reindex gate compares these between the staging core and
     * the live one; {@code category} is the field the site's own facets are
     * built on, so a category missing here is a category missing from search.
     *
     * <p>Returns an empty map for an empty index rather than throwing —
     * "no documents at all" is a legitimate reading that the caller decides
     * what to do about.
     */
    public java.util.Map<String, Long> categoryCounts() throws Exception {
        URI uri = new URIBuilder(coreBaseUrl + "select")
            .addParameter("q", "*:*")
            .addParameter("rows", "0")
            .addParameter("facet", "true")
            .addParameter("facet.field", "category")
            .addParameter("facet.limit", "-1")
            .addParameter("facet.mincount", "1")
            .addParameter("wt", "json")
            .build();
        JsonNode counts = mapper.readTree(get(uri, GET_TIMEOUT))
            .path("facet_counts").path("facet_fields").path("category");
        // Solr returns a flat [name, count, name, count, ...] array here.
        var out = new java.util.LinkedHashMap<String, Long>();
        for (int i = 0; i + 1 < counts.size(); i += 2) {
            out.put(counts.get(i).asText(), counts.get(i + 1).asLong());
        }
        return out;
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
        get(new URIBuilder(coreBaseUrl + "replication")
            .addParameter("command", "backup")
            .addParameter("location", location)
            .addParameter("name", name)
            .addParameter("wt", "json")
            .build(), GET_TIMEOUT);
        logger.info("backup triggered: {}/snapshot.{}", location, name);
        // snapshotName is the bare NAME (no prefix); pass the on-disk dir so the poll can fail fast if Solr never creates it.
        awaitSnapshot("details", new String[]{"details", "backup"}, name, "backup",
            location, name, new File(location, "snapshot." + name));
    }

    /**
     * Trigger an async replication restore and poll {@code restorestatus}
     * until it reports success/failed for this snapshot. Reads
     * {@code <location>/snapshot.<name>/}; replaces the core's index in place.
     */
    public void restore(String location, String name) throws Exception {
        name = stripSnapshotPrefix(name);
        get(new URIBuilder(coreBaseUrl + "replication")
            .addParameter("command", "restore")
            .addParameter("location", location)
            .addParameter("name", name)
            .addParameter("wt", "json")
            .build(), GET_TIMEOUT);
        logger.info("restore triggered: {}/snapshot.{}", location, name);
        // snapshotName comes back WITH the "snapshot." prefix; restore reads an existing snapshot, so no dir-creation gate (snapshotDir = null).
        awaitSnapshot("restorestatus", new String[]{"restorestatus"}, "snapshot." + name, "restore",
            location, name, null);
    }

    /**
     * Poll a replication status command until the named block reports
     * {@code success} (return) or {@code failed} (throw) for the expected
     * snapshot, or {@link #SNAPSHOT_TIMEOUT} elapses. Transient fetch/parse
     * errors mid-operation are swallowed and retried (mirrors the old
     * {@code curl ... || true} poll loop).
     */
    private void awaitSnapshot(String command, String[] blockPath, String expectedSnapshotName,
                               String label, String location, String name, File snapshotDir) throws Exception {
        URI statusUri = new URIBuilder(coreBaseUrl + "replication")
            .addParameter("command", command)
            .addParameter("wt", "json")
            .build();
        long start = System.currentTimeMillis();
        long deadline = start + SNAPSHOT_TIMEOUT.toMillis();
        boolean snapshotDirSeen = false;
        long lastProgressLogMs = 0;
        while (System.currentTimeMillis() < deadline) {
            long elapsedMs = System.currentTimeMillis() - start;

            // Solr creates the snapshot dir within ~1s of starting; if it never appears the backup couldn't start (typically permissions), so fail fast.
            if (snapshotDir != null) {
                if (snapshotDir.isDirectory()) {
                    snapshotDirSeen = true;
                } else if (!snapshotDirSeen && elapsedMs > BACKUP_DIR_APPEAR_GRACE.toMillis()) {
                    throw new RuntimeException(label + " did not start: Solr never created "
                        + snapshotDir + " within " + BACKUP_DIR_APPEAR_GRACE.toSeconds() + "s. "
                        + "The Solr process (a non-root user) most likely cannot write to the "
                        + "backup location — check ownership/permissions on " + location + ".");
                }
            }

            String status = "";
            String snapshotName = "";
            String exception = "";
            try {
                String body = get(statusUri, GET_TIMEOUT);
                JsonNode block = mapper.readTree(body);
                for (String seg : blockPath) block = block.path(seg);
                snapshotName = block.path("snapshotName").asText("");
                status = block.path("status").asText("");
                exception = block.path("exception").asText("");
            } catch (Exception transientErr) {
                /* core mid-operation / transient HTTP — keep polling */
            }
            if (snapshotName.equals(expectedSnapshotName)) {
                if ("success".equals(status)) {
                    logger.info("{} complete: {}/snapshot.{}", label, location, name);
                    return;
                }
                if ("failed".equals(status)) {
                    throw new RuntimeException(label + " failed for snapshot." + name
                        + (exception.isBlank() ? "" : ": " + exception));
                }
            }

            // Periodic progress (slow copy vs stall); on-disk size is the live signal since Solr reports the previous snapshot until this one completes.
            if (elapsedMs - lastProgressLogMs >= SNAPSHOT_PROGRESS_LOG_INTERVAL.toMillis()) {
                lastProgressLogMs = elapsedMs;
                logger.info("  {} in progress: {}s elapsed{}; solr reports snapshotName={} status={}",
                    label, elapsedMs / 1000,
                    snapshotDir != null ? ", on-disk=" + humanSize(dirSizeBytes(snapshotDir)) : "",
                    snapshotName.isEmpty() ? "(none)" : snapshotName,
                    status.isEmpty() ? "(none)" : status);
            }
            Thread.sleep(SNAPSHOT_POLL_INTERVAL.toMillis());
        }
        throw new RuntimeException(label + " timed out after " + SNAPSHOT_TIMEOUT.toSeconds() + "s");
    }

    /** Sum of file sizes directly under a snapshot directory (flat; 0 if absent). */
    private static long dirSizeBytes(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;
        long total = 0;
        for (File f : files) {
            if (f.isFile()) total += f.length();
        }
        return total;
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024L * 1024) return String.format("%.1fKB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1fMB", bytes / (1024.0 * 1024));
        return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
    }

    // ---------- HTTP plumbing ----------------------------------------------

    private String get(URI uri, Duration timeout) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            String body = resp.body();
            if (body.length() > 500) body = body.substring(0, 500) + "…";
            throw new RuntimeException("HTTP " + resp.statusCode() + " for " + uri + " — body: " + body);
        }
        return resp.body();
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
