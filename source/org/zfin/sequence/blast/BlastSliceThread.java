package org.zfin.sequence.blast;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.util.Date;

/**
 * A thread for a single blast, that handles a chunk.
 */
public class BlastSliceThread extends AbstractQueryThread {

    private final Logger logger = LogManager.getLogger(BlastSliceThread.class);

    private Database database;
    private int slice;

    public BlastSliceThread(XMLBlastBean xmlBlastBean, Database database, int slice) {
        super(xmlBlastBean);
        this.database = database;
        this.slice = slice;
    }

    public int getSlice() {
        return slice;
    }

    public void run() {
        startBlast();
        try {
            Unmarshaller u;
            BlastOutput blastOutput;

            try {
                u = getJAXBBlastContext().createUnmarshaller();
            } catch (JAXBException e) {
                finishBlast();
                logger.error(e);
                return;
            }


            try {

                // so there would not be any confusion, we set to null initially
                xmlBlastBean.setBlastOutput(null);

                // exec to xml
                String xml = null;
                try {
                    xml = MountedWublastBlastService.getInstance().robustlyBlastOneDBToString(xmlBlastBean, database);
                } catch (BusException e) {
                    xml = e.getReturnXML();
                    logger.error("bus exception caught", e);
                    xmlBlastBean.setErrorString("Some hits may not have been reported due to a system error.  You may wish to resubmit this job.");
                }

                blastOutput = (BlastOutput) u.unmarshal(new ByteArrayInputStream(xml.getBytes()));

                xmlBlastBean.setBlastOutput(blastOutput);
            }
            catch (Exception bde) {
                String errorString = "Failed to blast for ticket:" + xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean + "\n";
                logger.error(errorString, bde);
            }
        } finally {
            finishBlast();
        }
    }

    public void finishBlast() {
        finished = true;
        running = false;
        this.finishTime = new Date();
    }

}
