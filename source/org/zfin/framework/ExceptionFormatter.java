package org.zfin.framework;

/**
 * User: nathandunn
 * Date: Dec 5, 2007
 * Time: 9:23:14 AM
 */
public class ExceptionFormatter {
    public static String generateTraceFromCause(Exception e) {
        String message = e.getMessage() + "\n";
        message += e.toString() + "\n";
        Throwable cause ;
        while( (cause  = e.getCause())!=null){
            message +=  cause.toString() + "\n" ;
        }
        return message ; 
    }

}
