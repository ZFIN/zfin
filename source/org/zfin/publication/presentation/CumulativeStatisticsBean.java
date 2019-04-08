package org.zfin.publication.presentation;

public class CumulativeStatisticsBean {

    private Object category;
    private Number average;
    private Number standardDeviation;
    private Number minimum;
    private Number maximum;

    public Object getCategory() {
        return category;
    }

    public void setCategory(Object category) {
        this.category = category;
    }

    public Number getAverage() {
        return average;
    }

    public void setAverage(Number average) {
        this.average = average;
    }

    public Number getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Number standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }
}
