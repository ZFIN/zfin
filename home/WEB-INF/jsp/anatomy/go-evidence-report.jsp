<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="6" align="right"><a href="#modify-search">Modify Search</a></td>
    </tr>
    <tr>
        <td>
            <div align="center">
                <c:choose>
                    <c:when test="${formBean.totalRecords == 0}">
                        <b>No Go Evidence records were found matching your query.</b><br><br>
                    </c:when>
                    <c:otherwise>
                        <b>
                            <zfin:choice choicePattern="0#Go Evidences| 1#Go Evidence| 2#Go Evidences"
                                         integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                        </b>
                    </c:otherwise>
                </c:choose>
            </div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
</table>

<c:if test="${formBean.totalRecords > 0}">
    <table class="searchresults rowstripes">
        <tr>
            <th width=20%>Gene</th>
            <th width=80%>Term</th>
        </tr>
        <c:forEach var="goEvidence" items="${formBean.allGoEvidences}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${goEvidence.marker}"/>
                </td>
                <td>
                    <zfin:link entity="${goEvidence.goTerm}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

    <input name="page" type="hidden" value="1" id="page"/>
    <zfin2:pagination paginationBean="${formBean}"/>
</c:if>

<p></p>
<table width="100%">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        <a name="modify-search">Modify your search </a>
            </span>
            &nbsp;&nbsp; <a href="javascript:start_tips();">Search Tips</a>
        </td>
    </tr>
</table>

<table width="100%" class="error-box">
    <tr>
        <td>
            <form:errors path="*" cssClass="Error"/>
        </td>
    </tr>
</table>

<zfin-ontology:go-evidence-report-form formBean="${formBean}" />