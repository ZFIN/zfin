package org.zfin.anatomy.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This Controller serves the all morpholino experiment page.
 */
public class AllMorpholinoExperimentController extends AbstractCommandController {

    public AllMorpholinoExperimentController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        AnatomySearchBean form = (AnatomySearchBean) command;

        if (form.getAoTerm() == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, "");
        Term term = OntologyManager.getInstance().getTermByID(form.getAoTerm().getID());
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, form.getAoTerm().getID());
        form.setAoTerm(term);

        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        Term anatomyItem = OntologyManager.getInstance().getTermByID(form.getAoTerm().getID());
        List<GenotypeExperiment> morphResult =
                mutantRepository.getGenotypeExperimentMorpholinos(anatomyItem, form.isWildtype());
        form.setWildtypeMorpholinoCount(morphResult.size());
        List<MorpholinoStatistics> morpholinoStats = AnatomyAjaxController.createMorpholinoStats(morphResult, anatomyItem);
        Collections.sort(morpholinoStats, new Comparator<MorpholinoStatistics>() {
            public int compare(MorpholinoStatistics one, MorpholinoStatistics two) {
                return (one.getTargetGeneOrder().compareTo(two.getTargetGeneOrder()));
            }
        });
        form.setAllMorpholinos(morpholinoStats);
        return new ModelAndView("all-morpholino-experiments.page", LookupStrings.FORM_BEAN, form);
    }

}
