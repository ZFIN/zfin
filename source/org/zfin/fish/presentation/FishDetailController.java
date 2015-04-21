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
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.PhenotypeStatement;
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
        LOG.info("Start Fish Detail Controller");

        fishID = ZfinStringUtils.cleanUpConcatenatedZDBIdsDelimitedByComma(fishID);

        Fish fish = RepositoryFactory.getFishRepository().getFish(fishID);
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
        if (fish.getGenotype() != null && fish.getSequenceTargetingReagents().size() == 0) {
            return genotypeDetailController.getGenotypeDetail(fish.getGenotypeID(), model);
            //return genotypeDetailController.getGenotypePopup(fish.getGenotypeID(), model);
        }


        FishBean form = new FishBean();
        form.setFish(fish);
        retrieveGenotypeExperiment(form, fish);
        retrieveGenotypes(form, fish);
        retrievePhenotypeData(form, fish.getGenotypeExperimentIDs());
        retrieveSTRData(form, fish);
        retrievePublicationData(form, fish);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        String fishName = fish.getName();
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");

        addExpressionSummaryToForm(model, fishID);

        // the following put the fish Id to page title as debugging for FB case 8817
        // model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + fishID);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + fishName);

        return "fish/fish-detail.page";
    }

    @RequestMapping(value = "/fish-detail-popup/{ID}")
    protected String showFishDetailPopup(Model model, @PathVariable("ID") String fishId) {
        Fish fish = RepositoryFactory.getFishRepository().getFish(
                ZfinStringUtils.cleanUpConcatenatedZDBIdsDelimitedByComma(fishId)
        );
        FishBean form = new FishBean();
        form.setFish(fish);
        retrieveGenotypeExperiment(form, fish);
        retrieveGenotypes(form, fish);
        retrieveSTRData(form, fish);
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
            model.addAttribute(geneCentricExpressionData);
            model.addAttribute("expressionFigureCount", figureExpressionSummaryList.size());
        }
    }

    @RequestMapping(value = "/fish-show-all-phenotypes/{ID}")
    protected String showAllPhenotypes(Model model,
                                       @PathVariable("ID") String fishID) throws Exception {
        LOG.info("Start Fish Detail Controller");

        Fish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getSequenceTargetingReagents().size() == 0) {
            String genotypeExperimentIDsString = fish.getGenotypeExperimentIDs().get(0);
            GenotypeExperiment genotypeExperiment = getMutantRepository().getGenotypeExperiment(genotypeExperimentIDsString);
            return genotypeDetailController.getAllPhenotypesForGenotype(genotypeExperiment.getGenotype().getZdbID(), model);
        }

        FishBean form = new FishBean();
        form.setFish(fish);
        retrievePhenotypeData(form, fish.getGenotypeExperimentIDs());
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        String fishName = fish.getName();
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fishName);

        return "fish/fish-all-phenotype.page";
    }

    @RequestMapping(value = "/fish-show-all-expression/{ID}")
    protected String showAllExpression(Model model,
                                       @PathVariable("ID") String fishID) throws Exception {
        LOG.info("Start Fish Detail Controller");
        Fish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getSequenceTargetingReagents().size() == 0) {
            String genotypeExperimentIDsString = fish.getGenotypeExperimentIDs().get(0);
            GenotypeExperiment genotypeExperiment = getMutantRepository().getGenotypeExperiment(genotypeExperimentIDsString);
            return genotypeDetailController.getAllExpressionsPerGenotype(genotypeExperiment.getGenotype().getZdbID(), model);
        }

        GenotypeBean form = new GenotypeBean();
        retrieveExpressionData(form, fish.getGenotypeExperimentIDs());
        if (StringUtils.isNotEmpty(fish.getGenotypeID()))
            form.setGenotype(getMutantRepository().getGenotypeByID(fish.getGenotypeID()));
        addExpressionSummaryToForm(model, fishID);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute("sequenceTargetingReagents", getSequenceTargetingReagent(fish));
        model.addAttribute(fish);
        String fishName = fish.getName();
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fishName);

        return "genotype/fish-all-expressions.page";
    }

    private void retrieveSTRData(FishBean form, Fish fish) {
        if (fish.getSequenceTargetingReagents() == null || fish.getSequenceTargetingReagents().size() == 0)
            return;
        form.setSequenceTargetingReagents(getSequenceTargetingReagent(fish));
    }

    private List<SequenceTargetingReagent> getSequenceTargetingReagent(Fish fish) {
        if (fish.getSequenceTargetingReagents() == null || fish.getSequenceTargetingReagents().size() == 0)
            return null;
        Set<String> strIDs = new HashSet<String>(fish.getSequenceTargetingReagents().size());
        for (ZfinEntity str : fish.getSequenceTargetingReagents())
            strIDs.add(str.getID());
        List<SequenceTargetingReagent> sequenceTargetingReagents = new ArrayList<SequenceTargetingReagent>(2);
        for (String moID : strIDs)
            sequenceTargetingReagents.add(getMutantRepository().getSequenceTargetingReagentByID(moID));
        return sequenceTargetingReagents;
    }

    public void retrievePhenotypeData(FishBean form, List<String> genoxIds) {
        List<PhenotypeStatement> phenoStatements = getMutantRepository().getPhenotypeStatementsByGenotypeExperiments(genoxIds);
        form.setPhenoStatements(phenoStatements);
    }

    public void retrieveExpressionData(GenotypeBean form, List<String> genoxIds) {
        List<ExpressionStatement> phenoStatements = getMutantRepository().getExpressionStatementsByGenotypeExperiments(genoxIds);
        form.setExpressionStatements(phenoStatements);
    }


    private void retrieveGenotypeExperiment(FishBean form, Fish fish) {
        if (fish.getGenotypeExperimentIDs() == null) {
            return;
        }
        List<GenotypeExperiment> genotypeExperiments = new ArrayList<GenotypeExperiment>(fish.getGenotypeExperimentIDs().size());
        for (String genoID : fish.getGenotypeExperimentIDs()) {
            genotypeExperiments.add(getMutantRepository().getGenotypeExperiment(genoID));
        }
        form.setGenotypeExperimentsList(genotypeExperiments);
    }

    private void retrieveGenotypes(FishBean form, Fish fish) {
        List<Genotype> genotype = new ArrayList<Genotype>();
        if (fish.getGenotype() != null) {
            genotype.add(getMutantRepository().getGenotypeByID(fish.getGenotypeID()));
        }
        if (fish.getGenotypeExperimentIDs() != null) {
            for (String genoxID : fish.getGenotypeExperimentIDs()) {
                genotype.add(getMutantRepository().getGenotypeExperiment(genoxID).getGenotype());
            }
        }
        form.setGenotypes(genotype);
    }


    private void retrievePublicationData(FishBean form, Fish fish) {
        form.setTotalNumberOfPublications(RepositoryFactory.getMutantRepository().getGenoxAttributions(fish.getGenotypeExperimentIDs()).size());
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

