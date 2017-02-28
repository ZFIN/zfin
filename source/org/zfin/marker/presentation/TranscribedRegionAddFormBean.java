package org.zfin.marker.presentation;

import java.util.Map;

public class TranscribedRegionAddFormBean {

    private String type;
    private String publicationId;
    private String name;
    private String abbreviation;
    private String alias;
    private String publicNote;
    private String curatorNote;
    private Map<String, String> allTypes;

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPublicNote() {
        return publicNote;
    }

    public void setPublicNote(String publicNote) {
        this.publicNote = publicNote;
    }

    public String getCuratorNote() {
        return curatorNote;
    }

    public void setCuratorNote(String curatorNote) {
        this.curatorNote = curatorNote;
    }

    public Map<String, String> getAllTypes() {
        return allTypes;
    }

    public void setAllTypes(Map<String, String> allTypes) {
        this.allTypes = allTypes;
    }
}
