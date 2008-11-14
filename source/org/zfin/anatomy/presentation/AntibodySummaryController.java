package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class that serves the anatomy term detail page.
 */
public class AntibodySummaryController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

    private AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
    private AnatomyRepository anatomyRepository;

    public AntibodySummaryController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);

        retrieveAntibodyData(term, form);

        ModelAndView modelAndView = new ModelAndView("antibody-summary.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getName());

        return modelAndView;
    }

    private AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai = anatomyRepository.loadAnatomyItem(form.getAnatomyItem());
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        return ai;
    }

    private void retrieveAntibodyData(AnatomyItem aoTerm, AnatomySearchBean form) {

        int antibodyCount = antibodyRepository.getAntibodiesByAOTermCount(aoTerm);
        form.setAntibodyCount(antibodyCount);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(-1);
        List<Antibody> antibodies = antibodyRepository.getAntibodiesByAOTerm(aoTerm, pagination);
        List<AntibodyStatistics> abStats = createAntibodyStatistics(antibodies, aoTerm);
        form.setAntibodyStatistics(abStats);
    }

    private List<AntibodyStatistics> createAntibodyStatistics(List<Antibody> antibodies, AnatomyItem aoTerm) {
        if (antibodies == null)
            return null;

        List<AntibodyStatistics> stats = new ArrayList<AntibodyStatistics>();
        for (Antibody antibody : antibodies) {
            AntibodyStatistics stat = new AntibodyStatistics(antibody, aoTerm);
            stats.add(stat);
        }
        return stats;
    }

    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

}