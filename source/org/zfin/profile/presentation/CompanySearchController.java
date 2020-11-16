package org.zfin.profile.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.profile.Company;
import org.zfin.profile.repository.ProfileRepository;

import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class CompanySearchController {

    @Autowired
    private ProfileRepository profileRepository;

    // for value
    @RequestMapping(value = "/company/search", method = RequestMethod.GET)
    public String companySearch(
            Model model,
            CompanySearchBean formBean
    ) {
        formBean.setMaxDisplayRecords("25");
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Company Search");
        return "profile/company-search";
    }

    @RequestMapping(value = "/company/search/execute", method = RequestMethod.GET)
    public String companySearchSubmit(
            Model model
            , CompanySearchBean searchBean
    ) {
        PaginationResult<Company> companyPaginationResult = profileRepository.searchCompanies(searchBean);
        model.addAttribute("orgs", companyPaginationResult.getPopulatedResults());
        searchBean.setTotalRecords(companyPaginationResult.getTotalCount());

        // pass in "type" and organizations
        model.addAttribute(LookupStrings.FORM_BEAN, searchBean);
        model.addAttribute("type", Area.COMPANY.name());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Company Search");

        if (searchBean.getView().equalsIgnoreCase(AbstractProfileSearchBean.PRINT_VIEW)) {
            return "profile/organization-print";
        } else if (searchBean.getView().equalsIgnoreCase(AbstractProfileSearchBean.HTML_VIEW)) {
            return "profile/company-search";
        } else {
            return "profile/company-search";
        }
    }

    @RequestMapping(value = "/company/search/disclaimer")
    public String companySearchDisclaimer() {
        return "profile/company-search-disclaimer";
    }

    @RequestMapping("/company/all-companies")
    public String listAllLabs(Model model) {
        List<Company> companies = profileRepository.getCompanies();
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.COMPANY.getTitleString() + " All");
        model.addAttribute("orgs", companies);
        model.addAttribute("type", Area.COMPANY.name());
        return "profile/list-all-organizations";
    }


}
