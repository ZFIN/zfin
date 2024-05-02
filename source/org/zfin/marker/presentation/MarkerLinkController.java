package org.zfin.marker.presentation;

import org.apache.commons.collections4.CollectionUtils;
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
import org.zfin.gwt.marker.ui.SequenceValidator;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.BlastDatabaseAccessException;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.presentation.JSONMessageList;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.SequenceRepository;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.zfin.marker.service.MarkerService.addMarkerLinkByAccession;


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
//        DisplayGroup displayGroup = displayGroupRepository.getDisplayGroupByName(group);
        List<ReferenceDatabase> databases = displayGroupRepository.getReferenceDatabasesForDisplayGroup(group);
        return DTOConversionService.convertToReferenceDatabaseDTOs(databases, group);
    }

    @ResponseBody
    @RequestMapping("/{markerId}/link/{sequenceType}")
    public Collection<ReferenceDatabaseDTO> getNuclDatabases(@PathVariable String markerId, @PathVariable String sequenceType, @RequestParam(name = "group", required = true) String groupName) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        List<DisplayGroup.GroupName> groups = getGroupNamesForMarkerAndSequenceType(marker, sequenceType);

        return DTOConversionService.convertToReferenceDatabaseDTOs(
                displayGroupRepository.getReferenceDatabasesForDisplayGroup(
                        groups.toArray(new DisplayGroup.GroupName[0])));
    }

    private List<DisplayGroup.GroupName> getGroupNamesForMarkerAndSequenceType(Marker marker, String sequenceType) {
        List<DisplayGroup.GroupName> groups = new ArrayList<>();
        Marker.Type markerType = marker.getMarkerType().getType();
        boolean markerIsNtr = marker.getMarkerType().getTypeGroups().contains(Marker.TypeGroup.SEARCHABLE_NON_TRANSCRIBED_REGION);

        if (sequenceType.contains("Nucleotide")) {
            switch (markerType) {
                case GENE:
                    groups.add(DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);
                    groups.add(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);
                    break;
                case TSCRIPT:
                    groups.add(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
                    break;
                case RRNAG, LINCRNAG, LNCRNAG, MIRNAG, PIRNAG,
                        SCRNAG, SNORNAG, TRNAG, NCRNAG, SRPRNAG:
                    groups.add(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);
                    break;
                default:
                    if (markerIsNtr) {
                        groups.add(DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);
                        groups.add(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);
                    }
            }
        } else if (sequenceType.contains("Protein")) {
            groups.add(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_PROTEIN_SEQUENCE);
        }
        return groups;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/links", method = RequestMethod.GET)
    public List<LinkDisplay> getMarkerLinks(@PathVariable String markerId,
                                            @RequestParam(name = "group", required = true) String groupName) {
        return MarkerService.getMarkerLinksForDisplayGroup(markerId, groupName, false);
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/{type}/links", method = RequestMethod.GET)
    public List<LinkDisplay> getMarkerSeqLinks(@PathVariable String markerId, @PathVariable String type,
                                               @RequestParam(name = "group", required = true) String groupName) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        List<DisplayGroup.GroupName> groupNames = getGroupNamesForMarkerAndSequenceType(marker, type);

        List<LinkDisplay> links = new ArrayList<>();
        for (DisplayGroup.GroupName group : groupNames) {
            links.addAll(markerRepository.getMarkerDBLinksFast(marker, group));
        }
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
        HibernateUtil.createTransaction();

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid link", errors);
        }

        Integer length = null;
        String accessionLengthInput = newLink.getLength();
        if (StringUtils.isNotEmpty(accessionLengthInput)) {
            int len = 0;
            try {
                len = Integer.parseInt(accessionLengthInput);
                length = len;
            } catch (NumberFormatException e) {
                errors.addError(new FieldError("length", accessionLengthInput, "Invalid Length number"));
                throw new InvalidWebRequestException("Invalid length number", errors);
            }
        }

        Marker marker = markerRepository.getMarkerByID(markerId);
        String accessionNo = newLink.getAccession();
        ReferenceDatabase refDB = sequenceRepository.getReferenceDatabaseByID(newLink.getReferenceDatabaseZdbID());

        //validate accession format
        if (!refDB.isValidAccessionFormat(accessionNo)) {
            Optional<String> message = refDB.getValidationFailedMessage(accessionNo);
            errors.addError(new FieldError("accession", accessionNo, message.orElse("Invalid accession number")));
            throw new InvalidWebRequestException(message.orElse("Invalid accession number"), errors);
        }

        List<String> referenceIDs = newLink.getReferences().stream()
                .map(MarkerReferenceBean::getZdbID)
                .collect(Collectors.toList());

        try {
            DBLink link = addMarkerLinkByAccession(marker, accessionNo, refDB, referenceIDs, length);
            HibernateUtil.flushAndCommitCurrentSession();
            return getLinkDisplayById(link.getZdbID());
        } catch (InvalidWebRequestException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new InvalidWebRequestException("Error adding link");
        }
    }


    //new method for adding protein sequence
    public String getSequenceAsString(String seq) {
        char[] chars = seq.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char aChar : chars) {
            if (Character.isLetter(aChar)) {
                buffer.append(aChar);
            } else if (aChar == '-' || aChar == '*') {
                buffer.append(aChar);
            }
        }

        return buffer.toString().toUpperCase();
    }

    /**
     * Add new nucleotide sequence to markerId by posting sequence data of the form:
     *
     * {
     *   "data": "TTACAATTAAAGGATATTTCTTGCGGCTGAATACGAGAACAGAAATGTCCCTTAATTGTTTGGTT",
     *   "referenceDatabaseZdbID": "ZDB-FDBCONT-090929-4",
     *   "references": [
     *     {
     *       "zdbID": "ZDB-PUB-140520-12"
     *     }
     *   ]
     * }
     *
     * eg. curl -X POST --header 'Content-Type: application/json' --data '{"data": "TTACAATTAAAGGATATTTCTTGCGGCTGAATACGAGAACAGAAATGTCCCTTAATTGTTTGGTT", "referenceDatabaseZdbID": "ZDB-FDBCONT-090929-4", "references": [{"zdbID": "ZDB-PUB-140520-12"}]}' https://{site}.zfin.org/action/marker/ZDB-GENE-980526-166/Nucleotide/seqLinks
     *
     * @param markerId The marker ID to add the sequence to.
     * @param sequenceType (String) The type of sequence to add--either "Protein" or "Nucleotide".
     * @param newLink The sequence data to add (deserialized from json as described above).
     * @param errors The binding result to check for errors.
     * @return The DBLink for the created sequence. Serialized to json like: {"name":"ZFINNUCL0000006195"}
     */
    @ResponseBody
    @RequestMapping(value = "/{markerId}/{sequenceType}/seqLinks", method = RequestMethod.POST)
    public LinkDisplay addGeneSeqLink(@PathVariable String markerId, @PathVariable String sequenceType,
                                      @Valid @RequestBody SequenceFormBean newLink,
                                      BindingResult errors) throws Exception {
        Marker marker = null;

        ReferenceDatabase refDB = null;
        String sequenceStr = null;
        Sequence sequence = null;
        DBLink link = null;


        if (!errors.hasErrors()) {
            marker = markerRepository.getMarkerByID(markerId);
            refDB = sequenceRepository.getReferenceDatabaseByID(newLink.getReferenceDatabaseZdbID());
        }


        if (link == null) {
            Iterator<Publication> publicationIterator = newLink.getReferences().iterator();

            HibernateUtil.createTransaction();

            try {

                sequenceStr = getSequenceAsString(newLink.getData()).toUpperCase();
                if (sequenceType.equals("Protein")) {
                    int invalidSequenceCharacter = SequenceValidator.validatePolypeptideSequence(sequenceStr);
                    if (invalidSequenceCharacter != SequenceValidator.NOT_FOUND) {
                        errors.reject("Letter " + "[" + sequenceStr.substring(invalidSequenceCharacter, invalidSequenceCharacter + 1) + "]" + " at position " + (invalidSequenceCharacter + 1) + " is not a valid protein symbol.");
                    }


                    sequence = MountedWublastBlastService.getInstance().addProteinToMarker(marker, sequenceStr, publicationIterator.next().getZdbID(), refDB);
                } else {
                    int invalidSequenceCharacter = SequenceValidator.validateNucleotideSequence(sequenceStr);
                    if (invalidSequenceCharacter != SequenceValidator.NOT_FOUND) {
                        errors.reject("Letter " + "[" + sequenceStr.substring(invalidSequenceCharacter, invalidSequenceCharacter + 1) + "]" + " at position " + (invalidSequenceCharacter + 1) + " is not a valid nucleotide symbol.");
                    }


                    sequence = MountedWublastBlastService.getInstance().addSequenceToMarker(marker, sequenceStr, publicationIterator.next().getZdbID(), refDB);
                }


                link = sequence.getDbLink();
                while (publicationIterator.hasNext()) {
                    markerRepository.addDBLinkAttribution(link, publicationIterator.next(), marker);
                }

                String updateComment = "Adding " + sequenceType + " sequence " + link.getAccessionNumber() + " to " + marker.getAbbreviation();
                InfrastructureService.insertUpdate(marker, updateComment);

            } catch (Exception e) {
                LOG.error("Failure to add internal protein sequence", e);
                LOG.info("fail");


                throw new BlastDatabaseAccessException("Failed to add  sequence.", e);
            }


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

        ReferenceDatabase database = sequenceRepository.getReferenceDatabaseByID(updatedLink.getReferenceDatabaseZdbID());
        ;
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
    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE)
    public LinkDisplay deleteMarkerLink(@PathVariable String linkId) {
        LinkDisplay linkDisplay = getLinkDisplayById(linkId);
        HibernateUtil.createTransaction();
        DBLink link = sequenceRepository.getDBLinkByID(linkId);
        Marker marker = markerRepository.getMarker(link.getDataZdbID());
        sequenceRepository.deleteReferenceProteinByDBLinkID(linkId);
        sequenceRepository.removeDBLinks(Collections.singletonList(link));

        String publicationIDs = link.getPublicationIds();
        String updateComment = "Deleting dblink " + linkDisplay.getDisplayName() + (isEmpty(publicationIDs) ? "" : " with attributions " + publicationIDs);
        InfrastructureService.insertUpdate(marker, updateComment);

        HibernateUtil.flushAndCommitCurrentSession();
        return linkDisplay;
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
        if (linkDisplays.size() == 0) {
            LOG.error("zero LinkDisplays returned for " + linkId);
            return null;
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
