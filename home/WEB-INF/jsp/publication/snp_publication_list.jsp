<%--
  Created by IntelliJ IDEA.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td>
                <font size="-1"><b>ZFIN ID:</b>&nbsp;${formBean.marker.zdbID}</font>
            </td>
        </tr>
    </tbody>
</table>


<table width=100%>
    <tr>
        <td bgcolor=#ccccccc>
            <font size=+2><b>CITATIONS</b></font>

            (${formBean.numOfPublications} total)

        </td>
    </tr>
</table>
<p>

<form>

    <font size=+1>
        <b>
            Clone name: ${formBean.marker.name}
            <br/>
        </b>
    </font>

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
                        <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${publication.zdbID}">${publication.authors}
                            &nbsp;(${publication.year})&nbsp;${publication.title}
                        </a>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

</form>