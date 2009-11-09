package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

/**
 * A thread for a single blast, that handles a chunk.
 */
public class BlastSliceThread implements BlastQueryRunnable{

    private final Logger logger = Logger.getLogger(BlastSliceThread.class);

    private XMLBlastBean xmlBlastBean;
    private Database database;
    private int slice;

    private boolean finished = false ;
    private boolean running = false ;

    public BlastSliceThread(XMLBlastBean xmlBlastBean, Database database, int slice) {
        this.xmlBlastBean = xmlBlastBean;
        this.database = database;
        this.slice = slice;
    }

    public int getSlice() {
        return slice;
    }

    public XMLBlastBean getXmlBlastBean() {
        return xmlBlastBean;
    }

    public void run() {
        startBlast();
        try {

            // so there would not be any confusion, we set to null initially
            xmlBlastBean.setBlastOutput(null);

            // exec to xml
            String xml = MountedWublastBlastService.getInstance().blastOneDBToString(xmlBlastBean, database);

            // return if no way to process
            if (xml == null) {
                return;
            }

            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            Unmarshaller u = jc.createUnmarshaller();
            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            xmlBlastBean.setBlastOutput(blastOutput);
        }
        catch (Exception bde) {
            String errorString = "Failed to blast for ticket:" + xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean +"\n" ;
            bde.fillInStackTrace();
            logger.error(errorString,bde);
        }
        finishBlast();
    }

    public boolean isFinished() {
        return finished;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRunning() {
        return running ;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startBlast(){
        running = true ;
    }

    public void finishBlast(){
        finished = true ;
        running = false ;
    }

    public int getNumberThreads() {
        return 1;
    }
}
