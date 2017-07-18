package org.zfin.figure.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.expression.FigureService;
import org.zfin.expression.Image;
import org.zfin.figure.service.ImageService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FigureEditController {

    public static final Logger LOG = Logger.getLogger(FigureEditController.class);

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PublicationService publicationService;

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/figures", method = RequestMethod.GET)
    public PublicationFigureSet getFiguresForPub(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        PublicationFigureSet pubFigures = new PublicationFigureSet();
        pubFigures.setPubCanShowImages(publication.isCanShowImages());
        pubFigures.setFigures(
                publication.getFigures().stream()
                        .sorted(Comparator.comparing(Figure::getOrderingLabel).thenComparing(Figure::getZdbID))
                        .map(FigureService::convertToFigurePresentationBean)
                        .collect(Collectors.toList())
        );
        return pubFigures;
    }

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/figures", method = RequestMethod.POST)
    public FigurePresentationBean createNewFigure(@PathVariable String zdbID, @RequestParam String label,
                                                  @RequestParam String caption, @RequestParam List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (!file.getContentType().startsWith("image/")) {
                throw new InvalidWebRequestException("All files must be images. Invalid file type:" + file.getOriginalFilename());
            }
        }

        Publication publication = publicationRepository.getPublication(zdbID);

        if (publication == null) {
            throw new InvalidWebRequestException("Invalid publication");
        }

        if (publicationService.publicationHasFigureWithLabel(publication, label)) {
            throw new InvalidWebRequestException(label + " already exists");
        }

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Figure newFigure = new FigureFigure();
        newFigure.setLabel(label);
        newFigure.setCaption(caption);
        newFigure.setPublication(publication);
        session.save(newFigure);

        for (MultipartFile file : files) {
            try {
                ImageService.processImage(newFigure, file, ProfileService.getCurrentSecurityUser());
            } catch (IOException e) {
                LOG.error("Error processing image", e);
                throw new InvalidWebRequestException("Error processing image");
            }
        }

        infrastructureRepository.insertUpdatesTable(publication, "fig_zdb_id", "create new record", newFigure.getZdbID(), null);

        tx.commit();
        return FigureService.convertToFigurePresentationBean(newFigure);
    }

    @ResponseBody
    @RequestMapping(value = "/figure/{zdbID}", method = RequestMethod.DELETE)
    public String deleteFigure(@PathVariable String zdbID) {
        Figure figure = publicationRepository.getFigure(zdbID);
        Publication pub = figure.getPublication();

        if (CollectionUtils.isNotEmpty(figure.getExpressionResults()) ||
                CollectionUtils.isNotEmpty(figure.getPhenotypeExperiments())) {
            throw new InvalidWebRequestException("Figure has expression or phenotype data attached", null);
        }

        Transaction tx = HibernateUtil.createTransaction();
        infrastructureRepository.deleteActiveDataByZdbID(figure.getZdbID());
        infrastructureRepository.insertUpdatesTable(pub, "figure", "deleted", null, zdbID);
        tx.commit();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/figure/{zdbID}", method = RequestMethod.POST)
    public FigurePresentationBean updateFigure(@PathVariable String zdbID,
                                               @RequestBody FigurePresentationBean figureUpdates) {
        Figure figure = publicationRepository.getFigure(zdbID);

        if (!StringUtils.equals(figure.getLabel(), figureUpdates.getLabel()) &&
                publicationService.publicationHasFigureWithLabel(figure.getPublication(), figureUpdates.getLabel())) {
            throw new InvalidWebRequestException(figureUpdates.getLabel() + " already exists");
        }

        String oldValue = getFigureUpdateValue(figure);

        figure.setCaption(figureUpdates.getCaption());
        figure.setLabel(figureUpdates.getLabel());

        Transaction tx = HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(figure);

        String newValue = getFigureUpdateValue(figure);
        infrastructureRepository.insertUpdatesTable(figure.getPublication(), "figure", "update", newValue, oldValue);
        tx.commit();

        return FigureService.convertToFigurePresentationBean(figure);
    }

    @ResponseBody
    @RequestMapping(value = "/image/{zdbID}", method = RequestMethod.DELETE)
    public String deleteImage(@PathVariable String zdbID) {
        Image image = publicationRepository.getImageById(zdbID);
        Publication pub = image.getFigure().getPublication();

        Transaction tx = HibernateUtil.createTransaction();
        HibernateUtil.currentSession().delete(image);
        infrastructureRepository.insertUpdatesTable(pub, "img_zdb_id", "deleted", null, zdbID);
        tx.commit();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/figure/{zdbID}/images", method = RequestMethod.POST)
    public ImagePresentationBean addImage(@PathVariable String zdbID, @RequestParam MultipartFile file) {
        if (!file.getContentType().startsWith("image/")) {
            throw new InvalidWebRequestException("File must be an image");
        }

        Figure figure = publicationRepository.getFigure(zdbID);
        Image image;

        Transaction tx = HibernateUtil.createTransaction();
        try {
            image = ImageService.processImage(figure, file, ProfileService.getCurrentSecurityUser());
        } catch (IOException e) {
            LOG.error("Error processing image", e);
            throw new InvalidWebRequestException("Error processing image");
        }
        tx.commit();

        return FigureService.convertToImagePresentationBean(image);
    }

    private String getFigureUpdateValue(Figure figure) {
        return figure.getZdbID() + "<BR>" +
                figure.getLabel() + "<BR>" +
                figure.getCaption();
    }

}
