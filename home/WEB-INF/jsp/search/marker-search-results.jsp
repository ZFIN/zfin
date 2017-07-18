<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style>
    .marker-search-type-choice {
        font-size: large;
    }
    .marker-search-type-choice ul {
        list-style-type: none;
    }
</style>


<c:if test="${!empty criteria.typesFound && empty criteria.displayType}">
    <div class="marker-search-type-choice row">

        <div class="col-sm-4 col-sm-offset-4">
            <c:if test="${!empty criteria.name || !empty criteria.accession}">
                Search results for
                <c:if test="${!empty criteria.name}"> name: '${criteria.name}'</c:if>
                <c:if test="${!empty criteria.accession}"> accession: '${criteria.accession}'</c:if>
            </c:if>

            <ul>
                <c:forEach var="type" items="${criteria.typesFound}">
                    <li>
                        <a href="${criteria.baseUrl}&displayType=${type.name}">
                            ${type.name} (${type.count})
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</c:if>

<c:if test="${criteria.searchHappened && empty criteria.results && empty criteria.typesFound}">
    <div class="no-results-found-message">
        No results were found matching your query.
    </div>
</c:if>


<c:if test="${!empty criteria.results && !empty criteria.displayType}">

    <c:choose>  <%-- todo: use marker type enum? that should be what Solr always populates from --%>
        <c:when test="${criteria.isGenedomResult()}">
            <zfin-marker-search:geneResultTable/>
        </c:when>
        <c:when test="${criteria.isCloneResult()}">
            <zfin-marker-search:cloneResultTable/>
        </c:when>
        <c:when test="${criteria.isTranscriptResult()}">
            <zfin-marker-search:transcriptResultTable/>
        </c:when>
        <c:when test="${criteria.isStrResult()}">
            <zfin-marker-search:strResultTable/>
        </c:when>
        <c:otherwise>
            <zfin-marker-search:genericResultTable/>
        </c:otherwise>
    </c:choose>


    <div style="clear: both ; width: 80%" class="clearfix">
        <zfin2:pagination paginationBean="${paginationBean}"/>
    </div>

</c:if>

<zfin-marker-search:markerSearchForm criteria="${criteria}"/>
