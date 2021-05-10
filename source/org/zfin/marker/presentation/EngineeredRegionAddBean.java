package org.zfin.marker.presentation;


public class EngineeredRegionAddBean {

  public static final String NEW_REGION_NAME = "regionName";
  public static final String REGION_PUBLICATION_ZDB_ID = "regionPublicationZdbID";
  public static final String NEW_REGION_COMMENT = "regionComment";
  public static final String NEW_REGION_ALIAS = "regionAlias";
  public static final String NEW_REGION_CURNOTE = "regionCuratorNote";

  private String regionPublicationZdbID;
  private String regionName;
  private String regionComment;
  private String regionAlias;
  private String regionCuratorNote;


  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getRegionPublicationZdbID() {
    return regionPublicationZdbID;
  }

  public void setRegionPublicationZdbID(String regionPublicationZdbID) {
    this.regionPublicationZdbID = regionPublicationZdbID;
  }

    public String getRegionComment() {
        return regionComment;
    }

    public void setRegionComment(String regionComment) {
        this.regionComment = regionComment;
    }

    public String getRegionAlias() {
        return regionAlias;
    }

    public void setRegionAlias(String regionAlias) {
        this.regionAlias = regionAlias;
    }

    public String getRegionCuratorNote() {
        return regionCuratorNote;
    }

    public void setRegionCuratorNote(String regionCuratorNote) {
        this.regionCuratorNote = regionCuratorNote;
    }
}







