package org.zfin.publication.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.expression.Image;
import org.zfin.expression.FigureService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * For display of figure information
 */
public class ImageViewController extends AbstractCommandController {
    private static Logger LOG = Logger.getLogger(PublicationSearchResultController.class);
    private String successView;
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    
    public ImageViewController() {
        setCommandClass(ImageViewBean.class);
    }
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Image View Controller");
        ImageViewBean form = (ImageViewBean)command;
        getFigureData(form);
        return new ModelAndView(successView, LookupStrings.FORM_BEAN, form);
    }


    public Image getFigureData(ImageViewBean form) {


        String zdbID = form.getImage().getZdbID();
        LOG.debug("Image zdbID: " + zdbID);

        Image image = publicationRepository.getImageById(zdbID);

        if (image == null) return null;

        LOG.debug("Image.Figure zdbID: " + image.getFigure().getZdbID());

        form.setImage(image);

        form.setExpressionGenes(FigureService.getExpressionGenes(image.getFigure()));
        

        return image;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }


}
