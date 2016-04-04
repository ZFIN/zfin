<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="locations" required="true" type="java.util.Collection" %>
<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="gbrowseImage" required="false" type="org.zfin.gbrowse.presentation.GBrowseImage" %>

<script src="/javascript/gbrowse-image.js"></script>

<style>
    .gbrowse-image {
        width: 800px;
        margin: 0;
    }
</style>

<c:if test="${not empty locations && not isClone}">
    <table id="meioticPanel" class="summary">
        <tr>
            <td>
                <div class="gbrowse-image" />
            </td>
        </tr>
        <tr>
            <td>
                <div style="margin: .0em; border: 1px solid black ; width: 800px">
                    <table style="text-align: left; width: 100%" class="rowstripes">
                        <tr>
                            <th style="width: 200px">Genome Browser</th>
                            <th style="width: 100px">Chr</th>
                            <th style="width: 500px">Position</th>
                                <%--
                                                        <th>Build</th>
                                                        <th>Version</th>
                                --%>
                        </tr>
                        <c:forEach var="genomeLocation" items="${locations}" varStatus="loop">
                            <zfin:alternating-tr loopName="loop">
                                <td nowrap><a
                                        href="${genomeLocation.url}">${genomeLocation.source.displayName}</a>
                                </td>
                                <td>${genomeLocation.chromosome}</td>
                                <td nowrap>
                                    <fmt:formatNumber value="${genomeLocation.start}" pattern="##,###"/> -
                                    <fmt:formatNumber value="${genomeLocation.end}" pattern="##,###"/>
                                </td>
                                <%--
                                                            <td></td>
                                                            <td></td>
                                --%>
                            </zfin:alternating-tr>
                        </c:forEach>
                    </table>
                </div>
            </td>
        </tr>
    </table>

    <script>
        jQuery(".gbrowse-image").gbrowseImage({
            width: 700,
            imageUrl: "${gbrowseImage.imageUrl}",
            linkUrl: "${gbrowseImage.linkUrl}",
            build: "${gbrowseImage.build}"
        });
    </script>
</c:if>

<c:if test="${mappedClones.size() > 0}">
    <table class="summary rowstripes">
        <tr>
            <th colspan="2">
                Mapped Clones containing <zfin:abbrev entity="${marker}"/>
            </th>
        </tr>
        <c:forEach var="clone" items="${mappedClones}">
            <tr>
                <td width="10%">
                    <zfin:link entity="${clone}"/>
                </td>
                <td>
                    <zfin2:displayLocation entity="${clone}"/>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>
