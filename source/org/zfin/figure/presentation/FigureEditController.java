package org.zfin.figure.presentation;

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
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.ImageService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FigureEditController {

    public static final Logger LOG = Logger.getLogger(FigureEditController.class);

    @Autowired
    FigureRepository figureRepository;

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @Autowired
    PublicationRepository publicationRepository;

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/figures", method = RequestMethod.GET)
    public List<FigurePresentationBean> getFiguresForPub(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        return publication.getFigures().stream()
                .sorted(Comparator.comparing(Figure::getOrderingLabel).thenComparing(Figure::getZdbID))
                .map(FigureService::convertToFigurePresentationBean)
                .collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/figures", method = RequestMethod.POST)
    public FigurePresentationBean createNewFigure(@PathVariable String zdbID, @RequestParam String label,
                                                  @RequestParam String caption, @RequestParam List<MultipartFile> files) {

        Publication publication = publicationRepository.getPublication(zdbID);

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
            }
        }

        tx.commit();
        return FigureService.convertToFigurePresentationBean(newFigure);
    }

    @ResponseBody
    @RequestMapping(value = "/figure/{zdbID}", method = RequestMethod.DELETE)
    public String deleteFigure(@PathVariable String zdbID) {
        Figure figure = figureRepository.getFigure(zdbID);

        if (figure.getExpressionResults().size() > 0 || figure.getPhenotypeExperiments().size() > 0) {
            throw new InvalidWebRequestException("Figure has expression or phenotype data attached", null);
        }

        Transaction tx = HibernateUtil.createTransaction();
        infrastructureRepository.deleteActiveDataByZdbID(figure.getZdbID());
        tx.commit();

        return "OK";
    }

}
