package org.zfin.profile.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.profile.Lab;
import org.zfin.profile.repository.ProfileRepository;

import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class LabSearchController {

    @Autowired
    private ProfileRepository profileRepository;

    // for value
    @RequestMapping(value = "/lab/search", method = RequestMethod.GET)
    public String labSearch(
            Model model,
            LabSearchBean formBean
    ) {
        formBean.setMaxDisplayRecords("25");
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Lab Search");
        return "profile/lab-search";
    }

    @RequestMapping(value = "/lab/search/execute", method = RequestMethod.GET)
    public String labSearchSubmit(
            Model model
            , LabSearchBean searchBean
    ) {
        PaginationResult<Lab> labPaginationResult = profileRepository.searchLabs(searchBean);
        model.addAttribute("orgs", labPaginationResult.getPopulatedResults());
        searchBean.setTotalRecords(labPaginationResult.getTotalCount());

        // pass in "type" and organizations
        model.addAttribute(LookupStrings.FORM_BEAN, searchBean);
        model.addAttribute("type", Area.LAB.name());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Lab Search");

        if (searchBean.getView().equalsIgnoreCase(AbstractProfileSearchBean.PRINT_VIEW)) {
            return "profile/organization-print";
        } else if (searchBean.getView().equalsIgnoreCase(AbstractProfileSearchBean.HTML_VIEW)) {
            return "profile/lab-search";
        } else {
            return "profile/lab-search";
        }
    }

    @RequestMapping("/lab/all-labs")
    public String listAllLabs(Model model){
        List<Lab> labs = profileRepository.getLabs() ;
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.LAB.getTitleString() + " All");
        model.addAttribute("type", "LAB");
        model.addAttribute("orgs",labs);
        return "profile/list-all-organizations";
    }


}
