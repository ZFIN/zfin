package org.zfin.wiki.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.service.AntibodyWikiWebService;

/**
 */
@Controller
@RequestMapping(value = "/wiki")
public class WikiLinkController {

    private Logger logger = Logger.getLogger(WikiLinkController.class);

    @RequestMapping(value = "/wikiLink/{name}")
    protected String getWikiLink(@PathVariable String name, Model model) throws Exception {

        model.addAttribute("name", name);
        try {
            String url = AntibodyWikiWebService.getInstance().getWikiLink(name);
            model.addAttribute("url", url);
        } catch (WikiLoginException e1) {
            logger.error("problem showing antibody wiki link: " + name, e1);
            model.addAttribute("url", null);
        }

        return "wiki/wiki-link.insert";
    }
}
