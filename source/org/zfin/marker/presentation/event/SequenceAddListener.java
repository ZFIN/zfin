package org.zfin.marker.presentation.event;

/**
 */
public interface SequenceAddListener {
    void add(SequenceAddEvent sequenceAddEvent) ;
    void cancel(SequenceAddEvent sequenceAddEvent) ;
    void start(SequenceAddEvent sequenceAddEvent) ;
}
