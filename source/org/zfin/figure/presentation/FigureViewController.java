package org.zfin.figure.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.ComparatorCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
public class FigureViewController {

    @Autowired
    private FigureViewService figureViewService;

    private FigureRepository figureRepository = RepositoryFactory.getFigureRepository();

    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();


    // get together all of the data that you need later in the JSP it returns
    @RequestMapping("/figure-view/{zdbID}")
    public String getFigureView(Model model, @PathVariable("zdbID") String zdbID) {

        Figure figure = figureRepository.getFigure(zdbID);

        if (figure == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Clone probe = figureViewService.getProbeForFigure(figure);
        model.addAttribute("probe", probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }

        model.addAttribute("submitters", figureRepository.getSubmitters(figure.getPublication(), probe));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(figure.getPublication()));
        model.addAttribute("expressionGenes",figureViewService.getExpressionGenes(figure));
        model.addAttribute("expressionAntibodies",figureViewService.getAntibodies(figure));
        model.addAttribute("expressionFishesAndGenotypes",figureViewService.getExpressionFishesAndGenotypes(figure));
        model.addAttribute("expressionSTRs",figureViewService.getExpressionSTR(figure));
        model.addAttribute("expressionConditions", figureViewService.getExpressionCondition(figure)); // conditions are actually List<Experiment>
        model.addAttribute("expressionEntities",figureViewService.getExpressionEntities(figure));
        model.addAttribute("expressionStartStage",figureViewService.getExpressionStartStage(figure));
        model.addAttribute("expressionEndStage",figureViewService.getExpressionEndStage(figure));

        //fishes, STRs, conditions, terms, stages
        model.addAttribute("phenotypeFishesAndGenotypes",figureViewService.getPhenotypeFishesAndGenotypes(figure));
        model.addAttribute("phenotypeSTRs",figureViewService.getPhenotypeSTR(figure));
        model.addAttribute("phenotypeConditions",figureViewService.getPhenotypeCondition(figure));
        model.addAttribute("phenotypeEntities",figureViewService.getPhenotypeEntities(figure));
        model.addAttribute("phenotypeStartStage",figureViewService.getPhenotypeStartStage(figure));
        model.addAttribute("phenotypeEndStage",figureViewService.getPhenotypeEndStage(figure));

        List<ExpressionTableRow> expressionTableRows = figureViewService.getExpressionTableRows(figure);
        model.addAttribute("expressionTableRows",expressionTableRows);
        model.addAttribute("showExpressionQualifierColumn",figureViewService.showExpressionQualifierColumn(expressionTableRows));

        List<AntibodyTableRow> antibodyTableRows = figureViewService.getAntibodyTableRows(figure);
        model.addAttribute("antibodyTableRows",antibodyTableRows);
        model.addAttribute("showAntibodyQualifierColumn",figureViewService.showAntibodyQualifierColumn(antibodyTableRows));

        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(figure);
        model.addAttribute("phenotypeTableRows", phenotypeTableRows);

        model.addAttribute("figure",figure);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Figure: " + figureViewService.getFullFigureLabel(figure));

        model.addAttribute("showElsevierMessage",figureViewService.showElsevierMessage(figure.getPublication()));
        model.addAttribute("hasAcknowledgment",figureViewService.hasAcknowledgment(figure.getPublication()));

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
        model.addAttribute("showElsevierMessage",figureViewService.showElsevierMessage(publication));
        model.addAttribute("hasAcknowledgment",figureViewService.hasAcknowledgment(publication));

        //for direct submission pubs, publication.getFigures() won't be correct and we'll need to do a query...
        List<Figure> figures = new ArrayList<>();

        //also for direct submission pubs, we should see if we got a probe
        Clone probe = null;
        if (!StringUtils.isEmpty(probeZdbID))
            probe = RepositoryFactory.getMarkerRepository().getCloneById(probeZdbID);
        model.addAttribute("probe",probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }
        if (StringUtils.equals(publication.getType(),Publication.UNPUBLISHED)
                || StringUtils.equals(publication.getType(), Publication.CURATION) ) {
            if (StringUtils.isEmpty(probeZdbID)) {
                return "redirect:/" + publication.getZdbID();
            } else {
                figures.addAll(figureRepository.getFiguresForDirectSubmissionPublication(publication, probe));
            }
        } else {
            figures.addAll(publication.getFigures());
        }

        Collections.sort(figures,ComparatorCreator.orderBy("orderingLabel","zdbID"));

        model.addAttribute("figures", figures);

        model.addAttribute("submitters", figureRepository.getSubmitters(publication, probe));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(publication));

        model.addAttribute("expressionGeneMap", figureViewService.getExpressionGenes(figures));
        model.addAttribute("expressionAntibodyMap", figureViewService.getAntibodies(figures));
        model.addAttribute("expressionFishesAndGenotypeMap",figureViewService.getExpressionFishesAndGenotypes(figures));
        model.addAttribute("expressionSTRMap",figureViewService.getExpressionSTRs(figures));
        model.addAttribute("expressionConditionMap", figureViewService.getExpressionConditions(figures));
        model.addAttribute("expressionEntityMap", figureViewService.getExpressionEntities(figures));
        model.addAttribute("expressionStartStageMap", figureViewService.getExpressionStartStages(figures));
        model.addAttribute("expressionEndStageMap", figureViewService.getExpressionStartStages(figures));

        model.addAttribute("phenotypeFishesAndGenotypeMap",figureViewService.getPhenotypeFishesAndGenotypes(figures));
        model.addAttribute("phenotypeConditionMap", figureViewService.getPhenotypeConditions(figures));
        model.addAttribute("phenotypeSTRMap", figureViewService.getPhenotypeSTRs(figures));
        model.addAttribute("phenotypeEntitiesMap", figureViewService.getPhenotypeEntities(figures));
        model.addAttribute("phenotypeStartStageMap", figureViewService.getPhenotypeStartStage(figures));
        model.addAttribute("phenotypeEndStageMap", figureViewService.getPhenotypeEndStage(figures));

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Figures, " + publication.getShortAuthorList());

        return "figure/all-figure-view.page";

    }


}
