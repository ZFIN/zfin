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

<table class="summary groupstripes" style="margin-top: 1em;">
    <caption>Disease / Model Data
        (<zfin:choice choicePattern="0#Records| 1#Record| 2#Records"
                      integerEntity="${fn:length(diseases)}"
                      includeNumber="true"/>)
    </caption>
    <tr>
        <th> Human Disease</th>
        <th> Evidence Code</th>
        <th>Fish Model</th>
        <th>Environment</th>
    </tr>
    <c:forEach var="disease" items="${diseases}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupByBean="disease.termName" groupBeanCollection="${diseases}">
            <td>
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${diseases}"
                                     groupByBean="disease.termName">

                    <zfin:link entity="${disease.disease}"/>
                </zfin:groupByDisplay>
            </td>
            <td>${disease.evidenceCode}</td>
            <td>
                <c:if test="${not empty disease.fishModel }">
                    <zfin:link entity="${disease.fishModel.fish}"/>
                </c:if>
            </td>
            <td>
                <c:if test="${not empty disease.fishModel }">
                    <zfin:link entity="${disease.fishModel.experiment}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<script>
    $(document).ready(function () {
        $('.tooltip').tipsy({gravity: 's'});
    });

</script>