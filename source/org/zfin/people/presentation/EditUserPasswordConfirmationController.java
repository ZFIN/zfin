package org.zfin.people.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class EditUserPasswordConfirmationController extends AbstractCommandController {

    public EditUserPasswordConfirmationController() {
        setCommandClass(ProfileBean.class);
    }

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        ProfileBean bean = (ProfileBean) o;
        if (bean.deleteRecord())
            return new ModelAndView("delete-user-password-confirmation.page", LookupStrings.FORM_BEAN, bean);
        else
            return new ModelAndView("edit-user-password-confirmation.page", LookupStrings.FORM_BEAN, bean);
    }
}
