package org.zfin.framework.mail;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.zfin.properties.ZfinProperties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

/**
 * Uses JavaMail integrated with Spring to send email.
 */
public class IntegratedJavaMailSender extends AbstractZfinMailSender {

    private static Logger logger = Logger.getLogger(IntegratedJavaMailSender.class);

    private final String DEFAULT_MAILHOST = "localhost";
    private JavaMailSender mailSender = new JavaMailSenderImpl();
    private String mailHost = DEFAULT_MAILHOST;


    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, false);
    }

    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients, boolean useHtml) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, useHtml, null);
    }

    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients, boolean useHtml, String filename) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            mimeMessage.setFrom(new InternetAddress(filterEmail(fromEmail)));
            for (String recipient : recipients) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(filterEmail(recipient)));
            }

            if (doDefaultSubjectHeader) {
                subject = prependSubject(subject);
            }
            mimeMessage.setSubject(subject);

            MimeBodyPart messageBody = new MimeBodyPart();
            if (useHtml) {
                messageBody.setContent(message, "text/html");
            } else {
                messageBody.setText(message);
            }

            // create the Multipart and add its parts to it
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBody);

            // attach the file to the message if it exists
            if (filename != null) {
                MimeBodyPart attachment = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(filename);
                if (fds.getFile().exists()) {
                    attachment.setDataHandler(new DataHandler(fds));
                    attachment.setFileName(fds.getName());
                    multipart.addBodyPart(attachment);
                } else {
                    logger.error("Could not find file " + fds.getFile().getAbsolutePath() + "to attach to email");
                }
            }

            mimeMessage.setContent(multipart);

            if (mailSender instanceof JavaMailSenderImpl) {
                if (((JavaMailSenderImpl) mailSender).getHost() == null) {
                    ((JavaMailSenderImpl) mailSender).setHost(mailHost);
                }
            }
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send mail with subject[" + subject + "]", e);
            return false;
        }

    }

    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, true);
    }

    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients, String filename) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, true, filename);
    }

    private String[] filterEmail(String... emailAddresses) {
        String[] returnEmails = new String[emailAddresses.length];
        int i = 0;
        for (String emailAddress : emailAddresses) {
            returnEmails[i++] = filterEmail(emailAddress);
        }
        return returnEmails;
    }

    private String filterEmail(String emailAddress) {
        return emailAddress.replaceAll("\\\\@", "@");
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

        String messageText = null;
        String subjectText = null;
        String[] emailAddresses = ZfinProperties.getAdminEmailAddresses();
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        System.out.println("Sending mail with arguments: " + stringBuilder.toString() + " to " + emailAddresses[0]);
        if (args.length < 2) {
            subjectText = "test email from IntegratedJavaMailSender: " + new Date();
            messageText = "javamail message of test email: " + new Date();
        } else if (args.length >= 2) {
            subjectText = args[0] + " - " + new Date();
            messageText = args[1] + " - " + new Date();
            if (args.length > 2) {
                emailAddresses = args[2].split(" ");
            }
        }

        MailSender sender = new IntegratedJavaMailSender();
        sender.sendMail(subjectText, messageText, emailAddresses);
    }

}
