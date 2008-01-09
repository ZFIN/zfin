package org.zfin.framework.mail;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.zfin.properties.ZfinProperties;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * Uses JavaMail integrated with Spring to send email.
 */
public class IntegratedJavaMailSender extends MailSender{

    static Logger logger = Logger.getLogger(IntegratedJavaMailSender.class) ;

//    private JavaMailSender mailSender = new JavaMailSenderImpl();
//    private String mailHost = "mailhost.cs.uoregon.edu";

    private final String DEFAULT_MAILHOST = "mailhost.cs.uoregon.edu" ;
    private JavaMailSender mailSender = new JavaMailSenderImpl() ;;
    private String mailHost = DEFAULT_MAILHOST ;


    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String... recipients) {

        MimeMessage mimeMessage = mailSender.createMimeMessage() ;
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try{
            helper.setTo(recipients);
            // can only handle first
            helper.setFrom(ZfinProperties.getAdminEmailAddresses()[0]);

            if(doDefaultSubjectHeader){
                subject = prependSubject(subject) ;
            }
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            // todo: move to ZfinProperties

            if(mailSender instanceof JavaMailSenderImpl ){
                if(((JavaMailSenderImpl) mailSender).getHost()==null){
                    ((JavaMailSenderImpl) mailSender).setHost(mailHost);
                }
            }
            mailSender.send(mimeMessage);
            return true ;
        }
        catch(Exception e){
            System.out.println("Failed to send mail with subject[" + subject+"]\n"+e);
            logger.error("Failed to send mail with subject[" + subject+"]",e);
            return false ;
        }

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
     * @note  Set email to the one you want it to go to.
     * @param args
     */
    public static void main(String args[]){
        String file = "zfin-properties.xml";
        String dirRel = System.getenv("TARGETROOT"); ;
        String dir = dirRel + "/" + "home/WEB-INF/" ;
        ZfinProperties.init(dir, file);

        MailSender sender = new IntegratedJavaMailSender() ;
        sender.sendMail("test email from IntegratedJavaMailSender: "+new Date(),"javamail message of test email: "+
                new Date(), ZfinProperties.getAdminEmailAddresses());
    }
}
