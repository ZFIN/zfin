package org.zfin.framework.mail;

import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;

import java.io.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class MailSender{
    static Logger logger = Logger.getLogger(MailSender.class) ;

    public static boolean sendMail(String subject, String message, String... recipients) {
        try{
            List<String> commandList = new ArrayList<String>() ;
            commandList.add("mailx") ;
//            commandList.add("-v");
            commandList.add("-s");
            commandList.add(subject)  ;
            for(String recipientEmail: recipients){
                commandList.add(recipientEmail) ;
            }

            String[] commands = new String[commandList.size()] ;
            commands = commandList.toArray(commands); // redundant, but . . .
            for(String command: commands){
                System.out.print(command + " ");
            }
            System.out.println() ; 

            Process process = Runtime.getRuntime().exec(commands) ;
//            todo: add buffer output onto process . .. may need to use threading
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream())) ;
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream())) ;
            BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            System.out.println("flushing message: "+message) ;
            processInput.write(message+"\n");
            processInput.close();
            int exitValue = process.waitFor() ;


            // dump output
            String line ;
            String errorOutput = "" ;
            while( (line = stderr.readLine())!=null){
                errorOutput += line + "\n";
                logger.fatal("Failed to send mail due to process failure: "+ errorOutput);
                System.out.println("Failed to send mail due to process failure: "+ errorOutput);
            }

            // dump output 
            String standardOutput = "" ;
            while( (line = stdout.readLine())!=null){
                standardOutput += line + "\n";
                logger.fatal("Mail output: "+ standardOutput);
                System.out.println("Mail output: "+ standardOutput);
            }

            return exitValue==0 ;
        }
        catch(Exception e){
            logger.fatal("Failed to send mail because of error.",e);
            System.out.println("Failed to send mail because of error\n"+e);
            return false ;
        }

    }

    public static void main(String args[]){
        System.out.println("send a mail message") ;
        boolean status = MailSender.sendMail("subject TTTT of test email: "+new Date(),"message of test email: "+new Date(), ZfinProperties.getValidationEmailOther(true));
//        boolean status = MailSender.sendMail("subject TTTT of test email: "+new Date(),"message of test email: "+new Date(), "ndunn@uoregon.edu","ndunn@mac.com");
        System.out.println("sent: "+ status) ;
    }
}
