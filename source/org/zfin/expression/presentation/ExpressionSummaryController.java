package org.zfin.expression.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.ExpressionStageAnatomyContainer;
import org.zfin.marker.Gene;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.presentation.LookupStrings;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExpressionSummaryController extends AbstractCommandController {

    private static ExpressionRepository xsr = RepositoryFactory.getExpressionSummaryRepository();
    private static final Logger LOG = Logger.getLogger(ExpressionSummaryController.class);

    public ExpressionSummaryController() {
        setCommandClass(ExpressionSummaryBean.class);
    }

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  Object command, BindException errors) throws Exception {
        ExpressionSummaryBean form = (ExpressionSummaryBean) command;

        LOG.info("Start Expression Summary Controller");

        ModelAndView modelAndView = new ModelAndView("expression-summary.page", LookupStrings.FORM_BEAN, form);

        Gene gene = form.getGene();
        

        ExpressionStageAnatomyContainer xsac = xsr.getExpressionStages(gene);

        form.setGene(gene);
        form.setXsaList(xsac.getXsaList());

        return modelAndView;

    }


}
