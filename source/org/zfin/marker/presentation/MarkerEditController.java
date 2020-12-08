package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.marker.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.nomenclature.presentation.Nomenclature;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 *
 */
@Controller
@RequestMapping("/marker")
public class MarkerEditController {

    public static final String BR = "<br/>";
    private static Logger logger = LogManager.getLogger(MarkerEditController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/marker-edit")
    public String getMarkerEdit(Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {
        logger.info("zdbID: " + zdbID);

        MarkerBean markerBean = new MarkerBean();

        if (zdbID.startsWith("ZDB-TSCRIPT-")) {
            Transcript transcript = markerRepository.getTranscriptByZdbID(zdbID);
            if (transcript != null) {
                markerBean.setMarker(transcript);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.TRANSCRIPT.getEditTitleString() + transcript.getAbbreviation());
                return "marker/transcript-edit";
            }
        }


        if (zdbID.startsWith("ZDB-ATB-")) {
            Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
            if (antibody != null) {
                markerBean.setMarker(antibody);
                model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
                model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getEditTitleString() + antibody.getAbbreviation());
                return "marker/antibody-edit";
            }
        }


        // handle things that mark to clone
        Clone clone = markerRepository.getCloneById(zdbID);
        if (clone != null) {
            markerBean.setMarker(clone);
            model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.CLONE.getEditTitleString() + clone.getAbbreviation());
            return "marker/clone-edit";
        }

