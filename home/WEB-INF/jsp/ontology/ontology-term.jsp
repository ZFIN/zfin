<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<z:page>
    <div class="data-page">

        <authz:authorize access="hasRole('root')">
        <a class="dropdown-item" href="/action/ontology/prototype/${formBean.term.zdbID}">Prototype View</a>
        </authz:authorize>


    <zfin2:dataManager oboID="${formBean.term.oboID}" termID="${formBean.term.zdbID}"/>

        <table class="primary-entity-attributes">

            <tr>
                <th width="5%"><span class="name-label">Term&nbsp;Name:</span></th>
                <td><span class="name-value">${formBean.term.termName}</span></td>
                <td valign="top" align="right" width="5%">
                </td>
                <td rowspan="3" valign="top" align="right" width="5%">
                    <span style="font-size: 12px">
                    Search Ontology: <zfin2:lookup ontologyName="${Ontology.AOGODOCHEBI.toString()}"
                                                   action="${LookupStrings.ACTION_TERM_SEARCH}"
                                                   wildcard="true" useIdAsTerm="true" termsWithDataOnly="false"/>
                        </span>
                </td>
            </tr>
            <tr>

                <th>Synonyms:</th>
                <td id="term-synonyms">
                    <ul class="comma-separated" data-toggle="collapse" data-show="3">
                        <c:forEach items="${formBean.term.sortedAliases}" var="alias">


                            <li>${alias.alias}</li>

                        </c:forEach>
                    </ul>
                </td>
            </tr>

            <tr>
                <th>Definition:</th>
                <c:set var="term" value="${formBean.term}"/>
                <td id="term-definition">${term.definition}
                    <zfin2:termDefinitionReferences term="${term}"/>
                </td>
            </tr>
            <c:if test="${formBean.term.ontology.ontologyName == 'zebrafish_anatomy' && !term.obsolete}">
                <tr>
                    <th>Appears&nbsp;at:</th>
                    <td>
                        <zfin:link entity="${formBean.term.start}" longVersion="true"/>
                    </td>
                </tr>
                <tr>
                    <th>Evident&nbsp;until:</th>
                    <td>
                        <zfin:link entity="${formBean.term.end}" longVersion="true"/>
                    </td>
                </tr>
            </c:if>
            <tr>
                <th>References:</th>
                <td id="term-xrefs">
                    <zfin2:toggledLinkList collection="${formBean.term.sortedXrefs}" maxNumber="3" commaDelimited="true"/>
                </td>
            </tr>
            <tr>
                <th>Ontology:</th>
                <td id="ontology-name">${formBean.term.ontology.commonName}
                    <zfin2:ontologyTermLinks term="${formBean.term}"/>
                </td>
            </tr>

            <c:if test="${formBean.term.obsolete}">
                <tr>
                    <th class="red">Obsolete:</th>
                    <td class="red">true</td>
                </tr>
            </c:if>
        </table>

        <c:if test="${!empty formBean.term.images}">
            <div class="summary">
                <c:forEach var="image" items="${formBean.term.images}">
                    <zfin:link entity="${image}"/>
                </c:forEach>
            </div>
        </c:if>

        <div class="summary">
            <span class="summaryTitle">
                Relationships
                <a class='popup-link info-popup-link' href='/action/ontology/note/ontology-relationship'></a>
            </span>
            <table class="summary horizontal-solidblock">
                <c:forEach var="relationshipPresentation" items="${formBean.termRelationships}" varStatus="index">
                    <tr id="${zfn:makeDomIdentifier(relationshipPresentation.type)}">
                        <th>
                                ${relationshipPresentation.type}:
                        </th>
                        <td>
                            <zfin2:createExpandCollapseList items="${relationshipPresentation.items}" id="${index.count}"/>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>

        <script type="text/javascript">
            function toggle(shortVal, longVal) {
                document.getElementById(shortVal).style.display = 'none';
                document.getElementById(longVal).style.display = 'inline';
            }
        </script>


        <c:if test="${isDiseaseTerm}">
            <div class="summary">
    <span class="summaryTitle">OTHER ${formBean.term.termName} PAGES</span>


                <div id="other-pages">
                    <zfin2:subsection title="${title}" test="${!empty formBean.agrDiseaseLinks}" showNoData="true" noDataText="No links to external sites">
                        <table class="summary horizontal-solidblock">
                            <c:forEach var="link" items="${formBean.agrDiseaseLinks}" varStatus="loop">
                                <tr>
                                    <td>
                                        <zfin:link entity="${link}"/>
                                        <c:if test="${link.publicationCount > 0}">
                                            <c:choose>
                                                <c:when test="${link.publicationCount == 1}">
                                                    (<a href="/${link.singlePublication.zdbID}">${link.publicationCount}</a>)
                                                </c:when>
                                                <c:otherwise>
                                                    (<a href="/action/infrastructure/data-citation-list/${link.zdbID}">${link.publicationCount}</a>)
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </table>
                    </zfin2:subsection>
                </div>
            <div id="genes-involved">
                <zfin2:genesAssociatedWithDisease formBean="${formBean}"/>
            </div>
            <div id="fish-models">
                <zfin2:fishModels term="${formBean.term}" fishModels="${fishModels}"/>
            </div>
        </c:if>

        <c:if test="${formBean.term.ontology.expressionData}">
            <jsp:include page="../anatomy/anatomy_term_detail_expression.jsp" />
        </c:if>


        <c:if test="${showPhenotypeSection}">
            <jsp:include page="../anatomy/anatomy_term_detail_phenotype.jsp" />
        </c:if>

        <zfin2:ExpandRequestSections sectionVisibility="${formBean.sectionVisibility}"/>

        <c:if test="${isDiseaseTerm}">
            <div class="summary">
                <c:choose>
                    <c:when test="${numberOfCitations == 0}"><span class="name-label"> CITATIONS:</span> None</c:when>
                    <c:otherwise>
                        <a href="/action/ontology/disease-publication-list/${term.oboID}">CITATIONS</a> (${numberOfCitations})
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

    </div>
    <script>
        jQuery(function () {
            jQuery("#genes-involved").tableCollapse({label: ""});
        });
    </script>
</z:page>