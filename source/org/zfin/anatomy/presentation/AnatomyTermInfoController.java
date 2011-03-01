package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class that serves the anatomy term detail page.
 */
public class AnatomyTermInfoController extends AnatomyTermDetailController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermInfoController.class);

    public AnatomyTermInfoController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());


        ModelAndView modelAndView = new ModelAndView("anatomy-terminfo.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getTermName());

        return modelAndView;
    }

}