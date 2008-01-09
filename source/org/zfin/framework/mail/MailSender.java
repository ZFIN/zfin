package org.zfin.framework.mail;

import org.zfin.properties.ZfinProperties;

import java.util.List;

/**
 */
public abstract class MailSender {

    public abstract boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail , String... recipients) ;

    public boolean sendMail(String subject, String message, String... recipients) {
        return sendMail(subject,message,true, ZfinProperties.getAdminEmailAddresses()[0],recipients) ;
    }

    public boolean sendMail(String subject, String message, List<String> recipients) {
        String[] recipientList = new String[recipients.size()] ;
        return sendMail(subject,message,true,ZfinProperties.getAdminEmailAddresses()[0],recipients.toArray(recipientList)) ;
    }


    protected String prependSubject(String initialSubject){
        return "From [" + System.getenv("DOMAIN_NAME") + "] on [" + System.getenv("HOST") + "]: " + initialSubject;
    }

}
