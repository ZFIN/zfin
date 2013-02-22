<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<TABLE width="98%">
    <TR>
        <TD>
            <c:choose>
                <c:when test="${query == ''}">
                    All Anatomical Terms in alphabetical order:
                </c:when>
                <c:otherwise>
                    <table width="100%">
                        <tr>
                            <td>
                                <c:if test="${histogram != null && histogram.size() > 1}">
                                    <table>
                                        <c:forEach var="mapEntry" items="${histogram}" varStatus="index">
                                            <tr>
                                                <td align="right">
                                                    <a href="?name=${query}*&ontologyName=${mapEntry.key.ontologyName}">
                                                            ${mapEntry.value}
                                                    </a>
                                                </td>
                                                <td> ${mapEntry.key.displayName} </td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </c:if>
                            </td>
                            <td>
                                <zfin:collectionSize collectionEntity="${terms}"/>
                                <zfin:choice collectionEntity="${terms}" choicePattern="0# Terms| 1# Term| 2# Terms"
                                             scope="Request"/>
                                for: <span style="font-weight: bold;"> ${query} </span>
                            </td>
                        </tr>
                    </table>
                </c:otherwise>
            </c:choose>
            <HR width=500 size=1 noshade align=left>
            <table class="searchresults rowstripes">
                <tr>
                    <th width="30%">Term Name</th>
                    <th>Ontology</th>
                    <th>Synonyms</th>
                </tr>
                <c:forEach var="term" items="${terms}" varStatus="rowCounter">
                    <zfin:alternating-tr loopName="rowCounter">
                        <td>
                            <a href='/action/ontology/term-detail/<c:out value="${term.oboID}"/>'>
                            </a>
                            <zfin:link entity="${term}"/>
                        </td>
                        <td>${term.ontology.displayName}</td>
                        <td>
                            <c:if test="${fn:length(term.aliases) > 0}">
                                <c:forEach var="alias" items="${term.aliases}" varStatus="index">
                                    <zfin:highlight highlightEntity="${alias}"
                                                    highlightString="${query}"/>
                                    <c:if test="${!index.last}">,</c:if>
                                </c:forEach>
                            </c:if>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </TD>
    </TR>
</TABLE>

