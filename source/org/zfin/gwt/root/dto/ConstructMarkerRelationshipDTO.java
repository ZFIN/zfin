package org.zfin.gwt.root.dto;

/**
 */
public class ConstructMarkerRelationshipDTO extends RelatedEntityDTO{

    private String zdbID ;

    private MarkerDTO markerDTO ;
    private String relationshipType ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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
        if(obj instanceof ConstructMarkerRelationshipDTO){
            ConstructMarkerRelationshipDTO otherDTO = (ConstructMarkerRelationshipDTO) obj ;
            if(otherDTO.getZdbID()!=null && zdbID !=null){
                return otherDTO.getZdbID().equals(zdbID) ;
            }
            else
            if(otherDTO.getRelationshipType()!=null

                    && otherDTO.getMarkerDTO()!=null && otherDTO.getMarkerDTO().getName()!=null

                    && markerDTO!=null && markerDTO.getName()!=null
                    ){
                return (

                                otherDTO.getRelationshipType().equals(relationshipType)
                                &&
                                otherDTO.getMarkerDTO().getName().equals(markerDTO.getName())
                );
            }
        }

        return false ;

    }
}
