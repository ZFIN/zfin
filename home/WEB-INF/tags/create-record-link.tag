<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="value" type="java.lang.String" required="true" %>
<%@ attribute name="column" type="org.zfin.database.presentation.Column" required="true" %>
<%@ attribute name="identifier" type="java.lang.String" required="true" %>

<c:set var="val" value="${value}"/>

<c:if test="${!(fn:startsWith(value, 'ZDB-')) &&(column.foreignKey)}">
    <c:set var="val" value="${column.foreignKeyRelation.entityTable.pkIdentifier}-${value}"/>
</c:if>

<c:choose>
    <c:when test="${column.foreignKey}">
        <a href="/action/database/view-record/${val}">
            <span id="fetch-${identifier}">${value}</span>
            <span style="display: none" id="name-${identifier}">...</span>
        </a>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${column.primaryKey}">
                <c:choose>
                    <c:when test="${(fn:startsWith(value, 'ZDB-')) && !(fn:contains(value, ','))}">
                        <a href="/action/database/view-record/${value}">${value}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="/action/database/view-record/${column.table.pkIdentifier}-${value}">${value}</a>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                ${value}
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
<c:if test="${((column.foreignKey) &&  (column.entityName) && (value ne ''))}">
    <span id="showName-${identifier}">
    <a class="fetchable" onclick="jQuery('#fetch-${identifier}').hide();
            jQuery('#name-${identifier}').show();
            jQuery('#showID-${identifier}').show();
            jQuery('#showName-${identifier}').hide();
            jQuery('#name-${identifier}').load('/action/database/fetch-entity-name/${val}', function() { processPopupLinks(); }); return false;"
            >?</a>
</span>
<span id="showID-${identifier}" style="display: none">
    <a class="unfetchable" onclick="jQuery('#fetch-${identifier}').show();
            jQuery('#name-${identifier}').hide();
            jQuery('#showID-${identifier}').hide();
            jQuery('#showName-${identifier}').show();"></a>
</span>
</c:if>

