package org.zfin.fish.presentation;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.expression.Figure;
import org.zfin.infrastructure.ZfinFigureEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * This class serves the phenotype summary page.
 */
@Controller
public class FishPhenotypeController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }


    private static Logger LOG = Logger.getLogger(FishPhenotypeController.class);

    /**
     * Search submission handling.
     *
     * @param fishID genotype ID or genox Ids
     * @param model  Model
     * @return view page
     * @throws Exception exception
     */
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
        Fish fish = FishService.getFish(fishID);
        // get the all phenotype per genotype experiment data
        GenotypeBean form = new GenotypeBean();
        form.setPhenoStatements(getMutantRepository().getPhenotypeStatementsByGenotypeExperiments(fish.getGenotypeExperimentIDs()));
        model.addAttribute("genotypeFormBean", form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Phenotype Summary");

        return "fish/fish-phenotype-figure-summary.page";
    }

    @RequestMapping("/fish-publication-list")
    public String fishCitationList(@RequestParam(value = "fishID", required = true) String fishID,
                                       Model model) throws Exception {

        Fish fish = getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);
        FishPublicationBean bean = new FishPublicationBean();
        bean.setFish(fish);
        model.addAttribute("formBean", bean);
        return "fish/fish-publication-list.page";
    }

    @RequestMapping(value = "/phenotype/figures", method = RequestMethod.GET)
    public String getFishPhenotypeFigsPopup(@RequestParam(value = "fishID", required = true) String fishID,
                                            @ModelAttribute("formBean") FishSearchFormBean formBean,
                                            Model model) throws Exception {
		LOG.info("Fish phenotype figures");
		FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        if (criteria == null)
            criteria = new FishSearchCriteria();

        Set<ZfinFigureEntity> zfinFigureEntities = getFishRepository().getFiguresByFishAndTerms(fishID, criteria.getPhenotypeAnatomyCriteria().getValues());

        if (zfinFigureEntities == null)
            return "record-not-found.popup";

        SortedSet<Figure> figures = new TreeSet<Figure>();
        for (ZfinFigureEntity figureEntity : zfinFigureEntities) {
           Figure figure = getPublicationRepository().getFigure(figureEntity.getID());
           figures.add(figure);
        }
        model.addAttribute("phenotypeFigures", figures);
        return "fish/fish-phenotype-figures.insert";
    }
}