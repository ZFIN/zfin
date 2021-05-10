package org.zfin.publication.presentation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricsOnDateBean {

    private Object category;
    private Number average;
    private Number standardDeviation;
    private Number minimum;
    private Number maximum;
    private Number oldestAverage;

}
