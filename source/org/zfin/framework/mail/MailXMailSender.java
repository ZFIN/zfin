package org.zfin.framework.mail;

import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailXMailSender extends AbstractZfinMailSender {
    static Logger logger = Logger.getLogger(MailXMailSender.class);


    public boolean sendMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail,
                            String[] recipients) {
        try {
            List<String> commandList = new ArrayList<String>();
            commandList.add("mailx");
//            commandList.add("-v");
            commandList.add("-r");
            commandList.add(fromEmail);
            commandList.add("-s");
            if (doDefaultSubjectHeader) {
                subject = prependSubject(subject);
            }
            commandList.add(subject);
            for (String recipientEmail : recipients) {
                commandList.add(recipientEmail);
            }

            String[] commands = new String[commandList.size()];
            commands = commandList.toArray(commands); // redundant, but . . .
            for (String command : commands) {
                System.out.print(command + " ");
            }
            System.out.println();

            Process process = Runtime.getRuntime().exec(commands);
//            todo: add buffer output onto process . .. may need to use threading
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            System.out.println("flushing message: " + message);
            processInput.write(message + "\n");
            processInput.close();
            int exitValue = process.waitFor();

            // dump output
            String line;
            String errorOutput = "";
            while ((line = stderr.readLine()) != null) {
                errorOutput += line + "\n";
                logger.fatal("Failed to send mail due to process failure: " + errorOutput);
                System.out.println("Failed to send mail due to process failure: " + errorOutput);
            }

            // dump output 
            String standardOutput = "";
            while ((line = stdout.readLine()) != null) {
                standardOutput += line + "\n";
                logger.fatal("Mail output: " + standardOutput);
                System.out.println("Mail output: " + standardOutput);
            }

            return exitValue == 0;
        }
        catch (Exception e) {
            logger.fatal("Failed to send mail because of error.", e);
            System.out.println("Failed to send mail because of error\n" + e);
            return false;
        }

    }

    /**
     * ToDo: Needs to be implemented yet...
     * @param subject
     * @param message
     * @param doDefaultSubjectHeader
     * @param fromEmail
     * @param recipients
     * @return
     */
    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients) {
        return false;
    }

    @Override
    public boolean sendHtmlMail(String subject, String message, boolean doDefaultSubjectHeader, String fromEmail, String[] recipients, String filename) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String args[]) {
//        System.out.println("send a mail message") ;
////        boolean status = MailXMailSender.sendMail("subject TTTT of test email: "+new Date(),"message of test email: "+new Date(), ZfinProperties.getValidationEmailOtherString(true));
//        MailSender sender = new MailXMailSender() ;
//        sender.sendMail("subject TTTT of test email: "+new Date(),"message of test email: "+new Date(), "ndunn@uoregon.edu","ndunn@mac.com");

        ZfinProperties.init();

        MailSender sender = new MailXMailSender();
        sender.sendMail("test email from MailXMailSender: " + new Date(), "javamail message of test email: " +
                new Date(), ZfinProperties.getAdminEmailAddresses());

    }
}
