<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="genes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="antibodies" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="fishesAndGenotypes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="strs" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="experiments" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="entities" type="java.util.List" rtexprvalue="true" required="true" %>
<%@attribute name="start" type="org.zfin.anatomy.DevelopmentStage" rtexprvalue="true" required="true"%>
<%@attribute name="end" type="org.zfin.anatomy.DevelopmentStage" rtexprvalue="true" required="true"%>
<%@attribute name="probe" type="org.zfin.marker.Clone" rtexprvalue="true" required="false" %>
<%@attribute name="probeSuppliers" type="java.util.List" rtexprvalue="true" required="false" %>

<%@attribute name="suppressProbe" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<%-- We don't want the label to display if there's no data --%>

<c:if test="${!empty start}">
    <zfin2:subsection title="EXPRESSION / LABELING:" test="${!empty start}"> <%--test="${!empty entities}"--%>

        <table class="primary-entity-attributes">

            <c:if test="${!empty genes}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Genes:| 1#Gene:| 2#Genes:" integerEntity="${fn:length(genes)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${genes}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
            <c:if test="${!empty antibodies}">
                <tr>
                    <th>
                        <zfin:choice choicePattern="0#Antibodies:| 1#Antibody:| 2#Antibodies:" integerEntity="${fn:length(antibodies)}"/>
                    </th>
                    <td> <zfin2:toggledPostcomposedList entities="${antibodies}" maxNumber="5"/>  </td>
                </tr>
            </c:if>
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
                    <th>

                        <zfin:choice choicePattern="0#Anatomical Terms:| 1#Anatomical Term:| 2#Anatomical Terms:" integerEntity="${fn:length(entities)}"/>
                    </th>

                    <td> <zfin2:toggledPostcomposedList entities="${entities}" numberOfEntities="${fn:length(entities)}" maxNumber="5"/>  </td>

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

            <c:if test="${!suppressProbe}">
                <c:if test="${!empty probe}">
                    <tr>
                        <th>Probe:</th>
                        <td>
                            <zfin:link entity="${probe}"/>

                            &nbsp; <strong><a href="/zf_info/stars.html">Quality:</a></strong>
                            <img src="/images/${probe.rating+1}0stars.gif" alt="Rating ${probe.rating +1}">

                        </td>
                    </tr>
                    <%-- this is deviating from the old figureview, because the nice java tag we have doesn't seem to follow
                         that format.  Maybe people will like this better? --%>
                    <c:if test="${!empty probeSuppliers}">
                        <tr>
                            <th>
                                <zfin:choice choicePattern="0#Suppliers:| 1#Supplier:| 2#Suppliers:" integerEntity="${fn:length(probeSuppliers)}"/>
                            </th>
                            <td><c:forEach var="supplier" items="${probeSuppliers}">
                                ${supplier.linkWithAttributionAndOrderThis}
                            </c:forEach></td>
                        </tr>
                    </c:if>

                </c:if>
            </c:if>


        </table>

    </zfin2:subsection>
</c:if>