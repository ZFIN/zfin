package org.zfin.uniquery;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Indexing the Wiki site.
 */
public class IndexerUtilTest {

    @Test
    public void testHtmlParser() throws IOException {
        String html =
                "<a href=\"/action/ontology/post-composed-term-detail?superTermID=ZFA:0000008&amp;subTermID=ZFA:0009073\">brain&nbsp;glial cell</a>";
        WebPageSummary webPageSummary = new WebPageSummary();
        webPageSummary.setBody(html);
        URL url = new URL("http://zfin.org/ZDB-GENE-021015-1");
        webPageSummary.setUrl(url);
        IndexerUtil.updateSummary(webPageSummary);
        assertNotNull(webPageSummary);
        assertEquals("brain glial cell ", webPageSummary.getText());
    }

}