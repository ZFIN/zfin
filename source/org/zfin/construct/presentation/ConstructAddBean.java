package org.zfin.construct.presentation;


import org.apache.commons.lang.StringUtils;
import org.zfin.marker.Marker;

public class ConstructAddBean {

    public static final String NEW_CONSTRUCT_COMMENT = "constructComment";
    private String constructPublicationZdbID;
    private String constructComment;
    private String constructDisplayName;




    public String getConstructName() {
        return constructName;
    }

    public void setConstructName(String constructName) {
        this.constructName = constructName;
    }

    private String constructName;

    public String getConstructDisplayName() {
        return constructDisplayName;
    }

    public void setConstructDisplayName(String constructDisplayName) {
        this.constructDisplayName = constructDisplayName;
    }

    //private String disruptorComment;
    private String constructSynonym;

    public static String getNewConstructComment() {
        return NEW_CONSTRUCT_COMMENT;
    }

    public String getConstructStoredName() {
        return constructStoredName;
    }

    public void setConstructStoredName(String constructStoredName) {
        this.constructStoredName = constructStoredName;
    }

    private String constructStoredName;


   private String name;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }





    public String getConstructPublicationZdbID() {
        return constructPublicationZdbID;
    }

    public void setConstructPublicationZdbID(String constructPublicationZdbID) {
        this.constructPublicationZdbID = constructPublicationZdbID;
    }





}



