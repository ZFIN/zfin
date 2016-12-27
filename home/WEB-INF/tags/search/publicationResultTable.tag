<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<c:set var="abstrct" value="<%= ResultService.ABSTRACT %>"/>
<c:set var="authors" value="<%= ResultService.AUTHORS %>"/>
<c:set var="journal" value="<%= ResultService.JOURNAL %>"/>

<table class="table-results searchresults" style="display: none;">
    <tr>
        <th>Authors</th>
        <th>Title</th>
        <th>Journal</th>
        <th>ID</th>
        <th>Related</th>
        <th>Figures</th>
    </tr>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <tr class="${loop.index % 2 == 0 ? 'even' : 'odd'}">
            <td>${result.attributes[authors]}</td>
            <td>
                ${result.link}
                <div>
                    <a href class="small abstract-expand-button" data-index="${loop.index}">
                        <i class="fa fa-fw fa-caret-right icon-toggle"></i> Abstract
                    </a>
                </div>
            </td>
            <td>${result.attributes[journal]}</td>
            <td class="nowrap">${result.id}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
            <td>
                <c:if test="${not empty result.image}">
                    <zfin-search:imageModal result="${result}"/>
                </c:if>
            </td>
        </tr>
        <tr class="${loop.index % 2 == 0 ? 'even' : 'odd'} hidden" id="abstract-row-${loop.index}">
            <td colspan="6">
                ${result.attributes[abstrct]}
            </td>
        </tr>
    </c:forEach>
</table>

<script>
    $(function () {
        $('.abstract-expand-button').click(function (elm) {
            elm.preventDefault();
            var idx = $(this).data('index');
            $(this).find('.icon-toggle').toggleClass('open');
            $('#abstract-row-' + idx).toggleClass('hidden');
        });
    });
</script>