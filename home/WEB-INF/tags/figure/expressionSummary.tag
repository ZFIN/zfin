<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="summary" type="org.zfin.figure.presentation.FigureExpressionSummary" rtexprvalue="true" required="true" %>
<%@attribute name="suppressProbe" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<%-- We don't want the label to display if there's no data --%>

<c:if test="${!empty summary.startStage}">
    <zfin2:subsection title="EXPRESSION / LABELING:" test="${!empty summary.startStage}">

        <table class="primary-entity-attributes">

            <c:if test="${!empty summary.genes}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Genes:| 1#Gene:| 2#Genes:" integerEntity="${fn:length(summary.genes)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${summary.genes}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty summary.antibodies}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Antibodies:| 1#Antibody:| 2#Antibodies:" integerEntity="${fn:length(summary.antibodies)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${summary.antibodies}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
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
                        <td> <zfin2:toggledExperimentList expressionResults="${summary.experiments}" maxNumber="5"/>  </td>
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
                    <th>

                        <zfin:choice choicePattern="0#Anatomical Terms:| 1#Anatomical Term:| 2#Anatomical Terms:" integerEntity="${fn:length(summary.entities)}"/>
                    </th>

                    <td> <zfin2:toggledPostcomposedList entities="${summary.entities}" numberOfEntities="${fn:length(summary.entities)}" maxNumber="5"/>  </td>

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

            <c:if test="${!suppressProbe}">
                <c:if test="${!empty summary.probe}">
                    <tr>
                        <th>Probe:</th>
                        <td>
                            <zfin:link entity="${summary.probe}"/>

                            <c:if test="${!empty summary.probe.rating}">
                                &nbsp; <strong><a href="/zf_info/stars.html">Quality:</a></strong>
                                <img src="/images/${summary.probe.rating+1}0stars.gif" alt="Rating ${summary.probe.rating +1}">
                            </c:if>
                        </td>
                    </tr>
                    <%-- this is deviating from the old figureview, because the nice java tag we have doesn't seem to follow
                         that format.  Maybe people will like this better? --%>
                    <c:if test="${!empty summary.probeSuppliers}">
                        <tr>
                            <th>
                                <zfin:choice choicePattern="0#Suppliers:| 1#Supplier:| 2#Suppliers:" integerEntity="${fn:length(summary.probeSuppliers)}"/>
                            </th>
                            <td><c:forEach var="supplier" items="${summary.probeSuppliers}">
                                ${supplier.linkWithAttributionAndOrderThis}
                            </c:forEach></td>
                        </tr>
                    </c:if>

                </c:if>
            </c:if>
        </table>

    </zfin2:subsection>
</c:if>