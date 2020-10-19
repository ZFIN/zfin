package org.zfin.fish.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.expression.presentation.GeneCentricExpressionData;
import org.zfin.expression.presentation.ProteinExpressionDisplay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.service.ExpressionService;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.MutationType;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * Controller that serves the fish detail page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishDetailController {

    private static final Logger LOG = LogManager.getLogger(FishDetailController.class);

    @RequestMapping(value = "/fish-detail/{zdbID}")
    protected String showCuratedFish(@PathVariable String zdbID, Model model) {

        Fish fish = RepositoryFactory.getMutantRepository().getFish(zdbID);

        if (fish == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);

                Fish replacedFish = RepositoryFactory.getMutantRepository().getFish(zdbID);

                if (replacedFish != null) {
                    fish = replacedFish;
                } else {
                    return "redirect:/" + replacedZdbID;
                }
            }
        }

        if (fish == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (fish.isWildtypeWithoutReagents()) {
            return "redirect:/" + fish.getGenotype().getZdbID();
        }

        model.addAttribute("fish", fish);


        // phenotype
        List<PhenotypeStatementWarehouse> phenotypeStatements = getMutantRepository().getPhenotypeStatementWarehousesByFish(fish);
        model.addAttribute("phenotypeStatements", phenotypeStatements);
        model.addAttribute("phenotypeDisplays", PhenotypeService.getPhenotypeDisplays(phenotypeStatements, "condition", "phenotypeStatement"));

        // disease model
        List<DiseaseAnnotationModel> diseaseAnnotations = getPhenotypeRepository().getHumanDiseaseModelsByFish(zdbID);
        model.addAttribute("diseases", OntologyService.getDiseaseModelDisplay(diseaseAnnotations));

        // Expression data
        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
        List<ExpressionResult> fishNonEfgExpressionResults = expressionRepository.getNonEfgExpressionResultsByFish(fish);
        List<ExpressionResult> fishEfgExpressionResults = expressionRepository.getEfgExpressionResultsByFish(fish);
        List<ExpressionResult> fishProteinExpressionResults = expressionRepository.getProteinExpressionResultsByFish(fish);
        List<String> fishExpressionFigureIDs = expressionRepository.getExpressionFigureIDsByFish(fish);
        List<String> fishExpressionPublicationIDs = expressionRepository.getExpressionPublicationIDsByFish(fish);
        List<ExpressionDisplay> fishNonEfgExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishNonEfgExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);
        model.addAttribute("geneCentricNonEfgExpressionDataList", fishNonEfgExpressionDisplays);
        List<ExpressionDisplay> fishEfgExpressionDisplays = ExpressionService.createExpressionDisplays(fish.getZdbID(), fishEfgExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);
        model.addAttribute("geneCentricEfgExpressionDataList", fishEfgExpressionDisplays);
        List<ProteinExpressionDisplay> fishProteinExpressionDisplays = ExpressionService.createProteinExpressionDisplays(fish.getZdbID(), fishProteinExpressionResults, fishExpressionFigureIDs, fishExpressionPublicationIDs, true);
        model.addAttribute("proteinExpressionDataList", fishProteinExpressionDisplays);

        model.addAttribute("totalNumberOfPublications", FishService.getCitationCount(fish));
        model.addAttribute("fishIsWildtypeWithoutReagents", fish.isWildtypeWithoutReagents());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + getTitle(fish.getName()));

        return "fish/fish-detail";
    }

    private String getTitle(String fishName) {
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        return fishName;
    }


 @RequestMapping(value = "/fish-detail-popup/{ID}")

    protected String showFishDetailPopup(Model model, @PathVariable("ID") String fishZdbId) {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishZdbId);

        List<FeatureGene> genomicFeatures = new ArrayList<>();
        // remove any featureGenes that have an STR mutation type and use the resulting list
        // to populate the form's genomicFeatures field
        CollectionUtils.select(FishService.getFeatureGenes(fish, false), new Predicate() {
            @Override
            public boolean evaluate(Object featureGene) {
                MutationType m = ((FeatureGene) featureGene).getMutationTypeDisplay();
                return m != MutationType.MORPHOLINO && m != MutationType.CRISPR && m != MutationType.TALEN;
            }
        }, genomicFeatures);

        model.addAttribute("fish", fish);
        model.addAttribute("fishGenomicFeatures", genomicFeatures);
        return "fish/fish-detail-popup.popup";
    }





    private class MarkerCentricOrdering implements Comparator<GeneCentricExpressionData> {

        @Override
        public int compare(GeneCentricExpressionData leftsummary, GeneCentricExpressionData rightSummary) {
            ExpressedGene geneLeft = leftsummary.getExpressedGene();
            ExpressedGene geneRight = rightSummary.getExpressedGene();
            return geneLeft.getGene().compareTo(geneRight.getGene());
        }
    }
}

