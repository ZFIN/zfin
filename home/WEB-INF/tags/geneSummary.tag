<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>
<%@ attribute name="additionalCssClasses" description="additional css class for the table" required="false" %>

<div class="ontology-term-mini-summary">
    <table class="ontology-term-mini-summary <c:if test="${!empty additionalCssClasses}">${additionalCssClasses}</c:if>">
        <tr>
            <th class="name">Symbol:</th>
            <td class="name"><zfin:link entity="${gene}" suppressPopupLink="true"/></td>
        </tr>
        <tr>
            <th>Name:</th>
            <td><zfin:name entity="${gene}"/></td>
        </tr>
        <c:if test="${!empty previousNames}">
            <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}"/>
        </c:if>
        <tr>
            <th>Location:</th>
            <td>
                <zfin2:displayLocation entity="${gene}" longDetail="true"/>
            </td>
        </tr>
    </table>
</div>