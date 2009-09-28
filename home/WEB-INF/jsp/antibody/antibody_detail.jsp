<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>

<zfin2:dataManager zdbID="${formBean.antibody.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>

<table width="100%" border="0">
<tr>
    <td width="180">
        <FONT SIZE=+1><STRONG>Antibody Name:</STRONG></FONT>
    </td>
    <td>
      <div style="display:inline;vertical-align:middle;font-size:large;">
        <strong>
            ${formBean.antibody.name}
        </strong>
        </div>
        <c:set var="wikiLink" value="${formBean.wikiLink}"/>
        <c:if test="${!empty wikiLink}">
      <div style="display:inline;vertical-align:middle;font-size:small;">
            &nbsp; &nbsp; 
            <font size=-1>
                <a href="${formBean.wikiLink}" target="_blank" class="external">${formBean.antibody.name} Wiki Page</a>
            </font>
            </div>
        </c:if>
    </td>
    <td align="right">
        <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:put name="subjectName" value="${formBean.antibody.name}"/>
            <tiles:put name="subjectID" value="${formBean.antibody.zdbID}"/>
        </tiles:insert>
    </td>
</tr>

<c:if test="${formBean.antibody.aliases != null}">
    <tr>
        <td>
            <b> Alias: </b>
        </td>
        <td>
            <c:forEach var="markerAlias" items="${formBean.antibody.aliases}" varStatus="loop">
                ${markerAlias.alias}
                <c:if test="${markerAlias.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${markerAlias.publicationCount == 1}">
                            (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${markerAlias.singlePublication.zdbID}">${markerAlias.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="alias-publication-list?markerAlias.zdbID=${markerAlias.zdbID}&orderBy=author">${markerAlias.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </td>
    </tr>
</c:if>

<tr>
    <td>
        <b> Host Organism: </b>
    </td>
    <td>
        ${formBean.antibody.hostSpecies}
    </td>
</tr>

<tr>
    <td>
        <b> Immunogen Organism: </b>
    </td>
    <td>
        ${formBean.antibody.immunogenSpecies}
    </td>
</tr>

<tr>
    <td>
        <b> Isotype: </b>
    </td>
    <td>
        ${formBean.antibody.heavyChainIsotype}
        <c:if
                test="${formBean.antibody.heavyChainIsotype != null && formBean.antibody.lightChainIsotype != null}">,
        </c:if>
        <font face="symbol">${formBean.antibody.lightChainIsotype}</font>
    </td>
</tr>
<tr>
    <td>
        <b> Type: </b>
    </td>
    <td>
        ${formBean.antibody.clonalType}
    </td>
</tr>
<tr>
    <td>
        <b> Assays: </b>
    </td>
    <td>
        <c:forEach var="assay" items="${formBean.antibodyStat.distinctAssayNames}" varStatus="loop">
            ${assay}
            <c:if test="${!loop.last}">
                ,&nbsp;
            </c:if>
        </c:forEach>
    </td>
</tr>
<tr>
    <td width="180">
        <b><b>Antigen Genes:</b> </b>
    </td>
    <td>
        <c:forEach var="antigenRel" items="${formBean.antibodyStat.sortedAntigenRelationships}" varStatus="loop">
            <zfin:link entity="${antigenRel.firstMarker}"/>
            <c:if test="${antigenRel.publicationCount > 0}">
                <c:choose>
                    <c:when test="${antigenRel.publicationCount == 1}">
                        (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${antigenRel.singlePublication.zdbID}">${antigenRel.publicationCount}</a>)
                    </c:when>
                    <c:otherwise>
                        (<a href="relationship-publication-list?markerRelationship.zdbID=${antigenRel.zdbID}&orderBy=author">${antigenRel.publicationCount}</a>)
                    </c:otherwise>
                </c:choose>
            </c:if>
            <c:if test="${!loop.last}">
                ,&nbsp;
            </c:if>
        </c:forEach>
    </td>
</tr>

</table>

<p/>
<b>NOTES:</b>
<c:if test="${formBean.numOfUsageNotes eq null || formBean.numOfUsageNotes ==0 }">
    None Submitted
</c:if>

<c:if test="${formBean.numOfUsageNotes > 0}">
    <table width=100% border=0 cellspacing=0>
        <tr bgcolor="#cccccc">
            <td width="30%"><b>Reference</b></td>
            <td><b>Comment</b></td>
        </tr>
        <c:forEach var="extnote" items="${formBean.notesSortedByPubTime}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td valign="top">
                    <zfin:link entity="${extnote.singlePubAttribution.publication}"/>
                </td>
                <td>
                    <zfin2:toggleTextLength text="${extnote.note}" idName="${loop.index}" shortLength="80"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
<p/>

<b>ANATOMICAL LABELING</b>&nbsp;
<c:import url="/WEB-INF/jsp/antibody/antibody_labeling_detail.jsp"/>

<p/>
<b>SOURCE:</b>
<br/>
<c:choose>
    <c:when test="${formBean.antibody.suppliers ne null && fn:length(formBean.antibody.suppliers) > 0}">
        <table width=100% border=0 cellspacing=0>
            <c:forEach var="supplier" items="${formBean.antibody.suppliers}" varStatus="status">
                <zfin:alternating-tr loopName="status">
                    <td>
                        <c:choose>
                            <c:when test="${supplier.organization.url == null}">
                                <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}">
                                        ${supplier.organization.name}
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${supplier.organization.url}">${supplier.organization.name}</a>
                            </c:otherwise>
                        </c:choose>
<%--
                        <c:if test="${supplier.orderURL != null && supplier.available ne null}">
                            &nbsp;&nbsp;&nbsp;
                            <font size="-1">
                                <a href="${supplier.orderURL}">
                                    (order antibody / more info)
                                </a>
                            </font>
                        </c:if>
--%>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </c:when>
    <c:otherwise>
        <table width=100% border=0 cellspacing=0>
            <tr class="odd">
                <td width="30%">None Submitted</td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>

<hr width="80%">
<a href="publication-list?antibody.zdbID=${formBean.antibody.zdbID}&orderBy=author">CITATIONS</a>&nbsp;&nbsp;(${formBean.numOfPublications})

