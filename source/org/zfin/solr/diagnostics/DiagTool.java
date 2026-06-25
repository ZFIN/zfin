package org.zfin.solr.diagnostics;

/**
 * One diagnostic sub-tool inside the {@link SolrCharacterizer} suite —
 * dumps docs, enumerates fields, captures terms, replays queries, etc.
 *
 * <p>Each tool is independently runnable (via the {@code solrDiag}
 * gradle task with {@code -PdiagArgs="<name> ..."}) and composable
 * into the full characterization run by {@link SolrCharacterizer}'s
 * {@code all} subcommand.
 *
 * <p>Tools receive a shared {@link DiagContext} that owns the
 * {@link org.apache.solr.client.solrj.SolrClient} instances and the
 * output-directory plumbing. Tool-specific arguments come in via the
 * positional {@code args} array — slot 0 is the subcommand name and
 * has already been consumed by the dispatcher, so {@code args[0]} in
 * the tool's view is the first user-provided argument.
 */
public interface DiagTool {

    /** Subcommand name (matches the verb users type on the CLI). */
    String name();

    /** One-line usage hint shown by {@link SolrCharacterizer#main}. */
    String usage();

    /**
     * Execute the tool against the running Solr.
     *
     * @param ctx  shared SolrJ clients + output helpers
     * @param args tool-specific positional arguments (subcommand name already stripped)
     */
    void run(DiagContext ctx, String[] args) throws Exception;
}
