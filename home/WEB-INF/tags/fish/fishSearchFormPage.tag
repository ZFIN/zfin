<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="formBean" type="org.zfin.fish.presentation.FishSearchFormBean" required="true" %>

<style type="text/css">
    .search-form-top-bar {
        border: 1px solid #ddd;
        background: #efefef;
        border-radius: 8px;
        padding: 3px;
        padding-bottom: 7px;
    }

    .search-form-bottom-bar {
        border: 1px solid #ddd;
        background: #efefef;
        border-radius: 8px;
        padding: 3px;
        margin-bottom: 1em;
    }

    .search-form-title {
        font-size: large;
        margin-left: 2px;
        color: #666;
    }

    .search-form-your-input-welcome {
        float: right;
    }

    .search-form-alternate-link {
        font-weight: bold;
        font-size: small;
        color: #888;
    }

    .search-form-alternate-link a {
        opacity: .6;
    }


</style>

<div class="search-form-top-bar">
    <span class="search-form-title">
        Search for Mutants / Knockdowns / Tg
    </span>
    <a href="/ZFIN/misc_html/fish_search_tips.html" class="popup-link help-popup-link"></a>

    <authz:authorize ifAnyGranted="root">
    <span style="font-style: italic;">
        Last Updated:  ${zfn:getTimeDurationToday(formBean.summary.releaseDate)} ago
        (<fmt:formatDate value="${formBean.summary.releaseDate}" type="date"/>
        <fmt:formatDate value="${formBean.summary.releaseDate}" pattern="HH:mm:ss"/>)

    </span>
    </authz:authorize>
    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Fish search"/>
            <tiles:putAttribute name="subjectID" value=""/>
        </tiles:insertTemplate>
    </div>

</div>

<c:choose>
    <c:when test="${!zdbFlag.systemUpdateDisabled}">
        <zfin-fish:fishSearchForm formBean="${formBean}"/>
    </c:when>
    <c:otherwise>
        <span class="error">
            <p/>
            <div align="left" style="font-weight: bold;">System Update:</div>
            The fish mart is currently being re-built and does not allow any mutant searches.
    <authz:authorize ifAnyGranted="root">
        <br/>
        Update started ${zfn:getTimeDurationToday(zdbFlag.dateLastModified)} ago
    </authz:authorize>
        </span>
    </c:otherwise>
</c:choose>