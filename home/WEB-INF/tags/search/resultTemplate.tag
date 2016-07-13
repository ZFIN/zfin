<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag description="Search result template for faceted search" pageEncoding="UTF-8"%>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<%@attribute name="metadata" fragment="true" %>
<%@attribute name="relatedDataLinks" fragment="true" %>
<%@attribute name="curatorContent" fragment="true" %>
<%-- Initially set to display:none for root users only, since they're the only users that can select the table view,
    document.ready in prototype-results.jsp will make these visible --%>
<div style="clear:both; <authz:authorize access="hasRole('root')">display:none;</authz:authorize>" class="col-md-12 search-result boxy-search-result">

    <div class="result-meta-data search-result-category">
        <jsp:invoke fragment="metadata"/>
    </div>

    <c:if test="${!empty result.displayedID}">
        <span class="result-id"> ${result.displayedID}</span>
    </c:if>
    <div class="result-header search-result-name">
        <zfin:link entity="${result}"/>

        <authz:authorize access="hasRole('root')">
          <a title="see everything solr knows about this record"
             class="solr-document-link"
             href="/solr/prototype/select?q=id:${result.id}&fl=*&wt=json&indent=true&hl=false&rows=1"><i class="fa fa-file-text-o"></i></a>
          <c:if test="${not empty result.explain}">
            <span title="see solr query explain info"
               class="result-explain-link"><i class="fa fa-list-ol"></i></span>
          </c:if>
        </authz:authorize>


    </div>

    <div class="result-body">
        <table style="width: 99%">
            <tr>
                <td>
                    <c:if test="${!empty result.attributes}">
                        <table class="search-result-entity-attributes">
                            <c:forEach var="entry" items="${result.attributes}" varStatus="loop">
                                <tr>
                                    <th width="5%">${entry.key}</th>
                                    <td>${entry.value}</td>
                                </tr>
                            </c:forEach>
                        </table>
                    </c:if>
                </td>
                <td align="right" valign="top">
                    <c:if test="${not empty result.image}">
                        <zfin-search:imageModal result="${result}"/>
                    </c:if>
                    <c:if test="${not empty result.snapshot}">
                        <div class="pull-right result-thumbnail-container">
                            <div class="search-result-thumbnail">
                                <a href="${result.url}">
                                    <img style="max-width: 150px; max-height: 70px;"
                                         src="/action/profile/image/view/${result.snapshot}.jpg">
                                </a>
                            </div>
                        </div>
                    </c:if>
                </td>
            </tr>
        </table>

        <c:if test="${!empty result.featureGenes}">
            <table class="fish-result-table">
                <tr>
                    <th>Affected Gene</th>
                    <th>Line / Reagent</th>
                    <th>Mutation Type</th>
                    <th>Construct</th>
                    <th>Parental Zygosity</th>
                </tr>
                <c:forEach var="featureGene" items="${result.featureGenes}">
                    <tr>
                        <td title="Affected Gene">
                            <zfin:link entity="${featureGene.gene}" suppressPopupLink="true"/>
                        </td>
                        <td title="Line / Reagent">
                            <c:if test="${!empty featureGene.feature}">
                                <zfin:link entity="${featureGene.feature}" suppressPopupLink="true"/>
                            </c:if>
                            <c:if test="${!empty featureGene.sequenceTargetingReagent}">
                                <zfin:link entity="${featureGene.sequenceTargetingReagent}" suppressPopupLink="true"/>
                            </c:if>
                        </td>
                        <td title="Mutation Type">
                            <c:if test="${!empty featureGene.feature}">
                                ${featureGene.feature.type.display}
                            </c:if>
                        </td>
                        <td title="Construct">
                            <zfin:link entity="${featureGene.construct}" suppressPopupLink="true"/>
                        </td>
                        <td title="Parental Zygosity">
                                ${featureGene.parentalZygosityDisplay}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>

        <jsp:doBody/>
    </div>

    <div class="search-result-related-links">
        <c:choose>
            <c:when test="${!empty relatedDataLinks}">
                <jsp:invoke fragment="relatedDataLinks"/>
            </c:when>
            <c:when test="${!empty result.relatedLinks}">
                <ul>
                <c:forEach var="link" items="${result.relatedLinks}">
                    <li>${link}</li>
                </c:forEach>
                </ul>
            </c:when>
        </c:choose>
    </div>

    <authz:authorize access="hasRole('root')">
        <jsp:invoke fragment="curatorContent"/>
    </authz:authorize>

    <div class="result-matching-text search-result-snippet">
        <c:if test="${!empty result.matchingText}">
            <div class="snippet-title">
                Matching Text:
            </div>
        </c:if>
        ${result.matchingText}
    </div>

    <c:if test="${not empty result.explain}">
        <%-- indented this way on puprpose to preserve the indenting in the value --%>
        <div class="result-explain-container">
            <div class="result-explain-label">
            Debug Explain Output
            </div>
            <div class="result-explain">
${result.explain}
            </div>
        </div>
    </c:if>


        <div style="clear:both;">&nbsp;</div>


</div>