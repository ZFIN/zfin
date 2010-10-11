package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class GenotypeDetailController extends AbstractCommandController {
    private static final Logger LOG = Logger.getLogger(GenotypeDetailController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    public GenotypeDetailController() {
        setCommandClass(GenotypeBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Genotype Detail Controller");
        GenotypeBean form = (GenotypeBean) command;
        Genotype genotype = mutantRepository.getGenotypeByID(form.getGenotype().getZdbID());
        if (genotype == null){
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(form.getGenotype().getZdbID());
            if(replacedZdbID!=null){
                logger.debug("found a replaced zdbID for: " + form.getGenotype().getZdbID() + "->" + replacedZdbID);
                form.getGenotype().setZdbID(replacedZdbID);
                genotype = mutantRepository.getGenotypeByID(form.getGenotype().getZdbID());
            }
        }
        if (genotype == null){
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getGenotype().getZdbID());
        }

        form.setGenotype(genotype);

        retrieveGenotypeAndFeatureData(form,genotype);

        retrieveExpressionData(form, genotype);

        retrievePhenotypeData(form, genotype);

        retrievePublicationData(form, genotype);

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("genotype-detail.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, genotype.getName());

        return modelAndView;
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
}