<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="cloneBean" type="org.zfin.marker.presentation.CloneBean" rtexprvalue="true" required="true" %>
<%@ attribute name="soTerm" type="org.zfin.ontology.GenericTerm" rtexprvalue="true" required="false" %>

<%--<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>--%>
<%--<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>--%>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${cloneBean.clone.markerType.displayName}&nbsp;Name: </span></th>
        <td><span class="name-value"><zfin:name entity="${cloneBean.clone}"/> </span>
        </td>
    </tr>
    <tr>
        <th>Sequence Ontology ID :</th>
        <td>
            <a href="http://www.sequenceontology.org/browser/current_svn/term/${soTerm.oboID}"/> ${soTerm.oboID}</td></a>
        </td>
    </tr>
    <zfin2:previousNamesFast label="Previous Name" previousNames="${cloneBean.previousNames}"/>

    <c:if test="${!empty cloneBean.clone.problem}">
    <tr>
        <th>Clone Problem Type:</th>
        <td valign="middle">${cloneBean.clone.problem} <i class="warning-icon"></i></td>
    </c:if>

    <%--<zfin2:cloneData clone="${cloneBean.clone}" isThisseProbe="${cloneBean.isThisseProbe}"/>--%>
    <zfin2:cloneData cloneBean="${cloneBean}"/>

    <tr>
        <th>Location:</th>
        <td><zfin2:displayLocation entity="${cloneBean.clone}"/></td>
    </tr>

    <zfin2:entityNotes entity="${cloneBean.clone}"/>

</table>





