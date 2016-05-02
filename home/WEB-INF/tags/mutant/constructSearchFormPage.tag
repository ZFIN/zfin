<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="formBean" type="org.zfin.mutant.presentation.ConstructSearchFormBean" required="true" %>

<div class="search-form-top-bar">
    <span class="search-form-title">
        Search for Constructs
    </span>
    <a href="/ZFIN/misc_html/constructs_search_tips.html" class="popup-link help-popup-link"></a>


    <authz:authorize access="hasRole('root')">
    <span style="font-style: italic;">
        Last Updated:  ${zfn:getTimeDurationToday(formBean.summary.releaseDate)} ago
        (<fmt:formatDate value="${formBean.summary.releaseDate}" type="date"/>
        <fmt:formatDate value="${formBean.summary.releaseDate}" pattern="HH:mm:ss"/>)

    </span>
    </authz:authorize>
    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Construct search"/>
        </tiles:insertTemplate>
    </div>

</div>

<c:choose>
    <c:when test="${!zdbFlag.systemUpdateDisabled}">
        <zfin-mutant:constructSearchForm formBean="${formBean}"/>
    </c:when>
    <c:otherwise>
        <span class="error">
            <p/>
            <div align="left" style="font-weight: bold;">System Update:</div>
            The construct mart is currently being re-built and does not allow any construct searches.
    <authz:authorize access="hasRole('root')">
        <br/>
        Update started ${zfn:getTimeDurationToday(zdbFlag.dateLastModified)} ago
    </authz:authorize>
        </span>
    </c:otherwise>
</c:choose>
