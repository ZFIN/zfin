package org.zfin.search.service;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Service;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.presentation.MarkerSearchCriteria;
import org.zfin.search.presentation.MarkerSearchResult;
import org.zfin.util.URLCreator;

import java.util.ArrayList;
import java.util.List;


@Service
public class MarkerSearchService {

    private static Logger logger = Logger.getLogger(MarkerSearchService.class);

    public MarkerSearchCriteria injectFacets(MarkerSearchCriteria criteria) {


        return criteria;
    }


    public MarkerSearchCriteria injectResults(MarkerSearchCriteria criteria) {
        List<MarkerSearchResult> results = new ArrayList<>();

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        query.setQuery(buildQuery(criteria));

        if (StringUtils.isNotEmpty(criteria.getDisplayType())) {
            query.addFilterQuery(FieldName.TYPE.getName() + ":\"" + criteria.getDisplayType() + "\"");
        }

        query.addFacetField(FieldName.TYPE.getName());
        query.addFacetField(FieldName.CHROMOSOME.getName());

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

        criteria.setNumFound(response.getResults().getNumFound());

        for( SolrDocument doc : solrDocumentList) {
            MarkerSearchResult result = buildResult(doc);
            if (result != null) {
                results.add(result);
            }
        }

        criteria.setTypesFound(getTypesFound(response));

        criteria.setResults(results);
        return criteria;
    }

    public String buildQuery(MarkerSearchCriteria criteria) {
        StringBuilder q = new StringBuilder();

        if (StringUtils.isNotEmpty(criteria.getName())) {
            q.append(criteria.getName());
            q.append(" ");
        }

        if (StringUtils.isNotEmpty(criteria.getAccession())) {
            q.append(FieldName.RELATED_ACCESSION.getName());
            q.append(":(");
            q.append(criteria.getAccession());
            q.append(") ");
        }

        //todo: type and chromosome

        return q.toString();
    }

    public MarkerSearchResult buildResult(SolrDocument doc) {
        String id = (String) doc.get(FieldName.ID.getName());
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(id);

        if (marker == null) {
            return null;
        }

        MarkerSearchResult result = new MarkerSearchResult();
        result.setMarker(marker);

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

}
