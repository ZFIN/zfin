<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="cloneBean" type="org.zfin.marker.presentation.CloneBean" rtexprvalue="true" required="true" %>

<%--<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>--%>
<%--<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>--%>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${cloneBean.clone.markerType.displayName}&nbsp;Name: </span></th>
        <td><span class="name-value"><zfin:name entity="${cloneBean.clone}"/> </span></td>
    </tr>
    <zfin2:previousNamesFast label="Previous Names:" previousNames="${cloneBean.previousNames}"/>

    <c:if test="${!empty cloneBean.clone.problem}">
    <tr>
        <th>Clone Problem Type:</th>
        <td valign="middle">${cloneBean.clone.problem} <img src="/images/warning-noborder.gif" width="20" height="20" align="top"></td>
    </c:if>

    <%--<zfin2:cloneData clone="${cloneBean.clone}" isThisseProbe="${cloneBean.isThisseProbe}"/>--%>
    <zfin2:cloneData cloneBean="${cloneBean}"/>

    <%--<zfin2:notesInDiv hasNotes="${formBean.marker}"/>--%>
    <zfin2:notesInDiv hasNotes="${cloneBean.clone}"/>

</table>



