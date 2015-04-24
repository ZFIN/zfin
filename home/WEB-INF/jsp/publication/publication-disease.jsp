<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${publication.shortAuthorList} - ${figure.label}"/>
    </tiles:insertTemplate>
</div>

<zfin-figure:publicationInfo publication="${publication}"
                             showThisseInSituLink="false"
                             showErrataAndNotes="false"/>

<table class="summary rowstripes" style="margin-top: 1em;">
    <caption>Disease / Models
        (<zfin:choice choicePattern="0#Records| 1#Record| 2#Records"
                     integerEntity="${fn:length(diseases)}"
                     includeNumber="true"/>)
    </caption>
    <tr>
        <th style="width: 50%">
            Human Disease
        </th>
        <th><span class="tooltip" title="Fish = Genotype + Reagent">Fish Model</span></th>
    </tr>
    <c:forEach var="disease" items="${diseases}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupByBean="zdbID" groupBeanCollection="${diseases}">
            <td><zfin:link entity="${disease}"/></td>
            <td>
                <%-- nothing to see here --%>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<script>
    $(document).ready(function() {
        $('.tooltip').tipsy({gravity:'s'});
    });

</script>