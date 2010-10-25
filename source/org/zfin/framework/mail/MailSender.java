package org.zfin.framework.mail;

/**
 */
public interface MailSender {

    boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                     String[] recipients);

    boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                     String[] recipients);

    boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                     String[] recipients, String filename);

    boolean sendMail(String subject, String message, String[] recipients);

    String prependSubject(String initialSubject);

}
