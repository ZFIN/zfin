package org.zfin.framework.presentation.tags;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.zfin.search.service.SolrService;
import org.apache.commons.lang.StringUtils;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.*;


public class ShowFacetLinksTag extends TagSupport {

    private static int VISIBLE_FACET_VALUES = 4;
    private static int VISIBLE_FACETS = 3;

    private QueryResponse queryResponse;
    private HttpServletRequest request;
    private SolrQuery query;
    private String baseUrl;

    private Logger logger = Logger.getLogger(ShowFacetLinksTag.class);


    private String createFacetSection() {


        QueryResponse response = getQueryResponse();

        //a map to know whether or not to show a facet value as a link
        HashMap<String, Boolean> fqMap = new HashMap<String, Boolean>();

        if (query != null && query.getFilterQueries() != null) {
            for (String fq : query.getFilterQueries()) {
                fqMap.put(fq,true);
                logger.debug("added to fqMap: " + fq);
            }
        }

        if (response == null) {
            logger.error("query response was null");
            return null;
        }

        if (response.getFacetFields() == null) {
            logger.error("no facet fields in the query, set facet=true and facet.field=something");
            return null;
        }

        StringBuilder facetHtml = new StringBuilder();


        if (response.getFacetFields().size() > 0)
            facetHtml.append("<div class=\"facet-container\"> \n");

        int facetFieldIndex = 1;
        Boolean visible;
        for (FacetField facetField : response.getFacetFields()) {
            //no need to account for facets with no values
            if (facetField.getValueCount() > 0) {
                if (facetFieldIndex > VISIBLE_FACETS)
                    visible = Boolean.FALSE;
                else
                    visible = Boolean.TRUE;
                facetHtml.append(showSingleFacetField(facetField, fqMap, visible));
                facetFieldIndex++;
            }
        }
        if (response.getFacetFields().size() > 0)
            facetHtml.append("</div>");

        return facetHtml.toString();
    }

    public String getFacetFilterForm(FacetField facetField) {
        StringBuilder sb = new StringBuilder();

        String fieldName = facetField.getName();
        sb.append("<div class=\"facet-filter-box " + fieldName + "-toggle \">");
        sb.append("<form class=\"facet-filter-form form-inline form-search\" id=\"" + fieldName + "-facet-filter-form\" facetfield=\"" + fieldName + "\">");
        sb.append("</form>");
        sb.append("</div>");

        return sb.toString();
    }

    public String getFacetSortLinks(String baseUrl, FacetField facetField) {
        String fieldName = "f." + facetField.getName() + ".facet.sort";

        URLCreator alphaSortUrl = new URLCreator(baseUrl);
        alphaSortUrl.removeNameValuePair(fieldName);
        alphaSortUrl.addNameValuePair(fieldName,"index");

        URLCreator countSortUrl = new URLCreator(baseUrl);
        countSortUrl.removeNameValuePair(fieldName);
        countSortUrl.addNameValuePair(fieldName,"count");


        StringBuilder sb = new StringBuilder();
        sb.append("<span class=\"facet-sort-links " + facetField.getName() + "-toggle\">");
        sb.append("Sort: <a href=\"");
        sb.append(countSortUrl.getURL() + "#" + facetField.getName());
        sb.append("\">#</a>");

        sb.append(" | ");

        sb.append("<a href=\"");
        sb.append(alphaSortUrl.getURL() + "#" + facetField.getName());
        sb.append("\">A-Z</a>");
        sb.append("</span>");

        return sb.toString();
    }

