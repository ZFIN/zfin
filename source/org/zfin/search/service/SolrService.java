package org.zfin.search.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Service;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.search.*;
import org.zfin.search.presentation.FacetValue;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Service class for Solr methods not specific to any particular area of zfin
 */
@Service
public class SolrService {

    public static Logger logger = LogManager.getLogger(SolrService.class);

    private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\[\\{\\}\\~\\*\\?\\\"\\,]";
    private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
    private static final String REPLACEMENT_STRING = "\\\\$0";
    private static final String SEARCH_URL = "/search";
    private static final String PRIMARY_CORE = "prototype";
    private static final String IDLE = "idle";
    private static final int CURSOR_BATCH_SIZE = 100;

    private static SolrClient prototype;

    public static SolrClient getSolrClient() {
        return getSolrClient(PRIMARY_CORE);
    }

    public static SolrClient getSolrClient(String core) {

        if (prototype == null) {

            StringBuilder solrUrl = new StringBuilder();
            solrUrl.append("http://");
            solrUrl.append(ZfinPropertiesEnum.SOLR_HOST.value());
            solrUrl.append(":");
            solrUrl.append(ZfinPropertiesEnum.SOLR_PORT.value());
            solrUrl.append("/");
            solrUrl.append(ZfinPropertiesEnum.SOLR_CONTEXT.value());
            solrUrl.append("/");
            solrUrl.append(core);
            solrUrl.append("/");


            try {
                prototype = new HttpSolrClient(solrUrl.toString());
            } catch (Exception e) {
                logger.error("couldn't get SolrServer", e);
            }
        }

        return prototype;
    }


    public static String getSearchLinkPrefix() {
        StringBuilder link = new StringBuilder();
        link.append("<a href=\"");
        link.append(SEARCH_URL);
        return link.toString();
    }

    public static String getSearchUrlPrefix() {
        StringBuilder url = new StringBuilder();
        url.append(SEARCH_URL);
        return url.toString();
    }

    public static ArrayList<FacetValue> reorderFacetField(FacetField facetField, Map<String, Boolean> fqMap) {
        //We want to show selected values first, split the list into two groups
        //then put it back together...
        ArrayList<FacetValue> facetValues = new ArrayList<>();
        //The order of execution is different than the order of values... odd
        List<FacetValue> unselectedValues = getUnselectedValues(facetField, fqMap);
        List<FacetValue> selectedValues = getSelectedValues(facetField, fqMap);

        selectedValues = sortFacetValues(facetField, selectedValues);
        unselectedValues = sortFacetValues(facetField, unselectedValues);

        facetValues.addAll(unselectedValues);
        facetValues.addAll(selectedValues);

        return facetValues;
    }


    public static List<FacetValue> sortFacetValues(FacetField facetField, List<FacetValue> inputValues) {
        List<FacetValue> outputValues = new ArrayList<>();
        outputValues.addAll(inputValues);
        if (SolrService.isToBeHumanSorted(facetField.getName())) {
            Collections.sort(outputValues, new FacetValueAlphanumComparator());
        }
        if (facetField.getName().equals("category")) {
            Collections.sort(outputValues, new FacetCategoryComparator());
        }
        return outputValues;
    }

    private static List<FacetValue> getSelectedValues(FacetField facetField, Map<String, Boolean> fqMap) {
        return getFacetValues(facetField, fqMap, true);
    }

    private static List<FacetValue> getUnselectedValues(FacetField facetField, Map<String, Boolean> fqMap) {
        return getFacetValues(facetField, fqMap, false);
    }

    private static List<FacetValue> getFacetValues(FacetField facetField, Map<String, Boolean> fqMap, boolean selected) {
        List<FacetValue> values = new ArrayList<>();

        for (FacetField.Count count : facetField.getValues()) {
            String quotedFq = facetField.getName() + ":\"" + count.getName() + "\"";
            if (!selected == fqMap.containsKey(quotedFq)) {
                FacetValue value = new FacetValue(count);
                value.setSelected(selected);
                values.add(value);
            }
        }
        return values;
    }


    public static void setCategory(String categoryName, SolrQuery query) {

        Category category = Category.getCategory(categoryName);

        if (category == null || StringUtils.equals(categoryName, "Any")) {
            query.addFacetField("category");
            //100 is high enough that we should always get all category values back
            query.set("f.category.facet.limit", "100");
        } else {

            for (FacetQueryEnum facetQueryEnum : category.getFacetQueries()) {
                query.addFacetQuery(facetQueryEnum.getQuery());
            }
            for (String facetQuery : FacetBuilderService.getPublicationDateQueries().values()) {
                query.addFacetQuery(facetQuery);
            }

            String[] facetFields = category.getFieldArray();
            for (String ff : facetFields) {
                if (!StringUtils.isEmpty(ff)) {
                    query.addFacetField(ff);
                    query.addFacetQuery(ff + ":[* TO *]");
                }
            }
        }
        //facet on images no matter what
        query.addFacetField(FieldName.IMG_ZDB_ID.getName());

        if (category != null && CollectionUtils.isNotEmpty(category.getPivotFacetStrings())) {
            category.getPivotFacetStrings().forEach(pivot -> {
                query.addFacetPivotField(pivot);
            });
        }

    }


    public static String getWildtypeExpressionLink(String geneSymbol) {
        return getExpressionLink(geneSymbol, true);
    }

    public static String getAllExpressionLink(String geneSymbol) {
        return getExpressionLink(geneSymbol, false);
    }


    public static String getExpressionLink(String geneSymbol, boolean onlyWildtype) {
        SolrClient server = getSolrClient("prototype");
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("zebrafish_gene:\"" + geneSymbol + "\"");
        if (onlyWildtype) {
            query.addFilterQuery("is_genotype_wildtype:\"true\"");
            query.addFilterQuery(FieldName.CONDITIONS.getName() + ":\"standard or control\"");
        }
        query.addFilterQuery("category:Expression");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(100);
        query.addFacetField("category");

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        StringBuilder link = new StringBuilder();
        //loop, even though only one is expected back
        for (FacetField.Count count : response.getFacetField("category").getValues()) {
            link.append(getSearchLinkPrefix());
            link.append("?fq=category:Expression");
            link.append("&fq=zebrafish_gene%3A%22");
            link.append(geneSymbol);
            link.append("%22");

            if (onlyWildtype) {
                link.append("&fq=is_genotype_wildtype%3Atrue");
                link.append("&fq=" + FieldName.CONDITIONS.getName() + "%3A%22standard+or+control%22");
            }

            link.append("\">");

            if (onlyWildtype) {
                link.append("Wildtype Expression for ");
            } else {
                link.append("All Expression for ");
            }
            link.append("<span class=\"genedom\">");
            link.append(geneSymbol);
            link.append("</span>");
            link.append(" (");
            link.append(count.getCount());
            link.append(") </a>");
        }

        return link.toString();
    }

    public static Map<String, String> getExpressionTermLinks(String geneSymbol) {
        SolrClient server = getSolrClient("prototype");
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("zebrafish_gene:\"" + geneSymbol + "\"");
        query.addFilterQuery("is_wildtype:\"true\"");
        query.addFilterQuery("category:Expression");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setFacetSort("name");
        query.addFacetField("expression_anatomy_direct");

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }


        Map<String, String> expressionLinks = new LinkedHashMap<>();
        for (FacetField.Count count : response.getFacetField("expression_anatomy_direct").getValues()) {

            StringBuilder link = new StringBuilder();
            link.append(getSearchLinkPrefix());
            link.append("?fq=category:Expression");
            link.append("&fq=zebrafish_gene:");
            link.append(geneSymbol);

            String fq = SolrService.encode("expression_anatomy_direct" + ":\"" + count.getName() + "\"");
            link.append("&fq=");
            link.append(fq);

            link.append("&fq=is_wildtype%3A%22true%22");

            link.append("\">");
            link.append("Expression");
            link.append(" (");
            link.append(count.getCount());
            link.append(") </a>");

            expressionLinks.put(count.getName(), link.toString());

        }

        return expressionLinks;
    }


