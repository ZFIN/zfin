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
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.List;

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
}
