package org.zfin.solr.diagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.zfin.properties.ZfinPropertiesEnum;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shared SolrJ-client setup + path/config plumbing for the diagnostic
 * suite. One context is built up front by {@link SolrCharacterizer} and
 * handed to every {@link DiagTool} that runs in the session, so each
 * tool sees consistent endpoint/core values regardless of how it was
 * invoked.
 *
 * <p>Endpoint defaults come from {@link ZfinPropertiesEnum} so the tools
 * line up with the rest of the application's Solr setup, but environment
 * variables {@code SOLR} and {@code CORE} override — matching the legacy
 * bash scripts so a {@code SOLR=http://prod.example/solr gradle solrDiag}
 * invocation targets a different cluster.
 *
 * <p>The class owns two clients because Solr's URL hierarchy splits at
 * the {@code /admin} level: most endpoints are core-scoped
 * ({@code /select}, {@code /terms}, {@code /admin/luke},
 * {@code /analysis/field}) and want a client whose base URL ends with
 * the core name; {@code /admin/info/system} is server-scoped and wants
 * the bare {@code /solr/} base. Holding both avoids re-creating either
 * on every call.
 */
public final class DiagContext implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(DiagContext.class);

    private final String solrBase;       // http://host:port/solr
    private final String core;           // e.g. site_index
    private final SolrClient coreClient; // talks to /solr/<core>/...
    private final SolrClient adminClient;// talks to /solr/admin/...

    private DiagContext(String solrBase, String core) {
        this.solrBase    = solrBase;
        this.core        = core;
        this.coreClient  = new HttpSolrClient.Builder(solrBase + "/" + core).build();
        this.adminClient = new HttpSolrClient.Builder(solrBase).build();
    }

    /**
     * Build a context from {@link ZfinPropertiesEnum}, with the
     * {@code SOLR}/{@code CORE} env vars taking precedence — same
     * precedence the legacy bash scripts used.
     */
    public static DiagContext fromEnvOrProperties() {
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
        logger.info("DiagContext: solr={}, core={}", solr, core);
        return new DiagContext(solr, core);
    }

    public String solrBase()       { return solrBase; }
    public String core()           { return core; }
    public SolrClient coreClient() { return coreClient; }
    public SolrClient adminClient(){ return adminClient; }

    /**
     * Resolve a CLI-provided path against the project root. Absolute
     * paths pass through unchanged; relative paths are interpreted
     * against the JVM's current working directory — gradle's
     * {@code JavaExec} sets that to the project root, so a user typing
     * {@code -PdiagArgs="dump ./out/foo.ndjson"} from anywhere in the
     * checkout lands the file at {@code <repo>/out/foo.ndjson}.
     */
    public Path resolvePath(String pathArg) {
        return Paths.get(pathArg).toAbsolutePath().normalize();
    }

    @Override
    public void close() {
        try { coreClient.close(); }  catch (Exception ignored) { }
        try { adminClient.close(); } catch (Exception ignored) { }
    }
}
