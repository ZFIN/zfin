package org.zfin.construct.presentation;


import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructComponent;
import org.zfin.marker.presentation.PreviousNameLight;

import java.util.List;
import java.util.Set;

public class ConstructUpdateBean {






    public static final String NEW_CONSTRUCT_COMMENT = "constructComment";
    private String constructPublicationZdbID;
    private String constructComment;
    private Set<ConstructCuration> constructs;
    private List<ConstructComponent> constructComponent;
    private String constructDisplayName;
    public List<PreviousNameLight> getConstructAlias() {
        return constructAlias;
    }

    public void setConstructAlias(List<PreviousNameLight> constructAlias) {
        this.constructAlias = constructAlias;
    }

    private List<PreviousNameLight> constructAlias;

    public String getConstructDisplayName() {
        return constructDisplayName;
    }

    public void setConstructDisplayName(String constructDisplayName) {
        this.constructDisplayName = constructDisplayName;
    }

    public List<ConstructComponent> getConstructComponent() {
        return constructComponent;
    }

    public void setConstructComponent(List<ConstructComponent> constructComponent) {
        this.constructComponent = constructComponent;
    }

    public Set<ConstructCuration> getConstructs() {
        return constructs;
    }


    public void setConstructs(Set<ConstructCuration> constructs) {
        this.constructs = constructs;
    }

    public String getConstructEdit() {
        return constructEdit;
    }

    public void setConstructEdit(String constructEdit) {
        this.constructEdit = constructEdit;
    }

    public String getConstructName() {
        return constructName;
    }
    public String constructEdit;


    public List<ConstructCuration> getConstructsInPub() {
        return constructsInPub;
    }

    public void setConstructsInPub(List<ConstructCuration> constructsInPub) {
        this.constructsInPub = constructsInPub;
    }

    public void setConstructName(String constructName) {
        this.constructName = constructName;
    }
    private List<ConstructCuration> constructsInPub;

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



