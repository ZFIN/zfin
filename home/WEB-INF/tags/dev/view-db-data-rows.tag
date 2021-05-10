<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dataMap" type="java.util.Map" required="false" %>

<p/>
<table class="summary groupstripes">
    <c:forEach var="columnEntity" items="${dataMap}" varStatus="row_index">
        <tr>
            <th width="10%">
                    <span
                            <c:if test="${columnEntity.key.primaryKey}">style="color: maroon;"</c:if>
                            <c:if test="${columnEntity.key.foreignKey}">style="color: green;"</c:if>
                            >${columnEntity.key.name}</span>
            </th>
            <c:forEach var="value" items="${columnEntity.value}" varStatus="col_index">
                <c:set var="identifier" value="${row_index.index}-${col_index.index}"/>
                <td>
                    <zfin2:create-record-link value="${value}" column="${columnEntity.key}"
                                              identifier="${identifier}"/>
                    <c:if test="${((fn:contains(value, ',')) ||(fn:contains(value, '//|'))||(fn:contains(value, ' '))) &&
                    fn:contains(value, 'ZDB-')}">
                        <p/><zfin2:create-list-of-contained-record-links value="${value}"
                                                                         identifier="${identifier}"/>
                    </c:if>
                </td>
            </c:forEach>
        </tr>
    </c:forEach>
</table>
<table>
    <tr>
        <td style="text-align: left" width="30%">
            <div id="showAllLink">
                <a href="javascript:showAllNames();">Show Names for Ids</a>
            </div>
            <div id="hideAllLink" style="display: none">
                <a href="javascript:showAllIds();">Show Ids for names</a>
            </div>
        </td>
    </tr>
</table>

<script language="JavaScript">
    function showAllNames() {
        jQuery('.fetchable').each(function() {
            jQuery(this).click();
        });
        jQuery('#showAllLink').hide();
        jQuery('#hideAllLink').show();
    }

    function showAllIds() {
        jQuery('.unfetchable').each(function() {
            jQuery(this).click();
        });
        jQuery('#showAllLink').show();
        jQuery('#hideAllLink').hide();
    }
</script>
