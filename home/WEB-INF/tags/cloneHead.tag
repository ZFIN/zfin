<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="clone" type="org.zfin.marker.Clone"
              rtexprvalue="true" required="true" %>

<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${clone.markerType.name}&nbsp;Name: </span></th>
        <td><span class="name-value"><zfin:name entity="${clone}"/> </span></td>
    </tr>
    <zfin2:previousNames entity="${clone}"/>

    <c:if test="${!empty clone.problem}">
    <tr>
        <th>Clone Problem Type:</th>
        <td>{clone.problem}</td>
    </c:if>

    <zfin2:cloneData clone="${clone}" isThisseProbe="${isThisseProbe}"/>

</table>

<zfin2:notes hasNotes="${formBean.marker}"/>


