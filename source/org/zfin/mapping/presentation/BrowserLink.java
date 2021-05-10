package org.zfin.mapping.presentation;

public class BrowserLink implements Comparable<BrowserLink> {
    private String url;
    private String name;
    private int order;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(BrowserLink o) {
        return o.getOrder() - this.order;
    }
}