    private String showSingleFacetField(FacetField facetField, Map<String, Boolean> fqMap, boolean visible) {
        StringBuilder facetHtml = new StringBuilder();

        if (facetField.getValueCount() > 0) {
            boolean expandable = false;
            if (facetField.getValueCount() > 7 && !StringUtils.equals("category",facetField.getName()))
                expandable = true;

            facetHtml.append("<ol class=\"facet-value-list\" id=\"" + facetField.getName() + "-facet-value-list\"> \n");
            facetHtml.append("<a name=\"" + facetField.getName() + "\"/></a>");

            facetHtml.append("<div id=\"" + facetField.getName() + "-facet-group-label-container\" class=\"facet-group-label-container\">");
            facetHtml.append("<span class=\"facet-label\" id=\"" + facetField.getName() + "-facet-label\">");
            facetHtml.append(SolrService.getPrettyFieldName(facetField.getName()));
            facetHtml.append("</span>");

            if (!SolrService.hideFacetFiltersForField(facetField.getName()))
                facetHtml.append(getFacetSortLinks(baseUrl, facetField));

            facetHtml.append("</div> \n");

            facetHtml.append("<div class=\"facet-value-outer-box\" id=\"" + facetField.getName() + "-facet-value-outer-box\" ");
            if (!visible)
                    facetHtml.append(" style=\"display: none\"");
            facetHtml.append("   >");
            facetHtml.append("<script>jQuery('#" + facetField.getName() + "-facet-group-label-container').click(function() {" +
                    " jQuery('#" + facetField.getName() + "-facet-value-outer-box').slideToggle(); " +
                    " });  </script>");
            facetHtml.append(getFacetFilterForm(facetField));

            facetHtml.append("<div class=\"single-facet-value-container\" id=\"" + facetField.getName() +  "-facet-value-container\">");


            int facetValueIndex = 0;

            if (facetField.getValues() != null) {

                ArrayList<FacetField.Count> facetValues = SolrService.reorderFacetField(facetField, fqMap);

                for (FacetField.Count count : facetValues) {
                    if (expandable && facetValueIndex == VISIBLE_FACET_VALUES) {
                        facetHtml.append("<div ");
                        facetHtml.append(" id=\"" + facetField.getName() + "-additional-values\" " );
                        facetHtml.append(" class=\"additional-facet-values " + facetField.getName() + "-toggle \" ");
                        facetHtml.append(">");
                    }

                    String quotedFq = facetField.getName() + ":\"" + count.getName() + "\"";

                    if (!fqMap.containsKey(quotedFq)) {
                        facetHtml.append(showSingleFacetValue(facetField, count, quotedFq, false));
                        //only unselected facets count towards the number visible
                        facetValueIndex++;
                    } else {
                        facetHtml.append(showSingleFacetValue(facetField, count, quotedFq, true));

                    }


                    if (expandable && facetValueIndex == facetField.getValueCount())
                        facetHtml.append("</div>");
                }
            }

            if (expandable) {
                facetHtml.append("<li><div id=\"" + facetField.getName() + "-facet-expand-contract-links\" class=\"facet-expand-contract-links\">");
                facetHtml.append("<a id=\"" + facetField.getName()
                        + "\" class=\"facet-expand-more-link " + facetField.getName() + "-toggle \" href=\"#" + facetField.getName() +  "\" onClick=\"jQuery('."
                        + facetField.getName() + "-toggle').toggle();" +
                        "\">Show More</a> ");
                facetHtml.append("<a id=\"" + facetField.getName()
                        + "\" class=\"facet-expand-less-link " + facetField.getName() + "-toggle \" href=\"#" + facetField.getName() +  "\" onClick=\"jQuery('."
                        + facetField.getName() + "-toggle').toggle() ;"
                        + " jQuery('html,body').animate({scrollTop:jQuery('#" + facetField.getName() + "-facet-label').offset().top}, 50);  "
                        + "\">Show Less</a> ");

                facetHtml.append("</div></li>");
            }
            facetHtml.append("</div>");

            facetHtml.append("</ol>");
        }

        return facetHtml.toString();
    }

    public String showSingleFacetValue(FacetField facetField, FacetField.Count count, String quotedFq, Boolean selected) {
        StringBuilder facetHtml = new StringBuilder();

        facetHtml.append("<li style=\"min-height:10px\" class=\"facet-value row-fluid\">");

        //if it's going to be an 'unselected' link, rather than a 'selected / breadbox' link
        if (!selected) {

            //set css per category
            String categoryCssClasses = "";
            facetHtml.append("<span style=\"min-height:10px\" class=\"col-md-9 selectable-facet-value\">");
            facetHtml.append("<a title=\"require in results\" style=\"min-height:10px\" class=\" ");
            facetHtml.append(categoryCssClasses);
            facetHtml.append("\" href=\"");
            facetHtml.append(SolrService.getFacetUrl(facetField, count, baseUrl));
            facetHtml.append("\">");
            facetHtml.append("<img class=\"checkbox-icon\" src=\"/images/icon-check-empty.png\"/>");
            facetHtml.append(count.getName());
            facetHtml.append("</a>");
            facetHtml.append("</span>");

            String shortenedName = SolrService.shortenFacetValue(count.getName());

            facetHtml.append("<ul style=\"min-height:10px\" class=\"facet-count-container col-md-3 unstyled\">\n" +
                    "  <li class=\"dropdown\">\n" +
                    "    <a class=\"facet-count dropdown-toggle\"\n" +
                    "       data-toggle=\"dropdown\"\n" +
                    "       href=\"#\">\n(" +
                    count.getCount() +
                    ")        <b class=\"caret\"></b>\n" +
                    "      </a>\n" +
                    "    <ul class=\"dropdown-menu\">\n" +
                    "      <li><a href=\"" + SolrService.getFacetUrl(facetField, count, baseUrl) + "\">Require</a></li>\n" +
                    "      <li><a href=\"" + SolrService.getNotFacetUrl(facetField, count, baseUrl) + "\">Exclude</a></li>\n");
            if (SolrService.isAJoinableFacetField(facetField.getName())) {
                facetHtml.append("      <li class=\"divider\"></li>\n" +
                        "      <li><a target=\"_blank\" href=\"/prototype?q=" + count.getName()
                        + "\">Search for <strong>" + count.getName() + "</strong> in New Window</a></li>\n");
            }
            facetHtml.append(        "    </ul>\n" +
                    "  </li>\n" +
                    "</ul>");


        } else {
            facetHtml.append(SolrService.getBreadBoxLink(quotedFq, baseUrl, false));
        }
        facetHtml.append("</li>");

        return facetHtml.toString();
    }

    public int doStartTag() throws JspException {

        String facetHtml = createFacetSection();
        try {
            pageContext.getOut().print(facetHtml);
        } catch (IOException ioe) {
                    throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return SKIP_BODY;
    }


    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;

    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }

    public void setQueryResponse(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }

    public SolrQuery getQuery() {
        return query;
    }

    public void setQuery(SolrQuery query) {
        this.query = query;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
