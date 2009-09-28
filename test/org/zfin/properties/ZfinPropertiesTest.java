package org.zfin.properties;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zfin.TestConfiguration;

/**
 * Test if the property file can be read.
 */
public class ZfinPropertiesTest {

    public static final String FILE_SEP = System.getProperty("file.separator");

    @Before
    public void setUp() throws Exception {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();
    }

    /**
     * Test the test properties, coming from a test file.
     */
    @Test
    public void props() {
        assertEquals("Background Color", "#2244", ZfinProperties.getBackgroundColor());
        assertEquals("Highlight Color", "#4437", ZfinProperties.getHighlightColor());
        assertEquals("Highlighter Color", "#7743", ZfinProperties.getHighlighterColor());
        assertEquals("Link Color", "#9980", ZfinProperties.getLinkbarColor());
        assertEquals("Email Address", "zfin@zfin.org", ZfinProperties.getAdminEmailAddressString());
        assertEquals("FTP Path", "/ftp/zfin", ZfinProperties.getFtpPath());
        assertEquals("Image Load Path", "/image/load", ZfinProperties.getImageLoadPath());
        assertEquals("Load Up Path", "/image/full", ZfinProperties.getLoadUpFull());
        assertEquals("PDF Path", "/pdf/", ZfinProperties.getPdfPath());
    }

    @Test
    public void blastProps(){
        assertFalse("WebHost Database Path replaced",  ZfinProperties.getWebHostDatabasePath().contains("@"));
        assertFalse("WebHost Get binary",ZfinProperties.getWebHostBlastGetBinary().contains("@"));
        assertTrue("WebHost Get binary", ZfinProperties.getWebHostBlastGetBinary().contains("xdget"));
        assertFalse("WebHost Put binary",ZfinProperties.getWebHostBlastPutBinary().contains("@"));
        assertTrue("WebHost Put binary", ZfinProperties.getWebHostBlastPutBinary().contains("xdformat"));
        assertTrue("WebHost Server", ZfinProperties.getWebHostUserAtHost().indexOf("@")>1);
        assertTrue("WebHost Server", ZfinProperties.getWebHostUserAtHost().contains("@"));

        assertFalse("BlastServer Database Path", ZfinProperties.getBlastServerDatabasePath().contains("@"));
        assertFalse("BlastServer Get binary", ZfinProperties.getBlastServerGetBinary().contains("@"));
        assertTrue("BlastServer Get binary", ZfinProperties.getBlastServerGetBinary().contains("xdget"));
        assertFalse("BlastServer Put binary", ZfinProperties.getBlastServerPutBinary().contains("@"));
        assertTrue("BlastServer Put binary", ZfinProperties.getBlastServerPutBinary().contains("xdformat"));

        assertFalse("BlastServer Target",  ZfinProperties.getBlastServerTarget().contains("@"));
//        assertEquals("BlastServer Blast-all binary", "wu-blastall", ZfinProperties.getBlastAllBinary()); // currently not used
        assertFalse("BlastServer Distributed Query Path", ZfinProperties.getDistributedQueryPath().contains("@"));
        assertEquals("BlastServer Blast Access", ZfinProperties.SSH_STRING, ZfinProperties.getBlastServerAccessBinary());
    }

}
