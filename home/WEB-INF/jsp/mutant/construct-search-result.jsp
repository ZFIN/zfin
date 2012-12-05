<%@ page import="org.zfin.fish.presentation.SortBy" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.presentation.ConstructSearchFormBean" scope="request"/>


    <zfin-mutant:constructSearchFormPage formBean="${formBean}"/>

    <c:choose>
        <c:when test="${formBean.totalRecords > 0}">
            <zfin-mutant:constructSearchResultTable formBean="${formBean}"/>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; font-weight: bold; margin-bottom: 1em;">
                No matching constructs were found
            </div>
        </c:otherwise>
    </c:choose>



