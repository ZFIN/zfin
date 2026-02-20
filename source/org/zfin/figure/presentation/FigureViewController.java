package org.zfin.figure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.Figure;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.marker.Clone;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

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
    public String getFigureViewPrototype(Model model, @PathVariable("zdbID") String zdbID) {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        Figure figure = figureRepository.getFigure(zdbID);

        if (figure == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getWithdrawnZdbID(zdbID);
            if (replacedZdbID != null) {

                return "redirect:/" + replacedZdbID;
            } else {
                model.addAttribute(LookupStrings.ZDB_ID, zdbID);
                return LookupStrings.RECORD_NOT_FOUND_PAGE;
            }
        }

        model.addAttribute("figure", figure);
        Clone probe = figureViewService.getProbeForFigure(figure);
        model.addAttribute("probe", probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }

        List<Figure> otherFigures = figureViewService.getOrderedFiguresForPublication(figure.getPublication());
        model.addAttribute("otherFigures", otherFigures);

        List<PhenotypeWarehouse> warehouseList = getPhenotypeRepository().getPhenotypeWarehouse(figure.getZdbID());
        FigureExpressionSummary expressionSummary = figureViewService.getFigureExpressionSummary(figure);
        model.addAttribute("expressionSummary", expressionSummary);
        model.addAttribute("phenotypeSummary", figureViewService.getFigurePhenotypeSummary(figure));

        model.addAttribute("submitters", figureRepository.getSubmitters(figure.getPublication(), expressionSummary.getProbe()));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(figure.getPublication()));
        model.addAttribute("showErrataAndNotes", figureViewService.showErrataAndNotes(figure.getPublication()));
        model.addAttribute("showMultipleMediumSizedImages", figureViewService.showMultipleMediumSizedImages(figure.getPublication()));

        List<AntibodyTableRow> antibodyTableRows = figureViewService.getAntibodyTableRows(figure);
        model.addAttribute("antibodyTableRows", antibodyTableRows);
        model.addAttribute("showAntibodyQualifierColumn", figureViewService.showAntibodyQualifierColumn(antibodyTableRows));

        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList);
        model.addAttribute("phenotypeTableRows", phenotypeTableRows);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Figure: " + figureViewService.getFullFigureLabel(figure));

        model.addAttribute("showElsevierMessage", figureViewService.showElsevierMessage(figure.getPublication()));
        model.addAttribute("hasAcknowledgment", figureViewService.hasAcknowledgment(figure.getPublication()));
        model.addAttribute("isLargeDataPublication", figureViewService.isLargeDataPublication(figure.getPublication()));
        return "figure/figure-view";
    }

}
