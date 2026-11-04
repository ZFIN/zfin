package org.zfin.solr.admin;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Retention rules for Solr snapshot pruning.
 *
 * <p>These pin the two failures of the {@code cleanup-solr-backup-files} ant target this replaces:
 * it deleted by age alone (so a stalled backup job would eventually leave nothing to restore from),
 * and it deleted <em>everything</em> under the directory rather than just snapshots.
 */
public class SolrSnapshotCleanupTest {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm");

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File location;

    @Before
    public void setUp() throws IOException {
        location = tmp.newFolder("coral");
    }

    /** A snapshot dir named the way backup-solr-index stamps them, `age` days back, with content. */
    private File snapshot(int ageInDays) throws IOException {
        String name = SolrSnapshotCleanup.SNAPSHOT_PREFIX
            + LocalDateTime.now().minusDays(ageInDays).format(STAMP);
        File dir = new File(location, name);
        assertTrue(new File(dir, "index").mkdirs());
        assertTrue(new File(dir, "index/segments_1").createNewFile());
        return dir;
    }

    private SolrSnapshotCleanup cleanup(String... extraArgs) {
        String[] args = new String[extraArgs.length + 2];
        args[0] = "--location";
        args[1] = location.getAbsolutePath();
        System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);

        SolrSnapshotCleanup tool = new SolrSnapshotCleanup();
        tool.parseArgs(args);
        return tool;
    }

    @Test
    public void deletesSnapshotsPastTheRetentionWindow() throws IOException {
        File stale = snapshot(40);
        File alsoStale = snapshot(30);
        snapshot(1);
        snapshot(2);

        assertEquals(0, cleanup().execute());

        assertFalse("40-day-old snapshot should be gone", stale.exists());
        assertFalse("30-day-old snapshot should be gone", alsoStale.exists());
    }

    @Test
    public void keepsSnapshotsInsideTheRetentionWindow() throws IOException {
        File fresh = snapshot(1);
        File recent = snapshot(13);

        assertEquals(0, cleanup().execute());

        assertTrue(fresh.exists());
        assertTrue("13 days is inside the 14-day window", recent.exists());
    }

    /**
     * The regression that motivated the rewrite: if backups stop running, every snapshot eventually
     * ages out and the old target would delete the lot, leaving no index to restore.
     */
    @Test
    public void neverDeletesEverythingEvenWhenAllSnapshotsAreStale() throws IOException {
        File oldest = snapshot(365);
        File middle = snapshot(200);
        File newest = snapshot(100);

        assertEquals(0, cleanup().execute());

        assertTrue("newest must survive regardless of age", newest.exists());
        assertTrue("second-newest must survive as the fallback", middle.exists());
        assertFalse(oldest.exists());
    }

    @Test
    public void keepMinIsConfigurable() throws IOException {
        snapshot(365);
        File newest = snapshot(100);

        assertEquals(0, cleanup("--keep-min", "1").execute());

        assertTrue(newest.exists());
        assertEquals(1, location.list().length);
    }

    /** The old macro globbed `**&#47;*`; this one must ignore anything that is not a snapshot. */
    @Test
    public void leavesNonSnapshotEntriesAlone() throws IOException {
        File strayFile = new File(location, "README.txt");
        assertTrue(strayFile.createNewFile());
        File strayDir = new File(location, "not-a-snapshot");
        assertTrue(strayDir.mkdirs());
        assertTrue(new File(strayDir, "keep-me").createNewFile());
        // Old enough to be swept if age were the only criterion.
        assertTrue(strayFile.setLastModified(System.currentTimeMillis() - 400L * 86_400_000L));
        assertTrue(strayDir.setLastModified(System.currentTimeMillis() - 400L * 86_400_000L));

        snapshot(1);
        snapshot(2);
        snapshot(300);

        assertEquals(0, cleanup().execute());

        assertTrue("a plain file must not be touched", strayFile.exists());
        assertTrue("an unrelated directory must not be touched", strayDir.exists());
        assertTrue(new File(strayDir, "keep-me").exists());
    }

    @Test
    public void dryRunDeletesNothing() throws IOException {
        File stale = snapshot(400);
        snapshot(1);
        snapshot(2);

        assertEquals(0, cleanup("--dry-run").execute());

        assertTrue("dry run must leave the stale snapshot in place", stale.exists());
    }

    @Test
    public void missingDirectoryIsNotAnError() {
        SolrSnapshotCleanup tool = new SolrSnapshotCleanup();
        tool.parseArgs(new String[]{"--location", new File(location, "never-backed-up").getAbsolutePath()});

        // A fresh instance has no snapshot dir yet; failing would break the Jenkins job for no reason.
        assertEquals(0, tool.execute());
    }

    @Test
    public void emptyDirectoryIsNotAnError() {
        assertEquals(0, cleanup().execute());
    }

    @Test
    public void keepDaysIsConfigurable() throws IOException {
        File tenDays = snapshot(10);
        snapshot(1);
        snapshot(2);

        assertEquals(0, cleanup("--keep-days", "5").execute());

        assertFalse("10 days is outside a 5-day window", tenDays.exists());
    }

    /**
     * Age comes from the name, not the mtime: a snapshot copied in by rsync/getsolr carries a fresh
     * mtime that would otherwise make a year-old index look brand new.
     */
    @Test
    public void prefersTheNameStampOverMtime() throws IOException {
        File stale = snapshot(400);
        assertTrue(stale.setLastModified(System.currentTimeMillis()));
        snapshot(1);
        snapshot(2);

        assertEquals(0, cleanup().execute());

        assertFalse("freshly-touched but old-named snapshot should still be pruned", stale.exists());
    }

    /** Snapshots named some other way have no stamp to read, so mtime is the only signal left. */
    @Test
    public void fallsBackToMtimeForUnparseableNames() throws IOException {
        File odd = new File(location, SolrSnapshotCleanup.SNAPSHOT_PREFIX + "manual-backup");
        assertTrue(odd.mkdirs());
        assertTrue(odd.setLastModified(System.currentTimeMillis() - 400L * 86_400_000L));
        snapshot(1);
        snapshot(2);

        assertEquals(0, cleanup().execute());

        assertFalse(odd.exists());
    }
}
