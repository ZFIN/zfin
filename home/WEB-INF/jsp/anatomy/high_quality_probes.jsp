<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td><font size="-1">&nbsp;</font>
            </td>
        </tr>
    </tbody>
</table>

<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC" colspan="3">
            <STRONG>
                In-Situ Probes for <i>
                <zfin:link entity="${formBean.anatomyItem}" />
            </i></STRONG>: &nbsp; <a
                href="/zf_info/stars.html">Recommended</a>
            by
            <c:forEach var="pub" items="${formBean.qualityProbePublications}">
                (<zfin:link entity="${pub}"/>)
                &nbsp;
            </c:forEach>
        </TD>
    </TR>
</TABLE>
<TABLE width="70%">
    <tr>
        <td bgcolor="#EEEEEE" valign=top align=left width=110>
            Gene Symbol</td>
        <td bgcolor="#EEEEEE">Probe Name <br/>
        </td>
        <td bgcolor="#EEEEEE">Gene Expression Data<br/>
        </td>
    </tr>
    <c:forEach var="genes" items="${formBean.highQualityProbeGenes}" varStatus="rowCounter">
        <c:choose>
            <c:when test="${rowCounter.count % 2 != 0}">
                <tr class="odd">
            </c:when>
            <c:otherwise>
                <tr>
            </c:otherwise>
        </c:choose>
        <TD>
            <zfin:link entity="${genes.gene}" />
        </TD>
        <td>
            <zfin:link entity="${genes.subGene}" />
        </TD>
        <td>
            <a HREF='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxallfigures.apg&OID=<c:out value="${genes.publication.zdbID}" />&fxallfig_probe_zdb_id=<c:out value="${genes.subGene.zdbID}" />'>
                <c:out value="${genes.numberOfFigures}"/> (<c:out value="${genes.numberOfImages}"/>)</a>
        </TD>
        </TR>
    </c:forEach>
</TABLE>
