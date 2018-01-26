package org.zfin.expression.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zfin.search.service.SolrQueryFacade.addTo;

@Service
public class ExpressionSearchService {

    private static final String TRUE = "true";
    private static final String AND = "AND";
    private static final String OR = "OR";

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private ExpressionRepository expressionRepository;

    private SolrQuery applyCriteria(SolrQuery solrQuery,
                                    ExpressionSearchCriteria criteria,
                                    String anatomyBoolean) {

        solrQuery.addFilterQuery(fq(FieldName.CATEGORY, Category.EXPRESSIONS.getName()));

        //only interested in expression where there is a zebrafish gene or no reporter, no ATB expression
        solrQuery.addFilterQuery("(" + FieldName.ZEBRAFISH_GENE.getName()
                + ":[* TO *] OR "
                + FieldName.REPORTER_GENE + ":[* TO *])");

        if (CollectionUtils.isNotEmpty(criteria.getAnatomy())) {
            String termQuery = criteria.getAnatomy().stream()
                    .map(t -> "\"" + SolrService.luceneEscape(t) + "\"")
                    .collect(Collectors.joining(" " + anatomyBoolean + " "));
            solrQuery.addFilterQuery(FieldName.EXPRESSION_ANATOMY_TF.getName() + ":(" + termQuery + ")");
        }

        String geneField = criteria.getGeneField();
        if (StringUtils.isNotEmpty(geneField)) {
            solrQuery.addFilterQuery(any(
                    fq(FieldName.ZEBRAFISH_GENE, geneField),
                    fq(FieldName.ZEBRAFISH_GENE_T, geneField),
                    fq(FieldName.EXPRESSED_GENE_FULL_NAME, geneField),
                    fq(FieldName.EXPRESSED_GENE_PREVIOUS_NAME, geneField)
            ));
        }

        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            solrQuery.addFilterQuery(fq(FieldName.GENE_ZDB_ID, criteria.getGeneZdbID()));
        }

        String targetGene = criteria.getTargetGeneField();
        if (StringUtils.isNotEmpty(targetGene)) {
            solrQuery.addFilterQuery(any(
                    fq(FieldName.TARGET, targetGene),
                    fq(FieldName.TARGET_FULL_NAME, targetGene),
                    fq(FieldName.TARGET_PREVIOUS_NAME, targetGene)
            ));
        }

        if (StringUtils.isNotEmpty(criteria.getStartStageId()) && StringUtils.isNotEmpty(criteria.getEndStageId())) {
            DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStageByOboID(criteria.getStartStageId());
            DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getStageByOboID(criteria.getEndStageId());

            if (start != null && end != null) {
                solrQuery.addFilterQuery(SolrService.buildStageRangeQuery(
                        FieldName.STAGE_HOURS_START,
                        "[", DevelopmentStage.MIN, end.getHoursEnd(), "}"
                ));
                solrQuery.addFilterQuery(SolrService.buildStageRangeQuery(
                        FieldName.STAGE_HOURS_END,
                        "{", start.getHoursStart(), DevelopmentStage.MAX, "]"
                ));
            }

        }

        String assay = criteria.getAssayName();
        if (StringUtils.isNotEmpty(assay)) {
            solrQuery.addFilterQuery(fq(FieldName.ASSAY, assay));
        }

        String fish = criteria.getFish();
        if (StringUtils.isNotEmpty(fish)) {
            solrQuery.addFilterQuery(fq(FieldName.FISH_T, fish));
        }

        String author = criteria.getAuthorField();
        if (StringUtils.isNotEmpty(author)) {
            addTo(solrQuery).fq(author, FieldName.REGISTERED_AUTHOR_AUTOCOMPLETE);
        }

        if (criteria.isOnlyFiguresWithImages()) {
            solrQuery.addFilterQuery(fq(FieldName.HAS_IMAGE, TRUE));
        }

        if (criteria.isOnlyWildtype()) {
            solrQuery.addFilterQuery(fq(FieldName.IS_WILDTYPE, TRUE));
        }

        if (criteria.isOnlyReporter()) {
            addTo(solrQuery).fqAny(FieldName.REPORTER_GENE);
        }

