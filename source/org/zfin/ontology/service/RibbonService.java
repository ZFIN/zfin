package org.zfin.ontology.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.api.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RibbonService {


    public RibbonSummary buildGORibbonSummary(String zdbID) throws Exception {

        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

        // define the All terms and get the slim terms from the ontology subset
        List<GenericTerm> categoryTerms = List.of(
                ontologyRepository.getTermByOboID("GO:0003674"), // molecular_function
                ontologyRepository.getTermByOboID("GO:0008150"), // biological_process
                ontologyRepository.getTermByOboID("GO:0005575")  // cellular_component
        );
        List<GenericTerm> slimTerms = ontologyRepository.getTermsInSubset("goslim_agr");

        return buildRibbonSummary(zdbID, categoryTerms, slimTerms, "/go-annotation");
    }

    public RibbonSummary buildExpressionRibbonSummary(String zdbID) throws Exception {

        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

        List<GenericTerm> categoryTerms = List.of(
                ontologyRepository.getTermByOboID("ZFA:0100000"), // ZFA root
                ontologyRepository.getTermByOboID("ZFS:0100000"), // ZFS root
                ontologyRepository.getTermByOboID("GO:0005575")  // cellular_component
        );

        List<GenericTerm> stageSlim = List.of(
                ontologyRepository.getTermByOboID("ZFS:0000001"), //zygote
/*                ontologyRepository.getTermByOboID("ZFS:0000046"), //cleavage
                ontologyRepository.getTermByOboID("ZFS:0000045"), //blastula
                ontologyRepository.getTermByOboID("ZFS:0000047"), //gastrula
                ontologyRepository.getTermByOboID("ZFS:0000049"), //segmentation
                ontologyRepository.getTermByOboID("ZFS:0000050")//, //pharyngula
                ontologyRepository.getTermByOboID("ZFS:0007000"), //hatching
                ontologyRepository.getTermByOboID("ZFS:0000048"), //larva
                ontologyRepository.getTermByOboID("ZFS:0000051"), //juvenile
                ontologyRepository.getTermByOboID("ZFS:0000044"), //adult*/
                ontologyRepository.getTermByOboID("ZFS:0000000")  //unknown
        );

        List<GenericTerm> anatomySlim = List.of(
                ontologyRepository.getTermByOboID("ZFA:0000037"), //all anatomical structures
                ontologyRepository.getTermByOboID("ZFA:0000010"), //cardiovascular system
                ontologyRepository.getTermByOboID("ZFA:0000339"), //digestive system
                ontologyRepository.getTermByOboID("ZFA:0001158"), //endocrine system
                ontologyRepository.getTermByOboID("ZFA:0001159"), //immune system
                ontologyRepository.getTermByOboID("ZFA:0000036"), //liver and biliary system
                ontologyRepository.getTermByOboID("ZFA:0000548"), //musculature system
                ontologyRepository.getTermByOboID("ZFA:0000396"), //nervous system
                ontologyRepository.getTermByOboID("ZFA:0000163"), //renal system
                ontologyRepository.getTermByOboID("ZFA:0000632"), //reproductive system
                ontologyRepository.getTermByOboID("ZFA:0000272"), //respiratory system
                ontologyRepository.getTermByOboID("ZFA:0000282"), //sensory system
                ontologyRepository.getTermByOboID("ZFA:0001127"), //visual system
                ontologyRepository.getTermByOboID("ZFA:0000108"), //fin
                ontologyRepository.getTermByOboID("ZFA:0000368"), //integument
                ontologyRepository.getTermByOboID("ZFA:0001135"), //neural tube
                ontologyRepository.getTermByOboID("ZFA:0001122"), //primary germ layer
                ontologyRepository.getTermByOboID("ZFA:0000155")  //somite
        );
        List<GenericTerm> agrGoSlimTerms = ontologyRepository.getTermsInSubset("goslim_agr");

        List<GenericTerm> slimTerms = Stream.of(agrGoSlimTerms, stageSlim, anatomySlim)
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());


        return buildRibbonSummary(zdbID, categoryTerms, slimTerms, "/expression-annotation");

    }

    public RibbonSummary buildRibbonSummary(String zdbID,
                                            List<GenericTerm> categoryTerms,
                                            List<GenericTerm> slimTerms,
                                            String handler) throws Exception {

        // pull out just the IDs
        List<String> categoryIDs = categoryTerms.stream().map(GenericTerm::getOboID).collect(Collectors.toList());
        List<String> slimIDs = slimTerms.stream().map(GenericTerm::getOboID).collect(Collectors.toList());

        Map<String, Integer> otherCounts = getRibbonCounts(handler, zdbID, categoryIDs, slimIDs);
        Map<String, Integer> allCounts = getRibbonCounts(handler, zdbID, categoryIDs, Collections.emptyList());
        Map<String, Integer> slimCounts = getRibbonCounts(handler, zdbID, slimIDs, Collections.emptyList() );

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
        subject.setNumberOfAnnotations(allCounts.values().stream().mapToInt(Integer::intValue).sum());
        subject.setGroups(groups);

        // build the final result object
        RibbonSummary summary = new RibbonSummary();
        summary.setCategories(categories);
        summary.setSubjects(List.of(subject));
        return summary;


    }

    public Map<String,Integer> getRibbonCounts(String handler, String geneZdbId,
                                                      List<String> includeTermIDs, List<String> excludeTermIDs)
            throws SolrServerException, IOException {
        Map<String, Integer> termCounts = new HashMap<>(includeTermIDs.size());

        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRequestHandler(handler);
        query.addFilterQuery("gene_zdb_id:" + geneZdbId);

        includeTermIDs.forEach(t -> query.addFacetQuery("term_id:" + SolrService.luceneEscape(t)));
        excludeTermIDs.forEach(t -> query.addFilterQuery("-term_id:" + SolrService.luceneEscape(t)));

        QueryResponse response = SolrService.getSolrClient("prototype").query(query);

        Pattern pattern = Pattern.compile("([A-Z]+:\\d+)");
        for (Map.Entry<String, Integer> entry : response.getFacetQuery().entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey().replace("\\", ""));
            if (matcher.find()) {
                String termID = matcher.group(1);
                termCounts.put(termID, entry.getValue());
            }
        }

        return termCounts;
    }

}
