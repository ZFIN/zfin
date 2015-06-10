package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.FigureService;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
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
public class GenotypeExpressionSummaryController {


    @RequestMapping(value = {"/genotype-figure-summary"})
    protected String getGenotypeExpressionFigureSummary(@RequestParam String genoZdbID,
                                                        @RequestParam String expZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        FishExperiment genox = getMutantRepository().getGenotypeExperiment(genoZdbID, expZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        //I would prefer the record not found show both ids...maybe it would be best if we just used genox?
        if (genox == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
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
        return "expression/genotype-figure-summary.page";

    }


    @RequestMapping(value = {"/genotype-figure-summary-standard"})
    protected String getGenotypeExpressionFigureSummary(@RequestParam String genoZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        Genotype geno = getMutantRepository().getGenotypeByID(genoZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        if (geno == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(geno, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, geno.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }

    @RequestMapping("/genotype-figure-summary-chemical")
    protected String getGenotypeExpressionFigureSummaryChemical(@RequestParam String genoZdbID,
                                                                @RequestParam String geneZdbID,
                                                                @RequestParam boolean imagesOnly,
                                                                Model model) {
        Genotype geno = getMutantRepository().getGenotypeByID(genoZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        if (geno == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaChemicalEnvironment(geno, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, geno.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }

    @RequestMapping(value = {"/sequence-targeting-reagent-expression-figure-summary"})
    protected String getSequenceTargetingReagentExpressionFigureSummary(@RequestParam String strZdbID,
                                                                        @RequestParam String geneZdbID,
                                                                        @RequestParam boolean imagesOnly,
                                                                        Model model) {


        SequenceTargetingReagent sequenceTargetingReagent = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(strZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);

        if (sequenceTargetingReagent == null) {
            model.addAttribute(LookupStrings.ZDB_ID, strZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaSTR(sequenceTargetingReagent, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, sequenceTargetingReagent.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }

    @RequestMapping(value = {"/genotype-expression-figure-summary"})
    protected String getExpressionFigureSummaryForGenotype(@RequestParam String genoZdbID,
                                                           @RequestParam boolean imagesOnly,
                                                           Model model) {

        Genotype genotype = getMutantRepository().getGenotypeByID(genoZdbID);

        if (genotype == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(genotype, null, imagesOnly);
        expressionCriteria.setShowCondition(false);
        model.addAttribute("expressionCriteria", expressionCriteria);

        // use Fish instead of genotupe
        List<ExpressionResult> expressionResults = getExpressionRepository().getExpressionResultsByFish(null);

        List<FigureExpressionSummary> figureExpressionSummaries = ExpressionService.createExpressionFigureSummaryFromExpressionResults(expressionResults);

        Collections.sort(figureExpressionSummaries);

        List<FigureExpressionSummaryDisplay> figureExpressionSummaryDisplayList = PresentationConverter.getFigureExpressionSummaryDisplay(figureExpressionSummaries);

        model.addAttribute("figureCount", getExpressionRepository().getExpressionFigureCountForGenotype(genotype));

        model.addAttribute("figureSummaryDisplayList", figureExpressionSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotype.getName() + " Expression Figure Summary");
        return "expression/genotype-expression-figure-summary.page";

    }

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

        List<ExpressionResult> expressionResults = getExpressionRepository().getExpressionResultsByFish(fish);
        List<FigureExpressionSummary> figureExpressionSummaries = ExpressionService.createExpressionFigureSummaryFromExpressionResults(expressionResults);
        Collections.sort(figureExpressionSummaries);
        List<FigureExpressionSummaryDisplay> figureExpressionSummaryDisplayList = PresentationConverter.getFigureExpressionSummaryDisplay(figureExpressionSummaries);

        model.addAttribute("figureCount", getExpressionRepository().getExpressionFigureCountForFish(fish));
        model.addAttribute("figureSummaryDisplayList", figureExpressionSummaryDisplayList);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fish.getName() + " Expression Figure Summary");
        return "expression/fish-expression-figure-summary.page";

    }

}
