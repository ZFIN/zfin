package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Class DoBlastThread provides a thread for the blast process to run it
 * so that it doesn't hang the calling process but is allowed to finish.
 */
public class SMPBlastQueryThread extends AbstractQueryThread {

    private final static Logger logger = Logger.getLogger(SMPBlastQueryThread.class);

    private BlastService blastService ;

    public SMPBlastQueryThread(XMLBlastBean xmlBlastBean,BlastService blastService) {
        super(xmlBlastBean);
        this.blastService = blastService ;
    }

    public void run() {
        try {
            startBlast();

            String xml = blastService.robustlyBlastOneDBToString(xmlBlastBean);

            // return if no way to process
            if (xml == null) {
                throw new BlastDatabaseException("blast result was null for :" + xmlBlastBean);
            }

            logger.info("writing to file: " + xmlBlastBean.getResultFile());
            BufferedWriter writer = new BufferedWriter(new FileWriter(xmlBlastBean.getResultFile()));
            writer.write(xml);
            writer.close();
        }
        catch (Exception bde) {
            logger.error(bde);
        }
        finishBlast();
    }

}


