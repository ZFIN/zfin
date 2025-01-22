package org.zfin.framework.mail;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Uses JavaMail integrated with Spring to send email.
 */
public class IntegratedJavaMailSender extends AbstractZfinMailSender {

    private static final Logger logger = LogManager.getLogger(IntegratedJavaMailSender.class);

    private JavaMailSender mailSender = new JavaMailSenderImpl();
    private String mailHost = ZfinPropertiesEnum.SMTP_HOST.value();


    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, false);
    }

    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients, boolean useHtml) {
        return sendMailToMultipleRecipients(subject, message, doDefaultSubjectHeader, fromEmail, recipients, useHtml, null);
    }

    private boolean sendMailToMultipleRecipients(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                                                String[] recipients, boolean useHtml, String filename) {
        boolean success = true;
        for(String recipient : recipients) {
            if (!sendMailToSingleRecipient(subject, message, doDefaultSubjectHeader, fromEmail, recipient, useHtml, filename)) {
                success = false;
            }
        }
        return success;
    }

    private boolean sendMailToSingleRecipient(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                                                String recipient, boolean useHtml, String filename) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            mimeMessage.setFrom(new InternetAddress(filterEmail(fromEmail)));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(filterEmail(recipient)));

            if (doDefaultSubjectHeader) {
                subject = prependSubject(subject);
            }
            mimeMessage.setSubject(subject, "UTF-8");

            MimeBodyPart messageBody = new MimeBodyPart();
            if (useHtml) {
                messageBody.setContent(message, "text/html; charset=UTF-8");
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
            logger.error("Failed to send mail to[" + recipient + "] with subject[" + subject + "]", e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients) {
        return sendMail(subject, message, doDefaultSubjectHeader, fromEmail, recipients, true);
    }

    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients, String filename) {
        return sendMailToMultipleRecipients(subject, message, doDefaultSubjectHeader, fromEmail, recipients, true, filename);
    }

    public String[] filterEmail(String... emailAddresses) {
        String[] returnEmails = new String[emailAddresses.length];
        int i = 0;
        for (String emailAddress : emailAddresses) {
            returnEmails[i++] = filterEmail(emailAddress);
        }
        return returnEmails;
    }

    public String filterEmail(String emailAddress) {
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
        List<String> emailAddresses = List.of(ZfinProperties.getAdminEmailAddresses());
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }

        if (args.length < 2) {
            subjectText = "test email from IntegratedJavaMailSender: " + new Date();
            messageText = "javamail message of test email: " + new Date();
        } else if (args.length >= 2) {
            subjectText = args[0] + " - " + new Date();
            messageText = args[1] + " - " + new Date();
            if (args.length > 2) {
                emailAddresses = new ArrayList<>();
                for(int i = 2; i < args.length; i++) {
                    emailAddresses.addAll(List.of(args[i].split(" ")));
                }
            }
        }
        System.out.println("Sending mail with arguments: " + stringBuilder.toString() + " to the following addresses: " + String.join(",", emailAddresses));

        MailSender sender = new IntegratedJavaMailSender();
        sender.sendMail(subjectText, messageText, emailAddresses.toArray(new String[0]));
    }

}
