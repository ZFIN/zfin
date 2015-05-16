<%--
  Created by IntelliJ IDEA.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table class="data_manager">
    <tbody>
        <tr>
            <td>
                <strong>ZFIN ID:</strong>&nbsp;${formBean.marker.zdbID}
            </td>
        </tr>
    </tbody>
</table>


<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <span class="citation-heading">CITATIONS</span>&nbsp;(${formBean.numOfPublications} total)
        </td>
    </tr>
</table>
<div class="name-label">
  Clone name: <zfin:link entity="${formBean.marker}"/> 
</div>
<form>
    <c:if test="${formBean.numOfPublications > 1}">
        <c:choose>
            <c:when test="${formBean.orderBy == 'author'}">
                <input type=button name=resultOrder
                       onClick="document.location.replace('snp-publication-list?orderBy=date&markerID=${formBean.marker.zdbID}')"
                       value="Order By Date">
            </c:when>
            <c:otherwise>
                <input type=button name=resultOrder
                       onClick="document.location.replace('snp-publication-list?orderBy=author&markerID=${formBean.marker.zdbID}')"
                       value="Order By Author">
            </c:otherwise>
        </c:choose>
    </c:if>

    <table cellspacing=0>

        <c:forEach var="publication" items="${formBean.sortedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td align=left>
                    <div class="show_pubs">
                        <a href="/${publication.zdbID}">${publication.authors}
                            &nbsp;(${publication.year})&nbsp;${publication.title}
                        </a>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

</form>