        if (criteria.getJournalType() == ExpressionSearchCriteria.JournalTypeOption.DIRECT) {
            addTo(solrQuery).fq(Publication.Type.UNPUBLISHED.getDisplay(), FieldName.JOURNAL_TYPE);
        } else if (criteria.getJournalType() == ExpressionSearchCriteria.JournalTypeOption.PUBLISHED) {
            addTo(solrQuery).fqNot(Publication.Type.UNPUBLISHED.getDisplay(), FieldName.JOURNAL_TYPE);
        }

        solrQuery.setRows(criteria.getRows());
        solrQuery.setStart((criteria.getPage() - 1) * criteria.getRows());

        solrQuery.set("hl.q", String.join(" ", Arrays.asList(solrQuery.getFilterQueries())));

        return solrQuery;
    }

    public List<GeneResult> getGeneResults(ExpressionSearchCriteria criteria) {

        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, AND);

        solrQuery.add("group", TRUE);
        solrQuery.add("group.ngroups", TRUE);
        solrQuery.add("group.field", FieldName.GENE_ZDB_ID.getName());

        solrQuery.setFields(FieldName.GENE_ZDB_ID.getName(), FieldName.ID.getName());
        solrQuery.addSort(FieldName.GENE_SORT.getName(), SolrQuery.ORDER.asc);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        final QueryResponse finalQueryResponse = queryResponse;

        criteria.setNumFound(queryResponse.getGroupResponse().getValues().get(0).getNGroups());

        return queryResponse.getGroupResponse().getValues().get(0).getValues()
                .stream()
                .map(this::getFirstDocumentFromGroup)
                .map(doc -> buildGeneResult(doc, criteria, finalQueryResponse))
                .collect(Collectors.toList());
    }

    public List<FigureResult> getFigureResults(ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, OR);

        solrQuery.addFilterQuery(fq(FieldName.GENE_ZDB_ID, criteria.getGeneZdbID()));

        solrQuery.add("group", TRUE);
        solrQuery.add("group.ngroups", TRUE);
        solrQuery.add("group.field", FieldName.FIG_ZDB_ID.getName());
        solrQuery.add("group.field", FieldName.PUB_ZDB_ID.getName());
        solrQuery.add("group.limit", "100");

        solrQuery.setFields(
                FieldName.ID.getName(),
                FieldName.FIG_ZDB_ID.getName(),
                FieldName.PUB_ZDB_ID.getName(),
                FieldName.XPATRES_ID.getName()
        );

        solrQuery.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
        solrQuery.addSort(FieldName.AUTHOR_SORT.getName(), SolrQuery.ORDER.asc);
        solrQuery.addSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        // needs to be more null safe, I'm sure...
        // also this is assuming that the groups come back in the same order that they were
        // specified. not sure if that's guaranteed.
        criteria.setNumFound(queryResponse.getGroupResponse().getValues().get(0).getNGroups());
        criteria.setPubCount(queryResponse.getGroupResponse().getValues().get(1).getNGroups());

        return queryResponse.getGroupResponse().getValues().get(0).getValues().stream()
                .map(this::buildFigureResult)
                .collect(Collectors.toList());
    }

    public List<ImageResult> getImageResults(ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, OR);

        solrQuery.setFields(
                FieldName.ID.getName(),
                FieldName.IMG_ZDB_ID.getName(),
                FieldName.THUMBNAIL.getName()
        );
        solrQuery.add("group", TRUE);
        solrQuery.add("group.ngroups", TRUE);
        solrQuery.add("group.field", FieldName.FIG_ZDB_ID.getName());
        solrQuery.setRows(5000);
        solrQuery.setStart(0);
        solrQuery.addSort("date", SolrQuery.ORDER.asc);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return queryResponse.getGroupResponse().getValues().get(0).getValues().stream()
                .map(this::getFirstDocumentFromGroup)
                .map(this::buildImageResults)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private FigureResult buildFigureResult(Group group) {
        FigureResult figureResult = new FigureResult();

        Figure figure = publicationRepository.getFigure(group.getGroupValue());
        String pubZdbId = (String) group.getResult().get(0).get(FieldName.PUB_ZDB_ID.getName());
        Publication publication = publicationRepository.getPublication(pubZdbId);

        List<ExpressionResult2> results = group.getResult().stream()
                .flatMap(doc -> ((ArrayList<String>) doc.get(FieldName.XPATRES_ID.getName())).stream())
                .distinct()
                .map(id -> expressionRepository.getExpressionResult2(Integer.parseInt(id)))
                .filter(ExpressionResult2::isExpressionFound)
                .collect(Collectors.toList());

        List<ExpressionFigureStage> expressionFigureStages = results.stream()
                .map(ExpressionResult2::getExpressionFigureStage)
                .collect(Collectors.toList());

        Set<Fish> fish = expressionFigureStages.stream()
                .map(efs -> efs.getExpressionExperiment().getFishExperiment().getFish())
                .collect(Collectors.toCollection(TreeSet::new));

        Set<PostComposedEntity> anatomy = results.stream()
                .map(ExpressionResult2::getEntity)
                .collect(Collectors.toCollection(TreeSet::new));

        DevelopmentStage startStage = expressionFigureStages.stream()
                .map(ExpressionFigureStage::getStartStage)
                .min(DevelopmentStage::compareTo)
                .orElse(null);

        DevelopmentStage endStage = expressionFigureStages.stream()
                .map(ExpressionFigureStage::getEndStage)
                .max(DevelopmentStage::compareTo)
                .orElse(null);

        figureResult.setFigure(figure);
        figureResult.setId(figure.getZdbID());
        figureResult.setPublication(publication);
        figureResult.setFish(fish);
        figureResult.setAnatomy(anatomy);
        figureResult.setStartStage(startStage);
        figureResult.setEndStage(endStage);

        return figureResult;
    }

    private SolrDocument getFirstDocumentFromGroup(Group group) {
        return group.getResult().get(0);
    }

    private GeneResult buildGeneResult(SolrDocument document, ExpressionSearchCriteria criteria, QueryResponse response) {
        GeneResult geneResult = new GeneResult();

        geneResult.setId(document.get(FieldName.GENE_ZDB_ID.getName()).toString());

        geneResult.setExpressionId(document.get(FieldName.ID.getName()).toString());

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneResult.getId());
        geneResult.setGene(gene);
        geneResult.setSymbol(gene.getAbbreviation());

        //throw exception maybe?
        if (gene == null) {
            return geneResult;
        }

        populateFigureInfo(geneResult, criteria);
        populateStageRange(geneResult, criteria, AND, fq(FieldName.GENE_ZDB_ID, gene.getZdbID()));
        injectHighlighting(geneResult, response);
        return geneResult;
    }

    private List<ImageResult> buildImageResults(SolrDocument document) {
        ArrayList idList = (ArrayList) document.get(FieldName.IMG_ZDB_ID.getName());
        ArrayList thumbList = (ArrayList) document.get(FieldName.THUMBNAIL.getName());
        ArrayList<ImageResult> imageResults = new ArrayList<>();
        if (idList != null && thumbList != null && idList.size() == thumbList.size()) {
            for (int i = 0; i < idList.size(); i++) {
                ImageResult result = new ImageResult();
                result.setImageZdbId(idList.get(i).toString());
                result.setImageThumbnail(thumbList.get(i).toString());
                imageResults.add(result);
            }
        }
        return imageResults;
    }

    private void populateFigureInfo(GeneResult result, ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery = applyCriteria(solrQuery, criteria, AND);
        solrQuery.addFilterQuery(fq(FieldName.GENE_ZDB_ID, result.getGene().getZdbID()));
        solrQuery.add("group", TRUE);
        solrQuery.add("group.ngroups", TRUE);
        solrQuery.add("group.field", FieldName.PUB_ZDB_ID.getName());
        solrQuery.add("group.field", FieldName.FIG_ZDB_ID.getName());
        solrQuery.add("group.field", FieldName.HAS_IMAGE.getName());
        solrQuery.setRows(3);
        solrQuery.setStart(0);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        Map<String, GroupCommand> groups = queryResponse.getGroupResponse().getValues().stream()
                .collect(Collectors.toMap(GroupCommand::getName, Function.identity()));
        GroupCommand pubGroup = groups.get(FieldName.PUB_ZDB_ID.getName());
        result.setPublicationCount(pubGroup.getNGroups());
        if (pubGroup.getNGroups() == 1) {
            String pubId = pubGroup.getValues().get(0).getGroupValue();
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubId);
            result.setSinglePublication(publication);
        }
        GroupCommand figGroup = groups.get(FieldName.FIG_ZDB_ID.getName());
        result.setFigureCount(figGroup.getNGroups());
        if (figGroup.getNGroups() == 1) {
            String figId = figGroup.getValues().get(0).getGroupValue();
            Figure figure = RepositoryFactory.getPublicationRepository().getFigure(figId);
            result.setSingleFigure(figure);
        }
        GroupCommand imageGroup = groups.get(FieldName.HAS_IMAGE.getName());
        for (Group group : imageGroup.getValues()) {
            if (StringUtils.equals(group.getGroupValue(), "true")) {
                result.setHasImage(true);
            }
        }
    }

    private void populateStageRange(ExpressionSearchResult result, ExpressionSearchCriteria criteria,
                                    String anatomyBoolean, String... filters) {
        SolrQuery solrQuery = new SolrQuery();
        applyCriteria(solrQuery, criteria, anatomyBoolean);
        for (String filter : filters) {
            solrQuery.addFilterQuery(filter);
        }
        solrQuery.add("stats", TRUE);
        solrQuery.add("stats.field", FieldName.STAGE_HOURS_START.getName());
        solrQuery.add("stats.field", FieldName.STAGE_HOURS_END.getName());
        solrQuery.setRows(0);

        QueryResponse response = null;
        try {
            response = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        Map<String, FieldStatsInfo> stats = response.getFieldStatsInfo();
        Double startHours = (double) stats.get(FieldName.STAGE_HOURS_START.getName()).getMin();
        Double endHours = (double) stats.get(FieldName.STAGE_HOURS_END.getName()).getMax();
        result.setStartStage(ar.getStageByStartHours(startHours.floatValue()));
        result.setEndStage(ar.getStageByEndHours(endHours.floatValue()));
    }

    private void injectHighlighting(GeneResult result, QueryResponse response) {

        List<String> highlightBlacklist = new ArrayList<>();
        highlightBlacklist.add("name");

        String id = result.getExpressionId();
        List<String> highlightSnippets = new ArrayList<>();
        if (response.getHighlighting() != null && response.getHighlighting().get(id) != null) {
            for (String highlightField : response.getHighlighting().get(id).keySet()) {

                if (!highlightBlacklist.contains(highlightField) &&
                        response.getHighlighting().get(id).get(highlightField) != null) {
                    for (String snippet : response.getHighlighting().get(id).get(highlightField)) {

                        String prettyFieldName = SolrService.getPrettyFieldName(highlightField);

                        if (StringUtils.equals(highlightField, FieldName.RELATED_GENE_SYMBOL.toString())) {
                            prettyFieldName = "Expressed Gene Symbol";
                        }

                        highlightSnippets.add("<div class=\"snippet\">"
                                + prettyFieldName
                                + ": " + snippet + "</div>");

                        break;  // just do one, for now
                    }
                }
            }
        }
        if (!highlightSnippets.isEmpty()) {
            StringBuilder out = new StringBuilder();
            for (String snippet : highlightSnippets) {
                out.append(snippet);
            }
            result.setMatchingText(out.toString());
        }
    }

    public SortedMap<String, String> getStageOptions() {
        List<DevelopmentStage> stages = RepositoryFactory.getAnatomyRepository().getAllStagesWithoutUnknown();

        SortedMap<String, String> options = new TreeMap<>();

        stages.stream().sorted().forEach(s -> options.put(s.getOboID(), s.getName()));

        return options;
    }

    private String fq(FieldName fieldName, String value) {
        return fieldName.getName() + ":(\"" + SolrService.luceneEscape(value) + "\")";
    }

    private String any(String... fqs) {
        return String.join(" " + OR + " ", fqs);
    }

}
