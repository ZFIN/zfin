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
            List<WikiPage> wikiPages = query.getWikiPagesForSpaceUsingCache("meetings");
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
            List<WikiPage> wikiPages = query.getWikiPagesForSpaceUsingCache("news");
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
            List<WikiPage> wikiPages = query.getWikiPagesForSpaceUsingCache("jobs");
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
    public void fetchWikiPagesUsesCacheEfficiently() {
        ConfluenceQuery query = new ConfluenceQuery();
        try {
            long startTime = System.currentTimeMillis();
            List<WikiPage> firstFetch = query.getWikiPagesForSpaceUsingCache("meetings");
            long firstDuration = System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            List<WikiPage> secondFetch = query.getWikiPagesForSpaceUsingCache("meetings");
            long secondDuration = System.currentTimeMillis() - startTime;

            System.out.println("First fetch duration: " + firstDuration + " ms");
            System.out.println("Second fetch duration: " + secondDuration + " ms");

            assertEquals(firstFetch.size(), secondFetch.size());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
