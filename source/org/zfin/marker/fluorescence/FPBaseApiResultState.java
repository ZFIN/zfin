package org.zfin.marker.fluorescence;

public record FPBaseApiResultState(
        String slug,
        String name,
        Integer ex_max,
        Integer em_max,
        Integer ext_coeff,
        Double qy,
        Double pka,
        Double maturation,
        Double lifetime,
        Double brightness
) {}
