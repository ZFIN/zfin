package org.zfin.framework.mail;

import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;


/**
 */
public abstract class AbstractZfinMailSender implements MailSender {

    public boolean sendMail(String subject, String message, String[] recipients) {
        return sendMail(subject, message, true, ZfinProperties.getValidationOtherEmailAddresses()[0], recipients);
    }


    public String prependSubject(String initialSubject) {
        return "From [" + ZfinPropertiesEnum.DOMAIN_NAME + "] on [" + ZfinPropertiesEnum.HOSTNAME + "]: " + initialSubject;
    }
}
