package org.zfin.wiki.presentation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.wiki.RemotePageSummary;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.service.WikiWebService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class MeetingsController {

    private WikiWebService instance = WikiWebService.getInstance();

    public MeetingsController() throws WikiLoginException {
    }

    @RequestMapping(value = "/wiki/{wikiSpaceName}")
    public List<WikiPage> getSequenceView(@PathVariable String wikiSpaceName) {
        List<RemotePageSummary> pages = instance.getPagesSorted(wikiSpaceName);
        log.info(pages);
        return pages.stream().map(page -> {
            WikiPage wik = new WikiPage();
            wik.setId(page.getId());
            wik.setTitle(page.getTitle());
            wik.setUrl(page.getUrl());
            return wik;
        }).collect(Collectors.toList());
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
