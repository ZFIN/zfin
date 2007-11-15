package org.zfin.properties;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
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
        assertEquals("Email Address", "zfin@zfin.org", ZfinProperties.getAdminEmailAddress());
        assertEquals("FTP Path", "/ftp/zfin", ZfinProperties.getFtpPath());
        assertEquals("Image Load Path", "/image/load", ZfinProperties.getImageLoadPath());
        assertEquals("Load Up Path", "/image/full", ZfinProperties.getLoadUpFull());
        assertEquals("PDF Path", "/pdf/", ZfinProperties.getPdfPath());
    }

}
