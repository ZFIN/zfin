package org.zfin.solr.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Prune old Solr replication snapshots from an unloads directory.
 *
 * <p>Successor to the {@code cleanup-solr-backup-files} ant target (bugzID 14555, 2016), which the
 * Solr 9 upgrade (ZFIN-10276) removed along with the {@code solr.backup.dir} property it read. That
 * target called a generic {@code remove-old-files} macro that deleted <em>everything</em> under
 * {@code <base>/${DBNAME}/} older than two weeks. It had two problems this replacement fixes:
 *
 * <ul>
 *   <li>It cleaned {@code ${DBNAME}} while {@code backup-solr-index} wrote to {@code ${INSTANCE}} —
 *       different variables. Wherever the two differed (they do on every dev/stage box: DBNAME
 *       {@code zfindb} vs INSTANCE {@code coral}) it pruned a directory nothing wrote to, so
 *       snapshots accumulated regardless.</li>
 *   <li>Age was the only criterion, so a backup job that stopped running for longer than the
 *       retention window would have its last good snapshot deleted, leaving nothing to restore
 *       from.</li>
 * </ul>
 *
 * <p>This tool therefore deletes only directories named {@code snapshot.*} (the layout
 * {@link SolrAdminClient#backup} produces and {@code getLatestSolrIndex} restores from), and always
 * retains the newest {@code --keep-min} of them no matter how old they are.
 *
 * <p>Invoked via the zfin-util launcher; pure filesystem work, so it is registered without a
 * database and runs with no Hibernate/ZfinProperties bootstrap. Defaults come from the environment
 * so a Jenkins job needs no arguments:
 *
 * <pre>
 *   zfin-util solr-cleanup-snapshots
 *   zfin-util solr-cleanup-snapshots --keep-days 30 --dry-run
 *   zfin-util solr-cleanup-snapshots --location /opt/zfin/unloads/solr/zfindb
 * </pre>
 */
public class SolrSnapshotCleanup {

    static final String SNAPSHOT_PREFIX = "snapshot.";

    /** Retention window of the ant target this replaces. */
    static final int DEFAULT_KEEP_DAYS = 14;

    /**
     * Floor on how many snapshots survive regardless of age. Two rather than one so a restore still
     * has a fallback if the newest snapshot turns out to be bad.
     */
    static final int DEFAULT_KEEP_MIN = 2;

    /** Naming pattern {@code backup-solr-index} stamps snapshots with, e.g. snapshot.2026.09.15-14.30. */
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm");

    private File location;
    private int keepDays = DEFAULT_KEEP_DAYS;
    private int keepMin = DEFAULT_KEEP_MIN;
    private boolean dryRun;

    public static void main(String[] args) {
        SolrSnapshotCleanup cleanup = new SolrSnapshotCleanup();
        try {
            if (!cleanup.parseArgs(args)) {
                System.exit(0);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            usage(System.err);
            System.exit(1);
        }
        System.exit(cleanup.execute());
    }

    /** @return false if the caller asked for --help and nothing should run. */
    boolean parseArgs(String[] args) {
        String locationArg = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> {
                    usage(System.out);
                    return false;
                }
                case "--dry-run", "-n" -> dryRun = true;
                case "--location", "-l" -> locationArg = requireValue(args, ++i, "--location");
                case "--keep-days" -> keepDays = requireNonNegativeInt(requireValue(args, ++i, "--keep-days"), "--keep-days");
                case "--keep-min" -> keepMin = requireNonNegativeInt(requireValue(args, ++i, "--keep-min"), "--keep-min");
                default -> throw new IllegalArgumentException("unknown argument '" + args[i] + "'");
            }
        }
        location = new File(locationArg != null ? locationArg : defaultLocation());
        return true;
    }

    /**
     * {@code $SOLR_UNLOADS_PATH/$INSTANCE} — the same directory {@code backup-solr-index} writes to.
     * Read straight from the environment rather than ZfinPropertiesEnum: this utility is registered
     * as not requiring a database, so no properties are loaded by the time it runs.
     */
    private static String defaultLocation() {
        String base = System.getenv("SOLR_UNLOADS_PATH");
        String instance = System.getenv("INSTANCE");
        if (base == null || base.isBlank() || instance == null || instance.isBlank()) {
            throw new IllegalArgumentException(
                "SOLR_UNLOADS_PATH and INSTANCE must both be set to infer the snapshot directory "
                    + "(got SOLR_UNLOADS_PATH='" + base + "', INSTANCE='" + instance + "'); "
                    + "pass --location <dir> instead");
        }
        return base + File.separator + instance;
    }

    int execute() {
        System.out.println("Solr snapshot cleanup");
        System.out.println("  location:  " + location);
        System.out.println("  keep-days: " + keepDays);
        System.out.println("  keep-min:  " + keepMin);
        if (dryRun) {
            System.out.println("  DRY RUN — nothing will be deleted");
        }

        if (!location.isDirectory()) {
            // Not an error: a fresh instance that has never run a backup has no directory yet, and
            // failing here would break the Jenkins job for no reason.
            System.out.println("No snapshot directory at " + location + "; nothing to do.");
            return 0;
        }

        List<File> snapshots = listSnapshots(location);
        if (snapshots.isEmpty()) {
            System.out.println("No snapshot.* directories found; nothing to do.");
            return 0;
        }

        // Newest first, so the keep-min floor is simply the head of the list.
        snapshots.sort(Comparator.comparing(SolrSnapshotCleanup::snapshotInstant).reversed());
        Instant cutoff = Instant.now().minus(Duration.ofDays(keepDays));

        List<File> doomed = new ArrayList<>();
        for (int i = 0; i < snapshots.size(); i++) {
            File snapshot = snapshots.get(i);
            if (i < keepMin) {
                System.out.printf("  keep (newest %d)  %s%n", keepMin, snapshot.getName());
            } else if (snapshotInstant(snapshot).isAfter(cutoff)) {
                System.out.printf("  keep (recent)    %s%n", snapshot.getName());
            } else {
                doomed.add(snapshot);
            }
        }

        int failed = 0;
        for (File snapshot : doomed) {
            if (dryRun) {
                System.out.printf("  would delete     %s%n", snapshot.getName());
                continue;
            }
            try {
                deleteRecursively(snapshot.toPath());
                System.out.printf("  deleted          %s%n", snapshot.getName());
            } catch (IOException e) {
                // Keep going: one unreadable snapshot should not strand the rest.
                System.err.printf("  FAILED to delete %s: %s%n", snapshot.getName(), e);
                failed++;
            }
        }

        System.out.printf("%d snapshot(s) found, %d %s, %d retained.%n",
            snapshots.size(), doomed.size() - failed, dryRun ? "would be deleted" : "deleted",
            snapshots.size() - doomed.size() + failed);
        return failed == 0 ? 0 : 1;
    }

    private static List<File> listSnapshots(File location) {
        File[] entries = location.listFiles(
            (dir, name) -> name.startsWith(SNAPSHOT_PREFIX) && new File(dir, name).isDirectory());
        return entries == null ? List.of() : new ArrayList<>(List.of(entries));
    }

    /**
     * Age of a snapshot, preferring the timestamp baked into its name over the directory's mtime —
     * an rsync'd or otherwise copied snapshot carries a fresh mtime that would make an old index
     * look new. Falls back to mtime for snapshots named some other way.
     */
    static Instant snapshotInstant(File snapshot) {
        String stamp = snapshot.getName().substring(SNAPSHOT_PREFIX.length());
        try {
            return LocalDateTime.parse(stamp, STAMP).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            return Instant.ofEpochMilli(snapshot.lastModified());
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        try (var walk = Files.walk(path)) {
            // Deepest first, so directories are empty by the time they are removed.
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    private static String requireValue(String[] args, int index, String flag) {
        if (index >= args.length) {
            throw new IllegalArgumentException(flag + " requires a value");
        }
        return args[index];
    }

    private static int requireNonNegativeInt(String value, String flag) {
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " must be a number (got '" + value + "')");
        }
        if (parsed < 0) {
            throw new IllegalArgumentException(flag + " must not be negative (got " + parsed + ")");
        }
        return parsed;
    }

    private static void usage(java.io.PrintStream out) {
        out.println("""
            Usage: zfin-util solr-cleanup-snapshots [options]

            Delete snapshot.* directories older than the retention window, always keeping
            the newest --keep-min of them.

              --location, -l <dir>  snapshot directory (default: $SOLR_UNLOADS_PATH/$INSTANCE)
              --keep-days <n>       delete snapshots older than n days (default: %d)
              --keep-min <n>        always retain the n newest snapshots (default: %d)
              --dry-run, -n         report what would be deleted, delete nothing
              --help, -h            show this message
            """.formatted(DEFAULT_KEEP_DAYS, DEFAULT_KEEP_MIN));
    }
}
