package org.zfin.framework.mail;

/**
 */
public abstract class MailSender {

    public abstract boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String... recipients) ;

    public boolean sendMail(String subject, String message, String... recipients) {
        return sendMail(subject,message,true,recipients) ;
    }

    protected String prependSubject(String initialSubject){
        return "From [" + System.getenv("DOMAIN_NAME") + "] on [" + System.getenv("HOST") + "]: " + initialSubject;
    }

}
