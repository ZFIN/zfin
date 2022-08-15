<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ABSTRACT" value="Abstract"/>

<c:set var="secs" value="${figureCaptions}"/>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
    </jsp:attribute>


    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/figure/all-figure-view/${publication.zdbID}">Old View</a>
            </nav>
        </authz:authorize>
    </jsp:attribute>

    <jsp:body>

        <c:forEach var="figure" items="${secs}">

        </c:forEach>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FIGURE SUMMARY</div>
            <z:attributeList>
                <z:attributeListItem label="Authors">
                    <zfin:link entity="${publication}"/>
                </z:attributeListItem>

                <z:attributeListItem label="Title">
                    ${publication.title}
                </z:attributeListItem>
            </z:attributeList>
        </div>

        <c:forEach var="figure" items="${figures}">
            <z:section title="${figure.label}">
                <zfin-figure:imagesAndCaption figure="${figure}" autoplayVideo="false" showMultipleMediumSizedImages="${showMultipleMediumSizedImages}" showCaption="false">

                    <zfin-figure:expressionSummary summary="${expressionSummaryMap[figure]}" suppressProbe="true"/>

                    <c:if test="${!empty expressionSummaryMap[figure].startStage}">
                        <div style="margin-top: 1em;">
                            <a href="/${figure.zdbID}#expDetail">Expression / Labeling details</a>
                        </div>
                    </c:if>

                    <zfin-figure:phenotypeSummary summary="${phenotypeSummaryMap[figure]}" />

                    <c:if test="${!empty phenotypeSummaryMap[figure].fish}">
                        <div style="margin-top: 1em;">
                            <a href="/${figure.zdbID}#phenoDetail">Phenotype details</a>
                        </div>
                    </c:if>
                    <zfin-figure:constructLinks figure="${figure}"/>

                </zfin-figure:imagesAndCaption>
            </z:section>
        </c:forEach>

    </jsp:body>

</z:dataPage>
