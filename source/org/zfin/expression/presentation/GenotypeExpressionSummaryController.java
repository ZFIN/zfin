package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.expression.FigureService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Controller for display of expression figure summary pages linked from the genotype
 * page and genotype expression details page
 */
@Controller
public class GenotypeExpressionSummaryController   {


    @RequestMapping(value = { "/genotype-figure-summary" } )
    protected String getGenotypeExpressionFigureSummary(@RequestParam String genoZdbID,
                                                        @RequestParam String expZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        GenotypeExperiment genox = RepositoryFactory.getMutantRepository().getGenotypeExperiment(genoZdbID, expZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);
        
        //I would prefer the record not found show both ids...maybe it would be best if we just used genox?
        if (genox == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteria(genox, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genox.getGenotype().getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }


    @RequestMapping(value = { "/genotype-figure-summary-standard" } )
    protected String getGenotypeExpressionFigureSummary(@RequestParam String genoZdbID,
                                                        @RequestParam String geneZdbID,
                                                        @RequestParam boolean imagesOnly,
                                                        Model model) {


        Genotype geno = RepositoryFactory.getMutantRepository().getGenotypeByID(genoZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);
        boolean isStandardEnvironment = true;
        
        if (geno == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(geno, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, geno.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }

    @RequestMapping("/genotype-figure-summary-chemical")
    protected String getGenotypeExprssionFigureSummary(@RequestParam String genoZdbID,
                                                       @RequestParam String geneZdbID,
                                                       @RequestParam boolean imagesOnly,
                                                       Model model) {
        Genotype geno = RepositoryFactory.getMutantRepository().getGenotypeByID(genoZdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);
        boolean isChemicalEnvironment = true;

        if (geno == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        if (gene == null) {
                    model.addAttribute(LookupStrings.ZDB_ID, geneZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaChemicalEnvironment(geno, gene, imagesOnly);
        model.addAttribute("expressionCriteria", expressionCriteria);
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createExpressionFigureSummary(expressionCriteria);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, geno.getName() + " Expression Figure Summary");
        return "expression/genotype-figure-summary.page";

    }


}
