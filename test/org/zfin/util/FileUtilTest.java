package org.zfin.util;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Unit test class for FileUtil.
 */
public class FileUtilTest {

    private static final File testArchiveDir = new File("test", "test-archive-dir");
    private static final File testLoadDirectory = new File("test", "test-load-dir");
    private File testLoadFile;
    private File testPurgeFile;
    private final static String FILE_SEPARATOR = System.getProperty("file.separator");

    @Before
    public void setUp() {
        TestConfiguration.configure();
        setTestDirectories();
    }

    @After
    public void tearDown() {
        cleanupTestFilesStructure();
    }

    @Test
    public void fileBuilderFromString() {

        assertNull(FileUtil.createFileFromStrings());
        File file1 = new File("file1");
        assertEquals(file1.getAbsolutePath(),
                FileUtil.createFileFromStrings("file1").getAbsolutePath());
        File file2 = new File("file1" + FILE_SEPARATOR + "file2.txt");
        assertEquals(file2.getAbsolutePath(),
                FileUtil.createFileFromStrings("file1", "file2.txt").getAbsolutePath());

        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(".");
        File testFile1 = FileUtil.createFileFromStrings(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value(),
                "WEB-INF", "conf", "antibody.template");
        File file3 = new File(ZfinPropertiesEnum.WEBROOT_DIRECTORY + FILE_SEPARATOR +
                "WEB-INF" + FILE_SEPARATOR +
                "conf" + FILE_SEPARATOR +
                "antibody.template" + FILE_SEPARATOR);
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

        file = FileUtil.createFileFromStrings("source", "org");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));

        file = FileUtil.createFileFromStrings("source", "org", "zfin");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));
        assertTrue(file.getAbsolutePath().contains("zfin"));

        file = FileUtil.createFileFromStrings("source", "org", "zfin", "marker.hbm.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getAbsolutePath().contains("source"));
        assertTrue(file.getAbsolutePath().contains("org"));
        assertTrue(file.getAbsolutePath().contains("zfin"));
        assertTrue(file.getAbsolutePath().contains("marker.hbm.xml"));
    }

    @Test
    public void readNumberOfLines() throws IOException {
        String fileName = FileUtil.createAbsolutePath("test", "test-count-number-of-lines.txt");
        int numOfLines = FileUtil.countLines(new File(fileName));
        assertEquals(13, numOfLines);
    }

    @Test
    public void createFileInfo() throws IOException {
        String fileName = FileUtil.createAbsolutePath("test", "test-count-number-of-lines.txt");
        FileInfo fileInfo = FileUtil.getFileInfo(new File(fileName));
        assertNotNull(fileInfo);
        assertEquals(13, fileInfo.getNumberOfLines());
        assertEquals(60, fileInfo.getSize());
        assertEquals("test-count-number-of-lines.txt", fileInfo.getName());
    }

    @Test
    public void deleteDirectory() throws Exception {
        File dirTop = new File("test/delete-me");
        if (dirTop.exists()) {
            FileUtils.deleteDirectory(dirTop);
            assertFalse(dirTop.exists());
        }
        assertTrue(dirTop.mkdir());
        File dirNext = new File("test/delete-me/delete-you");
        assertTrue(dirNext.mkdir());
        File file1 = new File("test/delete-me/bob.txt");
        assertTrue(file1.createNewFile());
        File file2 = new File("test/delete-me/delete-you/bob.txt");
        assertTrue(file2.createNewFile());

        FileUtils.deleteDirectory(dirTop);
        assertFalse(dirTop.exists());

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
