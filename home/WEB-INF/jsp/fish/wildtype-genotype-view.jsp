<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>
<c:set var="genotype" value="${formBean.genotype}"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="NOTE" value="Notes"/>

<z:dataPage sections="${[SUMMARY, NOTE]}">

<jsp:attribute name="entityName">
        ${fish.name}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/curation">Edit</a>
            <a class="dropdown-item" href="/action/genotype/view/${genotype.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">GENOTYPE</div>
            <h1>${genotype.name}</h1>
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
    </jsp:body>

</z:dataPage>
