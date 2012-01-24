<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<table class="searchresults rowstripes">
    <tr>
        <th width="30%">Structure Name</th>
        <th>Synonyms</th>
    </tr>
    <c:forEach var="ao" items="${formBean.statisticItems}" varStatus="rowCounter">
        <zfin:alternating-tr loopName="rowCounter">
            <td>
                <c:if test="${ao.treeInfo != null}">
                <!-- This creates the visual level in the graph -->
                <span style="margin-left: <c:out value="${ao.indentationLevel}" />pt">
            </c:if>
            <a href='/action/anatomy/anatomy-view/<c:out value="${ao.zdbID}"/>'>
                    <%--<!-- Highlight the search term or the highlight term if provided -->--%>
                <zfin:highlight highlightEntity="${ao.anatomyItem.termName}"
                                highlightString="${formBean.highlightText}"/>
            </a>
            <c:if test="${ao.treeInfo != null}">
                </span>
                </c:if>
                <c:if test="${ao.anatomyItem.obsolete == true}">
                    (<span class="obsolete">obsolete</span>)
                </c:if>
            </td>
            <td>
                <c:if test="${ao.numberOfSynonyms > 0}">
                        <span class="anatomy-list-notes">
                            <!-- Highlight the search term or the highlight term if provided -->
                            <c:choose>
                                <c:when test="${formBean.stageSearch || formBean.termSearch} ">
                                    <zfin:highlight highlightEntity="${ao.formattedSynonymList}"
                                                    highlightString="${formBean.highlightText}"/>
                                </c:when>
                                <c:otherwise>
                                    <zfin:highlight highlightEntity="${ao.formattedSynonymList}"
                                                    highlightString="${formBean.searchTerm}"/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
