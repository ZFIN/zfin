package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.Ontology;

/**
 * Exception for structure duplicates.
 */
public class TermNotFoundException extends Exception implements IsSerializable{

    public TermNotFoundException() {
    }

    public TermNotFoundException(String message) {
        super(message);
    }

    public TermNotFoundException(String structure, Ontology ontology) {
        super("Sub-Structure [" + structure + "] not found in " + ontology.getDisplayName() + ".");
    }
}