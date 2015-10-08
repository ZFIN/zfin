package org.zfin.orthology.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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
