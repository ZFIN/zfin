package org.zfin.fish.presentation;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureExpressionSummaryDisplay;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.publication.presentation.FigurePresentation;

import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * This class serves the phenotype summary page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishExpressionController {

    @ModelAttribute("formBean")
    private FishSearchFormBean getDefaultSearchForm() {
        return new FishSearchFormBean();
    }


    private static Logger LOG = Logger.getLogger(FishExpressionController.class);

    /**
     * Search submission handling.
     *
     * @param fishID genotype ID or genox Ids
     * @param model  Model
     * @return view page
     * @throws Exception exception
     */
    @RequestMapping(value = "/expression-summary", method = RequestMethod.GET)
    protected String showExpressionSummary(@RequestParam(value = "fishID", required = true) String fishID,
                                           @RequestParam(value = "geneID", required = false) String geneID,
                                           @ModelAttribute("formBean") FishSearchFormBean formBean,
                                           Model model) throws Exception {

        LOG.info("Start MartFish Phenotype Controller");
        FishSearchCriteria criteria = new FishSearchCriteria(formBean);
        List<FigureExpressionSummary> figureExpressionSummaryList = FishService.getExpressionSummary(fishID, geneID);
        Collections.sort(figureExpressionSummaryList);
        model.addAttribute("figureCount", figureExpressionSummaryList.size());
        List<FigureExpressionSummaryDisplay> figureExpressionSummaryDisplayList = PresentationConverter.getFigureExpressionSummaryDisplay(figureExpressionSummaryList);
        // redirect to figure view page if only a single figure available
        if (figureExpressionSummaryList != null && figureExpressionSummaryList.size() == 1)
            return figureViewPage(figureExpressionSummaryList.get(0).getFigure());
        model.addAttribute(figureExpressionSummaryDisplayList);
        PhenotypeSummaryCriteria summaryCriteria = FishService.getPhenotypeSummaryCriteria(fishID);
        summaryCriteria.setCriteria(criteria);
        model.addAttribute(summaryCriteria);
        MartFish fish = FishService.getFish(fishID);
        GenotypeBean form = new GenotypeBean();
//        form.setExpressionStatements(getMutantRepository().getExpressionStatementsByGenotypeExperiments(fish.getGenotypeExperimentIDs()));
        model.addAttribute(form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Expression Summary: " + fish.getName());

        return "fish/fish-expression-figure-summary.page";
    }

    private String figureViewPage(Figure figure) {
        String figureUrl = FigurePresentation.getUrl(figure);
        StringBuilder builder = new StringBuilder("redirect:" + figureUrl);
        return builder.toString();
    }

    @RequestMapping(value = "/expression-image-exist", method = RequestMethod.GET)
    protected String imageExists(@RequestParam(value = "fishID", required = true) String fishID,
                                 @ModelAttribute("formBean") FishSearchFormBean formBean,
                                 Model model) throws Exception {

        boolean hasImages = FishService.hasImagesOnExpressionFigures(fishID);
        model.addAttribute("hasImages", hasImages);
        return "fish/image-camera-icon.popup";
    }

}