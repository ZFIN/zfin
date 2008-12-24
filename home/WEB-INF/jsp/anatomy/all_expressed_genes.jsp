<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC" colspan="3">
            <STRONG>
                All Expressed Genes in <i>
                <zfin:link entity="${formBean.anatomyItem}"/>
            </i>
            </STRONG> (sorted by number of publications)
        </TD>
    </TR>
</TABLE>
<TABLE width="400">
    <tr>
        <td bgcolor="#EEEEEE" valign=top width=110>
            Gene Symbol
        </td>
        <td bgcolor="#EEEEEE">Expression Data <br/>
        </td>
        <td bgcolor="#EEEEEE">Publications<br/>
        </td>
    </tr>
    <c:forEach var="marker" items="${formBean.allExpressedMarkers}" varStatus="rowCounter">
        <c:choose>
            <c:when test="${rowCounter.count % 2 != 0}">
                <tr class="odd">
            </c:when>
            <c:otherwise>
                <tr>
            </c:otherwise>
        </c:choose>
        <TD>
            <zfin:abbrev entity="${marker.markerStat.gene}"/>
        </TD>
        <td>
            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg&query_results=true&gene_name=<c:out value="${marker.markerStat.gene.abbreviation}" />&xpatsel_geneZdbId=<c:out value="${marker.markerStat.gene.zdbID}" />'>
                <c:out value="${marker.markerStat.numberOfFigures}"/>
            </a>
        </TD>
        <td>
            <a href="/action/publication/publication-search-result?marker.zdbID=<c:out value="${marker.markerStat.gene.zdbID}"/>&anatomyItem.zdbID=<c:out value="${formBean.anatomyItem.zdbID}"/>">
                <c:out value="${marker.markerStat.numberOfPublications}"/>
            </a>
        </TD>
        </TR>
    </c:forEach>
</TABLE>

<zfin2:pagination paginationBean="${formBean}" />

