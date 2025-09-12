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
}
