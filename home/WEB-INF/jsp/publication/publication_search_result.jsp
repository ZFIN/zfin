<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC" colspan="3">
            <STRONG>
                All Publications for Gene
                <zfin:link entity="${formBean.marker}" />
                and Anatomical Structure
                <zfin:link entity="${formBean.anatomyItem}" />

            </STRONG> (sorted by date)
        </TD>
    </TR>
</TABLE>
<TABLE width="100%" cellpadding="4">
    <tr class="odd">
        <td width=50> Date</td>
        <td>Authors </td>
        <td>Title </td>
        <td width=100>Journal </td>
    </tr>
    <c:forEach var="pub" items="${formBean.publications}" varStatus="rowCounter">
        <c:choose>
            <c:when test="${rowCounter.count % 2 != 0}">
                <tr class="odd">
            </c:when>
            <c:otherwise>
                <tr>
            </c:otherwise>
        </c:choose>
        <TD>
            <bean:write name="pub" property="publicationDate.time" format="yyyy"/>
        </TD>
        <td>
<!--
            Maybe here we need the autho long list???
-->
            <zfin:link entity="${pub}" />
        </TD>
        <td>
            <c:out value="${pub.title}"/>
        </TD>
        <td>
            <c:out value="${pub.journal.abbreviation}"/> <c:out value="${pub.volume}"/>:<c:out value="${pub.pages}"/>
        </TD>
        </TR>
    </c:forEach>
</TABLE>

<tiles:insert page="/WEB-INF/jsp-include/pagination.jsp" flush="false"/>
