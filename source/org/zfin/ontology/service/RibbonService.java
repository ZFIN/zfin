package org.zfin.ontology.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.api.*;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.presentation.ExpressionRibbonDetail;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@Log4j2
public class RibbonService {

    public static final String STAGE_DEFINED = "stage-defined";
    public static final String STAGE_SELECTED = "stage-selected";
    private OntologyService service = new OntologyService();

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
        List<GenericTerm> stageSlim = service.getRibbonStages();
        List<GenericTerm> anatomySlim = ontologyRepository.getZfaRibbonTerms();
        List<GenericTerm> agrGoSlimTerms = ontologyRepository.getTermsInSubset("goslim_agr");

        List<GenericTerm> slimTerms = getSlims();

        RibbonSummary ribbonSummary = buildRibbonSummary(zdbID, categoryTerms, slimTerms, "/expression-annotation");
        //remove the stage-other, because it isn't meaningful
        ribbonSummary.getSubjects().get(0).getGroups().remove("ZFS:0100000-other");
        return ribbonSummary;
    }

    public RibbonSummary buildPhenotypeRibbonSummary(String zdbID) throws Exception {
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

        List<GenericTerm> categoryTerms = List.of(
                ontologyRepository.getTermByOboID("ZFA:0100000"), // ZFA root
                ontologyRepository.getTermByOboID("ZFS:0100000"), // ZFS root
                ontologyRepository.getTermByOboID("GO:0003674"), // molecular_function
                ontologyRepository.getTermByOboID("GO:0008150"), // biological_process
                ontologyRepository.getTermByOboID("GO:0005575")  // cellular_component
        );


        List<GenericTerm> slimTerms = getSlims();

        RibbonSummary ribbonSummary = buildRibbonSummary(zdbID, categoryTerms, slimTerms, "/phenotype-annotation");
        //remove the stage-other, because it isn't meaningful
        ribbonSummary.getSubjects().get(0).getGroups().remove("ZFS:0100000-other");
        return ribbonSummary;
    }

    private List<GenericTerm> getSlims() {
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

        List<GenericTerm> stageSlim = service.getRibbonStages();
        List<GenericTerm> anatomySlim = ontologyRepository.getZfaRibbonTerms();
        List<GenericTerm> agrGoSlimTerms = ontologyRepository.getTermsInSubset("goslim_agr");

        return Stream.of(agrGoSlimTerms, stageSlim, anatomySlim)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public RibbonSummary buildRibbonSummary(String zdbID,
                                            List<GenericTerm> categoryTerms,
                                            List<GenericTerm> slimTerms,
                                            String handler) throws Exception {

        // pull out just the IDs
        List<String> categoryIDs = categoryTerms.stream().map(GenericTerm::getOboID).collect(toList());
        List<String> slimIDs = slimTerms.stream().map(GenericTerm::getOboID).collect(toList());

        Map<String, Integer> otherCounts = getRibbonCounts(handler, zdbID, categoryIDs, slimIDs);
        Map<String, Integer> allCounts = getRibbonCounts(handler, zdbID, categoryIDs, Collections.emptyList());
        Map<String, Integer> slimCounts = getRibbonCounts(handler, zdbID, slimIDs, Collections.emptyList());

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
                            .collect(toList())
                    );

                    RibbonGroup otherGroup = new RibbonGroup();
                    otherGroup.setId(categoryTerm.getOboID());
                    if (categoryTerm.getOboID().equals("ZFS:0100000"))
                        otherGroup.setLabel("Unknown stage " );
                    else
                        otherGroup.setLabel("Other " + termNameDisplay);
                    otherGroup.setDescription("Show all " + termNameDisplay + " annotations not mapped to a specific term");
                    otherGroup.setType(RibbonGroup.Type.OTHER);
                    groups.add(otherGroup);

                    category.setGroups(groups);
                    return category;
                })
                .collect(toList());

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

    public Map<String, Integer> getRibbonCounts(String handler, String geneZdbId,
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

    public List<ExpressionRibbonDetail> buildExpressionRibbonDetail(String geneID, String ribbonTermID) {
        SolrQuery query = new SolrQuery();
        //query.setRequestHandler("/images");
        query.setFilterQueries("category:" + Category.EXPRESSIONS.getName());
        query.addFilterQuery("gene_zdb_id:" + geneID);
        if (StringUtils.isNotEmpty(ribbonTermID)) {
            String escapedTermID = ribbonTermID.replace(":", "\\:");
            query.addFilterQuery("term_id:" + escapedTermID);
        }
        query.addFacetPivotField("anatomy_term_id,stage_term_id,pub_zdb_id");
        query.setStart(0);
        // get them all
        query.setFacetLimit(-1);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(query);
        } catch (SolrServerException | IOException e) {
            log.error("Error while retrieving data form SOLR...", e);
        }
        if (queryResponse == null || queryResponse.getFacetPivot() == null || queryResponse.getFacetPivot().getVal(0).size() == 0)
            return null;

        HashSet<String> termIDs = queryResponse.getFacetPivot().getVal(0).stream()
                .map(pivotField -> (String) pivotField.getValue()).collect(toCollection(HashSet::new));
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        List<GenericTerm> terms = aoRepository.getMultipleTerms(termIDs);
        Map<String, GenericTerm> termMap = terms.stream()
                .collect(toMap(GenericTerm::getOboID, term -> term));

        Set<String> stageTermIDs = queryResponse.getFacetPivot().getVal(0).stream()
                .map(pivotField -> pivotField.getPivot().stream().map(field -> (String) field.getValue()).collect(toList()))
                .flatMap(Collection::stream)
                .collect(toSet());
        List<GenericTerm> stageTerms = aoRepository.getMultipleTerms(stageTermIDs);
        Map<String, GenericTerm> stageTermMap = stageTerms.stream()
                .collect(toMap(GenericTerm::getOboID, term -> term));
        termMap.putAll(stageTermMap);

        List<ExpressionRibbonDetail> details = new ArrayList<>();
        queryResponse.getFacetPivot().getVal(0).forEach(pivotField -> {
            ExpressionRibbonDetail detail = new ExpressionRibbonDetail();
            TermDTO term = new TermDTO();
            String termID = (String) pivotField.getValue();
            term.setOboID(termID);
            term.setName(termMap.get(termID).getTermName());
            detail.setTerm(term);
            // stage pivot
            pivotField.getPivot().stream()
                    .filter(pivotField1 -> termMap.get(pivotField1.getValue()) != null)
                    .forEach(pivot -> {
                        StageDTO stage = new StageDTO();
                        String stageID = (String) pivot.getValue();
                        stage.setName(termMap.get(stageID).getTermName());
                        stage.setOboID(stageID);
                        detail.addStage(stage);
                        detail.addPublications(pivot.getPivot().stream().map(pivotField1 -> (String) pivotField1.getValue()).collect(toList()));
                    });
            List<GenericTerm> ribbonStages = service.getRibbonStages();

            GenericTerm genericTerm = termMap.get(termID);
            if (genericTerm != null && genericTerm.getOboID().contains("ZFA")) {
                List<Boolean> superStageSelections = new ArrayList<>(ribbonStages.size());
                ribbonStages.forEach(ribbonStage -> {
                    if (detail.getStages().stream().anyMatch(stage -> stage.getOboID().equals(ribbonStage.getOboID())))
                        superStageSelections.add(true);
                    else
                        superStageSelections.add(false);
                });

                List<Boolean> definedStages = new ArrayList<>(ribbonStages.size());
                boolean started = false;
                boolean finished = false;
                for (GenericTerm ribbonTerm : ribbonStages) {
                    if (!started) {
                        if (ribbonTerm.hasChildTerm(genericTerm.getStart().getOboID())) {
                            started = true;
                        }
                    }
                    if (started && !finished)
                        definedStages.add(true);
                    else
                        definedStages.add(false);
                    if (started) {
                        if (ribbonTerm.hasChildTerm(genericTerm.getEnd().getOboID())) {
                            finished = true;
                        }
                    }
                }
                // if uspecified have all stages be 'defined'
                if (genericTerm.getOboID().equals("ZFA:0001093")) {
                    List<Boolean> newDefinedStages = new ArrayList<>();
                    definedStages.forEach(aBoolean -> newDefinedStages.add(Boolean.TRUE));
                    definedStages = newDefinedStages;
                }
                detail.setDefinedStages(definedStages);
                Map<String, String> combinedSelection = new LinkedHashMap<>();
                int index = 0;
                for (Boolean selection : superStageSelections) {
                    String termName = ribbonStages.get(index).getTermName();
                    if (!selection && !definedStages.get(index)) {
                        combinedSelection.put(termName, "");
                    } else if (!selection && definedStages.get(index))
                        combinedSelection.put(termName, STAGE_DEFINED);
                    else if (selection)
                        combinedSelection.put(termName, STAGE_SELECTED);
                    index++;
                }
                detail.setStageHistogram(combinedSelection);
            }
            details.add(detail);
        });

        // remove BSPO
        details.removeIf(ribbonDetail -> ribbonDetail.getTerm().getOboID().startsWith("BSPO"));

        PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
        // fixup single publications.
        details.stream()
                .filter(ribbonDetail -> ribbonDetail.getPubIDs().size() == 1)
                .forEach(ribbonDetail1 -> {
                    Publication pub = pubRepository.getPublication(ribbonDetail1.getPubIDs().get(0));
                    ribbonDetail1.setPublication(DTOConversionService.convertToPublicationDTO(pub));
                });


        // keep only the ones that pertain to the given super / ribbon term: ribbonTermID
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        Map<String, List<GenericTerm>> getClosureForRibbonTerms = ontologyRepository.getRibbonClosure();
        if (ribbonTermID != null) {
            // filter by stage
            if (ribbonTermID.contains("ZFS:")) {
                details.removeIf(detail -> detail.getStages().stream().noneMatch(stage -> stage.getOboID().equals(ribbonTermID)));
            } else {
                details.removeIf(expressionRibbonDetail -> {
                    List<GenericTerm> closure = getClosureForRibbonTerms.get(ribbonTermID);
                    // remove if no closure element is found
                    if (closure == null)
                        return true;
                    return closure.stream().noneMatch(genericTerm -> genericTerm.getOboID().equals(expressionRibbonDetail.getTerm().getOboID()));
                });
            }
        }


        return details;
    }
}
