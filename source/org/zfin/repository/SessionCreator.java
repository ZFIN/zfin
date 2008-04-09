package org.zfin.repository;

import org.zfin.framework.HibernateSessionCreator;
import org.apache.log4j.Logger;

/**
 * This class instantiates exactly one session per VM.
 */
public class SessionCreator {

    private static Logger logger = Logger.getLogger(SessionCreator.class) ;

    private static boolean isInstantiated = false ;

    /**
     * Only creates session if in hosted mode.
     * @return boolean Instatiation success
     */
    public  static boolean instantiateDBForHostedMode(){
        String gwtArgs = System.getProperty("gwt.args") ;
        if(gwtArgs!=null && gwtArgs.indexOf("hosted")>=0 && isInstantiated==false){
            logger.warn("running in hosted mode");
            isInstantiated = createSession() ;
        }
        
        return isInstantiated ;
    }

    public static boolean createSession(){
        try{
            String[] confFiles = {
                    "anatomy.hbm.xml",
                    "mutant.hbm.xml",
                    "orthology.hbm.xml",
                    "people.hbm.xml",
                    "sequence.hbm.xml",
                    "blast.hbm.xml",
                    "reno.hbm.xml",
                    "publication.hbm.xml",
                    "marker.hbm.xml",
                    "mapping.hbm.xml",
                    "infrastructure.hbm.xml",
                    "expression.hbm.xml"
            };
            new HibernateSessionCreator(false, confFiles);
            return true ;
        }
        catch(Exception e){
            logger.error("session creation excpetion",e);
            return false ;
        }
    }

}
