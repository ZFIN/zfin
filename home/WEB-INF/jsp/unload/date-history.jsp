<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<b>History for ${formBean.date}:</b>

<p/>
<a href="/action/unload/summary">Back to Index Summary</a>


<style type="text/css">
    h3 {
        font-variant: small-caps;
    }
</style>

<zfin2:show-hide-all showName="Show All Modified Records" hideName="Hide All Modified Records"
                     classNamePatternShow="showNameUpdate" classNamePatternHide="hideAllModified"/>

<table class="summary rowstripes">
    <tr>
        <th class="sectionTitle" width="50">ID</th>
        <th class="sectionTitle" width="180">Table Name</th>
        <th class="sectionTitle" width="240">Entity ID</th>
        <th class="sectionTitle" >Action</th>
    </tr>
    <c:forEach var="entityTrace" items="${dateHistory}" varStatus="index">
        <tr>
            <td>${index.index}</td>
            <td>${entityTrace.tableName}</td>
            <td>${entityTrace.entityId}</td>
            <td>${entityTrace.action}
                <c:if test="${entityTrace.action == 'MODIFY'}">
                                    <span id="showNameUpdate-${index.index}">
                    <a onclick="jQuery('#showNameUpdate-${index.index}').hide();
                            jQuery('#fetchUpdate-${index.index}').show();
                            jQuery('#nameUpdate-${index.index}').show();
                            jQuery('#nameUpdate-${index.index}').load('/action/unload/fetch-changed-entity-records?entityID=${entityTrace.entityId}&tableName=${entityTrace.tableName}&date=${entityTrace.date}');
                            return false;"
                       class="showNameUpdate"></a>
                    </span>
                <span id="fetchUpdate-${index.index}" style="display: none">
                    <a onclick="jQuery('#fetchUpdate-${index.index}').hide();
                            jQuery('#showNameUpdate-${entityTrace.date}').show();
                            jQuery('#nameUpdate-${index.index}').hide();"
                       class="small-new-link hideAllModified"></a>
                    </span>

                    <div id="nameUpdate-${index.index}" style="display: none; font-size: 75%;"></div>

                </c:if>

            </td>
        </tr>
    </c:forEach>

</table>
