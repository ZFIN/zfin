package org.zfin.wiki.presentation;

/**
 */
public class WikiBean {

    private String zdbID;
    private String name ;
    private String url;

    public boolean isAvaiable(){
        return name!=null && url!=null ; 
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
