<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>OBO ID:</strong>&nbsp;${term.oboID}
        </td>
    </tr>
    </tbody>
</table>
<table width=100%>
    <tr>
        <td>
            <span class="citation-heading">CITATIONS</b></span>&nbsp;(${citationList.size()} total)
        </td>
    </tr>
</table>

<div class="name-label">
        Term Name:&nbsp;<a href="/action/ontology/term-detail/${term.oboID}">${term.termName}</a>
</div>

<c:choose>
    <c:when test="${orderBy eq 'author'}">
        <input type=button name=resultOrder
               onClick="document.location.replace('/action/ontology/disease-publication-list/${term.oboID}?orderBy=date')"
               value="Order By Date">
    </c:when>
    <c:otherwise>
        <input type=button name=resultOrder
               onClick="document.location.replace('/action/ontology/disease-publication-list/${term.oboID}?orderBy=author')"
               value="Order By Author">
    </c:otherwise>
</c:choose>
&nbsp;&nbsp;&nbsp;


<c:if test="${citationList.size() > 0}">
    <br/>
    <table class="summary rowstripes">
        <tbody>
        <c:forEach var="unpublishedPublication" items="${citationList}"
                   varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <div class="show_pubs">
                        <a href="/${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                            &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
                        </a>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>

