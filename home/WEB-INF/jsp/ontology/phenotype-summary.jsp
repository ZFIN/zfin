<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

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
                            <td><zfin:link entity="${entry.key}"/>
                                <c:if test="${!entry.key.imgless}">
                                    <img src="/images/camera_icon.gif" alt="with image">
                                </c:if>
                            </td>
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
