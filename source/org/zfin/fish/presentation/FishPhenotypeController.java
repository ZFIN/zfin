package org.zfin.fish.presentation;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.Fish;

import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * This class serves the phenotype summary page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishPhenotypeController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }


    private static Logger LOG = LogManager.getLogger(FishPhenotypeController.class);

    @RequestMapping(value = "/phenotype-summary", method = RequestMethod.GET)
    protected String showPhenotypeSummary(@RequestParam(value = "fishID", required = true) String fishID,
                                          @ModelAttribute("formBean") FishSearchFormBean formBean,
                                          Model model) throws Exception {

        LOG.info("Start Fish Phenotype Controller");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FishService.getPhenotypeSummary(fishID, criteria);
        Collections.sort(figureSummaryDisplayList);
        model.addAttribute("figureSummaryDisplay", figureSummaryDisplayList);
        PhenotypeSummaryCriteria summaryCriteria = FishService.getPhenotypeSummaryCriteria(fishID);
        summaryCriteria.setCriteria(criteria);
        model.addAttribute("phenotypeSummaryCriteria", summaryCriteria);
        Fish fish = getMutantRepository().getFish(fishID);
        model.addAttribute("fish", fish);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Phenotype Summary");
        return "fish/fish-phenotype-figure-summary";
    }
}