<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="phenotypeDisplays" type="java.util.Collection" required="false" %>
<%@ attribute name="suppressMoDetails" type="java.lang.Boolean" required="false" %>
<%@ attribute name="fishAndCondition" type="java.lang.Boolean" required="true" %>
<%@ attribute name="secondColumn" type="java.lang.String" required="true" %>

<table width="100%" class="summary rowstripes">
    <thead>
    <tr>
        <th width="40%">
            Phenotype
        </th>
        <c:if test="${fishAndCondition && secondColumn ne 'condition'}">
            <th>
                Fish
            </th>
        </c:if>
        <c:if test="${fishAndCondition}">
            <th>
                Conditions
            </th>
        </c:if>
        <th>
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="phenotypeDisplay" items="${phenotypeDisplays}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>
                <zfin:link entity="${phenotypeDisplay.phenoStatement}"/>
            </td>
            <c:if test="${fishAndCondition && secondColumn ne 'condition'}">
                <td>
                    <zfin:link entity="${phenotypeDisplay.phenoStatement.phenotypeExperiment.fishExperiment.fish}"/>
                </td>
            </c:if>
            <c:if test="${fishAndCondition}">
                <td>
                    <zfin:link entity="${phenotypeDisplay.experiment}" suppressMoDetails="${displayMoDetails}"/>
                </td>
            </c:if>
            <td>
                <c:forEach var="figsPub" items="${phenotypeDisplay.figuresPerPub}">
                    <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                        <a href='/${fig.zdbID}'>${fig.label}</a><c:if test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if test="${!figloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                    from <zfin:link entity="${figsPub.key}"/><br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
