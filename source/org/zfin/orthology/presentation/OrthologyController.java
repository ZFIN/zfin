package org.zfin.orthology.presentation;

import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.NcbiOtherSpeciesGeneDTO;
import org.zfin.gwt.root.dto.OrthologDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;
import static org.zfin.repository.RepositoryFactory.setAnatomyRepository;

@Controller
public class OrthologyController {

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
            throw new InvalidWebRequestException("Error while deleting Ortholg " + orthoID +
                    e.getMessage(), null);
        } finally {
            tx.rollback();
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


}
