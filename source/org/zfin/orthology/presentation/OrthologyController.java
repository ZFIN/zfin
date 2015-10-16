package org.zfin.orthology.presentation;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.NcbiOtherSpeciesGeneDTO;
import org.zfin.gwt.root.dto.OrthologDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.orthology.service.OrthologService;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.*;

@Controller
public class OrthologyController {

    @Autowired
    OrthologService orthologService;

    @RequestMapping(value = "/ortholog/{orthoID}/citation-list")
    public String orthologCitationList(@PathVariable String orthoID,
                                       @RequestParam(value = "evidenceCode", required = true) String evidenceCode,
                                       @RequestParam(value = "orderBy", required = false) String orderBy,
                                       Model model) {
        Ortholog ortholog = getOrthologyRepository().getOrtholog(orthoID);
        if (ortholog == null) {
            return LookupStrings.idNotFound(model, orthoID);
        }

        EvidenceCode code = getOrthologyRepository().getEvidenceCode(evidenceCode);
        if (code == null) {
            return LookupStrings.ERROR_PAGE;
        }

        OrthologPublicationListBean bean = new OrthologPublicationListBean();
        bean.setOrtholog(ortholog);
        bean.setEvidenceCode(code);
        if (StringUtils.isNotEmpty(orderBy)) {
            bean.setOrderBy(orderBy);
        }
        model.addAttribute("formBean", bean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "publication list");
        return "orthology/ortholog-publication-list.page";
    }

    @ResponseBody
    @RequestMapping(value = "/ortholog/evidence-codes", method = RequestMethod.GET)
    public List<EvidenceCode> listOrthologyEvidenceCodes() {
        return getOrthologyRepository().getEvidenceCodes();
    }

    @RequestMapping(value = "/gene/{geneID}/orthologs", method = RequestMethod.GET)
    public
    @ResponseBody
    List<OrthologDTO> listOrthologs(@PathVariable String geneID) throws InvalidWebRequestException {
        Marker gene = getMarkerRepository().getMarkerByID(geneID);
        if (gene == null)
            throw new InvalidWebRequestException("No gene with ID " + geneID + " found!", null);
        List<Ortholog> orthologList = getOrthologyRepository().getOrthologs(gene);
        List<OrthologDTO> orthologDTOs = new ArrayList<>(orthologList.size());
        for (Ortholog ortholog : orthologList) {
            OrthologDTO orthologDTO = DTOConversionService.convertToOrthologDTO(ortholog);
            orthologDTOs.add(orthologDTO);
        }
        return orthologDTOs;
    }

    @RequestMapping(value = "/gene/{geneID}/ortholog/{orthoID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    String deleteOrtholog(@PathVariable String geneID,
                          @PathVariable String orthoID) throws InvalidWebRequestException {
        Transaction tx = HibernateUtil.createTransaction();
        try {
            tx.begin();
            Marker gene = getMarkerRepository().getMarkerByID(geneID);
            if (gene == null)
                throw new InvalidWebRequestException("No gene with ID " + geneID + " found!", null);

            Ortholog ortholog = getOrthologyRepository().getOrtholog(orthoID);
            if (ortholog == null)
                throw new InvalidWebRequestException("No Ortholog with ID " + orthoID + " found!", null);
            if (!ortholog.getZebrafishGene().equals(gene))
                throw new InvalidWebRequestException("No Ortholog with ID " + orthoID + " found that matches " +
                        " zebrafish gene " + geneID, null);
            getOrthologyRepository().deleteOrtholog(ortholog);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new InvalidWebRequestException("Error while deleting Ortholg: " + orthoID + ":" +
                    e.getMessage(), null);
        }
        return "Successfully deleted " + orthoID;
    }

    @RequestMapping(value = "/gene/ncbi/{ncbiID}", method = RequestMethod.GET)
    public
    @ResponseBody
    NcbiOtherSpeciesGeneDTO getNcbiOrtholog(@PathVariable String ncbiID) throws InvalidWebRequestException {
        NcbiOtherSpeciesGene ncbiGene = getOrthologyRepository().getNcbiGene(ncbiID);
        if (ncbiGene == null)
            throw new InvalidWebRequestException("No NCBI ncbiGene with ID " + ncbiID + " found!", null);
        return DTOConversionService.convertToNcbiOtherSpeciesGeneDTO(ncbiGene);
    }

