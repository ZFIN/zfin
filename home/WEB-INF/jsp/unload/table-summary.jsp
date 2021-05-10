<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<b>Summary for Table ${tableName}:</b>

<p/>
<a href="/action/unload/summary">Back to Index Summary</a>

<table class="summary rowstripes">
    <tr>
        <th class="sectionTitle">Start Date</th>
        <th class="sectionTitle">End Date</th>
        <th class="sectionTitle">Indexed Tables</th>
    </tr>
    <tr>
        <td class="listContent">
            <c:out value='${formBean.unloadService.startDate}'/>
        </td>
        <td class="listContent">
            <c:out value='${formBean.unloadService.endDate}'/>
        </td>
        <td class="listContent">
            <%--
                            <c:forEach var="table" items="${formBean.unloadService.allIndexedTables}">
                                ${table}<br/>
                            </c:forEach>
            --%>
        </td>
    </tr>
</table>

<p/>

<form:form method="Get" action="/action/unload/entity-history" modelAttribute="formBean" name="Entity History"
           id="Entity History" onsubmit="return false;">
    Find History for <form:input path="entityID" size="40"/>
    <input value="Search" onclick="document.getElementById('Entity History').submit();" type="button">
    <form:hidden path="tableName"/>
</form:form>

<style type="text/css">
    h3 {
        font-variant: small-caps;
    }
</style>

<h3>
    <span id="deleted-entity-section-closed" onclick="jQuery('#deleted-entities-section').show();
                                                      jQuery('#deleted-entity-section-closed').hide();
                                                        jQuery('#deleted-entity-section-open').show();">+ Deleted Entities Report: </span>
    <span id="deleted-entity-section-open" style="display: none;" onclick="jQuery('#deleted-entities-section').hide();
                                                      jQuery('#deleted-entity-section-closed').show();
                                                        jQuery('#deleted-entity-section-open').hide();">- Deleted Entities Report: </span>

                            <span id="showAllDeletedLink" style="float: right; font-size:small; font-weight:normal;">
    <a href="javascript:showAllDeleted();">All Record Details</a>
</span>
<span id="hideAllDeletedLink" style="float: right; display: none;font-size:small; font-weight:normal;">
    <a href="javascript:hideAllDeleted();">Hide Record Details</a>
</span>
</h3>

<div id="deleted-entities-section" style="display: none;">
    <table class="summary rowstripes">
        <tr>
            <th class="sectionTitle" width="150">Last Date before Deletion</th>
            <th class="sectionTitle" width="150">First Date after Deletion</th>
            <th class="sectionTitle">Deleted Entities</th>
        </tr>
        <c:forEach var="map" items="${formBean.deletedEntityMap}" varStatus="index">
            <tr>
                <td>
                        ${map.key}<br/>
                </td>
                <td>
                    <span id="firstDateAfterDeletion-${index.index}"></span>
                </td>
                <td>
                    <c:forEach var="entity" items="${map.value}">
                        ${entity.entityId}
                        <script>
                            jQuery('#firstDateAfterDeletion-${index.index}').text('${entity.firstDateAfterDisappearance}');
                        </script>
                <span id="showNameDeleted-${entity.entityId}">
                    (<a onclick="jQuery('#showNameDeleted-${entity.entityId}').hide();
                        jQuery('#fetchDeleted-${entity.entityId}').show();
                        jQuery('#nameDeleted-${entity.entityId}').show();
                        jQuery('#nameDeleted-${entity.entityId}').load('/action/unload/fetch-entity-record?entityID=${entity.entityId}&tableName=${tableName}&date=${map.key}');
                        return false;"
                        class="small-new-link showAllDeleted">Show Record</a>)
                    </span>
                <span id="fetchDeleted-${entity.entityId}" style="display: none">
                    (<a onclick="jQuery('#fetchDeleted-${entity.entityId}').hide();
                        jQuery('#showNameDeleted-${entity.entityId}').show();
                        jQuery('#nameDeleted-${entity.entityId}').hide();"
                        class="small-new-link hideAllDeleted">Hide Record</a>)
                    </span>

                        <div id="nameDeleted-${entity.entityId}"
                             style="display: none; font-size: 75%; text-indent: 5pt;"></div>
                        <br>
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>

<h3>
    <span id="added-entity-section-closed" onclick="jQuery('#added-entities-section').show();
                                                      jQuery('#added-entity-section-closed').hide();
                                                        jQuery('#added-entity-section-open').show();">+ Added Entities Report: </span>
    <span id="added-entity-section-open" style="display: none;" onclick="jQuery('#added-entities-section').hide();
                                                      jQuery('#added-entity-section-closed').show();
                                                        jQuery('#added-entity-section-open').hide();">- Added Entities Report: </span>

                            <span id="showAllAddedLink" style="float: right; font-size:small; font-weight:normal;">
    <a href="javascript:showAllAdded();">All Record Details</a>
</span>
<span id="hideAllAddedLink" style="float: right; display: none;font-size:small; font-weight:normal;">
    <a href="javascript:hideAllAdded();">Hide Record Details</a>
</span>
</h3>

