<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="phenotypeSummaryCriteria" class="org.zfin.fish.presentation.PhenotypeSummaryCriteria" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="PHENOTYPES" value="Phenotypes"/>

<z:dataPage sections="${[SUMMARY, PHENOTYPES]}">

    <jsp:attribute name="entityName">
        ${zfn:getTruncatedName(fish.name, 30)}
    </jsp:attribute>

    <jsp:body>
        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FISH PHENOTYPE SUMMARY</div>
            <h1><zfin:name entity="${fish}"/></h1>
            <z:attributeList>
                <z:attributeListItem label="Fish">
                    <zfin:link entity="${fish}"/>
                </z:attributeListItem>
                <z:attributeListItem label="Conditions">
                    All
                </z:attributeListItem>
                <c:if test="${!empty phenotypeSummaryCriteria.searchCriteriaPhenotype}">
                    <z:attributeListItem label="Matching Terms">
                        <c:forEach var="term" items="${phenotypeSummaryCriteria.searchCriteriaPhenotype}" varStatus="index">
                            ${term.name}<c:if test="${!index.last}">,</c:if>
                        </c:forEach>
                    </z:attributeListItem>
                </c:if>
            </z:attributeList>
        </div>

        <z:section title="${PHENOTYPES}">
            <z:section title=" ">
                <zfin2:figurePhenotypeSummary figureSummaryDisplayList="${figureSummaryDisplay}"/>
            </z:section>
        </z:section>
    </jsp:body>

</z:dataPage>
