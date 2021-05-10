package org.zfin.gwt.marker.event;

/**
 */
public interface SequenceAddListener {
    void add(SequenceAddEvent sequenceAddEvent) ;
    void cancel(SequenceAddEvent sequenceAddEvent) ;
    void start(SequenceAddEvent sequenceAddEvent) ;
}
