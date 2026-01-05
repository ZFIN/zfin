package org.zfin.wiki;

import org.junit.Test;
import org.zfin.wiki.presentation.WikiPage;

import java.util.List;

import static org.junit.Assert.*;

public class ConfluenceQueryTest {

    @Test
    public void fetchWikiPagesForMeetings() {
        ConfluenceQuery query = new ConfluenceQuery();
        try {
            List<WikiPage> wikiPages = query.getWikiPagesForSpace("meetings", null);
            assertNotNull(wikiPages);
            assertTrue(wikiPages.size() > 0);
            for (WikiPage page : wikiPages) {
                System.out.println("Title: " + page.getTitle() + ", URL: " + page.getUrl());
            }
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void fetchWikiPagesForNews() {
        ConfluenceQuery query = new ConfluenceQuery();
        try {
            List<WikiPage> wikiPages = query.getWikiPagesForSpace("news", 120);
            assertNotNull(wikiPages);
            assertTrue(wikiPages.size() > 0);
            for (WikiPage page : wikiPages) {
                System.out.println("Title: " + page.getTitle() + ", URL: " + page.getUrl());
            }
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void fetchWikiPagesForJobs() {
        ConfluenceQuery query = new ConfluenceQuery();
        try {
            List<WikiPage> wikiPages = query.getWikiPagesForSpace("jobs", 120);
            assertNotNull(wikiPages);
            assertTrue(wikiPages.size() > 0);
            for (WikiPage page : wikiPages) {
                System.out.println("Title: " + page.getTitle() + ", URL: " + page.getUrl());
            }
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

}
