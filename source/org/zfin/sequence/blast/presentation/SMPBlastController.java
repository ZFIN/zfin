package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.blast.BlastQueryJob;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.SMPBlastQueryThread;
import org.zfin.sequence.blast.SMPWublastService;

/**
 * Provides a handle to the SMP BlastQueryThread.
 * Doing this also allows custom heuristics, as well.
 */
public class SMPBlastController extends XMLBlastController {

    @Override
    protected void scheduleBlast(XMLBlastBean blastBean){
        blastBean.setNumChunks(4);
        BlastQueryJob blastSingleTicketQueryThread = new SMPBlastQueryThread(blastBean,SMPWublastService.getInstance());
        BlastQueryThreadCollection.getInstance().addJobAndStart(blastSingleTicketQueryThread);
    }


}