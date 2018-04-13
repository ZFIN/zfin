package org.zfin.infrastructure.presentation;


import org.zfin.infrastructure.AnnualStats;

import java.io.Serializable;

public class AnnualStatsDisplay implements Serializable, Comparable<AnnualStatsDisplay> {
    AnnualStats annualStats;
    private String category;
    private int order;
    private String count;

    public AnnualStats getAnnualStats() {
        return annualStats;
    }

    public void setAnnualStats(AnnualStats annualStats) {
        this.annualStats = annualStats;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int compareTo(AnnualStatsDisplay anotherStatsDisplay) {
        int anotherOrder = anotherStatsDisplay.getOrder();
        if(order == anotherOrder) {
            return annualStats.compareTo(anotherStatsDisplay.getAnnualStats());
        }
        return order - anotherOrder;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
