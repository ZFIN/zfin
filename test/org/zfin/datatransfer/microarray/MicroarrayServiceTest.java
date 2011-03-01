package org.zfin.datatransfer.microarray;

import org.junit.Test;

/**
 */
public class MicroarrayServiceTest {

    // this is more for convenience and shouldn't be run as a regular test
    @Test
    public void testIndividualGPL() {
        DefaultGeoSoftParser defaultSoftParser = new DefaultGeoSoftParser();
//        defaultSoftParser.setAlwaysUseExistingFile(true);
        defaultSoftParser.parseUniqueNumbers("GPL1319", 2, new String[]{"Danio rerio"}, new String[]{"Control"});
    }
}
