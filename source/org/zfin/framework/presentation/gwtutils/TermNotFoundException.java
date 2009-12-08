package org.zfin.framework.presentation.gwtutils;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.presentation.client.Ontology;

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