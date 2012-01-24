<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.antibody.presentation.AntibodySearchFormBean" required="true" %>

<c:if test="${formBean.totalRecords > 0}">
    <table class="searchresults rowstripes">
        <tr>
            <th>Name</th>
            <th width=10%>Gene</th>
            <th width=25%>Anatomy</th>
            <th width=20%>Cellular Components</th>
            <th width=15%>Stage Range</th>
            <c:if test="${formBean.matchingTextSearch}">
                <th>Matching Text g</th>
            </c:if>
        </tr>
        <c:forEach var="antibodyStat" items="${formBean.antibodyStats}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${antibodyStat.antibody}"/>
                </td>
                <td>
                    <c:forEach var="antigenRel" items="${antibodyStat.sortedAntigenRelationships}"
                               varStatus="relIndex">
                        <zfin:link entity="${antigenRel.firstMarker}"/><c:if test="${!relIndex.last}">, </c:if>
                    </c:forEach>
                </td>
                <td>
                    <zfin2:toggledHyperlinkList collection="${antibodyStat.distinctAnatomyTerms}" maxNumber="3"
                                                id="${antibodyStat.antibody.zdbID}"/>
                </td>
                <td>
                    <c:forEach var="goTerm" items="${antibodyStat.distinctGoTermsWTAndStandard}"
                               varStatus="index">
                        <zfin:link entity="${goTerm}"/>
                        <c:if test="${!index.last}">, </c:if>
                    </c:forEach>
                </td>
                <td>
                    <zfin:link entity="${antibodyStat.earliestStartStage}"/>
                    <c:if test="${antibodyStat.earliestStartStage != antibodyStat.latestEndStage}">
                        to <zfin:link entity="${antibodyStat.latestEndStage}"/>
                    </c:if>
                </td>
                <c:if test="${formBean.matchingTextSearch}">
                    <td valign=top>
                        <zfin2:matching-text matchingTextList="${antibodyStat.matchingText}"/>
                    </td>
                </c:if>
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

            <a href="/ZFIN/misc_html/antibody_search_tips.html" class="popup-link help-popup-link"
                    ></a>
        </td>
    </tr>
</table>

