<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishSearchFormBean" scope="request"/>

<z:page>
    <zfin-fish:fishSearchFormPage formBean="${formBean}"/>

    <c:choose>
        <c:when test="${formBean.totalRecords > 0}">
            <zfin-fish:fishSearchResultTable formBean="${formBean}"/>
        </c:when>
        <c:otherwise>
            <div class="no-results-found-message" style="margin-bottom: 1em;">
                No matching fish were found.
            </div>
        </c:otherwise>
    </c:choose>

    <script language="JavaScript" type="text/javascript">

        var sortBy = document.getElementById("sort-by").value;
        jQuery('#sort-by-pulldown option[value="' + sortBy + '"]').attr('selected', 'selected');

        jQuery('#fish-form-loading-notify').hide();

    </script>
</z:page>