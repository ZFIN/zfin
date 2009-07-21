package org.zfin.anatomy.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.mutant.repository.MutantRepository;
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

        AnatomyItem term = retrieveAnatomyTerm(form.getAnatomyItem());
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
        form.setAnatomyItem(term);

        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        AnatomyItem anatomyItem = form.getAnatomyItem();
        List<GenotypeExperiment> morphResult =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(anatomyItem, form.isWildtype());
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

    public static AnatomyItem retrieveAnatomyTerm(AnatomyItem anatomyItem) {
        AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
        return anatomyRepository.getAnatomyTermByID(anatomyItem.getZdbID());
    }
}
