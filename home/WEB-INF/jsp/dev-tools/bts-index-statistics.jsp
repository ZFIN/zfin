<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">BTS Index Statistics</span></th>
    </tr>
</table>

<p/>
<span class="name-label">Single Letter Statistics</span>
<table class="summary">
    <tr>
        <th>Letter</th>
        <th>Number of matching Tokens</th>
    </tr>
    <c:forEach var="mapEntry" items="${statistics.sortedMapStatistics}">
        <tr>
            <td>
                    ${mapEntry.key}
            </td>
            <td>
                    ${mapEntry.value}
            </td>
        </tr>
    </c:forEach>
</table>

<p/>
<span class="name-label">All Tokens in BTS Index: ${fn:length(statistics.usedTokens)}</span>
<table class="summary">
    <tr>
        <th>Token</th>
    </tr>
    <c:forEach var="token" items="${statistics.usedTokens}" varStatus="index">
        <tr>
            <td>
                <a href="/action/fish/do-search?geneOrFeatureName=${token}">${token}</a>
            </td>
        </tr>
    </c:forEach>
</table>
