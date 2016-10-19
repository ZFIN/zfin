package org.zfin.figure.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Figure;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.ComparatorCreator;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

@Controller
@RequestMapping("/figure")
public class FigureViewController {

    @Autowired
    private FigureViewService figureViewService;

    @Autowired
    private FigureRepository figureRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    // get together all of the data that you need later in the JSP it returns
    @RequestMapping("/view/{zdbID}")
    public String getFigureView(Model model, @PathVariable("zdbID") String zdbID) {

        Figure figure = figureRepository.getFigure(zdbID);

        if (figure == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("figure", figure);
        Clone probe = figureViewService.getProbeForFigure(figure);
        model.addAttribute("probe", probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }

        List<PhenotypeWarehouse> warehouseList = getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID());
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
        model.addAttribute("phenotypeTableRows", phenotypeTableRows);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Figure: " + figureViewService.getFullFigureLabel(figure));

        model.addAttribute("showElsevierMessage", figureViewService.showElsevierMessage(figure.getPublication()));
        model.addAttribute("hasAcknowledgment", figureViewService.hasAcknowledgment(figure.getPublication()));

        return "figure/figure-view.page";
    }


    // get together all of the data that you need later in the JSP it returns
    @RequestMapping("/all-figure-view/{zdbID}")
    public String getAllFigureView(Model model,
                                   @PathVariable("zdbID") String zdbID,
                                   @RequestParam(value = "probeZdbID", required = false) String probeZdbID) {

        Publication publication = publicationRepository.getPublication(zdbID);

        if (publication == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("showElsevierMessage", figureViewService.showElsevierMessage(publication));
        model.addAttribute("hasAcknowledgment", figureViewService.hasAcknowledgment(publication));
        model.addAttribute("showMultipleMediumSizedImages", figureViewService.showMultipleMediumSizedImages(publication));
        //for direct submission pubs, publication.getFigures() won't be correct and we'll need to do a query...
        List<Figure> figures = new ArrayList<>();

        //also for direct submission pubs, we should see if we got a probe
        Clone probe = null;
        if (!StringUtils.isEmpty(probeZdbID)) {
            probe = RepositoryFactory.getMarkerRepository().getCloneById(probeZdbID);
        }
        model.addAttribute("probe", probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }
        if (publication.isUnpublished()) {
            if (StringUtils.isEmpty(probeZdbID)) {
                return "redirect:/" + publication.getZdbID();
            } else {
                figures.addAll(figureRepository.getFiguresForDirectSubmissionPublication(publication, probe));
            }
        } else {
            figures.addAll(publication.getFigures());
        }

        Collections.sort(figures, ComparatorCreator.orderBy("orderingLabel", "zdbID"));

        model.addAttribute("figures", figures);

        model.addAttribute("submitters", figureRepository.getSubmitters(publication, probe));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(publication));
        model.addAttribute("showErrataAndNotes", figureViewService.showErrataAndNotes(publication));

        Map<Figure, FigureExpressionSummary> expressionSummaryMap = new HashMap<>();
        Map<Figure, FigurePhenotypeSummary> phenotypeSummaryMap = new HashMap<>();
        for (Figure figure : figures) {
            expressionSummaryMap.put(figure, figureViewService.getFigureExpressionSummary(figure));
            phenotypeSummaryMap.put(figure, figureViewService.getFigurePhenotypeSummary(figure));
        }
        model.addAttribute("expressionSummaryMap", expressionSummaryMap);
        model.addAttribute("phenotypeSummaryMap", phenotypeSummaryMap);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Figures, " + publication.getShortAuthorList());

        return "figure/all-figure-view.page";

    }


}
