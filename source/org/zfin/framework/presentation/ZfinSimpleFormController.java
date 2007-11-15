package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class ZfinSimpleFormController extends SimpleFormController {

    public ZfinSimpleFormController() {
        // AbstractFormController sets default cache seconds to 0.
        super();
    }

    /**
     * This implementation calls <code>showForm</code> in case of errors,
     * and delegates to <code>onSubmit</code>'s full version else.
     * <p>This can only be overridden to check for an action that should be executed
     * without respect to binding errors, like a cancel action. To just handle successful
     * submissions without binding errors, override one of the <code>onSubmit</code>
     * methods or <code>doSubmitAction</code>.
     * @see #showForm(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException)
     * @see #onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
     * @see #onSubmit(Object, org.springframework.validation.BindException)
     * @see #onSubmit(Object)
     * @see #doSubmitAction(Object)
     */
    protected ModelAndView processFormSubmission(
            HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws Exception {

        if (errors.hasErrors()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Data binding errors: " + errors.getErrorCount());
            }
            return processFailedForm(request, response, command, errors);
        }
        else if (isFormChangeRequest(request)) {
            logger.debug("Detected form change request -> routing request to onFormChange");
            onFormChange(request, response, command, errors);
            return showForm(request, response, errors);
        }
        else {
            logger.debug("No errors -> processing submit");
            return onSubmit(request, response, command, errors);
        }
    }

    protected ModelAndView processFailedForm(
            HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
    throws Exception{
        return showForm(request, response, errors);
    }

}
