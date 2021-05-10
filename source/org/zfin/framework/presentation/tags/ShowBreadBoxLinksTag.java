package org.zfin.framework.presentation.tags;


import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.zfin.search.service.SolrService;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;


public class ShowBreadBoxLinksTag extends TagSupport {

    private QueryResponse queryResponse;
    private SolrQuery query;
    private String baseUrl;

    private Logger logger = LogManager.getLogger(ShowBreadBoxLinksTag.class);

    private String createBreadBoxSection()  {
        QueryResponse response = getQueryResponse();

        if (response == null) {
            logger.error("query response was null");
            return null;
        }

        if (response.getHeader() == null) {
            logger.error("header was null");
            return "";
        }

        if (query == null) {
            logger.error("query was null");
            return "";
        }

        //this one isn't an error state - it just means no facets are selected
        if (query.getFilterQueries() == null) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        out.append("<div class=\"breadbox\">");
        out.append("<ul class=\"breadbox-value-list\"><div class=\"breadbox-title-container\"><span class=\"breadbox-title\">Your Selections</span></div>");
        for (String fq : query.getFilterQueries()) {
            out.append(getBreadBoxLinkHtml(fq));
        }

        out.append("</ul>");
        out.append("</div>");

        return out.toString();
    }

    private String getBreadBoxLinkHtml(String fq) {
        return SolrService.getBreadBoxLink(fq, baseUrl, true);
    }

    public int doStartTag() throws JspException {

        String facetHtml = createBreadBoxSection();
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public SolrQuery getQuery() {
        return query;
    }

    public void setQuery(SolrQuery query) {
        this.query = query;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
