<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="locations" required="true" type="java.util.Collection" %>
<%@ attribute name="gbrowseImage" required="false" type="org.zfin.genomebrowser.presentation.GenomeBrowserImage" %>

<c:choose>
    <c:when test="${empty locations}">
        <!-- no locations -->
    </c:when>
    <c:otherwise>

    <table id="meioticPanel" class="summary">
        <tr>
            <td>
            <!-- Mapping Details Genome Browser -->
            <c:choose>
                <c:when test="${empty gbrowseImage}">
                    <!-- Without Genome Browser -->
                </c:when>
                <c:when test="${gbrowseImage.type.toString() == 'JBrowse'}">
                    <!-- JBROWSE Genome Browser -->
                    <div class="jbrowse-image">
                        <zfin-gbrowse:genomeBrowserImageComponent image="${gbrowseImage}" />
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- GBROWSE Genome Browser -->
                    <style>
                        .gbrowse-image {
                            width: 800px;
                            margin: 0;
                        }
                    </style>

                    <div class="gbrowse-image"></div>

                    <script>
                        $(".gbrowse-image").gbrowseImage({
                            width: 700,
                            imageUrl: "${gbrowseImage.imageUrl}",
                            linkUrl: "${gbrowseImage.linkUrl}",
                            build: "${gbrowseImage.build}"
                        });
                    </script>
                </c:otherwise>
            </c:choose>
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
                            <th style="width: 500px">Assembly</th>
                        </tr>
                        <c:forEach var="genomeLocation" items="${locations}" varStatus="loop">
                            <zfin:alternating-tr loopName="loop">
                                <c:choose>
                                    <c:when test="${genomeLocation.source.displayName eq 'Direct Data Submission'}">
                                        <td nowrap>${genomeLocation.source.displayName}
                                        </td>
                                    </c:when>
                                    <c:when test="${genomeLocation.source.displayName ne 'Direct Data Submission' && genomeLocation.source.displayName eq 'ZFIN Gbrowse' && gbrowseImage.type eq 'JBROWSE'}">
<%--                                        TODO: update name of genomeLocation.source.displayName to ZFIN JBrowse in DB when we switch to JBrowse--%>
                                            <td nowrap><a href="${genomeLocation.url}">ZFIN JBrowse</a>
                                            </td>
                                    </c:when>
                                    <c:otherwise>
                                        <td nowrap><a href="${genomeLocation.url}">${genomeLocation.source.displayName}</a>
                                        </td>
                                    </c:otherwise>
                                </c:choose>

                                <td>${genomeLocation.chromosome}</td>
                                <td nowrap>
                                    <fmt:formatNumber value="${genomeLocation.start}" pattern="##,###"/> -
                                    <fmt:formatNumber value="${genomeLocation.end}" pattern="##,###"/>
                                </td>
                                <td>${genomeLocation.assembly}</td>

                            </zfin:alternating-tr>
                        </c:forEach>
                    </table>
                </div>
            </td>
        </tr>
    </table>
    </c:otherwise>
</c:choose>
