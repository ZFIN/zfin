package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for requesting a new anatomical structure.
 * It sends out an email and acknowledges the users input.
 */
public class RequestNewAnatomyTermController extends SimpleFormController {

    private MailSender mailSender;
    private static final String NEWLINE = "\r";

    public RequestNewAnatomyTermController() {
        setCommandClass(RequestNewAnatomyTermBean.class);
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        RequestNewAnatomyTermBean formBean = (RequestNewAnatomyTermBean) command;
        StringBuffer emailContents = new StringBuffer();
        emailContents.append(formBean.getTermDetail());
        emailContents.append(NEWLINE);
        emailContents.append(" ************* End of description ***************");
        emailContents.append(NEWLINE);
        if (!StringUtils.isEmpty(formBean.getFirstname())) {
            emailContents.append(" FIRST NAME: ");
            emailContents.append(formBean.getFirstname());
            emailContents.append(NEWLINE);
        }
        if (!StringUtils.isEmpty(formBean.getLastname())) {
            emailContents.append(" LAST NAME: ");
            emailContents.append(formBean.getLastname());
            emailContents.append(NEWLINE);
        }
        if (!StringUtils.isEmpty(formBean.getInstitution())) {
            emailContents.append(" INSTITUTION: ");
            emailContents.append(formBean.getInstitution());
            emailContents.append(NEWLINE);
        }
        if (!StringUtils.isEmpty(formBean.getEmail())) {
            emailContents.append(" EMAIL ADDRESS: ");
            emailContents.append(formBean.getEmail());
            emailContents.append(NEWLINE);
        }
        mailSender.sendMail("Request for new Anatomical Structure", emailContents.toString(),
                ZfinProperties.splitValues(ZfinPropertiesEnum.REQUEST_NEW_ANATOMY_EMAIL));
        return new ModelAndView("request-term-feedback.page", LookupStrings.FORM_BEAN, formBean);
    }

    public MailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }
}