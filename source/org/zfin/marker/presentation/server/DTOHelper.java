package org.zfin.marker.presentation.server;

import org.zfin.marker.presentation.dto.*;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.sequence.*;
import org.zfin.sequence.presentation.DBLinkPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.orthology.Species;
import org.zfin.framework.HibernateUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 */
public class DTOHelper {

    private static Logger logger = Logger.getLogger(DTOHelper.class) ;

    public static Set<RelatedEntityDTO> createAttributesForPublication(String dataZdbID, String name,Set<PublicationAttribution> publications){
        Set<RelatedEntityDTO> relatedEntityDTOs = new HashSet<RelatedEntityDTO>();
        if(publications == null || publications.size()==0){
            RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
            relatedEntityDTO.setDataZdbID(dataZdbID);
            relatedEntityDTO.setName(name) ;
            relatedEntityDTOs.add(relatedEntityDTO) ;
        }
        else{
            for(PublicationAttribution publication: publications){
                RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
                relatedEntityDTO.setName(name) ;
                relatedEntityDTO.setDataZdbID(dataZdbID);                
                relatedEntityDTO.setPublicationZdbID(publication.getPublication().getZdbID());
                relatedEntityDTOs.add(relatedEntityDTO) ;
            }
        }
        return relatedEntityDTOs;
    }

    public static Set<SequenceDTO> createSequenceDTOsForPublications(Sequence sequence,String markerName){
        Set<SequenceDTO> sequenceDTOs = new HashSet<SequenceDTO>();
        Set<PublicationAttribution> publications = sequence.getDbLink().getPublications() ;
        if(publications == null || publications.size()==0){
            sequenceDTOs.add(createSequenceDTOFromSequence(sequence,markerName, null,null)) ;
        }
        else{
            for(PublicationAttribution publication: publications){
                sequenceDTOs.add(createSequenceDTOFromSequence(sequence,markerName, publication.getPublication().getZdbID(),publication.getSourceType().toString()))  ;
            }
        }
        return sequenceDTOs;
    }

    public static SequenceDTO createSequenceDTOFromSequence(Sequence sequence,String markerName,String publicationZdbID,String attributionType){

        SequenceDTO sequenceDTO = new SequenceDTO() ;
        DBLink dbLink = sequence.getDbLink() ;
        sequenceDTO.setDataZdbID(dbLink.getDataZdbID());
        sequenceDTO.setDataName(markerName);
        sequenceDTO.setDbLinkZdbID(dbLink.getZdbID());
        if(dbLink.getLength()==null){
            sequenceDTO.setLength(sequence.getData().length());
        }
        else{
            sequenceDTO.setLength(dbLink.getLength());
        }
        // don't set is editable here
        // don't set is link here
        sequenceDTO.setName(dbLink.getAccessionNumber());
        sequenceDTO.setPublicationZdbID(publicationZdbID);

        sequenceDTO.setSequence(sequence.getData());
        sequenceDTO.setDefLine(sequence.getDefLine().toString());
        sequenceDTO.setAttributionType(attributionType);

        ReferenceDatabase referenceDatabase = dbLink.getReferenceDatabase() ;
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO() ;
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        sequenceDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);

