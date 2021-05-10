package org.zfin.marker.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

@Controller
@RequestMapping("/marker")
public class GeneEditController {

    @Autowired
    private MarkerRepository markerRepository;
    @Autowired
    private MarkerService markerService;

    @SneakyThrows
    @RequestMapping(value = "/gene/prototype-edit/{zdbID}")
    public String getGeneEdit(Model model, @PathVariable String zdbID) {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        model.addAttribute("gene", gene);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Gene: " + gene.getAbbreviation());
        ObjectMapper objectMapper = new ObjectMapper();
        model.addAttribute("markerRelationshipTypes", objectMapper.writeValueAsString(
                markerService.getMarkerRelationshipEditMetadata(gene,
                        MarkerRelationship.Type.GENEDOM_CONTAINS_NTR,
                        MarkerRelationship.Type.CONTAINS_POLYMORPHISM,
                        MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT,
                        MarkerRelationship.Type.GENE_HAS_ARTIFACT,
                        MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT,
                        MarkerRelationship.Type.RNAGENE_INTERACTS_WITH_GENE,
                        MarkerRelationship.Type.NTR_INTERACTS_WITH_GENE,
                        MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                )));
        return "marker/gene/gene-edit";
    }

}
