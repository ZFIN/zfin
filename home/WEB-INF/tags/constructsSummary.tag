<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" required="true" %>
<%@ attribute name="constructBeans" type="java.util.Collection" required="true" %>

<style>
    .marker-go-table td, .marker-go-table th { line-height: 1.5em; }
</style>

<%-- Should always have atleast one sequence, so won't ever hide --%>
<zfin2:subsection title="CONSTRUCTS WITH SEQUENCES FROM <i>${gene.abbreviation}</i>" showNoData="true" showEditLink="false" test="${!empty constructBeans}">
    <table class="summary rowstripes marker-go-table">
        <tr>
            <th width="24%">Construct</th>
            <th width="18%">Regulatory Regions</th>
            <th width="18%">Coding Sequences</th>
            <th width="20%">Species</th>
            <th width="10%">Tg Lines</th>
            <th width="10%">Publications</th>
        </tr>
        <c:forEach var="constructBean" items="${constructBeans}" varStatus="loop">
            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td>
                    <zfin:link entity="${constructBean.marker}"/>
                </td>
                <td>
                    <c:if test="${not empty constructBean.regulatoryRegions}">
                        <c:forEach var="reg" items="${constructBean.regulatoryRegions}" varStatus="rloop">
                            <zfin:link entity="${reg}"/><c:if test="${!rloop.last}">,&nbsp;</c:if>
                        </c:forEach>
                    </c:if>
                </td>
                <td>
                    <c:if test="${not empty constructBean.codingSequences}">
                        <c:forEach var="codingSeq" items="${constructBean.codingSequences}" varStatus="cloop">
                            <zfin:link entity="${codingSeq}"/><c:if test="${!cloop.last}">,&nbsp;</c:if>
                        </c:forEach>
                    </c:if>
                </td>
                <td>
                    <c:forEach var="species" items="${constructBean.species}" varStatus="sloop">
                        <span title="${species.cvForeignSpecies}"><i>${species.cvNameDefinition}</i></span><c:if test="${!sloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                </td>
                <td>
                    <a href="/search?category=Mutation+/+Tg&q=&fq=xref:${constructBean.marker.zdbID}">${constructBean.numberOfTransgeniclines}</a>
                </td>
                <td style="vertical-align: text-top; text-align: right">
                    <a href="/action/marker/citation-list/${constructBean.marker.zdbID}">${constructBean.numPubs}</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>



