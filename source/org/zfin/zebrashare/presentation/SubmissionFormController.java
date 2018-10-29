package org.zfin.zebrashare.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

import javax.validation.Valid;

@Controller
@RequestMapping("/zebrashare")
public class SubmissionFormController {

    private static final Logger LOG = Logger.getLogger(SubmissionFormController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(new SubmissionFormValidator());
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String showSubmissionForm(@ModelAttribute("formBean") SubmissionFormBean formBean) {
        Person user = ProfileService.getCurrentSecurityUser();
        if (user != null) {
            if (StringUtils.isEmpty(formBean.getSubmitterName())) {
                formBean.setSubmitterName(user.getDisplay());
            }
            if (StringUtils.isEmpty(formBean.getSubmitterEmail())) {
                formBean.setSubmitterEmail(user.getEmail());
            }
        }
        return "zebrashare/new-submission.page";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(@ModelAttribute("formBean") SubmissionFormBean formBean) {
        return "zebrashare/home.page";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processSubmissionForm(@Valid @ModelAttribute("formBean") SubmissionFormBean formBean,
                                        BindingResult result) {
        if (result.hasErrors()) {
            return "zebrashare/new-submission.page";
        }

        LOG.warn(formBean.getTitle());
        LOG.warn(formBean.getAuthors());
        LOG.warn(formBean.getAbstractText());

        return "redirect:/";
    }

}
