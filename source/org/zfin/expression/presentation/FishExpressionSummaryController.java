package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.*;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.List;

import static org.zfin.expression.FigureService.getFigureSummaryDisplays;
import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Controller for display of expression figure summary pages linked from the genotype
 * page and genotype expression details page
 */
@Controller
@RequestMapping("/expression")
public class FishExpressionSummaryController {


    @RequestMapping(value = {"/fish-expression-figure-summary-experiment"})
    protected String getFishExpressionFigureSummaryNoneStandard(@RequestParam String fishZdbID,
                                                        @RequestParam String expZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        FishExperiment genox = getMutantRepository().getFishExperimentByFishAndExperimentID(fishZdbID, expZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        //I would prefer the record not found show both ids...maybe it would be best if we just used genox?
        if (genox == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteria(genox, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genox.getFish().getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary";
    }


    @RequestMapping(value = {"/fish-expression-figure-summary-standard"})
    protected String getFishExpressionFigureSummaryStandard(@RequestParam String fishZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        Fish fish = getMutantRepository().getFish(fishZdbID);

        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(fish, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fish.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary";

    }


    //Example: /action/expression/fish-expression-figure-summary?fishID=ZDB-FISH-171026-19&imagesOnly=false
    @RequestMapping(value = {"/fish-expression-figure-summary"})
    protected String getExpressionFigureSummaryForFish(@RequestParam String fishID,
                                                       @RequestParam boolean imagesOnly,
                                                       Model model) {

        Fish fish = getMutantRepository().getFish(fishID);

        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(fish, null, imagesOnly);
        expressionCriteria.setShowCondition(false);
        model.addAttribute("expressionCriteria", expressionCriteria);

        List<ExpressionFigureStage> figureStages = getExpressionRepository().getExpressionFigureStagesByFish(fish);
        List<FigureExpressionSummary> figureExpressionSummaries = ExpressionService.createExpressionFigureSummaryFromExpressionResults(figureStages);
        Collections.sort(figureExpressionSummaries);
        List<FigureExpressionSummaryDisplay> figureExpressionSummaryDisplayList = PresentationConverter.getFigureExpressionSummaryDisplay(figureExpressionSummaries);

        model.addAttribute("figureCount", getExpressionRepository().getExpressionFigureCountForFish(fish));
        model.addAttribute("figureSummaryDisplayList", figureExpressionSummaryDisplayList);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fish.getName() + " Expression Figure Summary");
        return "expression/fish-expression-figure-summary";
    }

    //Example: /action/expression/fish-expression-figure-by-ids?fishID=ZDB-FISH-231110-1&geneID=ZDB-EFG-070117-1&conditionID=ZDB-EXP-240503-6&figIDs=ZDB-FIG-220810-54,ZDB-FIG-241002-43
    @RequestMapping(value = {"/fish-expression-figure-by-ids"})
    protected String getExpressionFiguresByIDs(@RequestParam String fishID,
                                                       @RequestParam String geneID,
                                                       @RequestParam String conditionID,
                                                       @RequestParam String figIDs,
                                                       Model model) {

        Fish fish = getMutantRepository().getFish(fishID);

        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, fishID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneID);
        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Experiment experiment = getExpressionRepository().getExperimentByID(conditionID);
        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, conditionID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (figIDs == null || figIDs.isEmpty()) {
            model.addAttribute(LookupStrings.ZDB_ID, fishID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<String> figureIDs = List.of(figIDs.split(","));
        if (figureIDs.isEmpty()) {
            model.addAttribute(LookupStrings.ZDB_ID, fishID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<ExpressionFigureStage> figureStages = getExpressionRepository().getExpressionFigureStagesByFish(fish);
        List<ExpressionFigureStage> filteredFigureStages = figureStages
                .stream()
                .filter(efs -> figureIDs.contains(efs.getFigure().getZdbID()))
                .filter(efs -> efs.getExpressionExperiment().getGene().getZdbID().equals(geneID))
                .toList();

        List<Figure> figures = filteredFigureStages.stream().map(ExpressionFigureStage::getFigure).toList();

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(fish, gene, false);

        GenericTerm zecoTerm = experiment.getExperimentConditions().stream().findFirst().get().getZecoTerm();
        if (zecoTerm != null && "ZECO:0000238".equals(zecoTerm.getOboID())) {
            expressionCriteria.setChemicalEnvironment(true);
            expressionCriteria.setStandardEnvironment(false);
        }

        List<FigureSummaryDisplay> displays = getFigureSummaryDisplays(expressionCriteria, figures);

        model.addAttribute("figureSummaryDisplayList", displays);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fish.getName() + " Expression Figure Summary");

        model.addAttribute("expressionCriteria", expressionCriteria);

        return "expression/genotype-figure-summary";
    }
}
