package org.zfin.curation.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.curation.dto.ExpressedTermDTO;

/**
 * Exception for structure dup[licates.
 */
public class TermNotFoundException extends Exception implements IsSerializable{

    public TermNotFoundException() {
    }

    public TermNotFoundException(String message) {
        super(message);
    }

    public TermNotFoundException(String structure, String ontology) {
        super("Sub-Structure [" + structure + "] not found in " + ontology + ".");
    }
}