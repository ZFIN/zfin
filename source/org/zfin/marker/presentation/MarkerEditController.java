package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.*;
import org.zfin.nomenclature.presentation.Nomenclature;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;

/**
 */
@Controller
@RequestMapping("/marker")
public class MarkerEditController {

    private static Logger logger = Logger.getLogger(MarkerEditController.class);

    @RequestMapping("/marker-edit")
    public String getMarkerEdit(Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {
        logger.info("zdbID: " + zdbID);

        MarkerBean markerBean = new MarkerBean();

        if (zdbID.startsWith("ZDB-TSCRIPT-")) {
            Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(zdbID);
            if (transcript != null) {
                markerBean.setMarker(transcript);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.TRANSCRIPT.getEditTitleString() + transcript.getAbbreviation());
                return "marker/transcript-edit.page";
            }
        }


        if (zdbID.startsWith("ZDB-ATB-")) {
            Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
            if (antibody != null) {
                markerBean.setMarker(antibody);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getEditTitleString() + antibody.getAbbreviation());
                return "marker/antibody-edit.page";
            }
        }


        // handle things that mark to clone
        Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(zdbID);
        if (clone != null) {
            markerBean.setMarker(clone);
            model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getEditTitleString() + clone.getAbbreviation());
            return "marker/clone-edit.page";
        }

        model.addAttribute(LookupStrings.ZDB_ID, zdbID);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = "edit/{zdbID}", method = RequestMethod.POST)
    public Boolean editNameAndAbbreviation(@PathVariable String zdbID,
                                           @RequestBody Nomenclature nomenclature) {
        Marker marker = getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null)
            throw new RuntimeException("No Marker record found");

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            MarkerHistory history = new MarkerHistory();
            history.setComments(nomenclature.getComments());
            history.setReason(MarkerHistory.Reason.getReason(nomenclature.getReason()));
            history.setName(nomenclature.getName());
            history.setMarker(marker);
            if (nomenclature.isGeneAbbreviationChange()) {
                history.setEvent(MarkerHistory.Event.REASSIGNED);
                history.setOldMarkerName(marker.getAbbreviation());
                history.setSymbol(nomenclature.getAbbreviation());
                history.setName(marker.getName());
                MarkerAlias alias = getMarkerRepository().addMarkerAlias(marker, marker.getAbbreviation(), null);
                history.setMarkerAlias(alias);
                marker.setAbbreviation(nomenclature.getAbbreviation());
            } else if (nomenclature.isGeneNameChange()) {
                history.setEvent(MarkerHistory.Event.RENAMED);
                history.setOldMarkerName(marker.getName());
                history.setSymbol(marker.getAbbreviation());
                history.setName(nomenclature.getName());
                marker.setName(nomenclature.getName());
            }
            getInfrastructureRepository().insertMarkerHistory(history);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return true;
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/addAlias", method = RequestMethod.POST)
    public Boolean addAlias(@PathVariable String zdbID,
                            @RequestBody Nomenclature nomenclature) {
        Marker marker = getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null)
            throw new RuntimeException("No Marker record found");

        Publication publication = null;
        if (nomenclature.getAttribution() != null) {
            publication = getPublicationRepository().getPublication(nomenclature.getAttribution());
            if (publication == null)
                throw new RuntimeException("No valid publication record found");
        }

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            MarkerAlias alias = getMarkerRepository().addMarkerAlias(marker, nomenclature.getNewAlias(), publication);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return true;
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/remove-alias/{aliasZdbID}", method = RequestMethod.DELETE)
    public Boolean deleteAlias(@PathVariable String zdbID,
                               @PathVariable String aliasZdbID) {
        Marker marker = getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null)
            throw new RuntimeException("No Marker record found");
        MarkerAlias alias = getMarkerRepository().getMarkerAlias(aliasZdbID);
        if (alias == null)
            throw new RuntimeException("No Marker alias record found");

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            getMarkerRepository().deleteMarkerAlias(marker, alias);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return true;
    }

    @ResponseBody
    @RequestMapping(value = "alias/attributions/{aliasZdbID}", method = RequestMethod.GET)
    public List<PublicationDTO> getAttributions(@PathVariable String aliasZdbID) {
        MarkerAlias history = getMarkerRepository().getMarkerAlias(aliasZdbID);
        if (history == null)
            throw new RuntimeException("No Marker Alias found for ID: " + aliasZdbID);
        List<PublicationAttribution> attributionList = getInfrastructureRepository().getPublicationAttributions(aliasZdbID);
        List<PublicationDTO> dtoList = new ArrayList<>(attributionList.size());
        for (PublicationAttribution attribution : attributionList) {
            dtoList.add(DTOConversionService.convertToPublicationDTO(attribution.getPublication()));
        }
        return dtoList;
    }

    @ResponseBody
    @RequestMapping(value = "previous-name-list/{zdbID}", method = RequestMethod.GET)
    public List<PreviousNameLight> getPreviousNameList(@PathVariable String zdbID) {
        Marker marker = getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null)
            throw new RuntimeException("No Marker found for ID: " + zdbID);
        return getMarkerRepository().getPreviousNamesLight(marker);
    }

    @ResponseBody
    @RequestMapping(value = "alias/deleteAttribution/{zdbID}/{pubID}", method = RequestMethod.DELETE)
    public List<PublicationDTO> deleteAttribution(@PathVariable String zdbID,
                                                  @PathVariable String pubID) {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null)
            throw new RuntimeException("No publication found");
        MarkerAlias history = getMarkerRepository().getMarkerAlias(zdbID);
        if (history == null)
            throw new RuntimeException("No Marker Alias record found");

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            getInfrastructureRepository().removeRecordAttributionForData(zdbID, pubID);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return getAttributions(zdbID);
    }

    @ResponseBody
    @RequestMapping(value = "alias/addAttribution/{zdbID}", method = RequestMethod.POST)
    public List<PublicationDTO> addAttribution(@PathVariable String zdbID,
                                               @RequestBody String pubID) throws InvalidWebRequestException {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null)
            throw new InvalidWebRequestException("No publication found for ID: " + pubID, null);
        MarkerAlias history = getMarkerRepository().getMarkerAlias(zdbID);
        if (history == null)
            throw new InvalidWebRequestException("No Marker Alias record found for ID: " + zdbID, null);

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            getInfrastructureRepository().insertStandardPubAttribution(zdbID, publication);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new InvalidWebRequestException("Error during transaction. Rolled back.", null);
        }

        return getAttributions(zdbID);
    }

}