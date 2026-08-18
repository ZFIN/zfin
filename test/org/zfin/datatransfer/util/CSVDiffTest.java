package org.zfin.datatransfer.util;


import org.apache.commons.csv.CSVRecord;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSVDiffTest {

    private Path testDirectory;
    private String prefix;
    private CSVDiff diffTool;

    @Before
    public void setUp() throws IOException {
        this.testDirectory = Files.createTempDirectory("csvdiff");
        this.prefix = new File(testDirectory.toFile(), "test").toString();
        this.diffTool = new CSVDiff(prefix,
                new String[]{"fname","lname"},
                new String[]{"date"});
    }

    /**
     *
     * Set up a "CSVDiff" instance using data from historical reports:
     *
     *
     */
    @Test
    public void testCSVDiffWithOneAddition() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,2020-01-01
                        jane,smith,2020-02-01""",
                        """
                        fname,lname,date
                        john,doe,2020-01-01
                        jane,smith,1999-12-31
                        jane,doe,2020-03-01""");

        List<File> subsets = diffTool.process(beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());
        assertEquals(subsets.size(), 4);
        // expect 4 subsets: updated_1, updated_2, deletes, adds
        List<String> adds = Files.readAllLines(subsets.get(3).toPath());
        List<String> deletes = Files.readAllLines(subsets.get(2).toPath());
        List<String> updated2 = Files.readAllLines(subsets.get(1).toPath());
        List<String> updated1 = Files.readAllLines(subsets.get(0).toPath());

        //remove header lines
        adds.remove(0);
        deletes.remove(0);
        updated1.remove(0);
        updated2.remove(0);

        assertEquals(adds.size(), 1);
        assertEquals(deletes.size(), 0);
        assertEquals(updated1.size(), 0);
        assertEquals(updated2.size(), 0);

        //Should be one addition: jane,doe,2020-03-01
        assertTrue(adds.contains("jane,doe,2020-03-01"));
    }

    @Test
    public void testCSVDiffWithOneDeletion() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,2020-01-01
                        jane,smith,2020-02-01""",
                """
                        fname,lname,date
                        john,doe,2020-01-01""");

        List<File> subsets = diffTool.process(beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());
        assertEquals(subsets.size(), 4);
        // expect 4 subsets: updated_1, updated_2, deletes, adds
        List<String> adds = Files.readAllLines(subsets.get(3).toPath());
        List<String> deletes = Files.readAllLines(subsets.get(2).toPath());
        List<String> updated2 = Files.readAllLines(subsets.get(1).toPath());
        List<String> updated1 = Files.readAllLines(subsets.get(0).toPath());

        //remove header lines
        adds.remove(0);
        deletes.remove(0);
        updated1.remove(0);
        updated2.remove(0);

        assertEquals(adds.size(), 0);
        assertEquals(deletes.size(), 1);
        assertEquals(updated1.size(), 0);
        assertEquals(updated2.size(), 0);

        //Should be one deletion: jane,smith,2020-02-01
        assertTrue(deletes.contains("jane,smith,2020-02-01"));
    }

    @Test
    public void testCSVDiffWithOneUpdate() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,25
                        jane,smith,2020-02-01,33""",
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,25
                        jane,smith,2020-03-01,44""");

        List<File> subsets = diffTool.process(beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());
        assertEquals(subsets.size(), 4);
        // expect 4 subsets: updated_1, updated_2, deletes, adds
        List<String> adds = Files.readAllLines(subsets.get(3).toPath());
        List<String> deletes = Files.readAllLines(subsets.get(2).toPath());
        List<String> updated2 = Files.readAllLines(subsets.get(1).toPath());
        List<String> updated1 = Files.readAllLines(subsets.get(0).toPath());

        //remove header lines
        adds.remove(0);
        deletes.remove(0);
        updated1.remove(0);
        updated2.remove(0);

        assertEquals(adds.size(), 0);
        assertEquals(deletes.size(), 0);
        assertEquals(updated1.size(), 1);
        assertEquals(updated2.size(), 1);

        //Should be one update: jane,smith,2020-02-01 -> jane,smith,2020-03-01
        assertTrue(updated1.contains("jane,smith,2020-02-01,33"));
        assertTrue(updated2.contains("jane,smith,2020-03-01,44"));
    }

    @Test
    public void testCSVDiffWithOneUpdateAndProcessByMap() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,25
                        jane,smith,2020-02-01,33""",
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,25
                        jane,smith,2020-03-01,44""");

        Map<String, List<CSVRecord>> subsets = diffTool.processToMap(beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());
        assertEquals(subsets.size(), 5);
        // expect 5 subsets: summary, updated_1, updated_2, deletes, adds
        List<CSVRecord> adds = subsets.get("added");
        List<CSVRecord> deletes = subsets.get("deleted");
        List<CSVRecord> updated2 = subsets.get("updated2");
        List<CSVRecord> updated1 = subsets.get("updated1");
        assertEquals(adds.size(), 0);

        Map<String, String> map = updated1.get(0).toMap();

        //Should be one update: jane,smith,2020-02-01 -> jane,smith,2020-03-01
        assertEquals(map.get("fname"), "jane");
        assertEquals(map.get("lname"), "smith");
        assertEquals(map.get("date"), "2020-02-01");
        assertEquals(map.get("age"), "33");
    }

    @Test
    public void testCSVUpdateIgnoresFilesWhereConfigured() throws IOException {
        //we don't care about date changes, so this should be treated as no change
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,1944-11-21""",
                """
                        fname,lname,date
                        john,doe,2020-01-01""");

        List<File> subsets = diffTool.process(beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());
        assertEquals(subsets.size(), 4);
        // expect 4 subsets: updated_1, updated_2, deletes, adds
        List<String> adds = Files.readAllLines(subsets.get(3).toPath());
        List<String> deletes = Files.readAllLines(subsets.get(2).toPath());
        List<String> updated2 = Files.readAllLines(subsets.get(1).toPath());
        List<String> updated1 = Files.readAllLines(subsets.get(0).toPath());

        //remove header lines
        adds.remove(0);
        deletes.remove(0);
        updated1.remove(0);
        updated2.remove(0);

        assertEquals(adds.size(), 0);
        assertEquals(deletes.size(), 0);
        assertEquals(updated1.size(), 0);
        assertEquals(updated2.size(), 0);
    }

    private Tuple2<File,File> generateTestData(String beforeContent, String afterContent) {
        //create file in tempDir
        Path beforeFile = testDirectory.resolve("before.csv");
        Path afterFile = testDirectory.resolve("after.csv");

        try {
            Files.writeString(beforeFile, beforeContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.writeString(afterFile, afterContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Tuple2<>(beforeFile.toFile(), afterFile.toFile());
    }

    /**
     * ZFIN-8948: CSVDiff assumes the composite key is UNIQUE within each file. When two rows
     * share a key, results are wrong in two compounding ways:
     *
     *   1. findUpdatedRecords / findUpdatedRecordsOnlyInIgnoredColumns build
     *      Map<String, CSVRecord> -- one record per key -- so only ONE member of a key group
     *      is ever reported.
     *   2. removeRecordsFromLists then removes EVERY row sharing that key from the working
     *      lists, so the unreported members never fall through to deletes/adds either.
     *
     * Net effect: rows silently disappear from the output, and the accounting invariant
     *   retained + ignoredUpdated + updated + deleted == beforeCount
     * no longer holds. Because the counts get *smaller*, the summary looks healthier than
     * before, which is the dangerous part.
     *
     * Below: 2 before + 2 after rows all sharing key (john,doe), differing only in the
     * IGNORED `date` column. Correct output is 2 ignored-updates per side and no
     * deletes/adds. Actual today: 1 per side, and one before-row appears nowhere.
     *
     * This is what the GO loads depend on. Their csvDiff key deliberately excludes
     * `protein_acc` so UniProt isoform reassignment reads as an update instead of a
     * delete+add, and that makes keys non-unique: 3,175 colliding key groups over 14,037 rows
     * on the 2026.07.05.1 GOA snapshot. Before this was fixed, that configuration would have
     * silently lost rows. See server_apps/DB_maintenance/gafLoad/mgte_csvdiff.sh.
     */
    @Test
    public void testDuplicateCompositeKeysAreAllReported() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,2020-01-01
                        john,doe,2020-01-02""",
                """
                        fname,lname,date
                        john,doe,2021-01-01
                        john,doe,2021-01-02""");

        Map<String, List<CSVRecord>> subsets = diffTool.processToMap(
                beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());

        // Both rows differ only in an ignored column, so both are ignored-updates.
        assertCounts(subsets, 0, 2, 2, 0, 0, 0, 0);
    }

    /**
     * ZFIN-8948: asymmetric duplicate key groups. 3 before-rows and 2 after-rows share a key and
     * differ only in the ignored column, so 2 pair off as ignored-updates and the third before-row
     * is a genuine delete. Guards the "leftovers must fall through" half of the fix -- the old
     * key-based removal swept unpaired members out of the working lists entirely.
     */
    @Test
    public void testDuplicateKeysWithUnevenGroupSizes() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,2020-01-01
                        john,doe,2020-01-02
                        john,doe,2020-01-03""",
                """
                        fname,lname,date
                        john,doe,2021-01-01
                        john,doe,2021-01-02""");

        Map<String, List<CSVRecord>> subsets = diffTool.processToMap(
                beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());

        assertCounts(subsets, 0, 2, 2, 0, 0, 1, 0);
    }

    /**
     * ZFIN-8948: duplicate FULL-record rows exercise findRetainedRecords, which had the same
     * one-record-per-key flaw. Two byte-identical before-rows against one identical after-row is
     * 1 retained + 1 genuine delete. The old code reported 2 retained (more than existed on the
     * after side) and lost the delete.
     */
    @Test
    public void testDuplicateIdenticalRowsRetainOneAndDeleteTheRest() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date
                        john,doe,2020-01-01
                        john,doe,2020-01-01""",
                """
                        fname,lname,date
                        john,doe,2020-01-01""");

        Map<String, List<CSVRecord>> subsets = diffTool.processToMap(
                beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());

        assertCounts(subsets, 1, 0, 0, 0, 0, 1, 0);
    }

    /**
     * ZFIN-8948: a genuine update living inside a duplicate key group. One pair is byte-identical
     * (retained), the other differs in a non-ignored column (a real update). Both must be
     * classified independently rather than one shadowing the other.
     */
    @Test
    public void testGenuineUpdateWithinDuplicateKeyGroup() throws IOException {
        Tuple2<File, File> beforeAfterFiles = generateTestData(
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,25
                        john,doe,2020-01-02,30""",
                """
                        fname,lname,date,age
                        john,doe,2020-01-01,26
                        john,doe,2020-01-02,30""");

        Map<String, List<CSVRecord>> subsets = diffTool.processToMap(
                beforeAfterFiles.v1().getAbsolutePath(), beforeAfterFiles.v2().getAbsolutePath());

        assertCounts(subsets, 1, 0, 0, 1, 1, 0, 0);
    }

    /**
     * Assert every bucket count, and — regardless of the expectations — that the accounting
     * invariant holds on both sides:
     *   retained + ignoredUpdated + updated + deleted == beforeCount
     *   retained + ignoredUpdated + updated + added   == afterCount
     * Nothing may vanish. This is the assertion that would have caught the original defect,
     * since its symptom was counts getting smaller rather than wrong-looking.
     */
    private void assertCounts(Map<String, List<CSVRecord>> subsets, int expectedRetained,
                              int expectedIgnored1, int expectedIgnored2, int expectedUpdated1,
                              int expectedUpdated2, int expectedDeleted, int expectedAdded) {
        Map<String, String> summary = subsets.get("summary").get(0).toMap();
        int beforeCount = Integer.parseInt(summary.get("beforeCount"));
        int afterCount  = Integer.parseInt(summary.get("afterCount"));
        int retained    = Integer.parseInt(summary.get("retainedCount"));
        int ignored1    = Integer.parseInt(summary.get("ignoredCount1"));
        int ignored2    = Integer.parseInt(summary.get("ignoredCount2"));
        int updated1    = Integer.parseInt(summary.get("updated1Count"));
        int updated2    = Integer.parseInt(summary.get("updated2Count"));
        int deleted     = Integer.parseInt(summary.get("deletedCount"));
        int added       = Integer.parseInt(summary.get("addedCount"));

        assertEquals("retained", expectedRetained, retained);
        assertEquals("ignoredUpdated1", expectedIgnored1, ignored1);
        assertEquals("ignoredUpdated2", expectedIgnored2, ignored2);
        assertEquals("updated1", expectedUpdated1, updated1);
        assertEquals("updated2", expectedUpdated2, updated2);
        assertEquals("deleted", expectedDeleted, deleted);
        assertEquals("added", expectedAdded, added);

        assertEquals("accounting invariant: every before-row classified exactly once",
                beforeCount, retained + ignored1 + updated1 + deleted);
        assertEquals("accounting invariant: every after-row classified exactly once",
                afterCount, retained + ignored2 + updated2 + added);
    }
}
