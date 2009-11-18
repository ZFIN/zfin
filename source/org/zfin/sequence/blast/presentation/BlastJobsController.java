package org.zfin.sequence.blast.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastStatistics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class BlastJobsController extends AbstractCommandController {

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        BlastJobsBean blastJobsBean = (BlastJobsBean) o ;

        blastJobsBean.setBlastThreadCollection(BlastQueryThreadCollection.getInstance());
        blastJobsBean.setBlastStatistics(BlastStatistics.getInstance());

        return new ModelAndView("blast-jobs.page", LookupStrings.FORM_BEAN, blastJobsBean);
    }
}
