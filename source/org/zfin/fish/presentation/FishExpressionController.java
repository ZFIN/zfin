package org.zfin.fish.presentation;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Figure;
import org.zfin.fish.repository.FishService;
import org.zfin.publication.presentation.FigurePresentation;

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


    private static Logger LOG = LogManager.getLogger(FishExpressionController.class);

    /**
     * Search submission handling.
     *
     * @param fishID genotype ID or genox Ids
     * @param model  Model
     * @return view page
     */
    @RequestMapping(value = "/expression-summary", method = RequestMethod.GET)
    protected String showExpressionSummary(@RequestParam(value = "fishID", required = true) String fishID,
                                           @RequestParam(value = "geneID", required = false) String geneID,
                                           @ModelAttribute("formBean") FishSearchFormBean formBean,
                                           Model model) {

        return "fish/fish-expression-figure-summary";
    }

    private String figureViewPage(Figure figure) {
        String figureUrl = FigurePresentation.getUrl(figure);
        StringBuilder builder = new StringBuilder("redirect:" + figureUrl);
        return builder.toString();
    }

    @RequestMapping(value = "/expression-image-exist", method = RequestMethod.GET)
    protected String imageExists(@RequestParam(value = "fishID") String fishID,
                                 @ModelAttribute("formBean") FishSearchFormBean formBean,
                                 Model model) throws Exception {

        boolean hasImages = FishService.hasImagesOnExpressionFigures(fishID);
        model.addAttribute("hasImages", hasImages);
        return "fish/image-camera-icon";
    }

}