<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>
<%@ page import="org.zfin.framework.featureflag.FeatureFlagEnum" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<c:set var="SUMMARY" value="${NavigationMenuOptions.SUMMARY.value}"/>
<c:set var="RELATIONSHIP" value="${NavigationMenuOptions.RELATIONSHIPS.value}"/>
<c:set var="HUMAN_DISEASE" value="${NavigationMenuOptions.CHEBI_HUMAN_DISEASE.value}"/>
<c:set var="PHENOTYPE" value="${NavigationMenuOptions.PHENOTYPE.value}"/>
<c:set var="ALLELES" value="${NavigationMenuOptions.ALLELE.value}"/>
<c:set var="PHENOTYPE_CHEBI" value="${NavigationMenuOptions.PHENOTYPE_CHEBI.value}"/>
<c:set var="GENES_INVOLVED" value="${NavigationMenuOptions.GENES_INVOLVED.value}"/>
<c:set var="ZEBRAFISH_MODELS" value="${NavigationMenuOptions.ZEBRAFISH_MODELS.value}"/>
<c:set var="EXPRESSION" value="${NavigationMenuOptions.EXPRESSION.value}"/>
<c:set var="OTHER_PAGES" value="${NavigationMenuOptions.OTHER_PAGE.value}"/>
<c:set var="CITATIONS" value="${NavigationMenuOptions.CITATION.value}"/>

<c:set var="secs"/>

<z:dataPage sections="${[]}">

    <jsp:body>
        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">Phenotype Figure Summary</div>
            </p>
            <z:attributeList>
                <z:attributeListItem label="Term">
                    <zfin:link entity="${term}"/>
                </z:attributeListItem>
                <z:attributeListItem label="Fish">
                    <zfin:link entity="${fish}"/>
                </z:attributeListItem>
                <z:attributeListItem label="Condition">
                    <zfin:link entity="${experiment}"/>
                </z:attributeListItem>
            </z:attributeList>
        </div>

        <z:section title="Phenotype" show="${!empty phenotypeSummaryList}">
            <table class="data-table">
                <thead>
                    <tr>
                        <td>Publication</td>
                        <td>Figure</td>
                        <td>Phenotype</td>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="entry" items="${phenotypeSummaryList}" varStatus="status">
                        <tr>
                            <td><zfin:link entity="${entry.key.publication}"/></td>
                            <td><zfin:link entity="${entry.key}"/></td>
                            <td>
                                <c:forEach var="phenotype" items="${entry.value}" varStatus="status">
                                    <a href="/action/phenotype/statement/${phenotype.id}">
                                            ${phenotype.displayName}
                                    </a>
                                    <a href="/action/phenotype/statement-popup/${phenotype.id}" class="popup-link data-popup-link"></a>
                                    <div></div>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>

                </tbody>
            </table>
        </z:section>
    </jsp:body>


</z:dataPage>
