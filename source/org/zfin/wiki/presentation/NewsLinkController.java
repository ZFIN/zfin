package org.zfin.wiki.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.wiki.RemoteBlogEntrySummary;
import org.zfin.wiki.service.NewsWikiWebService;

/**
 */
@Controller
@RequestMapping(value = "/wiki")
public class NewsLinkController {

    private Logger logger = Logger.getLogger(NewsLinkController.class);

    private NewsWikiWebService newsWikiWebService;

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
            RemoteBlogEntrySummary[] summaries = newsWikiWebService.getNewsForSpace(space);
            model.addAttribute("summaries", summaries);
        } catch (Exception e) {
            logger.error(e);
            model.addAttribute("summaries", null);
        }

        return "wiki/news-summary.insert";
    }

}
