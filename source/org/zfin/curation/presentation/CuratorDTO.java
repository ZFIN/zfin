package org.zfin.curation.presentation;

import org.apache.commons.lang3.ObjectUtils;

public class CuratorDTO implements Comparable<CuratorDTO> {

    private String zdbID;
    private String name;
    private String imageURL;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int compareTo(CuratorDTO o) {
        return ObjectUtils.compare(name, o.getName());
    }
}
