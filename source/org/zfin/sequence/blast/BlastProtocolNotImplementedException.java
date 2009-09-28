package org.zfin.sequence.blast;

/**
 * This is thrown by methods where the service is not available even though the interface is provided.
 */
public class BlastProtocolNotImplementedException extends RuntimeException{

    public BlastProtocolNotImplementedException(){
        super("Method not supported.") ;
    }
}
