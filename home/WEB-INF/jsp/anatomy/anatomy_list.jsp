<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<table class="searchresults rowstripes">

    <c:if test="${!formBean.stageSearch}">

        <caption class="searchresults">
            <zfin:collectionSize collectionEntity="${formBean.statisticItems}"/>
            <zfin:choice collectionEntity="${formBean.statisticItems}" choicePattern="0# Results| 1# Result| 2# Results"
                         scope="Request"/>

            <!-- use only for term search. If all structures are listed leave it out -->
            <c:if test="${formBean.searchTerm != null}">
                for:<a>
                <c:out value="${formBean.searchTerm}"/>
                </a>
            </c:if>
        </caption>
    </c:if>
    <tr>
        <th width="30%">Structure Name</th>
        <th>Synonyms</th>
    </tr>
    <c:forEach var="ao" items="${formBean.sortedStatisticsItems}" varStatus="rowCounter">
        <zfin:alternating-tr loopName="rowCounter">
        <td>
            <c:if test="${ao.treeInfo != null}">
            <!-- This creates the visual level in the graph -->
                <span style="margin-left: <c:out value="${ao.indentationLevel}" />pt">
            </c:if>
            <a href='/action/anatomy/term-detail?anatomyItem.zdbID=<c:out value="${ao.zdbID}"/>'>
                <!-- Highlight the search term or the hightlight term if provided -->
                <c:choose>
                    <c:when test="${formBean.stageSearch || formBean.termSearch}">
                        <zfin:hightlight highlightEntity="${ao.anatomyItem.name}"
                                         highlightString="${formBean.highlightText}"/>
                    </c:when>
                    <c:otherwise>
                        <zfin:hightlight highlightEntity="${ao.anatomyItem.name}"
                                         highlightString="${formBean.searchTerm}"/>
                    </c:otherwise>
                </c:choose>
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
                            <!-- Highlight the search term or the hightlight term if provided -->
                            <c:choose>
                                <c:when test="${formBean.stageSearch || formBean.termSearch} ">
                                    <zfin:hightlight highlightEntity="${ao.formattedSynonymList}"
                                                     highlightString="${formBean.highlightText}"/>
                                </c:when>
                                <c:otherwise>
                                    <zfin:hightlight highlightEntity="${ao.formattedSynonymList}"
                                                     highlightString="${formBean.searchTerm}"/>
                                </c:otherwise>
                            </c:choose>
                        </span>
            </c:if>
        </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
