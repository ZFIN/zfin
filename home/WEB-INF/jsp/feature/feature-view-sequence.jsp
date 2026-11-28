<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>


<z:attributeList>
    <z:attributeListItem label="Flanking Sequence">
    <c:choose>
    <%-- ZFIN-10641: a Complex feature shows one sequence for all its variants, in genomic order --%>
    <c:when test="${!empty formBean.complexFlankingSequence}">
        <span style="word-wrap: break-word;"><c:forEach var="segment" items="${formBean.complexFlankingSequence.segments}"><c:choose><c:when test="${segment.variation}"><span style="color: red;">[<c:out value="${segment.sequence}"/>]</span></c:when><c:otherwise><c:out value="${segment.sequence}"/></c:otherwise></c:choose></c:forEach></span>
    </c:when>
    <c:otherwise>
    <z:ifHasData
            test="${!empty formBean.feature.featureGenomicMutationDetailSet && !(formBean.varSequence.vfsVariation.contains('null'))}"
            noDataMessage="None">

            <jsp:body>

                <span style="word-wrap: break-word;">${formBean.varSequence.vfsLeftEnd}
                <br>
                <span style="color: red;">
                        ${formBean.varSequence.vfsVariation}
                </span>
                <c:if test="${formBean.varSequence.vfsVariation.length()>450}">
                    <zfin2:toggleTextLength text="${formBean.varSequence.vfsRightEnd}"
                                            idName="${zfn:generateRandomDomID()}"
                                            shortLength="80"/>
                </c:if>
                <c:if test="${formBean.varSequence.vfsVariation.length()<450}">

                    ${formBean.varSequence.vfsRightEnd}
                </c:if>
                <c:if test="${empty formBean.varSequence.vfsVariation}">

                    ${formBean.varSequence.vfsRightEnd}
                </c:if>


            </jsp:body>

    </z:ifHasData>
    </c:otherwise>
    </c:choose>
    </z:attributeListItem>



        <z:attributeListItem label="Additional Sequence">
            <z:ifHasData test="${!empty formBean.genbankDbLinks}" noDataMessage="None">
            <c:forEach var="featureGenbankLink" items="${formBean.genbankDbLinks}" varStatus="loop">
                <zfin:link entity="${featureGenbankLink}"/>
                <c:if test="${featureGenbankLink.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${featureGenbankLink.publicationCount == 1}">
                            (<a href="/${featureGenbankLink.singlePublication.zdbID}">${featureGenbankLink.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="/action/infrastructure/data-citation-list/${featureGenbankLink.zdbID}">${featureGenbankLink.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
            </z:ifHasData>
        </z:attributeListItem>

</z:attributeList>