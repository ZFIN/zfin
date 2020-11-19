package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;

import java.util.Arrays;

@Controller
@RequestMapping("/marker")
public class GeneEditController {

    @Autowired
    MarkerRepository markerRepository;

    @RequestMapping(value = "/gene/prototype-edit/{zdbID}")
    public String getGeneEdit(Model model, @PathVariable String zdbID) {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        model.addAttribute("gene", gene);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Gene: " + gene.getAbbreviation());
        model.addAttribute("markerRelationshipTypes", String.join(",", Arrays.asList(
                MarkerRelationship.Type.GENEDOM_CONTAINS_NTR.toString(),
                MarkerRelationship.Type.CONTAINS_POLYMORPHISM.toString(),
                MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT.toString(),
                MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT.toString(),
                MarkerRelationship.Type.GENE_HAS_ARTIFACT.toString(),
                MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT.toString(),
                MarkerRelationship.Type.RNAGENE_INTERACTS_WITH_GENE.toString(),
                MarkerRelationship.Type.NTR_INTERACTS_WITH_GENE.toString()
        )));
        return "marker/gene/gene-edit";
    }

}
