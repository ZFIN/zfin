package org.zfin.fish.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.GeneCentricExpressionData;
import org.zfin.feature.presentation.GenotypeBean;
import org.zfin.feature.presentation.GenotypeDetailController;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PresentationConverter;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Morpholino;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Controller that serves the fish detail page.
 */
@Controller
public class FishDetailController {

    private static final Logger LOG = Logger.getLogger(FishDetailController.class);

    @Autowired
    GenotypeDetailController genotypeDetailController;

    @RequestMapping(value = "/fish-detail/{ID}")
    protected String showFishDetail(Model model,
                                    @PathVariable("ID") String fishID) {
        LOG.info("Start Fish Detail Controller");

        Fish fish = RepositoryFactory.getFishRepository().getFish(fishID);
        if (fish == null)
            return LookupStrings.idNotFound(model, fishID);

        if (fish.getGenotype() != null && fish.getMorpholinos().size() == 0) {
            return genotypeDetailController.getGenotypeDetail(fish.getGenotypeID(), model);
        }

        FishBean form = new FishBean();
        form.setFish(fish);
        retrieveGenotypeExperiment(form, fish);
        retrieveGenotypes(form, fish);
        retrievePhenotypeData(form, fish.getGenotypeExperimentIDs());
        retrieveMorpholinoData(form, fish);
        retrievePublicationData(form, fish);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        String fishName = fish.getName();
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");

        addExpressionSummaryToForm(model, fishID);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + fishName);

        return "fish/fish-detail.page";
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

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getMorpholinos().size() == 0) {
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

        if (fish.getGenotypeExperimentIDs() != null && fish.getGenotypeExperimentIDs().size() == 1 && fish.getMorpholinos().size() == 0) {
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
        model.addAttribute("morpholinos", getMorpholinos(fish));
        model.addAttribute(fish);
        String fishName = fish.getName();
        fishName = fishName.replaceAll("<sup>", "^");
        fishName = fishName.replaceAll("</sup>", "");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, fishName);

        return "genotype/fish-all-expressions.page";
    }

    private void retrieveMorpholinoData(FishBean form, Fish fish) {
        if (fish.getMorpholinos() == null || fish.getMorpholinos().size() == 0)
            return;
        form.setMorpholinos(getMorpholinos(fish));
    }

    private List<Morpholino> getMorpholinos(Fish fish) {
        if (fish.getMorpholinos() == null || fish.getMorpholinos().size() == 0)
            return null;
        Set<String> moIds = new HashSet<String>(fish.getMorpholinos().size());
        for (ZfinEntity morpholino : fish.getMorpholinos())
            moIds.add(morpholino.getID());
        List<Morpholino> morpholinos = new ArrayList<Morpholino>(2);
        for (String moID : moIds)
            morpholinos.add(getMutantRepository().getMorpholinosById(moID));
        return morpholinos;
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
        List<GenotypeExperiment> genotypeExperiments = new ArrayList<GenotypeExperiment>(fish.getGenotypeExperimentIDs().size());
        for (String genoID : fish.getGenotypeExperimentIDs()) {
            genotypeExperiments.add(getMutantRepository().getGenotypeExperiment(genoID));
        }
        form.setGenotypeExperimentsList(genotypeExperiments);
    }

    private void retrieveGenotypes(FishBean form, Fish fish) {
        List<Genotype> genotype = new ArrayList<Genotype>();
        for (String genoxID : fish.getGenotypeExperimentIDs()) {
            genotype.add(getMutantRepository().getGenotypeExperiment(genoxID).getGenotype());
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




