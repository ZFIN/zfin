package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.*;
import org.zfin.curation.repository.CurationRepository;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.database.InformixUtil;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.marker.repository.MarkerRepository;
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
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationTrackingController {

    private final static Logger LOG = LogManager.getLogger(PublicationTrackingController.class);

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

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @RequestMapping(value = "/{zdbID}/track")
    public String showPubTracker(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Track Pub: " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("allowCuration", publicationService.allowCuration(publication));
        model.addAttribute("hasCorrespondence", publicationService.hasCorrespondence(publication));
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
        SortedSet<PersonDTO> curatorDTOs = profileRepository.getCurators().stream()
                .map(converter::toPersonDTO)
                .collect(Collectors.toCollection(TreeSet::new));
        SortedSet<PersonDTO> studentsDTOs = profileRepository.getStudents().stream()
                .map(converter::toPersonDTO)
                .collect(Collectors.toCollection(TreeSet::new));
        curatorDTOs.addAll(studentsDTOs);
        // Add currently logged in user to allow developers to act like curators on their own sites
        curatorDTOs.add(converter.toPersonDTO(ProfileService.getCurrentSecurityUser()));
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
    public CurationStatusDTO updateCurationStatus(@PathVariable String zdbID,
                                                  @RequestParam(required = false, defaultValue = "false") Boolean checkOwner,
                                                  @RequestBody CurationStatusDTO dto) throws InvalidWebRequestException {
        Publication publication = publicationRepository.getPublication(zdbID);
        PublicationTrackingHistory pth = publicationRepository.currentTrackingStatus(publication);

        if (checkOwner && pth.getOwner() != null && dto.getOwner() != null && !Objects.equals(pth.getOwner().getZdbID(), dto.getOwner().getZdbID())) {
            throw new InvalidWebRequestException("Pub already claimed");
        }

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
            if (newStatus.getStatus().getName() != PublicationTrackingStatus.Name.CLOSED_PARTIALLY_CURATED) {
                curationRepository.closeCurationTopics(publication, ProfileService.getCurrentSecurityUser());
            }
            expressionRepository.deleteExpressionStructuresForPub(publication);
            publicationRepository.deleteExpressionExperimentIDswithNoExpressionResult(publication);
            mutantRepository.updateGenotypeNicknameWithHandleForPublication(publication);
        }
        session.save(newStatus);
        tx.commit();

        // refresh to get fully populated status and location objects
        session.refresh(newStatus);
        return converter.toCurationStatusDTO(publicationRepository.currentTrackingStatus(publication));
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/indexed", method = RequestMethod.GET)
    public IndexedStatusDTO getIndexedStatus(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        return converter.toIndexedStatusDTO(publication);
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/indexed", method = RequestMethod.POST)
    public IndexedStatusDTO setIndexedStatus(@PathVariable String zdbID, @RequestBody IndexedStatusDTO dto) {
        Publication publication = publicationRepository.getPublication(zdbID);

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        publication.setIndexed(dto.isIndexed());
        publication.setIndexedDate(new GregorianCalendar());
        publication.setIndexedBy(profileRepository.getPerson(dto.getIndexer().getZdbID()));
        session.save(publication);
        tx.commit();

        return converter.toIndexedStatusDTO(publication);
    }

    @RequestMapping(value = "/{zdbID}/status-history")
    public String showPubStatusHistory(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<PublicationEvent> events = new ArrayList<>();
        events.addAll(publicationRepository.fullTrackingHistory(publication));
        if (publication.isIndexed()) {
            events.add(new IndexedEvent(publication));
        }
        events.sort(Comparator.comparing(PublicationEvent::getDate).reversed());

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Status History for " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("events", events);
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

        List<Feature> zebrashareFeatures = zebrashareRepository.getZebraShareFeatureForPub(zdbID);
        List<String> zebrashareWarnings = new ArrayList<>();
        for (Feature feature : zebrashareFeatures) {
            FeatureCommunityContribution communityContribution = zebrashareRepository.getLatestCommunityContribution(feature);
            if (communityContribution == null || communityContribution.getFunctionalConsequence() == null) {
                zebrashareWarnings.add(feature.getName() + " Functional Consequence");
            }
            if (communityContribution == null || communityContribution.getAdultViable() == null) {
                zebrashareWarnings.add(feature.getName() + " Adult Viable");
            }
            if (communityContribution == null || communityContribution.getMaternalZygosityExamined() == null) {
                zebrashareWarnings.add(feature.getName() + " Maternal Zygocity Examined");
            }
            if (communityContribution == null || communityContribution.getCurrentlyAvailable() == null) {
                zebrashareWarnings.add(feature.getName() + " Currently Available");
            }
        }
        if (CollectionUtils.isNotEmpty(zebrashareWarnings)) {
            warnings.add("The following Zebrashare features have missing details: <br>" + StringUtils.join(zebrashareWarnings, "<br>"));
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
            try {
                if (dto.isResend()) {
                    correspondence = publicationRepository.addResentCorrespondence(publication, dto);
                } else {
                    correspondence = publicationRepository.addSentCorrespondence(publication, dto);
                }
            } catch (Exception e) {
                LOG.error("error saving correspondence", e);
                throw new InvalidWebRequestException("error saving correspondence");
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
                message = "[[ Recipient list on production environment: " + String.join(", ", externalRecipients) + " ]]\n\n" +
                        message;
            }

            String sender = correspondence.getFrom().getFirstName() + " " +
                    correspondence.getFrom().getLastName() +
                    " <" + correspondence.getFrom().getEmail() + ">";

            boolean sent = mailer.sendMail(correspondence.getMessage().getSubject(),
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
    public String deleteCorrespondence(@PathVariable long id, @RequestParam boolean outgoing) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        Class messageClass = outgoing ? CorrespondenceSentMessage.class : CorrespondenceReceivedMessage.class;
        Object message = session.get(messageClass, id);
        session.delete(message);

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
    public List<DataLinkBean> getCuratedData(@PathVariable String id) {
        Publication publication = publicationRepository.getPublication(id);
        return publicationService.getPublicationDataLinks(publication);
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
        InformixUtil.runProcedure("regen_expression_mart_per_pub", id);

        boolean sent = mailer.sendHtmlMail(
                "ZFIN Author Notification",
                letter.getMessage(),
                false,
                sender.getFirstName() + " " + sender.getLastName() + " <" + sender.getEmail() + ">",
                letter.getRecipients().toArray(new String[]{}));

        if (!sent) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Not sent";
        }

        return "OK";
    }

}
