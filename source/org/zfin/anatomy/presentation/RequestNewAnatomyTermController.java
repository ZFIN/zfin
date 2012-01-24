package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 * Controller for requesting a new anatomical structure.
 * It sends out an email and acknowledges the users input.
 */
@Controller
public class RequestNewAnatomyTermController {

    private static final Logger LOG = Logger.getLogger(RequestNewAnatomyTermController.class);

    private static final String NEWLINE = "\r";

    @ModelAttribute("formBean")
    public RequestNewAnatomyTermBean getDefaultFormBean() {
        return new RequestNewAnatomyTermBean();
    }

    @RequestMapping("/request-new-anatomy-term")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Request new anatomy term");
        return "anatomy/request-new-anatomy-term.page";
    }

    @RequestMapping(value = "/request-new-anatomy-term-submit", method = RequestMethod.POST)
    public String doSearch(Model model,
                           @ModelAttribute("formBean") RequestNewAnatomyTermBean formBean,
                           BindingResult result
    ) throws Exception {

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
        getMailSender().sendMail("Request for new Anatomical Structure", emailContents.toString(),
                ZfinProperties.splitValues(ZfinPropertiesEnum.REQUEST_NEW_ANATOMY_EMAIL));
        return "anatomy/request-term-feedback.page";
    }

    public MailSender getMailSender() {
        MailSender mailSender = null;
        if (ZfinPropertiesEnum.EMAIL_SENDER_CLASS.value() != null) {
            String className = ZfinPropertiesEnum.EMAIL_SENDER_CLASS.value();
            try {
                Class clazz = Class.forName(className);
                mailSender = (AbstractZfinMailSender) clazz.newInstance();
                return mailSender;
            } catch (ClassNotFoundException e) {
                LOG.error(e);
            } catch (InstantiationException e) {
                LOG.error(e);
            } catch (IllegalAccessException e) {
                LOG.error(e);
            }
        }
        return mailSender;
    }
}