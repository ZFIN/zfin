<%@ page import="org.zfin.publication.Publication" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<meta name="image-view-page"/> <%-- this is used by the web testing framework to know which page this is--%>

<%--
     Nothing is stored in the updates table for figures, so no lastUpdated date is passed in
--%>

<c:set var="UNPUBLISHED" value="<%=Publication.Type.UNPUBLISHED %>"/>
<c:set var="CURATION" value="<%=Publication.Type.CURATION %>"/>

<c:set var="editURL">/action/publication/image-edit?zdbID=${image.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${image.zdbID}</c:set>


<zfin2:dataManager zdbID="${image.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   rtype="image"/>





<c:if test="${image.figure ne null}">
<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">

        <tiles:putAttribute name="subjectName" value="${image.figure.publication.shortAuthorList} - ${image.figure.label}"/>

    </tiles:insertTemplate>
</div>
</c:if>

<%--<zfin-figure:publicationInfo publication="${image.figure.publication}"
                             submitters="${submitters}"
                             showThisseInSituLink="${showThisseInSituLink}"
                             showErrataAndNotes="${showErrataAndNotes}"/>--%>
<c:if test="${!empty image.figure}">
<c:if test="${fn:length(image.figure.publication.figures) > 1}">
    <div style="margin-top: 1em;">
        <c:set var="probeUrlPart" value=""/>
        <c:if test="${!empty probe}">
            <c:set var="probeUrlPart" value="?probeZdbID=${probe.zdbID}"/>
        </c:if>

    <c:if test="${image.figure.publication.type == CURATION}">
        <c:if test="${!empty probe}">
        <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">All Figures for ${image.figure.publication.shortAuthorList}</a>
        </c:if>
        </c:if>
    <c:if test="${image.figure.publication.type != CURATION}">
        <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">Figures for ${image.figure.publication.shortAuthorList}[${probe.abbreviation}]</a>
        </c:if>
    </div>
</c:if>
<p>
<c:if test="${!empty expressionGeneList}">
    <tr>
        <th>
            <zfin:choice choicePattern="0#Genes:| 1#Gene:| 2#Genes:" integerEntity="${fn:length(expressionGeneList)}"/>
        </th>
        <td> <zfin2:toggledPostcomposedList entities="${expressionGeneList}" maxNumber="5"/>  </td>
    </tr>
</c:if>

<c:if test="${!empty antibodyList}">
    <tr>
        <th>
            <zfin:choice choicePattern="0#Antibodies:| 1#Antibody:| 2#Antibodies:" integerEntity="${fn:length(antibodyList)}"/>
        </th>
        <td> <zfin2:toggledPostcomposedList entities="${antibodyList}" maxNumber="5"/>  </td>
    </tr>
</c:if>
</c:if>
<p>

<zfin-figure:imageView image="${image}"/>

<zfin-figure:imageDetails image="${image}"/>
    <c:if test="${!empty image.imageStage.start}">
 <zfin-figure:devStage image="${image}"/>
    </c:if>
    <c:if test="${image.preparation ne 'not specified'&&image.form ne 'not specified'&&image.direction ne 'not specified'&&image.view ne 'not specified'}">

    <zfin-figure:imageOrientation image="${image}"/>
    </c:if>
    <c:if test="${!empty image.figure}">
<c:choose>
    <c:when test="${image.figure.publication.canShowImages && image.figure.publication.type != UNPUBLISHED}">
        <zfin2:acknowledgment publication="${figure.publication}" showElsevierMessage="${showElsevierMessage}" hasAcknowledgment="${hasAcknowledgment}"/>
    </c:when>
    <c:otherwise>
        <zfin2:subsection>
            <zfin-figure:journalAbbrev publication="${image.figure.publication}"/>
        </zfin2:subsection>
    </c:otherwise>
</c:choose>
    </c:if>

<script>
    jQuery(document).ready(function() {
        jQuery('.fish-label').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
    });
</script>
