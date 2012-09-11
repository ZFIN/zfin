package org.zfin.profile.presentation;

import org.zfin.framework.presentation.ProvidesLink;

/**
 */
public class LabPresentation implements ProvidesLink{

    private String name;
    private String zdbID;
    private Integer position;
    private String flag;
    private String order;

    @Override
    public String getLink() {
        return "<a href=/action/profile/view/"+zdbID + ">"+name +"</a>" ;
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink() ;
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink() ;
    }

    public String getName() {
        return name;
    }

    public String getZdbID() {
        return zdbID;
    }

    public String getFlag() {
        return flag;
    }

    public String getOrder() {
        return order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
