<%--
  Created by IntelliJ IDEA.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td>
                <font size="-1"><b>ZFIN ID:</b>
                    ${formBean.markerAlias.zdbID}
                </font>&nbsp;&nbsp;&nbsp;&nbsp;
            </td>
        </tr>
    </tbody>
</table>

<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <font size=+2><b>CITATIONS</b></font>

            (${formBean.numOfPublications} total)

        </td>
    </tr>
</table>
<p>

<form>

    <c:if test="${formBean.numOfPublishedPublications > 1 || formBean.numOfUnpublishedPublications > 1}">
        <c:choose>
            <c:when test="${formBean.orderBy == 'author'}">
                <input type=button name=resultOrder
                       onClick="document.location.replace('alias-publication-list?orderBy=date&markerAlias.zdbID=${formBean.markerAlias.zdbID}')"
                       value="Order By Date">
            </c:when>
            <c:otherwise>
                <input type=button name=resultOrder
                       onClick="document.location.replace('alias-publication-list?orderBy=author&markerAlias.zdbID=${formBean.markerAlias.zdbID}')"
                       value="Order By Author">
            </c:otherwise>
        </c:choose>
    </c:if>

    <table cellspacing=0>
        <c:forEach var="publishedPublication" items="${formBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td align=left>
                    <div class="show_pubs">
                        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${publishedPublication.zdbID}">${publishedPublication.authors}
                            &nbsp;(${publishedPublication.year})&nbsp;${publishedPublication.title}.&nbsp;${publishedPublication.journal.abbreviation}&nbsp;<c:if test="${publishedPublication.volume != null}">${publishedPublication.volume}:</c:if>${publishedPublication.pages}
                        </a>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>

        <c:if test="${formBean.numOfUnpublishedPublications > 0}">
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td><b>Other Citations (${formBean.numOfUnpublishedPublications}):</b></td>
            </tr>
            <c:forEach var="unpublishedPublication" items="${formBean.sortedUnpublishedPublications}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td align=left>
                        <div class="show_pubs">
                            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                                &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
                            </a>
                        </div>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </c:if>
    </table>

</form>