<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>

<script src="/javascript/editMarker.js"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>
<script src="/javascript/curator-notes.directive.js"></script>
<script src="/javascript/public-note.directive.js"></script>
<script src="/javascript/marker.service.js"></script>

<script>
    $(function () {
        $('#transgenics').tableCollapse({label: "transgenics"});
        $('#transgenic-lines').tableCollapse({label: "transgenic lines"});
    });
</script>

<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl" ng-init="init('${gene.name}','${gene.abbreviation}')">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}
</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>

<%--Currently, not possible to merge these (not provided as an option on the merge page--%>
<%--mergeURL="${deleteURL}"--%>
<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   deleteURL="${deleteURL}"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

    <zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}" showEditControls="false"/>

    <%--Construct Features--%>
    <%--&lt;%&ndash;SEGMENT (CLONE AND PROBE) RELATIONSHIPS&ndash;%&gt;--%>
    <c:if test="${!empty formBean.marker.figures}">
        <%--<div style="float: right">--%>
        <div class="summary">
            <c:forEach var="fig" items="${formBean.marker.figures}">

                <c:forEach var="img" items="${fig.images}">
                    <a href="/${img.zdbID}"><img src="/imageLoadUp/${img.imageFilename}" width="300" height="200"></a>
                    <%--<zfin:link entity="${img}"/>--%>
                </c:forEach>
            </c:forEach>
        </div>
    </c:if>
    <zfin2:constructFeatures relationships="${formBean.markerRelationshipPresentationList}"
                             marker="${formBean.marker}"
                             title="CONSTRUCT COMPONENTS"/>

    <%--Transgenics that utilize the construct--%>
    <%--link to the facet search result if there are more than 50 features --%>
    <c:choose>
        <c:when test="${formBean.transgenics != null && fn:length(formBean.transgenics) > 50 }">
            <zfin2:subsection title="TRANSGENICS THAT UTILIZE <i>${formBean.marker.name}</i>">
                <table class="summary horizontal-solidblock">
                    <tr>
                        <td>
                            <a href="/prototype?q=&fq=category:%22Mutation+/+Tg%22&fq=xref:${formBean.marker.zdbID}">View
                                all transgenics that utilize <i>${formBean.marker.name}</i></a>
                        </td>
                    </tr>
                </table>
            </zfin2:subsection>
        </c:when>
        <c:otherwise>
            <div id="transgenics" class="summary">
                <zfin2:subsection title="TRANSGENICS THAT UTILIZE <i>${formBean.marker.name}</i>"
                                  test="${!empty formBean.transgenics}" showNoData="true">
                    <table id="features-table" class="summary rowstripes">
                        <tr>
                            <th width="25%">
                                Genomic Feature
                            </th>
                            <th width="25%">
                                Affected Genes
                            </th>
                            <th width="25%">
                                &nbsp;
                            </th>
                            <th width="25%">
                                &nbsp;
                            </th>
                        </tr>

                        <c:forEach var="feature" items="${formBean.transgenics}" varStatus="loop">
                            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                                <td>
                                    <zfin:link entity="${feature}"/>
                                </td>
                                <td>
                                    <zfin:link entity="${feature.affectedGenes}"/>
                                </td>
                                <td>
                                    &nbsp;
                                </td>
                                <td>
                                    &nbsp;
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </zfin2:subsection>
            </div>
        </c:otherwise>
    </c:choose>

    <div id="transgenic-lines" class="summary">
        <zfin2:subsection title="TRANSGENIC LINES" showNoData="true"
                          test="${!empty formBean.fish}">
            <table class="summary rowstripes">
                <tr>
                    <th>Fish</th>
                    <th>Affected Genes</th>
                    <th>Phenotype</th>
                    <th>Gene Expression</th>
                </tr>
                <c:forEach var="fishGenotypeStatistics" items="${formBean.fish}" varStatus="index">
                    <zfin:alternating-tr loopName="index">
                        <td>
                            <zfin:link entity="${fishGenotypeStatistics.fish}"/>
                        </td>
                        <td>
                            <c:forEach var="marker" items="${fishGenotypeStatistics.affectedMarkers}" varStatus="loop">
                                <zfin:link entity="${marker}"/><c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td>
                            <zfin2:showFigureData
                                    fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypePhenotypeStatistics}"
                                    link="/action/fish/phenotype-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                        </td>
                        <td>
                            <zfin2:showFigureData
                                    fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypeExpressionStatistics}"
                                    link="/action/expression/fish-expression-figure-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </zfin2:subsection>
    </div>

    <%--SEQUENCE INFORMATION--%>
    <zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}"
                                            title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>

    <%--OTHER PAGES--%>
    <zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

    <%--CITATIONS--%>
    <zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>
