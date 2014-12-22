package org.zfin.mutant.presentation;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.repository.ConstructService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * This class serves the phenotype summary page.
 */
@Controller
@RequestMapping("/mutant")
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

   @RequestMapping(value = "/construct-expression-image-exist", method = RequestMethod.GET)
    protected String imageExists(@RequestParam(value = "constructID", required = true) String constructID,
                                 @ModelAttribute("formBean") ConstructSearchFormBean formBean,
                                 Model model) throws Exception {

        boolean hasImages = ConstructService.hasImagesOnExpressionFigures(constructID);
        model.addAttribute("hasImages", hasImages);
        return "fish/image-camera-icon.popup";
    }

}
