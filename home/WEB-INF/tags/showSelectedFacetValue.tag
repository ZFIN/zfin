<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="value" type="org.zfin.search.presentation.FacetValue"%>

<li class="facet-value selected-facet-value row">
    <div class="col-md-2 col-xs-3 tight-on-the-right">
        <div class="pull-right">
            <i class="fa fa-check-square facet-selected"></i>
        </div>
    </div>
    <div class="col-md-10 col-xs-9 tight-on-the-left">
        <a class="facet-link" href="${value.url}">
            ${value.label}
        </a>
    </div>
</li>

