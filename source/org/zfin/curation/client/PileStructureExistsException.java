package org.zfin.curation.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.curation.dto.ExpressedTermDTO;

/**
 * Exception for structure dup[licates.
 */
public class PileStructureExistsException extends Exception implements IsSerializable{

    public PileStructureExistsException() {
    }

    public PileStructureExistsException(ExpressedTermDTO structure) {
        super("Structure [" + structure.getComposedTerm() + "] already on the pile. ");
    }
}