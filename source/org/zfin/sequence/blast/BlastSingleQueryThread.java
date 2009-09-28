package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

/**
 *  Class DoBlastThread provides a thread for the blast process to run it
 *  so that it doesn't hang the calling process but is allowed to finish. 
 */
public class BlastSingleQueryThread extends Thread {

    private final static Logger logger = Logger.getLogger(BlastSingleQueryThread.class) ;


    protected XMLBlastBean xmlBlastBean ;

    public BlastSingleQueryThread(XMLBlastBean xmlBlastBean){
        this.xmlBlastBean = xmlBlastBean ;
    }

    public void run(){
        try{
            List<Database> databases = xmlBlastBean.getActualDatabaseTargets() ;
            if(databases.size()!=1){
                throw new BlastDatabaseException("wrong number of databases: "+ databases.size()) ;
            }
            String xml = MountedWublastBlastService.getInstance().blastOneDBToString(xmlBlastBean,databases.get(0)) ;
            // return if no way to process
            if(xml==null){
                throw new BlastDatabaseException("blast result was null for :"+xmlBlastBean) ;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(xmlBlastBean.getResultFile())) ;
            writer.write(xml);
            writer.close(); 
        }
        catch(Exception bde){
            logger.error(bde.fillInStackTrace()) ;
        }
    }

    public XMLBlastBean getXmlBlastBean(){
        return xmlBlastBean ;
    }
} 


