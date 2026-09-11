package org.zfin.solr.indexer;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The gate that decides whether a freshly built index is allowed to replace
 * the live one (ZFIN-10497).
 *
 * <p>Worth its own test because the swap is atomic and automated: once
 * {@code publish()} runs there is no window in which a human notices that
 * half the index is missing. Before staging existed, a failed import was
 * loud -- the site was visibly empty. Now the only thing standing between a
 * broken import and a broken site is this comparison, so its edge cases are
 * pinned rather than left to read correctly.
 */
public class SolrReindexPublishGateTest {

    private static Map<String, Long> counts(Object... pairs) {
        var m = new LinkedHashMap<String, Long>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            m.put((String) pairs[i], ((Number) pairs[i + 1]).longValue());
        }
        return m;
    }

    private static List<String> gate(Map<String, Long> before, Map<String, Long> after) {
        return SolrReindexOrchestrator.compareCategories(before, after);
    }

    @Test
    public void publishesAnIndexThatMatchesTheLiveOne() {
        assertEquals(List.of(),
            gate(counts("Fish", 1000, "Publication", 500),
                 counts("Fish", 1000, "Publication", 500)));
    }

    @Test
    public void publishesThroughOrdinaryChurn() {
        // Records get merged, withdrawn and recategorised every day. A gate
        // that demanded equality would block every night for no reason.
        assertEquals(List.of(),
            gate(counts("Fish", 1000, "Publication", 500),
                 counts("Fish", 1040, "Publication", 480)));
    }

    @Test
    public void refusesAnEmptyIndex() {
        List<String> problems = gate(counts("Fish", 1000), counts());
        assertEquals(1, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("empty"));
    }

    @Test
    public void refusesWhenACategoryDisappears() {
        // The exact shape of the reported bug: one entity's import fails, so
        // its category is absent while everything else looks healthy.
        List<String> problems = gate(counts("Fish", 1000, "Expression", 900),
                                     counts("Fish", 1000));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("Expression"));
        assertTrue(problems.get(0), problems.get(0).contains("none"));
    }

    @Test
    public void refusesACategoryThatShrankPastTheThreshold() {
        // A partial import -- docs present, but far fewer than yesterday.
        List<String> problems = gate(counts("Fish", 1000), counts("Fish", 700));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("Fish"));
    }

    @Test
    public void toleratesAShrinkUpToTheThreshold() {
        // 20% exactly is allowed; the check is "more than", so the boundary
        // stays on the publishing side of the line.
        assertEquals(List.of(), gate(counts("Fish", 1000), counts("Fish", 800)));
        assertEquals(1, gate(counts("Fish", 1000), counts("Fish", 799)).size());
    }

    @Test
    public void allowsBrandNewCategories() {
        // Shipping a new facet must not look like a failure.
        assertEquals(List.of(),
            gate(counts("Fish", 1000),
                 counts("Fish", 1000, "ZIRC Line Submission", 12)));
    }

    @Test
    public void publishesWhenThereIsNothingToCompareAgainst() {
        // First run after a restore into an empty core: refusing here would
        // make the gate unbootstrappable, since nothing could ever populate
        // the live index it insists on comparing to.
        assertEquals(List.of(), gate(counts(), counts("Fish", 1000)));
    }

    @Test
    public void reportsEveryFailingCategoryAtOnce() {
        // One run, one log line per problem -- so a broken night is diagnosed
        // from the failure message instead of by rerunning to find the next.
        List<String> problems = gate(counts("Fish", 1000, "Expression", 900, "Publication", 500),
                                     counts("Fish", 100, "Publication", 500));
        assertEquals(2, problems.size());
    }
}