    public static String getExpressionLink(String geneSymbol, String termName) {
        String link = getExpressionLink(geneSymbol, termName, FieldName.EXPRESSION_ANATOMY_TF.getName());
        //if no link is returned, it might be a high level term that has been chopped out of the _TF field, so
        //try again against the non-TF field
        if (StringUtils.isEmpty(link)) {
            link = getExpressionLink(geneSymbol, termName, FieldName.EXPRESSION_ANATOMY.getName());
        }
        return link;
    }

    public static String getExpressionLink(String geneSymbol, String termName, String fieldName) {
        //query Solr to get the count...

        SolrClient server = getSolrClient("prototype");
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("zebrafish_gene:\"" + geneSymbol + "\"");
        query.addFilterQuery(fieldName + ":\"" + termName + "\"");
        query.addFilterQuery("is_wildtype:\"true\"");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(100);
        query.addFacetField("category");

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        StringBuilder link = new StringBuilder();
        //loop, even though only one is expected back
        for (FacetField.Count count : response.getFacetField("category").getValues()) {
            link.append(getSearchLinkPrefix());

            link.append("?fq=category:Expression");

            link.append("&fq=zebrafish_gene%3A%22");
            link.append(geneSymbol);
            link.append("%22");

            String fq = SolrService.encode(fieldName + ":" + getEscapedValue(termName));
            link.append("&fq=");
            link.append(fq);

            link.append("&fq=is_wildtype:true");

            link.append("\">");
            link.append(count.getName().replace("\"", ""));
            link.append(" (");
            link.append(count.getCount());
            link.append(") </a>");
        }

        return link.toString();
    }


