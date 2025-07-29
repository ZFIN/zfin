package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.ExternalNote;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Controller
@RequestMapping("/marker")
public class MarkerNotesController {
    private Logger logger = LogManager.getLogger(SequenceViewController.class);

    @Autowired
    private AntibodyRepository antibodyRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping("/note/phenotype")
    public String getPhenotypeSelectNote() {
        return "marker/phenotype-note";
    }

    @RequestMapping("/note/phenotype-summary-note")
    public String getPhenotypeSummarySelectNote() {
        return "marker/phenotype-summary-note";
    }

    @RequestMapping("/note/omim-phenotype")
    public String getOmimPhenotypeNote() {
        return "marker/omim-phenotype-note";
    }

    @RequestMapping("/note/transcripts")
    public String getTranscriptNote() {
        return "marker/transcript-note";
    }

    @RequestMapping("/note/sequences")
    public String geSequencesNote() {
        return "marker/sequences-note";
    }

    @RequestMapping("/note/markerrelationships")
    public String getMarkerRelationshipNote() {
        return "marker/markerrelationships-note";
    }

    @RequestMapping("/note/antibodies")
    public String getAntibodyNote() {
        return "marker/antibody-note";
    }

    @RequestMapping("/note/orthology")
    public String getOrthologyNote() {
        return "marker/orthology-note";
    }

    @RequestMapping("/note/citations")
    public String getCitationsNote() {
        return "marker/citations-note";
    }

    @RequestMapping("/note/proteins")
    public String getProteinNote() {
        return "marker/protein-note";
    }

    @RequestMapping("/note/mutants")
    public String getMutantsNote() {
        return "marker/mutants-note";
    }

    @RequestMapping("/note/str")
    public String getSTRNote() {
        return "marker/str-note";
    }

    @RequestMapping("/note/construct")
    public String getConstructNote() {
        return "marker/construct-note";
    }

    @RequestMapping("/note/go")
    public String getGoNote() {
        return "marker/go-note";
    }

    @RequestMapping("/note/automated-gene-desc")
    public String getGeneDescription() {
        return "marker/automated-gene-desc-note";
    }

    @RequestMapping("/note/annotation-status-desc")
    public String getAnnotationStatus() {
        return "marker/annotation-status-desc-note";
    }

    @RequestMapping("/note/disease-model")
    public String getDiseaseModelNote() {
        return "marker/disease-model-note";
    }

    @RequestMapping("/note/sequence-targeting-reagent-gbrowse")
    public String getSTRGbrowseNote() {
        return "marker/sequence-targeting-reagent-gbrowse-note";
    }

    @RequestMapping("/gene-product-description/{zdbID}")
    public String getGeneProducts(
            @PathVariable String zdbID
            , Model model
    ) {
        List<GeneProductsBean> geneProductsBeans = markerRepository.getGeneProducts(zdbID);
        Marker marker = markerRepository.getMarkerByID(zdbID);

        if (marker == null) {
            marker = getReplacedMarker(zdbID);
        }

        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_POPUP;
        } else {
            geneProductsBeans = markerRepository.getGeneProducts(marker.getZdbID());
        }

