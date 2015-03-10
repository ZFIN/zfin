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
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:disruptorInfo marker="${disruptor}" markerBean="${formBean}" previousNames="${formBean.previousNames}"/>

<%--// EXPRESSION --%>
<div class="summary">
    <b>GENE EXPRESSION</b>
    <small><a class="popup-link info-popup-link" href="/action/marker/note/expression"></a></small>
    <br/>
    <b>Gene expression in ${formBean.marker.name}</b>
    <div id="expression-short-version" class="summary">
        <c:choose>
            <c:when test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
                <zfin2:expressionData sequenceTargetingReagentID="${disruptor.zdbID}" expressionDisplays="${formBean.expressionDisplays}" showNumberOfRecords="5"
                                      showCondition="false" />
                <c:if test="${formBean.totalNumberOfExpressedGenes > 5}">
                    <div>
                        <a href="javascript:expandExpression()">
                            <img src="/images/darrow.gif" alt="expand" border="0">
                            Show all</a>
                        ${formBean.totalNumberOfExpressedGenes} expressed genes
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>
    <div style="display:none" id="expression-long-version" class="summary">
        <c:if test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
            <zfin2:expressionData sequenceTargetingReagentID="${disruptor.zdbID}" expressionDisplays="${formBean.expressionDisplays}" showNumberOfRecords="${fn:length(formBean.expressionDisplays)}"
                                  showCondition="false" />
        </c:if>
        <div>
            <a href="javascript:collapseExpression()">
                <img src="/images/up.gif" alt="expand" title="Show first 5 expressed genes" border="0">
                Show first</a> 5 expressed genes
        </div>
    </div>
</div>

<%--// PHENOTYPE --%>
<div class="summary">
    <b>PHENOTYPE</b>&nbsp;
    <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a></small>
    <br/>
    <b>Phenotype resulting from ${formBean.marker.name}</b>
    <div id="phenotype-short-version" class="summary">
        <c:choose>
            <c:when test="${formBean.phenotypeDisplays != null && fn:length(formBean.phenotypeDisplays) > 0 }">
                <zfin2:all-phenotype phenotypeDisplays="${formBean.phenotypeDisplays}" showNumberOfRecords="5"
                                     suppressMoDetails="true" secondColumn="fish"/>
                <c:if test="${fn:length(formBean.phenotypeDisplays) > 5}">
                    <div>
                        <a href="javascript:expandPhenotype()">
                            <img src="/images/darrow.gif" alt="expand" border="0">
                            Show all</a>
                        ${fn:length(formBean.phenotypeDisplays)} phenotypes
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>
    <div style="display:none" id="phenotype-long-version" class="summary">
        <c:if test="${formBean.phenotypeDisplays != null && fn:length(formBean.phenotypeDisplays) > 0 }">
            <zfin2:all-phenotype phenotypeDisplays="${formBean.phenotypeDisplays}" showNumberOfRecords="${fn:length(formBean.phenotypeDisplays)}"
                                 suppressMoDetails="true" secondColumn="fish"/>
        </c:if>
        <div>
            <a href="javascript:collapsePhenotype()">
                <img src="/images/up.gif" alt="expand" title="Show first 5 phenotypes" border="0">
                Show first</a> 5 phenotypes
        </div>
    </div>
</div>

<%--// GENOTYPE CREATED BY TALEN OR CRISPR --%>
<c:if test="${formBean.marker.markerType.name eq 'TALEN' || formBean.marker.markerType.name eq 'CRISPR'}">
    <div id="genotype-short-version" class="summary">
        <c:choose>
            <c:when test="${formBean.genotypeData != null && fn:length(formBean.genotypeData) > 0 }">
                <zfin2:genotype-information genotypes="${formBean.genotypeData}" sequenceTargetReagen="${formBean.marker.name}" showNumberOfRecords="5" />
                <c:if test="${fn:length(formBean.genotypeData) > 5}">
                    <div>
                        <a href="javascript:expandGenotype()">
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
    <div style="display:none" id="genotype-long-version" class="summary">
        <c:if test="${formBean.genotypeData != null && fn:length(formBean.genotypeData) > 0 }">
            <zfin2:genotype-information genotypes="${formBean.genotypeData}" sequenceTargetReagen="${formBean.marker.name}" showNumberOfRecords="${fn:length(formBean.genotypeData)}"/>
        </c:if>
        <div>
            <a href="javascript:collapseGenotype()">
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
    function expandGenotype() {
        document.getElementById('genotype-short-version').style.display = 'none';
        document.getElementById('genotype-long-version').style.display = 'block';
    }

    function collapseGenotype() {
        document.getElementById('genotype-short-version').style.display = 'block';
        document.getElementById('genotype-long-version').style.display = 'none';
    }

    function expandPhenotype() {
        document.getElementById('phenotype-short-version').style.display = 'none';
        document.getElementById('phenotype-long-version').style.display = 'block';
    }

    function collapsePhenotype() {
        document.getElementById('phenotype-short-version').style.display = 'block';
        document.getElementById('phenotype-long-version').style.display = 'none';
    }

    function expandExpression() {
        document.getElementById('expression-short-version').style.display = 'none';
        document.getElementById('expression-long-version').style.display = 'block';
    }

    function collapseExpression() {
        document.getElementById('expression-short-version').style.display = 'block';
        document.getElementById('expression-long-version').style.display = 'none';
    }
</script>