<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="criteria" type="org.zfin.expression.ExpressionSummaryCriteria"
              required="true" description="ExpressionSummaryCriteria object used to build the summary table" %>


<table class="primary-entity-attributes">
    <c:if test="${!empty criteria.gene}">
        <tr>
            <th>Gene:</th>
            <td><zfin:link entity="${criteria.gene}"/> </td>
        </tr>
    </c:if>
    <c:if test="${!empty criteria.fishExperiment}">
        <tr>
            <th>Genotype:</th>
            <td><zfin:link entity="${criteria.fishExperiment.fish.genotype}"/>
        </tr>
        <tr>
            <th>Conditions:</th>
            <td><zfin:link entity="${criteria.fishExperiment.experiment}"/></td>
        </tr>
    </c:if>
    <%--sometimes rather than a genox, it's just a genotype--%>
    <c:if test="${!empty criteria.fish}">
        <tr>
            <th class="genotype-name-label">Fish:</th>
            <td class="genotype-name-value"><zfin:link entity="${criteria.fish}"/>
        </tr>
    </c:if>
    <c:if test="${!empty criteria.antibody}">
        <tr>
            <th>Antibody:</th>
            <td><zfin:link entity="${criteria.antibody}"/> </td>
        </tr>
    </c:if>
    <c:if test="${!empty criteria.sequenceTargetingReagent}">
        <tr>
            <th>${criteria.sequenceTargetingReagent.markerType.displayName}:</th>
            <td><zfin:link entity="${criteria.sequenceTargetingReagent}"/> </td>
        </tr>
    </c:if>
    <c:if test="${!empty criteria.entity}">
        <tr>
            <th>Term:</th>
            <td><zfin:link entity="${criteria.entity}" suppressPopupLink="true"/></td>
        </tr>
    </c:if>

    <c:if test="${empty criteria.entity && !empty criteria.singleTermEitherPosition}">
        <tr>
            <th>Term:</th>
            <td><zfin:link entity="${criteria.singleTermEitherPosition}" suppressPopupLink="true"/></td>
        </tr>
    </c:if>

    <c:choose>
        <c:when test="${(!empty criteria.start) && (!empty criteria.end) && (criteria.start == criteria.end)}">
            <tr>
                <th>Stage:</th>
                <td><zfin:link entity="${criteria.start}"/></td>
            </tr>
        </c:when>
        <c:otherwise>
            <c:if test="${!empty criteria.start}">
                <tr>
                    <th>Start Stage:</th>
                    <td><zfin:link entity="${criteria.start}"/></td>
                </tr>
            </c:if>
            <c:if test="${!empty criteria.end}">
                <tr>
                    <th>End Stage:</th>
                    <td><zfin:link entity="${criteria.end}"/></td>
                </tr>
            </c:if>
        </c:otherwise>
    </c:choose>
    <c:if test="${criteria.showCondition && criteria.standardEnvironment}">
        <tr>
            <th>Condition:</th>
            <td>Standard or generic control only</td>
        </tr>
    </c:if>
    <c:if test="${criteria.chemicalEnvironment}">
        <tr>
            <th>Condition:</th>
            <td>Chemical environments only</td>
        </tr>
    </c:if>
    <c:if test="${criteria.wildtypeOnly}">
        <tr>
            <th>Background:</th>
            <td>Wild-type only</td>
        </tr>
    </c:if>


</table>
