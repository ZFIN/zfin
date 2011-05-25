package org.zfin.wiki.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.antibody.Antibody;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.service.AntibodyWikiWebService;

/**
 */
@Controller
@RequestMapping(value = "/wiki")
public class WikiLinkController {

    private Logger logger = Logger.getLogger(WikiLinkController.class);

    @RequestMapping(value = "/wikiLink/{zdbID}")
    protected String getWikiLink(@PathVariable String zdbID, Model model) throws Exception {

        Antibody antibody ;
        if(zdbID.startsWith("ZDB-ATB")){
            antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
        }
        else{
            antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByAbbrev(zdbID);
        }
        if(antibody!=null){
            try {
                model.addAttribute("name", antibody.getName());
                String url = AntibodyWikiWebService.getInstance().getWikiLink(antibody.getName());
                model.addAttribute("url", url);
            } catch (WikiLoginException e1) {
                logger.error("problem showing antibody wiki link: " + antibody.getName(), e1);
                model.addAttribute("url", null);
            }
        }
        else{
            logger.error("Unable to find antibody for key: " + zdbID);
        }

        return "wiki/wiki-link.insert";
    }
}
