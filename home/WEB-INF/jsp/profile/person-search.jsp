<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:listAllFromOrganization/>

    <zfin2:personSearchForm searchBean="${formBean}"/>

    <c:if test="${formBean.totalRecords == 0 && !(people == null)}">
        <div class="no-results-found-message">
            No results were found matching your query.
        </div>
    </c:if>
    <c:if test="${formBean.totalRecords>0}">

        <%--<div style="float: left; margin-top: 2px; width: auto !important;" class="pagination">--%>
            <%--<zfin2:recordsView view="html" searchBean="${formBean}" type="person"/>--%>
            <%--<zfin2:recordsView view="printable" searchBean="${formBean}" type="person"/>--%>
        <%--</div>--%>



        <div style="float: right; margin-top: 2px; width: auto !important;" class="zf-pagination ">
            <b>${formBean.totalRecords} ${formBean.totalRecords == 1 ? "person" :"people"} found</b> &nbsp;
            <zfin2:maxRecords count="10" searchBean="${formBean}" searchType="person"/>
            <zfin2:maxRecords count="25" searchBean="${formBean}" searchType="person"/>
            <zfin2:maxRecords count="50" searchBean="${formBean}" searchType="person"/>
            <zfin2:maxRecords count="200" searchBean="${formBean}" searchType="person"/>
        </div>

        <div style="display: inline; clear: both;">
            <zfin2:pagination paginationBean="${formBean}"/>
        </div>

        <zfin2:personList people="${people}"/>

        <div style="display: inline; clear: both;">
            <zfin2:pagination paginationBean="${formBean}"/>
        </div>

    </c:if>
</z:page>
