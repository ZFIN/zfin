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




<%--${criteria.name}
${criteria.accession}
${criteria.chromosome}--%>



<c:if test="${!empty criteria.typesFound && empty criteria.displayType}">
    <div class="marker-search-type-choice row">

        <div class="col-sm-4 col-sm-offset-4">
            <c:if test="${criteria.name != null || criteria.accession != null}">
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

<%--todo: handle 0 results --%>

<c:if test="${!empty criteria.results && !empty criteria.displayType}">

    <c:choose>  <%-- todo: use marker type enum? that should be what Solr always populates from --%>
        <c:when test="${criteria.isGenedomResult()}">
            <zfin-marker-search:geneResultTable/>
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
