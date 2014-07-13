<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.DisruptorBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${mergeURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:disruptorInfo marker="${disruptor}" markerBean="${formBean}" previousNames="${formBean.previousNames}"/>

<%--// PHENOTYPE --%>
<zfin2:phenotype phenotypeOnMarkerBean="${formBean.phenotypeOnMarkerBeans}" marker="${formBean.marker}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--// GENOTYPE CREATED BY TALEN OR CRISPR --%>
<c:if test="${formBean.marker.markerType.name eq 'TALEN' || formBean.marker.markerType.name eq 'CRISPR'}">
    <div id="short-version" class="summary">
    <c:choose>
        <c:when test="${formBean.genotypeData != null && fn:length(formBean.genotypeData) > 0 }">
            <zfin2:genotype-information genotypes="${formBean.genotypeData}" sequenceTargetReagen="${formBean.marker.name}" showNumberOfRecords="5" />
            <c:if test="${fn:length(formBean.genotypeData) > 5}">
                <div>
                    <a href="javascript:expand()">
                        <img src="/images/darrow.gif" alt="expand" border="0">
                        Show all</a>
                        ${fn:length(formBean.genotypeData)} genotypes
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <span><strong>GENOTYPES CREATED WITH ${formBean.marker.name}</strong></span> <span class="no-data-tag">No data available</span>
        </c:otherwise>
    </c:choose>
    </div>
    <div style="display:none" id="long-version" class="summary">
        <c:if test="${formBean.genotypeData != null && fn:length(formBean.genotypeData) > 0 }">
            <zfin2:genotype-information genotypes="${formBean.genotypeData}" sequenceTargetReagen="${formBean.marker.name}" showNumberOfRecords="${fn:length(formBean.genotypeData)}"/>
        </c:if>
        <div>
            <a href="javascript:collapse()">
                <img src="/images/up.gif" alt="expand" title="Show first 5 genotypes" border="0">
                Show first</a> 5 genotypes
        </div>
    </div>
</c:if>

<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<script type="text/javascript">
    function expand() {
        document.getElementById('short-version').style.display = 'none';
        document.getElementById('long-version').style.display = 'block';
    }

    function collapse() {
        document.getElementById('short-version').style.display = 'block';
        document.getElementById('long-version').style.display = 'none';
    }
</script>