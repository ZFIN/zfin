<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${marker.zdbID}"
                   deleteURL="none"
                   rtype="marker"
                   showLastUpdate="true"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${marker.abbreviation} "/>
    </tiles:insertTemplate>
</div>


<zfin2:subsection title="Nomenclature"
                  test="${!empty marker.markerHistory }"
                  showNoData="true">
    <authz:authorize access="hasRole('root')">
        <span id="showAllEventsToggle"><a href="javascript:showAll(true)">Show All Events</a></span>
        <span id="showReducedEventsToggle" style="display: none"><a href="javascript:showAll(false)">Hide Naming
            Events</a></span>
    </authz:authorize>
    <table class="summary sortable">
        <th>New Symbol</th>
        <th>Event</th>
        <th>Old Symbol</th>
        <th>Date</th>
        <th>Reason</th>
        <th>Comments</th>
        <c:forEach var="link" items="${marker.markerHistory}" varStatus="loop">
            <c:if test="${link.eventType.toString() ne 'renamed'}">
                <tr id="reduced_${loop.index}">
                    <td><span class="genedom">${marker.abbreviation}</span></td>
                    <td>${link.eventType.display}</td>
                    <td><span class="genedom">${link.oldSymbol}</span></td>
                    <td><fmt:formatDate value="${link.date}" pattern="yyyy-MM-dd"/></td>
                    <td>${link.reason.toString()}</td>
                    <td>${link.comments}</td>
                </tr>
            </c:if>
        </c:forEach>
        <c:forEach var="link" items="${marker.markerHistory}" varStatus="loop">
            <tr style="display: none" id="all_${loop.index}">
                <td><span class="genedom">${marker.abbreviation}</span></td>
                <td>${link.eventType.display}</td>
                <td><span class="genedom"> ${link.oldSymbol}</span></td>
                <td><fmt:formatDate value="${link.date}" pattern="yyyy-MM-dd"/></td>
                <td>${link.reason.toString()}</td>
                <td>${link.comments}</td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>

<script>
    function showAll(all) {
        if (all) {
            jQuery("tr[id^='all_']").show();
            jQuery("tr[id^='reduced_']").hide();
            jQuery("#showAllEventsToggle").hide();
            jQuery("#showReducedEventsToggle").show();
        } else {
            jQuery("tr[id^='all_']").hide();
            jQuery("tr[id^='reduced_']").show();
            jQuery("#showAllEventsToggle").show();
            jQuery("#showReducedEventsToggle").hide();
        }
    }
</script>
