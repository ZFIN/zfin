package org.zfin.search.service;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Service;
import org.zfin.search.FieldName;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchSuggestionService {

    public static Logger logger = Logger.getLogger(SearchSuggestionService.class);

    public List<String> getSuggestions(String queryString) {
        //todo: add a spellcheck layer here?
        return getSynonymSuggestions(queryString);
    }

    public List<String> getSynonymSuggestions(String queryString) {

        List<String> suggestions = new ArrayList<>();

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        query.setQuery(FieldName.ALIAS_KEYWORD.getName() + ":\"" + queryString + "\"");
        query.setFields(FieldName.NAME.getName());

        QueryResponse response = new QueryResponse();

        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        for (SolrDocument doc : response.getResults()) {
            suggestions.add((String) doc.get(FieldName.NAME.getName()));
        }

        return suggestions;
    }



}
