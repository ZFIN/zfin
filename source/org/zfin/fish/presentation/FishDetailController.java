package org.zfin.fish.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.GeneCentricExpressionData;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.feature.presentation.GenotypeDetailController;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.MutationType;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.*;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinStringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Controller that serves the fish detail page.
 */
@Controller
@RequestMapping(value = "/fish")
public class FishDetailController {

    private static final Logger LOG = Logger.getLogger(FishDetailController.class);

    @Autowired
    GenotypeDetailController genotypeDetailController;

    @RequestMapping(value = "/fish-detail/{ID}")
    protected String showFishDetail(Model model,
                                    @PathVariable("ID") String fishID, HttpServletResponse response) {
        LOG.info("Start MartFish Detail Controller");


        if (fishID.startsWith("ZDB-FISH")) {
            return showCuratedFish(fishID, model, response);
        }

        fishID = ZfinStringUtils.cleanUpConcatenatedZDBIdsDelimitedByComma(fishID);

        MartFish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)    {
            String newZdbID = RepositoryFactory.getInfrastructureRepository().getNewZdbID(fishID);
            if (newZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + fishID + "->" + newZdbID);

                return "redirect:/ZDB-PUB-121121-2";
            }
            else{
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return LookupStrings.idNotFound(model, fishID);
            }
        }
        if (fish.getGenotype() != null && fish.getStrList().size() == 0) {
            return genotypeDetailController.getGenotypeDetail(fish.getGenotypeID(), model);
            //return genotypeDetailController.getGenotypePopup(fish.getGenotypeID(), model);
        }

        model.addAttribute("fish", fish);
        FishBean form = new FishBean();
        retrieveGenotypeExperiment(form, fish);
        retrievePhenotypeData(form, fish.getGenotypeExperimentIDs());
        model.addAttribute("totalNumberOfPublications", FishService.getCitationCount(fish));
        model.addAttribute(LookupStrings.FORM_BEAN, form);

        addExpressionSummaryToForm(model, fishID);

        // the following put the fish Id to page title as debugging for FB case 8817
        // model.addAttribute(LookupStrings.DYNAMIC_TITLE, "MartFish: " + fishID);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + getTitle(fish.getName()));

        return "fish/fish-detail.page";
    }

    protected String showCuratedFish(String fishZdbId, Model model, HttpServletResponse response) {

        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishZdbId);

        if (fish == null) {
            String newZdbID = RepositoryFactory.getInfrastructureRepository().getNewZdbID(fishZdbId);
            if (newZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + fishZdbId + "->" + newZdbID);
                return "redirect:/" + newZdbID;
            }
            else{
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return LookupStrings.idNotFound(model, fishZdbId);
            }
        }

        model.addAttribute("fish", fish);
        FishBean form = new FishBean();

        model.addAttribute("formBean", form);
        model.addAttribute("totalNumberOfPublications", FishService.getCitationCount(fish));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + getTitle(fish.getName()));

        return "fish/fish-detail.page";
    }

    private String getTitle(String fishName) {
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        return fishName;
    }

    @RequestMapping(value = "/fish-detail-popup/{ID}")
    protected String showFishDetailPopup(Model model, @PathVariable("ID") String fishId) {
        MartFish fish = RepositoryFactory.getFishRepository().getFish(
                ZfinStringUtils.cleanUpConcatenatedZDBIdsDelimitedByComma(fishId)
        );
        FishBean form = new FishBean();
        model.addAttribute("fish", fish);
        retrieveGenotypeExperiment(form, fish);
        List<FeatureGene> genomicFeatures = new ArrayList<>();
        // remove any featureGenes that have an STR mutation type and use the resulting list
        // to populate the form's genomicFeatures field
        CollectionUtils.select(fish.getFeatureGenes(), new Predicate() {
            @Override
            public boolean evaluate(Object featureGene) {
                MutationType m = ((FeatureGene) featureGene).getMutationTypeDisplay();
                return m != MutationType.MORPHOLINO && m != MutationType.CRISPR && m != MutationType.TALEN;
            }
        }, genomicFeatures);
        form.setGenomicFeatures(genomicFeatures);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "fish/fish-detail-popup.popup";
    }

    private void addExpressionSummaryToForm(Model model, String fishID) {
        List<FigureExpressionSummary> figureExpressionSummaryList = FishService.getExpressionSummary(fishID);
        if (figureExpressionSummaryList != null) {
            List<GeneCentricExpressionData> geneCentricExpressionData = PresentationConverter.getGeneCentricExpressionData(figureExpressionSummaryList);
            Collections.sort(geneCentricExpressionData, new MarkerCentricOrdering());
            model.addAttribute("geneCentricExpressionDataList", geneCentricExpressionData);
            model.addAttribute("expressionFigureCount", figureExpressionSummaryList.size());
        }
    }

    @RequestMapping(value = "/fish-show-all-phenotypes/{ID}")
    protected String showAllPhenotypes(Model model,
                                       @PathVariable("ID") String fishID) throws Exception {
        LOG.info("Start MartFish Detail Controller");

        MartFish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getStrList().size() == 0) {
            String genotypeExperimentIDsString = fish.getGenotypeExperimentIDs().get(0);
            FishExperiment fishExperiment = getMutantRepository().getGenotypeExperiment(genotypeExperimentIDsString);
            return genotypeDetailController.getAllPhenotypesForGenotype(fishExperiment.getFish().getGenotype().getZdbID(), model);
        }

        FishBean form = new FishBean();
        model.addAttribute("fish", fish);
        retrievePhenotypeData(form, fish.getGenotypeExperimentIDs());
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(fish.getName()));

        return "fish/fish-all-phenotype.page";
    }

    @RequestMapping(value = "/fish-show-all-expression/{ID}")
    protected String showAllExpression(Model model,
                                       @PathVariable("ID") String fishID) throws Exception {
        LOG.info("Start MartFish Detail Controller");
        MartFish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getStrList().size() == 0) {
            String genotypeExperimentIDsString = fish.getGenotypeExperimentIDs().get(0);
            FishExperiment fishExperiment = getMutantRepository().getGenotypeExperiment(genotypeExperimentIDsString);
            return genotypeDetailController.getAllExpressionsPerGenotype(fishExperiment.getFish().getGenotype().getZdbID(), model);
        }

        GenotypeBean form = new GenotypeBean();
        retrieveExpressionData(form, fish.getGenotypeExperimentIDs());
        if (StringUtils.isNotEmpty(fish.getGenotypeID()))
            form.setGenotype(getMutantRepository().getGenotypeByID(fish.getGenotypeID()));
        addExpressionSummaryToForm(model, fishID);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(fish);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(fish.getName()));

        return "genotype/fish-all-expressions.page";
    }

    public void retrievePhenotypeData(FishBean form, List<String> genoxIds) {
        List<PhenotypeStatement> phenoStatements = getMutantRepository().getPhenotypeStatementsByGenotypeExperiments(genoxIds);
        form.setPhenoStatements(phenoStatements);
    }

    public void retrieveExpressionData(GenotypeBean form, List<String> genoxIds) {
        List<ExpressionStatement> phenoStatements = getMutantRepository().getExpressionStatementsByGenotypeExperiments(genoxIds);
        form.setExpressionStatements(phenoStatements);
    }


    private void retrieveGenotypeExperiment(FishBean form, MartFish fish) {
        if (fish.getGenotypeExperimentIDs() == null) {
            return;
        }
        List<FishExperiment> fishExperiments = new ArrayList<>(fish.getGenotypeExperimentIDs().size());
        for (String genoID : fish.getGenotypeExperimentIDs()) {
            fishExperiments.add(getMutantRepository().getGenotypeExperiment(genoID));
        }
        form.setGenotypeExperimentsList(fishExperiments);
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

