package org.zfin.properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;

import static org.junit.Assert.*;

/**
 * Test if the property file can be read.
 */
public class ZfinPropertiesTest {

    public static final String TEST_PROPERTIES_FILE = "./externals/properties/test-properties.properties";
    private String currentPropertyFile ;

    /**
     * We init to get the mutant, but we re-init in order to evaluate our "test" case.
     */
    @Before
    public void setUp() throws Exception {
        currentPropertyFile = ZfinProperties.getCurrentPropertyFile() ;
        TestConfiguration.configure();
        ZfinProperties.init(TEST_PROPERTIES_FILE) ;
    }

    /**
     * This cleans up the property file to the old one.
     */
    @After
    public void cleanup(){
        ZfinProperties.init(currentPropertyFile);
    }

    /**
     * Test the test properties, coming from a test file.
     */
    @Test
    public void props() {
        Assert.assertEquals("Background Color", "#2244", ZfinPropertiesEnum.BACKGROUND_COLOR.value());
        Assert.assertEquals("Highlight Color", "#4437", ZfinPropertiesEnum.HIGHLIGHT_COLOR.value());
        assertEquals("Highlighter Color", "#7743", ZfinPropertiesEnum.HIGHLIGHTER_COLOR.value());
        assertEquals("Link Color", "#9980", ZfinPropertiesEnum.LINKBAR_COLOR.value());
        assertEquals("Email Address", "test@zfin.org", ZfinProperties.splitValues(ZfinPropertiesEnum.ZFIN_ADMIN)[0]);
        assertEquals("FTP Path", "/research/zcentral/ftp/test/test", ZfinPropertiesEnum.FTP_ROOT.value());
        assertEquals("Image Load Path", "/imageLoadUp", ZfinPropertiesEnum.IMAGE_LOAD.value());
        assertEquals("Load Up Path", "/image/full", ZfinPropertiesEnum.LOADUP_FULL.value());
        assertEquals("PDF Path", "/pdf/", ZfinPropertiesEnum.PDF_PATH.value());
        assertEquals("test@zfin.org",ZfinPropertiesEnum.MICROARRAY_EMAIL.value());
    }

    @Test
    public void blastProps(){
        assertFalse("WebHost Database Path replaced",  ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH.value().contains("@"));
        assertFalse("WebHost Get binary", ZfinProperties.getWebHostBlastGetBinary().contains("@"));
        assertTrue("WebHost Get binary", ZfinProperties.getWebHostBlastGetBinary().contains("xdget"));
        assertFalse("WebHost Put binary",ZfinProperties.getWebHostBlastPutBinary().contains("@"));
        assertTrue("WebHost Put binary", ZfinProperties.getWebHostBlastPutBinary().contains("xdformat"));
        assertTrue("WebHost Server", ZfinProperties.getWebHostUserAtHost().contains("@"));

        assertFalse("BlastServer Database Path", ZfinPropertiesEnum.BLASTSERVER_BLAST_DATABASE_PATH.value().contains("@"));
        assertFalse("BlastServer Get binary", ZfinPropertiesEnum.BLASTSERVER_XDGET.value().contains("@"));
        assertTrue("BlastServer Get binary", ZfinPropertiesEnum.BLASTSERVER_XDGET.value().contains("xdget"));
        assertFalse("BlastServer Put binary", ZfinPropertiesEnum.BLASTSERVER_XDFORMAT.value().contains("@"));
        assertTrue("BlastServer Put binary", ZfinPropertiesEnum.BLASTSERVER_XDFORMAT.value().contains("xdformat"));

        assertFalse("BlastServer Target",  ZfinPropertiesEnum.BLASTSCRIPT_TARGET_PATH.value().contains("@"));
        assertEquals("BlastServer Blast-all binary", "wu-blastall", ZfinPropertiesEnum.WEBHOST_BLASTALL.value()); // currently not used
        assertFalse("BlastServer Distributed Query Path", ZfinPropertiesEnum.BLASTSERVER_DISTRIBUTED_QUERY_PATH.value().contains("@"));
        assertEquals("BlastServer Blast Access", "ssh", ZfinPropertiesEnum.SSH.value());
    }

}
