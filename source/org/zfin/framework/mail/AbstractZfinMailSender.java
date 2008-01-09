package org.zfin.framework.mail;

import org.zfin.properties.ZfinProperties;

/**
 */
public abstract class AbstractZfinMailSender implements MailSender {

    public boolean sendMail(String subject, String message, String[] recipients) {
        return sendMail(subject, message, true, ZfinProperties.getAdminEmailAddresses()[0], recipients);
    }

    public String prependSubject(String initialSubject) {
        return "From [" + System.getenv("DOMAIN_NAME") + "] on [" + System.getenv("HOST") + "]: " + initialSubject;
    }
}
