<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@attribute name="fishesAndGenotypes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="strs" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="entities" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="experiments" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="start" type="org.zfin.anatomy.DevelopmentStage" rtexprvalue="true" required="true"%>
<%@attribute name="end" type="org.zfin.anatomy.DevelopmentStage" rtexprvalue="true" required="true"%>

<c:if test="${!empty fishesAndGenotypes}">  <%-- don't display the summary at all if there's no data--%>
    <zfin2:subsection title="PHENOTYPE:" test="${!empty fishesAndGenotypes}">

        <table class="primary-entity-attributes">

            <c:if test="${!empty fishesAndGenotypes}">
                <tr>
                    <th>
                        <span class="fish-label" title="Fish = Genotype + Reagents">Fish:</span>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${fishesAndGenotypes}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty experiments}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Conditions:| 1#Condition:| 2#Conditions:" integerEntity="${fn:length(experiments)}"/>
                    </th>
                    <td> <zfin2:toggledExperimentList expressionResults="${experiments}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty strs}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Knockdown Reagents:| 1#Knockdown Reagent:| 2#Knockdown Reagents:" integerEntity="${fn:length(strs)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${strs}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty entities}">
                <tr>
                    <th>Observed In:</th>
                    <td> <zfin2:toggledPostcomposedList entities="${entities}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <tr>
                <th>
                    <c:choose>
                        <c:when test="${start != end}">Stage Range:</c:when>
                        <c:otherwise>Stage:</c:otherwise>
                    </c:choose>
                </th>
                <td>
                    <zfin:link entity="${start}"/>
                    <c:if test="${start != end}">
                        to <zfin:link entity="${end}"/>
                    </c:if>
                </td>
            </tr>

        </table>

    </zfin2:subsection>
</c:if>