package org.zfin.search.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.zfin.search.Category;
import org.zfin.search.FieldName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SolrQueryFacade {

    private SolrQuery query;

    private SolrQueryFacade(SolrQuery query) {
        this.query = query;
    }

    public static SolrQueryFacade addTo(SolrQuery query) {
        return new SolrQueryFacade(query);
    }

    public SolrQueryFacade category(Category category) {
        return fq(category.getName(), FieldName.CATEGORY);
    }

    public SolrQueryFacade fq(String value, FieldName... fields) {
        if (StringUtils.isNotEmpty(value)) {
            query.addFilterQuery(Arrays.stream(fields)
                    .map(field -> fqString(value, field))
                    .collect(Collectors.joining(" OR "))
            );
        }
        return this;
    }

    public SolrQueryFacade fqAny(FieldName field) {
        return fqRange("*", "*", field);
    }

    public SolrQueryFacade fqNotAny(FieldName field) {
        query.addFilterQuery("-" + field.getName() + ":[* TO *]");
        return this;
    }

    public SolrQueryFacade fqRange(String from, String to, FieldName field) {
        query.addFilterQuery(field.getName() + ":[" + from + " TO " + to + "]");
        return this;
    }

    public SolrQueryFacade fqLessThanOrEqual(String to, FieldName field) {
        return fqRange("*", to, field);
    }

    public SolrQueryFacade fqGreaterThanOrEqual(String from, FieldName field) {
        return fqRange(from, "*", field);
    }

    public SolrQueryFacade fqParsed(String value, FieldName... fields) {
        if (StringUtils.isNotEmpty(value)) {
            query.addFilterQuery("{!edismax qf='" +
                    Arrays.stream(fields).map(FieldName::getName).collect(Collectors.joining(" ")) +
                    "'}" + SolrService.luceneEscape(value));
        }
        return this;
    }

    private String fqString(String value, FieldName field) {
        return field.getName() + ":\"" + SolrService.luceneEscape(value) + "\"";
    }

}
