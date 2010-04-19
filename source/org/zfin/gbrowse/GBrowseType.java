package org.zfin.gbrowse;

public class GBrowseType {

    public static final String TRANSCRIPT = "transcript";
    public static final String MRNA = "mRNA";

    private Integer id;
    private String tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
