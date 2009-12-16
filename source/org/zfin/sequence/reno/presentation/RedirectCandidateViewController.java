package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Class CandidateController.
 */
public class RedirectCandidateViewController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(NomenclatureCandidateController.class);


    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  Object command, BindException e) throws Exception {
        CandidateBean candidateBean = (CandidateBean) command;
        RunCandidate rc = RepositoryFactory.getRenoRepository().getRunCandidateByID(candidateBean.getRunCandidate().getZdbID());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(LookupStrings.FORM_BEAN, candidateBean);
        if (rc.getRun().isNomenclature()) {
            modelAndView.setViewName("forward:/action/reno/nomenclature-candidate-view");
        } else {
            modelAndView.setViewName("forward:/action/reno/redundancy-candidate-view");
        }
        return modelAndView;
    }

}
