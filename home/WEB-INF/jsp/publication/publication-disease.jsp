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
    <caption>Human Disease / Model Data
        (<zfin:choice choicePattern="0#Records| 1#Record| 2#Records"
                      integerEntity="${fn:length(diseases)}"
                      includeNumber="true"/>)
    </caption>
    <tr>
        <th>Human Disease</th>
        <th>Fish</th>
        <th>Environment</th>
        <th>Evidence Code</th>
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

            <td>
                <c:forEach var="diseaseAnnos" items="${disease.diseaseAnnotationModel}" varStatus="fishLoop">
                    <zfin:link entity="${diseaseAnnos.fishExperiment.fish}"/><c:if test="${!fishLoop.last}"><br></c:if>
                </c:forEach>

            </td>
            <td>
                <c:forEach var="diseaseAnnos" items="${disease.diseaseAnnotationModel}" varStatus="loop">

                    <zfin:link entity="${diseaseAnnos.fishExperiment.experiment}"/><c:if
                        test="${!loop.last}"><br></c:if>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="diseaseAnnos" items="${disease.diseaseAnnotationModel}" varStatus="loop">
                    <c:if test="${diseaseAnnos.diseaseAnnotation.evidenceCode == 'ZDB-TERM-170419-250'}">
                       TAS
                    </c:if>
                    <c:if test="${diseaseAnnos.diseaseAnnotation.evidenceCode == 'ZDB-TERM-170419-251'}">
                        IC
                    </c:if>
                    <c:if test="${!loop.last}"><br></c:if>
                </c:forEach>
            </td>


        </zfin:alternating-tr>
    </c:forEach>
</table>

<script>
    $(document).ready(function () {
        $('.tooltip').tipsy({gravity: 's'});
    });

</script>