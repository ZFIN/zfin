package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.ExpressedTermDTO;

/**
 * Exception for structure dup[licates.
 */
public class PileStructureExistsException extends Exception implements IsSerializable{

    public PileStructureExistsException() {
    }

    public PileStructureExistsException(ExpressedTermDTO structure) {
        super("Structure [" + structure.getDisplayName() + "] already on the pile. ");
    }
}