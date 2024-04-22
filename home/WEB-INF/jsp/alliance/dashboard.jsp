<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="GENE" value="Gene"/>
<c:set var="FISH" value="Fish"/>

<z:dataPage sections="${[FISH]}" title="Alliance Data Submission">

        <jsp:attribute name="entityName">
       Alliance Data
    </jsp:attribute>

    <jsp:body>
        <div id="${zfn:makeDomIdentifier(FISH)}">
            <div class="small text-uppercase text-muted">Submission Dashboard</div>
            <h1>Alliance Data</h1>
        </div>

        <z:section title="${FISH}">
            <z:attributeList>
                <z:attributeListItem label="Fish in Alliance and not ZFIN:" copyable="true">
                    ${fn:length(differencesAlliance)}
                </z:attributeListItem>
                <z:attributeListItem label="Fish in ZFIN and not Alliance:" copyable="true">
                    ${fn:length(differencesZfin)}
                </z:attributeListItem>
            </z:attributeList>
            Fish in Alliance and not ZFIN: <span> ${differencesAlliance}</span>
            <div></div>
            Fish in ZFIN and not Alliance: <span> ${differencesZfin}</span>
        </z:section>
    </jsp:body>

</z:dataPage>
