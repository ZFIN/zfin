<%@ page import="org.zfin.fish.presentation.SortBy" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishSearchFormBean" scope="request"/>


    <zfin-fish:fishSearchForm formBean="${formBean}"/>

    <c:choose>
        <c:when test="${formBean.totalRecords > 0}">
            <zfin-fish:fishSearchResultTable formBean="${formBean}"/>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; font-weight: bold; margin-bottom: 1em;">
                No matching fish were found
            </div>
        </c:otherwise>
    </c:choose>



<script language="JavaScript">

    var sortBy = document.getElementById("sort-by").value;
    jQuery('#sort-by-pulldown option[value="' + sortBy + '"]').attr('selected', 'selected');


</script>