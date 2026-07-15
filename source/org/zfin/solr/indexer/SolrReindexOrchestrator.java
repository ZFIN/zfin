package org.zfin.solr.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.solr.admin.SolrAdminClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nightly Solr reindex driver — Java port of {@code solr-reindex-pipeline.sh}.
 *
 * <p>The legacy shell pipeline iterated the DIH entity batches (heavy →
 * medium → light), polled DIH after each entity, and ran a core RELOAD
 * between batches to release Lucene's IndexWriter buffers (the dominant
 * heap-growth source on a multi-GB rebuild — see ZFIN-10171). The Java
 * port keeps the same orchestration but lets a step opt into a native
 * {@link SolrIndexer} from {@link IndexerRegistry} instead of DIH, so
 * the cutover off DIH can happen entity-by-entity without two parallel
 * pipelines.
 *
 * <p>The Solr-admin plumbing (DIH full-import + idle poll, core RELOAD +
 * ping, the delete-all wipe) lives in {@link SolrAdminClient}, shared with
 * the backup/restore tool. This class is just the batch plan and the
 * sequencing around it.
 *
 * <h2>Migration model</h2>
 *
 * Each entry in {@link #BATCHES} is tagged with {@link Source#DIH} or
 * {@link Source#JAVA}. Migrating an entity is two coordinated changes:
 *
 * <ol>
 *   <li>Add a {@link SolrIndexer} implementation and register it in
 *       {@link IndexerRegistry}.</li>
 *   <li>Flip the matching {@link Step} in {@link #BATCHES} from
 *       {@link Source#DIH} to {@link Source#JAVA}, and remove the
 *       {@code <entity name="…">} block from {@code db-data-config.xml}
 *       so DIH stops emitting it.</li>
 * </ol>
 *
 * <h2>Clean semantics</h2>
 *
 * Because DIH and Java steps share a batch, we can't rely on DIH's
 * {@code clean=true&entity=X} (which only wipes once, on its first
 * caller). The orchestrator instead issues an explicit
 * {@code <delete><query>*:*</query></delete>} at the very start of a
 * normal run, and then every step runs with {@code clean=false}. The
 * {@code --no-clean} / {@code --resume-from} flags skip that wipe so
 * the partial index from a previous failed run is preserved.
 *
 * <h2>Invocation</h2>
 *
 * <pre>
 *   gradle solrReindex
 *   gradle solrReindex -PsolrResumeFrom=construct
 *   gradle solrReindex -PsolrNoClean=true
 * </pre>
 */
public class SolrReindexOrchestrator extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SolrReindexOrchestrator.class);

    /** Which engine indexes an entity. Migrating an entity flips this value. */
    enum Source { DIH, JAVA }

    /** One reindex step — names the entity and the engine that indexes it. */
    record Step(String entity, Source source) { }

    /**
     * Batches mirror {@code solr-reindex-pipeline.sh} (heavy → medium →
     * light). The shell-script ordering is preserved so behavior under
     * memory pressure stays predictable — a regression surfaces in the
     * heavy batch instead of after an hour of successful work.
     *
     * <p>A core RELOAD runs between batches (not within), so groups of
     * entities that share a SolrCore lifetime should land in the same
     * batch — colocate entities that benefit from warm caches, separate
     * the heap-hungry ones.
     */
    private static final List<Batch> BATCHES = List.of(
        // The heavy entities are split one-per-batch so a core RELOAD runs
        // between each, rather than once after all seven. A RELOAD releases
        // Lucene's IndexWriter buffers (the dominant heap-growth source; see
        // ZFIN-10171); accumulating all seven before the first reload pushed
        // the 12g heap into OutOfMemoryError partway through `expression`
        // (~600k docs already buffered from figure + phenotype). Each entity
        // fits comfortably in 12g on its own, so reloading between them bounds
        // peak heap. Costs a few extra reloads (~minutes) for crash-safety.
        new Batch("figure",                      List.of(new Step("figure",                      Source.DIH))),
        new Batch("phenotype",                   List.of(new Step("phenotype",                   Source.DIH))),
        new Batch("phenotype_misexpressed_gene", List.of(new Step("phenotype_misexpressed_gene", Source.DIH))),
        new Batch("expression",                  List.of(new Step("expression",                  Source.DIH))),
        new Batch("feature",                     List.of(new Step("feature",                     Source.DIH))),
        new Batch("expression_result",           List.of(new Step("expression_result",           Source.DIH))),
        new Batch("phenotype_observation",       List.of(new Step("phenotype_observation",       Source.DIH))),
        new Batch("medium", List.of(
            new Step("fish",        Source.DIH),
            new Step("construct",   Source.DIH),
            new Step("gene",        Source.DIH),
            new Step("marker",      Source.DIH),
            new Step("str",         Source.DIH),
            new Step("antibody",    Source.DIH),
            new Step("term",        Source.DIH),
            new Step("publication", Source.DIH)
        )),
        new Batch("light", List.of(
            new Step("person",           Source.DIH),
            new Step("lab",              Source.JAVA),  // migrated ZFIN-10171
            new Step("company",          Source.DIH),
            new Step("journal",          Source.DIH),
            new Step("go_annotation",    Source.DIH),
            new Step("str_relationship", Source.DIH)
        ))
    );

    record Batch(String name, List<Step> steps) { }

    /** Solr-admin operations (DIH import, core RELOAD, wipe). */
    private SolrAdminClient admin;

    private String resumeFrom;  // null = start from scratch
    private boolean noClean;    // true = skip the initial wipe

    public SolrReindexOrchestrator(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        var job = new SolrReindexOrchestrator(args[2], args[0], args[1]);
        if (args.length > 3 && !args[3].isBlank()) job.resumeFrom = args[3].trim();
        if (args.length > 4 && !args[4].isBlank()) job.noClean = Boolean.parseBoolean(args[4].trim());
        job.initDatabase(true);
        System.exit(job.execute());
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        SolrClient solr = null;
        try {
            solr = newSolrClient();
            admin = SolrAdminClient.fromEnvOrProperties();
            List<Batch> plan = plan();
            logBanner(plan);

            // Initial wipe unless we're resuming or were told not to.
            if (resumeFrom == null && !noClean) {
                logger.info("Wiping index (delete *:*)");
                admin.deleteAll();
            } else {
                logger.info("Skipping initial wipe (resumeFrom={}, noClean={})", resumeFrom, noClean);
            }

            for (int i = 0; i < plan.size(); i++) {
                Batch batch = plan.get(i);
                logger.info("=== batch '{}' ({} steps) ===", batch.name(), batch.steps().size());
                for (Step step : batch.steps()) {
                    runStep(step, solr);
                }
                // Release Lucene state between batches, except after the
                // last one — there's no further entity that would benefit.
                if (i + 1 < plan.size()) admin.reloadCore();
            }

            logger.info("Committing");
            solr.commit();
            logger.info("All batches complete.");
            return 0;
        } catch (Exception e) {
            logger.error("Reindex failed", e);
            return 1;
        } finally {
            if (solr != null) try { solr.close(); } catch (Exception ignored) { }
            HibernateUtil.closeSession();
        }
    }

    // ---------- planning ----------------------------------------------------

    /**
     * Materialize the run plan, honoring {@code --resume-from}. Entities
     * before the resume point are skipped; the batch containing it starts
     * with that entity (preserving inter-batch reload semantics).
     */
    private List<Batch> plan() {
        if (resumeFrom == null) return BATCHES;

        var trimmed = new ArrayList<Batch>();
        boolean found = false;
        for (Batch b : BATCHES) {
            if (found) { trimmed.add(b); continue; }
            var pickedSteps = new ArrayList<Step>();
            for (Step s : b.steps()) {
                if (!found && s.entity().equals(resumeFrom)) found = true;
                if (found) pickedSteps.add(s);
            }
            if (!pickedSteps.isEmpty()) {
                trimmed.add(new Batch(b.name(), pickedSteps));
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                "--resumeFrom '" + resumeFrom + "' didn't match any known entity. Known: " + allEntityNames());
        }
        return trimmed;
    }

    private List<String> allEntityNames() {
        return BATCHES.stream().flatMap(b -> b.steps().stream()).map(Step::entity).toList();
    }

    private void logBanner(List<Batch> plan) {
        Map<Source, Long> counts = plan.stream()
            .flatMap(b -> b.steps().stream())
            .collect(java.util.stream.Collectors.groupingBy(Step::source, java.util.stream.Collectors.counting()));
        logger.info("Plan: {} batches, {} DIH steps, {} JAVA steps{}",
            plan.size(),
            counts.getOrDefault(Source.DIH,  0L),
            counts.getOrDefault(Source.JAVA, 0L),
            resumeFrom == null ? "" : " (resumeFrom=" + resumeFrom + ")");
    }

    // ---------- per-step execution -----------------------------------------

    private void runStep(Step step, SolrClient solr) throws Exception {
        logger.info("  · {} ({})", step.entity(), step.source());
        long t0 = System.currentTimeMillis();
        switch (step.source()) {
            case DIH  -> admin.runDihImport(step.entity());
            case JAVA -> runJavaStep(step.entity(), solr);
        }
        logger.info("    ok ({} ms)", System.currentTimeMillis() - t0);
    }

    private void runJavaStep(String entity, SolrClient solr) throws Exception {
        SolrIndexer indexer = IndexerRegistry.get(entity);
        if (indexer == null) {
            throw new IllegalStateException(
                "BATCHES says '" + entity + "' is JAVA-sourced but no SolrIndexer is registered for it. " +
                "Either register one in IndexerRegistry or flip the BATCHES entry back to DIH.");
        }
        indexer.index(solr);
        // Commit so a subsequent DIH step sees the just-indexed docs.
        // The final solr.commit() at end-of-run is still issued for safety.
        solr.commit();
    }

    // ---------- Solr client -------------------------------------------------

    private String solrCoreBaseUrl() {
        return "http://"
            + ZfinPropertiesEnum.SOLR_HOST.value() + ":"
            + ZfinPropertiesEnum.SOLR_PORT.value() + "/"
            + ZfinPropertiesEnum.SOLR_CONTEXT.value() + "/"
            + ZfinPropertiesEnum.SOLR_CORE.value() + "/";
    }

    private SolrClient newSolrClient() {
        return new HttpSolrClient.Builder(solrCoreBaseUrl()).build();
    }
}
