package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.Date;

/**
 */
public interface BlastQueryJob extends Runnable{
    XMLBlastBean getXmlBlastBean();

    String getTicket();

    boolean isFinished();

    boolean isRunning();

    void startBlast();

    void finishBlast();

    int getNumberThreads() ;

    public Date getQueueTime() ;

    public Date getStartTime() ;

    public Date getFinishTime() ;
}
