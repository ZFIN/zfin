package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 * Controller class that serves a figure summary page for a given antibody and labeling structure,
 * i.e. for a given antibody, superterm:subterm, start and end stage it lists all figures and publications.
 * Optionally it serves figures with images only or all.
 */
public class AntibodyFigureSummaryController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AntibodyFigureSummaryController.class);

    public AntibodyFigureSummaryController() {
        setCommandClass(AntibodyBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Antibody Figure Summary Controller");
        AntibodyBean form = (AntibodyBean) command;

        Antibody ab = getAntibodyRepository().getAntibodyByID(form.getAntibody().getZdbID());
        if (ab == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAntibody().getZdbID());

        GenericTerm superterm = RepositoryFactory.getOntologyRepository().getTermByZdbID(form.getSuperTerm().getZdbID());
        form.setSuperTerm(superterm);

        GenericTerm subterm = null;
        if (StringUtils.isNotEmpty(form.getSubTerm().getZdbID())) {
            subterm = RepositoryFactory.getOntologyRepository().getTermByZdbID(form.getSubTerm().getZdbID());
            form.setSubTerm(subterm);
        }

        DevelopmentStage startStage = getAnatomyRepository().getStage(form.getStartStage());
        form.setStartStage(startStage);

        DevelopmentStage endStage = getAnatomyRepository().getStage(form.getEndStage());
        form.setEndStage(endStage);

        AntibodyService abStat = new AntibodyService(ab);

        ExpressionSummaryCriteria criteria = abStat.createExpressionSummaryCriteria(superterm, subterm, startStage, endStage, form.isOnlyFiguresWithImg());
        form.setExpressionSummaryCriteria(criteria);
        abStat.createFigureSummary(criteria);

        form.setAntibodyStat(abStat);
        form.setAntibody(ab);

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("antibody-figure-summary.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, ab.getName());

        return modelAndView;
    }
}
