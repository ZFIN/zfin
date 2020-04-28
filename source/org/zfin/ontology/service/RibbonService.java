package org.zfin.ontology.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
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
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
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

    @Autowired
    private AnatomyRepository anatomyRepository;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    public RibbonSummary buildGORibbonSummary(String zdbID) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/go-annotation");
        query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        return buildRibbonSummary(zdbID, query, List.of(
                RibbonCategoryConfig.molecularFunction(),
                RibbonCategoryConfig.biologicalProcess(),
                RibbonCategoryConfig.cellularComponent()
        ));
    }

    public RibbonSummary buildExpressionRibbonSummary(String zdbID, boolean includeReporter, boolean onlyDirectlySubmitted) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        expressionService.addReporterFilter(query, includeReporter);
        expressionService.addDirectSubmissionFilter(query, onlyDirectlySubmitted);
        return buildRibbonSummary(zdbID, query, List.of(
                RibbonCategoryConfig.anatomy(),
                RibbonCategoryConfig.stage(),
                RibbonCategoryConfig.cellularComponent()
        ));
    }

    public RibbonSummary buildPhenotypeRibbonSummary(String zdbID) throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/phenotype-annotation");
        query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        return buildRibbonSummary(zdbID, query, List.of(
                RibbonCategoryConfig.anatomy(),
                RibbonCategoryConfig.stage(),
                RibbonCategoryConfig.molecularFunction(),
                RibbonCategoryConfig.biologicalProcess(),
                RibbonCategoryConfig.cellularComponent()
        ));
    }

    public RibbonSummary buildRibbonSummary(String zdbID,
                                            SolrQuery partialQuery,
                                            List<RibbonCategoryConfig> categoryConfigs) throws Exception {

        // pull out just the IDs
        List<String> categoryIDs = categoryConfigs.stream()
                .map(RibbonCategoryConfig::getCategoryTerm)
                .map(GenericTerm::getOboID)
                .collect(toList());
        List<String> slimIDs = categoryConfigs.stream()
                .map(RibbonCategoryConfig::getSlimTerms)
                .flatMap(List::stream)
                .map(GenericTerm::getOboID)
                .collect(toList());

        Map<String, Integer> otherCounts = getRibbonCounts(partialQuery, categoryIDs, slimIDs);
        Map<String, Integer> allCounts = getRibbonCounts(partialQuery, categoryIDs);
        Map<String, Integer> slimCounts = getRibbonCounts(partialQuery, slimIDs);

        // build the categories field with term names and definitions
        List<RibbonCategory> categories = categoryConfigs.stream()
                .map(categoryConfig -> {
                    RibbonCategory category = new RibbonCategory();
                    GenericTerm categoryTerm = categoryConfig.getCategoryTerm();
                    String termNameDisplay = categoryTerm.getTermName().replace('_', ' ');

                    category.setDescription(categoryTerm.getDefinition());
                    category.setId(categoryTerm.getOboID());
                    category.setLabel(termNameDisplay);
                    List<RibbonGroup> groups = new ArrayList<>();

                    if (categoryConfig.isIncludeAll()) {
                        RibbonGroup allGroup = new RibbonGroup();
                        allGroup.setId(categoryTerm.getOboID());
                        allGroup.setLabel(StringUtils.defaultIfEmpty(categoryConfig.getAllLabel(), "All " + termNameDisplay));
                        allGroup.setDescription(StringUtils.defaultIfEmpty(categoryConfig.getAllDescription(), "Show all " + termNameDisplay + " annotations"));
                        allGroup.setType(RibbonGroup.Type.ALL);
                        groups.add(allGroup);
                    }

                    groups.addAll(categoryConfig.getSlimTerms().stream()
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

                    if (categoryConfig.isIncludeOther()) {
                        RibbonGroup otherGroup = new RibbonGroup();
                        otherGroup.setId(categoryTerm.getOboID());
                        otherGroup.setLabel(StringUtils.defaultIfEmpty(categoryConfig.getOtherLabel(), "Other " + termNameDisplay));
                        otherGroup.setDescription(StringUtils.defaultIfEmpty(categoryConfig.getOtherDescription(), "Show all " + termNameDisplay + " annotations not mapped to a specific term"));
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

    public Map<String, Integer> getRibbonCounts(SolrQuery defaultQuery, List<String> includeTermIDs)
            throws SolrServerException, IOException {
        return getRibbonCounts(defaultQuery, includeTermIDs, Collections.emptyList());
    }

    public Map<String, Integer> getRibbonCounts(SolrQuery partialQuery, List<String> includeTermIDs, List<String> excludeTermIDs)
            throws SolrServerException, IOException {
        Map<String, Integer> termCounts = new HashMap<>(includeTermIDs.size());

        SolrQuery query = partialQuery.getCopy();
        query.setQuery("*:*");
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

    public JsonResultResponse<ExpressionDetail> buildExpressionDetail(String geneID, String termID, Pagination pagination) {
        HashSet<String> expressionIDs = getDetailExpressionInfo(geneID, termID);
        if (expressionIDs == null) {
            return null;
        }

        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
        List<Integer> ids = expressionIDs.stream().map(Integer::parseInt).collect(toList());
        List<ExpressionFigureStage> expressions = expressionRepository.getExperimentFigureStagesByIds(ids);
        List<ExpressionDetail> detailList = expressions.stream()
                .map(expression -> {
                    ExpressionDetail detail = new ExpressionDetail();
                    detail.setPublication(expression.getExpressionExperiment().getPublication());
                    detail.setStartStage(expression.getStartStage().getName());
                    detail.setEndStage(expression.getEndStage().getName());
                    detail.setFish(expression.getExpressionExperiment().getFishExperiment().getFish());
                    detail.setFigure(expression.getFigure());
                    detail.setGene(expression.getExpressionExperiment().getGene());
                    detail.setAssay(expression.getExpressionExperiment().getAssay());
                    detail.setExperiment(expression.getExpressionExperiment().getFishExperiment().getExperiment());
                    detail.setAntibody(expression.getExpressionExperiment().getAntibody());
                    detail.setId(expression.getId());
                    // filter out the terms that contain the term in question.
                    detail.setEntities(expression.getExpressionResultSet().stream()
                            .filter(eResult -> eResult.getEntity().getSuperterm().getOboID().equals(termID)
                                    || (eResult.getEntity().getSubterm() != null && eResult.getEntity().getSubterm().getOboID().equals(termID)))
                            .map(ExpressionResult2::getEntity)
                            .collect(Collectors.toSet()));
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

    private HashSet<String> getDetailExpressionInfo(String geneID, String termID) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.setFilterQueries("category:" + Category.EXPRESSIONS.getName());
        query.addFilterQuery("gene_zdb_id:" + geneID);
        if (StringUtils.isNotEmpty(termID)) {
            query.add("f.anatomy_term_id.facet.prefix", termID);
        }
        // get Facet for the ao term and the PK of the expression record.
        query.addFacetPivotField("anatomy_term_id,efs_id");
        query.setStart(0);
        // get them all
        query.setFacetLimit(-1);


        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(query);
        } catch (SolrServerException | IOException e) {
            log.error("Error while retrieving data form SOLR...", e);
        }
        if (queryResponse == null || queryResponse.getFacetPivot() == null || queryResponse.getFacetPivot().getVal(0).size() == 0) {
            return null;
        }

        // remove artificial PK prefix: 'xpatres-'
        HashSet<String> expressionIDs = queryResponse.getFacetPivot().getVal(0).get(0).getPivot().stream()
                .map(pivotField -> (String) pivotField.getValue()).map(name -> name.replace("xpatres-", "")).collect(toCollection(HashSet::new));
        return expressionIDs;
    }


    public List<ExpressionRibbonDetail> buildExpressionRibbonDetail(String geneID, String ribbonTermID, boolean includeReporter, boolean onlyDirectlySubmitted) {
        List<ExpressionRibbonDetail> details = getExpressionRibbonDetails(geneID, ribbonTermID, includeReporter, onlyDirectlySubmitted);
        if (details == null) {
            return null;
        }

        // remove BSPO
        details.removeIf(ribbonDetail -> ribbonDetail.getEntity().getSubterm() != null
                && ribbonDetail.getEntity().getSubterm().getOboID().startsWith("BSPO"));

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
                details.removeIf(detail -> detail.getStages().stream().noneMatch(stage -> stage.getOboID().equals(ribbonTermID)));
            }
        }
        return details;
    }

    private List<ExpressionRibbonDetail> getExpressionRibbonDetails(String geneID, String ribbonTermID, boolean includeReporter, boolean onlyDirectlySubmitted) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.setFilterQueries("category:" + Category.EXPRESSIONS.getName());
        query.addFilterQuery("gene_zdb_id:" + geneID);
        if (StringUtils.isNotEmpty(ribbonTermID)) {
            String escapedTermID = ribbonTermID.replace(":", "\\:");
            query.addFilterQuery("term_id:" + escapedTermID);
        }
        expressionService.addReporterFilter(query, includeReporter);
        expressionService.addDirectSubmissionFilter(query, onlyDirectlySubmitted);
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

        // remove BSPO
        details.removeIf(ribbonDetail -> ribbonDetail.getEntity().getSubterm() != null
                && ribbonDetail.getEntity().getSubterm().getOboID().startsWith("BSPO"));

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

        // keep only the ones that pertain to the given super / ribbon term: ribbonTermID
        Map<String, List<GenericTerm>> getClosureForRibbonTerms = ontologyRepository.getRibbonClosure();
        if (ribbonTermID != null) {
            // filter by stage
            if (ribbonTermID.contains("ZFS:")) {
                details.removeIf(detail -> detail.getStages().stream().noneMatch(stage -> stage.getOboID().equals(ribbonTermID)));
            }
        }
        return details;
    }

}
