package org.zfin.profile.presentation;

import org.zfin.framework.presentation.ProvidesLink;

import java.io.Serializable;

/**
 */
public class PersonMemberPresentation implements ProvidesLink, Serializable {
    private String name;
    private String zdbID;
    private Integer position;
    private String positionString ;
    private Integer order;
    private String organizationZdbID;
    private String address ;

//    private final static String OTHER_POSITION = "Other";
    private final static Integer COMPANY_OTHER_POSITION = 4 ;
    private final static Integer LAB_OTHER_POSITION = 6 ;

    // 5 in company, 10 in lab

    private ACTION_ENUM function = ACTION_ENUM.DO_NOTHING;

    public static enum ACTION_ENUM {
        ADD_MEMBER,
        REMOVE_MEMBER,
        ALL_ADDRESS_TO_ORGANIZATION,
        SET_ADDRESS_TO_EXISTING,
        DO_NOTHING,
        ;
    }

    @Override
    public String getLink() {
        String link = "<a href='/action/profile/view/" + zdbID + "'>";
//        if (order == null || order < 3) {
//            link += "<b>" + name + "</b>";
//        } else {
            link += name;
//        }
        link += "</a>";

        if (position != null && false == isOtherPosition(position)) {
            link += " <font size=-1> " + positionString + "</font>";
        }

        return link;
    }

    private boolean isOtherPosition(Integer position) {
        if(organizationZdbID.startsWith("ZDB-LAB")){
            return position.equals(LAB_OTHER_POSITION);
        }
        else
        if(organizationZdbID.startsWith("ZDB-COMPANY")){
            return position.equals(COMPANY_OTHER_POSITION);
        }
        return false;
    }


    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }


    public ACTION_ENUM getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public String getZdbID() {
        return zdbID;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setPersonZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getOrganizationZdbID() {
        return organizationZdbID;
    }

    public void setOrganizationZdbID(String organizationZdbID) {
        this.organizationZdbID = organizationZdbID;
    }

    public void setAddFunction() {
        function = ACTION_ENUM.ADD_MEMBER ;
    }

    public String getPositionString() {
        return positionString;
    }

    public void setPositionString(String positionString) {
        this.positionString = positionString;
    }

    public void setAllAddressToOrganization() {
        function = ACTION_ENUM.ALL_ADDRESS_TO_ORGANIZATION ;
    }

    public void setAddressToExisting(String address, String personZdbId) {
        function = ACTION_ENUM.SET_ADDRESS_TO_EXISTING;
        this.address = address;
        this.zdbID = personZdbId ;
    }

    public void setRemoveFunction() {
        function = ACTION_ENUM.REMOVE_MEMBER ;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(Long addressID) {
        this.address = address;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PersonMemberPresentation");
        sb.append("{name='").append(name).append('\'');
        sb.append(", zdbID='").append(zdbID).append('\'');
        sb.append(", position=").append(position);
        sb.append(", positionString='").append(positionString).append('\'');
        sb.append(", order=").append(order);
        sb.append(", organizationZdbID='").append(organizationZdbID).append('\'');
        sb.append(", address=").append(address);
        sb.append(", function=").append(function);
        sb.append('}');
        return sb.toString();
    }
}
