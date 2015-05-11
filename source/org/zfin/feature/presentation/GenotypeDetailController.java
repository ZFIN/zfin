package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.fish.presentation.Fish;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;


@Controller
@RequestMapping(value = "/genotype")
public class GenotypeDetailController {
    private static final Logger LOG = Logger.getLogger(GenotypeDetailController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    @RequestMapping(value = {"/genotype-detail-popup"})
    public String getGenotypePopup(@RequestParam String zdbID, Model model) {

        LOG.debug("Start Genotype Detail Controller");
        GenotypeBean form = new GenotypeBean();

        if (zdbID.contains(",")) {
            Fish fish = getFishRepository().getFish(zdbID);
            form.setFishName(fish.getName());
            Genotype geno = fish.getGenotype();
            Genotype genotype = mutantRepository.getGenotypeByID(geno.getZdbID());
            form.setGenotype(genotype);
            retrieveGenotypeAndFeatureData(form, genotype);
            retrieveSequenceTargetingReagentData(form, fish);
            model.addAttribute(LookupStrings.FORM_BEAN, form);
            String genotypeName = genotype.getName();
            genotypeName = genotypeName.replaceAll("<sup>", "^");

            model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotypeName);

            return "genotype/genotype-detail-popup.popup";
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
                return "record-not-found.popup";
            }

            form.setGenotype(genotype);
            if (!genotype.isWildtype()) {
                retrieveGenotypeAndFeatureData(form, genotype);

            }

            model.addAttribute(LookupStrings.FORM_BEAN, form);
            String genotypeName = genotype.getName();
            genotypeName = genotypeName.replaceAll("<sup>", "^");
            genotypeName = genotypeName.replaceAll("</sup>", "");
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotypeName);

            return "genotype/genotype-detail-popup.popup";
        }
    }

    @RequestMapping(value = "/view/{zdbID}")
    public String getGenotypeDetail(@PathVariable String zdbID, Model model) {

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
        if (!genotype.isWildtype()) {
            retrieveGenotypeAndFeatureData(form, genotype);
            retrieveExpressionData(form, genotype);
            retrievePhenotypeData(form, genotype);
            retrievePublicationData(form, genotype);
        }

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        String genotypeName = genotype.getName();
        genotypeName = genotypeName.replaceAll("<sup>", "^");
        genotypeName = genotypeName.replaceAll("</sup>", "");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotypeName);

        return "genotype/genotype-detail.page";
    }


    @RequestMapping(value = {"/show_all_expression"})
    public String getAllExpressionsPerGenotype(@RequestParam String genoID, Model model) {
        LOG.debug("Start All Expressions for Genotype");
        Genotype genotype = mutantRepository.getGenotypeByID(genoID);
        if (genotype == null) {
            model.addAttribute(LookupStrings.ZDB_ID, genoID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);

        retrieveExpressionData(form, genotype);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotype.getName());
        return "genotype/genotype_all_expression.page";
    }

    private void retrieveGenotypeAndFeatureData(GenotypeBean form, Genotype genotype) {
        List<GenotypeFeature> genotypeFeatures = mutantRepository.getGenotypeFeaturesByGenotype(genotype);
        form.setGenotypeFeatures(genotypeFeatures);

        GenotypeStatistics genoStat = new GenotypeStatistics(genotype);
        form.setGenotypeStatistics(genoStat);
    }

    private void retrieveSequenceTargetingReagentData(GenotypeBean form, Fish fish) {
        if (fish.getSequenceTargetingReagents() == null || fish.getSequenceTargetingReagents().size() == 0)
            return;
        form.setSequenceTargetingReagents(getSequenceTargetingReagent(fish));
    }

    private List<SequenceTargetingReagent> getSequenceTargetingReagent(Fish fish) {
        return fish.getSequenceTargetingReagents();
    }

    private void retrieveExpressionData(GenotypeBean form, Genotype genotype) {
        List<ExpressionResult> expressionResults = expressionRepository.getExpressionResultsByGenotype(genotype);
        form.setExpressionResults(expressionResults);
    }

    public void retrievePhenotypeData(GenotypeBean form, Genotype genotype) {
        List<PhenotypeStatement> phenoStatements = mutantRepository.getPhenotypeStatementsByGenotype(genotype);

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

        return "genotype/genotype-all-phenotype.page";
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

        return "genotype/genotype-phenotype-figure-summary.page";
    }
}