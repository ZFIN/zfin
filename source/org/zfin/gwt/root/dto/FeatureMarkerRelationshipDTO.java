package org.zfin.gwt.root.dto;

/**
 */
public class FeatureMarkerRelationshipDTO extends RelatedEntityDTO{

    private String zdbID ;
    private FeatureDTO featureDTO ;
    private MarkerDTO markerDTO ;
    private String relationshipType ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public FeatureDTO getFeatureDTO() {
        return featureDTO;
    }

    public void setFeatureDTO(FeatureDTO featureDTO) {
        this.featureDTO = featureDTO;
    }

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }

    public void setMarkerDTO(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public boolean equals(Object obj) {

        // compare primary key if they both exist
        if(obj instanceof FeatureMarkerRelationshipDTO){
            FeatureMarkerRelationshipDTO otherDTO = (FeatureMarkerRelationshipDTO) obj ;
            if(otherDTO.getZdbID()!=null && zdbID !=null){
                return otherDTO.getZdbID().equals(zdbID) ;
            }
            else
            if(otherDTO.getRelationshipType()!=null
                    && otherDTO.getFeatureDTO()!=null && otherDTO.getFeatureDTO().getName()!=null
                    && otherDTO.getMarkerDTO()!=null && otherDTO.getMarkerDTO().getName()!=null
                    && featureDTO!=null && featureDTO.getName()!=null
                    && markerDTO!=null && markerDTO.getName()!=null
                    ){
                return (
                        otherDTO.getFeatureDTO().getName().equals(featureDTO.getName())
                                &&
                                otherDTO.getRelationshipType().equals(relationshipType)
                                &&
                                otherDTO.getMarkerDTO().getName().equals(markerDTO.getName())
                );
            }
        }

        return false ;

    }
}
