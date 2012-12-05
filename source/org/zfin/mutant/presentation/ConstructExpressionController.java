package org.zfin.mutant.presentation;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureExpressionSummaryDisplay;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.fish.repository.FishService;
import org.zfin.marker.Marker;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.PhenotypeSummaryCriteria;
import org.zfin.mutant.repository.ConstructService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * This class serves the phenotype summary page.
 */
@Controller
public class ConstructExpressionController {

    @ModelAttribute("formBean")
    private ConstructSearchFormBean getDefaultSearchForm() {
        return new ConstructSearchFormBean();
    }


    private static Logger LOG = Logger.getLogger(ConstructExpressionController.class);

    /**
     * Search submission handling.
     *
     * @param  constructID
     * @param model  Model
     * @return view page
     * @throws Exception exception
     */
    @RequestMapping(value = "/construct-expression-summary", method = RequestMethod.GET)
    protected String showExpressionSummary(@RequestParam(value = "constructID", required = true) String constructID,
                                           @ModelAttribute("formBean") ConstructSearchFormBean formBean,
                                           Model model) throws Exception {

        LOG.info("Start Construct Expression Controller");
        Marker construct= RepositoryFactory.getMarkerRepository().getMarkerByID(constructID);
        if (construct == null)
            return "record-not-found.page";
        List<Marker> expressedGene=RepositoryFactory.getMarkerRepository().getCodingSequence(construct);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Construct expression figure summary: " + construct.getName());
        ConstructSearchCriteria criteria = new ConstructSearchCriteria(formBean);
        formBean.setConstructSearchCriteria(criteria);
        formBean.setConstructObj(construct);
        formBean.setExpressedGene(expressedGene);
        ConstructService constructStat = new ConstructService(construct);
        constructStat.createFigureSummary(criteria, constructID);
        formBean.setConstructService(constructStat);



        return "mutant/construct-expression-figure-summary.page";
    }

    private String figureViewPage(Figure figure) {
        String figureUrl = FigurePresentation.getUrl(figure);
        StringBuilder builder = new StringBuilder("redirect:" + figureUrl);
        return builder.toString();
    }

   @RequestMapping(value = "/construct-expression-image-exist", method = RequestMethod.GET)
    protected String imageExists(@RequestParam(value = "constructID", required = true) String constructID,
                                 @ModelAttribute("formBean") ConstructSearchFormBean formBean,
                                 Model model) throws Exception {

        boolean hasImages = ConstructService.hasImagesOnExpressionFigures(constructID);
        model.addAttribute("hasImages", hasImages);
        return "fish/image-camera-icon.popup";
    }

}