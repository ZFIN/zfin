<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.publication.presentation.UnfinishedPhenotypeBean" scope="request"/>

<c:if test="${ not empty formBean.distinctFigures }">
    <div class="summary">
        The following figures still have mutants without phenotypes defined.
        <br/>
        <c:forEach var="figure" items="${formBean.distinctFigures}" varStatus="loop">
            ${figure.label}  <c:if test="${!loop.last}">,</c:if>
        </c:forEach>
    </div>
</c:if>