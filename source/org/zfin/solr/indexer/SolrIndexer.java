package org.zfin.solr.indexer;

import org.apache.solr.client.solrj.SolrClient;

/**
 * Pushes the documents for one top-level Solr entity into a target core.
 * Replaces the corresponding {@code <entity name="…">} block in the legacy
 * DIH {@code db-data-config.xml}.
 *
 * <p>Migration is piecemeal: a given DIH entity can be implemented by a
 * {@code SolrIndexer} while the rest of the entities remain DIH-driven.
 * The {@link #name()} string is the binding — it matches the entity name
 * that DIH would use, both so operators can swap one for the other and so
 * the in-tree DIH config can be updated by removing the matching block.
 *
 * <p>Implementations are responsible for their own batching and commit
 * cadence. The driver opens the {@link SolrClient} against the right core
 * and is responsible for the final commit once all indexers in a run
 * have completed.
 */
public interface SolrIndexer {

    /** Matches the corresponding DIH entity name in db-data-config.xml. */
    String name();

    /**
     * Read this entity's source data and push it as Solr documents through
     * the given client. The client is already configured for the target core;
     * implementations should call {@code solr.add(doc)} (or the batch form)
     * and leave the final commit to the driver.
     */
    void index(SolrClient solr) throws Exception;
}