        if (geneProductsBeans == null) {
            geneProductsBeans = new ArrayList<GeneProductsBean>();
        } else {
            for (GeneProductsBean geneProductsBean : geneProductsBeans) {
                geneProductsBean.setMarker(marker);
            }
        }
        model.addAttribute(LookupStrings.FORM_BEAN, geneProductsBeans);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Gene Products: " + marker.getAbbreviation());
        return "marker/gene-product-description";
    }

    @RequestMapping("/snp-publication-list")
    public String snpPublicationListHandler(
            @RequestParam("markerID") String zdbID
            , @RequestParam String orderBy
            , Model model
    ) {
        SNPBean bean = new SNPBean();
        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (marker == null) {
            marker = getReplacedMarker(zdbID);
        }

        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        bean.setMarker(marker);

        // TODO: make this method suck less
        // TODO: implement order by
        List<String> pubIDs = RepositoryFactory.getPublicationRepository().getSNPPublicationIDs(bean.getMarker());
        Set<Publication> pubs = new HashSet<Publication>();
        for (String id : pubIDs) {
            pubs.add(RepositoryFactory.getPublicationRepository().getPublication(id));
        }

        bean.setPublications(pubs);
        model.addAttribute(LookupStrings.FORM_BEAN, bean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "SNP Publication List");

        return "marker/snp-publication-list";
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/notes", method = RequestMethod.GET)
    public List<? super NoteDTO> getMarkerNotes(@PathVariable String markerId) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        List<? super NoteDTO> notes = DTOMarkerService.getCuratorNoteDTOs(marker);
        notes.add(DTOMarkerService.getPublicNoteDTO(marker));
        if (marker instanceof Antibody) {
            notes.addAll(DTOMarkerService.getExternalNoteDTOs((Antibody) marker));
        }
        return notes;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/public-note", method = RequestMethod.POST)
    public NoteDTO updatePublicNote(@PathVariable String markerId,
                                    @RequestBody NoteDTO noteDTO) {
        Marker marker = markerRepository.getMarkerByID(markerId);

        HibernateUtil.createTransaction();
        markerRepository.updateMarkerPublicNote(marker, noteDTO.getNoteData());
        NoteDTO newNote = DTOMarkerService.getPublicNoteDTO(marker);
        HibernateUtil.flushAndCommitCurrentSession();

        return newNote;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/curator-notes", method = RequestMethod.POST)
    public CuratorNoteDTO addCuratorNote(@PathVariable String markerId,
                                         @RequestBody NoteDTO noteDTO) {
        Marker marker = markerRepository.getMarkerByID(markerId);

        HibernateUtil.createTransaction();
        DataNote note = markerRepository.addMarkerDataNote(marker, noteDTO.getNoteData());
        CuratorNoteDTO newNote = DTOMarkerService.convertToCuratorNoteDto(note, marker);
        HibernateUtil.flushAndCommitCurrentSession();

        return newNote;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/curator-notes/{noteID}", method = RequestMethod.POST)
    public CuratorNoteDTO updateCuratorNote(@PathVariable String markerId,
                                            @PathVariable String noteID,
                                            @RequestBody NoteDTO noteDTO) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DataNote note = infrastructureRepository.getDataNoteByID(noteID);

        HibernateUtil.createTransaction();
        markerRepository.updateCuratorNote(marker, note, noteDTO.getNoteData());
        HibernateUtil.flushAndCommitCurrentSession();

        return DTOMarkerService.convertToCuratorNoteDto(note, marker);
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/curator-notes/{noteID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deleteCuratorNote(@PathVariable String markerId,
                                    @PathVariable String noteID) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DataNote note = infrastructureRepository.getDataNoteByID(noteID);

        HibernateUtil.createTransaction();
        markerRepository.removeCuratorNote(marker, note);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/external-notes", method = RequestMethod.POST)
    public NoteDTO addExternalNote(@PathVariable String markerId,
                                   @RequestParam String type,
                                   @RequestBody NoteDTO noteDTO) {
        switch (type) {
            case "antibody":
                Antibody antibody = antibodyRepository.getAntibodyByID(markerId);
                HibernateUtil.createTransaction();
                ExternalNote note = markerRepository.addAntibodyExternalNote(antibody, noteDTO.getNoteData(), noteDTO.getPublicationZdbID());
                HibernateUtil.flushAndCommitCurrentSession();
                return DTOMarkerService.convertToNoteDTO(note);
            default:
                throw new InvalidWebRequestException("Unhandled external note type: " + type);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/external-notes/{noteID}", method = RequestMethod.POST)
    public NoteDTO updateExternalNote(@PathVariable String noteID,
                                              @RequestBody NoteDTO noteDTO) {
        ExternalNote note = infrastructureRepository.getExternalNoteByID(noteID);
        Publication publication = publicationRepository.getPublication(noteDTO.getPublicationZdbID());

        HibernateUtil.createTransaction();
        note = infrastructureRepository.updateExternalNote(note, noteDTO.getNoteData(), publication);
        HibernateUtil.flushAndCommitCurrentSession();

        return DTOMarkerService.convertToNoteDTO(note);
    }

    @ResponseBody
    @RequestMapping(value = "/external-notes/{noteID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String deleteExternalNote(@PathVariable String noteID) {
        ExternalNote note = infrastructureRepository.getExternalNoteByID(noteID);

        HibernateUtil.createTransaction();
        infrastructureRepository.deleteExternalNote(note);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    private Marker getReplacedMarker(String markerZdbId) {
        String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(markerZdbId);
        Marker replacedMarker = null;
        if (replacedZdbID != null) {
            logger.debug("found a replaced zdbID for: " + markerZdbId + "->" + replacedZdbID);
            replacedMarker = markerRepository.getMarkerByID(replacedZdbID);
        }
        return replacedMarker;
    }
}

