package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class AllPhenotypeController extends AbstractCommandController {
    private static final Logger LOG = Logger.getLogger(AllPhenotypeController.class);
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    public AllPhenotypeController() {
        setCommandClass(GenotypeBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start All Phenotype Controller");
        GenotypeBean form = (GenotypeBean) command;
        Genotype genotype = mutantRepository.getGenotypeByID(form.getGenotype().getZdbID());
        if (genotype == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getGenotype().getZdbID());

        form.setGenotype(genotype);

        List<GenotypeFigure> genoFigs = mutantRepository.getCleanGenoFigsByGenotype(genotype);

        form.setGenotypeFigures(genoFigs);

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("genotype_all_phenotype.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, genotype.getName());

        return modelAndView;
    }

}