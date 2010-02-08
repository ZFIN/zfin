package org.zfin.wiki;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinProperties;

import static org.junit.Assert.*;

/**
 */
public class WikiConnectTest {

    @Before
    public void setUp() throws Exception {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();
    }


    @Test
    public void login(){
        try {
            assertTrue(WikiWebService.getInstance().login());
            assertTrue(WikiWebService.getInstance().login());
            assertTrue(WikiWebService.getInstance().logout());
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    /**
     * This test verifies that the protocol space has at least 100 pages in it.
     */
    @Test
    public void getPagesForspace(){
        try {
            assertTrue(WikiWebService.getInstance().getAllPagesForSpace("prot").length>100);
        } catch (WikiLoginException e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void antibodyPageTest(){
        try {
            RemotePage remotePage = AntibodyWikiWebService.getInstance().getPageForAntibodyName("zn-5") ;
            assertNotNull(remotePage) ;
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void antibodyLinkTest(){
        try {
            String linkName = AntibodyWikiWebService.getInstance().getWikiLink("zn-5") ;
            assertNotNull(linkName) ;
            assertTrue(linkName.endsWith("/display/AB/zn-5")) ;
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }


}
