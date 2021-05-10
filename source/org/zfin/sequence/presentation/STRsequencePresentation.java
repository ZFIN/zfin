package org.zfin.sequence.presentation;

import org.zfin.framework.presentation.ProvidesLink;

import java.io.Serializable;

/**
 */
public class STRsequencePresentation implements Serializable {
    private String sequence;
    private String secondSequence;

    public String getSequence() {
        return sequence;
    }

    public String getSecondSequence() {
        return secondSequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setSecondSequence(String secondSequence) {
        this.secondSequence = secondSequence;
    }
}
