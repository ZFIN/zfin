package org.zfin.expression.service;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.ExpressionSearchCriteria;
import org.zfin.expression.presentation.FigureResult;
import org.zfin.expression.presentation.GeneResult;
import org.zfin.expression.presentation.ImageResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.QueryManipulationService;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kschaper on 1/17/17.
 */
@Service
public class ExpressionSearchService {

    public static SolrQuery applyCriteria(SolrQuery solrQuery,
                                          ExpressionSearchCriteria criteria,
                                          String anatomyBoolean) {

        solrQuery.addFilterQuery(FieldName.CATEGORY.getName() + ":(" + "Expression" + ")");

        //only interested in expression where there is a zebrafish gene, no reporter & no AB expression
        solrQuery.addFilterQuery(FieldName.ZEBRAFISH_GENE.getName() + ":[* TO *]");

        if (CollectionUtils.isNotEmpty(criteria.getAnatomy())) {
            String termQuery = criteria.getAnatomy().stream()
                    .collect(Collectors.joining(" " + anatomyBoolean + " "));
            solrQuery.addFilterQuery(FieldName.EXPRESSION_ANATOMY_TF.getName() + ":(" + termQuery + ")");
        }

        if (StringUtils.isNotEmpty(criteria.getGeneField())) {
            StringBuilder gfq = new StringBuilder();
            gfq.append(FieldName.ZEBRAFISH_GENE.getName() + ":(" + SolrService.luceneEscape(criteria.getGeneField()) + ")");
            gfq.append(" OR zebrafish_gene_t:(" + SolrService.luceneEscape(criteria.getGeneField()) + ")");
            gfq.append(" OR " + "expressed_gene_full_name:(" + SolrService.luceneEscape(criteria.getGeneField()) + ")");
            gfq.append(" OR " + "expressed_gene_previous_name:(" + SolrService.luceneEscape(criteria.getGeneField()) + ")");
            solrQuery.addFilterQuery(gfq.toString());
        }

        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            solrQuery.addFilterQuery("gene_zdb_id:(" + criteria.getGeneZdbID() + ")");
        }

        if (StringUtils.isNotEmpty(criteria.getStartStageId()) && StringUtils.isNotEmpty(criteria.getEndStageId())) {
            DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStageByOboID(criteria.getStartStageId());
            DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getStageByOboID(criteria.getEndStageId());

            if (start != null && end != null) {
                solrQuery.addFilterQuery(SolrService.buildStageRangeQuery(
                        FieldName.STAGE_HOURS_START,
                        "[", DevelopmentStage.MIN, end.getHoursEnd(),  "}"
                ));
                solrQuery.addFilterQuery(SolrService.buildStageRangeQuery(
                        FieldName.STAGE_HOURS_END,
                        "{", start.getHoursStart(), DevelopmentStage.MAX, "]"
                ));
            }

        }


        if (criteria.isOnlyFiguresWithImages()) {
            solrQuery.addFilterQuery(FieldName.HAS_IMAGE.getName() + ":(true)");
        }
        
        solrQuery.setRows(criteria.getRows());
        solrQuery.setStart((criteria.getPage() - 1) * criteria.getRows());

