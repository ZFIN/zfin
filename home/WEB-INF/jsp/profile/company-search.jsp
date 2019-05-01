<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:listAllFromOrganization/>

<zfin2:companySearchForm searchBean="${formBean}"/>

<c:if test="${formBean.totalRecords == 0 && !(orgs == null)}">
    <div class="no-results-found-message">
        No results were found matching your query.
    </div>
</c:if>

<c:if test="${formBean.totalRecords>0}">

    <%--<div style="float: left; margin-top: 2px; width: auto !important;" class="pagination">--%>
        <%--<zfin2:recordsView view="html" searchBean="${formBean}" type="company"/>--%>
        <%--<zfin2:recordsView view="printable" searchBean="${formBean}" type="company"/>--%>
    <%--</div>--%>


    <div style="float: right; margin-top: 2px; width: auto !important;" class="zf-pagination ">
        <b>${formBean.totalRecords}&nbsp;${formBean.totalRecords == 1 ? 'company' : 'companies'} found </b> &nbsp;
        <zfin2:maxRecords count="10" searchBean="${formBean}" searchType="company"/>
        <zfin2:maxRecords count="25" searchBean="${formBean}" searchType="company"/>
        <zfin2:maxRecords count="50" searchBean="${formBean}" searchType="company"/>
        <zfin2:maxRecords count="200" searchBean="${formBean}" searchType="company"/>
    </div>

    <div style="display: inline;">
        <zfin2:pagination paginationBean="${formBean}"/>
    </div>

    <zfin2:organizationList type="${type}" organizations="${orgs}"/>

    <div style="display: inline;">
        <zfin2:pagination paginationBean="${formBean}"/>
    </div>

</c:if>
