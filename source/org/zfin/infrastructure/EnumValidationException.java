package org.zfin.infrastructure;

/**
 * Created by IntelliJ IDEA.
 * User: nathandunn
 * Date: Sep 28, 2007
 * Time: 2:35:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnumValidationException extends Exception{

    private final static String internalMessage = "Failed to validate enum versus database" ;

    public EnumValidationException(Exception e){
        super(internalMessage,e) ;
    }

    public EnumValidationException(String message){
        super(internalMessage+":\n"+message) ;
    }

    public EnumValidationException(String message,Throwable t){
        super(internalMessage+":\n"+message,t) ;
    }


}
