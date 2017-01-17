package org.zfin.curation.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.curation.ui.CurationModuleType;
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
        return CurationModuleType.FEATURE_CURATION.getValue();
    }

    @RequestMapping("/{pubID}")
    protected String curationPage(@PathVariable String pubID,
                                  @ModelAttribute("currentTab") String currentTab,
                                  Model model) throws Exception {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            return "record-not-found.page";
        }
        model.addAttribute("publication", publication);
        model.addAttribute("curationTabs", CurationModuleType.values());
        model.addAttribute("currentTab", currentTab);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Curate: " + publication.getTitle());
        return "curation/curation.page";
    }

    @ResponseBody
    @RequestMapping("/currentTab/{currentTab}")
    protected String setCurrentTab(@PathVariable String currentTab,
                                   Model model) throws Exception {
        if (currentTab == null)
            return "error: no tab name provided";
        if (CurationModuleType.getType(currentTab) == null)
            return "error: no tab name found by name: " + currentTab;
        String tabName = CurationModuleType.getType(currentTab).getValue();
        model.addAttribute("currentTab", tabName);
        return "success";
    }


}