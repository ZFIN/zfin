package org.zfin.solr.indexer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Which directory the reindex builds into (ZFIN-10497).
 *
 * <p>This exists because getting it wrong once nearly destroyed a production
 * index. A CoreAdmin SWAP exchanges core names and leaves the directories
 * alone, so after one publish the core named {@code site_index} occupies the
 * directory {@code site_index_staging}. CREATE defaults its instanceDir to
 * {@code $SOLR_HOME/<name>}, which by then is the live index's own directory:
 * the second nightly run tried to build on top of production and was stopped
 * only by Lucene's write lock.
 *
 * <p>So the rule under test is narrow and absolute: whatever the live core's
 * directory is, the answer is never that directory.
 */
public class SolrStagingDirectoryTest {

    private static final String LIVE = "site_index";
    private static final String STAGING = "site_index_staging";

    private static String sibling(String liveDir) {
        return SolrReindexOrchestrator.siblingDir(liveDir, LIVE, STAGING);
    }

    @Test
    public void buildsBesideTheLiveCoreBeforeAnySwap() {
        assertEquals("/var/solr/data/site_index_staging", sibling("/var/solr/data/site_index"));
    }

    @Test
    public void buildsBackIntoTheOtherDirectoryAfterASwap() {
        // The case that bit us: live is in the *staging-named* directory, so
        // the run has to go back to the live-named one.
        assertEquals("/var/solr/data/site_index", sibling("/var/solr/data/site_index_staging"));
    }

    @Test
    public void alternatesAcrossRepeatedSwaps() {
        String dir = "/var/solr/data/site_index";
        for (int publish = 0; publish < 6; publish++) {
            String next = sibling(dir);
            assertNotEquals("run " + publish + " would have built on the live index", dir, next);
            dir = next;   // the swap makes what we just built the live core
        }
        assertEquals("/var/solr/data/site_index", dir);
    }

    @Test
    public void neverReturnsTheLiveDirectory() {
        for (String liveDir : new String[]{
            "/var/solr/data/site_index",
            "/var/solr/data/site_index_staging",
            "/var/solr/data/site_index/",          // trailing slash
            "/some/other/root/site_index",
        }) {
            assertNotEquals(liveDir, sibling(liveDir));
        }
    }

    @Test
    public void ignoresATrailingSlashRatherThanBuildingAPathFromIt() {
        assertEquals("/var/solr/data/site_index_staging", sibling("/var/solr/data/site_index/"));
    }

    @Test
    public void staysUnderTheSolrHomeItWasGiven() {
        // Never assume /var/solr/data: a dev instance, or a core moved by hand,
        // puts the pair somewhere else entirely.
        assertEquals("/opt/solr/home/site_index_staging", sibling("/opt/solr/home/site_index"));
    }

    @Test
    public void treatsAnUnrecognisedDirectoryAsTheLiveSide() {
        // A core someone relocated: the safe reading is "that is the live one",
        // which yields the staging name and so still differs from what we were
        // given. Handing back the same directory would be the dangerous answer.
        assertEquals("/var/solr/data/site_index_staging", sibling("/var/solr/data/renamed_by_hand"));
    }

    @Test
    public void defersToSolrWhenThereIsNoLiveCore() {
        // Bootstrap: nothing to protect and nothing to compute from, so let
        // CREATE use its own default.
        assertNull(sibling(null));
        assertNull(sibling(""));
    }
}