    public static Map<String, String> getPhenotypeLink(String geneSymbol) {

        QueryResponse response = getGenePhenotypeQuery(geneSymbol, false);

        //loop, even though only one is expected back
        List<FacetField.Count> phenotypeStatementList = response.getFacetField("phenotype_statement").getValues();
        Map<String, String> linkedMap = new TreeMap<>();
        for (FacetField.Count entry : phenotypeStatementList) {
            StringBuilder link = new StringBuilder();
            link.append(getSearchLinkPrefix());
            link.append("?fq=category:Phenotype");
            link.append("&fq=gene%3A%22");
            link.append(geneSymbol);
            link.append("%22");

            String fq = SolrService.encode("phenotype_statement" + ":" + getEscapedValue(entry.getName()));
            link.append("&fq=");
            link.append(fq);

            link.append("\">");
            link.append("Phenotype");
            link.append(" (");
            link.append(entry.getCount());
            link.append(") </a>");
            linkedMap.put(entry.getName(), link.toString());
        }

        return linkedMap;
    }

    protected static QueryResponse getGenePhenotypeQuery(String geneSymbol, boolean isMonogenic) {
        SolrClient server = getSolrClient("prototype");
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("gene:" + geneSymbol);
        if (isMonogenic) {
            query.addFilterQuery("is_monogenic:true");
        }
        //only look for genes
        query.addFilterQuery("category:Phenotype");

        query.setRows(100);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.addFacetField("phenotype_statement");

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    private static String getEscapedValue(String entry) {
        return "\"" + entry + "\"";
    }

    public QueryResponse getRelatedDataResponse(String id) {
        String field = "category";
        SolrClient server = getSolrClient("prototype");
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("xref:\"" + id + "\"");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(100);
        query.addFacetField(field);

        query.set("qf", "xref");

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        return response;
    }


    public static String getBreadBoxLink(String fq, String baseUrl, boolean showLabel) {
        StringBuilder out = new StringBuilder();

        NameValuePair nameValuePair = SolrService.splitFilterQuery(fq);

        if (nameValuePair != null && nameValuePair.getName() != null && nameValuePair.getValue() != null) {
            if (showLabel) {
                out.append("<li class=\"breadbox-value row-fluid\">");
                out.append("<span class=\"breadbox-facet-label\">");
                out.append(getPrettyFieldName(nameValuePair.getName()));
                out.append(":</span> ");
            }
            out.append("<a class=\"breadbox-link\" href=\"");
            out.append(getBreadBoxUrl(fq, baseUrl));
            out.append("\">");
            out.append("<img class=\"checkbox-icon\" src=\"/images/icon-checked.png\"/>");
            String displayValue = nameValuePair.getValue().replace("\"", "");
            out.append(displayValue);
            out.append("</a>");
            if (showLabel) {
                out.append("</li>");
            }
            logger.debug("constructed pretty breadbox link text as: \"" + out.toString() + "\" ");
        }
        return out.toString();

    }

    public static String getBreadBoxUrl(String fq, String baseUrl) {

        logger.debug("breadbox link should remove: " + fq);

        StringBuilder out = new StringBuilder();

        NameValuePair nameValuePair = SolrService.splitFilterQuery(fq);

        URLCreator urlCreator = new URLCreator(baseUrl);
        logger.debug("before remove fq: " + urlCreator.getURL());
        urlCreator.removeNameValuePair(new BasicNameValuePair("fq", fq));
        logger.debug("after remove fq: " + urlCreator.getURL());
        urlCreator.removeNameValuePair("page");

        //if this is the category breadbox link, set category to Any
        if (StringUtils.equals(nameValuePair.getName(), "category")) {
            urlCreator.removeNameValuePair("category");
            urlCreator.addNameValuePair("category", "Any");
        }
        out.append(urlCreator.getURL());

        logger.debug("getBreadBoxUrl, remove: " + fq);
        logger.debug(out.toString());
        return out.toString();
    }

    public static String getFacetUrl(String fieldName, String value, String baseUrl) {
        String quotedFq = fieldName + ":\"" + value + "\"";

        URLCreator urlCreator = new URLCreator(baseUrl);
        urlCreator.addNameValuePair("fq", quotedFq);
        urlCreator.removeNameValuePair("page");
        if (StringUtils.equals("category", fieldName)) {
            urlCreator.removeNameValuePair("category");
            urlCreator.addNameValuePair("category", value);
        }
        return urlCreator.getURL();
    }

    public static String getNotFacetUrl(String fieldName, String value, String baseUrl) {
        String quotedFq = "-" + fieldName + ":\"" + value + "\"";
        URLCreator urlCreator = new URLCreator(baseUrl);
        urlCreator.addNameValuePair("fq", quotedFq);
        urlCreator.removeNameValuePair("page");
        if (StringUtils.equals("category", fieldName)) {
            urlCreator.removeNameValuePair("category");
        }
        logger.debug("exclude URL for " + value + ": " + urlCreator.getURL());
        return urlCreator.getURL();
    }

    public static String getPrettyFieldName(String fieldName) {
        return getPrettyFieldName(fieldName, false);
    }

    public static String getPrettyFieldName(String fieldName, Boolean ignoreMinus) {


        FieldName fName = FieldName.getFieldName(fieldName);
        if (fName != null && fName.hasDefinedPrettyName()) {
            return fName.getPrettyName();
        }


        //        if (StringUtils.equals(fieldName,"normal_phenotype_statement"))
        //             return "Normal Phenotype Statement";

        //in breadbox links, field names might start with a - for exclusion facets, make it nicer?
        if (!ignoreMinus) {
            fieldName = StringUtils.replace(fieldName, "-", "NOT ");
        } else {
            fieldName = StringUtils.replace(fieldName, "-", "");
        }

        //remove fieldType suffix cruft
        fieldName = fieldName.replaceAll("_t$", "");
        fieldName = StringUtils.replace(fieldName, "_tf", "");
        fieldName = StringUtils.replace(fieldName, "_hl", "");
        fieldName = StringUtils.replace(fieldName, "_ac", "");
        fieldName = fieldName.replaceAll("_([0-9]+)$", "");

        /* these should only get replaced at the end of the word! */

/*      fieldName = StringUtils.replace(fieldName,"_ac","");
        fieldName = StringUtils.replace(fieldName,"_t","");
        fieldName = StringUtils.replace(fieldName,"_s","");*/

        fieldName = StringUtils.replace(fieldName, "_", " ");
        fieldName = WordUtils.capitalize(fieldName);

        return fieldName;
    }

    public static String getPrettyFieldValue(String displayValue) {
        //Case 12390 - Remove the time from the date display
        displayValue = StringUtils.replace(displayValue, "T00:00:00Z", "");
        return displayValue;
    }

    public static String getSortValueForFacetField(String fieldName, HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String values[] = null;

        for (String key : parameterMap.keySet()) {
            if (StringUtils.equals(key, "f." + fieldName + "facet.sort")) {
                values = parameterMap.get(key);
            }

        }

        List<String> valueList = new ArrayList<>();
        //todo: possibly throw an exception if a sort value made it in more than once?
        //todo: also, if it did, the graceful thing to do is probably take the last value...
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return "count";
        }
        //todo: here too... returning "the default" instead of null...but the default is set in solrconfig.xml,
        //todo: so it's sort of duplicated here in a little bit of an ugly way..
    }

