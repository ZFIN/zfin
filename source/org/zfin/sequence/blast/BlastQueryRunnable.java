package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;

/**
 */
public interface BlastQueryRunnable extends Runnable{
    XMLBlastBean getXmlBlastBean();

    boolean isFinished();

    boolean isRunning();

    void startBlast();

    void finishBlast();

    int getNumberThreads() ;
}
