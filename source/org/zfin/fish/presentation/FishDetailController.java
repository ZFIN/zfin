package org.zfin.fish.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.feature.presentation.GenotypeDetailController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Morpholino;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Fish: " + fishName);

        return "fish/fish-detail.page";
    }

    @RequestMapping(value = "/fish-show-all-phenotypes/{ID}")
    protected String showAllPhenotypes(Model model,
                                       @PathVariable("ID") String ID) throws Exception {
        LOG.info("Start Fish Detail Controller");

        Fish fish = RepositoryFactory.getFishRepository().getFish(ID);
        if (fish == null)
            return LookupStrings.idNotFound(model, ID);

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

    private void retrieveMorpholinoData(FishBean form, Fish fish) {
        if (fish.getMorpholinos() == null || fish.getMorpholinos().size() == 0)
            return;
        Set<String> moIds = new HashSet<String>(fish.getMorpholinos().size());
        for (ZfinEntity morpholino : fish.getMorpholinos())
            moIds.add(morpholino.getID());
        List<Morpholino> morpholinos = new ArrayList<Morpholino>(2);
        for (String moID : moIds)
            morpholinos.add(getMutantRepository().getMorpholinosById(moID));
        form.setMorpholinos(morpholinos);
    }

    public void retrievePhenotypeData(FishBean form, List<String> genoxIds) {
        List<PhenotypeStatement> phenoStatements = getMutantRepository().getPhenotypeStatementsByGenotypeExperiments(genoxIds);
        form.setPhenoStatements(phenoStatements);
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

}




