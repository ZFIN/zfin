package org.zfin.framework.mail;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.biojava.bio.AnnotationType;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;


/**
 */
public abstract class AbstractZfinMailSender implements MailSender {

    private static final Logger LOG = LogManager.getLogger(AnnotationType.Abstract.class);

    public boolean sendMail(String subject, String message, String[] recipients) {
        return sendMail(subject, message, true, ZfinProperties.getValidationOtherEmailAddresses()[0], recipients);
    }

    public boolean sendHtmlMail(String subject, String message, String[] recipients, String filename) {
        return sendHtmlMail(subject, message, true, ZfinProperties.getValidationOtherEmailAddresses()[0], recipients, filename);
    }

    public boolean sendHtmlMail(String subject, String message, String[] recipients) {
        return sendHtmlMail(subject, message, true, ZfinProperties.getValidationOtherEmailAddresses()[0], recipients);
    }



    public String prependSubject(String initialSubject) {
        return "From [" + ZfinPropertiesEnum.DOMAIN_NAME + "] on [" + ZfinPropertiesEnum.HOSTNAME + "]: " + initialSubject;
    }

    public static MailSender getInstance() {
        MailSender mailSender;
        if (ZfinPropertiesEnum.EMAIL_SENDER_CLASS.value() != null) {
            String className = ZfinPropertiesEnum.EMAIL_SENDER_CLASS.value();
            try {
                Class clazz = Class.forName(className);
                mailSender = (MailSender) clazz.newInstance();
                return mailSender;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error(e);
            }
        }
        return null;
    }
}