        return sequenceDTO ;
    }

    public static <U extends HasLink> Set<U> createLinksForPublication(U linkableData,Set<PublicationAttribution> publications){
        Set<U> attributeDTOs = new HashSet<U>();
        if(publications == null || publications.size()==0){
            attributeDTOs.add(linkableData) ;
        }
        else{
            for(PublicationAttribution publicationAttribution: publications){
                U newLink = linkableData.<U>deepCopy();
                newLink.setPublicationZdbID(publicationAttribution.getPublication().getZdbID());
                attributeDTOs.add(newLink) ;
            }
        }
        return attributeDTOs;
    }


    public static List<DBLinkDTO> createDBLinkDTOsFromTranscriptDBLink(TranscriptDBLink transcriptDBLink){
        return createDBLinkDTOsFromDBLink(transcriptDBLink,transcriptDBLink.getTranscript().getZdbID(),transcriptDBLink.getTranscript().getAbbreviation() );
    }

    public static List<DBLinkDTO> createDBLinkDTOsFromMarkerDBLink(MarkerDBLink markerDBLink){
        return createDBLinkDTOsFromDBLink(markerDBLink,markerDBLink.getMarker().getZdbID(),markerDBLink.getMarker().getAbbreviation() );
    }

    public static ReferenceDatabase getReferenceDatabase(ReferenceDatabaseDTO referenceDatabaseDTO){
        if(referenceDatabaseDTO==null ) return null ;

        ReferenceDatabase referenceDatabase ;
        if(referenceDatabaseDTO.getZdbID()==null
                && referenceDatabaseDTO.getName()!=null){
            referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.getType(referenceDatabaseDTO.getName()),
                    ForeignDBDataType.DataType.getType(referenceDatabaseDTO.getType()),
                    ForeignDBDataType.SuperType.getType(referenceDatabaseDTO.getSuperType()),
                    Species.ZEBRAFISH) ;
        }
        else{
            referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, referenceDatabaseDTO.getZdbID()) ;
        }
        return referenceDatabase ;
    }

    public static List<DBLinkDTO> createDBLinkDTOsFromDBLink(List<DBLink> dbLinks,String markerZdbID,String markerName){
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>() ;

        for(DBLink dbLink: dbLinks){
            dbLinkDTOs.addAll(createDBLinkDTOsFromDBLink(dbLink,markerZdbID,markerName)) ;
        }
        return dbLinkDTOs;
    }

    public static Transcript getTranscriptFromDBLinkDTO(DBLinkDTO dbLinkDTO){
        if(dbLinkDTO.getDataZdbID()!=null){
            return (Transcript) HibernateUtil.currentSession().get(Transcript.class,dbLinkDTO.getDataZdbID());
        }
        else
        if(dbLinkDTO.getDataName()!=null){
            return (Transcript) RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName()) ;
        }
        else{
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null ;
        }
    }

    public static Marker getMarkerFromDBLinkDTO(DBLinkDTO dbLinkDTO){
        if(dbLinkDTO.getDataZdbID()!=null){
            return (Marker) HibernateUtil.currentSession().get(Marker.class,dbLinkDTO.getDataZdbID());
        }
        else
        if(dbLinkDTO.getDataName()!=null){
            return RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(dbLinkDTO.getDataName()) ;
        }
        else{
            logger.error("Unable to get marker from dblinkDTO: " + dbLinkDTO);
            return null ;
        }
    }

    public static DBLinkDTO createDBLinkDTOFromDBLinkForPub(DBLink dbLink,String markerZdbID,String markerName){
        return createDBLinkDTOFromDBLinkForPub(dbLink,markerZdbID,markerName,null) ;
    }

    public static DBLinkDTO createDBLinkDTOFromDBLinkForPub(DBLink dbLink,String markerZdbID,String markerName,String publicationZdbID){
        DBLinkDTO dbLinkDTO = new DBLinkDTO() ;
        dbLinkDTO.setDbLinkZdbID(dbLink.getZdbID());
        dbLinkDTO.setDataZdbID(markerZdbID);
        dbLinkDTO.setDataName(markerName);
        dbLinkDTO.setName(dbLink.getAccessionNumber()) ;
        dbLinkDTO.setLength(dbLink.getLength());

        dbLinkDTO.setLink(DBLinkPresentation.getLink(dbLink)) ;


        ReferenceDatabase referenceDatabase = dbLink.getReferenceDatabase() ;
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO() ;
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
        dbLinkDTO.setPublicationZdbID(publicationZdbID);

        return dbLinkDTO ;
    }

    /**
     * Craete a list of DBLinkDTO, one for each reference.
     * @param dbLink Template DBLink.
     * @param markerZdbID MarkerZdbID to mirror.
     * @param markerName Marker name.
     * @return A list of DBLinkDTOs, one for each reference.
     */
    public static List<DBLinkDTO> createDBLinkDTOsFromDBLink(DBLink dbLink,String markerZdbID,String markerName){
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>() ;

        Set<PublicationAttribution> publicationAttributions = dbLink.getPublications();
        if(publicationAttributions == null || publicationAttributions.size()==0){
            dbLinkDTOs.add(createDBLinkDTOFromDBLinkForPub(dbLink,markerZdbID,markerName)) ;
        }
        else{
            for(PublicationAttribution publicationAttribution : dbLink.getPublications()){
                dbLinkDTOs.add(createDBLinkDTOFromDBLinkForPub(dbLink,markerZdbID,markerName,publicationAttribution.getPublication().getZdbID()) );
            }
        }
        return dbLinkDTOs;
    }

    public static List<ReferenceDatabaseDTO> convertReferenceDTOs(List<ReferenceDatabase> referenceDatabases){
        List<ReferenceDatabaseDTO> referenceDatabaseDTOList = new ArrayList<ReferenceDatabaseDTO>() ;

        for(ReferenceDatabase referenceDatabase : referenceDatabases){
            referenceDatabaseDTOList.add(convertReferenceDTO(referenceDatabase)) ;
        }

        return referenceDatabaseDTOList;
    }

    public static ReferenceDatabaseDTO convertReferenceDTO(ReferenceDatabase referenceDatabase){
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO() ;
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        if(referenceDatabase.getPrimaryBlastDatabase()!=null){
            referenceDatabaseDTO.setBlastName(referenceDatabase.getPrimaryBlastDatabase().getName());
        }
        return referenceDatabaseDTO ;
    }

    public static TranscriptDBLink createTranscriptDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO){
        TranscriptDBLink dbLink = new TranscriptDBLink() ;
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // set transcript
        Transcript transcript = getTranscriptFromDBLinkDTO(dbLinkDTO) ;
        dbLink.setTranscript(transcript);
        dbLink.setDataZdbID(transcript.getZdbID());

        // reference DBs
        ReferenceDatabase referenceDatabase = null ;
        if(dbLinkDTO.getReferenceDatabaseDTO()!=null){
            referenceDatabase = DTOHelper.getReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());
        }

        logger.info("referenceDB: "+referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink ;
    }

    public static MarkerDBLink createMarkerDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO){
        MarkerDBLink dbLink = new MarkerDBLink() ;
        dbLink.setAccessionNumber(dbLinkDTO.getName());
        dbLink.setLength(dbLinkDTO.getLength());

        // though we can't save this into the database, we can set it here to make things easier
        dbLink.setDataZdbID(dbLinkDTO.getDataZdbID());

        logger.info("creating marker dblink: "+ dbLinkDTO.getDataZdbID());

        // set marker
        dbLink.setMarker(getMarkerFromDBLinkDTO(dbLinkDTO));

        logger.info("got marker dblink: "+ dbLink.getMarker());

        // reference DBs
        ReferenceDatabase referenceDatabase = DTOHelper.getReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO());

        logger.info("referenceDB: "+referenceDatabase);
        dbLink.setReferenceDatabase(referenceDatabase);
        return dbLink ;
    }

    public static DBLink createDBLinkFromDBLinkDTO(DBLinkDTO dbLinkDTO){
        return (dbLinkDTO.isTranscriptDBLink() ? createTranscriptDBLinkFromDBLinkDTO(dbLinkDTO) : createMarkerDBLinkFromDBLinkDTO(dbLinkDTO)) ;
    }

    //todo: do these need to get the dataZdbID set?
    public static MarkerDTO createMarkerDTOFromMarker(Marker marker){
        MarkerDTO markerDTO = new MarkerDTO() ;
        markerDTO.setName(marker.getAbbreviation());
        markerDTO.setAbbreviationOrder(marker.getAbbreviationOrder());
        markerDTO.setZdbID(marker.getZdbID());
        markerDTO.setLink(MarkerPresentation.getLink(marker)) ;
        return markerDTO ;
    }
}
