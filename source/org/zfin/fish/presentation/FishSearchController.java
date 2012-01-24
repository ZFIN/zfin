package org.zfin.fish.presentation;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.repository.FishMatchingService;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.MatchingText;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * This class serves the antibody search page.
 */
@Controller
public class FishSearchController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }

    private
    @Autowired
    HttpServletRequest request;

    /**
     * Search submission handling.
     *
     * @param model    Model
     * @param formBean FishSearchFormBean
     * @param result   BindingResult
     * @return view page
     */
    @RequestMapping(value = "/do-search", method = RequestMethod.GET)
    protected String search(Model model,
                            @ModelAttribute("formBean")
                            FishSearchFormBean formBean, BindingResult result) {

        if (result.getErrorCount() > 0)
            LOG.error("Errors found during form binding: " + result);

        formBean.setQueryString(request.getQueryString());
        formBean.setRequestUrl(request.getRequestURL());
        FishSearchCriteria criteria = FishService.getFishSearchCriteria(formBean);
        FishSearchResult searchResult = FishService.getFish(criteria);
        if (searchResult != null) {
            List<Fish> fishList = searchResult.getResults();
            formBean.setTotalRecords(searchResult.getResultsFound());
            formBean.setFishList(fishList);
        }
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish Search Results");
        return "fish/fish-search-result.page";
    }

    /**
     * Initial search form request when no parameters are submitted
     *
     * @param model Model
     * @return view page name
     */
    @RequestMapping(value = "/search")
    protected String showSearchForm(Model model) {
        FishSearchFormBean formBean = new FishSearchFormBean();
        formBean.setIncludeSubstructures(true);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish Search");
        return "fish/fish-search.page";
    }

    /**
     * Initial search form request when no parameters are submitted
     *
     * @param model    Model
     * @param formBean form bean
     * @param result   binding errors
     * @return view page name
     */
    @RequestMapping(value = "/matching-detail")
    protected String showMatchingDetails(Model model,
                                         @ModelAttribute("formBean") FishSearchFormBean formBean,
                                         BindingResult result) {
        if (result.getErrorCount() > 0)
            LOG.error("Errors found during form binding: " + result);

        Fish fish = FishService.getFish(formBean.getFishID());
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        FishMatchingService service = new FishMatchingService(fish);
        Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
        model.addAttribute("matchingTextList", matchingTextList);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "");

        return "fish/matching-detail.popup";
    }

    private static final Logger LOG = Logger.getLogger(FishSearchController.class);

}