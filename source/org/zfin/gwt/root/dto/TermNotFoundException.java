package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 */
public class TermNotFoundException extends Exception implements IsSerializable {

    private String term;
    private String type;

    public TermNotFoundException() { }

    public TermNotFoundException(String message) {
        super(message);
    }


    public TermNotFoundException(String term, String type) {
        super();
        this.term = term;
        this.type = type;
    }

    public TermNotFoundException(String structure, OntologyDTO ontology) {
        super("Sub-Structure [" + structure + "] not found in " + ontology.getDisplayName() + ".");
    }

    public String getTerm() {
        return term;
    }

    public String getType() {
        return type;
    }
}
