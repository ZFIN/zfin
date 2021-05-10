package org.zfin.uniquery;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.wiki.*;
import org.zfin.wiki.service.WikiWebService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexer {

    private List<String> wikiSpaces = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(WikiIndexer.class);

    public WikiIndexer() {
        // obtain wiki spaces to be indexed
        //List<SearchCategory> wikiCategories = SiteSearchCategories.getWikiCategories();
        // need to find the categories from faceted search if we ever wanted to index the wiki within FS
        List<SearchCategory> wikiCategories = null;
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
        List<WebPageSummary> summaries = new ArrayList<>();
        for (String wikiSpace : wikiSpaces) {
            summaries.addAll(processPagesForSpace(wikiSpace));
        }
        return summaries;
    }

    private List<WebPageSummary> processPagesForSpace(String wikiSpace) throws WikiLoginException {
        RemotePageSummary[] pages = WikiWebService.getInstance("wiki.zfin.org").getAllPagesForSpace(wikiSpace);
        RemoteBlogEntrySummary[] blogPages = WikiWebService.getInstance("wiki.zfin.org").getAllBLogPagesForSpace(wikiSpace);
        List<WebPageSummary> summaryList = new ArrayList<>(pages.length + blogPages.length);
        for (AbstractRemotePageSummary remoteSummary : pages) {
            try {
                summaryList.add(createWebPageSummary(remoteSummary));
            } catch (Exception e) {
                logger.error("failed to create summary for page: " + remoteSummary.getTitle(), e);
            }
        }
        // retrieve blog pages
        for (RemoteBlogEntrySummary blogSummaryPage : blogPages) {
            try {
                summaryList.add(createBlogPageSummary(blogSummaryPage));
            } catch (Exception e) {
                logger.error("failed to create summary for page: " + blogSummaryPage.getTitle(), e);
            }
        }
        //List<WebPageSummary> summaryBlogList = new ArrayList<WebPageSummary>(pages.length);

        return summaryList;
    }

    private WebPageSummary createWebPageSummary(AbstractRemotePageSummary remoteSummary) throws Exception {
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

    private WebPageSummary createBlogPageSummary(RemoteBlogEntrySummary remoteSummary) throws Exception {
        WebPageSummary summary = new WebPageSummary();
        RemoteBlogEntry page = WikiWebService.getInstance().getBlogPage(remoteSummary.getId());
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
