<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>

<%--Currently, not possible to merge these (not provided as an option on the merge page--%>
<%--mergeURL="${deleteURL}"--%>
<zfin2:dataManager zdbID="${formBean.marker.zdbID}"

                   deleteURL="${deleteURL}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}"/>

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
        title="CONSTRUCT COMPONENTS" />

<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />

<zfin2:subsection title="TRANSGENIC LINES"
                        test="${!empty formBean.transgenicLineLinks}" showNoData="true">
    <table class="summary horizontal-solidblock">
        <tr>
            <td>
                <a href="/action/fish/do-search?geneOrFeatureName=${formBean.marker.name}">View all lines that utilize <i>${formBean.marker.name}</i></a>
                <%--<zfin2:toggledHyperlinkStrings collection="${formBean.transgenicLineLinks}" maxNumber="5" suffix="<br>"/>--%>
            </td>
        </tr>
    </table>
</zfin2:subsection>
<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>



<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

