package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.blast.BlastQueryJob;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.SMPBlastQueryThread;
import org.zfin.sequence.blast.SMPNCBIBlastService;

/**
 * Provides a handle to the SMP BlastQueryThread.
 * Doing this also allows custom heuristics, as well.
 */
public class NCBIBlastController extends XMLBlastController {

    @Override
    protected void scheduleBlast(XMLBlastBean blastBean){
        blastBean.setNumChunks(4);
        BlastQueryJob blastSingleTicketQueryThread = new SMPBlastQueryThread(blastBean, SMPNCBIBlastService.getInstance());
        BlastQueryThreadCollection.getInstance().addJobAndStart(blastSingleTicketQueryThread);
    }


}