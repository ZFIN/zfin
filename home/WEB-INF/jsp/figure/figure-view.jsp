<%@ page import="org.zfin.publication.Publication" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<meta name="figure-view-page"/> <%-- this is used by the web testing framework to know which page this is--%>

<%--
     Nothing is stored in the updates table for figures, so no lastUpdated date is passed in
--%>

<c:set var="UNPUBLISHED" value="<%=Publication.Type.UNPUBLISHED %>"/>
<c:set var="CURATION" value="<%=Publication.Type.CURATION %>"/>

<zfin2:dataManager zdbID="${figure.zdbID}"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${figure.publication.shortAuthorList} - ${figure.label}"/>
    </tiles:insertTemplate>
</div>

<zfin-figure:publicationInfo publication="${figure.publication}"
                             submitters="${submitters}"
                             showThisseInSituLink="${showThisseInSituLink}"
                             showErrataAndNotes="${showErrataAndNotes}"/>

<c:if test="${fn:length(figure.publication.figures) > 1}">
    <div style="margin-top: 1em;">
        <c:set var="probeUrlPart" value=""/>
        <c:if test="${!empty probe}">
            <c:set var="probeUrlPart" value="?probeZdbID=${probe.zdbID}"/>
        </c:if>

    <c:if test="${figure.publication.type == CURATION}">
        <c:if test="${!empty probe}">
        <a class="additional-figures-link" href="/action/figure/all-figure-view/${figure.publication.zdbID}${probeUrlPart}">ADDITIONAL FIGURES</a>
        </c:if>
        </c:if>
    <c:if test="${figure.publication.type != CURATION}">
        <a class="additional-figures-link" href="/action/figure/all-figure-view/${figure.publication.zdbID}${probeUrlPart}">ADDITIONAL FIGURES</a>
        </c:if>
    </div>
</c:if>

<zfin-figure:expressionSummary summary="${expressionSummary}"/>

<zfin-figure:phenotypeSummary summary="${phenotypeSummary}"/>

<zfin-figure:imagesAndCaption figure="${figure}" showMultipleMediumSizedImages="${showMultipleMediumSizedImages}"/>

<zfin-figure:expressionTable expressionTableRows="${expressionTableRows}" showQualifierColumn="${showExpressionQualifierColumn}"/>

<zfin-figure:antibodyTable antibodyTableRows="${antibodyTableRows}" showQualifierColumn="${showAntibodyQualifierColumn}"/>

<zfin-figure:phenotypeTable phenotypeTableRows="${phenotypeTableRows}"/>

<zfin-figure:constructLinks figure="${figure}"/>


<c:choose>
    <c:when test="${figure.publication.canShowImages && figure.publication.type != UNPUBLISHED}">
        <zfin2:acknowledgment publication="${figure.publication}" showElsevierMessage="${showElsevierMessage}" hasAcknowledgment="${hasAcknowledgment}"/>
    </c:when>
    <c:otherwise>
        <zfin2:subsection>
            <zfin-figure:journalAbbrev publication="${figure.publication}"/>
        </zfin2:subsection>
    </c:otherwise>
</c:choose>

<script>
    jQuery(document).ready(function() {
        jQuery('.fish-label').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
    });
</script>
