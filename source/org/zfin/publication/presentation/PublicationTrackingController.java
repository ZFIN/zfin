package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.CurationDTO;
import org.zfin.curation.presentation.CurationStatusDTO;
import org.zfin.curation.presentation.PublicationNoteDTO;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.util.*;

@Controller
@RequestMapping("/publication")
public class PublicationTrackingController {

    private final static Logger LOG = Logger.getLogger(PublicationTrackingController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PhenotypeRepository phenotypeRepository;

    @RequestMapping(value = "/{zdbID}/track")
    public String showPubTracker(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Track Pub: " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("hasFile", StringUtils.isNotEmpty(publication.getFileName()));
        model.addAttribute("loggedInUser", ProfileService.getCurrentSecurityUser().getZdbID());
        return "publication/track-publication.page";
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/notes", method = RequestMethod.GET)
    public Collection<PublicationNoteDTO> getPublicationNotes(@PathVariable String zdbID) {
        return getPubNotes(publicationRepository.getPublication(zdbID));
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
        tx.commit();
        
        return CurationDTOConversionService.publicationNoteToDTO(note);
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
        tx.commit();

        return CurationDTOConversionService.publicationNoteToDTO(note);
    }

    @ResponseBody
    @RequestMapping(value = "/notes/{zdbID}", method = RequestMethod.DELETE)
    public String deletePublicationNote(@PathVariable String zdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        PublicationNote note = (PublicationNote) session.get(PublicationNote.class, zdbID);
        session.delete(note);
        tx.commit();

        return "";
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/topics", method = RequestMethod.GET)
    public Collection<CurationDTO> getCurationTopics(@PathVariable String zdbID) {

        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(
                "from Curation c " +
                        "where c.publication = :pub " +
                        "and c.topic != :linkedAuthors");
        query.setParameter("pub", publicationRepository.getPublication(zdbID));
        query.setParameter("linkedAuthors", Curation.Topic.LINKED_AUTHORS);

        Set<CurationDTO> curation = new TreeSet<>(new Comparator<CurationDTO>() {
            @Override
            public int compare(CurationDTO o1, CurationDTO o2) {
                return o1.getTopic().compareTo(o2.getTopic());
            }
        });
        List<Curation.Topic> topics = new ArrayList<>();
        for (Object o : query.list()) {
            Curation c = (Curation) o;
            curation.add(CurationDTOConversionService.curationToDTO(c));
            topics.add(c.getTopic());
        }
        for (Curation.Topic t : Curation.Topic.values()) {
            if (!topics.contains(t) && t != Curation.Topic.LINKED_AUTHORS) {
                CurationDTO dto = new CurationDTO();
                dto.setTopic(t.toString());
                curation.add(dto);
            }
        }
        return curation;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/status", method = RequestMethod.GET)
    public CurationStatusDTO getCurationStatus(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);

        CurationStatusDTO dto = new CurationStatusDTO();
        dto.setClosedDate(publication.getCloseDate());
        dto.setIndexed(publication.isIndexed());
        dto.setIndexedDate(publication.getIndexedDate());
        dto.setPubZdbID(publication.getZdbID());
        dto.setCurationAllowed(PublicationService.allowCuration(publication));

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/status", method = RequestMethod.POST)
    public CurationStatusDTO updateCurationStatus(@PathVariable String zdbID, @RequestBody CurationStatusDTO dto) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        Publication publication = publicationRepository.getPublication(zdbID);
        publication.setIndexed(dto.isIndexed());
        publication.setIndexedDate((GregorianCalendar) dto.getIndexedDate());
        if (publication.getCloseDate() == null && dto.getClosedDate() != null) {
            // looks like this paper's getting closed. close all the topics, too.
            publicationRepository.closeCurationTopics(publication, ProfileService.getCurrentSecurityUser());
        }
        publication.setCloseDate((GregorianCalendar) dto.getClosedDate());
        session.update(publication);
        tx.commit();

        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/validate", method = RequestMethod.POST)
    public JSONMessageList validatePublication(@PathVariable String zdbID) {
        Collection<String> warnings = new ArrayList<>();

        List<Curation> openTopics = publicationRepository.getOpenCurationTopics(zdbID);
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
        if (CollectionUtils.isNotEmpty(phenotypeExperiments)) {
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
        tx.commit();

        return CurationDTOConversionService.curationToDTO(curation);
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
        tx.commit();

        return CurationDTOConversionService.curationToDTO(curation);
    }

    private Collection<PublicationNoteDTO> getPubNotes(Publication publication) {
        return CollectionUtils.collect(publication.getNotes(), new Transformer() {
            @Override
            public Object transform(Object o) {
                return CurationDTOConversionService.publicationNoteToDTO((PublicationNote) o);
            }
        });
    }

}
