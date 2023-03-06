package org.zfin.publication.presentation;

import org.zfin.publication.CorrespondenceNeed;
import org.zfin.publication.CorrespondenceNeedReason;
import org.zfin.publication.CorrespondenceResolution;
import org.zfin.publication.CorrespondenceResolutionType;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class CorrespondenceService {

    public static List<CorrespondenceNeedDTO> getCorrespondenceNeedDTOsGridByPublicationID(String pubID) {
        List<CorrespondenceNeedReason> neededReasons = getCorrespondenceNeedReasonsByPublicationID(pubID);
        return getCorrespondenceNeedDTOsGridFromCorrespondenceNeedList(neededReasons);
    }

    private static List<CorrespondenceNeedReason> getCorrespondenceNeedReasonsByPublicationID(String pubID) {
        List<CorrespondenceNeed> cn = getPublicationRepository().getCorrespondenceNeedByPublicationID(pubID);
        return cn.stream().map(CorrespondenceNeed::getReason).toList();
    }

    private static List<CorrespondenceNeedDTO> getCorrespondenceNeedDTOsGridFromCorrespondenceNeedList(List<CorrespondenceNeedReason> neededReasons) {
        List<CorrespondenceNeedReason> allReasons = getPublicationRepository().getAllCorrespondenceNeedReasons();
        ArrayList<CorrespondenceNeedDTO> correspondenceNeedDTOS = new ArrayList<>();
        for (CorrespondenceNeedReason reason : allReasons) {
            correspondenceNeedDTOS.add(CorrespondenceNeedDTO.fromCorrespondenceNeedReason(reason, neededReasons.contains(reason)));
        }
        return correspondenceNeedDTOS;
    }

    public static void setCorrespondenceNeedByPublicationID(String pubID, List<CorrespondenceNeedDTO> correspondenceNeedDTOS) {
        //delete all correspondence needed for this publication
        getPublicationRepository().deleteCorrespondenceNeedByPublicationID(pubID);

        //add all correspondence needed for this publication
        for (CorrespondenceNeedDTO correspondenceNeedDTO : correspondenceNeedDTOS) {
            if (correspondenceNeedDTO.isNeeded()) {
                CorrespondenceNeed correspondenceNeed = new CorrespondenceNeed();
                correspondenceNeed.setPublication(getPublicationRepository().getPublication(pubID));
                correspondenceNeed.setReason(getPublicationRepository().getCorrespondenceNeedReasonByID(correspondenceNeedDTO.getId()));
                getPublicationRepository().insertCorrespondenceNeed(correspondenceNeed);
            }
        }
    }

    public static List<CorrespondenceResolutionDTO> getCorrespondenceResolutionDTOsGridByPublicationID(String pubID) {
        List<CorrespondenceResolutionType> neededReasons = getCorrespondenceResolutionTypesByPublicationID(pubID);
        return getCorrespondenceResolutionDTOsGridFromCorrespondenceResolutionList(neededReasons);
    }

    private static List<CorrespondenceResolutionType> getCorrespondenceResolutionTypesByPublicationID(String pubID) {
        List<CorrespondenceResolution> cn = getPublicationRepository().getCorrespondenceResolutionByPublicationID(pubID);
        return cn.stream().map(CorrespondenceResolution::getResolutionType).toList();
    }

    private static List<CorrespondenceResolutionDTO> getCorrespondenceResolutionDTOsGridFromCorrespondenceResolutionList(List<CorrespondenceResolutionType> neededReasons) {
        List<CorrespondenceResolutionType> allResolutionTypes = getPublicationRepository().getAllCorrespondenceResolutionTypes();
        ArrayList<CorrespondenceResolutionDTO> correspondenceResolutionDTOS = new ArrayList<>();
        for (CorrespondenceResolutionType resolutionType : allResolutionTypes) {
            correspondenceResolutionDTOS.add(CorrespondenceResolutionDTO.fromCorrespondenceResolutionType(resolutionType, neededReasons.contains(resolutionType)));
        }
        return correspondenceResolutionDTOS;
    }

    public static void setCorrespondenceResolutionByPublicationID(String pubID, List<CorrespondenceResolutionDTO> correspondenceResolutionDTOS) {
        //delete all correspondence needed for this publication
        getPublicationRepository().deleteCorrespondenceResolutionByPublicationID(pubID);

        //add all correspondence needed for this publication
        for (CorrespondenceResolutionDTO correspondenceResolutionDTO : correspondenceResolutionDTOS) {
            if (correspondenceResolutionDTO.isResolved()) {
                CorrespondenceResolution correspondenceResolution = new CorrespondenceResolution();
                correspondenceResolution.setPublication(getPublicationRepository().getPublication(pubID));
                correspondenceResolution.setResolutionType(getPublicationRepository().getCorrespondenceResolutionTypeByID(correspondenceResolutionDTO.getId()));
                getPublicationRepository().insertCorrespondenceResolution(correspondenceResolution);
            }
        }
    }

}
