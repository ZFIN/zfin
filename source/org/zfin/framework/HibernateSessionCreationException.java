package org.zfin.framework ;
/**
 *  Class HibernateSessionCreationException.  Throws when fails to create a hibernate session.
 */

public class HibernateSessionCreationException extends Exception {



    public HibernateSessionCreationException(String message){
        super(message ) ; 

    }

    public String toString(){
        String returnString = "Failed to create hibernate session: "+ getMessage() + "\n" ;

        StackTraceElement[] elements = getStackTrace() ; 
        for(int i = 0 ; i < elements.length ; i++){
            returnString += elements[i].toString() +"\n" ; 
        }
        return returnString ; 
    }


} 


