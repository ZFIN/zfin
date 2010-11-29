package org.zfin.marker.webservice;

import org.zfin.anatomy.AnatomyItem;

/**
*/
public class Anatomy {

    private String zdbId ;
    private String oboId ;
    private String name ;
    private String definition ;
    private String description;
    private String stageStart ;
    private String stageEnd ;

    // no-arg constructor required for marshalling
    public Anatomy(){}

    public Anatomy(AnatomyItem anatomyItem){
        zdbId = anatomyItem.getZdbID() ;
        oboId = anatomyItem.getOboID() ;
        name = anatomyItem.getName();
        definition = anatomyItem.getDefinition();
        description = anatomyItem.getDescription();
        stageStart = anatomyItem.getStart().abbreviation() ;
        stageEnd = anatomyItem.getEnd().abbreviation() ;
    }


    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getOboId() {
        return oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = oboId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStageStart() {
        return stageStart;
    }

    public void setStageStart(String stageStart) {
        this.stageStart = stageStart;
    }

    public String getStageEnd() {
        return stageEnd;
    }

    public void setStageEnd(String stageEnd) {
        this.stageEnd = stageEnd;
    }
}
