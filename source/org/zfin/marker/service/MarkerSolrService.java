package org.zfin.marker.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.Marker;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MarkerSolrService {

    public void addMarkerStub(Marker marker, Category category) throws IOException, SolrServerException {
        addMarkerStub(marker, category, new HashMap<String,Object>());
    }

    public void addMarkerStub(Marker marker, Category category, Map<String,Object> extras) throws IOException, SolrServerException {

        Map<FieldName, Object> solrDoc = new HashMap<>(12);
        solrDoc.put(FieldName.ID, marker.getZdbID());
        solrDoc.put(FieldName.CATEGORY, category.getName());
        solrDoc.put(FieldName.TYPE, marker.getMarkerType().getDisplayName());
        solrDoc.put(FieldName.NOTE, marker.getPublicComments());
        solrDoc.put(FieldName.NAME, marker.getAbbreviation());
        solrDoc.put(FieldName.PROPER_NAME, marker.getAbbreviation());
        solrDoc.put(FieldName.GENE, marker.getAbbreviation());
        solrDoc.put(FieldName.FULL_NAME, marker.getName());
        solrDoc.put(FieldName.GENE_FULL_NAME, marker.getName());
        solrDoc.put(FieldName.NAME_SORT, marker.getAbbreviationOrder());
        solrDoc.put(FieldName.URL, "/" + marker.getZdbID());
        solrDoc.put(FieldName.DATE, new Date());

        if (marker.getAliases() != null) {
            List<String> aliases = marker.getAliases().stream()
                    .map(DataAlias::getAlias)
                    .collect(Collectors.toList());
            solrDoc.put(FieldName.ALIAS, aliases);
            solrDoc.put(FieldName.GENE_PREVIOUS_NAME, aliases);
        }

        SolrService.addDocument(solrDoc, extras);

    }

}
