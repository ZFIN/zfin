package org.zfin.wiki.presentation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.wiki.RemotePageSummary;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.service.WikiWebService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class MeetingsController {

    private WikiWebService instance = WikiWebService.getInstance();

    public MeetingsController() throws WikiLoginException {
    }

    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/wiki/{wikiSpaceName}")
    public JsonResultResponse<WikiPage> getMeetingsWikiPages(@PathVariable String wikiSpaceName,
                                                             @Version Pagination pagination) {
        final String note = "Retrieving wiki pages from space: " + wikiSpaceName;
        log.info(note);
        long start = System.currentTimeMillis();
        List<RemotePageSummary> pages = instance.getPagesSorted(wikiSpaceName, pagination);
        JsonResultResponse<WikiPage> response = new JsonResultResponse<>();
        response.setPagination(pagination);
        response.setResults(pages.stream().map(wikiPage -> {
            WikiPage wik = new WikiPage();
            wik.setId(wikiPage.getId());
            wik.setTitle(wikiPage.getTitle());
            wik.setUrl(wikiPage.getUrl());
            return wik;
        }).collect(Collectors.toList()));
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(start);
        response.setNote(note);
        return response;
    }

    @Setter
    @Getter
    private
    class WikiPage {
        private long id;
        private String title;
        private String url;
    }

}
