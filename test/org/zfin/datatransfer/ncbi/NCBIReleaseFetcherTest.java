package org.zfin.datatransfer.ncbi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for FileUtil.
 */
public class NCBIReleaseFetcherTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void getCurrentReleaseTest() {
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        Optional<Integer> release = fetcher.getCurrentRelease();
        assertTrue(release.isPresent());

        //should be greater than 100
        assertTrue(release.get() > 100);
    }

    @Test
    public void getReleaseUrlCannotConnectTest() {
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        fetcher.setReleaseUrl("http://127.0.0.1/bogus/url/meant/to/fail");
        Optional<Integer> release = fetcher.getCurrentRelease();
        assertTrue(release.isEmpty());
    }

    @Test
    public void getReleaseUrlCannotParseTest() {
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        fetcher.setReleaseUrl("https://www.google.com");
        Optional<Integer> release = fetcher.getCurrentRelease();
        assertTrue(release.isEmpty());
    }

    @Test
    public void getReleaseUrlFailsOnZeroTest() throws IOException {
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();

        //temporary file that will be deleted after test using the jre temp directory
        Path file = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")), "test", ".txt");         
        Files.writeString(file, "0");

        fetcher.setReleaseUrl(file.toUri().toString());
        Optional<Integer> release = fetcher.getCurrentRelease();
        Files.delete(file);
        assertTrue(release.isEmpty());
    }
}
