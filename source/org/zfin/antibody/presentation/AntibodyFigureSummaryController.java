package org.zfin.antibody.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class that serves the antibody details page.
 */
public class AntibodyFigureSummaryController extends AbstractCommandController {
    private static final Logger LOG = Logger.getLogger(AntibodyDetailController.class);

    public AntibodyFigureSummaryController() {
        setCommandClass(AntibodyBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Antibody Figure Summary Controller");
        AntibodyBean form = (AntibodyBean) command;

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();

        Antibody ab = antibodyRepository.getAntibodyByID(form.getAntibody().getZdbID());
        if (ab == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAntibody().getZdbID());

        AnatomyRepository anatomyItemRepository = RepositoryFactory.getAnatomyRepository();

        AnatomyItem anatomy = anatomyItemRepository.getAnatomyTermByID(form.getAnatomyItem().getZdbID());
        form.setAnatomyItem(anatomy);

        DevelopmentStage startStage = anatomyItemRepository.getStage(form.getStartStage());
        form.setStartStage(startStage);

        DevelopmentStage endStage = anatomyItemRepository.getStage(form.getEndStage());
        form.setEndStage(endStage);        

        AntibodyService abStat = new AntibodyService(ab);
        abStat.createFigureSummary(form.getAnatomyItem(), form.getStartStage(), form.getEndStage(), form.isOnlyFiguesWithImg());
        form.setAntibodyStat(abStat);
        form.setAntibody(ab);

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("antibody-figure-summary.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, ab.getName());

        return modelAndView;
    }
}