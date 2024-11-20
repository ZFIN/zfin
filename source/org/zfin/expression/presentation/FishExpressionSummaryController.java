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



    //Not seeing any traffic to this page in our access logs other than some hits from bingbot
    //Should we remove this method?
    //Examples:
    // 40.77.167.13 - - [03/Jan/2024:10:48:00 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-110810-1&geneZdbID=ZDB-GENE-980526-437&imagesOnly=false HTTP/1.1" 200 5486 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //157.55.39.59 - - [07/Jan/2024:16:16:52 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-080227-4&geneZdbID=ZDB-GENE-980526-143&imagesOnly=false HTTP/1.1" 200 5580 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //157.55.39.56 - - [26/Jan/2024:23:23:11 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //207.46.13.168 - - [27/Jan/2024:09:09:42 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-100429-3&geneZdbID=ZDB-GENE-980526-406&imagesOnly=false HTTP/1.1" 200 5654 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //207.46.13.141 - - [27/Jan/2024:13:42:51 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //207.46.13.153 - - [28/Jan/2024:20:05:00 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //52.167.144.24 - - [30/Jan/2024:23:13:12 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //207.46.13.107 - - [31/Jan/2024:21:54:55 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-050221-3&geneZdbID=ZDB-GENE-980526-437&imagesOnly=false HTTP/1.1" 200 5789 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //40.77.167.62 - - [09/Feb/2024:03:43:19 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //52.167.144.195 - - [11/Feb/2024:10:12:37 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //52.167.144.216 - - [19/Feb/2024:01:33:22 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //52.167.144.140 - - [19/Feb/2024:23:09:52 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //40.77.167.70 - - [21/Feb/2024:05:54:52 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //40.77.167.1 - - [21/Feb/2024:08:53:30 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-111222-1&geneZdbID=ZDB-GENE-060312-41&imagesOnly=false HTTP/1.1" 200 5984 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
    //52.167.144.171 - - [22/Feb/2024:06:27:24 -0800] "GET /action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=ZDB-MRPHLNO-070126-7&geneZdbID=ZDB-GENE-990415-267&imagesOnly=false HTTP/1.1" 200 5550 "-" "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm) Chrome/116.0.1938.76 Safari/537.36"
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
