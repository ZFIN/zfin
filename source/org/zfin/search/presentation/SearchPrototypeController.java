package org.zfin.search.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FacetCategoryComparator;
import org.zfin.search.FacetValueAlphanumComparator;
import org.zfin.search.service.*;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class SearchPrototypeController {

    @Autowired
    private SolrService solrService;

    @Autowired
    private QueryManipulationService queryManipulationService;

    @Autowired
    private FacetBuilderService facetBuilderService;

    @Autowired
    private RelatedDataService relatedDataService;

    @Autowired
    private ResultService resultService;

    public Logger logger = Logger.getLogger(SearchPrototypeController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }


    @RequestMapping(value = "/angularTest")
    public String angularTest() {
        return "search/angular-test.popup";
    }

    @RequestMapping(value = "/angularTestPage")
    public String angulartestPage() {
        return "search/angular-test-page.page";
    }

    @RequestMapping(value = "/prototype")
    public String viewResults(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "fq", required = false) String[] filterQuery,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "page", required = false) String pageNumber,
                              @RequestParam(value = "sort", required = false) String sort,
                              @RequestParam(value = "hl", required = false, defaultValue = "false") Boolean highlight,
                              @RequestParam(value = "explain", required = false, defaultValue = "false") Boolean explain,
                              @RequestParam(required = false, defaultValue = "true") Boolean appendCategoryToBaseUrl,
                              Model model,
                              HttpServletRequest request) {
        Integer page = 1;
        if (StringUtils.isNotEmpty(pageNumber)) {
            try {
                page = Integer.parseInt(pageNumber);
            } catch (NumberFormatException e) {
                logger.error(e);
            }
        }

        Boolean redirectToFirstResult = false;
        if (StringUtils.startsWith(q,"!!")) {
            redirectToFirstResult = true;
            q = q.substring(2);
        }

        if (StringUtils.isNotEmpty(q) && q.trim().startsWith("-")) {
            model.addAttribute("isDashQuery", true);
            model.addAttribute("newQuery", q.trim().substring(1));
        }

        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();

        String queryStringInput;
        String requestQueryString = request.getQueryString();
        queryStringInput = requestQueryString == null ? "q=" : requestQueryString;

        String baseUrl = SolrService.getSearchUrlPrefix() + "?" + queryStringInput;
        model.addAttribute("queryString", queryStringInput);
        model.addAttribute("request", request);

        if (explain) {
            query.set("fl", "name, type, id, category, full_name, url, thumbnail, image, snapshot, date, attribution_count, result_table, screen, has_orthology, score, xpat_zdb_id, fig_zdb_id, [explain]");
        }


        URLCreator resubmitUrlCreator = new URLCreator(baseUrl);
        resubmitUrlCreator.removeNameValuePair("q");
        resubmitUrlCreator.addNamevaluePair("q","");
        model.addAttribute("baseUrlWithoutQ", resubmitUrlCreator.getURL());

        query = handleFacetSorting(query, request);

        if (page == null)
            page = 1;
        int rows = 20;
        query.setRows(rows);
        int start = (page - 1) * rows;
        model.addAttribute("start", start);
        query.setStart(start);


        if (highlight) {
            query.setHighlight(true);
            //gonna be slow!
            query.setParam("hl.fl", "*");
        }


        //handle sorting... move this a separate method?  use an enum?
        model = handleSorting(model, query, baseUrl, sort);

        category = getCategory(filterQuery, category);
        Map<String, String> facetMap = Category.getFacetMap();

        // If results only matched in one category, the controller calls itself with this extra flag,
        // the 'We picked your category' message will keep showing up unless the category gets added to
        // the baseUrl, so it's added here...
        if (appendCategoryToBaseUrl) {
            URLCreator baseUrlWithCategory = new URLCreator(baseUrl);
            baseUrlWithCategory.removeNameValuePair("category");
            baseUrlWithCategory.addNamevaluePair("category", category);
            baseUrl = baseUrlWithCategory.getURL();
        }

        model.addAttribute("baseUrl", baseUrl);


        //if category comes in from the form rather than as an fq, make it into an fq...
        // ...   (as long as it's a valid category, check for that by using the facetMap keys)
        if ((SolrService.getFilterQueryMap(filterQuery) == null || !SolrService.getFilterQueryMap(filterQuery).containsKey("category"))
                && facetMap.containsKey(category)) {
            String[] newFq = new String[1];
            newFq[0] = "category:" + "\"" + category + "\"";
            filterQuery = ArrayUtils.addAll(newFq, filterQuery);

        }
        SolrService.setCategory(category, query);

        StringBuilder queryString = new StringBuilder();

        if (!StringUtils.isEmpty(q)) {

            //handle a very specific case, id:idb names need to be escaped
            q = queryManipulationService.processQueryString(q);

            queryString.append(q);
            queryString.append(" ");
        }


        query.setQuery(queryString.toString());
        query.addFilterQuery(filterQuery);

        model.addAttribute("q", (q != null ? q : ""));


        //hide results when no criteria are selected, also don't ask solr for results
        if (StringUtils.isEmpty(q)
                && (filterQuery == null
                || filterQuery.length == 0)) {
            model.addAttribute("showResults", false);
            query.setRows(0);
        }


        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        SolrDocumentList solrDocumentList = response.getResults();


        model.addAttribute("facetGroups", facetBuilderService.buildFacetGroup(category, response, query, baseUrl));
        model.addAttribute("facetQueries", facetBuilderService.getFacetQueries(response, baseUrl));
        model.addAttribute("response", response);
        model.addAttribute("query", query);

        List<SearchResult> results = response.getBeans(SearchResult.class);

        //Easter Egg!  If the query starts with !, just go right to the first result
        if (redirectToFirstResult && !CollectionUtils.isEmpty(results)) {
            model.asMap().clear();
            return "redirect:" + results.get(0).getUrl();
        }

        //if the category is Any, and we only get one value back in the category facet,
        //set it and do everything again...

        if ((StringUtils.equals(category, "Any") || StringUtils.isEmpty(category))
                && response.getFacetFields() != null
                && response.getFacetFields().size() == 1
                && response.getFacetFields().get(0).getValues() != null
                && response.getFacetFields().get(0).getValues().size() == 1
                && StringUtils.equals(response.getFacetFields().get(0).getName(), "category")) {
            String automaticallySelectedCategory = response.getFacetFields().get(0).getValues().get(0).getName();

            model.addAttribute("message", "All matching results were in the <strong>" + automaticallySelectedCategory + "</strong> category, so it was automatically selected.");

            return viewResults(q, filterQuery, automaticallySelectedCategory, pageNumber, sort,
                    highlight, explain, true, model, request);
        }


        injectHighlighting(results, response);
        resultService.injectAttributes(results);
        injectRelatedLinks(results); //this cost 200-300ms per request, it's less now, but still a good place to optimize

        Map<String, List<String>> fqMap = SolrService.getFilterQueryMap(filterQuery);


        List<String> xrefList = fqMap.get("xref");

        logger.debug(xrefList);

        if (!CollectionUtils.isEmpty(xrefList)) {
            List<SearchResult> xrefResults = getXrefResult(server, xrefList);
            model.addAttribute("xrefResults", xrefResults);
        }

        PaginationBean paginationBean = new PaginationBean();
        URLCreator urlCreator = new URLCreator(baseUrl);
        urlCreator.removeNameValuePair("page");

        paginationBean.setActionUrl(urlCreator.getFullURLPlusSeparator());
        model.addAttribute("baseUrlWithoutPage", urlCreator.getFullURLPlusSeparator());
        paginationBean.setPage(page.toString());
        paginationBean.setTotalRecords((int) solrDocumentList.getNumFound());
        paginationBean.setQueryString(request.getQueryString());
        paginationBean.setMaxDisplayRecords(rows);
        model.addAttribute("paginationBean", paginationBean);

        List<String> categories = new ArrayList<>();
        categories.addAll(org.zfin.search.Category.getFacetMap().keySet());
        Collections.sort(categories, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return FacetCategoryComparator.compareString(o1, o2);
            }
        });


        model.addAttribute("category", category);
        model.addAttribute("categories", categories);
        model.addAttribute("numFound", solrDocumentList.getNumFound());
        model.addAttribute("results", results);
        model.addAttribute("debug", "header: " + response.getHeader() + "<br><br>highlighting: ");

        //these are used to decide which search result template jsp tag to use
        model.addAttribute("geneCategoryName", Category.GENE.getName());
        model.addAttribute("fishCategoryName", Category.FISH.getName());


        return "search/prototype-results.page";
    }


    @RequestMapping(value = "/facet-autocomplete")
    public
    @ResponseBody
    List<FacetLookupEntry>
    facetAutocomplete(@RequestParam(value = "q", required = false) String q,
                      @RequestParam(value = "fq", required = false) String[] filterQuery,
                      @RequestParam(value = "category", required = false) String category,
                      @RequestParam(value = "field", required = true) String field,
                      @RequestParam(value = "term", required = true) String term,
                      @RequestParam(value = "limit", required = false) Integer limit,
                      @RequestParam(value = "sort", required = false) String sort,
                      HttpServletRequest request) {
        List<FacetLookupEntry> facets = new ArrayList<FacetLookupEntry>();

        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();

        query = handleFacetSorting(query, request);

        StringBuilder queryString = new StringBuilder();

        if (!StringUtils.isEmpty(q)) {
            queryString.append(q);
            queryString.append(" ");
        }
        query.setQuery(queryString.toString());

        query.setFilterQueries(filterQuery);

        if (!StringUtils.isEmpty(category)) {
            query.addFilterQuery("category:\"" + category + "\"");
        }

        query.setFacet(true);
        if (limit == null)
            limit = 10;
        query.setFacetLimit(limit);

        if (StringUtils.isNotEmpty(sort))
            query.setFacetSort("index");

        //facet on a single field, limit by prefix
        query.addFacetField(field);
        query.setFacetPrefix(term);

        //because we only want the facets, we don't have to get any rows
        query.setRows(0);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        FacetField facetField = response.getFacetField(field);

        if (facetField != null && facetField.getValues() != null) {

            List<FacetField.Count> facetValues = new ArrayList<>();

            facetValues.addAll(facetField.getValues());


            //todo: use field name enum!
            if (!StringUtils.equals(facetField.getName(), "stage"))
                Collections.sort(facetValues, new FacetValueAlphanumComparator());

//            facetBuilderService.sortFacetValues(facetField.getName(), facetValues);


            for (FacetField.Count count : facetValues) {
                FacetLookupEntry entry = new FacetLookupEntry();
                entry.setName(count.getName());
                entry.setLabel(count.getName().replace("\"", "") + " (" + count.getCount() + ") ");
                String fq = SolrService.encode(facetField.getName() + ":\"" + count.getName() + "\"");
                entry.setFq(fq);
                entry.setValue(count.getName());
                entry.setCount(String.valueOf(count.getCount()));
                facets.add(entry);
            }
        }

        return facets;
    }


    @RequestMapping(value = "/autocomplete")
    public
    @ResponseBody
    List<FacetLookupEntry> autocomplete(@RequestParam(required = true) String q,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String type) {
        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();

        query.setRequestHandler("/name-autocomplete");
        query.setQuery(q);

        if (StringUtils.isNotEmpty(category)) {
            if (StringUtils.contains(category, " "))
                category = "\"" + category + "\"";
            query.addFilterQuery("category:" + category);
        }
        if (StringUtils.isNotEmpty(type))
            query.addFilterQuery("type:" + type);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

/*
        SolrDocumentList solrDocumentList = response.getResults();
*/

        List<SearchResult> results = response.getBeans(SearchResult.class);
        injectAutocompleteHighlighting(results, response);

        List<FacetLookupEntry> values = new ArrayList<FacetLookupEntry>();

        for (SearchResult result : results) {
            FacetLookupEntry entry = new FacetLookupEntry();
            StringBuilder label = new StringBuilder();

            //The injectAutocompleteHighlighting method will set a the autocomplete label if there
            //is going to be some special highlighting in there, otherwise, use the name.
            if (StringUtils.isNotEmpty(result.getAutocompleteLabel()))
                label.append(result.getAutocompleteLabel());
            else
                label.append(result.getName());

            //Only show additional highlighting information if the name didn't create a highlight match
            if (StringUtils.isNotEmpty(result.getMatchingText()) && StringUtils.isEmpty(result.getAutocompleteLabel())) {
                label.append(" [");
                label.append(result.getMatchingText());
                label.append("]");
            }
/*
            if (StringUtils.isEmpty(category) && StringUtils.isEmpty(type)) {
                label.append(" <span class=\"label\" style=\"float:right\">");
                label.append(result.getCategory());
                if (StringUtils.isNotEmpty(result.getType())) {
                    label.append(" > ");
                    label.append(result.getType());
                }
                label.append("</span>");
            }
*/

            entry.setLabel(label.toString());

            String value = result.getName();

/*
            if (StringUtils.contains(value, " "))
                value = "\"" + value + "\"";
*/

            entry.setValue(value);

            values.add(entry);
        }

/*
        query = new SolrQuery();
        query.addFilterQuery("category:Gene");
        query.setRows(0);
        query.setHighlight(false);
        for (FacetLookupEntry fle : values) {
            query.addFacetQuery("expression_anatomy:\"" + fle.getValue() + "\"");
        }
        response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }
        Map<String, Integer> facetQueries = response.getFacetQuery();
        for (FacetLookupEntry fle : values) {
            Integer count = facetQueries.get("expression_anatomy:\"" + fle.getValue() + "\"");
            fle.setLabel(fle.getLabel() + " (" + count + ") ");
        }
*/

        return values;
    }

    @RequestMapping(value = "/download")
    public String downloadResults(@RequestParam(value = "q", required = false) String q,
                                  @RequestParam(value = "fq", required = false) String[] filterQuery,
                                  @RequestParam(value = "category", required = false) String category,
                                  @RequestParam(value = "page", required = false) String pageNumber,
                                  @RequestParam(value = "keepfacets", required = false) Boolean isKeptFacets,
                                  HttpServletResponse response,
                                  HttpServletRequest request) {

        response.setContentType("data:text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"zfin_search_results.csv\"");

        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();
        //set handler to a csv specific handler?


        //this should do everything exactly the same as regular controller method to build the result set,
        //but...
        //  leave facets off
        //  no highlighting
        //  set the fl to just name, id
        //  then connect to solr by http?  or solrj and convert to csv? hmmm..

/*
        OutputStream resOs= response.getOutputStream();
        OutputStream buffOs= new BufferedOutputStream(resOs);
        OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);

        CsvWriter writer = new CsvWriter(outputwriter, '\u0009');
        for(int i=1;i <allRecords.size();i++){
            CompositeRequirement aReq=allRecords.get(i);
            writer.write(aReq.toString());
        }
        outputwriter.flush();
        outputwriter.close();
*/
        return "";
    }


    /* It's possible that this is really just a subset of the functionality of the facet-autocomplete method... */
    @RequestMapping(value = "/cross-reference")
    public
    @ResponseBody
    List<FacetLookupEntry>
    crossReference(@RequestParam(value = "term", required = true) String q,
                   @RequestParam(value = "category", required = false) String category) {

        List<FacetLookupEntry> facetValues = new ArrayList<FacetLookupEntry>();


        QueryResponse response = solrService.getRelatedDataResponse(q);


        FacetField facetField = response.getFacetField("category");

        if (facetField != null && facetField.getValues() != null)
            for (FacetField.Count count : facetField.getValues()) {
                FacetLookupEntry entry = new FacetLookupEntry();
                entry.setName(count.getName());
                entry.setLabel(count.getName().replace("\"", "") + " (" + count.getCount() + ") ");
                String fq = SolrService.encode(facetField.getName() + ":\"" + count.getName() + "\"");
                entry.setFq(fq);
                entry.setValue(count.getName());
                entry.setCount(String.valueOf(count.getCount()));
                facetValues.add(entry);
            }

        return facetValues;
    }

    @RequestMapping(value = "/gene-expression/{geneZdbID}")
    public String geneExpressionModal(Model model, @PathVariable String geneZdbID) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);
        List<GenericTerm> terms = RepositoryFactory.getExpressionRepository().getWildTypeAnatomyExpressionForMarker(geneZdbID);
        Collections.sort(terms);

        LinkedHashMap expressionLinks = new LinkedHashMap();

        if (gene != null && terms != null && terms.size() > 0) {
            for (GenericTerm term : terms) {
                if (!StringUtils.equals(term.getTermName(), "unspecified"))
                    expressionLinks.put(term.getTermName(), SolrService.getExpressionLink(gene.getAbbreviation(), term.getTermName()));
            }
            model.addAttribute("expressionTermLinks", expressionLinks);
            model.addAttribute("gene", gene);

            //this method is fast, because it's in one query, but on the other end of the results, it doesn't include parents...
            //model.addAttribute("expressionTermLinks",SolrService.getExpressionTermLinks(gene.getAbbreviation()));
            model.addAttribute("allExpressionLink", SolrService.getAllExpressionLink(gene.getAbbreviation()));
            model.addAttribute("wtExpressionLink", SolrService.getWildtypeExpressionLink(gene.getAbbreviation()));
        }
        return "search/gene-expression.popup";
    }

    @RequestMapping(value = "/phenotype/{geneZdbID}")
    public String phenotypeModal(Model model, @PathVariable String geneZdbID) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbID);
        SolrService.getPhenotypeLink(gene.getAbbreviation());
        Map<String, String> phenotypeLinks = SolrService.getPhenotypeLink(gene.getAbbreviation());
        model.addAttribute("phenotypeLinks", phenotypeLinks);
        model.addAttribute("gene", gene);
        model.addAttribute("allPhenotypeLink", SolrService.getAllPhenotypeLink(gene.getAbbreviation(), true));
        model.addAttribute("allPhenotypeInvolvingLink", SolrService.getAllPhenotypeLink(gene.getAbbreviation(), false));

        return "search/phenotype.popup";
    }

    @RequestMapping(value = "/message")
    public String getSearchMessage() {
        return "search/searchMessage.popup";
    }

    public Model handleSorting(Model model, SolrQuery query, String baseUrl, String sort) {
        URLCreator urlWithoutSort = new URLCreator(baseUrl);
        urlWithoutSort.removeNameValuePair("sort");
        model.addAttribute("baseUrlWithoutSort", urlWithoutSort.getURL());
        String sortUrlSeparator = "?";
        if (StringUtils.contains(urlWithoutSort.getURL(), "?"))
            sortUrlSeparator = "&";
        model.addAttribute("sortUrlSeparator", sortUrlSeparator);
        if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "A to Z")) {
            query.setSortField("name_sort", SolrQuery.ORDER.asc);
            model.addAttribute("sortDisplay", "A to Z");
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Z to A")) {
            query.setSortField("name_sort", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Z to A");
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Newest")) {
            query.setSortField("date", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Newest First");
            model.addAttribute("showDates", true);
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Oldest")) {
            query.setSortField("date", SolrQuery.ORDER.asc);
            model.addAttribute("sortDisplay", "Oldest First");
            model.addAttribute("showDates", true);
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Most Attributed")) {
            query.setSortField("attribution_count", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Most Attributed First");
            model.addAttribute("showAttributionCount", true);

        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Least Attributed")) {
            query.setSortField("date", SolrQuery.ORDER.asc);
            model.addAttribute("sortDisplay", "Least Attributed First");
            model.addAttribute("showAttributionCount", true);

        } else {
            model.addAttribute("sortDisplay", "by Relevance");
        }


        return model;
    }


    private SolrQuery handleFacetSorting(SolrQuery query, HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            if (key.startsWith("f.") && key.endsWith("facet.sort"))
                query.add(key, parameterMap.get(key));

        }
        return query;
    }


    private String getCategory(String[] filterQuery, String categoryFromPulldown) {
        if (StringUtils.isNotEmpty(categoryFromPulldown))
            return categoryFromPulldown;

        Map<String, List<String>> fqMap = SolrService.getFilterQueryMap(filterQuery);

        List<String> categories = fqMap.get("category");
        if (categories == null || categories.isEmpty())
            return null;
        else
            return categories.get(0).replace("\"", "");
    }


    private void injectAutocompleteHighlighting(List<SearchResult> results, QueryResponse response) {
        for (SearchResult result : results) {
            String id = result.getId();
            if (response.getHighlighting() != null && response.getHighlighting().get(id) != null) {

                for (String highlightField : response.getHighlighting().get(id).keySet()) {
                    logger.debug("highlight field: " + highlightField);
                    /*todo: add some kind of priority for which highlight fields can overwrite the existing single value..  */
                    if (response.getHighlighting().get(id).get(highlightField) != null) {
                        for (String snippet : response.getHighlighting().get(id).get(highlightField)) {
                            if (StringUtils.equals(highlightField, "name_ac")) {
                                result.setAutocompleteLabel(snippet);
                            } else {
                                result.setMatchingText(snippet);
                            }
                        }
                    }
                }
            }
        }

    }


    private void injectHighlighting(List<SearchResult> results, QueryResponse response) {
        for (SearchResult result : results) {
            String id = result.getId();
            List<String> highlightSnippets = new ArrayList<String>();
            if (response.getHighlighting() != null && response.getHighlighting().get(id) != null) {

                for (String highlightField : response.getHighlighting().get(id).keySet()) {
                    logger.debug("highlight field keys? => " + response.getHighlighting().get(id).keySet());
                    if (response.getHighlighting().get(id).get(highlightField) != null) {
                        for (String snippet : response.getHighlighting().get(id).get(highlightField)) {
                            logger.debug("snippet: " + snippet);
                            //don't show the field name for the "catch-all" field
                            if (StringUtils.equals(highlightField, "text")) {
                                highlightSnippets.add("<tr><td class=\"snippet-label\">"
                                        + "&nbsp;" + "</td><td class=\"snippet-value\"> " + snippet + "</td></tr>");
                            } else {
                                highlightSnippets.add("<tr><td class=\"snippet-label\">"
                                        + SolrService.getPrettyFieldName(highlightField) + "</td><td class=\"snippet-value\"> " + snippet + "</td></tr>");
                            }
                        }
                    }
                }
            }
            if (!highlightSnippets.isEmpty()) {
                StringBuilder out = new StringBuilder();
                out.append("<table class=\"search-result-snippet\">");
                for (String snippet : highlightSnippets) {
                    out.append(snippet);
                }
                out.append("</table>");
                result.setMatchingText(out.toString());

            }
        }

    }


    private void injectRelatedLinks(List<SearchResult> results) {
        for (SearchResult result : results) {
            result.setRelatedLinks(relatedDataService.getRelatedDataLinks(result));
        }

    }

    private List<SearchResult> getXrefResult(SolrServer server, List<String> xrefList) {


        if (CollectionUtils.isEmpty(xrefList))
            return null;


        SolrQuery query = new SolrQuery();
        StringBuilder sb = new StringBuilder();

        for (String xref : xrefList) {
            if (!StringUtils.isEmpty(sb.toString()))
                sb.append(" OR ");
            sb.append("id:");
            sb.append(xref);
        }

        if (StringUtils.isEmpty(sb.toString()))
            return null;
        query.setQuery(sb.toString());

        //more than a few would be silly...
        query.setRows(4);
        query.setHighlight(false);
        query.setFacet(false);

        logger.debug("xref query: " + query);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        List<SearchResult> results = response.getBeans(SearchResult.class);

        return results;
    }

}
