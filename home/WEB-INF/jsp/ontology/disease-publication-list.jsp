<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="summary rowstripes">
    <tbody>
    <tr>
        <td>
            <font size="-1"><b>OBO ID:</b>
                ${term.oboID}
            </font>
        </td>
    </tr>
    </tbody>
</table>
<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <font size=+2><b>CITATIONS</b></font>

            (${citationList.size()} total)

        </td>
    </tr>
</table>

<font size=+1>
    <b>
        Term Name:&nbsp;<a href="/action/ontology/term-detail/${term.oboID}">${term.termName}</a>
        <br/>
    </b>
</font>

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

