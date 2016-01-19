package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.validation.Valid;

/**
 * Controller for requesting a new anatomical structure.
 * It sends out an email and acknowledges the users input.
 */
@Controller
@RequestMapping("/ontology")
public class RequestNewAnatomyTermController {

    private static final String NEWLINE = "\r";

    @ModelAttribute("formBean")
    public RequestNewAnatomyTermBean getDefaultFormBean() {
        return new RequestNewAnatomyTermBean();
    }

    @Autowired
    private javax.servlet.http.HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new RequestNewAnatomyTermBeanValidator());
    }


    @RequestMapping("/request-new-anatomy-term")
    protected String showRequestForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Request new anatomy term");
        return "anatomy/request-new-anatomy-term.page";
    }

    @RequestMapping(value = "/request-new-anatomy-term-submit", method = RequestMethod.POST)
    public String requestNewTerm (Model model,
                                  @Valid @ModelAttribute("formBean") RequestNewAnatomyTermBean formBean,
                                  BindingResult result) throws Exception {

        if(result.hasErrors())
            return showRequestForm(model);

        StringBuilder emailContents = new StringBuilder();
        emailContents.append(formBean.getTermDetail());
        emailContents.append(NEWLINE);
        emailContents.append(" ************* End of description ***************");
        emailContents.append(NEWLINE);
        if (!StringUtils.isEmpty(formBean.getFirstName())) {
            emailContents.append(" FIRST NAME: ");
            emailContents.append(formBean.getFirstName());
            emailContents.append(NEWLINE);
        }
        if (!StringUtils.isEmpty(formBean.getLastName())) {
            emailContents.append(" LAST NAME: ");
            emailContents.append(formBean.getLastName());
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
        AbstractZfinMailSender.getInstance().sendMail("Request for new Anatomical Structure", emailContents.toString(),
                ZfinProperties.splitValues(ZfinPropertiesEnum.REQUEST_NEW_ANATOMY_EMAIL));
        return "anatomy/request-term-feedback.page";
    }

}