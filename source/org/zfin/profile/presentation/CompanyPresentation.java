package org.zfin.profile.presentation;

import org.zfin.framework.presentation.ProvidesLink;

/**
 */
public class CompanyPresentation implements ProvidesLink {

    private String zdbID;
    private String name;
    private Integer position;
    private String order; // ?
    private Boolean showPosition = true;

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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Boolean getShowPosition() {
        return showPosition;
    }

    public void setShowPosition(Boolean showPosition) {
        this.showPosition = showPosition;
    }

    @Override
    public String getLink() {
        String link = "<a href=/action/profile/view/" + zdbID + ">" + name + "</a> ";
        if (showPosition) {
            link += " (<font size=-1> " + position + " </font>)";
        }
        return link;
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }
}
