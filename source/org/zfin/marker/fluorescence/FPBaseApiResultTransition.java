package org.zfin.marker.fluorescence;

public record FPBaseApiResultTransition(
    String from_state,
    String to_state,
    Double trans_wave
) {}
