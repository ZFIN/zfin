package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.audit.AuditLogItem;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
@RequestMapping("/marker")
public class SequenceViewController {

    private Logger logger = LogManager.getLogger(SequenceViewController.class);

    @Autowired
    private MarkerRepository markerRepository ;

    @RequestMapping(value ="/sequence/view/{zdbID}")
    public String getSequenceView(
            Model model
            ,@PathVariable("zdbID") String zdbID)
            throws Exception {
        // set base bean

        logger.debug("Start SequenceView Controller");

        Marker marker = markerRepository.getMarkerByID(zdbID);

        if (marker == null){
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if(replacedZdbID !=null){
                logger.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                marker = markerRepository.getMarkerByID(replacedZdbID);
            }
        }

        if (marker == null){
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        //setting supporting sequences
        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(marker);
        sequenceInfo.setMarker(marker);

        AuditLogItem lastUpdated = RepositoryFactory.getAuditLogRepository().getLatestAuditLogItem(zdbID);

        model.addAttribute("lastUpdated", lastUpdated);
        model.addAttribute(LookupStrings.FORM_BEAN, sequenceInfo);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Sequences for Gene: " + marker.getAbbreviation());

        return "marker/sequence-view.page";
    }


    @RequestMapping(value ="/sequence/edit/{zdbID}")
    public String getSequenceEditView(
            Model model
            ,@PathVariable("zdbID") String zdbID)
            throws Exception {

        logger.debug("Start SequenceView Controller");

        Marker marker = markerRepository.getMarkerByID(zdbID);

        if (marker == null){
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if(replacedZdbID !=null){
                logger.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                marker = markerRepository.getMarkerByID(replacedZdbID);
            }
        }

        if (marker == null){
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        //setting supporting sequences
        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(marker);
        sequenceInfo.setMarker(marker);

        AuditLogItem lastUpdated = RepositoryFactory.getAuditLogRepository().getLatestAuditLogItem(zdbID);

        model.addAttribute("lastUpdated", lastUpdated);
        model.addAttribute(LookupStrings.FORM_BEAN, sequenceInfo);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Sequences for Gene: " + marker.getAbbreviation());

        return "marker/sequence-edit.page";
    }
}