package org.zfin.solr.indexer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central registry of Java-backed {@link SolrIndexer}s. Both
 * {@link SolrIndexerJob} (which runs a subset for ad-hoc reindexing) and
 * {@link SolrReindexOrchestrator} (which interleaves them with DIH entities
 * during a full reindex) look entities up here, so the set of migrated
 * entities lives in exactly one place.
 *
 * <p>Adding a migrated entity is two lines: add a new
 * {@link SolrIndexer} implementation and a {@code register(...)} call in
 * the static initializer below. The orchestrator's BATCHES table separately
 * tracks which entities still use DIH vs. which use the registry — flip
 * one entry in BATCHES from {@code DIH} to {@code JAVA} at the same time.
 */
public final class IndexerRegistry {

    // LinkedHashMap so iteration order matches registration order — useful
    // when ad-hoc tooling runs every indexer without specifying an order.
    private static final Map<String, SolrIndexer> INDEXERS = new LinkedHashMap<>();

    static {
        register(new LabIndexer());
    }

    private IndexerRegistry() { }

    private static void register(SolrIndexer i) {
        INDEXERS.put(i.name(), i);
    }

    public static SolrIndexer get(String name) {
        return INDEXERS.get(name);
    }

    public static Collection<String> names() {
        return INDEXERS.keySet();
    }

    public static Collection<SolrIndexer> all() {
        return INDEXERS.values();
    }
}
