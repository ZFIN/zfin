package org.zfin.search.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.captcha.CaptchaService;
import org.zfin.infrastructure.service.ZdbIDService;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.*;
import org.zfin.util.URLCreator;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.search.service.FacetBuilderService.categorySupportsAttributionSort;
import static org.zfin.search.service.FacetBuilderService.categorySupportsSort;

@Controller
@RequestMapping("/quicksearch")
public class SearchPrototypeController {

    private static final boolean ENABLE_CAPTCHA = true;

    @Autowired
    private SolrService solrService;

    @Autowired
    private QueryManipulationService queryManipulationService;

    @Autowired
    SearchSuggestionService searchSuggestionService;

    private FacetBuilderService facetBuilderService;

    @Autowired
    private RelatedDataService relatedDataService;

    @Autowired
    private ResultService resultService;

    @Autowired
    private ZdbIDService zdbIDService;

    public Logger logger = LogManager.getLogger(SearchPrototypeController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }


    public String viewWtExpressionGalleryResults(String geneZdbId, Model model, HttpServletRequest request) {
        String fq[] = {"category:\"Expression\"",
                "conditions:\"standard or control\"",
                "-sequence_targeting_reagent:[* TO *]",
                "is_genotype_wildtype:true",
                "xref:" + geneZdbId};

        return viewResults(null, fq, Category.EXPRESSIONS.getName(),
                null, null, null, false, false,
                true, true, model, request);

    }

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @RequestMapping(value = "/prototype")
    public String viewResults(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "fq", required = false) String[] filterQuery,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "rows", required = false) Integer rows,
                              @RequestParam(value = "sort", required = false) String sort,
                              @RequestParam(value = "hl", required = false, defaultValue = "false") Boolean highlight,
                              @RequestParam(value = "explain", required = false, defaultValue = "false") Boolean explain,
                              @RequestParam(value = "galleryMode", required = false, defaultValue = "false") Boolean galleryMode,
                              @RequestParam(required = false, defaultValue = "true") Boolean appendCategoryToBaseUrl,
                              Model model,
                              HttpServletRequest request) {
        //TODO: This would read better if it was an annotation on the method (eg. `@RequiresCaptcha`)
        Optional<String> captchaRedirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (captchaRedirectUrl.isPresent()) {
            return "redirect:" + captchaRedirectUrl.get();
        }

        if (page == null) {
            page = 1;
        }
        if (category != null) {
            category = POLICY.sanitize(category);
        }
        if (StringUtils.isNotEmpty(q)) {
            q = q.trim();
        }

        Boolean redirectToFirstResult = false;
        if (StringUtils.startsWith(q, "!!")) {
            redirectToFirstResult = true;
            q = q.substring(2);
        }

        if (StringUtils.isNotEmpty(q)) {
            String url = null;
            //support for ZFIN:ZDB-... ID format
            if (q.contains("ZDB")) {
                String zdbQuery = q.replace("ZFIN:ZDB", "ZDB");

                String replacementZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbQuery);

                if (zdbIDService.isActiveZdbID(zdbQuery)) {
                    url = "/" + zdbQuery;
                } else if (StringUtils.isNotEmpty(replacementZdbID)) {
                    url = "/" + replacementZdbID;
                }

                if (StringUtils.isNotEmpty(url)) {
                    return "redirect:" + url;
                }
            }
        }

        if (StringUtils.isNotEmpty(q) && q.startsWith("-")) {
            model.addAttribute("isDashQuery", true);
            model.addAttribute("newQuery", q.substring(1));
        }

        SolrClient client = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        String queryStringInput;
        String requestQueryString = request.getQueryString();
        queryStringInput = requestQueryString == null ? "q=" : requestQueryString;

        String baseUrl = SolrService.getSearchUrlPrefix() + "?" + queryStringInput;
        model.addAttribute("queryString", queryStringInput);
        model.addAttribute("request", request);

        if (explain) {
            query.set("fl", "name, type, id, category, full_name, url, thumbnail, image, profile_image, date, attribution_count, screen, has_orthology, score, xpat_zdb_id, fig_zdb_id, [explain], is_curatable", "pgcmid");
        }

        URLCreator resubmitUrlCreator = new URLCreator(baseUrl);
        resubmitUrlCreator.removeNameValuePair("q");
        resubmitUrlCreator.addNameValuePair("q", "");
        model.addAttribute("baseUrlWithoutQ", resubmitUrlCreator.getURL());

        query = handleFacetSorting(query, request);

        if (galleryMode) {
            rows = 50;  // 50 comes from f.img_zdb_id.facet.limit set in solrconfig.xml
        }

        //default to 20 rows
        if (rows == null) {
            rows = 20;
        }
        //allow for no more than 500
        if (rows > 500) {
            rows = 500;
        }

        query.setRows(rows);
        int start = (page - 1) * rows;
        //allow for not paginating beyond record number 10_000
        if (start > 10000) {
            start = 10000;
        }

        model.addAttribute("start", start);
        query.setStart(start);

        if (galleryMode) {
            query.setParam("f." + FieldName.IMG_ZDB_ID.getName() + ".facet.offset", Integer.toString(start));
            query.setGetFieldStatistics("{!countDistinct=true}" + FieldName.IMG_ZDB_ID.getName());
        }

        if (highlight) {
            query.setHighlight(true);
            //gonna be slow!
            query.setParam("hl.fl", "*");
        }


        category = getCategory(filterQuery, category);
        Map<String, String> facetMap = Category.getFacetMap();

        //handle sorting... move this a separate method?  use an enum?
        model = handleSorting(model, query, baseUrl, sort, category);

        // If results only matched in one category, the controller calls itself with this extra flag,
        // the 'We picked your category' message will keep showing up unless the category gets added to
        // the baseUrl, so it's added here...
        if (appendCategoryToBaseUrl) {
            URLCreator baseUrlWithCategory = new URLCreator(baseUrl);
            baseUrlWithCategory.removeNameValuePair("category");
            baseUrlWithCategory.addNameValuePair("category", category);
            baseUrl = baseUrlWithCategory.getURL();
        }

        if (!categorySupportsSort(category, sort)) {
            URLCreator baseUrlWithoutSort = new URLCreator(baseUrl);
            baseUrlWithoutSort.removeNameValuePair("sort");
            return "redirect:" + baseUrlWithoutSort.getURL();
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

        String originalQ = q;
        if (!StringUtils.isEmpty(q)) {

            //handle a very specific case, id:idb names need to be escaped
            q = queryManipulationService.processQueryString(q);

            queryString.append(q);
            queryString.append(" ");
        }

        query.setQuery(queryString.toString());
        query.addFilterQuery(filterQuery);

        model.addAttribute("q", (originalQ != null ? originalQ : ""));

        handleRootOnlyResults(query);

        //hide results when no criteria are selected, also don't ask solr for results
        if (StringUtils.isEmpty(q)
                && (filterQuery == null
                || filterQuery.length == 0)) {
            model.addAttribute("showResults", false);
            query.setRows(0);
        }

        QueryResponse response = new QueryResponse();
        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        SolrDocumentList solrDocumentList = response.getResults();

        //a map to know whether or not to show a facet value as a link
        Map<String, Boolean> filterQuerySelectionMap = new HashMap<>();

        if (query != null && query.getFilterQueries() != null) {
            for (String fq : query.getFilterQueries()) {
                filterQuerySelectionMap.put(fq, true);
                logger.debug("added to filterQuerySelectionMap: " + fq);
            }
        }

        facetBuilderService = new FacetBuilderService(response, baseUrl, filterQuerySelectionMap);
        model.addAttribute("facetGroups", facetBuilderService.buildFacetGroup(category));
        model.addAttribute("sortingOptions", facetBuilderService.buildSortingOptions(category));
//        facetBuilderService.buildSortingOptions(category, model);
        model.addAttribute("facetQueries", facetBuilderService.getFacetQueries());
        model.addAttribute("response", response);
        model.addAttribute("query", query);

        final List<SearchResult> results = response.getBeans(SearchResult.class);

        if (CollectionUtils.isNotEmpty(results)) {
            List<String> suggestions = searchSuggestionService.getSuggestions(q);
            // It looks a little funny when a suggestion is already the first search result, so filter
            // the suggestions list accordingly.
            CollectionUtils.filter(suggestions, (Predicate) o -> !results.get(0).getName().equals(o));
            model.addAttribute("suggestions", suggestions);
        }

        //Easter Egg!  If the query starts with !, just go right to the first result
        if (redirectToFirstResult && CollectionUtils.isNotEmpty(results)) {
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

            return viewResults(q, filterQuery, automaticallySelectedCategory, page, rows, sort,
                    highlight, explain, galleryMode, true, model, request);
        }

        injectHighlighting(results, response);
        resultService.injectAttributes(results);
        relatedDataService.injectRelatedLinks(results); //this cost 200-300ms per request, it's less now, but still a good place to optimize

        Map<String, List<String>> fqMap = SolrService.getFilterQueryMap(filterQuery);


        List<String> xrefList = fqMap.get("xref");

        logger.debug(xrefList);

        if (CollectionUtils.isNotEmpty(xrefList)) {
            List<SearchResult> xrefResults = getXrefResult(client, xrefList);
            model.addAttribute("xrefResults", xrefResults);
        }

        long numImages = galleryMode ? response.getFieldStatsInfo().get(FieldName.IMG_ZDB_ID.getName()).getCountDistinct() : 0;
        long numFound = solrDocumentList.getNumFound();

        if (galleryMode && numImages == 0) {
            model.addAttribute("message", "No images were found, showing all results.");
            return viewResults(q, filterQuery, category, page, rows, sort, highlight, explain, false, appendCategoryToBaseUrl, model, request);
        }

        //Set up pagination
        PaginationBean paginationBean = new PaginationBean();
        URLCreator paginationUrlCreator = new URLCreator(baseUrl);
        paginationUrlCreator.removeNameValuePair("page");
        paginationBean.setActionUrl(paginationUrlCreator.getFullURLPlusSeparator());
        model.addAttribute("baseUrlWithoutPage", paginationUrlCreator.getFullURLPlusSeparator());

        //Set up number of rows
        URLCreator rowsUrlCreator = new URLCreator(baseUrl);
        rowsUrlCreator.removeNameValuePair("page");
        rowsUrlCreator.removeNameValuePair("rows");
        model.addAttribute("baseUrlWithoutRows", rowsUrlCreator.getFullURLPlusSeparator());

        String rowsUrlSeparator = "?";
        if (StringUtils.contains(rowsUrlCreator.getURL(), "?")) {
            rowsUrlSeparator = "&";
        }
        model.addAttribute("rowsUrlSeparator", rowsUrlSeparator);
        model.addAttribute("rows", rows);
        model.addAttribute("allowDownload", solrService.allowDownload(q, filterQuery));
        model.addAttribute("downloadUrl", baseUrl.replaceAll("^/search", "/action/quicksearch/download"));

        paginationBean.setPage(page.toString());
        paginationBean.setTotalRecords((int) (galleryMode ? numImages : numFound));
        paginationBean.setQueryString(request.getQueryString());
        paginationBean.setMaxDisplayRecords(rows);
        model.addAttribute("paginationBean", paginationBean);

        //set up images
        List<String> imageIDs = new ArrayList<>();
        FacetField imageFacet = response.getFacetField(FieldName.IMG_ZDB_ID.getName());
        if (imageFacet != null) {
            for (FacetField.Count count : imageFacet.getValues()) {
                imageIDs.add(count.getName());
            }
        }

        if (!CollectionUtils.isEmpty(imageIDs)) {
            model.addAttribute("images", RepositoryFactory.getFigureRepository().getImages(imageIDs));
        }

        URLCreator galleryModeUrlCreator = new URLCreator(baseUrl);
        galleryModeUrlCreator.removeNameValuePair("page");
        galleryModeUrlCreator.removeNameValuePair("rows");
        galleryModeUrlCreator.removeNameValuePair("galleryMode");
        model.addAttribute("baseUrlWithoutGalleryMode", galleryModeUrlCreator.getFullURLPlusSeparator());

        model.addAttribute("galleryMode", galleryMode);
        model.addAttribute("category", category);
        model.addAttribute("categories", Category.getCategoryDisplayList());
        model.addAttribute("numFound", numFound);
        model.addAttribute("numImages", numImages);
        model.addAttribute("results", results);
        model.addAttribute("debug", "header: " + response.getHeader() + "<br><br>highlighting: ");

        //these are used to decide which search result template jsp tag to use
        model.addAttribute("geneCategoryName", Category.GENE.getName());
        model.addAttribute("fishCategoryName", Category.FISH.getName());

        return "search/prototype-results";
    }


    @RequestMapping(value = "/facet-autocomplete")
    public
    @ResponseBody
    List<FacetLookupEntry>
    facetAutocomplete(@RequestParam(value = "q", required = false) String q,
                      @RequestParam(value = "fq", required = false) String[] filterQuery,
                      @RequestParam(value = "category", required = false) String category,
                      @RequestParam(value = "field", required = true) String field,
                      @RequestParam(value = "term", required = false) String term,
                      @RequestParam(value = "limit", required = false) Integer limit,
                      @RequestParam(value = "sort", required = false) String sort,
                      HttpServletRequest request) {
        List<FacetLookupEntry> facets = new ArrayList<FacetLookupEntry>();

        SolrClient server = SolrService.getSolrClient();
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
        if (limit == null) {
            limit = 10;
        }
        query.setFacetLimit(limit);

        if (StringUtils.isNotEmpty(sort)) {
            query.setFacetSort("index");
        }

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

            List<FacetValue> facetValues = new ArrayList<>();

            for (FacetField.Count count : facetField.getValues()) {
                facetValues.add(new FacetValue(count));
            }

            facetBuilderService.sortFacetValues(facetField.getName(), facetValues);

            for (FacetValue facetValue : facetValues) {
                FacetLookupEntry entry = new FacetLookupEntry();
                entry.setName(SolrService.encode(facetValue.getLabel()));
                entry.setLabel(facetValue.getLabel().replace("\"", "") + " (" + facetValue.getCount() + ") ");
                String fq = SolrService.encode(facetField.getName() + ":\"" + facetValue.getLabel() + "\"");
                entry.setFq(fq);
                entry.setValue(facetValue.getLabel());
                entry.setCount(String.valueOf(facetValue.getCount()));
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
                                        @RequestParam(required = false) String type,
                                        @RequestParam(required = false) Integer rows) {
        SolrClient server = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();

        query.setRequestHandler("/name-autocomplete");
        query.setQuery(q);

        if (StringUtils.isNotEmpty(category)) {
            if (StringUtils.contains(category, " ")) {
                category = "\"" + category + "\"";
            }
            query.addFilterQuery("category:" + category);
        }
        if (StringUtils.isNotEmpty(type)) {
            query.addFilterQuery("type:" + type);
        }

        if (rows != null) {
            query.setRows(rows);
        }

        handleRootOnlyResults(query);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        List<SearchResult> results = response.getBeans(SearchResult.class);
        injectAutocompleteHighlighting(results, response);

        List<FacetLookupEntry> values = new ArrayList<>();

        for (SearchResult result : results) {
            FacetLookupEntry entry = new FacetLookupEntry();
            StringBuilder label = new StringBuilder();
            String resultAutocompleteLabel = result.getAutocompleteLabel();
            resultAutocompleteLabel = sanitizeAutocompleteLabel(resultAutocompleteLabel);



            //The injectAutocompleteHighlighting method will set a the autocomplete label if there
            //is going to be some special highlighting in there, otherwise, use the name.
            if (StringUtils.isNotEmpty(resultAutocompleteLabel)) {
                label.append(resultAutocompleteLabel);
            } else {
                label.append(result.getName());
            }

            //Only show additional highlighting information if the name didn't create a highlight match
            if (StringUtils.isNotEmpty(result.getMatchingText()) && StringUtils.isEmpty(resultAutocompleteLabel)) {
                label.append(" [");
                label.append(result.getMatchingText());
                label.append("]");
            }

            entry.setId(result.getId());
            entry.setLabel(label.toString());
            entry.setValue(result.getName());
            entry.setName(result.getFullName());
            entry.setUrl(result.getUrl());
            entry.setCategory(result.getCategory());

            values.add(entry);
        }

        return values;
    }

    /**
     * Do not highlight tags themselves in the autocomplete results.
     * For example, if a search result has the name:
     * "<i>cox4i2</i> expression in cox17<sup>hza3/hza3</sup> from Zhao <i>et al.</i>, 2020 Fig. 6"
     * and the search term contained an "i", the normal highlighting would highlight the "i" in the tag for "<i>et al.</i>"
     * This would result in that tag becoming "<<span class=\"search-result-highlight\">i</span>>et al..."
     * This method removes the tags from the autocomplete label in those cases.
     *
     * @param resultAutocompleteLabel the autocomplete label from the search result to clean up
     * @return the cleaned up autocomplete label
     */
    private String sanitizeAutocompleteLabel(String resultAutocompleteLabel) {
        if (StringUtils.isEmpty(resultAutocompleteLabel)) {
            return resultAutocompleteLabel;
        }
        String regexOpeningTag = """
                                <<span class=\"search-result-highlight\">(.*?)</span>>
                                """;

        String regexClosingTag = """
                                 </<span class=\"search-result-highlight\">(.*?)</span>>
                                 """;
        resultAutocompleteLabel = resultAutocompleteLabel.replaceAll(regexOpeningTag.trim(), "<$1>");
        resultAutocompleteLabel = resultAutocompleteLabel.replaceAll(regexClosingTag.trim(), "</$1>");

        return resultAutocompleteLabel;
    }

    @RequestMapping(value = "/download")
    public void downloadResults(@RequestParam(value = "q", required = false) String q,
                                @RequestParam(value = "fq", required = false) String[] filterQuery,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "page", required = false) String pageNumber,
                                @RequestParam(value = "keepfacets", required = false) Boolean isKeptFacets,
                                HttpServletResponse response,
                                HttpServletRequest request) {

        if (!solrService.allowDownload(q, filterQuery)) {
            try {
                response.sendError(403);
                return;
            } catch (IOException e) {
                logger.error(e);
            }
        }

        response.setContentType("data:text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"zfin_search_results.csv\"");

        SolrQuery query = new SolrQuery();
        query.setParam("wt", "csv");

        category = getCategory(filterQuery, category);

        if (StringUtils.isNotEmpty(category) && !category.equals("Any")) {
            query.addFilterQuery("category:\"" + category + "\"");
        }

        //this should do everything exactly the same as regular controller method to build the result set,
        //but...
        //  leave facets off
        query.setFacet(false);
        //  no highlighting
        query.setHighlight(false);
        //  set the fl to just name, id
        query.set("fl", "id, name");

        if (StringUtils.isNotEmpty(q)) {
            query.setQuery(q);
        }
        if (ArrayUtils.isNotEmpty(filterQuery)) {
            for (String fq : filterQuery) {
                query.addFilterQuery(fq);
            }
        }

        //set rows to...lots
        query.setRows(Integer.MAX_VALUE);

        handleRootOnlyResults(query);

        try {

            URL urlForQuery = SolrService.getUrlForQuery(query);
            URLConnection connection = urlForQuery.openConnection();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            solrService.transformSolrCsvToZfinCsv(query.getFilterQueries(), connection.getInputStream())));

            OutputStreamWriter out = new OutputStreamWriter(
                    new BufferedOutputStream(
                            response.getOutputStream()));

            IOUtils.copy(in, out);

            in.close();
            out.flush();
            out.close();

        } catch (IOException e) {
            logger.error(e);
        }


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

        if (facetField != null && facetField.getValues() != null) {
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
        }

        return facetValues;
    }

    @RequestMapping(value = "/gene-expression/{geneZdbID}")
    public String geneExpressionModal(Model model, @PathVariable String geneZdbID) {
        Marker gene = getMarkerRepository().getMarkerByID(geneZdbID);
        List<GenericTerm> terms = RepositoryFactory.getExpressionRepository().getWildTypeAnatomyExpressionForMarker(geneZdbID);
        Collections.sort(terms);

        LinkedHashMap expressionLinks = new LinkedHashMap();

        if (gene != null && terms != null && terms.size() > 0) {
            for (GenericTerm term : terms) {
                if (!StringUtils.equals(term.getTermName(), "unspecified")) {
                    expressionLinks.put(term.getTermName(), SolrService.getExpressionLink(gene.getAbbreviation(), term.getTermName()));
                }
            }
            model.addAttribute("expressionTermLinks", expressionLinks);
            model.addAttribute("gene", gene);

            //this method is fast, because it's in one query, but on the other end of the results, it doesn't include parents...
            //model.addAttribute("expressionTermLinks",SolrService.getExpressionTermLinks(gene.getAbbreviation()));
            model.addAttribute("allExpressionLink", SolrService.getAllExpressionLink(gene.getAbbreviation()));
            model.addAttribute("wtExpressionLink", SolrService.getWildtypeExpressionLink(gene.getAbbreviation()));
        }
        return "search/gene-expression";
    }

    @RequestMapping(value = "/phenotype/{geneZdbID}")
    public String phenotypeModal(Model model, @PathVariable String geneZdbID) {
        Marker gene = getMarkerRepository().getMarkerByID(geneZdbID);
        // graceful return.
        if (gene == null)
            return "No gene found by ID: " + geneZdbID;
        SolrService.getPhenotypeLink(gene.getAbbreviation());
        Map<String, String> phenotypeLinks = SolrService.getPhenotypeLink(gene.getAbbreviation());
        model.addAttribute("phenotypeLinks", phenotypeLinks);
        model.addAttribute("gene", gene);
        model.addAttribute("allPhenotypeLink", SolrService.getAllPhenotypeLink(gene.getAbbreviation(), true));
        model.addAttribute("allPhenotypeInvolvingLink", SolrService.getAllPhenotypeLink(gene.getAbbreviation(), false));

        return "search/phenotype";
    }

    public Model handleSorting(Model model, SolrQuery query, String baseUrl, String sort, String category) {
        URLCreator urlWithoutSort = new URLCreator(baseUrl);
        urlWithoutSort.removeNameValuePair("sort");
        model.addAttribute("baseUrlWithoutSort", urlWithoutSort.getURL());
        String sortUrlSeparator = "?";
        if (StringUtils.contains(urlWithoutSort.getURL(), "?")) {
            sortUrlSeparator = "&";
        }
        model.addAttribute("sortUrlSeparator", sortUrlSeparator);
        if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "A to Z")) {
            query.addSort("name_sort", SolrQuery.ORDER.asc);
            model.addAttribute("sortDisplay", "A to Z");
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Z to A")) {
            query.addSort("name_sort", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Z to A");
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Newest")) {
            query.addSort("date", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Newest First");
            model.addAttribute("showDates", true);
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Oldest")) {
            query.addSort("date", SolrQuery.ORDER.asc);
            model.addAttribute("sortDisplay", "Oldest First");
            model.addAttribute("showDates", true);
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Most Attributed") && categorySupportsAttributionSort(category)) {
            query.addSort("attribution_count", SolrQuery.ORDER.desc);
            model.addAttribute("sortDisplay", "Most Attributed First");
            model.addAttribute("showAttributionCount", true);
        } else if (!StringUtils.isEmpty(sort) && StringUtils.equals(sort, "Least Attributed") && categorySupportsAttributionSort(category)) {
            query.addSort("attribution_count", SolrQuery.ORDER.asc);
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
            if (key.startsWith("f.") && key.endsWith("facet.sort")) {
                query.add(key, parameterMap.get(key));
            }

        }
        return query;
    }

    private void handleRootOnlyResults(SolrQuery query) {
        if (!ProfileService.isRootUser()) {
            query.add("fq", "root_only:false");
        }
    }

    private String getCategory(String[] filterQuery, String categoryFromPulldown) {
        if (StringUtils.isNotEmpty(categoryFromPulldown)) {
            return categoryFromPulldown;
        }

        Map<String, List<String>> fqMap = SolrService.getFilterQueryMap(filterQuery);

        List<String> categories = fqMap.get("category");
        if (categories == null || categories.isEmpty()) {
            return null;
        } else {
            return categories.get(0).replace("\"", "");
        }
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

        if (response.getHighlighting() == null) {
            return;
        }

        for (SearchResult result : results) {
            result.setHighlightsPreservingPrivacy(solrService.getHighlights(result.getId(), response));
        }

    }

    private List<SearchResult> getXrefResult(SolrClient server, List<String> xrefList) {


        if (CollectionUtils.isEmpty(xrefList)) {
            return null;
        }


        SolrQuery query = new SolrQuery();
        StringBuilder sb = new StringBuilder();

        for (String xref : xrefList) {
            if (!StringUtils.isEmpty(sb.toString())) {
                sb.append(" OR ");
            }
            sb.append("id:");
            sb.append(xref);
        }

        if (StringUtils.isEmpty(sb.toString())) {
            return null;
        }
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
