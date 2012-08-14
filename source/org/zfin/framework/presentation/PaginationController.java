package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that is called to produce Pagination = walking windows.
 */
public class PaginationController extends AbstractCommandController {

    public PaginationController() {
        setCommandClass(ApgPaginationBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        ApgPaginationBean bean = (ApgPaginationBean) command;
        // apg call
        if (bean.getFirstPageRecord() > 0)
            if (bean.getMaxDisplayRecordsInteger() > 1)
                bean.setPageInteger(bean.getFirstPageRecord() / bean.getMaxDisplayRecordsInteger() + 1);
            else
                bean.setPageInteger(bean.getFirstPageRecord());
        return new ModelAndView("pagination.page", LookupStrings.FORM_BEAN, bean);
    }
}
