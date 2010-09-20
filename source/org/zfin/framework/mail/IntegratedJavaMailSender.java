package org.zfin.framework.mail;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.zfin.properties.ZfinProperties;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * Uses JavaMail integrated with Spring to send email.
 */
public class IntegratedJavaMailSender extends AbstractZfinMailSender {

    static Logger logger = Logger.getLogger(IntegratedJavaMailSender.class);

//    private JavaMailSender mailSender = new JavaMailSenderImpl();
//    private String mailHost = "mailhost.cs.uoregon.edu";

    private final String DEFAULT_MAILHOST = "smtp.uoregon.edu";
    private JavaMailSender mailSender = new JavaMailSenderImpl();
    private String mailHost = DEFAULT_MAILHOST;


    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try {
            helper.setTo(filterEmail(recipients));
            // can only handle first
            helper.setFrom(filterEmail(fromEmail));
//            helper.setFrom(ZfinProperties.getAdminEmailAddresses()[0]);

            if (doDefaultSubjectHeader) {
                subject = prependSubject(subject);
            }
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            // todo: move to ZfinProperties

            if (mailSender instanceof JavaMailSenderImpl) {
                if (((JavaMailSenderImpl) mailSender).getHost() == null) {
                    ((JavaMailSenderImpl) mailSender).setHost(mailHost);
                }
            }
            mailSender.send(mimeMessage);
            return true;
        }
        catch (Exception e) {
            System.out.println("Failed to send mail with subject[" + subject + "]\n" + e);
            logger.error("Failed to send mail with subject[" + subject + "]", e);
            return false;
        }

    }

    protected String[] filterEmail(String... emailAddresses) {
        String[] returnEmails = new String[emailAddresses.length] ;
        int i = 0 ;
        for(String emailAddress : emailAddresses){
            returnEmails[i++] = filterEmail(emailAddress) ;
        }
        return returnEmails ;
    }

    protected String filterEmail(String emailAddress) {
        return emailAddress.replaceAll("\\\\@","@") ;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    /**
     * Just a hacky test.
     *
     * @param args Main arguments
     *             Note: Set email to the one you want it to go to.
     */
    public static void main(String args[]) {
        ZfinProperties.init();

        String messageText = null ;
        String subjectText = null ;
        String[] emailAddresses = ZfinProperties.getAdminEmailAddresses();
        StringBuilder stringBuilder = new StringBuilder() ;
        for(String arg : args){
            stringBuilder.append(arg).append(" ") ;
        }
        System.out.println("Sending mail with arguments: " + stringBuilder.toString() + " to "+ emailAddresses[0]) ;
        if(args.length<2){
            subjectText = "test email from IntegratedJavaMailSender: " + new Date();
            messageText = "javamail message of test email: " + new Date() ;
        }
        else
        if(args.length>=2){
            subjectText = args[0] + " - " + new Date();
            messageText = args[1] + " - " + new Date();
            if(args.length>2){
                emailAddresses = args[2].split(" ");
            }
        }

        MailSender sender = new IntegratedJavaMailSender();
        sender.sendMail(subjectText, messageText , emailAddresses);
    }
}
