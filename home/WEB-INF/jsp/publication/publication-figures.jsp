<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ABSTRACT" value="Abstract"/>
<c:set var="ACKNOWLEDGEMENT" value="Acknowledgments"/>

<c:set var="secs" value="${figureCaptions}"/>
<c:set var="UNPUBLISHED" value="${PublicationType.UNPUBLISHED}"/>

<z:dataPage sections="${secs}" additionalBodyClass="all-figures">

    <jsp:attribute name="entityName">
        <div data-toggle="tooltip" data-placement="bottom" title="${publication.citation}">
                ${publication.shortAuthorList}
        </div>
    </jsp:attribute>
    <jsp:attribute name="entityNameAddendum">
        <div style="font-size: 12px">
                ${publication.zdbID}
            <c:if test="${!empty publication.accessionNumber}"><br/>PMID:${publication.accessionNumber}</c:if>
        </div>
    </jsp:attribute>

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FIGURE SUMMARY</div>
            <z:attributeList>
                <z:attributeListItem label="Title">
                    <h4><a href="/${publication.zdbID}">${publication.title}</a></h4>
                </z:attributeListItem>
                <z:attributeListItem label="Authors">
                    ${publication.authors}
                </z:attributeListItem>
                <z:attributeListItem label="Source">
                    <zfin-figure:journalAbbrev publication="${publication}"/>
                </z:attributeListItem>
                <c:if test="${!empty submitters}">
                    <z:attributeListItem label="Submitted By">
                        <zfin:link entity="${submitters}"/>
                        <a style="margin-left: 5em"
                           class="citing-this-work-link"
                           href=/action/publication/search/printable?zdbID=${publication.zdbID}>(Citing this work)</a>
                    </z:attributeListItem>
                </c:if>
                <c:if test="${showThisseInSituLink}">
                    <z:attributeListItem label="Protocol">
                        <a class="thisse-protocol-link" href="/ZFIN/Methods/ThisseProtocol.html"><b>Thisse <i>in situ</i> hybridization protocol</b></a>
                    </z:attributeListItem>
                </c:if>
                <c:if test="${!empty probe}">
                    <z:attributeListItem label="Probe">
                        <zfin:link entity="${probe}"/>

                        <c:if test="${!empty probe.rating}">
                            &nbsp; <strong><a href="/zf_info/stars.html">Quality:</a></strong>
                            <zfin2:starRating rating="${probe.rating}" />
                        </c:if>
                    </z:attributeListItem>
                    <z:attributeListItem label="Supplier">
                        <c:if test="${!empty probeSuppliers}">
                            <tr>
                                <th>Supplier:</th>
                                <td><c:forEach var="supplier" items="${probeSuppliers}">
                                    ${supplier.linkWithAttributionAndOrderThis}
                                </c:forEach></td>
                            </tr>
                        </c:if>
                    </z:attributeListItem>
                </c:if>
            </z:attributeList>
        </div>

        <div class='mb-2'>
            <div class='btn-group btn-group-sm' role='group'>
                <button
                        type='button'
                        class='btn btn-outline-secondary ${allFiguresCssClass}'
                        onclick="location.href='/action/publication/${publication.zdbID}/all-figures?showDataOnly=false';"
                >
                    All Figures
                </button>
                <button
                        type='button'
                        class='btn btn-outline-secondary ${dataFiguresCssClass}'
                        onclick="location.href='/action/publication/${publication.zdbID}/all-figures?showDataOnly=true';"
                >
                    Figures with Data
                </button>
            </div>
        </div>

        <zfin2:pagination paginationBean="${pagination}" />

        <c:forEach var="figure" items="${figures}">
            <z:section title="${figure.label}" entity="${figure}">
                <zfin-figure:imagesAndCaptionPrototype
                        figure="${figure}"
                        autoplayVideo="false"
                        showMultipleMediumSizedImages="${showMultipleMediumSizedImages}"
                        showCaption="true">

                    <zfin-figure:expressionSummary summary="${expressionSummaryMap[figure]}" suppressProbe="true"/>

                    <c:if test="${!empty expressionSummaryMap[figure].startStage}">
                        <div style="margin-top: 1em;">
                            <a href="/${figure.zdbID}#expDetail">Expression / Labeling details</a>
                        </div>
                    </c:if>

                    <zfin-figure:phenotypeSummary summary="${phenotypeSummaryMap[figure]}"/>

                    <c:if test="${!empty phenotypeSummaryMap[figure].fish}">
                        <div style="margin-top: 1em;">
                            <a href="/${figure.zdbID}#phenoDetail">Phenotype details</a>
                        </div>
                    </c:if>
                    <zfin-figure:constructLinks figure="${figure}"/>

                </zfin-figure:imagesAndCaptionPrototype>
            </z:section>
        </c:forEach>

        <c:if test="${pagination.lastRecord < pagination.totalRecords}">
            <z:section title="More Figures...">
                <zfin2:pagination paginationBean="${pagination}" />
            </z:section>
        </c:if>
        <c:if test="${pagination.lastRecord >= pagination.totalRecords}">
            <zfin2:pagination paginationBean="${pagination}" />
        </c:if>

        <z:section title="${ACKNOWLEDGEMENT}">
            <c:choose>
                <c:when test="${publication.canShowImages && publication.type != UNPUBLISHED}">
                    <zfin2:acknowledgment-text hasAcknowledgment="${hasAcknowledgment}" showElsevierMessage="${showElsevierMessage}" publication="${publication}"/>
                </c:when>
                <c:otherwise>
                    <zfin2:subsection>
                        <zfin-figure:journalAbbrev publication="${publication}"/>
                    </zfin2:subsection>
                </c:otherwise>
            </c:choose>
        </z:section>


    </jsp:body>

</z:dataPage>
