package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


//@RequestMapping(value ="/genotype")
@Controller
public class GenotypeDetailController {
    private static final Logger LOG = Logger.getLogger(GenotypeDetailController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    @RequestMapping(value = {
//            "/detail/{zdbID}", // TODO: go to a newer style once context is switched?
            "/genotype-detail" // this is the current style, have not changed
    }
    )
    public String getGenotypeDetail(@RequestParam String zdbID, Model model) {

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
        LOG.debug("Start All Phenotype Controller");
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
}