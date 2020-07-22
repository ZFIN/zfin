package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.repository.RepositoryFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marker")
public class GeneEditController {

    @Autowired
    MarkerRepository markerRepository;

    @RequestMapping(value = "/gene/prototype-edit/{zdbID}")
    public String getGeneEdit(Model model, @PathVariable String zdbID) throws JsonProcessingException {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        model.addAttribute("gene", gene);
        List<Ortholog> orthologs = RepositoryFactory.getOrthologyRepository().getOrthologs(gene);
        List<LookupEntry> orthoPubs = orthologs.stream()
                .map(Ortholog::getEvidenceSet)
                .flatMap(Collection::stream)
                .map(OrthologEvidence::getPublication)
                .distinct()
                .map(publication -> {
                    LookupEntry entry = new LookupEntry();
                    entry.setId(publication.getZdbID());
                    entry.setName(publication.getCitation());
                    return entry;
                })
                .collect(Collectors.toList());
        model.addAttribute("orthoPubsJSON", new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(orthoPubs));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Gene: " + gene.getAbbreviation());
        return "marker/gene/gene-edit.page";
    }

}
