package org.zfin.ontology.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.api.*;
import org.zfin.marker.presentation.ExpressionDetail;
import org.zfin.marker.presentation.ExpressionRibbonDetail;
import org.zfin.marker.presentation.PhenotypeRibbonSummary;
import org.zfin.mutant.PhenotypeObservationStatement;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.zfin.framework.api.RibbonType.EXPRESSION;
import static org.zfin.framework.api.RibbonType.PHENOTYPE;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@Service
@Log4j2
public class RibbonService {

    @Autowired
    private AnatomyRepository anatomyRepository;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private PublicationRepository publicationRepository;

    private static final String GLOBAL_ALL = "GlobalAll";

    public RibbonSummary buildGORibbonSummary(String zdbID) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/go-annotation");
        query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        return buildRibbonSummary(zdbID, query, List.of(
                new RibbonCategoryConfig.GeneOntologyMolecularFunction(),
                new RibbonCategoryConfig.GeneOntologyBiologicalProcess(),
                new RibbonCategoryConfig.GeneOntologyCellularComponent()
        ));
    }

    public RibbonSummary buildExpressionRibbonSummary(String zdbID, boolean includeReporter, boolean onlyInSitu) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query = addExpressionMarkerConstraint(query, zdbID);
        expressionService.addReporterFilter(query, includeReporter);
        expressionService.addInSituFilter(query, onlyInSitu);
        return buildRibbonSummary(zdbID, query, List.of(
                new RibbonCategoryConfig.Anatomy(),
                new RibbonCategoryConfig.Stage(),
                new RibbonCategoryConfig.ExpressionCellularComponent()
        ));
    }

    public RibbonSummary buildPhenotypeRibbonSummary(String zdbID, boolean excludeEaps, boolean excludeSTRs) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/phenotype-annotation");
        query.addFilterQuery(FieldName.MONOGENIC_GENE_ZDB_ID.getName() + ":" + zdbID);
        if (excludeEaps) {
            query.addFilterQuery("is_eap:false");
        }
        if (excludeSTRs) {
            query.addFilterQuery("has_str:false");
        }
        return buildRibbonSummary(zdbID, query, List.of(
                new RibbonCategoryConfig.Anatomy(),
                new RibbonCategoryConfig.Stage(),
                new RibbonCategoryConfig.PhenotypeMolecularFunction(),
                new RibbonCategoryConfig.PhenotypeBiologicalProcess(),
                new RibbonCategoryConfig.PhenotypeCellularComponent()
        ));
    }

    public RibbonSummary buildRibbonSummary(String zdbID,
                                            SolrQuery partialQuery,
                                            List<RibbonCategoryConfig> categoryConfigs) throws Exception {

        // pull out just the IDs
        List<String> categoryIDs = categoryConfigs.stream()
                .map(RibbonCategoryConfig::getCategoryId)
                .collect(toList());
        List<String> slimIDs = categoryConfigs.stream()
                .map(RibbonCategoryConfig::getSlimTerms)
                .flatMap(List::stream)
                .map(GenericTerm::getOboID)
                .collect(toList());

        // get the counts for the All annotation blocks and the slim blocks
        Map<String, RibbonSubjectGroupCounts> allCounts = getRibbonCounts(partialQuery, categoryIDs);
        Map<String, RibbonSubjectGroupCounts> slimCounts = getRibbonCounts(partialQuery, slimIDs);
        // other counts have to be done one category at a time so that post-composed terms which are included
        // under a slim term of one category aren't excluded from the Other of another category
        Map<String, RibbonSubjectGroupCounts> otherCounts = new HashMap<>();
        for (RibbonCategoryConfig category : categoryConfigs) {
            if (category.isIncludeOther()) {
                otherCounts.putAll(getRibbonCounts(
                        partialQuery,
                        List.of(category.getCategoryId()),
                        category.getSlimTerms().stream().map(GenericTerm::getOboID).collect(toList())
                ));
            }
        }

        // build the categories field with term names and definitions
        List<RibbonCategory> categories = categoryConfigs.stream()
                .map(categoryConfig -> {
                    RibbonCategory category = new RibbonCategory();

                    category.setDescription(categoryConfig.getCategoryDefinition());
                    category.setId(categoryConfig.getCategoryId());
                    category.setLabel(categoryConfig.getCategoryLabel());
                    List<RibbonGroup> groups = new ArrayList<>();

                    if (categoryConfig.isIncludeAll()) {
                        RibbonGroup allGroup = new RibbonGroup();
                        allGroup.setId(categoryConfig.getCategoryId());
                        allGroup.setLabel(categoryConfig.getAllLabel());
                        allGroup.setDescription(categoryConfig.getAllDescription());
                        allGroup.setType(RibbonGroup.Type.ALL);
                        groups.add(allGroup);
                    }

                    groups.addAll(categoryConfig.getSlimTerms().stream()
                            .map(slimTerm -> {
                                RibbonGroup group = new RibbonGroup();
                                group.setId(slimTerm.getOboID());
                                group.setLabel(categoryConfig.getSlimTermLabel(slimTerm));
                                group.setDescription(slimTerm.getDefinition());
                                group.setType(RibbonGroup.Type.TERM);
                                return group;
                            })
                            .collect(toList())
                    );

                    if (categoryConfig.isIncludeOther()) {
                        RibbonGroup otherGroup = new RibbonGroup();
                        otherGroup.setId(categoryConfig.getCategoryId());
                        otherGroup.setLabel(categoryConfig.getOtherLabel());
                        otherGroup.setDescription(categoryConfig.getOtherDescription());
                        otherGroup.setType(RibbonGroup.Type.OTHER);
                        groups.add(otherGroup);
                    }

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
                slimCounts.entrySet().stream().filter(count -> count.getValue().getNumberOfAnnotations() > 0),
                Stream.concat(allCounts.entrySet().stream(), otherCounts.entrySet().stream()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        count -> Map.of("ALL", count.getValue())
                ));
        RibbonSubject subject = new RibbonSubject();
        subject.setId(zdbID);
        subject.setGroups(groups);
        Map<String, RibbonSubjectGroupCounts> globalAllCounts = getRibbonCounts(partialQuery, Collections.emptyList());
        subject.setNumberOfAnnotations(globalAllCounts.get(GLOBAL_ALL).getNumberOfAnnotations());
        subject.setNumberOfClasses(globalAllCounts.get(GLOBAL_ALL).getNumberOfClasses());

        // build the final result object
        RibbonSummary summary = new RibbonSummary();
        summary.setCategories(categories);
        summary.setSubjects(List.of(subject));
        return summary;
    }

    public Map<String, RibbonSubjectGroupCounts> getRibbonCounts(SolrQuery defaultQuery, List<String> includeTermIDs)
            throws SolrServerException, IOException {
        return getRibbonCounts(defaultQuery, includeTermIDs, Collections.emptyList());
    }

    public Map<String, RibbonSubjectGroupCounts> getRibbonCounts(SolrQuery partialQuery, List<String> includeTermIDs, List<String> excludeTermIDs)
            throws SolrServerException, IOException {
        Map<String, RibbonSubjectGroupCounts> termCounts = new HashMap<>(includeTermIDs.size());

        SolrQuery query = partialQuery.getCopy();
        query.setQuery("*:*");

        // start building the JSON object which specifies the facet query
        final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
        final ObjectNode jsonFacetQuery = jsonFactory.objectNode();
        // an empty includeTermIDs indicates this is the query for the GlobalAll field.
        // TODO: refactor to allow the GlobalAll numbers to be fetched in the same query as ribbon terms?
        if (CollectionUtils.isEmpty(includeTermIDs)) {
            jsonFacetQuery.put("num_terms", "unique(name_s)");
        }
        includeTermIDs.forEach(t -> {
            final ObjectNode termQuery = jsonFactory.objectNode();
            final ObjectNode termQueryFacet = jsonFactory.objectNode();
            termQuery.put("type", "query");
            termQuery.put("q", "term_id:" + SolrService.luceneEscape(t));
            termQueryFacet.put("num_terms", "unique(name_s)");
            termQuery.set("facet", termQueryFacet);
            jsonFacetQuery.set(t, termQuery);
        });
        // serialize the JSON object and add it to the query
        query.set("json.facet", new ObjectMapper().writeValueAsString(jsonFacetQuery));
        excludeTermIDs.forEach(t -> query.addFilterQuery("-term_id:" + SolrService.luceneEscape(t)));

        // need to POST here because otherwise the underlying URI gets too long
        QueryResponse response = SolrService.getSolrClient("prototype").query(query, SolrRequest.METHOD.POST);

        // the json facet API isn't well-supported by SolrJ so there a lot of getting
        // fields by name and casting from Object here
        NamedList<Object> facets = (NamedList<Object>) response.getResponse().get("facets");
        if (CollectionUtils.isEmpty(includeTermIDs)) {
            RibbonSubjectGroupCounts counts = new RibbonSubjectGroupCounts();
            counts.setNumberOfAnnotations((Integer) Optional.ofNullable(facets.get("count")).orElse(0));
            counts.setNumberOfClasses((Integer) Optional.ofNullable(facets.get("num_terms")).orElse(0));
            termCounts.put(GLOBAL_ALL, counts);
        } else {
            for (String id : includeTermIDs) {
                NamedList<Object> facet = (NamedList<Object>) facets.get(id);
                RibbonSubjectGroupCounts counts = new RibbonSubjectGroupCounts();
                if (facet != null) {
                    counts.setNumberOfAnnotations((Integer) Optional.ofNullable(facet.get("count")).orElse(0));
                    counts.setNumberOfClasses((Integer) Optional.ofNullable(facet.get("num_terms")).orElse(0));
                } else {
                    counts.setNumberOfAnnotations(0);
                    counts.setNumberOfClasses(0);
                }
                termCounts.put(id, counts);
            }
        }

        return termCounts;
    }

    public JsonResultResponse<ExpressionDetail> buildExpressionDetail(String geneID, String supertermID, String subtermID, String ribbonTermID, boolean includeReporter, boolean onlyInSitu, Pagination pagination) {
        HashSet<String> expressionIDs = getDetailExpressionInfo(geneID, supertermID, subtermID, ribbonTermID, includeReporter, onlyInSitu);
        if (expressionIDs == null || CollectionUtils.isEmpty(expressionIDs)) {
            return null;
        }

        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
        List<Integer> ids = expressionIDs.stream().map(Integer::parseInt).collect(toList());
        List<ExpressionFigureStage> expressions = expressionRepository.getExperimentFigureStagesByIds(ids);
        List<ExpressionDetail> detailList = expressions.stream()
                .map(expression -> {
                    ExpressionDetail detail = new ExpressionDetail();
                    detail.setPublication(expression.getExpressionExperiment().getPublication());
                    detail.setEntities(expression.getExpressionResultSet().stream().map(ExpressionResult2::getEntity).collect(toSet()));
                    detail.setStartStage(expression.getStartStage());
                    detail.setEndStage(expression.getEndStage());
                    detail.setFish(expression.getExpressionExperiment().getFishExperiment().getFish());
                    detail.setFigure(expression.getFigure());
                    detail.setGene(expression.getExpressionExperiment().getGene());
                    detail.setAssay(expression.getExpressionExperiment().getAssay());
                    detail.setExperiment(expression.getExpressionExperiment().getFishExperiment().getExperiment());
                    detail.setAntibody(expression.getExpressionExperiment().getAntibody());
                    detail.setId(expression.getId());
                    return detail;
                })
                .sorted(Comparator.comparing(expression -> expression.getFish().getDisplayName()))
                .collect(Collectors.toList());

        JsonResultResponse<ExpressionDetail> response = new JsonResultResponse<>();
        response.setTotal(detailList.size());

        // paginating
        response.setResults(detailList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public JsonResultResponse<PhenotypeRibbonSummary> buildPhenotypeSummary(String geneID, String termID, Pagination pagination, Boolean isOther, boolean excludeEaps,  boolean excludeSTRs) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/phenotype-annotation");
        query.addFilterQuery(FieldName.MONOGENIC_GENE_ZDB_ID + ":" + geneID);
        addRibbonTermQuery(query, PHENOTYPE, termID, isOther);
        if (excludeEaps) {
            query.addFilterQuery("is_eap:false");
        }
        if (excludeSTRs) {
            query.addFilterQuery("has_str:false");
        }
        final String filterValue = pagination.getFieldFilter(FieldFilter.FILTER_TERM_NAME);
        if (StringUtils.isNotEmpty(filterValue)) {
            query.addFilterQuery("phenotype_statement_ac:(" + filterValue.trim() + ")");
        }
        // get Facet for the ao term and the PK of the expression record.
        query.addFacetPivotField("name_sort,phenotype_statement,phenotype_statement_term_id,stage_term_id,pub_zdb_id,id");
        query.setParam("f.name_sort.facet.offset", String.valueOf(pagination.getStart()));
        query.setParam("f.name_sort.facet.limit", String.valueOf(pagination.getLimit()));
        query.setParam("f.name_sort.facet.sort", "index");
        query.setGetFieldStatistics("{!countDistinct=true}phenotype_statement_s");

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(query);
        } catch (SolrServerException | IOException e) {
            log.error("Error while retrieving data form SOLR...", e);
        }
        if (queryResponse == null || queryResponse.getFacetPivot() == null || queryResponse.getFacetPivot().getVal(0).size() == 0) {
            JsonResultResponse<PhenotypeRibbonSummary> response = new JsonResultResponse<>();
            return response;
        }

        List<DevelopmentStage> stageTerms = anatomyRepository.getAllStages();
        Map<String, DevelopmentStage> stageTermMap = stageTerms.stream()
                .collect(toMap(DevelopmentStage::getOboID, term -> term));

        List<PhenotypeRibbonSummary> phenotypeRibbonDetails = new ArrayList<>();
        queryResponse.getFacetPivot().forEach((pivotFieldName, pivotFields) ->
                pivotFields.forEach(field -> {
                    field.getPivot().forEach(pivotField -> {
                        PhenotypeRibbonSummary detail = new PhenotypeRibbonSummary();
                        detail.setPhenotype((String) pivotField.getValue());
                        // stage pivot
                        PivotField pivotField2 = pivotField.getPivot().get(0);
                        detail.setId((String) pivotField2.getValue());
                        pivotField2.getPivot().stream()
                                .filter(pivotField1 -> stageTermMap.containsKey(pivotField1.getValue()))
                                .forEach(pivot -> {
                                    String stageID = (String) pivot.getValue();
                                    detail.addStage(stageTermMap.get(stageID));
                                    detail.addPublications(pivot.getPivot().stream().map(pivotField1 -> (String) pivotField1.getValue()).collect(toList()));
                                    detail.addPhenotypeIds(pivot.getPivot().stream()
                                            .map(pivotPubs -> pivotPubs.getPivot().stream()
                                                    .map(pivotID -> ((String) pivotID.getValue()).replace("psg-", ""))
                                                    .collect(Collectors.toList()))
                                            .flatMap(Collection::stream)
                                            .collect(Collectors.toList())
                                    );
                                });
                        phenotypeRibbonDetails.add(detail);
                    });
                }));


        // fixup single publications.
        phenotypeRibbonDetails.stream()
                .filter(ribbonDetail -> ribbonDetail.getPubIDs() != null)
                .filter(ribbonDetail -> ribbonDetail.getPubIDs().size() == 1)
                .forEach(ribbonDetail1 -> {
                    Publication pub = publicationRepository.getPublication(ribbonDetail1.getPubIDs().get(0));
                    ribbonDetail1.setPublication(pub);
                });
        phenotypeRibbonDetails
                .forEach(ribbonDetail1 -> {
                    List<String> pubRibbon = ribbonDetail1.getPubIDs();
                    ribbonDetail1.setRibbonPubs(pubRibbon);
                });

        JsonResultResponse<PhenotypeRibbonSummary> response = new JsonResultResponse<>();
        response.setTotal(queryResponse.getFieldStatsInfo().get("phenotype_statement_s").getCountDistinct());
        response.setResults(phenotypeRibbonDetails);

        return response;
    }

    public JsonResultResponse<PhenotypeObservationStatement> getPhenotypeDetails(String geneID, String termIDs, Pagination pagination) {
        String[] termIDList = termIDs.split(",");
        String paginatedTermIDs = Arrays.stream(termIDList)
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(joining(","));
        List<PhenotypeObservationStatement> list = getMutantRepository().getPhenotypeStatements(geneID, paginatedTermIDs);
        JsonResultResponse<PhenotypeObservationStatement> response = new JsonResultResponse<>();
        response.setResults(list);
        response.setTotal(termIDList.length);
        return response;
    }

    private HashSet<String> getDetailExpressionInfo(String geneID, String supertermID, String subtermID, String ribbonTermID, boolean includeReporter, boolean onlyInSitu) {

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.addFilterQuery("gene_zdb_id:" + geneID);

        if (StringUtils.isNotEmpty(supertermID)) {
            query.addFilterQuery("superterm_id:" + SolrService.luceneEscape(supertermID));
        }
        if (StringUtils.isNotEmpty(subtermID)) {
            query.addFilterQuery("subterm_id:" + SolrService.luceneEscape(subtermID));
        }
        if (StringUtils.isNotEmpty(ribbonTermID)) {
            query.addFilterQuery("term_id:" + SolrService.luceneEscape(ribbonTermID));
        }

        expressionService.addReporterFilter(query, includeReporter);
        expressionService.addInSituFilter(query, onlyInSitu);

        // get Facet for the ao term and the PK of the expression record.
        query.addFacetField("efs_id");

        // get them all
        query.setFacetLimit(-1);


        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(query);
        } catch (SolrServerException | IOException e) {
            log.error("Error while retrieving data form SOLR...", e);
        }
        if (queryResponse == null || queryResponse.getFacetField("efs_id") == null) {
            return null;
        }

        HashSet<String> expressionIDs = queryResponse.getFacetField("efs_id").getValues().stream()
                .map(count -> count.getName())
                .collect(toCollection(HashSet::new));

        return expressionIDs;
    }


    public List<ExpressionRibbonDetail> buildExpressionRibbonDetail(String geneID, String ribbonTermID, boolean includeReporter, boolean onlyInSitu, boolean isOther) {
        List<ExpressionRibbonDetail> details = getExpressionRibbonDetails(geneID, ribbonTermID, includeReporter, onlyInSitu, isOther);
        if (details == null) {
            return null;
        }

        // remove BSPO
        /*details.removeIf(ribbonDetail -> ribbonDetail.getEntity().getSubterm() != null
                && ribbonDetail.getEntity().getSubterm().getOboID().startsWith("BSPO"));*/

        // fixup single publications.
        PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
        details.stream()
                .filter(ribbonDetail -> ribbonDetail.getPubIDs().size() == 1)
                .forEach(ribbonDetail1 -> {
                    Publication pub = pubRepository.getPublication(ribbonDetail1.getPubIDs().get(0));
                    ribbonDetail1.setPublication(pub);
                });
        details.stream()
                .forEach(ribbonDetail1 -> {
                    List<String> pubRibbon = ribbonDetail1.getPubIDs();
                    ribbonDetail1.setRibbonPubs(pubRibbon);
                });

        if (StringUtils.isNotEmpty(ribbonTermID)) {
            // filter by stage
            if (ribbonTermID.contains("ZFS:")) {

                details.removeIf(detail -> detail.getStages() == null);
            }
        }
        return details;
    }

    private List<ExpressionRibbonDetail> getExpressionRibbonDetails(String geneID, String ribbonTermID, boolean includeReporter, boolean onlyInSitu, boolean isOther) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.addFilterQuery("gene_zdb_id:" + geneID);
        addRibbonTermQuery(query, EXPRESSION, ribbonTermID, isOther);
        expressionService.addReporterFilter(query, includeReporter);
        expressionService.addInSituFilter(query, onlyInSitu);
        query.addFacetPivotField("postcomposed_term_id,stage_term_id,pub_zdb_id");
        query.setStart(0);
        // get them all
        query.setFacetLimit(-1);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(query);
        } catch (SolrServerException | IOException e) {
            log.error("Error while retrieving data form Solr...", e);
        }
        if (queryResponse == null || queryResponse.getFacetPivot() == null || queryResponse.getFacetPivot().getVal(0).size() == 0) {
            return null;
        }

        HashSet<String> termIDs = queryResponse.getFacetPivot().getVal(0).stream()
                .map(pivotField -> (String) pivotField.getValue())
                .flatMap(x -> Arrays.stream(x.split(",")))
                .collect(toCollection(HashSet::new));
        List<GenericTerm> terms = anatomyRepository.getMultipleTerms(termIDs);
        Map<String, GenericTerm> anatomyTermMap = terms.stream()
                .collect(toMap(GenericTerm::getOboID, term -> term));

        List<DevelopmentStage> stageTerms = anatomyRepository.getAllStagesWithoutUnknown();
        Map<String, DevelopmentStage> stageTermMap = stageTerms.stream()
                .collect(toMap(DevelopmentStage::getOboID, term -> term));

        List<ExpressionRibbonDetail> details = new ArrayList<>();
        queryResponse.getFacetPivot().getVal(0).forEach(pivotField -> {
            ExpressionRibbonDetail detail = new ExpressionRibbonDetail();
            PostComposedEntity entity = new PostComposedEntity();
            String pivotValue = (String) pivotField.getValue();
            if (pivotValue.contains(",")) {
                List<String> termIds = Arrays.asList(pivotValue.split(","));
                entity.setSuperterm(anatomyTermMap.get(termIds.get(0)));
                entity.setSubterm(anatomyTermMap.get(termIds.get(1)));
            } else {
                entity.setSuperterm(anatomyTermMap.get(pivotValue));
            }

            detail.setEntity(entity);
            // stage pivot
            pivotField.getPivot().stream()
                    .filter(pivotField1 -> stageTermMap.containsKey(pivotField1.getValue()))
                    .forEach(pivot -> {
                        String stageID = (String) pivot.getValue();
                        detail.addStage(stageTermMap.get(stageID));
                        detail.addPublications(pivot.getPivot().stream().map(pivotField1 -> (String) pivotField1.getValue()).collect(toList()));
                    });
            details.add(detail);
        });

        // fixup single publications.
        details.stream()
                .filter(ribbonDetail -> ribbonDetail.getPubIDs().size() == 1)
                .forEach(ribbonDetail1 -> {
                    Publication pub = publicationRepository.getPublication(ribbonDetail1.getPubIDs().get(0));
                    ribbonDetail1.setPublication(pub);
                });
        details.stream()
                .forEach(ribbonDetail1 -> {
                    List<String> pubRibbon = ribbonDetail1.getPubIDs();
                    ribbonDetail1.setRibbonPubs(pubRibbon);
                });

        if (ribbonTermID != null) {

            // filter by stage
            if (ribbonTermID.contains("ZFS:")) {

                details.removeIf(detail -> detail.getStages() == null);
            }
        }
        return details;
    }

    public void addRibbonTermQuery(SolrQuery query, RibbonType ribbon, String termId, boolean isOther) {
        if (StringUtils.isEmpty(termId)) {
            return;
        }
        query.addFilterQuery("term_id:" + SolrService.luceneEscape(termId));
        if (isOther) {
            RibbonCategoryConfig config = RibbonCategoryConfig.forTerm(ribbon, termId);
            if (config != null) {
                config.getSlimTerms().forEach(term -> {
                    query.addFilterQuery("-term_id:" + SolrService.luceneEscape(term.getOboID()));
                });
            }
        }
    }

    private SolrQuery addExpressionMarkerConstraint(SolrQuery query, String zdbID) {
        if (StringUtils.startsWith(zdbID, "ZDB-ATB-")) {
            query.addFilterQuery(FieldName.ANTIBODY_ZDB_ID.getName() + ":" + zdbID);
        } else {
            query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        }
        return query;
    }

}
