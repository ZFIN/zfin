<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="markerHistory" type="org.zfin.marker.MarkerHistory" scope="request"/>

<zfin2:dataManager zdbID="${markerHistory.zdbID}"
                   showLastUpdate="false"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${markerHistory.event} "/>
    </tiles:insertTemplate>
</div>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>

<zfin2:subsection title="Nomenclature"
                  showNoData="false" inlineTitle="true">
    Event for <zfin:link entity="${markerHistory.marker}"/>
    <p/>
    <table class="summary sortable">
        <th>New Symbol</th>
        <th>Event</th>
        <th>Old Symbol</th>
        <th>Date</th>
        <th>Reason</th>
        <th>Comments</th>
        <tr>
            <td><span class="genedom">${markerHistory.marker.abbreviation}</span></td>
            <td>${markerHistory.event.display}</td>
            <td><span class="genedom">${markerHistory.oldSymbol}</span></td>
            <td><fmt:formatDate value="${markerHistory.date}" pattern="yyyy-MM-dd"/></td>
            <td>${markerHistory.reason.toString()}
                <c:if test="${!empty markerHistory.attributions }">
                    <c:choose>
                        <c:when test="${markerHistory.attributions.size() ==1 }">
                            (<a href="/${markerHistory.attributions.iterator().next().publication.zdbID}">1</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href='/action/publication/list/${markerHistory.zdbID}'>${markerHistory.attributions.size()}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </td>
            <td>${markerHistory.comments}</td>
        </tr>
    </table>
</zfin2:subsection>