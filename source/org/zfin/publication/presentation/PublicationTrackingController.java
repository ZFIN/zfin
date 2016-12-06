package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.Correspondence;
import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.*;
import org.zfin.curation.repository.CurationRepository;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.EntityZdbIdDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.*;
import org.zfin.publication.repository.PublicationRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationTrackingController {

    private final static Logger LOG = Logger.getLogger(PublicationTrackingController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PhenotypeRepository phenotypeRepository;

    @Autowired
    private ExpressionRepository expressionRepository;

    @Autowired
    private MutantRepository mutantRepository;

    @Autowired
    private CurationRepository curationRepository;

    @Autowired
    private CurationDTOConversionService converter;

    @RequestMapping(value = "/{zdbID}/track")
    public String showPubTracker(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Track Pub: " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("allowCuration", PublicationService.allowCuration(publication));
        model.addAttribute("loggedInUser", ProfileService.getCurrentSecurityUser());
        return "publication/track-publication.page";
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/notes", method = RequestMethod.GET)
    public Collection<PublicationNoteDTO> getPublicationNotes(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        return publication.getNotes().stream()
                .map(converter::toPublicationNoteDTO)
                .collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/notes", method = RequestMethod.POST)
    public PublicationNoteDTO addPublicationNote(@PathVariable String zdbID,
                                                             @RequestBody PublicationNoteDTO noteDTO) {
        Publication publication = publicationRepository.getPublication(zdbID);

        PublicationNote note = new PublicationNote();
        note.setText(noteDTO.getText());
        note.setDate(new Date());
        note.setCurator(ProfileService.getCurrentSecurityUser());
        note.setPublication(publication);

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.save(note);

        PublicationNoteDTO dto = converter.toPublicationNoteDTO(note);

        tx.commit();
        
        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/notes/{zdbID}", method = RequestMethod.POST)
    public PublicationNoteDTO editPublicationNode(@PathVariable String zdbID,
                                                  @RequestBody PublicationNoteDTO noteDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        PublicationNote note = (PublicationNote) session.get(PublicationNote.class, zdbID);
        note.setText(noteDTO.getText());
        session.update(note);

        PublicationNoteDTO dto = converter.toPublicationNoteDTO(note);

        tx.commit();

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/notes/{zdbID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deletePublicationNote(@PathVariable String zdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        PublicationNote note = (PublicationNote) session.get(PublicationNote.class, zdbID);
        session.delete(note);
        tx.commit();

        return "";
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/topics", method = RequestMethod.GET)
    public Collection<CurationDTO> getCurationTopics(@PathVariable String zdbID) {
        Publication pub = publicationRepository.getPublication(zdbID);
        List<Curation> curationList = curationRepository.getCurationForPub(pub);
        return converter.allCurationTopics(curationList);
    }

    @ResponseBody
    @RequestMapping(value = "/statuses", method = RequestMethod.GET)
    public Collection<PublicationTrackingStatus> getAllStatuses() {
        return publicationRepository.getAllPublicationStatuses();
    }

    @ResponseBody
    @RequestMapping(value = "/locations", method = RequestMethod.GET)
    public Collection<PublicationTrackingLocation> getAllLocations() {
        return publicationRepository.getAllPublicationLocations();
    }

    @ResponseBody
    @RequestMapping(value = "/curators", method = RequestMethod.GET)
    public Collection<PersonDTO> getAllCurators() {
        Set<PersonDTO> curatorDTOs = new TreeSet<>();
        // maybe one day we'll have a separate role for just curators?
        List<Person> curators = profileRepository.getUsersByRole("root");
        for (Person curator : curators) {
            curatorDTOs.add(converter.toPersonDTO(curator));
        }
        return curatorDTOs;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/status", method = RequestMethod.GET)
    public CurationStatusDTO getCurationStatus(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        PublicationTrackingHistory currentStatus = publicationRepository.currentTrackingStatus(publication);
        return converter.toCurationStatusDTO(currentStatus);
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/status", method = RequestMethod.POST)
    public CurationStatusDTO updateCurationStatus(@PathVariable String zdbID,@RequestParam(required = false, defaultValue = "false") Boolean claimedFlag,@RequestBody CurationStatusDTO dto) throws InvalidWebRequestException{
        Publication publication = publicationRepository.getPublication(zdbID);
        PublicationTrackingHistory pth=publicationRepository.currentTrackingStatus(publication);
        if (claimedFlag &&pth.getOwner() != null && dto.getOwner() != null && pth.getOwner().getZdbID() != dto.getOwner().getZdbID()) {
            throw new InvalidWebRequestException("Pub already claimed");
        }
        else{
            PublicationTrackingHistory newStatus = new PublicationTrackingHistory();
            newStatus.setPublication(publication);
            newStatus.setStatus(dto.getStatus());
            newStatus.setLocation(dto.getLocation());
            newStatus.setOwner(dto.getOwner() == null ? null : profileRepository.getPerson(dto.getOwner().getZdbID()));
            newStatus.setUpdater(ProfileService.getCurrentSecurityUser());
            newStatus.setDate(new GregorianCalendar());

            Session session = HibernateUtil.currentSession();
            Transaction tx = session.beginTransaction();
            if (newStatus.getStatus().getType() == PublicationTrackingStatus.Type.CLOSED) {
                curationRepository.closeCurationTopics(publication, ProfileService.getCurrentSecurityUser());
                expressionRepository.deleteExpressionStructuresForPub(publication);
                publicationRepository.deleteExpressionExperimentIDswithNoExpressionResult(publication);
                mutantRepository.updateGenotypeNicknameWithHandleForPublication(publication);
            }
            session.save(newStatus);
            tx.commit();
        }

        return converter.toCurationStatusDTO(publicationRepository.currentTrackingStatus(publication));
    }

    @RequestMapping(value = "/{zdbID}/status-history")
    public String showPubStatusHistory(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Status History for " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("statusUpdates", publicationRepository.fullTrackingHistory(publication));
        return "publication/status-history.page";
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/validate", method = RequestMethod.POST)
    public JSONMessageList validatePublication(@PathVariable String zdbID) {
        Collection<String> warnings = new ArrayList<>();

        List<Curation> openTopics = curationRepository.getOpenCurationTopics(zdbID);
        if (CollectionUtils.isNotEmpty(openTopics)) {
            warnings.add("There are open topics which will be closed");
        }

        List<String> featureNames = publicationRepository.getFeatureNamesWithNoGenotypesForPub(zdbID);
        if (CollectionUtils.isNotEmpty(featureNames)) {
            warnings.add("The following features have no genotypes: " + StringUtils.join(featureNames, ", "));
        }

        featureNames = publicationRepository.getTalenOrCrisprFeaturesWithNoRelationship(zdbID);
        if (CollectionUtils.isNotEmpty(featureNames)) {
            warnings.add("The following features were created by a CRISPR or TALEN with no feature marker relationship: " + StringUtils.join(featureNames, ", "));
        }

        List<PhenotypeExperiment> phenotypeExperiments = phenotypeRepository.getPhenotypeExperimentsWithoutAnnotation(zdbID);
        Set<String> figures = new TreeSet<>();
        for (PhenotypeExperiment phenotypeExperiment : phenotypeExperiments) {
            figures.add(phenotypeExperiment.getFigure().getLabel());
        }
        if (CollectionUtils.isNotEmpty(figures)) {
            warnings.add("The following figures still have mutants without phenotypes defined: " + StringUtils.join(figures, ", "));
        }

        JSONMessageList messages = new JSONMessageList();
        messages.setWarnings(warnings);
        return messages;
    }


    @ResponseBody
    @RequestMapping(value = "/{zdbID}/topics", method = RequestMethod.POST)
    public CurationDTO addCurationTopic(@PathVariable String zdbID, @RequestBody CurationDTO topicDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Curation curation = new Curation();
        curation.setTopic(Curation.Topic.fromString(topicDTO.getTopic()));
        curation.setPublication(publicationRepository.getPublication(zdbID));
        curation.setCurator(ProfileService.getCurrentSecurityUser());
        curation.setEntryDate(new Date());
        curation.setDataFound(topicDTO.isDataFound());
        curation.setOpenedDate(topicDTO.getOpenedDate());
        curation.setClosedDate(topicDTO.getClosedDate());
        session.save(curation);

        CurationDTO dto = converter.toCurationDTO(curation);

        tx.commit();

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/topics/{zdbID}", method = RequestMethod.POST)
    public CurationDTO editCurationTopic(@PathVariable String zdbID, @RequestBody CurationDTO topicDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Curation curation = (Curation) session.get(Curation.class, zdbID);
        curation.setDataFound(topicDTO.isDataFound());
        curation.setCurator(ProfileService.getCurrentSecurityUser());
        curation.setOpenedDate(topicDTO.getOpenedDate());
        curation.setClosedDate(topicDTO.getClosedDate());
        session.update(curation);

        CurationDTO dto = converter.toCurationDTO(curation);

        tx.commit();

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/correspondences", method = RequestMethod.GET)
    public Collection<CorrespondenceDTO> getCorrespondences(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        List<CorrespondenceDTO> correspondences = new ArrayList<>();
        correspondences.addAll(publication.getSentMessages().stream()
                .map(converter::toCorrespondenceDTO)
                .collect(Collectors.toList()));
        correspondences.addAll(publication.getReceivedMessages().stream()
                .map(converter::toCorrespondenceDTO)
                .collect(Collectors.toList()));
        Collections.sort(correspondences, Collections.reverseOrder());
        return correspondences;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/correspondences", method = RequestMethod.POST)
    public CorrespondenceDTO newCorrespondence(@PathVariable String zdbID, @RequestBody CorrespondenceDTO dto) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Publication publication = publicationRepository.getPublication(zdbID);

        if (dto.isOutgoing()) {
            CorrespondenceSentMessage correspondence;
            if (dto.isResend()) {
                correspondence = publicationRepository.addResentCorrespondence(publication, dto);
            } else {
                correspondence = publicationRepository.addSentCorrespondence(publication, dto);
            }

            MailSender mailer = AbstractZfinMailSender.getInstance();
            if (mailer == null) {
                throw new InvalidWebRequestException("no mail sender available");
            }

            List<String> recipients = new ArrayList<>();
            Person currentUser = ProfileService.getCurrentSecurityUser();
            if (currentUser == null) {
                throw new InvalidWebRequestException("unauthorized");
            }
            recipients.add(currentUser.getEmail());
            List<String> externalRecipients = correspondence.getMessage().getRecipients().stream()
                    .map(CorrespondenceRecipient::getEmail)
                    .collect(Collectors.toList());

            String message = correspondence.getMessage().getText();

            if (Boolean.valueOf(ZfinPropertiesEnum.SEND_AUTHOR_NOTIF_EMAIL.value())) {
                recipients.addAll(externalRecipients);
            } else {
                message = "[[ Recipient list on production environment: " + String.join(", ", externalRecipients) + " ]]<br><br>" +
                        "------------------<br><br>" +
                        message;
            }

            String sender = correspondence.getFrom().getFirstName() + " " +
                    correspondence.getFrom().getLastName() +
                    " <" + correspondence.getFrom().getEmail() + ">";

            boolean sent = mailer.sendHtmlMail(correspondence.getMessage().getSubject(),
                    message, false, sender, recipients.toArray(new String[]{}));

            if (!sent) {
                throw new InvalidWebRequestException("error sending email");
            }

            dto = converter.toCorrespondenceDTO(correspondence);
        } else {
            CorrespondenceReceivedMessage correspondence = publicationRepository.addReceivedCorrespondence(publication, dto);
            dto = converter.toCorrespondenceDTO(correspondence);
        }

        tx.commit();

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/correspondences/{id}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deleteCorrespondence(@PathVariable long id) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Correspondence correspondence = (Correspondence) session.get(Correspondence.class, id);
        session.delete(correspondence);

        tx.commit();

        return "";
    }

    @ResponseBody
    @RequestMapping(value = "{id}/details", method = RequestMethod.GET)
    public PublicationDTO getPublicationDetails(@PathVariable String id) {
        Publication publication = publicationRepository.getPublication(id);
        return DTOConversionService.convertToPublicationDTO(publication);
    }

    @ResponseBody
    @RequestMapping(value = "{id}/curatedEntities", method = RequestMethod.GET)
    public Map<String, Set<EntityZdbIdDTO>> getCuratedData(@PathVariable String id) {
        Map<String, Set<EntityZdbIdDTO>> data = new HashMap<>();

        Set<EntityZdbIdDTO> markers = new TreeSet<>();
        for (Marker marker : markerRepository.getMarkersForAttribution(id)) {
            markers.add(DTOConversionService.convertToEntityZdbIdDTO(marker));
        }
        data.put("markers", markers);

        Set<EntityZdbIdDTO> expressionGenes = new TreeSet<>();
        for (ExpressionExperiment2 experiment : expressionRepository.getExperiments2(id)) {
            Marker gene = experiment.getGene();
            if (gene != null) {
                expressionGenes.add(DTOConversionService.convertToEntityZdbIdDTO(experiment.getGene()));
            }
        }
        data.put("expressionGenes", expressionGenes);

        Set<EntityZdbIdDTO> genotypes = new TreeSet<>();
        for (Genotype genotype : mutantRepository.getGenotypesForAttribution(id)) {
            if (!genotype.isWildtype()) {
                genotypes.add(DTOConversionService.convertToEntityZdbIdDTO(genotype));
            }
        }
        data.put("genotypes", genotypes);

        return data;
    }

    @ResponseBody
    @RequestMapping(value = "{id}/notification", method = RequestMethod.POST, produces = "text/plain")
    public String sendNotificationLetter(@PathVariable String id,
                                         @RequestBody NotificationLetter letter,
                                         HttpServletResponse response) {
        Person sender = ProfileService.getCurrentSecurityUser();
        if (sender == null || !sender.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Unauthorized";
        }

        MailSender mailer = AbstractZfinMailSender.getInstance();
        if (mailer == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "No mail sender";
        }

        // don't send to the real recipients unless we're on production
        if (!Boolean.valueOf(ZfinPropertiesEnum.SEND_AUTHOR_NOTIF_EMAIL.value())) {
            letter.getRecipients().clear();
        }

        // always send to the curator
        letter.getRecipients().add(sender.getEmail());

        // make sure all the expression data will appear on the all-figure-view page we
        // link to in the email
        InformixUtil.runInformixProcedure("regen_expression_mart_per_pub", id);

        boolean sent = mailer.sendHtmlMail(
                "ZFIN Author Notification",
                letter.getMessage(),
                false,
                sender.getFirstName() + " " + sender.getLastName() + " <" + sender.getEmail() + ">",
                letter.getRecipients().toArray(new String[] {}));

        if (!sent) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Not sent";
        }

        return "OK";
    }

}
