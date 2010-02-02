package org.zfin.gwt.root.dto;

import java.util.List;
import java.util.Set;

/**
 * Represents Clone object
 */
public class AntibodyDTO extends MarkerDTO {

    // clone table data
    String hostOrganism ;
    String immunogenOrganism ;
    String heavyChain ;
    String lightChain ;
    String type ;
    List<NoteDTO> externalNotes ;

    public AntibodyDTO copyFrom(MarkerDTO otherMarkerDTO){
        setName(otherMarkerDTO.getName());
        return this ;
    }

    public AntibodyDTO copyFrom(AntibodyDTO otherCloneDTO){
        setName(otherCloneDTO.getName());
        return this ;
    }

    public String getHostOrganism() {
        return hostOrganism;
    }

    public void setHostOrganism(String hostOrganism) {
        this.hostOrganism = hostOrganism;
    }

    public String getImmunogenOrganism() {
        return immunogenOrganism;
    }

    public void setImmunogenOrganism(String immunogenOrganism) {
        this.immunogenOrganism = immunogenOrganism;
    }

    public String getHeavyChain() {
        return heavyChain;
    }

    public void setHeavyChain(String heavyChain) {
        this.heavyChain = heavyChain;
    }

    public String getLightChain() {
        return lightChain;
    }

    public void setLightChain(String lightChain) {
        this.lightChain = lightChain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NoteDTO> getExternalNotes() {
        return externalNotes;
    }

    public void setExternalNotes(List<NoteDTO> externalNotes) {
        this.externalNotes = externalNotes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AntibodyDTO");
        sb.append("{hostOrganism='").append(hostOrganism).append('\'');
        sb.append(", immunogenOrganism='").append(immunogenOrganism).append('\'');
        sb.append(", heavyChain='").append(heavyChain).append('\'');
        sb.append(", lightChain='").append(lightChain).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", externalNotes='").append(externalNotes).append('\'');
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}