        model.addAttribute(LookupStrings.ZDB_ID, zdbID);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }

    private MarkerHistory newMarkerHistory(Marker marker, MarkerHistory.Event event, String reason, String comments) {
        MarkerHistory history = new MarkerHistory();
        history.setComments(comments);
        history.setReason(MarkerHistory.Reason.getReason(reason));
        history.setEvent(event);
        history.setMarker(marker);
        return history;
    }

    private Nomenclature getNomenclatureForMarker(Marker marker) {
        Nomenclature nomenclature = new Nomenclature();
        nomenclature.setName(marker.getName());
        nomenclature.setAbbreviation(marker.getAbbreviation());
        nomenclature.setReason("");
        nomenclature.setComments("");
        nomenclature.putMeta("reasons", Arrays.stream(MarkerHistory.Reason.values())
                .map(MarkerHistory.Reason::toString)
                .toArray()
        );
        return nomenclature;
    }

    @ResponseBody
    @RequestMapping(value = "/validate", method = RequestMethod.GET)
    public JSONMessageList queryMarkerLookup(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String abbreviation) {
        JSONMessageList response = new JSONMessageList();
        List<String> errors = new ArrayList<>();
        if (StringUtils.isNotEmpty(name)) {
            String result = NomenclatureValidationService.validateMarkerName(name);
            if (result != null) {
                errors.add(messageSource.getMessage(result, null, Locale.ENGLISH));
            }
        }
        if (StringUtils.isNotEmpty(abbreviation)) {
            String result = NomenclatureValidationService.validateMarkerAbbreviation(abbreviation);
            if (result != null) {
                errors.add(messageSource.getMessage(result, null, Locale.ENGLISH));
            }
        }
        response.setErrors(errors);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/nomenclature", method = RequestMethod.GET)
    public Nomenclature getMarkerNomenclature(@PathVariable String markerID) {
        Marker marker = markerRepository.getMarkerByID(markerID);
        if (marker == null) {
            throw new InvalidWebRequestException("No Marker record found");
        }
        return getNomenclatureForMarker(marker);
    }

    // This serves the new gene edit page which allows updating name and abbreviation simultaneously
    @ResponseBody
    @RequestMapping(value = "/{markerID}/nomenclature", method = RequestMethod.POST)
    public Nomenclature updateMarkerNomenclature(@PathVariable String markerID,
                                                 @RequestBody Nomenclature nomenclature) {
        Marker marker = markerRepository.getMarkerByID(markerID);
        if (marker == null) {
            throw new InvalidWebRequestException("No Marker record found");
        }

        if (marker.isInTypeGroup(Marker.TypeGroup.ABBREV_EQ_NAME)) {
            nomenclature.setAbbreviation(nomenclature.getName());
        }

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            boolean nameChanged = !Objects.equals(marker.getName(), nomenclature.getName());
            boolean abbreviationChanged = !Objects.equals(marker.getAbbreviation(), nomenclature.getAbbreviation());
            if (nameChanged) {
                MarkerHistory history = newMarkerHistory(marker, MarkerHistory.Event.RENAMED, nomenclature.getReason(), nomenclature.getComments());
                history.setOldMarkerName(marker.getName());
                history.setSymbol(marker.getAbbreviation());
                history.setName(nomenclature.getName());
                getInfrastructureRepository().insertMarkerHistory(history);
            }
            if (abbreviationChanged) {
                MarkerHistory history = newMarkerHistory(marker, MarkerHistory.Event.REASSIGNED, nomenclature.getReason(), nomenclature.getComments());
                history.setOldMarkerName(marker.getAbbreviation());
                history.setSymbol(nomenclature.getAbbreviation());
                history.setName(marker.getName());
                MarkerAlias alias = markerRepository.addMarkerAlias(marker, marker.getAbbreviation(), null);
                history.setMarkerAlias(alias);
                getInfrastructureRepository().insertMarkerHistory(history);
            }
            marker.setAbbreviation(nomenclature.getAbbreviation());
            marker.setName(nomenclature.getName());
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new InvalidWebRequestException(getExceptionErrorMessages("Could not update marker", e), null);
        }

        return getNomenclatureForMarker(marker);
    }

    // This serves the old gene edit page which updates name and abbreviation independently
    @ResponseBody
    @RequestMapping(value = "edit/{zdbID}", method = RequestMethod.POST)
    public Boolean editNameAndAbbreviation(@PathVariable String zdbID,
                                           @RequestBody Nomenclature nomenclature) throws InvalidWebRequestException {
        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (marker == null) {
            throw new InvalidWebRequestException("No Marker record found");
        }

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();

            if (ActiveData.isInGroupGenedom(zdbID) || marker.isInTypeGroup(Marker.TypeGroup.KNOCKDOWN_REAGENT)) {
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
                    MarkerAlias alias = markerRepository.addMarkerAlias(marker, marker.getAbbreviation(), null);
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
            } else {
                marker.setName(nomenclature.getName());
            }
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new InvalidWebRequestException(getExceptionErrorMessages("Could not update gene record", e), null);
        }

        return true;
    }

    private String getExceptionErrorMessages(String header, Exception e) {
        String errorMessage = "";
        if (header != null) {
            errorMessage += header;
        }
        errorMessage += BR;
        errorMessage += e.getMessage();
        if (e.getCause() != null) {
            errorMessage += BR + e.getCause().getMessage();
        }
        return errorMessage;
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/addAlias", method = RequestMethod.POST)
    public Boolean addAlias(@PathVariable String zdbID,
                            @RequestBody Nomenclature nomenclature) throws InvalidWebRequestException {
        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (marker == null) {
            throw new InvalidWebRequestException("No Marker record found");
        }

        Publication publication = null;
        if (nomenclature.getAttribution() != null) {
            publication = getPublicationRepository().getPublication(nomenclature.getAttribution());
            if (publication == null) {
                throw new InvalidWebRequestException("No valid publication record found");
            }
        }

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            MarkerAlias alias = markerRepository.addMarkerAlias(marker, nomenclature.getNewAlias(), publication);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new InvalidWebRequestException(getExceptionErrorMessages("Could not update gene record", e), null);
        }

        return true;
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/remove-alias/{aliasZdbID}", method = RequestMethod.DELETE)
    public Boolean deleteAlias(@PathVariable String zdbID,
                               @PathVariable String aliasZdbID) {
        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (marker == null) {
            throw new RuntimeException("No Marker record found");
        }
        MarkerAlias alias = markerRepository.getMarkerAlias(aliasZdbID);
        if (alias == null) {
            throw new RuntimeException("No Marker alias record found");
        }

        Transaction tx = null;
        try {
            tx = HibernateUtil.createTransaction();
            markerRepository.deleteMarkerAlias(marker, alias);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
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
        MarkerAlias history = markerRepository.getMarkerAlias(aliasZdbID);
        if (history == null) {
            throw new RuntimeException("No Marker Alias found for ID: " + aliasZdbID);
        }
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
        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (marker == null) {
            throw new RuntimeException("No Marker found for ID: " + zdbID);
        }
        return markerRepository.getPreviousNamesLight(marker);
    }

    @ResponseBody
    @RequestMapping(value = "alias/deleteAttribution/{zdbID}/{pubID}", method = RequestMethod.DELETE)
    public List<PublicationDTO> deleteAttribution(@PathVariable String zdbID,
                                                  @PathVariable String pubID) {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            throw new RuntimeException("No publication found");
        }
        MarkerAlias history = markerRepository.getMarkerAlias(zdbID);
        if (history == null) {
            throw new RuntimeException("No Marker Alias record found");
        }

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            getInfrastructureRepository().removeRecordAttributionForData(zdbID, pubID);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
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
        if (publication == null) {
            throw new InvalidWebRequestException("No publication found for ID: " + pubID, null);
        }
        MarkerAlias history = markerRepository.getMarkerAlias(zdbID);
        if (history == null) {
            throw new InvalidWebRequestException("No Marker Alias record found for ID: " + zdbID, null);
        }

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            getInfrastructureRepository().insertStandardPubAttribution(zdbID, publication);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new InvalidWebRequestException("Error during transaction. Rolled back.", null);
        }

        return getAttributions(zdbID);
    }

}