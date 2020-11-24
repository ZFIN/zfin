package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.SequenceRepository;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marker")
public class MarkerLinkController {

    private static Logger LOG = LogManager.getLogger(MarkerLinkController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private DisplayGroupRepository displayGroupRepository;

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @InitBinder("linkDisplay")
    public void initLinkBinder(WebDataBinder binder) {
        binder.setValidator(new LinkDisplayValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }


    @ResponseBody
    @RequestMapping("/link/databases")
    public Collection<ReferenceDatabaseDTO> getLinkDatabases(@RequestParam(name = "group", required = true) String groupName) {
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);
        List<ReferenceDatabase> databases = displayGroupRepository.getReferenceDatabasesForDisplayGroup(group);
        List<ReferenceDatabaseDTO> databaseNames = new ArrayList<>(databases.size());
        for (ReferenceDatabase database : databases) {
            databaseNames.add(DTOConversionService.convertToReferenceDatabaseDTO(database));
        }
        return databaseNames;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/links", method = RequestMethod.GET)
    public List<LinkDisplay> getMarkerLinks(@PathVariable String markerId,
                                            @RequestParam(name = "group", required = true) String groupName) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);

        List<LinkDisplay> links = markerRepository.getMarkerDBLinksFast(marker, group);
        if (groupName.equals(DisplayGroup.GroupName.OTHER_MARKER_PAGES.toString())) {
            links.addAll(markerRepository.getVegaGeneDBLinksTranscript(marker, DisplayGroup.GroupName.SUMMARY_PAGE));
        }
        return links;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/links", method = RequestMethod.POST)
    public LinkDisplay addMarkerLink(@PathVariable String markerId,
                                     @Valid @RequestBody LinkDisplay newLink,
                                     BindingResult errors) {
        Marker marker = null;
        String accessionNo = null;
        ReferenceDatabase refDB = null;

        DBLink link = null;

        HibernateUtil.createTransaction();

        if (!errors.hasErrors()) {
            marker = markerRepository.getMarkerByID(markerId);
            accessionNo = newLink.getAccession();
            refDB = sequenceRepository.getReferenceDatabaseByID(newLink.getReferenceDatabaseZdbID());

            Collection<? extends DBLink> links = marker.getDbLinks();
            if (CollectionUtils.isNotEmpty(links)) {
                for (DBLink dbLink : marker.getDbLinks()) {
                    if (dbLink.getReferenceDatabase().equals(refDB) && dbLink.getAccessionNumber().equals(accessionNo)) {
                        errors.reject("marker.link.duplicate");
                    }
                }
            }
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker DBLink", errors);
        }

        Iterator<MarkerReferenceBean> references = newLink.getReferences().iterator();

        if (link == null) {
            final String length = newLink.getLength();
            String pubId = references.next().getZdbID();
            if (StringUtils.isNotEmpty(length)) {
                int len = 0;
                try {
                    len = Integer.parseInt(length);
                } catch (NumberFormatException e) {
                    errors.addError(new FieldError("length", length, "Invalid Length number"));
                    throw new InvalidWebRequestException("Invalid length number", errors);
                }
                link = markerRepository.addDBLinkWithLenth(marker, accessionNo, refDB, pubId, len);
            } else {
                link = markerRepository.addDBLink(marker, accessionNo, refDB, pubId);
            }
        }
        while (references.hasNext()) {
            Publication publication = publicationRepository.getPublication(references.next().getZdbID());
            markerRepository.addDBLinkAttribution(link, publication, marker);
        }
        HibernateUtil.flushAndCommitCurrentSession();

        return getLinkDisplayById(link.getZdbID());
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.POST)
    public LinkDisplay updateMarkerLink(@PathVariable String linkId,
                                        @Valid @RequestBody LinkDisplay updatedLink,
                                        BindingResult errors) {
        DBLink link = sequenceRepository.getDBLinkByID(linkId);
        boolean accessionUpdated = !StringUtils.equals(link.getAccessionNumber(), updatedLink.getAccession());
        boolean databaseUpdated = !StringUtils.equals(link.getReferenceDatabase().getZdbID(), updatedLink.getReferenceDatabaseZdbID());

        ReferenceDatabase database = sequenceRepository.getReferenceDatabaseByID(updatedLink.getReferenceDatabaseZdbID());;
        if (!errors.hasErrors() && (accessionUpdated || databaseUpdated)) {
            DBLink existingLink = sequenceRepository.getDBLinkByAlternateKey(updatedLink.getAccession(), link.getDataZdbID(), database);
            if (existingLink != null) {
                errors.reject("marker.link.duplicate");
            }
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker DBLink", errors);
        }

        List<String> currentPublicationIds = link.getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .map(Publication::getZdbID)
                .collect(Collectors.toList());
        List<String> updatedPublicationIds = updatedLink.getReferences().stream()
                .map(MarkerReferenceBean::getZdbID)
                .collect(Collectors.toList());
        Collection<String> publicationsToAdd = CollectionUtils.subtract(updatedPublicationIds, currentPublicationIds);
        Collection<String> publicationsToRemove = CollectionUtils.subtract(currentPublicationIds, updatedPublicationIds);

        HibernateUtil.createTransaction();
        link.setAccessionNumber(updatedLink.getAccession());
        link.setReferenceDatabase(database);
        if (StringUtils.isNotEmpty(updatedLink.getLength())) {
            link.setLength(Integer.parseInt(updatedLink.getLength()));
        }
        for (String pubId : publicationsToAdd) {
            Publication publication = publicationRepository.getPublication(pubId);
            markerRepository.addDBLinkAttribution(link, publication, link.getDataZdbID());
        }
        for (String pubId : publicationsToRemove) {
            infrastructureRepository.deleteRecordAttribution(linkId, pubId);
        }
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().evict(link);
        return getLinkDisplayById(link.getZdbID());
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deleteMarkerLink(@PathVariable String linkId) {
        DBLink link = sequenceRepository.getDBLinkByID(linkId);
        sequenceRepository.removeDBLinks(Collections.singletonList(link));
        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}/references", method = RequestMethod.POST)
    public LinkDisplay addLinkReference(@PathVariable String linkId,
                                        @Valid @RequestBody MarkerReferenceBean newReference,
                                        BindingResult errors) {
        if (infrastructureRepository.getRecordAttribution(linkId, newReference.getZdbID(), RecordAttribution.SourceType.STANDARD) != null) {
            errors.rejectValue("zdbID", "marker.reference.inuse");
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid reference", errors);
        }

        MarkerDBLink dbLink = markerRepository.getMarkerDBLink(linkId);
        Publication publication = publicationRepository.getPublication(newReference.getZdbID());

        HibernateUtil.createTransaction();
        markerRepository.addDBLinkAttribution(dbLink, publication, dbLink.getMarker());
        HibernateUtil.flushAndCommitCurrentSession();

        return getLinkDisplayById(linkId);
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}/references/{pubID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String removeLinkReference(@PathVariable String linkId,
                                      @PathVariable String pubID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteRecordAttribution(linkId, pubID);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    private LinkDisplay getLinkDisplayById(String linkId) {
        // this is kinda weird because LinkDisplay is only produced by a query transformer
        List<LinkDisplay> linkDisplays = markerRepository.getMarkerLinkDisplay(linkId);
        if (linkDisplays.size() > 1) {
            LOG.error("too many LinkDisplays returned for " + linkId);
        }
        return linkDisplays.get(0);
    }

    @ResponseBody
    @RequestMapping(value = "/link/reference/{zdbID}/validate", method = RequestMethod.POST)
    public JSONMessageList validateReference(@PathVariable String zdbID) {

        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        Collection<String> errors = new ArrayList<>();
        JSONMessageList messages = new JSONMessageList();

        if (publication == null) {
            String error = "Invalid reference";
            errors.add(error);
            messages.setErrors(errors);
        }
        return messages;
    }

}
