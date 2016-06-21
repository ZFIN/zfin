package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.Image;
import org.zfin.figure.service.FigureViewService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * For display of figure information
 */
@Controller
public class ImageViewController {

    private static Logger LOG = Logger.getLogger(ImageViewController.class);

    @Autowired
    private FigureViewService figureViewService;

    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

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

}
