package org.zfin.mutant.presentation;


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
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.presentation.Fish;
import org.zfin.mutant.presentation.ConstructSearchFormBean;
import org.zfin.fish.repository.FishMatchingService;
import org.zfin.fish.repository.FishRepository;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.ConstructSearchResult;
import org.zfin.mutant.repository.ConstructRepository;
import org.zfin.mutant.repository.ConstructService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Controller
public class ConstructSearchController {

    @ModelAttribute("formBean")
    private ConstructSearchFormBean getDefaultSearchForm() {
        return new ConstructSearchFormBean();
    }

    @Autowired
    ConstructRepository constructRepository;

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
     @RequestMapping(value = "/construct-do-search", method = RequestMethod.GET)
protected String search(Model model,
                        @ModelAttribute("formBean")
                        ConstructSearchFormBean formBean, BindingResult result) {

    if (result.getErrorCount() > 0)
        LOG.error("Errors found during form binding: " + result);

    formBean.setQueryString(request.getQueryString());
    formBean.setRequestUrl(request.getRequestURL());
    ConstructSearchCriteria criteria = ConstructService.getConstructSearchCriteria(formBean);
    ConstructSearchResult searchResult = ConstructService.getConstruct(criteria);
    if (searchResult != null) {
        List<Construct> constructList = searchResult.getResults();
        formBean.setTotalRecords(searchResult.getResultsFound());
        formBean.setConstructList(constructList);
    }
         attachMetaData(model, formBean);
        return "mutant/construct-search-result.page";
    }
    private void attachMetaData(Model model, ConstructSearchFormBean formBean) {
        formBean.setSummary(constructRepository.getWarehouseSummary(WarehouseSummary.Mart.CONSTRUCT_MART));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Construct Search Results");
        ZdbFlag status = constructRepository.getConstructMartStatus();
        model.addAttribute(status);
    }
    /**
     * Initial search form request when no parameters are submitted
     *
     * @param model Model
     * @return view page name
     */
    @RequestMapping(value = "/construct-search")
    protected String showSearchForm(Model model) {
        ConstructSearchFormBean formBean = new ConstructSearchFormBean();

        formBean.setSummary(constructRepository.getWarehouseSummary(WarehouseSummary.Mart.CONSTRUCT_MART));
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Construct Search");
        ZdbFlag status = constructRepository.getConstructMartStatus();
        model.addAttribute(status);
        return "mutant/construct-search.page";
    }

    /**
     * Initial search form request when no parameters are submitted
     *
     * @param model    Model
     * @param formBean form bean
     * @param result   binding errors
     * @return view page name
     */
    /*@RequestMapping(value = "/matching-detail")
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
*/
    private static final Logger LOG = Logger.getLogger(ConstructSearchController.class);

}