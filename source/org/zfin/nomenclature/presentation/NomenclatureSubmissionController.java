package org.zfin.nomenclature.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.captcha.CaptchaService;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.nomenclature.*;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

@Controller
@RequestMapping("/nomenclature")
public class NomenclatureSubmissionController {

    private static final Logger LOG = LogManager.getLogger(NomenclatureSubmissionController.class);

    @RequestMapping(value = "/history/{zdbID}")
    public String getHistoryView(@PathVariable("zdbID") String zdbID,
                                 Model model) {
        if (zdbID == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Marker History");

        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No marker with ID " + zdbID + " found");
        } else {
            model.addAttribute("markerZdbID", marker.getZdbID());
        }

        return "nomenclature/history-view";
    }

    @RequestMapping(value = "/view/{zdbID}")
    public String getView(@PathVariable("zdbID") String zdbID,
                          Model model) {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        if (zdbID == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null) {
            throw new RuntimeException("No Marker History record found");
        }
        model.addAttribute("markerHistory", history);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Marker History Event");
        return "nomenclature/event-view";
    }

    @RequestMapping(value = "/gene-name", method = RequestMethod.GET)
    public String newGeneNameForm(Model model, HttpServletRequest request) {
        //TODO: This would read better if it was an annotation on the method (eg. `@RequiresCaptcha`)
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return "redirect:" + captchaRedirectUrl.get();
        }
        GeneNameSubmission submission = new GeneNameSubmission();
        submission.setHomologyInfoList(Arrays.asList(new HomologyInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Gene Name");
        return "nomenclature/gene-name-form";
    }

    @RequestMapping(value = "/gene-name", method = RequestMethod.POST)
    public String newGeneNameSubmit(@ModelAttribute GeneNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from homology table
        removeEmptyRows(submission.getHomologyInfoList());

        model.addAttribute("sent", sendNameSubmissionEmail(submission));
        return "nomenclature/gene-name-submit";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.GET)
    public String newLineNameForm(Model model, HttpServletRequest request) {
        //TODO: This would read better if it was an annotation on the method (eg. `@RequiresCaptcha`)
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return "redirect:" + captchaRedirectUrl.get();
        }
        LineNameSubmission submission = new LineNameSubmission();
        submission.setLineDetails(Arrays.asList(new LineInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Mutant/Transgenic Line Name");
        return "nomenclature/line-name-form";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.POST)
    public String newLineNameSubmit(@ModelAttribute LineNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from line information table
        removeEmptyRows(submission.getLineDetails());

        model.addAttribute("sent", sendNameSubmissionEmail(submission));
        return "nomenclature/line-name-submit";
    }

    @ModelAttribute("pubStatusOptions")
    public List<String> getPubStatusOptions() {
        return Arrays.asList("Published", "In Press", "Submitted", "In Preparation", "Unpublished");
    }

    private static void removeEmptyRows(Collection<? extends EmptyTestable> collection) {
        CollectionUtils.filter(collection, o -> (o instanceof EmptyTestable) && !((EmptyTestable) o).isEmpty());
    }

    @ResponseBody
    @RequestMapping(value = "addAttribution/{zdbID}", method = RequestMethod.POST)
    public List<PublicationDTO> addAttribution(@PathVariable String zdbID,
                                               @RequestBody String pubID) throws InvalidWebRequestException {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            throw new InvalidWebRequestException("No publication found for ID: " + pubID, null);
        }
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null) {
            throw new InvalidWebRequestException("No Marker History record found for ID: " + zdbID, null);
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
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new InvalidWebRequestException("Error during transaction. Rolled back.", null);
        }

        return getAttributions(zdbID);
    }

    @ResponseBody
    @RequestMapping(value = "update/{zdbID}", method = RequestMethod.POST)
    public Boolean update(@PathVariable String zdbID,
                          @RequestBody Nomenclature nomenclature) {
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null) {
            throw new RuntimeException("No Marker History record found");
        }

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            history.setComments(nomenclature.getComments());
            history.setReason(MarkerHistory.Reason.getReason(nomenclature.getReason()));
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return true;
    }

    @ResponseBody
    @RequestMapping(value = "deleteAttribution/{zdbID}/{pubID}", method = RequestMethod.DELETE)
    public List<PublicationDTO> deleteAttribution(@PathVariable String zdbID,
                                                  @PathVariable String pubID) {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            throw new RuntimeException("No publication found");
        }
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null) {
            throw new RuntimeException("No Marker History record found");
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
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return getAttributions(zdbID);
    }


    @ResponseBody
    @RequestMapping(value = "attributions/{zdbID}", method = RequestMethod.GET)
    public List<PublicationDTO> getAttributions(@PathVariable String zdbID) {
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null) {
            throw new RuntimeException("No Marker History record found");
        }
        List<PublicationAttribution> attributionList = getInfrastructureRepository().getPublicationAttributions(zdbID);
        List<PublicationDTO> dtoList = new ArrayList<>(attributionList.size());
        for (PublicationAttribution attribution : attributionList) {
            dtoList.add(DTOConversionService.convertToPublicationDTO(attribution.getPublication()));
        }
        return dtoList;
    }

    private boolean sendNameSubmissionEmail(NameSubmission submission) {
        // send email to nomenclature coordinator if decoy email field is empty to reduce spam
        if (StringUtils.isNotEmpty(submission.getEmail())) {
            return false;
        }

        String submitterEmail = submission.getEmail2();
        String[] nomenclatureCoordinatorEmails = ZfinPropertiesEnum.NOMEN_COORDINATOR.value().split(" ");
        String nomenclatureCoordinatorEmail = nomenclatureCoordinatorEmails[0];

        boolean success = false;
        success = AbstractZfinMailSender.getInstance().sendMail(
                submission.getSubjectLine(),
                submission.toString(),
                false,
                submitterEmail,
                nomenclatureCoordinatorEmails
        );

        success = success && AbstractZfinMailSender.getInstance().sendMail(
                this.getConfirmationSubjectLine(submission),
                this.getConfirmationBody(submission),
                false,
                nomenclatureCoordinatorEmail,
                new String[]{submitterEmail}
        );

        return success;
    }

    private String getConfirmationBody(NameSubmission submission) {
        return "The following submission has been received:\n\n" +
                submission.toString() +
                "\n\n" +
                "Your contribution will be reviewed by the nomenclature coordinator and will be added to the database " +
                "once it has been approved.\n\n" +
                "Thank you for your contribution to the Zebrafish Information Network.";
    }

    private String getConfirmationSubjectLine(NameSubmission submission) {
        return "ZFIN Confirmation for " + submission.getSubjectLine();
    }

}
