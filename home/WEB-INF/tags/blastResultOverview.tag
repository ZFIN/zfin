<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastResults" type="org.zfin.sequence.blast.results.view.BlastResultBean" rtexprvalue="true" required="true" %>

<strong>Key</strong>
<img src="/images/E_letter.png" class="blast-key"> Gene Expression
<img src="/images/E_camera.png" class="blast-key"> with Images
<img src="/images/G_letter.png" class="blast-key"> GO Annotations
<img src="/images/P_letter.png" class="blast-key"> Phenotype Data
<img src="/images/P_camera.png" class="blast-key"> with Images

<img src="/images/warning-noborder.gif" alt="transcript withdrawn" width="20" height="20" align="top" class="blast-key" > Withdrawn
<table class="searchresults rowstripes">
    <%--<tr bgcolor="darkgray">--%>
    <tr>
        <th>Hit</th>
        <th>Score</th>
        <th>E value</th>
        <th>N</th>
        <th>Transcript<br>or Clone</th>
        <th>Associated<br> Genes</th>
        <th>Gene Data</th>
    </tr>


    <%--mostly there should be a single hit #--%>
    <c:forEach var="hit" items="${blastResults.hits}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${blastResults.hits}">
            <td>
                <zfin:link entity="${hit}"/>
            </td>
            <td>
                <a href="#${hit.hitNumber}">${hit.score}</a>
            </td>
            <td nowrap="nowrap">
                <c:choose>
                    <c:when test="${hit.EValue eq 0}">
                        0
                    </c:when>
                    <c:otherwise>
                        <fmt:formatNumber type="number" value="${hit.EValue}" pattern="0.#E0" />
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                    ${hit.NValue}
            </td>
            <td>
                <c:if test="${!hit.markerIsHit}">
                    <zfin:link entity="${hit.hitMarker}"/>
                    ${hit.withdrawn ? '<img src="/images/warning-noborder.gif" alt="transcript withdrawn" width="20" height="20" align="top"/>' : ""}
                </c:if>
            </td>
            <td>
                <zfin2:toggledHyperlinkList collection="${hit.genes}"
                                            id="relatedGenesOverview${hit.hitNumber}"
                                            maxNumber="1"
                                            showAttributionLinks="false"/>
                    <%--<zfin:link entity="${hit.genes}"/>--%>
            </td>
            <td valign="top">
                <zfin2:blastResultGeneData hit="${hit}"/>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>



