<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-figure:publicationInfo publication="${publication}"
                             showThisseInSituLink="false"
                             showErrataAndNotes="false"/>

<table class="summary rowstripes" style="margin-top: 1em;">
    <caption>
        <zfin:choice choicePattern="0#Engineered Foreign Genes| 1#Engineered Foreign Gene| 2#Engineered Foreign Genes"
                     integerEntity="${fn:length(markers)}"
                     includeNumber="true"/>
    </caption>
    <tr>
        <th>Name</th>
    </tr>
    <c:forEach var="marker" items="${markers}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td><zfin:link entity="${marker}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
