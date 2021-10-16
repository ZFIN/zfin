<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="PROTEINS" value="Fluorescent Proteins"/>
<c:set var="CONSTRUCTS" value="Constructs"/>

<z:dataPage sections="${[PROTEINS, CONSTRUCTS]}">
    <jsp:attribute name="entityName">
    </jsp:attribute>

    <jsp:body>
        <%--
                <z:dataManagerDropdown>
                    <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
                    <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${formBean.marker.zdbID}">Delete</a>
                    <div class="dropdown-divider"></div>
                    <a class="dropdown-item" href="/action/marker/efg/prototype-edit/${formBean.marker.zdbID}">Prototype Edit</a>
                </z:dataManagerDropdown>
        --%>
        <z:section title="${PROTEINS}">
            <z:section title="FPbase Proteins" infoPopup="/action/marker/note/mutants">
                <div class="__react-root" id="FluorescentProteinTable"></div>
            </z:section>
        </z:section>


        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
                <%--
                            <jsp:include page="home/WEB-INF/jsp/marker/efg/view-fluorescent-proteins.jsp"/>
                --%>
            fluo
        </div>

    </jsp:body>
</z:dataPage>

