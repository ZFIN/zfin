<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="FPBASE_PROTEINS" value="FPbase Proteins"/>
<c:set var="EFGs" value="EFGs"/>
<c:set var="CONSTRUCTS" value="Constructs"/>

<z:dataPage sections="${[FPBASE_PROTEINS, EFGs, CONSTRUCTS]}">
    <jsp:attribute name="entityName">
    </jsp:attribute>

    <jsp:body>
        <z:section title="${FPBASE_PROTEINS}">
            <z:section infoPopup="/action/marker/note/mutants">
                <div class="__react-root" id="FluorescentProteinTable"></div>
            </z:section>
        </z:section>

        <z:section title="${EFGs}">
            <z:section infoPopup="/action/marker/note/mutants">
                <div class="__react-root" id="EfgTable" data-type="efg"></div>
            </z:section>
        </z:section>

        <z:section title="${CONSTRUCTS}">
            <z:section infoPopup="/action/marker/note/mutants">
                <div class="__react-root" id="EfgTable" data-type="construct"></div>
            </z:section>
        </z:section>


    </jsp:body>
</z:dataPage>

