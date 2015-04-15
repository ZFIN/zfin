<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/gbrowse-image.js"></script>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${mergeURL}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:sequenceTargetingReagentInfo marker="${sequenceTargetingReagent}" markerBean="${formBean}" previousNames="${formBean.previousNames}"/>

<div id="gbrowse-images" class="summary">
    <div class="summaryTitle">
        TARGET LOCATION${fn:length(formBean.gbrowseImages) == 1 ? "" : "S"}
        <small><a class="popup-link info-popup-link" href="/action/marker/note/sequence-targeting-reagent-gbrowse"></a></small>
    </div>

    <c:forEach items="${formBean.gbrowseImages}" var="image" end="1">
        <div class="gbrowse-image"
             data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}"}'>
        </div>
    </c:forEach>

    <c:if test="${fn:length(formBean.gbrowseImages) > 2}">
        <div>
            <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View all ${fn:length(formBean.gbrowseImages)} target locations</a>
        </div>
    </c:if>

    <span id="gbrowse-no-data" class="no-data-tag">No data available</span>
</div>

<script>
    jQuery(".gbrowse-image").gbrowseImage({
        success: function() {
            jQuery("#gbrowse-no-data").hide();
        }
    });
</script>

<%--// EXPRESSION --%>
<div class="summary" id="expression">
    <b>GENE EXPRESSION</b>
    <small><a class="popup-link info-popup-link" href="/action/marker/note/expression"></a></small>
    <br/>
    <b>Gene expression in ${formBean.marker.name}</b>
    <c:choose>
        <c:when test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
            <zfin2:expressionData sequenceTargetingReagentID="${sequenceTargetingReagent.zdbID}" expressionDisplays="${formBean.expressionDisplays}" showCondition="false" />
        </c:when>
        <c:otherwise>
            <span class="no-data-tag">No data available</span>
        </c:otherwise>
    </c:choose>
</div>

<%--// PHENOTYPE --%>
<div class="summary" id="phenotype">
    <b>PHENOTYPE</b>&nbsp;
    <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a></small>
    <br/>
    <b>Phenotype resulting from ${formBean.marker.name}</b>
    <c:choose>
        <c:when test="${formBean.phenotypeDisplays != null && fn:length(formBean.phenotypeDisplays) > 0 }">
            <zfin2:all-phenotype phenotypeDisplays="${formBean.phenotypeDisplays}" suppressMoDetails="true" secondColumn="fish"/>
        </c:when>
        <c:otherwise>
            <span class="no-data-tag">No data available</span>
        </c:otherwise>
    </c:choose>
</div>

<%--// GENOTYPE CREATED BY TALEN OR CRISPR --%>
<c:if test="${formBean.marker.markerType.name eq 'TALEN' || formBean.marker.markerType.name eq 'CRISPR'}">
    <div id="genotype" class="summary">
        <c:choose>
            <c:when test="${formBean.genotypeData != null && fn:length(formBean.genotypeData) > 0 }">
                <zfin2:genotype-information genotypes="${formBean.genotypeData}" sequenceTargetReagen="${formBean.marker.name}" />
            </c:when>
            <c:otherwise>
                <span><strong>GENOTYPES CREATED WITH ${formBean.marker.name}</strong></span> <span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>
</c:if>

<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<script src="/javascript/table-collapse.js"></script>
<script>
    jQuery(function () {
        jQuery('#expression').tableCollapse({label: 'expressed genes'});
        jQuery('#phenotype').tableCollapse({label: 'phenotypes'});
        jQuery('#genotype').tableCollapse({label: 'genotypes'});
    });
</script>