<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>


<z:attributeList>
    <z:attributeListItem label="Flanking Sequence">
        <jsp:body>
        <z:ifHasData test="${!empty formBean.feature.featureGenomicMutationDetailSet && !(formBean.varSequence.vfsVariation.contains('null'))}"
                     noDataMessage="None">
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

        </z:ifHasData>
        </jsp:body>
    </z:attributeListItem>

    <z:attributeListItem label="Sequence">
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

    </z:attributeListItem>
</z:attributeList>