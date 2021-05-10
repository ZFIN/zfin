package org.zfin.publication.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.publication.repository.PublicationRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicationAPIController {

    @Autowired
    private PublicationRepository publicationRepository;

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/genes", method = RequestMethod.GET)
    public List<MarkerDTO> getPublicationGenes(@PathVariable String zdbID) {
        List<Marker> genes = publicationRepository.getGenesByPublication(zdbID, false);
        List<MarkerDTO> dtos = new ArrayList<>();
        for (Marker gene : genes) {
            dtos.add(DTOConversionService.convertToMarkerDTO(gene));
        }
        return dtos;
    }

}
