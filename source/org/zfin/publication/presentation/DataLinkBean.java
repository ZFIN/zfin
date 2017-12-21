package org.zfin.publication.presentation;

public class DataLinkBean {

    private String path;
    private String label;
    private Long count;

    public DataLinkBean(String path, String label) {
        this.path = path;
        this.label = label;
    }

    public DataLinkBean(String path, String label, Long count) {
        this.path = path;
        this.label = label;
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
