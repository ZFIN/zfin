package org.zfin.marker.presentation;

import org.zfin.framework.presentation.LookupEntry;

import java.io.Serializable;

/**
 */
public class SequenceTargetingReagentLookupEntry extends LookupEntry implements Serializable, Comparable<SequenceTargetingReagentLookupEntry>  {
    //todo: this class should go away ?   maybe move the comparator up...

    public int compareTo(SequenceTargetingReagentLookupEntry anotherSequenceTargetingReagentLookupEntry) {
        return label.compareTo(anotherSequenceTargetingReagentLookupEntry.getLabel());
    }
}
