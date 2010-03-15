package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Date;


/**
 * Class DoBlastThread provides a thread for the blast process to run it
 * so that it doesn't hang the calling process but is allowed to finish.
 */
public abstract class AbstractQueryThread implements BlastQueryJob {

    private final static Logger logger = Logger.getLogger(AbstractQueryThread.class);

    protected boolean finished = false;
    protected boolean running = false;
    protected Date queueTime;
    protected Date startTime;
    protected Date finishTime;

    protected XMLBlastBean xmlBlastBean;
    private JAXBContext JAXBBlastContext;
    private BlastStatistics blastStatistics = BlastStatistics.getInstance();

    public AbstractQueryThread(XMLBlastBean xmlBlastBean) {
        this.xmlBlastBean = xmlBlastBean;
        this.queueTime = new Date();
        try {
            JAXBBlastContext = JAXBContext.newInstance("org.zfin.sequence.blast.results");
        } catch (JAXBException e) {
            logger.error("Failed to instantiate a new context", e);
        }
    }


    public void startBlast() {
        running = true;
        this.startTime = new Date();
    }

    public void finishBlast() {
        finished = true;
        running = false;
        this.finishTime = new Date();
        blastStatistics.recordStatistics(this);
    }

    public Date getQueueTime() {
        return queueTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }


    public int getNumberThreads() {
        return 1;
    }

    public XMLBlastBean getXmlBlastBean() {
        return xmlBlastBean;
    }

    public String getTicket() {
        if (xmlBlastBean == null) {
            return null;
        }
        return xmlBlastBean.getTicketNumber();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }

    public JAXBContext getJAXBBlastContext() {
        return JAXBBlastContext;
    }
}