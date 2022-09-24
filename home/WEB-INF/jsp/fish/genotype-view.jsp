<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>
<c:set var="genotype" value="${formBean.genotype}"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="NOTE" value="Notes"/>
<c:set var="FISH" value="Fish"/>
<c:set var="COMPOSITION" value="Genotype Composition"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, NOTE, COMPOSITION, FISH, CITATIONS]}">

<jsp:attribute name="entityName">
        <zfin:name entity="${genotype}"/>
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/genotype/view/${genotype.zdbID}">Old View</a>
                <a class="col-sm" href="/action/updates/${genotype.zdbID}">
                    Last Update:
                    <c:set var="latestUpdate" value="${zfn:getLastUpdate(genotype.zdbID)}"/>
                    <c:choose>
                    <c:when test="${!empty latestUpdate}">
                        <fmt:formatDate value="${latestUpdate.dateUpdated}" type="date"/>
                    </c:when>
                    <c:otherwise>
                        Never modified
                    </c:otherwise>
                </c:choose>
                </a>
            </nav>
        </authz:authorize>
    </jsp:attribute>

    <jsp:body>
        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">GENOTYPE</div>
            <h1><zfin:name entity="${genotype}"/></h1>
            <jsp:include page="genotype-view-summary.jsp"/>
        </div>

        <z:section title="${NOTE}">
            <authz:authorize access="hasRole('root')">
                <z:section title="Curator Notes">
                    <z:dataTable collapse="true"
                                 hasData="${genotype.sortedDataNotes != null && fn:length(genotype.sortedDataNotes) > 0 }">
                        <thead>
                            <tr>
                                <th>Curator</th>
                                <th>Note</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="dataNote" items="${genotype.sortedDataNotes}" varStatus="loopCurNote">
                                <tr>
                                    <td>${dataNote.curator.fullName}&nbsp;
                                        <fmt:formatDate value="${dataNote.date}" pattern="yyyy/MM/dd hh:mm"/>
                                    </td>
                                    <td>
                                            ${dataNote.note}
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </z:dataTable>
                </z:section>
            </authz:authorize>
            <z:section title="External Notes">
                <c:if test="${genotype.externalNotes ne null && fn:length(genotype.externalNotes) > 0 }">
                    <c:forEach var="extNote" items="${formBean.genotype.externalNotes}">
                        <div>
                                ${extNote.note} &nbsp;(<a href='/${extNote.publication.zdbID}'>1</a>)
                        </div>
                    </c:forEach>
                </c:if>
            </z:section>
        </z:section>

        <c:if test="${!formBean.genotype.wildtype}">
            <z:section title="${COMPOSITION}">
                <z:section title=" ">
                    <jsp:include page="genotype-composition.jsp"/>
                </z:section>
            </z:section>

            <z:section title="${FISH}" appendedText="utilizing ${genotype.name}">
                <z:section title=" ">
                    <jsp:include page="genotype-fish.jsp"/>
                </z:section>
            </z:section>

            <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
                <z:section title=" ">
                    <div class="__react-root" id="CitationTable" data-marker-id="${genotype.zdbID}"></div>
                </z:section>
            </z:section>

        </c:if>
    </jsp:body>

</z:dataPage>
