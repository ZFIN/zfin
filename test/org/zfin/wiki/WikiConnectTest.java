package org.zfin.wiki;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zfin.TestConfiguration;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.NewsWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import static org.junit.Assert.*;

/**
 */
public class WikiConnectTest {

    private NewsWikiWebService newsWikiWebService ;


    @Before
    public void setUp() throws Exception {
        TestConfiguration.configure();
        newsWikiWebService = new NewsWikiWebService();
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

    @Test
    public void testNews() throws Exception{
        RemoteBlogEntrySummary[] summaries = newsWikiWebService.getNewsForSpace("meetings") ;
        assertNotNull(summaries);
        assertTrue("should have more than 0",summaries.length>0 ) ;
    }




}
