package org.zfin.wiki.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.wiki.ConfluenceQuery;
import org.zfin.wiki.WikiLoginException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Log4j2
public class MeetingsController {

    public MeetingsController() throws WikiLoginException {
    }

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse httpResponse;

    @RequestMapping(value = "/wiki/{wikiSpaceName}")
    public JsonResultResponse<WikiPage> getMeetingsWikiPages(@PathVariable String wikiSpaceName,
                                                             @Version Pagination pagination) throws IOException, URISyntaxException {
        final String note = "Retrieving wiki pages from space: " + wikiSpaceName;
        log.info(note);
        long start = System.currentTimeMillis();

        ConfluenceQuery query = new ConfluenceQuery();
        List<WikiPage> list = query.getWikiPagesForSpaceUsingCache(wikiSpaceName);

        JsonResultResponse<WikiPage> response = new JsonResultResponse<>();
        response.setPagination(pagination);
        response.setResults(list);
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(start);
        response.setNote(note);
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

}