    @RequestMapping(value = "/gene/{geneID}/ortholog/ncbi/{ncbiID}", method = RequestMethod.POST)
    public
    @ResponseBody
    OrthologDTO createOrthologFromNcbi(@PathVariable String geneID,
                                       @PathVariable String ncbiID) throws InvalidWebRequestException {
        Transaction tx = HibernateUtil.createTransaction();
        Ortholog ortholog;
        try {
            tx.begin();
            NcbiOtherSpeciesGene ncbiGene = getOrthologyRepository().getNcbiGene(ncbiID);
            if (ncbiGene == null)
                throw new InvalidWebRequestException("No NCBI ncbiGene with ID " + ncbiID + " found!", null);
            Marker gene = getMarkerRepository().getMarkerByID(geneID);
            if (gene == null)
                throw new InvalidWebRequestException("No Zebrafish gene with ID " + geneID + " found!", null);
            Ortholog existingOrtholog = getOrthologyRepository().getOrthologByGeneAndNcbi(gene, ncbiGene);
            if (existingOrtholog != null)
                throw new InvalidWebRequestException("Ortholog with ID (" + ncbiID + ", " + geneID + ") already exists.  " +
                        existingOrtholog.getZdbID(), null);
            ortholog = new Ortholog();
            ortholog.setZebrafishGene(gene);
            ortholog.setNcbiOtherSpeciesGene(ncbiGene);
            List<NcbiOrthoExternalReference> ncbiOrthoExternalReferenceList = getOrthologyRepository().getNcbiExternalReferenceList(ncbiID);
            Set<OrthologExternalReference> referenceList = new HashSet<>(ncbiOrthoExternalReferenceList.size());
            for (NcbiOrthoExternalReference ref : ncbiOrthoExternalReferenceList) {
                OrthologExternalReference orthoRef = new OrthologExternalReference();
                orthoRef.setAccessionNumber(ncbiID);
                orthoRef.setOrtholog(ortholog);
                orthoRef.setReferenceDatabase(ref.getReferenceDatabase());
                referenceList.add(orthoRef);
            }
            ortholog.setExternalReferenceList(referenceList);
            getOrthologyRepository().saveOrthology(ortholog, null);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new InvalidWebRequestException("Error while creating an Ortholog with (" + ncbiID + ", " + geneID + "): " +
                    e.getMessage(), null);
        }
        return DTOConversionService.convertToOrthologDTO(ortholog);
    }


    @RequestMapping(value = "/gene/{geneID}/ortholog/evidence", method = RequestMethod.POST)
    public
    @ResponseBody
    String createOrthologFromNcbi(@PathVariable String geneID,
                                  @RequestBody OrthologPublicationEvidenceCodeDTO orthologDTO) throws InvalidWebRequestException {

        Transaction tx = HibernateUtil.createTransaction();
        try {
            tx.begin();
//            checkSecurityProfile();
            checkValidGene(geneID);
            Publication publication = checkValidPublication(orthologDTO.getPublicationID());
            Ortholog ortholog = checkValidOrtholog(orthologDTO.getOrthologID());
            Set<OrthologEvidence> evidenceSet = new HashSet<>(4);
            for (String code : orthologDTO.getEvidenceCodeList()) {
                OrthologEvidence evidence = new OrthologEvidence();
                evidence.setPublication(publication);
                evidence.setOrtholog(ortholog);
                EvidenceCode evCode = getOrthologyRepository().getEvidenceCode(code);
                if (evCode == null)
                    throw new InvalidWebRequestException("No No evidence code with name " + code + " found;", null);
                evidence.setEvidenceCode(evCode);
                evidenceSet.add(evidence);
            }
            // this will re-create the set of evidence codes. No update is done but all of them are deleted first.
            // that is ok as those records are value objects and not entities.
            orthologService.replaceEvidenceCodes(ortholog, evidenceSet, publication);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new InvalidWebRequestException("Error while creating new Evidence code for gene " + geneID + ": " +
                    e.getMessage(), null);
        }
        return "Successful added Evidence";
    }

    private void checkSecurityProfile() {
        if (!ProfileService.isRootUser())
            throw new InvalidWebRequestException("No authenitcated user found in session.", null);
    }

    private Ortholog checkValidOrtholog(String orthologID) {
        if (StringUtils.isEmpty(orthologID))
            throw new InvalidWebRequestException("No Zebrafish gene ID provided.", null);
        // find the ortholog
        Ortholog ortholog = getOrthologyRepository().getOrtholog(orthologID);
        if (ortholog == null)
            throw new InvalidWebRequestException("No ortholog with ID " + orthologID + " found!", null);
        return ortholog;
    }

    private Publication checkValidPublication(String publicationID) {
        if (StringUtils.isEmpty(publicationID))
            throw new InvalidWebRequestException("No Zebrafish gene ID provided.", null);
        // find the publication
        Publication publication = getPublicationRepository().getPublication(publicationID);
        if (publication == null)
            throw new InvalidWebRequestException("No publication with ID " + publicationID + " found!", null);
        return publication;
    }

    private Marker checkValidGene(@PathVariable String geneID) {
        if (StringUtils.isEmpty(geneID))
            throw new InvalidWebRequestException("No Zebrafish gene ID provided.", null);
        // find the gene
        Marker gene = getMarkerRepository().getMarkerByID(geneID);
        if (gene == null)
            throw new InvalidWebRequestException("No Zebrafish gene with ID " + geneID + " found!", null);
        return gene;
    }


}
