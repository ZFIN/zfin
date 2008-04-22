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
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
        form.setAnatomyItem(term);

        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        AnatomyItem anatomyItem = form.getAnatomyItem();
        int wildtypeMorphCount = mutantRepository.getNumberOfMorpholinoExperiments(anatomyItem, form.isWildtype());
        form.setWildtypeMorpholinoCount(wildtypeMorphCount);
        mutantRepository.setPaginationParameters(null);
        List<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(anatomyItem, form.isWildtype());
        List<MorpholinoStatistics> morpholinoStats = AnatomyTermDetailController.createMorpholinoStats(morphs, anatomyItem);
        form.setAllMorpholinos(morpholinoStats);
        return new ModelAndView("all-morpholino-experiments.page", LookupStrings.FORM_BEAN, form);
    }

    public static AnatomyItem retrieveAnatomyTerm(AnatomyItem anatomyItem) {
        AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
        return anatomyRepository.getAnatomyTermByID(anatomyItem.getZdbID());
    }
}
