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
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.repository.FishMatchingService;
import org.zfin.fish.repository.FishRepository;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.mutant.Fish;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Set;

/**
 * This class serves the antibody search page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishSearchController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }

    @Autowired
    FishRepository fishRepository;

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
                            @Valid @ModelAttribute("formBean")
                            FishSearchFormBean formBean, BindingResult result) {

        //fishSearchFormValidator.validate(formBean, result);

        if (result.getErrorCount() > 0){
            LOG.info("Errors found during form binding: " + result);
            attachMetaData(model, formBean);
            return "fish/fish-search-result.page";
        }

        formBean.setQueryString(request.getQueryString());
        formBean.setRequestUrl(request.getRequestURL());
        FishSearchCriteria criteria = FishService.getFishSearchCriteria(formBean);
        FishSearchResult searchResult = FishService.getFish(criteria);
        if (searchResult != null) {
            formBean.setTotalRecords(searchResult.getResultsFound());
            formBean.setFishSearchResult(searchResult);
        }
        attachMetaData(model, formBean);

        return "fish/fish-search-result.page";
    }

    private void attachMetaData(Model model, FishSearchFormBean formBean) {
        formBean.setSummary(fishRepository.getWarehouseSummary(WarehouseSummary.Mart.FISH_MART));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish Search Results");
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
        formBean.setSummary(fishRepository.getWarehouseSummary(WarehouseSummary.Mart.FISH_MART));
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
        if (result.getErrorCount() > 0)  {
            LOG.error("Errors found during form binding: " + result);
        }
        if (formBean.getFishID().contains("GENO")){
            Fish fish=RepositoryFactory.getMutantRepository().getFish(formBean.getFishID());
            FishSearchCriteria criteria = new FishSearchCriteria(formBean);
            FishMatchingService service = new FishMatchingService(fish);
            Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
            model.addAttribute("matchingTextList", matchingTextList);
            model.addAttribute(LookupStrings.FORM_BEAN, formBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "");

            return "fish/matching-detail.popup";
        }
        else
        {
            Fish fish=RepositoryFactory.getMutantRepository().getFish(formBean.getFishID());
            FishSearchCriteria criteria = new FishSearchCriteria(formBean);
            FishMatchingService service = new FishMatchingService(fish);
            Set<MatchingText> matchingTextList = service.getMatchingText(criteria);
            model.addAttribute("matchingTextList", matchingTextList);
            model.addAttribute(LookupStrings.FORM_BEAN, formBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "");

            return "fish/matching-detail.popup";
        }

    }

    private static final Logger LOG = Logger.getLogger(FishSearchController.class);

}