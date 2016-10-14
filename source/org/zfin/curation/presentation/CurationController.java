package org.zfin.curation.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * This Controller is used to facilitate the curation tabs.
 */
@Controller
@RequestMapping("/curation")
@SessionAttributes({"currentTab"})
public class CurationController {

    @ModelAttribute("currentTab")
    public String getCurrentTab() {
        return CurationTab.FEATURE.getName();
    }

    @RequestMapping("/{pubID}")
    protected String publicationCurationPage(@PathVariable String pubID,
                                             @ModelAttribute("currentTab") String currentTab,
                                             Model model) throws Exception {
        return curationPage(currentTab, pubID, model);
    }

    @RequestMapping("/")
    protected String generalCurationPage(@ModelAttribute("currentTab") String currentTab,
                                         Model model) throws Exception {
        return curationPage(currentTab, "ZDB-PUB-990507-16", model);
    }

    @RequestMapping("/{module}/{pubID}")
    protected String curationPage(@PathVariable String module,
                                  @PathVariable String pubID,
                                  Model model) throws Exception {
        model.addAttribute("module", module);

        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            return "record-not-found.page";
        }
        model.addAttribute("publication", publication);
        model.addAttribute("curationTabs", CurationTab.values());
        model.addAttribute("currentTab", module);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Curate: " + publication.getTitle());
        return "curation/" + module + ".page";
    }


}