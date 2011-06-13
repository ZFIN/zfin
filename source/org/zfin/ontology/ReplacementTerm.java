package org.zfin.ontology;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the Term interface.
 */
public class ReplacementTerm  {

    private long id;
    private GenericTerm obsoletedTerm;
    private GenericTerm replacementTerm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GenericTerm getObsoletedTerm() {
        return obsoletedTerm;
    }

    public void setObsoletedTerm(GenericTerm obsoletedTerm) {
        this.obsoletedTerm = obsoletedTerm;
    }

    public GenericTerm getReplacementTerm() {
        return replacementTerm;
    }

    public void setReplacementTerm(GenericTerm replacementTerm) {
        this.replacementTerm = replacementTerm;
    }
}
