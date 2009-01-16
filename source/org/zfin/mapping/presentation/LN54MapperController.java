package org.zfin.mapping.presentation;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map ;
import java.util.HashMap;

/**
 *
 */
public class LN54MapperController extends SimpleFormController {

    private Logger logger = Logger.getLogger(LN54MapperController.class) ;

    private AbstractZfinMailSender mailSender ;

    protected Map referenceData(HttpServletRequest httpServletRequest, java.lang.Object object, Errors errors)
            throws java.lang.Exception {
        LN54MapperBean form = (LN54MapperBean) object;

        Object name = httpServletRequest.getParameter("name") ;
        logger.info("name: " + name);
        if(name!=null){
            form.setName(name.toString());
        }
        Object email = httpServletRequest.getParameter("email") ;
        logger.info("email: " + email);
        if(email!=null){
            form.setEmail(email.toString());
        }

        // prepoulate with anything?
        Map map = new HashMap() ;
        map.put(LookupStrings.FORM_BEAN, form);
        return map ;
    }

    protected ModelAndView onSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    java.lang.Object object, BindException e) throws java.lang.Exception {
        LN54MapperBean form = (LN54MapperBean) object;


        ModelAndView modelAndView = new ModelAndView(getSuccessView(), LookupStrings.FORM_BEAN, form);

        String emailContents = "Request for LN mapping" ;
        emailContents += "\n" ;
        emailContents += "Name: " + form.getName() + "\n";
        emailContents += "Email: " + form.getEmail() + "\n";
        emailContents += "Character Marker: " + form.getScoringVector() + "\n";
        if(false==StringUtils.isEmpty(form.getMarkerName())){
            emailContents += "Optional Marker Name: " + form.getMarkerName() + "\n";
        }


        String[] emails = new String[3] ;
//        emails[0] = "ndunn@uoregon.edu" ; // the contact person! from zfin admin
//        ZfinProperties.getLN54ContactEmail() ;
        emails[0] = ZfinProperties.getLN54ContactEmail() ; // the contact person! from zfin admin
        emails[1] = form.getEmail() ; // zfin properties
        emails[2] = ZfinProperties.getAdminEmailAddresses()[0] ; // zfin admin
        mailSender.sendMail("Request for new LN 54 mapping", emailContents.toString(), false, ZfinProperties.getAdminEmailAddresses()[0],emails);

        return modelAndView ;
    }

    public AbstractZfinMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(AbstractZfinMailSender mailSender) {
        this.mailSender = mailSender;
    }
}
