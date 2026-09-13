package org.zfin.solr.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
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
 * ping, core create/unload/swap) lives in {@link SolrAdminClient}, shared
 * with the backup/restore tool. This class is just the batch plan, the
 * staging lifecycle, and the sequencing around them.
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
 * <h2>Staging and publish (ZFIN-10497)</h2>
 *
 * The run builds into a staging core and publishes with a single atomic
 * CoreAdmin SWAP. Until that swap the site keeps serving the previous
 * index, complete and untouched.
 *
 * <p>This replaces wiping the live index up front. That wipe emptied every
 * category at once and then refilled them one entity at a time, so for the
 * length of a rebuild the site returned nothing for every category whose
 * step had not run yet — the whole of ZFIN-10497.
 *
 * <p>Per-category replacement, which the ticket asks for literally, is not
 * available: {@code category} is not 1:1 with a step. {@code person},
 * {@code company} and {@code lab} all emit "Community", {@code fish} emits
 * two categories, and {@code antibody} derives its category from the
 * database ({@code mrkrtype_type_display as category}) so its values are
 * not knowable from config at all. Deleting per category would destroy
 * documents belonging to steps that had not run yet. Swapping the whole
 * core is both safe and strictly better: nothing is ever partially visible.
 *
 * <p>Because DIH and Java steps share a batch we still cannot rely on DIH's
 * {@code clean=true&entity=X} (it only wipes once, on its first caller), so
 * every step continues to run {@code clean=false}. Cleanliness now comes
 * from the staging core being created fresh, not from a delete.
 *
 * <p>After the swap the previously live index survives under the staging
 * name. It is the rollback: swap the two names back. The next run recycles
 * it, so exactly one generation is kept.
 *
 * <h2>Invocation</h2>
 *
 * <pre>
 *   gradle solrReindex
 *   gradle solrReindex -PsolrResumeFrom=construct
 *   gradle solrReindex -PsolrNoClean=true
 * </pre>
 *
 * {@code --resume-from} and {@code --no-clean} both continue into the
 * existing staging core instead of creating a fresh one, so a run that died
 * half way can be picked up where it stopped. Neither touches the live core;
 * the swap still only happens once the resumed run reaches the end.
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

    /**
     * Suffix for the core the run builds into. The live core keeps its own
     * name throughout; the two exchange names once, at the end.
     */
    private static final String STAGING_SUFFIX = "_staging";

    /**
     * Configset the staging core is built from. A constant, not the live core's
     * name: the image ships exactly one configset ({@code configsets/site_index}
     * in {@code docker/solr/Dockerfile}) while the core name is per-instance
     * ({@code site_index_local}, {@code site_index_hoover}, ...), so deriving
     * one from the other only works on the instances where they happen to
     * match. Override with {@code SOLR_CONFIGSET} if an instance ships its own.
     */
    private static final String DEFAULT_CONFIGSET = "site_index";

    /**
     * A category may shrink this much between the live index and the freshly
     * built one before the run refuses to publish. Content does legitimately
     * move -- records get merged, withdrawn, recategorised -- so an exact
     * match would fail nightly for no reason; losing a fifth of a category
     * overnight is the shape of a broken import, not of curation.
     */
    private static final double MAX_CATEGORY_SHRINK = 0.20;

    /** Solr-admin operations against the live (currently published) core. */
    private SolrAdminClient live;
    /** Same, against the core this run builds into. */
    private SolrAdminClient staging;

    private String resumeFrom;  // null = start from scratch
    private boolean noClean;    // true = continue into the existing staging core

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
            live = SolrAdminClient.fromEnvOrProperties();
            String stagingCore = live.core() + STAGING_SUFFIX;
            staging = live.forCore(stagingCore);

            List<Batch> plan = plan();
            logBanner(plan);

            prepareStagingCore(stagingCore);
            solr = newSolrClient(staging);

            for (int i = 0; i < plan.size(); i++) {
                Batch batch = plan.get(i);
                logger.info("=== batch '{}' ({} steps) ===", batch.name(), batch.steps().size());
                for (Step step : batch.steps()) {
                    runStep(step, solr);
                }
                // Release Lucene state between batches, except after the
                // last one — there's no further entity that would benefit.
                if (i + 1 < plan.size()) staging.reloadCore();
            }

            logger.info("Committing");
            solr.commit();
            logger.info("All batches complete.");

            publish(stagingCore);
            return 0;
        } catch (Exception e) {
            logger.error("Reindex failed", e);
            return 1;
        } finally {
            if (solr != null) try { solr.close(); } catch (Exception ignored) { }
            HibernateUtil.closeSession();
        }
    }

    // ---------- staging / publish -------------------------------------------

    /**
     * Get the staging core ready to be indexed into.
     *
     * <p>A normal run recreates it, which is where the run's clean semantics
     * now live: a core created from the configset starts empty, so no delete
     * is needed and the live index is never touched. A resumed run keeps
     * whatever the failed run left there, which is the point of resuming.
     *
     * <p>The directory is computed, never derived from the name. A SWAP
     * exchanges core names and leaves the directories where they are, so after
     * one publish the core named {@code site_index} occupies the directory
     * {@code site_index_staging}. CoreAdmin CREATE defaults instanceDir to
     * {@code $SOLR_HOME/<name>}, which at that point is the live index's own
     * directory -- the second nightly run would have tried to build on top of
     * production. It failed only because the live core held Lucene's write
     * lock. So the run asks Solr where the live core actually is and builds in
     * the other of the two directories, refusing outright if those turn out to
     * be the same place.
     */
    private void prepareStagingCore(String stagingCore) throws Exception {
        String liveDir = live.instanceDir(live.core());
        String stagingDir = siblingDir(liveDir, live.core(), stagingCore);
        String existingStagingDir = staging.instanceDir(stagingCore);
        boolean reuse = resumeFrom != null || noClean;

        // The guard that matters. Everything below either deletes a directory
        // or opens an IndexWriter on one; doing either to the live index would
        // destroy the thing this whole staging scheme exists to protect.
        if (stagingDir != null && stagingDir.equals(liveDir)) {
            throw new IllegalStateException(
                "Refusing to build: the staging directory resolved to the live core's own directory ("
                + liveDir + "). Solr's core layout is not what this expects; inspect "
                + "admin/cores?action=STATUS before rerunning.");
        }
        if (!existingStagingDir.isBlank() && existingStagingDir.equals(liveDir)) {
            throw new IllegalStateException(
                "Refusing to build: a core named '" + stagingCore + "' is serving the live core's "
                + "directory (" + liveDir + "). Unloading it would delete the live index.");
        }

        if (reuse) {
            if (existingStagingDir.isBlank()) {
                throw new IllegalStateException(
                    "resumeFrom/noClean asked to continue into '" + stagingCore + "', but no such core exists. "
                    + "There is nothing to resume; rerun without those flags to build from scratch.");
            }
            logger.info("Continuing into existing staging core '{}' at {} (resumeFrom={}, noClean={})",
                stagingCore, existingStagingDir, resumeFrom, noClean);
            return;
        }

        // Unload whether or not STATUS admitted to the core existing. A CREATE
        // that failed partway registers under initFailures instead of status,
        // so it is invisible to instanceDir() yet still holds the name -- that
        // is exactly the state a previously failed run leaves behind.
        staging.unloadCoreQuietly(stagingCore, true);
        staging.createCore(stagingCore, configSet(), stagingDir);
        // Belt and braces: the directory can outlive the core (an unload that
        // only deregistered, a directory Solr never owned), and CREATE happily
        // adopts an existing index. The run must start from nothing, and this
        // is provably not the live core.
        staging.deleteAll();
        logger.info("Staging core '{}' ready (empty) at {}", stagingCore, stagingDir);
    }

    /**
     * The other of the two directories the live/staging pair alternates
     * between, given where the live core currently is.
     *
     * <p>Null when Solr reports no live core at all -- the bootstrap case,
     * where letting CREATE pick its own default is right and there is no
     * index to endanger.
     */
    static String siblingDir(String liveDir, String liveCore, String stagingCore) {
        if (liveDir == null || liveDir.isBlank()) {return null;}
        String dir = liveDir;
        while (dir.endsWith("/")) {dir = dir.substring(0, dir.length() - 1);}
        int cut = dir.lastIndexOf('/');
        String parent = cut < 0 ? "" : dir.substring(0, cut);
        String base = dir.substring(cut + 1);
        // Anything that is not the staging name is treated as the live side,
        // so an unrecognised directory still yields the staging name rather
        // than handing back the directory we were given.
        String other = base.equals(stagingCore) ? liveCore : stagingCore;
        return parent.isEmpty() ? other : parent + "/" + other;
    }

    /** {@code SOLR_CONFIGSET} wins, else {@link #DEFAULT_CONFIGSET}. */
    private static String configSet() {
        String fromEnv = System.getenv("SOLR_CONFIGSET");
        return fromEnv == null || fromEnv.isBlank() ? DEFAULT_CONFIGSET : fromEnv.trim();
    }

    /**
     * Publish the staging index: check it, then exchange the two core names.
     *
     * <p>The check is the whole reason a swap is safe to automate. Publishing
     * atomically means a half-built index goes live atomically too, so the
     * run compares what it built against what is currently serving and
     * refuses rather than swap something visibly worse. A failed check leaves
     * the live core untouched and the staging core intact for inspection.
     */
    private void publish(String stagingCore) throws Exception {
        Map<String, Long> before = live.categoryCounts();
        Map<String, Long> after  = staging.categoryCounts();
        logger.info("Category counts — live: {}", before);
        logger.info("Category counts — staging: {}", after);

        List<String> problems = compareCategories(before, after);
        if (!problems.isEmpty()) {
            throw new RuntimeException(
                "Refusing to publish: the new index looks worse than the live one. "
                + String.join("; ", problems)
                + ". The live index is untouched; '" + stagingCore + "' is left in place to inspect.");
        }

        live.swapCores(live.core(), stagingCore);
        logger.info("Published: '{}' is now live; the previous index is parked at '{}' for rollback "
            + "(swap the two names back to undo).", stagingCore, stagingCore);
    }

    /**
     * Reasons not to publish, as human-readable sentences. Empty means go.
     *
     * <p>Three things are checked, in increasing subtlety: the new index has
     * documents at all; no category the live index had has vanished; and no
     * surviving category has shrunk past {@link #MAX_CATEGORY_SHRINK}.
     *
     * <p>A category appearing for the first time is never a problem -- that is
     * what shipping a new facet looks like. An empty live index is not a
     * problem either: the first run after a restore has nothing to compare
     * against, and blocking it would make the gate unbootstrappable.
     */
    static List<String> compareCategories(Map<String, Long> before, Map<String, Long> after) {
        var problems = new ArrayList<String>();
        long total = after.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            problems.add("the new index is empty");
            return problems;
        }
        if (before.isEmpty()) {
            logger.warn("Live index reports no documents; publishing without a comparison.");
            return problems;
        }
        for (Map.Entry<String, Long> e : before.entrySet()) {
            long was = e.getValue();
            long now = after.getOrDefault(e.getKey(), 0L);
            if (now == 0) {
                problems.add("category '" + e.getKey() + "' went from " + was + " documents to none");
            } else if (was > 0 && (double) (was - now) / was > MAX_CATEGORY_SHRINK) {
                problems.add(String.format("category '%s' shrank %.0f%% (%d -> %d)",
                    e.getKey(), 100.0 * (was - now) / was, was, now));
            }
        }
        return problems;
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
            case DIH  -> staging.runDihImport(step.entity());
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

    /**
     * SolrJ client for the core the given admin client acts on. Derived from
     * the admin client rather than rebuilt from properties so the two cannot
     * disagree about which core they are talking to -- the admin client also
     * honours the {@code SOLR}/{@code CORE} environment variables, which a
     * properties-only URL would ignore.
     */
    private SolrClient newSolrClient(SolrAdminClient forClient) {
        return new HttpSolrClient.Builder(forClient.coreBaseUrl()).build();
    }
}
