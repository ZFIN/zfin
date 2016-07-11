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
import org.zfin.figure.presentation.FigureFromPublicationLink;
import org.zfin.figure.presentation.FigureGalleryImagePresentation;
import org.zfin.figure.service.FigureViewService;
import org.zfin.ontology.repository.OntologyRepository;
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

    @Autowired
    private OntologyRepository ontologyRepository;

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
                                       @RequestParam(required = false) String record,
                                       HttpServletResponse response) {
        Image image = publicationRepository.getImageById(zdbID);

        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        FigureGalleryImagePresentation bean = new FigureGalleryImagePresentation();
        bean.setImage(image);
        bean.setImageLinkEntity(image.getFigure() == null ? image : image.getFigure());

        Figure figure = image.getFigure();
        if (category.equals(Category.EXPRESSIONS.getName())) {
            bean.setFigureExpressionSummary(figureViewService.getFigureExpressionSummary(figure));
            bean.setTitleLinkEntity(new FigureFromPublicationLink(figure));
        } else if (category.equals(Category.PHENOTYPE.getName())) {
            bean.setFigurePhenotypeSummary(figureViewService.getFigurePhenotypeSummary(figure));
            bean.setTitleLinkEntity(new FigureFromPublicationLink(figure));
        } else if (category.equals(Category.PUBLICATION.getName())) {
            bean.setDetails(figure.getCaption());
            bean.setTitleLinkEntity(new FigureFromPublicationLink(figure));
        } else if (category.equals(Category.ANATOMY.getName())) {
            bean.setTitleLinkEntity(ontologyRepository.getTermByOboID(record));
        }

        model.addAttribute("bean", bean);
        return "figure/figure-summary.fragment";
    }

}
