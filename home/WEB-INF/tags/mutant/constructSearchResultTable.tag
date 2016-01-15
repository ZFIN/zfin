<%@ tag import="org.zfin.fish.presentation.SortBy" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.mutant.presentation.ConstructSearchFormBean" required="false" %>


<div style="margin-top: 2em ; margin-bottom: .3em">

<span style="text-align: center; margin-top: 8px; margin-left: 4px;">
    <c:if test="${formBean.totalRecords > 0}">
        <b>
            <fmt:formatNumber value="${formBean.totalRecords}" pattern="##,###"/> Constructs found
        </b>
    </c:if>
</span>

    <p/>

    <div style="float:right ; margin-top: 2px;">
        <select name="maxDisplayRecordsTop" id="max-display-records-top" class="max-results">
            <c:forEach items="${formBean.recordsPerPageList}" var="option">
                <option>${option}</option>
            </c:forEach>
        </select>
        <label for="max-display-records-top">results per page</label>

    </div>

    <zfin2:pagination paginationBean="${formBean}"/>
</div>


<table class="searchresults rowstripes" style="clear: both;">

    <tr>

    <th>Construct</th>
    <th>Expression</th>


    <c:forEach var="construct" items="${formBean.constructList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td class="bold">

                <a href="/${construct.ID}"> <i>${construct.name}</i></a>
            </td>
            <td>
                <c:if test="${construct.expressionFigureCount != 0}">
                    <%-- Case of a single figure --%>
                <c:if test="${construct.expressionFigureCount == 1}">
                    <zfin:link entity="${construct.singleFigure}"/>
                </c:if>
                    <%-- case of multiple figures --%>
                <c:if test="${construct.expressionFigureCount > 1}">
                <a href="/action/mutant/construct-expression-summary?constructID=${construct.ID}&<%= request.getQueryString()%>">
                        <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                                     integerEntity="${construct.expressionFigureCount}"/>
                        <%--</a>--%>
                    </c:if>
                        <zfin2:showCameraIcon hasImage="${construct.imageAvailable}"/>
                    </c:if>
            </td>

        </zfin:alternating-tr>

    </c:forEach>
</table>
<input name="page" type="hidden" value="1" id="page"/>
<div style="float:right ; margin-top: 2px;">
    <select name="maxDisplayRecordsBottom" id="max-display-records-bottom" class="max-results">
        <c:forEach items="${formBean.recordsPerPageList}" var="option">
            <option>${option}</option>
        </c:forEach>
    </select>
    <label for="max-display-records-bottom">results per page</label>

</div>

<zfin2:pagination paginationBean="${formBean}"/>

<script>
    jQuery('.max-results').change(function () {
        var $maxDisplayHidden = jQuery('#max-display-records-hidden');
        $maxDisplayHidden.val(jQuery(this).val());
        $maxDisplayHidden.change();
    });
</script>

