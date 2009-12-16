package org.zfin.uniquery;

import org.apache.log4j.Logger;
import org.zfin.uniquery.categories.SiteSearchCategories;
import org.zfin.wiki.RemotePage;
import org.zfin.wiki.RemotePageSummary;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.WikiWebService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexer {

    private List<String> wikiSpaces = new ArrayList<String>();
    public static final String WIKI_CATEGORY_ID = "WIKI";
    private static final Logger logger = Logger.getLogger(WikiIndexer.class);

    public WikiIndexer() {
        // obtain wiki spaces to be indexed
        List<SearchCategory> wikiCategories = SiteSearchCategories.getWikiCategories();
        if (wikiCategories == null)
            return;

        for (SearchCategory wikiCategory : wikiCategories) {
            if (wikiCategory != null) {
                List<UrlPattern> patterns = wikiCategory.getUrlPatterns();
                for (UrlPattern pattern : patterns) {
                    int indexOfSlash = pattern.getPattern().lastIndexOf("/");
                    String space = pattern.getPattern().substring(indexOfSlash + 1);
                    wikiSpaces.add(space);
                }
            }
        }
    }

    public List<WebPageSummary> getUrlSummary() throws WikiLoginException {
        List<WebPageSummary> summaries = new ArrayList<WebPageSummary>();
        for (String wikispace : wikiSpaces) {
            summaries.addAll(processPagesForSpace(wikispace));
        }
        return summaries;
    }

    private List<WebPageSummary> processPagesForSpace(String wikispace) throws WikiLoginException {
        RemotePageSummary[] pages = WikiWebService.getInstance().getAllPagesForSpace(wikispace);
        List<WebPageSummary> summaryList = new ArrayList<WebPageSummary>(pages.length);
        for (RemotePageSummary remoteSummary : pages) {
            try {
                summaryList.add(createWebPageSummary(remoteSummary));
            } catch (Exception e) {
                logger.error("failed to create summary for page: " + remoteSummary.getTitle(), e);
            }
        }
        return summaryList;
    }

    private WebPageSummary createWebPageSummary(RemotePageSummary remoteSummary) throws Exception {
        WebPageSummary summary = new WebPageSummary();
        RemotePage page = WikiWebService.getInstance().getPage(remoteSummary.getId());
        String title = remoteSummary.getTitle();
        summary.setTitle(title);
        String url = remoteSummary.getUrl();
        summary.setUrl(new URL(url));
        summary.setUrlName(url);
        summary.setText(page.getContent());
        summary.setBody(page.getContent());
        return summary;
    }


}
