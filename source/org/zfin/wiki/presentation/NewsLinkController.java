package org.zfin.wiki.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.wiki.RemoteBlogEntrySummary;
import org.zfin.wiki.RemotePage;
import org.zfin.wiki.RemotePageSummary;
import org.zfin.wiki.service.NewsWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

/**
 */
@Controller
@RequestMapping(value = "/wiki")
public class NewsLinkController {

    private Logger logger = LogManager.getLogger(NewsLinkController.class);

    private NewsWikiWebService newsWikiWebService;
    private NewsComparator newsComparator = new NewsComparator();

    @Autowired
    public NewsLinkController(NewsWikiWebService newsWikiWebService) {
        this.newsWikiWebService = newsWikiWebService;
    }

    @RequestMapping(value = "/summary/{space}")
    public String getSummary(@PathVariable String space
            , @RequestParam(defaultValue = "-1") String length
            , Model model) {
        try {
            model.addAttribute("length", Integer.valueOf(length));
        } catch (NumberFormatException nfe) {
            logger.error("Failed to format [" + length + "]", nfe);
            model.addAttribute("length", -1);
        }

        try {
            RemoteBlogEntrySummary[] newsItems = newsWikiWebService.getNewsForSpace(space);
            TreeSet<RemoteBlogEntrySummary> summaries = new TreeSet<RemoteBlogEntrySummary>(newsComparator);
            summaries.addAll(Arrays.asList(newsItems));
            model.addAttribute("summaries", summaries);
        } catch (Exception e) {
            logger.error(e);
            model.addAttribute("summaries", null);
        }

        return "wiki/news-summary.insert";
    }

    @RequestMapping(value = "/view/{space}/{pageName}")
    public String getPage(@PathVariable String space,
                          @PathVariable String pageName,
                          Model model) {
        pageName = pageName.replace("+", " ");
        RemotePage remotePage = null;
        try {
//            RemotePageSummary[] remotePageS = WikiWebService.getInstance("wiki.zfin.org").getAllPagesForSpace(space);
            remotePage = WikiWebService.getInstance("wiki.zfin.org").getPageForTitleAndSpace(pageName, space);
        } catch (Exception e) {
            logger.error(e);
            model.addAttribute("summaries", null);
        }

        model.addAttribute("wikiPage", getPurePage(remotePage));
        return "wiki/view-wiki-page.insert";
    }

    private String getPurePage(RemotePage remotePage) {
        String content = remotePage.getContent();
        String newContent = "";
        while (true) {
            newContent = removeMacros(content);
            if (newContent.length() == content.length())
                break;
            content = newContent;
        }
        while (true) {
            newContent = removeClassFromDiv(content);
            if (newContent.length() == content.length())
                break;
            content = newContent;
        }
        return newContent;
    }

    private String removeMacros(String content) {
        StringBuilder builder = new StringBuilder();
        int startIndex = content.indexOf("<ac:macro");
        if (startIndex == -1)
            return content;
        String endOfMacro = "</ac:macro>";
        int endIndex = content.indexOf(endOfMacro);
        builder.append(content.substring(0, startIndex));
        builder.append(content.substring(endIndex + endOfMacro.length()));
        return builder.toString();
    }

    private String removeClassFromDiv(String content) {
        String s = content.replaceAll("<div [^>]*>", "");
        s = s.replaceAll("</div\\s*>", "");
        return s;
    }

    class NewsComparator implements Comparator<RemoteBlogEntrySummary> {

        @Override
        public int compare(RemoteBlogEntrySummary remoteBlogEntrySummary, RemoteBlogEntrySummary remoteBlogEntrySummary1) {
            return remoteBlogEntrySummary1.getPublishDate().compareTo(remoteBlogEntrySummary.getPublishDate());
        }
    }

}
