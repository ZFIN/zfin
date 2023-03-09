package org.zfin.feature.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Controller
@RequestMapping(value = "/genotype")
public class GenotypeDetailController {

    private static final Logger LOG = LogManager.getLogger(GenotypeDetailController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    @RequestMapping(value = {"/genotype-detail-popup"})
    public String getGenotypePopup(@RequestParam String zdbID, Model model) {

        LOG.debug("Start Genotype Detail Controller");
        GenotypeBean form = new GenotypeBean();

        if (zdbID.contains(",")) {
            Fish fish = RepositoryFactory.getMutantRepository().getFish(zdbID);
            form.setFishName(fish.getName());
            Genotype geno = fish.getGenotype();
            Genotype genotype = mutantRepository.getGenotypeByID(geno.getZdbID());
            form.setGenotype(genotype);
            retrieveGenotypeFeatureData(form, genotype);
            retrieveSequenceTargetingReagentData(form, fish);
            model.addAttribute(LookupStrings.FORM_BEAN, form);
            String genotypeName = genotype.getName();
            genotypeName = genotypeName.replaceAll("<sup>", "^");

            model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotypeName);

            return "feature/genotype-detail-popup";
        } else {
            Genotype genotype = mutantRepository.getGenotypeByID(zdbID);

            if (genotype == null) {
                String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
                if (replacedZdbID != null) {
                    LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                    genotype = mutantRepository.getGenotypeByID(replacedZdbID);
                } else {
                    String newZdbID = RepositoryFactory.getInfrastructureRepository().getNewZdbID(zdbID);
                    if (newZdbID != null) {
                        LOG.debug("found a replaced zdbID for: " + zdbID + "->" + newZdbID);
                        return "redirect:/ZDB-PUB-121121-2";
                    }
                }
            }
            if (genotype == null) {
                model.addAttribute(LookupStrings.ZDB_ID, zdbID);
                return LookupStrings.RECORD_NOT_FOUND_POPUP;
            }

            form.setGenotype(genotype);
            if (!genotype.isWildtype()) {
                retrieveGenotypeFeatureData(form, genotype);

            }
            model.addAttribute("affectedMarkerList", GenotypeService.getAffectedMarker(genotype));
            model.addAttribute(LookupStrings.FORM_BEAN, form);
            String genotypeName = genotype.getName();
            genotypeName = genotypeName.replaceAll("<sup>", "^");
            genotypeName = genotypeName.replaceAll("</sup>", "");
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotypeName);

            return "feature/genotype-detail-popup";
        }
    }

    @RequestMapping(value = "/view/{zdbID}")
    public String getGenotypePrototypeDetail(@PathVariable String zdbID, Model model) {

        LOG.debug("Start Genotype Detail Controller");
        Genotype genotype = mutantRepository.getGenotypeByID(zdbID);
        if (genotype == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                genotype = mutantRepository.getGenotypeByID(replacedZdbID);
            } else {
                String newZdbID = RepositoryFactory.getInfrastructureRepository().getNewZdbID(zdbID);
                if (newZdbID != null) {
                    LOG.debug("found a replaced zdbID for: " + zdbID + "->" + newZdbID);
                    return "redirect:/ZDB-PUB-121121-2";
                }
            }
        }
        if (genotype == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);
        retrievePublicationData(form, genotype);
        if (!genotype.isWildtype()) {
            retrieveGenotypeFeatureData(form, genotype);
            model.addAttribute("affectedMarkerList", GenotypeService.getAffectedMarker(genotype));
        }

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        String genotypeName = genotype.getName();
        genotypeName = genotypeName.replaceAll("<sup>", "^");
        genotypeName = genotypeName.replaceAll("</sup>", "");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Genotype: " + genotypeName);
        if (genotype.isWildtype())
            return "fish/wildtype-genotype-view";
        return "fish/genotype-view";
    }

    private void retrieveGenotypeFeatureData(GenotypeBean form, Genotype genotype) {
        List<GenotypeFeature> genotypeFeatures = mutantRepository.getGenotypeFeaturesByGenotype(genotype);
        form.setGenotypeFeatures(genotypeFeatures);

        /// TODO still needs to be handled: either go to fish controller or use fish here...
/*
        FishStatistics genoStat = new FishStatistics(genotype);
        form.setFishStatistics(genoStat);
*/
    }

    private void retrieveSequenceTargetingReagentData(GenotypeBean form, Fish fish) {
        if (fish.getStrList() == null || fish.getStrList().size() == 0) {
            return;
        }
        form.setSequenceTargetingReagents(getSequenceTargetingReagent(fish));
    }

    private List<SequenceTargetingReagent> getSequenceTargetingReagent(Fish fish) {
        return fish.getStrList();
    }

    public void retrievePhenotypeData(GenotypeBean form, Genotype genotype) {
        List<PhenotypeStatementWarehouse> phenoStatements = mutantRepository.getPhenotypeStatementsByGenotype(genotype);
        form.setPhenoStatements(phenoStatements);
    }

    private void retrievePublicationData(GenotypeBean form, Genotype genotype) {
        form.setTotalNumberOfPublications(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForGenotype(genotype, 0).getTotalCount());
        form.setPreviousNames(RepositoryFactory.getFeatureRepository().getPreviousNamesLight(genotype));
    }

    @RequestMapping(value = {
        "/show_all_phenotype" // this is the current style, have not changed
    }
    )
    public String getAllPhenotypesForGenotype(@RequestParam String zdbID, Model model) throws Exception {
        LOG.debug("Start Genotype Controller");
        Genotype genotype = mutantRepository.getGenotypeByID(zdbID);
        if (genotype == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);

        retrievePhenotypeData(form, genotype);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Phenotypes with " + genotype.getName());

        return "feature/genotype-all-phenotype";
    }

    @RequestMapping(value = {"genotype-phenotype-figure-summary"})
    public String getPhenotypeSummaryForGenotype(@RequestParam(value = "genoZdbID", required = true) String genoZdbID, Model model) throws Exception {
        LOG.debug("Start Genotype Controller");
        Genotype genotype = mutantRepository.getGenotypeByID(genoZdbID);
        if (genotype == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoZdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);

        List<FigureSummaryDisplay> figureSummaryDisplayList = PhenotypeService.getPhenotypeFigureSummaryForGenotype(genotype);

        Collections.sort(figureSummaryDisplayList);
        model.addAttribute("figureSummaryDisplay", figureSummaryDisplayList);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Phenotypes with " + genotype.getName());

        return "feature/genotype-phenotype-figure-summary";
    }

}