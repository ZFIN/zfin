package org.zfin.gwt.root.dto;

import java.util.List;

/**
 * Represents Clone object
 */
public class CloneDTO extends MarkerDTO {

    // clone table data
    private String probeLibraryName;
    private Integer rating ;
    private String vectorName;
    private String digest ;
    private String polymerase ;
    private Integer insertSize ;
    private String cloneComments ;
    private String pcrAmplification ;
    private String problemType ;
    private String cloningSite ;
    private List<String> problemTypes ;

    public CloneDTO copyFrom(MarkerDTO otherMarkerDTO){
        setName(otherMarkerDTO.getName());
        return this ;
    }

    public CloneDTO deepCopy() {
        CloneDTO cloneDTO = new CloneDTO();
        cloneDTO.supportingSequenceLinks = supportingSequenceLinks;
        cloneDTO.zdbID = zdbID;
        cloneDTO.name = name;
        cloneDTO.abbreviationOrder = abbreviationOrder;
        cloneDTO.markerType = markerType;
        cloneDTO.setLink(link);
        cloneDTO.setPublicationZdbID(publicationZdbID);
        cloneDTO.markerRelationshipType = markerRelationshipType;
        cloneDTO.zdbIDThenAbbrev = zdbIDThenAbbrev;
        return cloneDTO;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getVectorName() {
        return vectorName;
    }

    public void setVectorName(String vectorName) {
        this.vectorName = vectorName;
    }

    public String getProbeLibraryName() {
        return probeLibraryName;
    }

    public void setProbeLibraryName(String probeLibraryName) {
        this.probeLibraryName = probeLibraryName;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getPolymerase() {
        return polymerase;
    }

    public void setPolymerase(String polymerase) {
        this.polymerase = polymerase;
    }

    public Integer getInsertSize() {
        return insertSize;
    }

    public void setInsertSize(Integer insertSize) {
        this.insertSize = insertSize;
    }

    public String getCloneComments() {
        return cloneComments;
    }

    public void setCloneComments(String cloneComments) {
        this.cloneComments = cloneComments;
    }

    public String getPcrAmplification() {
        return pcrAmplification;
    }

    public void setPcrAmplification(String pcrAmplification) {
        this.pcrAmplification = pcrAmplification;
    }

    public String getCloningSite() {
        return cloningSite;
    }

    public void setCloningSite(String cloningSite) {
        this.cloningSite = cloningSite;
    }


    public List<String> getProblemTypes(){
        return problemTypes ;
    }

    public void setProblemTypes(List<String> problemTypes) {
        this.problemTypes = problemTypes;
    }

    public String toString(){
        String returnString = "" ;
        returnString += "zdbID: " + zdbID + "\n" ;
        returnString += "name: " + name + "\n" ;
        returnString += "markerType: " + markerType + "\n" ;
        returnString += "cloningSite: " + cloningSite+ "\n" ;

        return returnString ;
    }
}