<div id="added-entities-section" style="display: none;">
    <table class="summary rowstripes">
        <tr>
            <th class="sectionTitle" width="150">Date</th>
            <th class="sectionTitle">Added Entities</th>
        </tr>
        <c:forEach var="map" items="${formBean.addedEntityMap}" varStatus="index">
            <tr>
                <td>
                        ${map.key}<br/>
                </td>
                <td>
                    <c:forEach var="entity" items="${map.value}">
                        ${entity.entityId}
                <span id="showName-${entity.entityId}">
                    (<a onclick="jQuery('#showName-${entity.entityId}').hide();
                        jQuery('#fetch-${entity.entityId}').show();
                        jQuery('#name-${entity.entityId}').show();
                        jQuery('#name-${entity.entityId}').load('/action/unload/fetch-entity-record?entityID=${entity.entityId}&tableName=${tableName}&date=${map.key}');
                        return false;"
                        class="small-new-link showAllAdded">Show Record</a>)
                    </span>
                <span id="fetch-${entity.entityId}" style="display: none">
                    (<a onclick="jQuery('#fetch-${entity.entityId}').hide();
                        jQuery('#showName-${entity.entityId}').show();
                        jQuery('#name-${entity.entityId}').hide();"
                        class="small-new-link hideAllAdded">Hide Record</a>)
                    </span>

                        <div id="name-${entity.entityId}"
                             style="display: none; font-size: 75%; text-indent: 5pt;"></div>
                        <br>
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>

<h3>
    <span id="modified-entity-section-closed" onclick="jQuery('#modified-entities-section').show();
                                                      jQuery('#modified-entity-section-closed').hide();
                                                        jQuery('#modified-entity-section-open').show();">+ Modified Entities Report: </span>
    <span id="modified-entity-section-open" style="display: none;" onclick="jQuery('#modified-entities-section').hide();
                                                      jQuery('#modified-entity-section-closed').show();
                                                        jQuery('#modified-entity-section-open').hide();">- Modified Entities Report: </span>
                            <span id="showAllModifiedLink" style="float: right; font-size:small; font-weight:normal;">
    <a href="javascript:showAllModified();">All Record Details</a>
</span>
<span id="hideAllModifiedLink" style="float: right; display: none;font-size:small; font-weight:normal;">
    <a href="javascript:hideAllModified();">Hide Record Details</a>
</span>
</h3>

<div id="modified-entities-section" style="display: none; text-indent: 20px">
    <table class="summary rowstripes">
        <tr>
            <th class="sectionTitle" width="150">Date</th>
            <th class="sectionTitle">Modified Entities</th>
        </tr>
        <c:forEach var="map" items="${formBean.modifiedEntityMap}" varStatus="index">
            <tr>
                <td>
                        ${map.key}<br/>
                </td>
                <td>
                    <c:forEach var="entity" items="${map.value}">
                        ${entity.entityId}
                <span id="showNameUpdate-${entity.entityId}">
                    (<a onclick="jQuery('#showNameUpdate-${entity.entityId}').hide();
                        jQuery('#fetchUpdate-${entity.entityId}').show();
                        jQuery('#nameUpdate-${entity.entityId}').show();
                        jQuery('#nameUpdate-${entity.entityId}').load('/action/unload/fetch-changed-entity-records?entityID=${entity.entityId}&tableName=${tableName}&date=${map.key}');
                        return false;"
                        class="small-new-link showAllModified">Show Record</a>)
                    </span>
                <span id="fetchUpdate-${entity.entityId}" style="display: none">
                    (<a onclick="jQuery('#fetchUpdate-${entity.entityId}').hide();
                        jQuery('#showNameUpdate-${entity.entityId}').show();
                        jQuery('#nameUpdate-${entity.entityId}').hide();"
                        class="small-new-link hideAllModified">Hide Record</a>)
                    </span>

                        <div id="nameUpdate-${entity.entityId}" style="display: none; font-size: 75%;"></div>
                        <br>
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>

<p/>


<table class="summary rowstripes">
    <tr>
        <th class="sectionTitle" width="150">Date</th>
        <th class="sectionTitle" width="250">Number Of Records</th>
        <th class="sectionTitle">Delta</th>
    </tr>
    <c:forEach var="histogramItem" items="${formBean.tableHistogram}">
        <tr>
            <td class="listContent">
                <c:out value='${histogramItem.date}'/>
            </td>
            <td class="listContent">
                <fmt:formatNumber type="number" pattern="###,###" value="${histogramItem.numberOfRecords}"/>
            </td>
            <td class="listContent">
                <fmt:formatNumber type="number" pattern="###,###" value="${histogramItem.delta}" />
            </td>
        </tr>
    </c:forEach>
</table>


<script type="text/javascript">

    function showAllDeleted() {
        jQuery('.showAllDeleted').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllDeletedLink').hide();
        jQuery('#hideAllDeletedLink').show();
    }
    function hideAllDeleted() {
        jQuery('.hideAllDeleted').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllDeletedLink').show();
        jQuery('#hideAllDeletedLink').hide();
    }

    function showAllAdded() {
        jQuery('.showAllAdded').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllAddedLink').hide();
        jQuery('#hideAllAddedLink').show();
    }
    function hideAllAdded() {
        jQuery('.hideAllModified').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllAddedLink').show();
        jQuery('#hideAllAddedLink').hide();
    }

    function showAllModified() {
        jQuery('.showAllModified').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllModifiedLink').hide();
        jQuery('#hideAllModifiedLink').show();
    }
    function hideAllModified() {
        jQuery('.hideAllModified').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllModifiedLink').show();
        jQuery('#hideAllModifiedLink').hide();
    }


</script>
