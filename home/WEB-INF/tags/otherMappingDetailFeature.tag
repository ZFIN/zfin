<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${isFeature && (featureHasMappingInfo ne 'true' || deletionMarkersPresent)}">
    <c:if test="${markerPresentList.size() > 0 || markerMissingList.size() > 0}">
        <table class="summary rowstripes">
            <tr>
                <td><span class="bold">Deletion Markers:</span></td>
            </tr>
            <c:if test="${markerPresentList.size() > 0}">
                <tr>
                    <td><span class="bold" style="margin-left: 10px">Present: </span>
                        <c:forEach var="marker" items="${markerPresentList}" varStatus="loop">
                            <zfin:link entity="${marker}"/>
                            <zfin2:displayLocation entity="${marker}" hideLink="true"/>
                            <c:if test="${!loop.last}">,</c:if>
                        </c:forEach>
                    </td>
                </tr>
            </c:if>
            <c:if test="${markerMissingList.size() > 0}">
                <tr>
                    <td><span class="bold" style="margin-left: 10px">Missing: </span>
                        <c:forEach var="marker" items="${markerMissingList}" varStatus="loop">
                            <zfin:link entity="${marker}"/>
                            <zfin2:displayLocation entity="${marker}" hideLink="true"/>
                            <c:if test="${!loop.last}">,</c:if>
                        </c:forEach>
                    </td>
                </tr>
            </c:if>
        </table>
    </c:if>
</c:if>