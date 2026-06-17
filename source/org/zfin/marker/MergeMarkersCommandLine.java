package org.zfin.marker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Command-line front end for the marker merge (registered as {@code merge-markers} with
 * {@code requiresDatabase=true}, so it runs inside {@code ToolBootstrap.run}'s managed transaction).
 * The actual merge lives in {@link MarkerMergeService}, which is shared with the webapp
 * ({@code MergeMarkerController}) so there is a single, equivalence-tested implementation.
 *
 * <pre>{@code
 *   zfin-util merge-markers <zdbIdToDelete> <zdbIdToMergeInto>              # execute (commit)
 *   zfin-util merge-markers <zdbIdToDelete> <zdbIdToMergeInto> --dry-run   # log SQL, roll back
 *   zfin-util merge-markers <zdbIdToDelete> <zdbIdToMergeInto> --skip-regen
 * }</pre>
 *
 * In dry-run mode the merge is executed inside the transaction (so the generated SQL reflects the
 * real, consistent state) and then rolled back; regen is skipped (dry-run implies --skip-regen).
 * Sequence values consumed by {@code get_id(...)} are not reclaimed by the rollback.
 */
public class MergeMarkersCommandLine {

    private static final Logger LOG = LogManager.getLogger(MergeMarkersCommandLine.class);

    public static void main(String[] args) {
        List<String> positional = new ArrayList<>();
        boolean dryRun = false;
        boolean skipRegen = false;
        for (String arg : args) {
            if ("--dry-run".equals(arg) || "-n".equals(arg)) {
                dryRun = true;
            } else if ("--skip-regen".equals(arg)) {
                skipRegen = true;
            } else {
                positional.add(arg);
            }
        }
        if (positional.size() != 2) {
            throw new IllegalArgumentException(
                    "Usage: merge-markers <zdbIdToDelete> <zdbIdToMergeInto> [--dry-run] [--skip-regen]");
        }

        String deleted = positional.get(0);
        String mergedInto = positional.get(1);
        LOG.info("Merging {} into {}{}", deleted, mergedInto, dryRun ? " (DRY RUN)" : "");

        // dry-run implies skip-regen (regen is a non-transactional recompute we don't want to exercise)
        List<String> sql = new MarkerMergeService(deleted, mergedInto, dryRun || skipRegen).merge();

        if (dryRun) {
            HibernateUtil.rollbackTransaction();
            LOG.warn("DRY RUN complete: rolled back. {} statements would have executed:", sql.size());
            for (String stmt : sql) {
                LOG.warn("  {}", stmt);
            }
        } else {
            LOG.info("Merge complete: executed {} statements.", sql.size());
        }
    }
}