    public static Map<String, List<String>> getFilterQueryMap(String[] filterQueries) {
        Map<String, List<String>> fqMap = new HashMap<>();

        if (filterQueries == null) {
            return fqMap;
        }

        for (String fq : filterQueries) {

            NameValuePair nameValuePair = splitFilterQuery(fq);
            if (nameValuePair == null || nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                return null;
            }
            String field = nameValuePair.getName();
            String value = nameValuePair.getValue();

            //if it doesn't have the key yet, make a new list
            if (!fqMap.containsKey(field)) {
                List<String> list = new ArrayList<>();
                list.add(value);
                fqMap.put(field, list);
            } else {
                fqMap.get(field).add(value);
            }
        }
        return fqMap;
    }

    public static NameValuePair splitFilterQuery(String fq) {
        fq = decode(fq);
        String field = "";
        String value = "";

        StringTokenizer tokenizer = new StringTokenizer(fq, ":");

        if (tokenizer.hasMoreTokens()) {
            field = tokenizer.nextToken();
        }
        //why loop? some values have a : in them.
        while (tokenizer.hasMoreTokens()) {
            if (!StringUtils.isEmpty(value)) {
                value = value + ":";
            }
            value = value + tokenizer.nextToken();
        }
        if (StringUtils.isEmpty(field) || StringUtils.isEmpty(value)) {
            return null;
        }

        return new BasicNameValuePair(field, value);
    }

