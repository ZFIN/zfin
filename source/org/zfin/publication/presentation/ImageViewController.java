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
import org.zfin.figure.presentation.*;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.search.Category;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFigureRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * For display of figure information
 */
@Controller
@RequestMapping("/image")
public class ImageViewController {

    private static Logger LOG = Logger.getLogger(ImageViewController.class);

    @Autowired
    private FigureViewService figureViewService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private OntologyRepository ontologyRepository;

   /* @RequestMapping("/view/{zdbID}")
    public String getImageView(Model model, @PathVariable("zdbID") String zdbID) {

        Image image = publicationRepository.getImageById(zdbID);
        if (image == null) {
            return null;
        }


        model.addAttribute("image", image);

       *//* List<PhenotypeWarehouse> warehouseList = getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID());
        FigureExpressionSummary expressionSummary = figureViewService.getFigureExpressionSummary(figure);
        model.addAttribute("expressionSummary", expressionSummary);
        model.addAttribute("phenotypeSummary", figureViewService.getFigurePhenotypeSummary(figure));

        model.addAttribute("submitters", figureRepository.getSubmitters(figure.getPublication(), expressionSummary.getProbe()));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(figure.getPublication()));
        model.addAttribute("showErrataAndNotes", figureViewService.showErrataAndNotes(figure.getPublication()));
        model.addAttribute("showMultipleMediumSizedImages", figureViewService.showMultipleMediumSizedImages(figure.getPublication()));

        List<ExpressionTableRow> expressionTableRows = figureViewService.getExpressionTableRows(figure);
        model.addAttribute("expressionTableRows", expressionTableRows);
        model.addAttribute("showExpressionQualifierColumn", figureViewService.showExpressionQualifierColumn(expressionTableRows));

        List<AntibodyTableRow> antibodyTableRows = figureViewService.getAntibodyTableRows(figure);
        model.addAttribute("antibodyTableRows", antibodyTableRows);
        model.addAttribute("showAntibodyQualifierColumn", figureViewService.showAntibodyQualifierColumn(antibodyTableRows));

        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList);
        model.addAttribute("phenotypeTableRows", phenotypeTableRows);*//*

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Image: " + figureViewService.getFullFigureLabel(image.getFigure()));
        model.addAttribute("expressionGeneList",figureViewService.getExpressionGenes(image.getFigure()));
        model.addAttribute("antibodyList",figureViewService.getAntibodies(image.getFigure()));

       *//* model.addAttribute("showElsevierMessage", figureViewService.showElsevierMessage(figure.getPublication()));
        model.addAttribute("hasAcknowledgment", figureViewService.hasAcknowledgment(figure.getPublication()));*//*

        return "image/image-view.page";
    }*/



    /*@RequestMapping("/publication/image-popup/{zdbID}")
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
*/
   /* @RequestMapping("/image/{zdbID}/summary")
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

        Figure figure = image.getFigure();
        FigureGalleryImagePresentation bean = new FigureGalleryImagePresentation();
        bean.setImage(image);
        bean.setImageLinkEntity(image.getFigure() == null ? image : image.getFigure());
        bean.setTitleLinkEntity(new FigureFromPublicationLink(figure));

        switch (Category.getCategory(category)) {
            case EXPRESSIONS:
                bean.setFigureExpressionSummary(figureViewService.getFigureExpressionSummary(figure));
                break;

            case PHENOTYPE:
                bean.setFigurePhenotypeSummary(figureViewService.getFigurePhenotypeSummary(figure));
                break;

            case PUBLICATION:
                bean.setDetails(figure.getCaption());
                break;

            // warning: intentional fallthrough ahead!
            case REPORTER_LINE:
            case FIGURE:
            case FISH:
                bean.setFigureExpressionSummary(figureViewService.getFigureExpressionSummary(figure));
                bean.setFigurePhenotypeSummary(figureViewService.getFigurePhenotypeSummary(figure));
                break;

            case ANATOMY:
                GenericTerm term = ontologyRepository.getTermByOboID(record);
                bean.setTitleLinkEntity(term);
                bean.setDetails(term.getDefinition());
        }

        model.addAttribute("bean", bean);
        return "figure/figure-summary.fragment";
    }*/

}
