package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.*;
import org.zfin.marker.service.MarkerGoService;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class GeneOntologyRibbonController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MarkerGoService markerGoService;

    @Autowired
    private OntologyRepository ontologyRepository;

    @RequestMapping(value = "/marker/{zdbID}/go/ribbon-summary")
    public RibbonSummary getGoRibbonSummary(@PathVariable("zdbID") String zdbID) throws Exception {
        // define the All terms and get the slim terms from the ontology subset
        List<GenericTerm> categoryTerms = List.of(
                ontologyRepository.getTermByOboID("GO:0003674"), // molecular_function
                ontologyRepository.getTermByOboID("GO:0008150"), // biological_process
                ontologyRepository.getTermByOboID("GO:0005575")  // cellular_component
        );
        List<GenericTerm> slimTerms = ontologyRepository.getTermsInSubset("goslim_agr");

        // pull out just the IDs
        List<String> categoryIDs = categoryTerms.stream().map(GenericTerm::getOboID).collect(Collectors.toList());
        List<String> slimIDs = slimTerms.stream().map(GenericTerm::getOboID).collect(Collectors.toList());

        // get annotation counts from solr
        Map<String, Integer> otherCounts = markerGoService.getRibbonAnnotationCountsForGene(zdbID, categoryIDs, slimIDs);
        Map<String, Integer> allCounts = markerGoService.getRibbonAnnotationCountsForGene(zdbID, categoryIDs, Collections.emptyList());
        Map<String, Integer> slimCounts = markerGoService.getRibbonAnnotationCountsForGene(zdbID, slimIDs, Collections.emptyList());

        // build the categories field with term names and definitions
        List<RibbonCategory> categories = categoryTerms.stream()
                .map(categoryTerm -> {
                    RibbonCategory category = new RibbonCategory();
                    String termNameDisplay = categoryTerm.getTermName().replace('_', ' ');

                    category.setDescription(categoryTerm.getDefinition());
                    category.setId(categoryTerm.getOboID());
                    category.setLabel(termNameDisplay);
                    List<RibbonGroup> groups = new ArrayList<>();

                    RibbonGroup allGroup = new RibbonGroup();
                    allGroup.setId(categoryTerm.getOboID());
                    allGroup.setLabel("All " + termNameDisplay);
                    allGroup.setDescription("Show all " + termNameDisplay + " annotations");
                    allGroup.setType(RibbonGroup.Type.ALL);
                    groups.add(allGroup);

                    groups.addAll(slimTerms.stream()
                            .filter(slimTerm -> slimTerm.getOntology() == categoryTerm.getOntology())
                            .map(slimTerm -> {
                                RibbonGroup group = new RibbonGroup();
                                group.setId(slimTerm.getOboID());
                                group.setLabel(slimTerm.getTermName());
                                group.setDescription(slimTerm.getDefinition());
                                group.setType(RibbonGroup.Type.TERM);
                                return group;
                            })
                            .collect(Collectors.toList())
                    );

                    RibbonGroup otherGroup = new RibbonGroup();
                    otherGroup.setId(categoryTerm.getOboID());
                    otherGroup.setLabel("Other " + termNameDisplay);
                    otherGroup.setDescription("Show all " + termNameDisplay + " annotations not mapped to a specific term");
                    otherGroup.setType(RibbonGroup.Type.OTHER);
                    groups.add(otherGroup);

                    category.setGroups(groups);
                    return category;
                })
                .collect(Collectors.toList());

        // rename keys of other counts map with '-other' suffix
        otherCounts = otherCounts.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey() + "-other",
                Map.Entry::getValue
        ));

        // build the groups field from the counts from solr. the All and Other counts should
        // always be there. the slim terms only need to be there if the count is over 0
        Map<String, Map<String, RibbonSubjectGroupCounts>> groups = Stream.concat(
                slimCounts.entrySet().stream().filter(count -> count.getValue() > 0),
                Stream.concat(allCounts.entrySet().stream(), otherCounts.entrySet().stream()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        count -> {
                            RibbonSubjectGroupCounts groupCounts = new RibbonSubjectGroupCounts();
                            groupCounts.setNumberOfAnnotations(count.getValue());
                            return Map.of("ALL", groupCounts);
                        }
                ));
        RibbonSubject subject = new RibbonSubject();
        subject.setId(zdbID);
        subject.setGroups(groups);

        // build the final result object
        RibbonSummary summary = new RibbonSummary();
        summary.setCategories(categories);
        summary.setSubjects(List.of(subject));
        return summary;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/{zdbID}/go")
    public JsonResultResponse<MarkerGoViewTableRow> getGoAnnotations(@PathVariable String zdbID,
                                                                     @RequestParam(required = false) String termId,
                                                                     @RequestParam(required = false) boolean isOther,
                                                                     @Version Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<MarkerGoViewTableRow> response = markerGoService.getGoEvidence(zdbID, termId, isOther, pagination);
        response.setHttpServletRequest(request);

        return response;
    }
}
