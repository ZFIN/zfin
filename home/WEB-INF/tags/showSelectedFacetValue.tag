<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="value" type="org.zfin.search.presentation.FacetValue"%>

<li style="min-height:10px; padding-left: 16px; " class="facet-value row-fluid">
        <a class="breadbox-link" href="${value.url}">
<%--            <img class="checkbox-icon" src="/images/icon-checked.png">--%>
            <i class="fa fa-check-square"></i> ${value.label}
        </a>
</li>

