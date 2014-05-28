package org.zfin.mapping;

/**
 * linkage info for singleton records, i.e. old linkage members that were never paired up.
 */
public class SingletonLinkage {

    private long id;
    private String zdbID;
    private Linkage linkage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Linkage getLinkage() {
        return linkage;
    }

    public void setLinkage(Linkage linkage) {
        this.linkage = linkage;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
