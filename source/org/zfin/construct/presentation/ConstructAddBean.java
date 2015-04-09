package org.zfin.construct.presentation;


import org.apache.commons.lang.StringUtils;
import org.zfin.marker.Marker;

public class ConstructAddBean {






    public static final String NEW_CONSTRUCT_COMMENT = "constructComment";
    private String constructPublicationZdbID;
    private String constructComment;
    private String constructDisplayName;


    public String getConstructDisplayName() {
        return constructDisplayName;
    }

    public void setConstructDisplayName(String constructDisplayName) {
        this.constructDisplayName = constructDisplayName;
    }

    public String getConstructName() {
        return constructName;
    }

    public void setConstructName(String constructName) {
        this.constructName = constructName;
    }

    private String constructName;

    //private String disruptorComment;
    private String constructSynonym;
    public String getConstructComment() {
        return constructComment;
    }

    public void setConstructComment(String constructComment) {
        this.constructComment = constructComment;
    }
















    public String getConstructStoredName() {
        return constructStoredName;
    }

    public void setConstructStoredName(String constructStoredName) {
        this.constructStoredName = constructStoredName;
    }

    public void setConstructPromoter(String constructPromoter) {
        this.constructPromoter = constructPromoter;
    }

    public void setConstructCoding(String constructCoding) {

        this.constructCoding = constructCoding;
    }




    private String constructStoredName;

    public String getConstructCoding() {
        return constructCoding;
    }

    public String getConstructPromoter() {
        return constructPromoter;
    }

    private String constructCoding;
    private String constructPromoter;
   private String name;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getConstructSynonym() {
        return constructSynonym;
    }

    public void setConstructSynonym(String constructSynonym) {
        this.constructSynonym = constructSynonym;
    }

    public String getConstructPublicationZdbID() {
        return constructPublicationZdbID;
    }

    public void setConstructPublicationZdbID(String constructPublicationZdbID) {
        this.constructPublicationZdbID = constructPublicationZdbID;
    }





}



