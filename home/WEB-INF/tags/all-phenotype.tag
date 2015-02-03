<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="phenotypeDisplays" type="java.util.Collection" required="false" %>
<%@ attribute name="suppressMoDetails" type="java.lang.Boolean" required="false" %>
<%@ attribute name="showNumberOfRecords" type="java.lang.Integer" required="true" %>
<%@ attribute name="secondColumn" type="java.lang.String" required="true" %>

<table class="summary rowstripes">
    <thead>
    <tr>
        <th width="48%">
            Phenotype
        </th>
        <th width="17%">
            <c:choose>
                <c:when test="${secondColumn eq 'condition'}">
                    Conditions
                </c:when>
                <c:otherwise>
                    Fish
                </c:otherwise>
            </c:choose>
        </th>
        <th width="35%">
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="phenotypeDisplay" items="${phenotypeDisplays}" varStatus="loop" end="${showNumberOfRecords-1}">
        <zfin:alternating-tr loopName="loop">
            <td>
                <zfin:link entity="${phenotypeDisplay.phenoStatement}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${secondColumn eq 'condition'}">
                        <zfin:link entity="${phenotypeDisplay.experiment}" suppressMoDetails="${displayMoDetails}"/>
                    </c:when>
                    <c:otherwise>
                        <zfin:link entity="${phenotypeDisplay.phenoStatement.phenotypeExperiment.genotypeExperiment.genotype}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:forEach var="figsPub" items="${phenotypeDisplay.figuresPerPub}">
                    <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                        <a href='/${fig.zdbID}'><zfin2:figureOrTextOnlyLink figure="${fig}" integerEntity="1"/></a><zfin2:showCameraIcon hasImage="${!fig.imgless}"/><c:if test="${!figloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                    from <zfin:link entity="${figsPub.key}"/><br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
