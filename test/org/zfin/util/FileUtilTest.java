package org.zfin.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test class for FileUtil.
 */
public class FileUtilTest {

    private static final File testArchiveDir = new File("test", "test-archive-dir");
    private static final File testLoadDirectory = new File("test", "test-load-dir");
    private File testLoadFile;
    private File testPurgeFile;

    @Before
    public void setUp() {
        TestConfiguration.configure();
        setTestDirectories();
    }

    @After
    public void tearDown() {
        cleanupTestFilesStructure();
    }

    /**
     * Create a single file and archive it. Make sure it moved into the archive directory.
     */
    @Test
    public void archiveFile() {
        File archivedFile = FileUtil.archiveFile(testLoadFile, testArchiveDir);
        File[] files = testArchiveDir.listFiles();
        assertEquals("Number of files", 1, files.length);
        assertEquals("File Name", archivedFile.getName(), files[0].getName());
        assertTrue("Original File still exists", testLoadFile.exists());
    }

    /**
     * Create a single file and archive it. Make sure it moved into the archive directory.
     * Sleep for a second and then archive another file and then purge the first file while the
     * second file does not get purged.
     */
    @Test
    public void purgeArchiveFile() {
        File archivedFile = FileUtil.archiveFile(testLoadFile, testArchiveDir);
        File[] files = testArchiveDir.listFiles();
        assertEquals("Number of files", 1, files.length);
        assertEquals("File Name", archivedFile.getName(), files[0].getName());
        assertTrue("Original File still exists", testLoadFile.exists());

        // create the second file
        testPurgeFile = new File(testLoadDirectory, "test-file-two.txt");
        try {
            Thread.sleep(1000);
            testPurgeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        archivedFile = FileUtil.archiveFile(testPurgeFile, testArchiveDir);
        files = testArchiveDir.listFiles();
        assertEquals("Number of files", 2, files.length);
        FileUtil.purgeArchiveDirectory(testArchiveDir, 990);

        files = testArchiveDir.listFiles();
        assertEquals("Number of files", 1, files.length);
        assertEquals("File Name", archivedFile.getName(), files[0].getName());

    }

    @Ignore
    public void apgFiles() {
        ZfinProperties.setWebRootDirectory("home");
        List<File> apgFiles = FileUtil.countApgFiles();

        assertEquals("Number of apg files", 200, apgFiles.size());
    }

    @Test
    public void fileBuilderFromString() {

        assertNull(FileUtil.createFileFromStrings());
        File file1 = new File("file1");
        assertEquals(file1.getAbsolutePath(),
                FileUtil.createFileFromStrings("file1").getAbsolutePath());
        File file2 = new File("file1" +
                System.getProperty("file.separator") +
                "file2.txt");
        assertEquals(file2.getAbsolutePath(),
                FileUtil.createFileFromStrings("file1", "file2.txt").getAbsolutePath());

        ZfinProperties.setWebRootDirectory(".");
        File testFile1 = FileUtil.createFileFromStrings(ZfinProperties.getWebRootDirectory(),
                "WEB-INF", "conf", "antibody.template");
        File file3 = new File(ZfinProperties.getWebRootDirectory() + System.getProperty("file.separator") +
                "WEB-INF" + System.getProperty("file.separator") +
                "conf" + System.getProperty("file.separator") +
                "antibody.template" + System.getProperty("file.separator"));
        assertEquals(file3.getAbsolutePath(), testFile1.getAbsolutePath());
    }

    @Test
    public void fileFromPaths() {
        File file = FileUtil.createFileFromStrings();
        assertNull(file);

        file = FileUtil.createFileFromStrings("source");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));

        file = FileUtil.createFileFromStrings("source","org");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));

        file = FileUtil.createFileFromStrings("source","org","zfin");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));
        assertTrue(file.getAbsolutePath().contains("zfin"));

        file = FileUtil.createFileFromStrings("source","org","zfin","marker.hbm.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));
        assertTrue(file.getAbsolutePath().contains("zfin"));
        assertTrue(file.getAbsolutePath().contains("marker.hbm.xml"));
    }

    private void setTestDirectories() {
        testLoadDirectory.mkdir();

        testLoadFile = new File(testLoadDirectory, "test-file-one.txt");
        testPurgeFile = new File(testLoadDirectory, "test-file-two.txt");
        try {
            testLoadFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanupTestFilesStructure() {
        testLoadFile.delete();
        testPurgeFile.delete();
        testLoadDirectory.delete();
        File[] files = testArchiveDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        testArchiveDir.delete();
    }


}
