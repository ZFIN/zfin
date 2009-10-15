package org.zfin.datatransfer;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps statistics about microarray job.
 */
public class MicroArrayBean {

    private Logger logger = Logger.getLogger(MicroArrayBean.class) ;

    List<String> messages = new ArrayList<String>() ;
    List<String> notFoundAccessions = new ArrayList<String>() ;

    public void addMessage(String s) {
        logger.info(s);
        messages.add(s) ;
    }

    public void addNotFound(String newMicroArrayAccession) {
        notFoundAccessions.add(newMicroArrayAccession) ;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder() ;
        stringBuilder.append("MESSAGES").append("\n") ;
        for(String message:messages){
            stringBuilder.append(message).append("\n") ;
        }

        stringBuilder.append("ACCESSIONS NOT FOUND").append("\n") ;

        for(String s:notFoundAccessions){
            stringBuilder.append(s).append("\n") ;
        }

        return stringBuilder.toString() ;
    }
}
