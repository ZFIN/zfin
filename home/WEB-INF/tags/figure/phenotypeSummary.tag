<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="summary" type="org.zfin.figure.presentation.FigurePhenotypeSummary" rtexprvalue="true" required="true" %>

<c:if test="${!empty summary.fish}">  <%-- don't display the summary at all if there's no data--%>
    <zfin2:subsection title="PHENOTYPE:" test="${!empty summary.fish}">

        <table class="primary-entity-attributes">

            <c:if test="${!empty summary.fish}">
                <tr>
                    <th>
                        Fish:
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${summary.fish}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty summary.experiments}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Conditions:| 1#Condition:| 2#Conditions:" integerEntity="${fn:length(summary.experiments)}"/>
                    </th>
                    <td> <zfin2:toggledExperimentList experimentList="${summary.experiments}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty summary.sequenceTargetingReagents}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Knockdown Reagents:| 1#Knockdown Reagent:| 2#Knockdown Reagents:" integerEntity="${fn:length(summary.sequenceTargetingReagents)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${summary.sequenceTargetingReagents}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty summary.entities}">
                <tr>
                    <th>Observed In:</th>
                    <td> <zfin2:toggledPostcomposedList entities="${summary.entities}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <tr>
                <th>
                    <c:choose>
                        <c:when test="${summary.startStage != summary.endStage}">Stage Range:</c:when>
                        <c:otherwise>Stage:</c:otherwise>
                    </c:choose>
                </th>
                <td>
                    <zfin:link entity="${summary.startStage}"/>
                    <c:if test="${summary.startStage != summary.endStage}">
                        to <zfin:link entity="${summary.endStage}"/>
                    </c:if>
                </td>
            </tr>

        </table>

    </zfin2:subsection>
</c:if>