package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.Collection;

/**
 */
public interface BlastThreadCollection {

    Collection<BlastQueryJob> getQueue() ;

}
