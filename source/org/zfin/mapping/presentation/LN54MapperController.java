package org.zfin.mapping.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.validation.Valid;

@Controller
@RequestMapping("/devtool")
public class LN54MapperController {

    private Logger logger = LogManager.getLogger(LN54MapperController.class);

    private AbstractZfinMailSender mailSender = new IntegratedJavaMailSender();

    @RequestMapping(value = "/ln54mapper", method = RequestMethod.GET)
    protected String showLN54MapperPage(@ModelAttribute("formBean") LN54MapperBean form) throws Exception {
        return "mapping/ln54mapper-request";
    }

    @Autowired
    private LN54MapperValidator validator;

    @RequestMapping("/ln54mapper")
    public String sendRequest(@Valid @ModelAttribute("formBean") LN54MapperBean form,
                              BindingResult result) throws Exception {

        validator.validate(form, result);
        if (result.hasErrors())
            return "mapping/ln54mapper-request";

        String emailContents = "Request for LN mapping";
        emailContents += "\n";
        emailContents += "Name: " + form.getName() + "\n";
        emailContents += "Email: " + form.getEmail() + "\n";
        emailContents += "Character Marker: " + form.getScoringVector() + "\n";
        if (!StringUtils.isEmpty(form.getMarkerName())) {
            emailContents += "Optional Marker Name: " + form.getMarkerName() + "\n";
        }


        String[] emails = new String[3];
        emails[0] = ZfinPropertiesEnum.LN54_CONTACT_EMAIL.value(); // the contact person! from zfin admin
        logger.debug("ZfinPropertiesEnum.LN54_CONTACT_EMAIL.value(): " + ZfinPropertiesEnum.LN54_CONTACT_EMAIL.value());
        int commaIndex = form.getEmail().indexOf(",");
        // in the case where both the command and request object get the same data
        if (commaIndex > 0) {
            emails[1] = form.getEmail().split(",")[0]; // zfin properties
        } else {
            emails[1] = form.getEmail(); // zfin properties
        }
        logger.debug("form.getEmail(): " + form.getEmail());
        emails[2] = ZfinProperties.getAdminEmailAddresses()[0]; // zfin admin
        logger.debug("ZfinProperties.getAdminEmailAddresses()[0]: " + ZfinProperties.getAdminEmailAddresses()[0]);
        mailSender.sendMail("Request for new LN 54 mapping", emailContents, false, ZfinPropertiesEnum.LN54_CONTACT_EMAIL.value(), emails);

        return "mapping/ln54mapper-result";
    }

}
