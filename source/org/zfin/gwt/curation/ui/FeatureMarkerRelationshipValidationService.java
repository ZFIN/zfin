package org.zfin.gwt.curation.ui;

import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.ui.ValidationException;

import java.util.*;

/**
 * Service that validates FeatureMarkerRelationship entries.
 */
public class FeatureMarkerRelationshipValidationService {

    public static boolean validateFeatureMarkerRelationshipToAdd(FeatureMarkerRelationshipDTO newFeatureMarkerRelationshipDTO,List<FeatureMarkerRelationshipDTO> existingFeatureMarkerRelationshipDTOs)
            throws ValidationException{

        List<FeatureMarkerRelationshipDTO> relationshipsForFeatureDTOList = new ArrayList<FeatureMarkerRelationshipDTO>();

        for(FeatureMarkerRelationshipDTO compareDto : existingFeatureMarkerRelationshipDTOs){
            if(compareDto.equals(newFeatureMarkerRelationshipDTO)){
                throw new ValidationException("Relationship already exists.") ;
            }

            if(compareDto.getFeatureDTO().getName().equals(newFeatureMarkerRelationshipDTO.getFeatureDTO().getName())){
                relationshipsForFeatureDTOList.add(compareDto) ;
            }
        }
        FeatureTypeEnum featureTypeEnum = newFeatureMarkerRelationshipDTO.getFeatureDTO().getFeatureType() ;
        switch(featureTypeEnum){
            case POINT_MUTATION:
            case DELETION:
            case INSERTION:
            case UNSPECIFIED:
                return validateMaxIsAlleleOfRelationships(newFeatureMarkerRelationshipDTO,existingFeatureMarkerRelationshipDTOs,1) ;
            case TRANSLOC:
                return
                        validateMaxIsAlleleOfRelationships(newFeatureMarkerRelationshipDTO ,existingFeatureMarkerRelationshipDTOs,4)
                                &&
                                validateExclusiveMarkerRelationshipTypes(newFeatureMarkerRelationshipDTO,relationshipsForFeatureDTOList
                                        , FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF
                                        , FeatureMarkerRelationshipTypeEnum.MARKERS_MOVED
                                ) ;
            case DEFICIENCY:
                return
                        validateMaxIsAlleleOfRelationships(newFeatureMarkerRelationshipDTO,existingFeatureMarkerRelationshipDTOs,2)
                                &&
                                validateExclusiveMarkerRelationshipTypes(newFeatureMarkerRelationshipDTO,relationshipsForFeatureDTOList
                                        , FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF
                                        , FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING
                                        , FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT
                                ) ;
            case INVERSION:
                return validateExclusiveMarkerRelationshipTypes(newFeatureMarkerRelationshipDTO,relationshipsForFeatureDTOList
                        , FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF
                        , FeatureMarkerRelationshipTypeEnum.MARKERS_MOVED)
                 &&
                validateMaxIsAlleleOfRelationships(newFeatureMarkerRelationshipDTO,existingFeatureMarkerRelationshipDTOs,2) ;
            default:
                return true ;

        }

    }

    /**
     * For a given feature type, we need to see if all of the same feature markers have the same relationship type.
     * @param relationshipsForMarkerDTOList
     * @param featureMarkerRelationshipTypeEnums
     * @throws ValidationException
     */
    private static boolean validateExclusiveMarkerRelationshipTypes(FeatureMarkerRelationshipDTO newFeatureMarkerRelationshipDTO
            ,List<FeatureMarkerRelationshipDTO> relationshipsForMarkerDTOList
            , FeatureMarkerRelationshipTypeEnum... featureMarkerRelationshipTypeEnums )  throws  ValidationException{

        // marker, relationships types
        Map<String,Set<String>> markerTypeDTOs = generateFeatureMarkerTypes(relationshipsForMarkerDTOList) ;

        // if my type is an exclusive type
        String newType = newFeatureMarkerRelationshipDTO.getRelationshipType() ;
        boolean isExclusiveType = false ;
        for(FeatureMarkerRelationshipTypeEnum featureMarkerRelationshipTypeEnum: featureMarkerRelationshipTypeEnums){
            if(featureMarkerRelationshipTypeEnum.toString().equals(newType)){
                isExclusiveType = true ;
            }
        }
        // if it is not an exclusive type, then there is nothing to check
        if(false==isExclusiveType){
            return true ;
        }

        // if any of the other types is an exclusive type that is not this type, then throw exception
        Set<String> existingRelationshipTypes = markerTypeDTOs.get(newFeatureMarkerRelationshipDTO.getMarkerDTO().getName()) ;
        if(existingRelationshipTypes!=null && existingRelationshipTypes.size()>0){
            for(String existingType : existingRelationshipTypes){
                if(false==existingType.equals(newType)){
                    throw new ValidationException("Can not have relationships ot the same marker of the following types together: "
                            + FeatureMarkerRelationshipTypeEnum.dumpValues()) ;
                }
            }
        }
        return true ;
    }


    private static Map<String, Set<String>> generateFeatureMarkerTypes(List<FeatureMarkerRelationshipDTO> relationshipsForMarkerDTOList) {
        Map<String,Set<String>> markerTypeDTOs = new HashMap<String,Set<String>>();
        for(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO: relationshipsForMarkerDTOList){
            String markerName = featureMarkerRelationshipDTO.getMarkerDTO().getName() ;
            String currentRelationshipType = featureMarkerRelationshipDTO.getRelationshipType();
            if(markerTypeDTOs.containsKey(markerName)){
                Set<String> relationshipTypesForMarker = markerTypeDTOs.get(markerName) ;
                String existingRelationshipType = featureMarkerRelationshipDTO.getRelationshipType() ;
                relationshipTypesForMarker.add(existingRelationshipType) ;
                // if it does not contain the relationship
                // and relationshiptype contains one of the other relationship types then throw an exception
                // else add relationshp
            }
            else{
                Set<String> types = new HashSet<String>() ;
                types.add(currentRelationshipType) ;
                markerTypeDTOs.put(markerName, types) ;
            }
        }


        return  markerTypeDTOs ;
    }

    protected static boolean validateMaxIsAlleleOfRelationships(FeatureMarkerRelationshipDTO newFeatureMarkerRelationship,
                                                              List<FeatureMarkerRelationshipDTO> relationshipsForMarkerDTOList,
                                                              int maxIsAlleleOfRelationships) throws  ValidationException{
        if(countFeatureMarkerRelationshipsForType(newFeatureMarkerRelationship,relationshipsForMarkerDTOList)>= maxIsAlleleOfRelationships){
            throw new ValidationException("Features of type "+ newFeatureMarkerRelationship.getFeatureDTO().getFeatureType().toString() +
                    " may only have up to "+maxIsAlleleOfRelationships +" '"+ newFeatureMarkerRelationship.getRelationshipType()+"' relationships.") ;
        }
        return true ;
    }

    protected static int countFeatureMarkerRelationshipsForType(FeatureMarkerRelationshipDTO newFeatureMarkerRelationship,List<FeatureMarkerRelationshipDTO> relationshipsForMarkerDTOList) {
        int count = 0 ;
        for(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO: relationshipsForMarkerDTOList){
            if(featureMarkerRelationshipDTO.getRelationshipType().equals(newFeatureMarkerRelationship.getRelationshipType())
                    &&
                    featureMarkerRelationshipDTO.getFeatureDTO().getName().equals(newFeatureMarkerRelationship.getFeatureDTO().getName())
                    ){
                ++count;
            }
        }
        return count;
    }


}