        return solrQuery;
    }

    public static List<GeneResult> getGeneResults(ExpressionSearchCriteria criteria) {

        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, "AND");

        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", "gene_zdb_id");

        solrQuery.setFields("gene_zdb_id");

        solrQuery.setRows(100);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        criteria.setNumFound(queryResponse.getGroupResponse().getValues().get(0).getNGroups());

        List<SolrDocument> solrDocumentList = queryResponse.getGroupResponse().getValues().get(0).getValues()
                .stream()
                .map(ExpressionSearchService::getFirstDocumentFromGroup)
                .collect(Collectors.toList());

        return buildGeneResults(solrDocumentList, criteria);
    }

    public static List<FigureResult> getFigureResults(ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, "OR");

        solrQuery.addFilterQuery("gene_zdb_id:(" + criteria.getGeneZdbID() + ")");

        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", "fig_zdb_id");
        solrQuery.add("group.field", "pub_zdb_id");

        solrQuery.setFields("id", "fig_zdb_id", "pub_zdb_id", "fish_zdb_id");

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // needs to be more null safe, I'm sure...
        // also this is assuming that the groups come back in the same order that they were
        // specified. not sure if that's guaranteed.
        criteria.setNumFound(queryResponse.getGroupResponse().getValues().get(0).getNGroups());
        criteria.setPubCount(queryResponse.getGroupResponse().getValues().get(1).getNGroups());

        List<SolrDocument> solrDocumentList = queryResponse.getGroupResponse().getValues().get(0).getValues()
                .stream()
                .map(ExpressionSearchService::getFirstDocumentFromGroup)
                .collect(Collectors.toList());

        return buildFigureResults(solrDocumentList);

    }

    public static List<ImageResult> getImageResults(ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, "OR");

        solrQuery.setFields("id", "img_zdb_id", "thumbnail");
        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", "fig_zdb_id");
        solrQuery.setRows(5000);
        solrQuery.setStart(1);
        solrQuery.addSort("date", SolrQuery.ORDER.asc);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return queryResponse.getGroupResponse().getValues().get(0).getValues().stream()
                .map(ExpressionSearchService::getFirstDocumentFromGroup)
                .map(ExpressionSearchService::buildImageResults)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static List<FigureResult> buildFigureResults(List<SolrDocument> solrDocumentList) {
        return solrDocumentList.stream()
                .map(it -> buildFigureResult(it))
                .collect(Collectors.toList());
    }

    public static FigureResult buildFigureResult(SolrDocument document) {
        FigureResult figureResult = new FigureResult();

        Figure figure = RepositoryFactory.getPublicationRepository().getFigure((String) document.get("fig_zdb_id"));
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication((String) document.get("pub_zdb_id"));
        Fish fish = RepositoryFactory.getMutantRepository().getFish((String) document.get("fish_zdb_id"));

        figureResult.setFigure(figure);
        figureResult.setPublication(publication);
        figureResult.setFish(fish);

        return figureResult;
    }

    public static List<GeneResult> buildGeneResults(List<SolrDocument> solrDocumentList, ExpressionSearchCriteria criteria) {
        return solrDocumentList.stream()
                .map(d -> buildGeneResult(d, criteria))
                .collect(Collectors.toList());
    }

    public static SolrDocument getFirstDocumentFromGroup(Group group) {
        return group.getResult().get(0);
    }

    public static GeneResult buildGeneResult(SolrDocument document, ExpressionSearchCriteria criteria) {
        GeneResult geneResult = new GeneResult();

        geneResult.setId(document.get("gene_zdb_id").toString());

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneResult.getId());
        geneResult.setGene(gene);
        geneResult.setSymbol(gene.getAbbreviation());

        //throw exception maybe?
        if (gene == null) {
            return geneResult;
        }

        geneResult.setPublicationCount(getPublicationCount(gene, criteria));
        geneResult.setFigureCount(getFigureCount(gene, criteria));


        return geneResult;
    }

    public static List<ImageResult> buildImageResults(SolrDocument document) {
        ArrayList idList = (ArrayList) document.get("img_zdb_id");
        ArrayList thumbList = (ArrayList) document.get("thumbnail");
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

    public static Integer getPublicationCount(Marker gene, ExpressionSearchCriteria criteria) {
        return getCount(gene, criteria, "publication");
    }

    public static Integer getFigureCount(Marker gene, ExpressionSearchCriteria criteria) {
        return getCount(gene, criteria, "fig_zdb_id");
    }

    public static Integer getCount(Marker gene, ExpressionSearchCriteria criteria, String groupingField) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery = applyCriteria(solrQuery, criteria, "OR");

        solrQuery.addFilterQuery("gene_zdb_id:(" + gene.getZdbID() + ")");

        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", groupingField);
        solrQuery.setRows(0);

        QueryResponse queryResponse = null;
        try {
            queryResponse = SolrService.getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //todo: needs to be a little more null safe...
        return queryResponse.getGroupResponse().getValues().iterator().next().getNGroups();

    }

    public static SortedMap<String, String> getStageOptions() {
        List<DevelopmentStage> stages = RepositoryFactory.getAnatomyRepository().getAllStagesWithoutUnknown();

        SortedMap<String,String> options = new TreeMap<>();

        stages.stream().sorted().forEach(s -> options.put(s.getOboID(), s.getName()));

        return options;
    }

}
