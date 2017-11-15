package org.zfin.search.service;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
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
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.MarkerSearchTypeGroupComparator;
import org.zfin.search.presentation.FacetValue;
import org.zfin.search.presentation.MarkerSearchCriteria;
import org.zfin.search.presentation.MarkerSearchResult;

import java.util.*;
import java.util.stream.Collectors;
import static org.zfin.search.service.SolrQueryFacade.addTo;


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

        query.addFacetField(FieldName.TYPEGROUP.getName());
        query.addFacetField(FieldName.CHROMOSOME.getName());

        query.setRows(0);

        QueryResponse response = new QueryResponse();
        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        criteria.setChromosomeOptions(getFacetStrings(response, FieldName.CHROMOSOME));
        criteria.setTypeOptions(getFacetStrings(response, FieldName.TYPEGROUP));

//        List<MarkerType> markerTypes = RepositoryFactory.getMarkerRepository().getMarkerTypesByGroup(Marker.TypeGroup.SEARCH_MKSEG);
//        criteria.setTypeOptions(sortMarkerTypes(markerTypes));

        return criteria;
    }

    public List<String> getFacetStrings(QueryResponse response, FieldName fieldName) {
        FacetField facetField = response.getFacetField(fieldName.getName());

        List<FacetValue> sortedFacetValues = SolrService.sortFacetValues(facetField, buildFacetValues(facetField.getValues()));

        return sortedFacetValues.stream().map(FacetValue::getLabel).collect(Collectors.toList());
    }


    public MarkerSearchCriteria injectResults(MarkerSearchCriteria criteria) {
        List<MarkerSearchResult> results = new ArrayList<>();

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        applyQuery(criteria, query);

        query.setRequestHandler("/marker-search");

        if (StringUtils.isNotEmpty(criteria.getChromosome())) {
            query.addFilterQuery(FieldName.CHROMOSOME.getName() + ":\"" + criteria.getChromosome() + "\"");
        }

        if (StringUtils.isNotEmpty(criteria.getSelectedType())) {
            query.addFilterQuery(FieldName.TYPEGROUP.getName() + ":\"" + criteria.getSelectedType() + "\"");
        }

        if (StringUtils.isNotEmpty(criteria.getDisplayType())) {
            query.addFilterQuery(FieldName.TYPEGROUP.getName() + ":\"" + criteria.getDisplayType() + "\"");
            criteria.setSelectedType(criteria.getDisplayType());
        }

        //sorting, handled here so that the name query can be dropped in
        //starts with matches against the abbrev will get alphabetized first, everything else alphabetized after
        if (StringUtils.isNotEmpty(criteria.getName())) {
            String symbolMatch = "mul(termfreq(name_ac, '" + SolrService.luceneEscape(criteria.getName()) + "'),1000)";
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

        criteria.setTypesFound(getTypesFound(response));

        if (CollectionUtils.size(criteria.getTypesFound()) == 1) {
            criteria.setDisplayType(criteria.getTypesFound().iterator().next().getName());
        }

        return criteria;
    }

    public void applyQuery(MarkerSearchCriteria criteria, SolrQuery query) {

        Map<FieldName, String> startsWithFields = new HashMap<>();
        startsWithFields.put(FieldName.NAME_AC,"^10");
        startsWithFields.put(FieldName.FULL_NAME_AC,"^3");
        startsWithFields.put(FieldName.ALIAS_AC,"^5");
        startsWithFields.put(FieldName.ORTHOLOG_OTHER_SPECIES_SYMBOL_AUTOCOMPLETE,"^2");
        startsWithFields.put(FieldName.ORTHOLOG_OTHER_SPECIES_NAME_AUTOCOMPLETE,"");
        startsWithFields.put(FieldName.RELATED_GENE_SYMBOL,"");
        startsWithFields.put(FieldName.GENE_PREVIOUS_NAME_AUTOCOMPLETE,"");
        startsWithFields.put(FieldName.GENE_FULL_NAME_AUTOCOMPLETE,"");
        startsWithFields.put(FieldName.CLONE_AUTOCOMPLETE,"");
        startsWithFields.put(FieldName.TARGET_FULL_NAME_AUTOCOMPLETE,"");
        startsWithFields.put(FieldName.TARGET_PREVIOUS_NAME_AUTOCOMPLETE,"");
        Map<FieldName, String> matchesFields = new HashMap<>();
        matchesFields.putAll(startsWithFields);
        matchesFields.put(FieldName.FULL_NAME,"^3");

        if (StringUtils.isNotEmpty(criteria.getName())) {

            if (StringUtils.equals(criteria.getMatchType(),BEGINS_WITH)) {
                query.setQuery(SolrService.dismax(criteria.getName(),startsWithFields));

            } else if (StringUtils.equals(criteria.getMatchType(),CONTAINS)) {
                //String wildcardQuery = "*" + SolrService.luceneEscape(criteria.getName() + "*");
                String wildcardQuery = "*" + criteria.getName();
                query.setQuery(SolrService.dismax(wildcardQuery, matchesFields, false));
            } else {  //default to MATCHES
                query.setQuery(SolrService.dismax(criteria.getName(), matchesFields));
            }


        }

        if (StringUtils.isNotEmpty(criteria.getAccession())) {
            addTo(query)
                    .fq(criteria.getAccession(),
                            FieldName.RELATED_ACCESSION,
                            FieldName.RELATED_ACCESSION_TEXT);
        }
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

        FacetField type = response.getFacetField(FieldName.TYPEGROUP.getName());
        if (type != null) {
            for (FacetField.Count count : type.getValues()) {
                types.add(count);
            }
        }

        Collections.sort(types, new MarkerSearchTypeGroupComparator<>());

        logger.error("types: " + types);

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

                            String prettyFieldName = SolrService.getPrettyFieldName(highlightField);

                            FieldName fieldName = FieldName.getFieldName(highlightField);

                            //for genes only, the field name for 'name' fields should be 'Current Symbol'
                            if (result.getMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)
                                && (fieldName == FieldName.NAME
                                    || fieldName == FieldName.NAME_AC)) {
                                prettyFieldName = "Current Symbol";
                            }

                            highlightSnippets.add("<div class=\"snippet\">"
                                    + prettyFieldName
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


    /*public List<String> sortMarkerTypes(List<MarkerType> markerTypes) {
        List<String> returnStrings = new ArrayList<>();
        Collections.sort(markerTypes, new MarkerTypeSignificanceComparator<MarkerType>());
        returnStrings.addAll(CollectionUtils.collect(markerTypes, new BeanToPropertyValueTransformer("displayName")));
        return returnStrings;
    }
*/
    public List<FacetField.Count> sortMarkerTypeFacet(List<FacetField.Count> countList) {
        Collections.sort(countList, new MarkerSearchTypeGroupComparator<>());
        return countList;
    }

    //This is the slower, but "safer" (less redundant?) option, alternatively,
    // we could store the display name and signficiance in the Marker.Type enumeration
    public List<String> sortMarkerTypes(List<String> typeStrings) {
        List<MarkerType> markerTypes = getSortedMarkerTypes(typeStrings);
        List<String> returnStrings = new ArrayList<>();
        Collections.sort(markerTypes, new MarkerTypeSignificanceComparator<MarkerType>());
        returnStrings.addAll(CollectionUtils.collect(markerTypes, new BeanToPropertyValueTransformer("displayName")));
        return returnStrings;
    }

    public List<MarkerType> getSortedMarkerTypes(List<String> typeStrings) {
        List<MarkerType> markerTypes = new ArrayList<>();

        for (String typeString : typeStrings) {
            markerTypes.add(RepositoryFactory.getMarkerRepository().getMarkerTypeByDisplayName(typeString));
        }
        return markerTypes;
    }


    private List<FacetValue> buildFacetValues(List<FacetField.Count> counts) {
        List<FacetValue> values = new ArrayList<>();
        for (FacetField.Count count : counts) {
            values.add(new FacetValue(count));
        }
        return values;
    }

}
