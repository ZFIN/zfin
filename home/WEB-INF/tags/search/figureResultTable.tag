<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<table class="table-results searchresults" style="display: none;">
    <thead>
    <tr>
        <th>Publication</th>
        <th></th>
        <th>Figure</th>
        <th></th>
        <th>ID</th>
        <th>Related</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <tr class="${loop.index % 2 == 0 ? 'even' : 'odd'}">
            <td><zfin:link entity="${result.entity.publication}"/></td>
            <td>${result.attributes['Publication:']}</td>
            <td>
                <zfin:link entity="${result.entity}"/>
            </td>
            <td>
                <c:if test="${not empty result.image}">
                    <zfin-search:imageModal result="${result}"/>
                </c:if>
                <div class="clearfix">
                    <a href class="small caption-expand-button nowrap" data-index="${loop.index}">
                        <i class="fa fa-fw fa-caret-right icon-toggle"></i> Caption
                    </a>
                </div>
            </td>
            <td class="nowrap">${result.id}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </tr>
        <tr class="${loop.index % 2 == 0 ? 'even' : 'odd'} hidden" id="caption-row-${loop.index}">
            <td colspan="6">
                ${result.attributes['Caption:']}
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<script>
    $(function () {
        $('.caption-expand-button').click(function (elm) {
            elm.preventDefault();
            var idx = $(this).data('index');
            $(this).find('.icon-toggle').toggleClass('open');
            $('#caption-row-' + idx).toggleClass('hidden');
        });
    });
</script>