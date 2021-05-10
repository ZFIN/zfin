package org.zfin.expression.service;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class MicroarrayWebServiceBean {

    Set<String> removedZdbIDs = new HashSet<String>();
    Set<String> addZdbIds = new HashSet<String>();

    public Set<String> getRemovedZdbIDs() {
        return removedZdbIDs;
    }

    public void setRemovedZdbIDs(Set<String> removedZdbIDs) {
        this.removedZdbIDs = removedZdbIDs;
    }

    public Set<String> getAddZdbIds() {
        return addZdbIds;
    }

    public void setAddZdbIds(Set<String> addZdbIds) {
        this.addZdbIds = addZdbIds;
    }
}
