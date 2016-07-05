package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.figure.service.FigureViewService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.search.Category;

import javax.servlet.http.HttpServletResponse;

/**
 * For display of figure information
 */
@Controller
public class ImageViewController {

    private static Logger LOG = Logger.getLogger(ImageViewController.class);

    @Autowired
    private FigureViewService figureViewService;

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping("/publication/image-popup/{zdbID}")
    public String updateOrthologyNote(@PathVariable String zdbID,
                                      @ModelAttribute("formBean") ImageViewBean form) throws Exception {
        LOG.info("Start Image View Controller");
        Image image = publicationRepository.getImageById(zdbID);
        if (image == null) {
            return null;
        }

        LOG.debug("Image.Figure zdbID: " + image.getFigure().getZdbID());
        form.setImage(image);
        form.setExpressionGenes(figureViewService.getExpressionGenes(image.getFigure()));
        return "image-popup.page";
    }

    @RequestMapping("/image/{zdbID}/summary")
    public String getImageSummaryPopup(Model model,
                                       @PathVariable("zdbID") String zdbID,
                                       @RequestParam(required = false) String category,
                                       HttpServletResponse response) {
        Image image = publicationRepository.getImageById(zdbID);

        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        model.addAttribute("image", image);

        Figure figure = image.getFigure();
        if (category.equals(Category.EXPRESSIONS.getName())) {
            model.addAttribute("expressionSummary", figureViewService.getFigureExpressionSummary(figure));
            model.addAttribute("showDetails", true);
        } else if (category.equals(Category.PHENOTYPE.getName())) {
            model.addAttribute("phenotypeSummary", figureViewService.getFigurePhenotypeSummary(figure));
            model.addAttribute("showDetails", true);
        } else if (category.equals(Category.PUBLICATION.getName())) {
            model.addAttribute("figureCaption", figure.getCaption());
            model.addAttribute("showDetails", true);
        }

        return "figure/figure-summary.fragment";
    }

}
