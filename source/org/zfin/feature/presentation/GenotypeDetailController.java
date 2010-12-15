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
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


@Controller
// already specified in the context
//@RequestMapping(value ="/genotype")
public class GenotypeDetailController {
    private static final Logger LOG = Logger.getLogger(GenotypeDetailController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();


    @RequestMapping( value={
//            "/detail/{zdbID}", // TODO: go to a newer style once context is switched?
            "/genotype-detail" // this is the current style, have not changed
    }
    )
    protected String getGenotypeDetail(@RequestParam String zdbID, Model model) {
        LOG.debug("Start Genotype Detail Controller");
        Genotype genotype = mutantRepository.getGenotypeByID(zdbID);
        if (genotype == null){
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if(replacedZdbID!=null){
                LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                genotype = mutantRepository.getGenotypeByID(replacedZdbID);
            }
        }
        if (genotype == null){
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return "record-not-found.page";
        }
        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);
        retrieveGenotypeAndFeatureData(form,genotype);
        retrieveExpressionData(form, genotype);
        retrievePhenotypeData(form, genotype);
        retrievePublicationData(form, genotype);


        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, genotype.getName());

        return "genotype/genotype-detail.page" ;
    }

    private void retrieveGenotypeAndFeatureData(GenotypeBean form, Genotype genotype) {
		List<GenotypeFeature> genotypeFeatures = mutantRepository.getGenotypeFeaturesByGenotype(genotype);
		form.setGenotypeFeatures(genotypeFeatures);

		GenotypeStatistics genoStat = new GenotypeStatistics(genotype);
        form.setGenotypeStatistics(genoStat);
    }

    private void retrieveExpressionData(GenotypeBean form, Genotype genotype) {
		List<ExpressionResult> xpRslts = expressionRepository.getExpressionResultsByGenotype(genotype);

        form.setExpressionResults(xpRslts);
	}

    private void retrievePhenotypeData(GenotypeBean form, Genotype genotype) {
        List<GenotypeFigure> genoFigs = mutantRepository.getCleanGenoFigsByGenotype(genotype);

        form.setGenotypeFigures(genoFigs);

	}

    private void retrievePublicationData(GenotypeBean form, Genotype genotype) {
        form.setTotalNumberOfPublications(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForGenotype(genotype, 0).getTotalCount());
    }

    @RequestMapping( value={
            "/show_all_phenotype" // this is the current style, have not changed
    }
    )
    protected String getAllPhenotypesForGenotype(@RequestParam String zdbID, Model model) throws Exception {
        LOG.debug("Start All Phenotype Controller");
        Genotype genotype = mutantRepository.getGenotypeByID(zdbID);
        if (genotype == null){
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return "record-not-found.page";
        }

        GenotypeBean form = new GenotypeBean();
        form.setGenotype(genotype);

        List<GenotypeFigure> genoFigs = mutantRepository.getCleanGenoFigsByGenotype(genotype);

        form.setGenotypeFigures(genoFigs);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Phenotypes with "+genotype.getName());

        return "genotype/genotype-all-phenotype.page";
    }
}