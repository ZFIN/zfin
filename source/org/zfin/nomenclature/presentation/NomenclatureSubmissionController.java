package org.zfin.nomenclature.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.nomenclature.*;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;

@Controller
@RequestMapping("/nomenclature")
public class NomenclatureSubmissionController {

    private static final Logger LOG = Logger.getLogger(NomenclatureSubmissionController.class);

    @RequestMapping(value = "/history/{zdbID}")
    public String getHistoryView(@PathVariable("zdbID") String zdbID,
                                 Model model) {
        if (zdbID == null)
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No marker with ID " + zdbID + " found");
        }
        model.addAttribute("marker", marker);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "ZFIN Marker History");
        return "nomenclature/history-view.page";
    }

    @RequestMapping(value = "/view/{zdbID}")
    public String getView(@PathVariable("zdbID") String zdbID,
                          Model model) {
        if (zdbID == null)
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null)
            throw new RuntimeException("No Marker History record found");
        model.addAttribute("markerHistory", history);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "ZFIN Marker History");
        return "nomenclature/event-view.page";
    }

    @RequestMapping(value = "/gene-name", method = RequestMethod.GET)
    public String newGeneNameForm(Model model) {
        GeneNameSubmission submission = new GeneNameSubmission();
        submission.setHomologyInfoList(Arrays.asList(new HomologyInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Gene Name");
        return "nomenclature/gene-name-form.page";
    }

    @RequestMapping(value = "/gene-name", method = RequestMethod.POST)
    public String newGeneNameSubmit(@ModelAttribute GeneNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from homology table
        removeEmptyRows(submission.getHomologyInfoList());

        // send email to nomenclature coordinator if decoy email field is empty
        boolean sent = false;
        if (StringUtils.isEmpty(submission.getEmail())) {
            MailSender mailer = AbstractZfinMailSender.getInstance();
            sent = mailer.sendMail("Gene Submission: " + submission.getGeneSymbol(),
                    submission.toString(),
                    false,
                    submission.getEmail2(),
                    ZfinPropertiesEnum.NOMEN_COORDINATOR.value().split(" "));
        }
        model.addAttribute("sent", sent);

        return "nomenclature/gene-name-submit.page";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.GET)
    public String newLineNameForm(Model model) {
        LineNameSubmission submission = new LineNameSubmission();
        submission.setLineDetails(Arrays.asList(new LineInfo()));
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Submit a Proposed Mutant/Transgenic Line Name");
        return "nomenclature/line-name-form.page";
    }

    @RequestMapping(value = "/line-name", method = RequestMethod.POST)
    public String newLineNameSubmit(@ModelAttribute LineNameSubmission submission, Model model) {
        model.addAttribute("submission", submission);

        // get rid of any blank rows from line information table
        removeEmptyRows(submission.getLineDetails());

        // send email to nomenclature coordinator if decoy email field is empty
        boolean sent = false;
        if (StringUtils.isEmpty(submission.getEmail())) {
            MailSender mailer = AbstractZfinMailSender.getInstance();
            List<LineInfo> details = submission.getLineDetails();
            sent = mailer.sendMail(
                    "Mutant Submission: " + (details.size() > 0 ? details.get(0).getDesignation() : ""),
                    submission.toString(),
                    false,
                    submission.getEmail2(),
                    ZfinPropertiesEnum.NOMEN_COORDINATOR.value().split(" "));
        }
        model.addAttribute("sent", sent);

        return "nomenclature/line-name-submit.page";
    }

    @ModelAttribute("pubStatusOptions")
    public List<String> getPubStatusOptions() {
        return Arrays.asList("Published", "In Press", "Submitted", "In Preparation", "Unpublished");
    }

    @ModelAttribute("reserveTypeOptions")
    public List<String> getReserveTypeOptions() {
        return Arrays.asList("in my name", "as an anonymous submission");
    }

    private static void removeEmptyRows(Collection<? extends EmptyTestable> collection) {
        CollectionUtils.filter(collection, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return (o instanceof EmptyTestable) && !((EmptyTestable) o).isEmpty();
            }
        });
    }

    @ResponseBody
    @RequestMapping(value = "addAttribution/{zdbID}", method = RequestMethod.POST)
    public List<PublicationDTO> addAttribution(@PathVariable String zdbID,
                                               @RequestBody String pubID) {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null)
            throw new RuntimeException("No publication found");
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null)
            throw new RuntimeException("No Marker History record found");

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            getInfrastructureRepository().insertPublicAttribution(zdbID, pubID);
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return getAttributions(zdbID);
    }

    @ResponseBody
    @RequestMapping(value = "update/{zdbID}", method = RequestMethod.POST)
    public Boolean update(@PathVariable String zdbID,
                          @RequestBody Nomenclature nomenclature) {
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null)
            throw new RuntimeException("No Marker History record found");

        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            history.setComments(nomenclature.getComments());
            history.setReason(MarkerHistory.Reason.getReason(nomenclature.getReason()));
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null)
                    tx.rollback();
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
        if (publication == null)
            throw new RuntimeException("No publication found");
        MarkerHistory history = getMarkerRepository().getMarkerHistory(zdbID);
        if (history == null)
            throw new RuntimeException("No Marker History record found");

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
        if (history == null)
            throw new RuntimeException("No Marker History record found");
        List<PublicationAttribution> attributionList = getInfrastructureRepository().getPublicationAttributions(zdbID);
        List<PublicationDTO> dtoList = new ArrayList<>(attributionList.size());
        for (PublicationAttribution attribution : attributionList) {
            dtoList.add(DTOConversionService.convertToPublicationDTO(attribution.getPublication()));
        }
        return dtoList;
    }

}