    public static List<String> getFacetQueryValues(List<FacetQueryEnum> facetQueryEnumList) {
        List<String> values = new ArrayList<>();

        for (FacetQueryEnum facetQueryEnum : facetQueryEnumList) {
            String value = splitFilterQuery(facetQueryEnum.getQuery()).getValue();
            if (!values.contains(value)) {
                values.add(value);
            }
        }

        return values;
    }

    public static String decode(String value) {
        if (value == null) {
            return null;
        }

        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            return null;
        }

    }


    public static String encode(String value) {
        if (value == null) {
            return null;
        }

        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            return null;
        }
    }

    public static String shortenFacetValue(String facetValue) {
        String shortenedName;

        if (facetValue == null) {
            return null;
        }

        shortenedName = StringUtils.abbreviate(facetValue, 15);
        return shortenedName;
    }

    public static boolean hideFacetFiltersForField(String field) {
        if (StringUtils.contains(field, "chromosome")) {
            return true;
        } else if (StringUtils.equals(field, "affected_gene_count")) {
            return true;
        } else {
            return false;
        }
    }

    /* for now, hideFacetFiltersForField and this method travel together and follow the same rules */
    public static boolean isToBeHumanSorted(String field) {
        return hideFacetFiltersForField(field);
    }

    public static boolean isAJoinableFacetField(String field) {

        if (field == null) {
            return false;
        }

        if (StringUtils.contains(field, "gene")) {
            return true;
        }
        if (StringUtils.contains(field, "sequence_alteration")) {
            return true;
        }
        //_tf is all of the term facets
        if (StringUtils.contains(field, "_tf")) {
            return true;
        }
        if (StringUtils.contains(field, "background")) {
            return true;
        }
        if (StringUtils.contains(field, "morpholino")) {
            return true;
        }
        if (StringUtils.contains(field, "construct")) {
            return true;
        }
        if (StringUtils.startsWith(field, "lab")) {
            return true;
        }
        if (StringUtils.equals(field, "genotype")) {
            return true;
        }
        if (StringUtils.contains(field, "coding_sequence")) {
            return true;
        }
        if (StringUtils.contains(field, "regulatory_region")) {
            return true;
        }
        if (StringUtils.contains(field, "engineered_region")) {
            return true;
        }
        if (StringUtils.contains(field, "supplier")) {
            return true;
        }
        if (StringUtils.contains(field, "registered_author")) {
            return true;
        }

        return false;
    }

    /* Default to true, but there might be some exceptions... */
    public static boolean isAnAutocompletableFacet(String field) {

        if (field == null) {
            return false;
        }

        if (StringUtils.contains(field, "chromosome")) {
            return false;
        }

        return true;
    }

    public static boolean addFilterQueryToQuery(String fq) {
        if (fq == null) {
            return false;
        }

        NameValuePair nvp = splitFilterQuery(fq);
        String field = nvp.getName();

        if (field == null) {
            return false;
        }

        //this catches lots, but we might want a few extra...
        if (isAJoinableFacetField(field)) {
            return true;
        }

        if (StringUtils.contains(field, "disease")) {
            return true;
        }

        return false;
    }


    public static String luceneEscape(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        String escaped = LUCENE_PATTERN.matcher(value).replaceAll(REPLACEMENT_STRING);
        return escaped;
    }


    public static String getAllPhenotypeLink(String geneSymbol, boolean isMonogenic) {
        QueryResponse response = getGenePhenotypeQuery(geneSymbol, isMonogenic);
        StringBuilder link = new StringBuilder();
        link.append(getSearchLinkPrefix());
        link.append("?fq=category:Phenotype");
        link.append("&fq=gene%3A%22");
        link.append(geneSymbol);
        link.append("%22");
        if (isMonogenic) {
            link.append("&fq=is_monogenic%3Atrue");
        }
        link.append("\">");
        if (isMonogenic) {
            link.append("All Phenotypes for " + geneSymbol);
        } else {
            link.append("All Phenotypes involving " + geneSymbol);
        }
        link.append(" (");
        link.append(response.getResults().getNumFound());
        link.append(") </a>");

        return link.toString();
    }

    public static boolean queryHasFilterQueries(SolrQuery query) {
        String[] filterQueries = query.getFilterQueries();
        return filterQueries != null && !(filterQueries.length == 1 && filterQueries[0].startsWith("root_only:"));
    }

    public static String buildStageRangeQuery(FieldName fieldName, String leftBracket,
                                              float start, float end, String rightBracket) {
        StringBuilder sb = new StringBuilder();

        sb.append(fieldName.getName());
        sb.append(":");
        sb.append(leftBracket);
        sb.append(start);
        sb.append(" TO ");
        sb.append(end);
        sb.append(rightBracket);
        sb.append(" ");

        return sb.toString();
    }

    public static void addDocument(Map<FieldName, Object> fields) throws IOException, SolrServerException {
        addDocument(fields, new HashMap<>());
    }

    public static void addDocument(Map<FieldName, Object> fields, Map<String, Object> extras) throws IOException, SolrServerException {
        SolrInputDocument document = new SolrInputDocument();
        SolrClient solr = getSolrClient();
        for (Map.Entry<FieldName, Object> field : fields.entrySet()) {
            document.addField(field.getKey().getName(), field.getValue());
        }

        for (Map.Entry<String, Object> field : extras.entrySet()) {
            document.addField(field.getKey(), field.getValue());
        }

        solr.add(document);
        if (!isIndexingInProgress()) {
            solr.commit();
        }
    }

    public static String getServerStatus() throws IOException, SolrServerException {
        SolrRequest req = new DirectXmlRequest("/dataimport", null);
        NamedList<Object> response = getSolrClient().request(req);
        return (String) response.get("status");
    }

    public static boolean isIndexingInProgress() throws IOException, SolrServerException {
        return !getServerStatus().equals(IDLE);
    }

    public static String dismax(String value, FieldName... fields) {
        return dismax(value, Arrays.asList(fields));
    }

    public static String dismax(String value, List<FieldName> fields) {
        return "{!edismax qf='" +
                fields.stream().map(FieldName::getName).collect(Collectors.joining(" ")) +
                "'}" + SolrService.luceneEscape(value);
    }

    public static String dismax(String value, Map<FieldName, String> fields) {
        return dismax(value, fields, true);
    }

    public static String dismax(String value, Map<FieldName, String> fields, boolean escapeQuery) {
        if (escapeQuery) {
            value = luceneEscape(value);
        }
        return "{!edismax qf='" +
                fields.keySet().stream().map(fieldName -> fieldName.getName() + fields.get(fieldName)).collect(Collectors.joining(" ")) +
                "'}" + value;
    }

    public static URL getUrlForQuery(SolrQuery query) throws MalformedURLException {
        String host = ZfinPropertiesEnum.SOLR_HOST.value();
        String port = ZfinPropertiesEnum.SOLR_PORT.value();
        String path = "/solr/" + SolrService.PRIMARY_CORE + "/select";
        String url = "http://" + host + ":" + port + path + query.toQueryString();

        return new URL(url);
    }

    public static void getAllResults(SolrQuery query, Consumer<QueryResponse> responseConsumer) throws IOException, SolrServerException {
        SolrQuery cursorQuery = query.getCopy()
                .setRows(CURSOR_BATCH_SIZE)
                .addSort(SolrQuery.SortClause.asc(FieldName.ID.getName()));
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            cursorQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse response = SolrService.getSolrClient().query(cursorQuery);
            String nextCursorMark = response.getNextCursorMark();

            responseConsumer.accept(response);

            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
    }

    public static void deleteByIds(List<String> ids, boolean commit)  {
        try {
            getSolrClient().deleteById(ids);
        } catch (Exception e) {
            logger.error(e);
        }

        if (commit) {
            try {
                getSolrClient().commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

    public boolean allowDownload(String q, String[] filterQuery) {
        if (q != null && StringUtils.isNotEmpty(q.trim()) && !StringUtils.equals(q,"*:*")) {
            return true;
        }

        if (ArrayUtils.isNotEmpty(filterQuery)) {
            Map<String, List<String>> fqMap = getFilterQueryMap(filterQuery);
            fqMap.remove("category");
            if (fqMap.size() > 0) {
                return true;
            }
        }

        return false;
    }

    public Map<String, List<String>> getHighlights(String id, QueryResponse response) {
        Map<String, List<String>> highlights = new HashMap<>();

        if (response == null || response.getHighlighting() == null || response.getHighlighting().get(id) == null) {
            return highlights;
        }

        for (String field : response.getHighlighting().get(id).keySet()) {
            if (CollectionUtils.isNotEmpty(response.getHighlighting().get(id).get(field))) {
                highlights.put(getPrettyFieldName(field), response.getHighlighting().get(id).get(field));
            }
        }

        return highlights;
    }

}

