package org.zfin.search.service;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.service.ExpressionService;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.MarkerTypeSignificanceComparator;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FacetValueAlphanumComparator;
import org.zfin.search.FieldName;
import org.zfin.search.MarkerTypeFacetComparator;
import org.zfin.search.presentation.MarkerSearchCriteria;
import org.zfin.search.presentation.MarkerSearchResult;
import org.zfin.util.URLCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class MarkerSearchService {

    @Autowired
    ExpressionService expressionService;

    private static Logger logger = Logger.getLogger(MarkerSearchService.class);

    public static String BEGINS_WITH = "Begins With";
    public static String CONTAINS = "Contains";
    public static String MATCHES = "Matches";

    public MarkerSearchCriteria injectFacets(MarkerSearchCriteria criteria) {

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        query.setRequestHandler("/marker-search");

        query.addFacetField(FieldName.TYPE.getName());
        query.addFacetField(FieldName.CHROMOSOME.getName());

        query.setRows(0);

        QueryResponse response = new QueryResponse();
        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        criteria.setChromosomeOptions(getFacetStrings(response, FieldName.CHROMOSOME));

        List<MarkerType> markerTypes = RepositoryFactory.getMarkerRepository().getMarkerTypesByGroup(Marker.TypeGroup.SEARCH_MKSEG);
        criteria.setTypeOptions(sortMarkerTypes(markerTypes));

        return criteria;
    }

    public List<String> getFacetStrings(QueryResponse response, FieldName fieldName) {
        List<String> values = new ArrayList<>();

        FacetField facetField = response.getFacetField(fieldName.getName());
        List<FacetField.Count> facetValues = SolrService.sortFacets(facetField, facetField.getValues());

        for (FacetField.Count count : facetValues) {
            values.add(count.getName());
        }

        return values;
    }


    public MarkerSearchCriteria injectResults(MarkerSearchCriteria criteria) {
        List<MarkerSearchResult> results = new ArrayList<>();

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        query.setQuery(buildQuery(criteria, query));

        query.setRequestHandler("/marker-search");

        if (StringUtils.isNotEmpty(criteria.getChromosome())) {
            query.addFilterQuery(FieldName.CHROMOSOME.getName() + ":\"" + criteria.getChromosome() + "\"");
        }

        if (StringUtils.isNotEmpty(criteria.getSelectedType())) {
            query.addFilterQuery(FieldName.TYPE.getName() + ":\"" + criteria.getSelectedType() + "\"");
        }

        if (StringUtils.isNotEmpty(criteria.getDisplayType())) {
            query.addFilterQuery(FieldName.TYPE.getName() + ":\"" + criteria.getDisplayType() + "\"");
            criteria.setSelectedType(criteria.getDisplayType());
        }

        //sorting, handled here so that the name query can be dropped in
        if (StringUtils.isNotEmpty(criteria.getName())) {
            String symbolMatch = "mul(termfreq(name, '" + SolrService.luceneEscape(criteria.getName()) + "'),1000)";
            String alphaRank = "scale(rord(name_sort),1,100)";
            String computedScore = "sum(" + symbolMatch + "," + alphaRank + ")";

            query.setSort(computedScore, SolrQuery.ORDER.desc);
        }


        // pagination
        if (criteria.getPage() != null && criteria.getRows() != null) {
            query.setRows(criteria.getRows());
            int start = (criteria.getPage() - 1) * criteria.getRows();
            query.setStart(start);
            query.setRows(criteria.getRows());
        }

        QueryResponse response = new QueryResponse();
        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        SolrDocumentList solrDocumentList = response.getResults();

        if (response.getResults() != null) {
            criteria.setNumFound(response.getResults().getNumFound());
        } else {
            criteria.setNumFound((long)0);
        }

        criteria.setResults(results);

        for( SolrDocument doc : solrDocumentList) {
            MarkerSearchResult result = buildResult(doc);
            if (result != null) {
                results.add(result);
            }
        }

        injectHighlighting(results, response);

        criteria.setTypesFound(sortMarkerTypeFacet(getTypesFound(response)));

        if (CollectionUtils.size(criteria.getTypesFound()) == 1) {
            criteria.setDisplayType(criteria.getTypesFound().iterator().next().getName());
        }

        return criteria;
    }

    public String buildQuery(MarkerSearchCriteria criteria, SolrQuery query) {

        StringBuilder q = new StringBuilder();

        if (StringUtils.isNotEmpty(criteria.getName())) {
            String nameQuery = criteria.getName();

            nameQuery = SolrService.luceneEscape(nameQuery);

            if (StringUtils.equals(criteria.getMatchType(), MATCHES)) {
                q.append(nameQuery);
            } else if (StringUtils.equals(criteria.getMatchType(),BEGINS_WITH)) {
                q.append(nameQuery + "*");
            } else if (StringUtils.equals(criteria.getMatchType(),CONTAINS)) {
                q.append("*" + nameQuery + "*");
            }

            q.append(" ");
        }

        if (StringUtils.isNotEmpty(criteria.getAccession())) {
            q.append("(");

            q.append(FieldName.RELATED_ACCESSION.getName());
            q.append(":(");
            q.append(criteria.getAccession());
            q.append(") ");

            q.append(" OR ");
            q.append(FieldName.RELATED_ACCESSION_TEXT.getName());
            q.append(":(");
            q.append(criteria.getAccession());
            q.append(") ");

            q.append(")");

        }

        return q.toString();
    }

    public MarkerSearchResult buildResult(SolrDocument doc) {
        String id = (String) doc.get(FieldName.ID.getName());

        MarkerSearchResult result = new MarkerSearchResult();

        Marker marker;
        MarkerRelationship mrel;

        if (id.startsWith("ZDB-MREL")) {
            mrel = RepositoryFactory.getMarkerRepository().getMarkerRelationshipByID(id);
            marker = mrel.getFirstMarker();
            result.setTargetGene(mrel.getSecondMarker());
        } else {
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(id);
        }


        if (marker == null) {
            return null;
        }


        result.setMarker(marker);

        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            result.setMarkerExpression(expressionService.getExpressionForGene(marker));
            result.setMarkerPhenotype(MarkerService.getPhenotypeOnGene(marker));
        }


        return result;
    }

    public List<FacetField.Count> getTypesFound(QueryResponse response) {
        List<FacetField.Count> types = new ArrayList<>();

        FacetField type = response.getFacetField(FieldName.TYPE.getName());
        if (type != null) {
            for (FacetField.Count count : type.getValues()) {
                types.add(count);
            }
        }


        return types;
    }

    private void injectHighlighting(List<MarkerSearchResult> results, QueryResponse response) {
        for (MarkerSearchResult result : results) {
            String id = result.getMarker().getZdbID();
            List<String> highlightSnippets = new ArrayList<String>();
            if (response.getHighlighting() != null && response.getHighlighting().get(id) != null) {

                for (String highlightField : response.getHighlighting().get(id).keySet()) {
                    logger.debug("highlight field keys? => " + response.getHighlighting().get(id).keySet());
                    if (response.getHighlighting().get(id).get(highlightField) != null) {
                        for (String snippet : response.getHighlighting().get(id).get(highlightField)) {
                            logger.debug("snippet: " + snippet);

                            highlightSnippets.add("<div class=\"snippet\">"
                                    + SolrService.getPrettyFieldName(highlightField)
                                    + ": " + snippet + "</div>");

                        }
                    }
                break;  // just do one, for now
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

    }


    public List<String> sortMarkerTypes(List<MarkerType> markerTypes) {
        List<String> returnStrings = new ArrayList<>();
        Collections.sort(markerTypes, new MarkerTypeSignificanceComparator<MarkerType>());
        returnStrings.addAll(CollectionUtils.collect(markerTypes, new BeanToPropertyValueTransformer("displayName")));
        return returnStrings;
    }

    public List<FacetField.Count> sortMarkerTypeFacet(List<FacetField.Count> countList) {
        Collections.sort(countList, new MarkerTypeFacetComparator<>());
        return countList;
    }



}
